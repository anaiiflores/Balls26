
import javax.swing.*;

public class MainSC {
    public static void main(String[] args) throws Exception {
        int port = 51121;

        ServerConection sc = new ServerConection();
        sc.bind(port);
        System.out.println("Server listening on " + port);

        Channel ch = sc.accept();
        System.out.println("Client connected!");

        NetworkManager net = new NetworkManager();
        net.attachChannel(ch);

        GameModel model = new GameModel(800, 500,true);
        GameView view = new GameView(model);

        ch.startReader(
                df -> net.onIncoming(df),
                ex -> System.out.println("CH fail: " + ex)
        );

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SERVER (SC)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(new GameController(model, view, net), "GameLoop").start();
        });
        model.spawnTest(800, 500);

    }
}
