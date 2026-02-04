
import java.awt.image.BufferedImage;

public class Animation {

    private final BufferedImage[] frames;
    private final long frameTimeMs;
    private int idx = 0;
    private long accMs = 0;

    public Animation(BufferedImage sheet, int frameW, int frameH, int frameCount, long frameTimeMs) {
        this.frames = new BufferedImage[frameCount];
        this.frameTimeMs = frameTimeMs;

        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameW, 0, frameW, frameH);
        }
    }

    public void update(long dtMs) {
        accMs += dtMs;
        while (accMs >= frameTimeMs) {
            accMs -= frameTimeMs;
            idx = (idx + 1) % frames.length;
        }
    }

    public BufferedImage getFrame() {
        return frames[idx];
    }

    public boolean isLastFrame() {
        return idx == frames.length - 1;
    }

    public int getIndex() {
        return idx;
    }

    public int getFrameCount() {
        return frames.length;
    }

    public void reset() {
        idx = 0;
        accMs = 0;
    }
}
