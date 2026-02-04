
import javax.swing.*;
import java.awt.*;

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

        for (Walker w : model.getWalkers()) {
            if (w.getState() != Walker.State.DEAD) {
                g.drawImage(w.getImage(), w.getDrawX(), w.getDrawY(), null);
            }
        }

        g.setColor(Color.DARK_GRAY);
        g.drawString("Walkers: " + model.getWalkers().size(), 10, 20);
    }
}
