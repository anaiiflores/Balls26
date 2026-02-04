

import java.io.IOException;
import java.net.Socket;

public class ClientConection {
    public Channel connect(String host, int port) throws IOException {
        return new Channel(new Socket(host, port));
    }
}
