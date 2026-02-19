
import java.awt.image.BufferedImage;
import java.util.*;

public class GameModel {

    private final List<Walker> walkers = new ArrayList<>();

    private final BufferedImage walkSheet;
    private final BufferedImage boomSheet;

    private long spawnAccMs = 0;
    private final long spawnEveryMs = 900;
    private final int maxWalkers = 10;

    private int nextId = 1;
    private final boolean spawningEnabled;

    public GameModel(int width, int height, boolean spawningEnabled) {
        this.spawningEnabled = spawningEnabled;
        walkSheet = SpriteSheetFactory.makeWalkerSheet();
        boomSheet = SpriteSheetFactory.makeExplosionSheet();
    }

    private Animation newWalkAnim() { return new Animation(walkSheet, 48, 48, 4, 140); }
    private Animation newBoomAnim() { return new Animation(boomSheet, 64, 64, 6, 180); }

    public synchronized void update(long dtMs, int w, int h) {
        if (spawningEnabled) {
            spawnAccMs += dtMs;
            if (spawnAccMs >= spawnEveryMs && walkers.size() < maxWalkers) {
                spawnAccMs = 0;
                walkers.add(new Walker(nextId++, w / 2.0, h / 2.0, newWalkAnim(), newBoomAnim(), 60));
            }
        }

        for (Walker wk : walkers) wk.update(dtMs, w, h);
        walkers.removeIf(wk -> wk.getState() == Walker.State.DEAD);
    }

    public synchronized java.util.List<Walker> snapshotWalkers() {
        return new ArrayList<>(walkers);
    }

    public synchronized void removeById(int id) {
        walkers.removeIf(w -> w.getId() == id);
    }

    public void spawnFromNetwork(WalkerDTO dto, int screenW, int screenH) {

        // X depende del lado por el que entra
        double x = (dto.entrySide == WalkerDTO.Side.LEFT)
                ? 20
                : screenW - 20;

        // 🔥 Convertimos yNorm (0..1) a píxeles reales
        double y = dto.yNorm * screenH;

        // clamp por seguridad
        if (y < 20) y = 20;
        if (y > screenH - 40) y = screenH - 40;

        Walker w = new Walker(
                dto.id,
                x,
                y,
                dto.vx,
                dto.vy,
                newWalkAnim(),
                newBoomAnim()
        );

        walkers.add(w);
    }


}
