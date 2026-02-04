
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConection {
    private ServerSocket server;

    public void bind(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public Channel accept() throws IOException {
        Socket s = server.accept(); // bloquea
        return new Channel(s);
    }

    public void close() {
        try { server.close(); } catch (Exception ignored) {}
    }
}
