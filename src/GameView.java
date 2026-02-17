import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameView extends JPanel {
    private final GameModel model;

    public GameView(GameModel model) {
        this.model = model;
        setBackground(Color.WHITE);
        setFocusable(true);
    }

    @Override

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        java.util.List<Walker> snapshot = new java.util.ArrayList<>(model.getWalkers());

        for (Walker w : snapshot) {
            if (w.getState() != Walker.State.DEAD) {
                g.drawImage(w.getImage(), w.getDrawX(), w.getDrawY(), null);
            }
        }

        g.setColor(Color.DARK_GRAY);
        g.drawString("Walkers: " + snapshot.size(), 10, 20);
    }

}
