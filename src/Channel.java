import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Channel {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private final AtomicBoolean readerStarted = new AtomicBoolean(false);

    private HealthTestChannel health;
    private Consumer<WalkerDTO> onTransfer;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;

        // SIEMPRE: OUT -> flush -> IN
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in  = new ObjectInputStream(socket.getInputStream());
    }

    public void setOnTransfer(Consumer<WalkerDTO> onTransfer) { this.onTransfer = onTransfer; }
    public void attachHealth(HealthTestChannel health) { this.health = health; }

    public synchronized void sendMsg(MsgDTO msg) throws IOException {
        out.writeObject(msg);
        out.flush();
        out.reset();
    }

    public boolean isOpen() {
        return socket != null && socket.isConnected() && !socket.isClosed()
                && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }

    private void process(MsgDTO msg) throws IOException {
        switch (msg.code) {
            case 0 -> { // TRANSFER
                if (msg.hasObject == 1 && msg.payload instanceof WalkerDTO dto) {
                    if (onTransfer != null) onTransfer.accept(dto);
                }
            }
            case 1 -> sendMsg(new MsgDTO(2, 0, null)); // PING -> PONG
            case 2 -> { if (health != null) health.notifyPong(); } // PONG
        }
    }

    public void startReader(Consumer<Exception> onFail) {
        if (!readerStarted.compareAndSet(false, true)) {
            System.out.println("[CH] Reader already started. Ignoring.");
            return;
        }

        Thread t = new Thread(() -> {
            try {
                while (isOpen()) {
                    Object obj = in.readObject(); // SOLO AQUÍ SE LEE
                    if (obj instanceof MsgDTO msg) process(msg);
                    else System.out.println("[CH] Unknown incoming: " + obj);
                }
            } catch (Exception ex) {
                onFail.accept(ex);
            } finally {
                close();
            }
        }, "CH-Reader");

        t.setDaemon(false);
        t.start();
    }
}
