
import java.awt.image.BufferedImage;
import java.util.Random;

public class Walker {
    public enum State { WALKING, EXPLODING, DEAD }

    private final int id;
    private double x, y;
    private double vx, vy;

    private final Animation walkAnim;
    private final Animation explosionAnim;
    private State state = State.WALKING;
    private boolean transferred = false;

    private long explosionHoldMs = 0;

    public Walker(int id, double startX, double startY, Animation walkAnim, Animation explosionAnim, double speed) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.walkAnim = walkAnim;
        this.explosionAnim = explosionAnim;

        Random r = new Random();
        double angle = r.nextDouble() * Math.PI * 2;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    // Constructor para crear desde red
    public Walker(int id, double x, double y, double vx, double vy, Animation walkAnim, Animation explosionAnim) {
        this.id = id;
        this.x = x; this.y = y;
        this.vx = vx; this.vy = vy;
        this.walkAnim = walkAnim;
        this.explosionAnim = explosionAnim;
    }

    public void update(long dtMs, int width, int height) {
        if (state == State.WALKING) {
            double dt = dtMs / 1000.0;
            x += vx * dt;
            y += vy * dt;
            walkAnim.update(dtMs);

        } else if (state == State.EXPLODING) {
            if (explosionAnim.getIndex() < explosionAnim.getFrameCount() - 1) {
                explosionAnim.update(dtMs);
            } else {
                explosionHoldMs += dtMs;
                if (explosionHoldMs >= 500) state = State.DEAD;
            }
        }
    }

    public Side hitWhichSide(int width, int height) {
        // detecta salida por izquierda/derecha/arriba/abajo
        int halfW = getImage().getWidth() / 2;
        int halfH = getImage().getHeight() / 2;

        if (x + halfW >= width) return Side.RIGHT;
        if (x - halfW <= 0) return Side.LEFT;
        if (y - halfH <= 0) return Side.TOP;
        if (y + halfH >= height) return Side.BOTTOM;
        return Side.NONE;
    }

    public enum Side { NONE, LEFT, RIGHT, TOP, BOTTOM }

    public void explode() {
        state = State.EXPLODING;
        explosionAnim.reset();
        explosionHoldMs = 0;
        vx = 0; vy = 0;
    }

    public BufferedImage getImage() {
        return (state == State.WALKING) ? walkAnim.getFrame() : explosionAnim.getFrame();
    }

    public int getDrawX() { return (int) Math.round(x - getImage().getWidth() / 2.0); }
    public int getDrawY() { return (int) Math.round(y - getImage().getHeight() / 2.0); }

    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public State getState() { return state; }

    public void setX(double x) { this.x = x; }
    public boolean isTransferred() { return transferred; }
    public void markTransferred() { transferred = true; }
}
