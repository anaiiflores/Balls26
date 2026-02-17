import javax.swing.*;

public class MainSC {
    public static void main(String[] args) throws Exception {
        int port = 51121;

        NetworkManager net = new NetworkManager();
        GameModel model = new GameModel(800, 500, true);
        GameView view = new GameView(model);

        // UI + GameLoop siempre
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

        // Accept en thread (no bloquea UI)
        new Thread(() -> {
            try {
                Channel ch = sc.accept();
                System.out.println("Client connected!");

                net.attachChannel(ch);

                // ✅ primero arrancamos el reader
                ch.startReader(df -> net.onIncoming(df), ex -> {
                    System.out.println("CH fail:");
                    ex.printStackTrace();
                });

                // ✅ y DESPUÉS el health (evita reset)
                net.startHealth();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "SC-Acceptor").start();
    }
}
