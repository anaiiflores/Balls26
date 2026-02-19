

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class Channel implements Runnable {

    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private volatile boolean running = true;

    private Consumer<WalkerDTO> onTransfer;     // manda al NetworkManager
    private HealthChannel health;               // para notifyPong()

    public Channel(Socket socket) throws IOException {
        this.socket = socket;
        openStreams();
    }

    private void openStreams() throws IOException {
        // ✅ SIEMPRE ESTE ORDEN
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void setOnTransfer(Consumer<WalkerDTO> onTransfer) {
        this.onTransfer = onTransfer;
    }

    public void attachHealth(HealthChannel health) {
        this.health = health;
    }

    public synchronized void send(MsgDTO msg) throws IOException {
        if (!isOpen()) throw new IOException("Socket closed");
        out.writeObject(msg);
        out.flush();
        out.reset(); // evita cache raro
    }

    public boolean isOpen() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    public void stop() {
        running = false;
        close();
    }

    public synchronized void close() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        in = null; out = null;
    }

    private void process(MsgDTO msg) throws IOException {
        switch (msg.code) {
            case 0 -> { // TRANSFER
                if (msg.hasObject == 1 && msg.payload instanceof WalkerDTO dto) {
                    if (onTransfer != null) onTransfer.accept(dto);
                }
            }
            case 1 -> { // PING -> responde PONG
                send(new MsgDTO(2, 0, null));
            }
            case 2 -> { // PONG
                if (health != null) health.notifyPong();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (running && isOpen()) {
                Object obj = in.readObject();          // bloqueante
                if (obj instanceof MsgDTO msg) {
                    process(msg);
                }
            }
        } catch (EOFException eof) {
            // el otro extremo cerró
        } catch (Exception e) {
            System.out.println("[Channel] Reader error: " + e);
        } finally {
            close();
        }
    }
}
