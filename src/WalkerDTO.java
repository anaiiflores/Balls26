import java.io.Serializable;

public class WalkerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int id;

    // 🔥 Posición relativa (0..1) en Y
    public final double yNorm;

    // Velocidad se manda tal cual (px/s aprox)
    public final double vx;
    public final double vy;

    // lado por el que entra en el receptor
    public final Side entrySide;

    public enum Side { LEFT, RIGHT }

    public WalkerDTO(int id, double yNorm, double vx, double vy, Side entrySide) {
        this.id = id;
        this.yNorm = yNorm;
        this.vx = vx;
        this.vy = vy;
        this.entrySide = entrySide;
    }
}
