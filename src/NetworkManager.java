import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {

    private Channel ch;
    private HealthTestChannel health;
    private Thread healthThread;

    private final Queue<WalkerDTO> inbox = new ConcurrentLinkedQueue<>();

    public synchronized void attachChannel(Channel ch) {
        // si ya hay uno, lo cierro
        detachAndClose();

        this.ch = ch;

        // health
        this.health = new HealthTestChannel(ch, 1000);
        ch.attachHealth(health);

        // cuando llegue un transfer, lo encolo
        ch.setOnTransfer(dto -> {
            System.out.println("[NET] INCOMING TRANSFER id=" + dto.id);
            inbox.add(dto);
        });

        // ⚠️ IMPORTANTE: el reader debe arrancarse aquí (porque ya no hay onIncoming DataFrame)
        ch.startReader(ex -> {
            System.out.println("[CH fail]");
            ex.printStackTrace();
            detachAndClose();
        });
    }

    public synchronized void startHealth() {
        if (health == null) return;

        // evita arrancar 2 veces
        if (healthThread != null && healthThread.isAlive()) return;

        health.notifyPong(); // arranca como si hubiera pong reciente
        healthThread = new Thread(health, "Health");
        healthThread.setDaemon(false);
        healthThread.start();
    }

    public boolean isConnected() {
        return ch != null && ch.isOpen();
    }

    public boolean isHealthy() {
        return health != null && health.isHealthy();
    }

    public void drainInbox(GameModel model, int screenW, int screenH) {
        WalkerDTO dto;
        while ((dto = inbox.poll()) != null) {
            model.spawnFromNetwork(dto, screenW, screenH);
        }
    }

    public void sendTransfer(WalkerDTO dto) {
        if (!isConnected()) return;

        try {
            ch.sendMsg(new MsgDTO(0, 1, dto));
        } catch (IOException e) {
            System.out.println("[NET] sendTransfer IOException -> closing");
            e.printStackTrace();
            detachAndClose();
        }
    }

    public synchronized void detachAndClose() {
        try { if (health != null) health.stop(); } catch (Exception ignored) {}

        // intenta parar el hilo de health de forma limpia
        try { if (healthThread != null) healthThread.interrupt(); } catch (Exception ignored) {}

        try { if (ch != null) ch.close(); } catch (Exception ignored) {}

        ch = null;
        health = null;
        healthThread = null;
        inbox.clear();
    }
}
