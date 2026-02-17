import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class HealthTestChannel implements Runnable {
    private final Channel ch;
    private final long intervalMs;

    private final AtomicLong lastPong = new AtomicLong(System.currentTimeMillis());
    private volatile boolean running = true;
    private volatile boolean healthy = true;

    public HealthTestChannel(Channel ch, long intervalMs) {
        this.ch = ch;
        this.intervalMs = intervalMs;
    }

    public void notifyPong() {
        lastPong.set(System.currentTimeMillis());
        healthy = true;
    }

    public boolean isHealthy() {
        long diff = System.currentTimeMillis() - lastPong.get();
        return ch.isOpen() && diff <= intervalMs * 5 && healthy;
    }

    @Override
    public void run() {
        sleep(300);

        while (running && ch.isOpen()) {
            try {
                ch.sendMsg(new MsgDTO(1, 0, null)); // PING
            } catch (IOException e) {
                healthy = false;
                break;
            }

            long diff = System.currentTimeMillis() - lastPong.get();
            if (diff > intervalMs * 5) {
                healthy = false;
            }

            sleep(intervalMs);
        }
    }

    public void stop() { running = false; }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
