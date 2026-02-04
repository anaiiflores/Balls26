
import javax.swing.*;

public class MainCC {
    public static void main(String[] args) throws Exception {
        String host = "192.168.1.205";
        int port = 51121;

        ClientConection cc = new ClientConection();
        Channel ch = cc.connect(host, port);
        System.out.println("Connected to server!");

        NetworkManager net = new NetworkManager();
        net.attachChannel(ch);

        GameModel model = new GameModel(800, 500,false );
        GameView view = new GameView(model);

        ch.startReader(
                df -> net.onIncoming(df),
                ex -> System.out.println("CH fail: " + ex)
        );

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CLIENT (CC)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(new GameController(model, view, net), "GameLoop").start();
        });
    }
}
