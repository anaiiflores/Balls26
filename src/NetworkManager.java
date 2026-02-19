

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {

    private final Queue<WalkerDTO> inbox = new ConcurrentLinkedQueue<>();

    private volatile Channel ch;

    public void attach(Channel ch) {
        this.ch = ch;
        ch.setOnTransfer(this::onTransfer);
    }

    public boolean isConnected() {
        return ch != null && ch.isOpen();
    }

    public void onTransfer(WalkerDTO dto) {
        inbox.add(dto);
    }

    public void drainInbox(GameModel model, int w, int h) {
        WalkerDTO dto;
        while ((dto = inbox.poll()) != null) {
            model.spawnFromNetwork(dto, w, h);
        }
    }

    public void sendTransfer(WalkerDTO dto) {
        if (!isConnected()) return;
        try {
            ch.send(new MsgDTO(0, 1, dto));
        } catch (IOException e) {
            System.out.println("[NET] sendTransfer failed -> closing");
            ch.close();
        }
    }
}
