import java.io.*;
import java.net.Socket;

public class Channel {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;

        // ✅ IMPORTANTE: primero OUT, flush, luego IN
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized void send(Object obj) throws IOException {
        out.writeObject(obj);
        out.flush();
        out.reset(); // ✅ evita cache raro de objetos
    }

    public Object receive() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public void close() {
        try { socket.close(); } catch (Exception ignored) {}
    }

    public boolean isOpen() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }


    public void startReader(java.util.function.Consumer<DataFrame> onMsg,
                            java.util.function.Consumer<Exception> onFail) {

        Thread t = new Thread(() -> {
            try {
                while (isOpen()) {
                    Object obj = receive();
                    if (obj instanceof DataFrame df) onMsg.accept(df);
                }
            } catch (Exception ex) {
                onFail.accept(ex);
            } finally {
                close();
            }
        }, "CH-Reader");

        t.setDaemon(false); // ✅ que no muera “porque sí”
        t.start();
    }
}
