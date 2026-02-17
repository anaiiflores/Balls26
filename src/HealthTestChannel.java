import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class HealthTestChannel implements Runnable {
    private final Channel ch;
    private final long intervalMs;

    private final AtomicLong lastPong = new AtomicLong(System.currentTimeMillis());
    private volatile boolean running = true;
    private volatile boolean healthy = true; // ✅ estado interno

    public HealthTestChannel(Channel ch, long intervalMs) {
        this.ch = ch;
        this.intervalMs = intervalMs;
    }

    public void notifyPong() {
        lastPong.set(System.currentTimeMillis());
        healthy = true;
    }

    public boolean isHealthy() {
        // ✅ ya NO cierra socket, solo informa estado
        long diff = System.currentTimeMillis() - lastPong.get();
        return ch.isOpen() && diff <= intervalMs * 5 && healthy;
    }

    @Override
    public void run() {
        sleep(300); // ✅ da tiempo a arrancar readers en ambos lados

        while (running && ch.isOpen()) {
            try {
                System.out.println("[HEALTH] send PING");
                ch.send(new DataFrame(DataFrameType.PING, "health"));
            } catch (IOException e) {
                System.out.println("[HEALTH] send failed (no cierro socket)");
                healthy = false;
                // no cierres: break
                break;
            }

            long diff = System.currentTimeMillis() - lastPong.get();
            if (diff > intervalMs * 5) {
                System.out.println("[HEALTH] no PONG (" + diff + "ms) -> unhealthy (no cierro socket)");
                healthy = false;
                // seguimos, por si se recupera
            }

            sleep(intervalMs);
        }
    }

    public void stop() { running = false; }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
