import javax.swing.*;

public class MainCC {
    public static void main(String[] args) {

        String host = "192.168.1.211";
        int port = 51121;

        NetworkManager net = new NetworkManager();
        GameModel model = new GameModel(800, 500, false);
        GameView view = new GameView(model);

        // UI + GameLoop siempre
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CLIENT (CC)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(new GameController(model, view, net), "GameLoop").start();
        });

        // Connect en thread con reintento
        new Thread(() -> {
            ClientConection cc = new ClientConection();

            while (true) {
                try {
                    System.out.println("Trying to connect to " + host + ":" + port + " ...");
                    Channel ch = cc.connect(host, port);
                    System.out.println("Connected to server!");

                    net.attachChannel(ch);

                    // ✅ primero reader
                    ch.startReader(df -> net.onIncoming(df), ex -> {
                        System.out.println("CH fail:");
                        ex.printStackTrace();
                    });

                    // ✅ y DESPUÉS health
                    net.startHealth();

                    break;

                } catch (Exception e) {
                    System.out.println("Connect failed, retrying in 1s...");
                    sleep(1000);
                }
            }
        }, "CC-Connector").start();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
