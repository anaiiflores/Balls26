import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GameModel {

    // ✅ Lista interna (NO la exponemos directamente)
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

    /**
     * ✅ Update del juego (lo llama el GameController cada tick)
     * synchronized para que no choque con spawnFromNetwork/removeById/etc.
     */
    public synchronized void update(long dtMs, int w, int h) {

        // Spawn local si toca
        if (spawningEnabled) {
            spawnAccMs += dtMs;
            if (spawnAccMs >= spawnEveryMs && walkers.size() < maxWalkers) {
                spawnAccMs = 0;
                walkers.add(new Walker(nextId++, w / 2.0, h / 2.0, newWalkAnim(), newBoomAnim(), 60));
            }
        }

        // Actualizar walkers y limpiar muertos
        for (Walker wk : walkers) wk.update(dtMs, w, h);
        walkers.removeIf(wk -> wk.getState() == Walker.State.DEAD);
    }

    /**
     * ✅ Esto es CLAVE para MVC + Threads:
     * Devuelve una copia para iterar/pintar sin petar.
     */
    public synchronized List<Walker> snapshotWalkers() {
        return new ArrayList<>(walkers);
    }

    /**
     * ✅ Si necesitas buscar/borrar por id desde el controller
     */
    public synchronized void removeById(int id) {
        walkers.removeIf(w -> w.getId() == id);
    }

    /**
     * ✅ Cuando llega un DTO desde red, recreamos el walker aquí.
     * synchronized para evitar líos con update/pintado.
     */
    public synchronized void spawnFromNetwork(WalkerDTO dto, int screenW, int screenH) {
        double x = (dto.entrySide == WalkerDTO.Side.LEFT) ? 20 : (screenW - 20);

        double y = dto.y;
        if (y < 20) y = 20;
        if (y > screenH - 40) y = screenH - 40;

        Walker w = new Walker(dto.id, x, y, dto.vx, dto.vy, newWalkAnim(), newBoomAnim());
        walkers.add(w);
    }

    /**
     * ✅ Spawn manual de prueba (local)
     */
    public synchronized void spawnTest(int w, int h) {
        walkers.add(new Walker(nextId++, w / 2.0, h / 2.0, newWalkAnim(), newBoomAnim(), 60));
    }
}
