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

        for (Walker w : model.snapshotWalkers()) {
            if (w.getState() != Walker.State.DEAD) {
                g.drawImage(w.getImage(), w.getDrawX(), w.getDrawY(), null);
            }
        }

        g.drawString("Walkers: " + model.snapshotWalkers().size(), 10, 20);
    }


}
