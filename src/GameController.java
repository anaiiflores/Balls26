
import javax.swing.*;

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
            net.drainInbox(model, w);

            model.update(dt, w, h);

            // revisar bordes y decidir: transferir (izq/der) o explotar (arriba/abajo)
            for (Walker wk : model.getWalkers().toArray(new Walker[0])) {
                if (wk.getState() != Walker.State.WALKING) continue;

                Walker.Side side = wk.hitWhichSide(w, h);
                if (side == Walker.Side.NONE) continue;

                if ((side == Walker.Side.LEFT || side == Walker.Side.RIGHT) && net.isConnected()) {
                    // transfer
                    WalkerDTO.Side entry = (side == Walker.Side.LEFT) ? WalkerDTO.Side.RIGHT : WalkerDTO.Side.LEFT;

                    // y: mantenemos altura, x se ajusta al entrar
                    WalkerDTO dto = new WalkerDTO(
                            wk.getId(),
                            wk.getX(),
                            wk.getY(),
                            wk.getVx(),
                            wk.getVy(),
                            entry
                    );
                    net.sendTransfer(dto);
                    model.removeById(wk.getId());

                } else {
                    // si no hay conexión o es TOP/BOTTOM: explota
                    wk.explode();
                }
            }

            SwingUtilities.invokeLater(view::repaint);
            sleep(16);
        }
    }

    private void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
