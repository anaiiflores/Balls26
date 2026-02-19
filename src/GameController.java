
import javax.swing.*;

public class GameController implements Runnable {

    private final GameModel model;
    private final GameView view;
    private final NetworkManager net;


    public GameController(GameModel model, GameView view, NetworkManager net) {
        this.model = model;
        this.view = view;
        this.net = net;
    }

    @Override
    public void run() {
        long last = System.currentTimeMillis();

        while (true) {
            long now = System.currentTimeMillis();
            long dt = now - last;
            last = now;

            int w = view.getWidth();
            int h = view.getHeight();
            if (w <= 0 || h <= 0) { sleep(10); continue; }

            // 1) lo recibido por red entra al modelo
            net.drainInbox(model, w, h);

            // 2) actualiza walkers
            model.update(dt, w, h);

            // 3) si sale por LEFT o RIGHT, lo mando y lo elimino local
            for (Walker wk : model.snapshotWalkers()) {
                if (wk.getState() != Walker.State.WALKING) continue;

                Walker.Side side = wk.hitWhichSide(w, h);
                if (side == Walker.Side.LEFT || side == Walker.Side.RIGHT) {

                    if (!net.isConnected()) continue;
                    if (wk.isTransferred()) continue;

                    wk.markTransferred();

                    WalkerDTO.Side entry = (side == Walker.Side.LEFT)
                            ? WalkerDTO.Side.RIGHT
                            : WalkerDTO.Side.LEFT;

                    double yNorm = wk.getY() / (double) h;
                    yNorm = Math.max(0.0, Math.min(1.0, yNorm));

                    WalkerDTO dto = new WalkerDTO(wk.getId(), yNorm, wk.getVx(), wk.getVy(), entry);

                    net.sendTransfer(dto);
                    wk.markTransferred();
                    model.removeById(wk.getId());

                }
            }

            SwingUtilities.invokeLater(view::repaint);
            sleep(16);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
