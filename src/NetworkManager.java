
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private Channel ch;
    private HealthTestChannel health;

    // Cola de mensajes recibidos (thread-safe)
    private final Queue<DataFrame> inbox = new ConcurrentLinkedQueue<>();

    public void attachChannel(Channel ch) {
        this.ch = ch;
        this.health = new HealthTestChannel(ch, 400);
        new Thread(health, "Health").start();
    }

    public boolean isConnected() {
        return ch != null && health != null && health.isHealthy();
    }

    // CH-Reader llama a esto: NO TOCA el modelo
    public void onIncoming(DataFrame df) {
        try {
            if (df.type == DataFrameType.PING) {
                ch.send(new DataFrame(DataFrameType.PONG, "ok"));
                return;
            }
            if (df.type == DataFrameType.PONG) {
                health.notifyPong();
                return;
            }
            // resto a la cola
            inbox.add(df);
        } catch (IOException e) {
            ch.close();
        }
    }

    // GameController llama a esto (hilo del juego)
    public void drainInbox(GameModel model, int screenW) {
        DataFrame df;
        while ((df = inbox.poll()) != null) {
            if (df.type == DataFrameType.TRANSFER_WALKER && df.payload instanceof WalkerDTO dto) {
                model.spawnFromNetwork(dto, screenW);
            }
        }
    }

    public void sendTransfer(WalkerDTO dto) {
        if (!isConnected()) return;
        try {
            ch.send(new DataFrame(DataFrameType.TRANSFER_WALKER, dto));
        } catch (IOException e) {
            ch.close();
        }
    }
}
