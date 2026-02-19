
import javax.swing.*;
import java.net.ServerSocket;

public class MainP2P {

    public static void main(String[] args) throws Exception {

        final int WIDTH = 800, HEIGHT = 500;

        final int MAIN_PORT = 51121;
        final int AUX_PORT  = 51122;

        // 1) Decidir mi puerto (player1 / player2)
        int myPort;
        int peerPort;
        boolean isPlayer1;

        // intento bindear MAIN_PORT
        if (canBind(MAIN_PORT)) {
            myPort = MAIN_PORT;
            peerPort = AUX_PORT;
            isPlayer1 = true;
        } else {
            myPort = AUX_PORT;
            peerPort = MAIN_PORT;
            isPlayer1 = false;
        }

        System.out.println("I am " + (isPlayer1 ? "PLAYER 1" : "PLAYER 2"));
        System.out.println("MyPort=" + myPort + " PeerPort=" + peerPort);

        // 2) MVC
        NetworkManager net = new NetworkManager();
        GameModel model = new GameModel(WIDTH, HEIGHT, isPlayer1); // solo P1 spawnea (ejemplo)
        GameView view = new GameView(model);

        JFrame frame = new JFrame("P2P - " + (isPlayer1 ? "P1" : "P2"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(view);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 3) Nodo P2P (1 canal)
        P2PNode node = new P2PNode(net);

        // 4) Oreja (server) en un thread
        new Thread(new ServerConnector(myPort, node::tryAttach), "ServerConnector").start();

        // 5) Intento conectar al otro puerto (misma máquina por ahora)
        String host = "127.0.0.1";
        new Thread(new ClientConnector(host, peerPort, node::tryAttach), "ClientConnector").start();

        // 6) GameLoop
        new Thread(new GameController(model, view, net), "GameLoop").start();
    }

    private static boolean canBind(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
