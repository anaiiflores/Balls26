import javax.swing.*;
import java.util.List;

public class GameController implements Runnable {

    private final GameModel model;
    private final GameView view;
    private final NetworkManager net;

    private volatile boolean running = true;

    public GameController(GameModel model, GameView view, NetworkManager net) {
        this.model = model;
        this.view = view;
        this.net = net;
    }

    @Override
    public void run() {
        long last = System.currentTimeMillis();

        while (running) {
            long now = System.currentTimeMillis();
            long dt = now - last;
            last = now;

            int w = view.getWidth();
            int h = view.getHeight();
            if (w <= 0 || h <= 0) { sleep(10); continue; }

            // 1) ✅ aplicar lo que llegó por red (DTOs)
            net.drainInbox(model, w, h);

            // 2) ✅ actualizar el modelo (movimiento + explosiones + spawn local si toca)
            model.update(dt, w, h);

            // 3) ✅ usamos snapshot para iterar (NO lista real)
            List<Walker> snapshot = model.snapshotWalkers();

            // 4) revisar bordes y decidir: transferir o explotar
            for (Walker wk : snapshot) {

                if (wk.getState() != Walker.State.WALKING) continue;

                Walker.Side side = wk.hitWhichSide(w, h);
                if (side == Walker.Side.NONE) continue;

                // Si sale por IZQ/DER => enviar al otro si hay conexión
                if ((side == Walker.Side.LEFT || side == Walker.Side.RIGHT) && net.isConnected()) {

                    // evita spam: solo una vez
                    if (wk.isTransferred()) continue;
                    wk.markTransferred();

                    WalkerDTO.Side entry = (side == Walker.Side.LEFT)
                            ? WalkerDTO.Side.RIGHT
                            : WalkerDTO.Side.LEFT;

                    WalkerDTO dto = new WalkerDTO(
                            wk.getId(),
                            wk.getX(),
                            wk.getY(),
                            wk.getVx(),
                            wk.getVy(),
                            entry
                    );

                    // ✅ manda DTO
                    net.sendTransfer(dto);

                    // ✅ quita del modelo local (ya no lo gestionas aquí)
                    model.removeById(wk.getId());

                } else {
                    // sale por ARRIBA/ABAJO o no hay conexión => explota aquí
                    // (ojo: explota "en el modelo", wk viene del snapshot pero apunta al mismo objeto)
                    wk.explode();
                }
            }

            // 5) repintar UI en EDT
            SwingUtilities.invokeLater(view::repaint);

            sleep(16); // ~60 FPS
        }
    }

    public void stop() { running = false; }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
