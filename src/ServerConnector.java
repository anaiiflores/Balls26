
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnector implements Runnable {

    public interface OnSocket {
        void onSocket(Socket s);
    }

    private final int port;
    private final OnSocket callback;

    public ServerConnector(int port, OnSocket callback) {
        this.port = port;
        this.callback = callback;
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[SERVER] Listening on " + port);

            while (true) {
                Socket s = ss.accept(); // bloquea
                callback.onSocket(s);
            }

        } catch (IOException e) {
            System.out.println("[SERVER] stopped: " + e.getMessage());
        }
    }
}
