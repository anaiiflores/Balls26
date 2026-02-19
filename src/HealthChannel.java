

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class HealthChannel implements Runnable {

    private final Channel ch;
    private final long intervalMs;

    private final AtomicLong lastPong = new AtomicLong(System.currentTimeMillis());
    private volatile boolean running = true;

    public HealthChannel(Channel ch, long intervalMs) {
        this.ch = ch;
        this.intervalMs = intervalMs;
    }

    public void notifyPong() {
        lastPong.set(System.currentTimeMillis());
    }

    public boolean isHealthy() {
        long diff = System.currentTimeMillis() - lastPong.get();
        return ch.isOpen() && diff <= intervalMs * 5;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        sleep(300); // deja arrancar el reader

        while (running && ch.isOpen()) {
            try {
                ch.send(new MsgDTO(1, 0, null)); // PING
            } catch (IOException e) {
                break;
            }

            // si pasan muchos intervalos sin PONG, cerramos para forzar reconexión
            if (!isHealthy()) {
                System.out.println("[HEALTH] timeout -> closing channel");
                ch.close();
                break;
            }

            sleep(intervalMs);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
