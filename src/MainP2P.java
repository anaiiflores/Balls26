import javax.swing.*;
import java.net.Socket;

public class MainP2P {

    public static void main(String[] args) throws Exception {

        final int PORT = 51121;
        final String HOST = "localhost"; // para probar en la misma máquina

        // ===== MVC =====
        GameModel model = new GameModel(800, 500, true);
        NetworkManager net = new NetworkManager();
        GameView view = new GameView(model);

        JFrame frame = new JFrame("P2P WALKERS");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(view);
        frame.setVisible(true);

        // ===== SERVER (oreja) en hilo =====
        new Thread(() -> {
            try {
                ServerConection sc = new ServerConection();
                sc.bind(PORT);
                System.out.println("[SERVER] Listening on " + PORT);

                Socket socket = sc.accept();   // 🔴 bloquea, PERO NO la UI
                System.out.println("[SERVER] Accepted connection");

                Channel ch = new Channel(socket);
                ch.setOnTransfer(net::onTransfer);

                HealthTestChannel health = new HealthTestChannel(ch);
                ch.attachHealth(health);

                new Thread(ch, "Channel-Reader").start();
                new Thread(health, "Health").start();

            } catch (Exception e) {
                System.out.println("[SERVER] failed");
            }
        }).start();

        // ===== CLIENT (intenta conectar) =====
        new Thread(() -> {
            try {
                Thread.sleep(500); // deja tiempo a que el server arranque

                ClientConection cc = new ClientConection();
                Socket socket = cc.connect(HOST, PORT);
                System.out.println("[CLIENT] Connected to " + HOST);

                Channel ch = new Channel(socket);
                ch.setOnTransfer(net::onTransfer);

                HealthTestChannel health = new HealthTestChannel(ch);
                ch.attachHealth(health);

                new Thread(ch, "Channel-Reader").start();
                new Thread(health, "Health").start();

            } catch (Exception e) {
                System.out.println("[CLIENT] no connection (ok)");
            }
        }).start();

        // ===== GAME LOOP =====
        new Thread(new GameController(model, view, net), "GameLoop").start();

        // Walker local para probar
        model.spawnTest(800, 500);
    }
}
