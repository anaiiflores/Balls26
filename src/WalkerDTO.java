
import java.io.Serializable;

public class WalkerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int id;
    public final double x;
    public final double y;
    public final double vx;
    public final double vy;

    // lado por el que entra en el receptor
    public final Side entrySide;

    public enum Side { LEFT, RIGHT }

    public WalkerDTO(int id, double x, double y, double vx, double vy, Side entrySide) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.entrySide = entrySide;
    }
}
