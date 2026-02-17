import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private Channel ch;
    private HealthTestChannel health;
    private Thread healthThread;

    private final Queue<DataFrame> inbox = new ConcurrentLinkedQueue<>();

    public void attachChannel(Channel ch) {
        this.ch = ch;
        this.health = new HealthTestChannel(ch, 1000);
        // ❌ NO arrancar aquí el health
    }

    // ✅ LLÁMALO SOLO cuando el reader ya está arrancado
    public void startHealth() {
        health.notifyPong(); // ✅ arranca "como si hubiera un pong reciente"
        healthThread = new Thread(health, "Health");
        healthThread.start();

    }

    public boolean isConnected() {
        return ch != null && ch.isOpen();
    }

    // (opcional) si quieres ver salud aparte:
    public boolean isHealthy() {
        return health != null && health.isHealthy();
    }


    public void onIncoming(DataFrame df) {
        System.out.println("[NET] INCOMING " + df.type);

        try {
            if (df.type == DataFrameType.PING) {
                ch.send(new DataFrame(DataFrameType.PONG, "ok"));
                return;
            }
            if (df.type == DataFrameType.PONG) {
                health.notifyPong();
                return;
            }
            inbox.add(df);
        } catch (IOException e) {
            System.out.println("[NET] onIncoming IOException -> closing");
            e.printStackTrace();
            ch.close();
        }

    }


    public void drainInbox(GameModel model, int screenW, int screenH) {
        DataFrame df;
        while ((df = inbox.poll()) != null) {
            if (df.type == DataFrameType.TRANSFER_WALKER && df.payload instanceof WalkerDTO dto) {
                model.spawnFromNetwork(dto, screenW, screenH);
            }
        }
    }

    public void sendTransfer(WalkerDTO dto) {
        if (!isConnected()) return;
        try {
            ch.send(new DataFrame(DataFrameType.TRANSFER_WALKER, dto));
        } catch (IOException e) {
            System.out.println("[NET] sendTransfer IOException -> closing");
            e.printStackTrace();
            ch.close();
        }

    }
}
