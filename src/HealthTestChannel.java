import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class HealthTestChannel implements Runnable {

    private final Channel ch;
    private final long intervalMs;

    private final AtomicLong lastPong = new AtomicLong(System.currentTimeMillis());
    private volatile boolean running = true;

    public HealthTestChannel(Channel ch) {
        this(ch, 1000);
    }

    public HealthTestChannel(Channel ch, long intervalMs) {
        this.ch = ch;
        this.intervalMs = intervalMs;
    }

    public void notifyPong() {
        lastPong.set(System.currentTimeMillis());
    }

    public boolean isHealthy() {
        return ch.isOpen() &&
                (System.currentTimeMillis() - lastPong.get()) <= intervalMs * 5;
    }

    @Override
    public void run() {
        sleep(300); // deja arrancar reader

        while (running && ch.isOpen()) {
            try {
                ch.send(new DataFrame(DataFrameType.PING, null));
            } catch (IOException e) {
                break;
            }

            sleep(intervalMs);
        }
    }

    public void stop() { running = false; }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
