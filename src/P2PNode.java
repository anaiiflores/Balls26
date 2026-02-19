
import java.net.Socket;

public class P2PNode {

    private final NetworkManager net;

    private volatile Channel channel;
    private volatile Thread channelThread;
    private volatile Thread healthThread;

    public P2PNode(NetworkManager net) {
        this.net = net;
    }

    public synchronized boolean hasChannel() {
        return channel != null && channel.isOpen();
    }

    public synchronized void tryAttach(Socket socket) {
        try {
            // ✅ si ya tengo canal, cierro el nuevo socket
            if (hasChannel()) {
                socket.close();
                return;
            }

            System.out.println("[P2P] Attaching new channel...");
            channel = new Channel(socket);
            net.attach(channel);

            HealthChannel health = new HealthChannel(channel, 1000);
            channel.attachHealth(health);

            channelThread = new Thread(channel, "Channel-Reader");
            channelThread.start();

            healthThread = new Thread(health, "Health");
            healthThread.start();

        } catch (Exception e) {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
