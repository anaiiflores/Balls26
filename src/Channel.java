
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
//se envian objetos de tipo frame, 2 tipos de comunicainoes y el de arriba, framecom, frame es app...
public class Channel {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private volatile boolean running = true;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;

        // orden correcto
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void startReader(Consumer<DataFrame> onMessage, Consumer<Exception> onFail) {
        Thread reader = new Thread(() -> {
            while (running && isOpen()) {
                try {
                    Object obj = in.readObject(); // BLOQUEA -> hilo dedicado
                    if (obj instanceof DataFrame df) {
                        onMessage.accept(df);
                    }
                } catch (Exception e) {
                    running = false;
                    onFail.accept(e);
                    close();
                }
            }
        }, "CH-Reader");
        reader.start();
    }

    public synchronized void send(DataFrame df) throws IOException {
        out.writeObject(df);
        out.flush();
    }

    public boolean isOpen() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void close() {
        running = false;
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
