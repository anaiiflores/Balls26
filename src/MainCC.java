import javax.swing.*;

public class MainCC {
    public static void main(String[] args) {

        String host = "172.16.141.73";
        int port = 51121;

        NetworkManager net = new NetworkManager();

        // Cliente: NO spawnea
        GameModel model = new GameModel(800, 500, false);
        GameView view = new GameView(model);

        // 1) UI primero (siempre)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CLIENT (CC)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(new GameController(model, view, net), "GameLoop").start();
        });

        // 2) Conector en thread aparte (no bloquea UI)
        new Thread(() -> {
            ClientConection cc = new ClientConection();

            while (true) {
                try {
                    System.out.println("Trying to connect to " + host + ":" + port + " ...");
                    Channel ch = cc.connect(host, port);
                    System.out.println("Connected to server!");

                    net.attachChannel(ch);

                    ch.startReader(
                            df -> net.onIncoming(df),
                            ex -> {
                                System.out.println("CH fail:");
                                ex.printStackTrace();
                            }
                    );

                    break; // conectado -> salimos del bucle
                } catch (Exception e) {
                    System.out.println("Connect failed, retrying in 1s...");
                    sleep(1000);
                }
            }
        }, "CC-Connector").start();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ignored) {}
    }
}
