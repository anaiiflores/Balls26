import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class Channel implements Runnable {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private Consumer<WalkerDTO> onTransfer;
    private HealthTestChannel health;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;

        // ⚠️ ORDEN CRÍTICO
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    // ===== CONFIGURACIÓN =====

    public void setOnTransfer(Consumer<WalkerDTO> onTransfer) {
        this.onTransfer = onTransfer;
    }

    public void attachHealth(HealthTestChannel health) {
        this.health = health;
    }

    public boolean isOpen() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // ===== ENVÍO =====

    public synchronized void send(DataFrame df) throws IOException {
        out.writeObject(df);
        out.flush();
        out.reset(); // evita caché
    }

    // ===== RECEPCIÓN (THREAD) =====

    @Override
    public void run() {
        try {
            while (isOpen()) {
                Object obj = in.readObject();

                if (!(obj instanceof DataFrame df)) continue;

                switch (df.type) {

                    case TRANSFER_WALKER -> {
                        if (df.payload instanceof WalkerDTO dto && onTransfer != null) {
                            onTransfer.accept(dto);
                        }
                    }

                    case PING -> {
                        send(new DataFrame(DataFrameType.PONG, null));
                    }

                    case PONG -> {
                        if (health != null) health.notifyPong();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[CHANNEL] closed");
        } finally {
            close();
        }
    }

    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
