import javax.swing.*;

public class MainSC {
    public static void main(String[] args) throws Exception {
        int port = 51121;

        NetworkManager net = new NetworkManager();
        GameModel model = new GameModel(800, 500, true);
        GameView view = new GameView(model);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SERVER (SC)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(new GameController(model, view, net), "GameLoop").start();
        });

        ServerConection sc = new ServerConection();
        sc.bind(port);
        System.out.println("Server listening on " + port);

        new Thread(() -> {
            while (true) {
                try {
                    Channel ch = sc.accept();
                    System.out.println("Client connected!");

                    // ✅ engancha channel + arranca reader dentro del attachChannel
                    net.attachChannel(ch);

                    // ✅ luego health
                    net.startHealth();

                } catch (Exception e) {
                    System.out.println("Accept failed, retrying...");
                    e.printStackTrace();
                    sleep(1000);
                }
            }
        }, "SC-Acceptor").start();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
