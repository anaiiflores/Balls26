

import java.io.IOException;
import java.net.Socket;

public class ClientConnector implements Runnable {

    public interface OnSocket {
        void onSocket(Socket s);
    }

    private final String host;
    private final int port;
    private final OnSocket callback;

    public ClientConnector(String host, int port, OnSocket callback) {
        this.host = host;
        this.port = port;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket s = new Socket(host, port);
                callback.onSocket(s);
                // si conectó, esperamos un poco antes de intentar otra vez
                Thread.sleep(2000);
            } catch (IOException e) {
                sleep(800);
            } catch (InterruptedException ignored) {}
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
