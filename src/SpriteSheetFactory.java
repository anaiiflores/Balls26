
import java.awt.*;
import java.awt.image.BufferedImage;

public class SpriteSheetFactory {

    // 4 frames, cada frame 48x48
    public static BufferedImage makeWalkerSheet() {
        int frameW = 48, frameH = 48, frames = 4;
        BufferedImage sheet = new BufferedImage(frameW * frames, frameH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < frames; i++) {
            int ox = i * frameW;
            drawWalkerFrame(g, ox, 0, frameW, frameH, i);
        }
        g.dispose();
        return sheet;
    }

    private static void drawWalkerFrame(Graphics2D g, int x, int y, int w, int h, int frame) {
        // Fondo transparente (no hace falta pintar)
        int cx = x + w / 2;
        int headY = y + 10;

        // “oscilación” piernas/brazos
        int swing = (frame % 2 == 0) ? 6 : -6;

        // Cabeza
        g.setColor(new Color(30, 30, 30));
        g.fillOval(cx - 6, headY, 12, 12);

        // Cuerpo
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx, headY + 12, cx, headY + 28);

        // Brazos
        g.drawLine(cx, headY + 16, cx - 10, headY + 22 + swing);
        g.drawLine(cx, headY + 16, cx + 10, headY + 22 - swing);

        // Piernas
        g.drawLine(cx, headY + 28, cx - 10, headY + 40 - swing);
        g.drawLine(cx, headY + 28, cx + 10, headY + 40 + swing);
    }

    // 6 frames, cada frame 64x64
    public static BufferedImage makeExplosionSheet() {
        int frameW = 64, frameH = 64, frames = 6;
        BufferedImage sheet = new BufferedImage(frameW * frames, frameH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < frames; i++) {
            int ox = i * frameW;
            drawExplosionFrame(g, ox, 0, frameW, frameH, i, frames);
        }
        g.dispose();
        return sheet;
    }

    private static void drawExplosionFrame(Graphics2D g, int x, int y, int w, int h, int i, int total) {
        int cx = x + w / 2;
        int cy = y + h / 2;

        // radio crece con el frame
        float t = (i + 1) / (float) total;
        int r1 = (int) (6 + 22 * t);
        int r2 = (int) (3 + 30 * t);

        // “bola” central
        g.setColor(new Color(255, 200, 0, 220));
        g.fillOval(cx - r1, cy - r1, r1 * 2, r1 * 2);

        // anillo exterior
        g.setColor(new Color(255, 80, 0, 160));
        g.setStroke(new BasicStroke(6));
        g.drawOval(cx - r2, cy - r2, r2 * 2, r2 * 2);

        // chispas
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(255, 255, 255, 180));
        for (int k = 0; k < 8; k++) {
            double ang = (Math.PI * 2) * k / 8.0;
            int sx = (int) (cx + Math.cos(ang) * (8 + 16 * t));
            int sy = (int) (cy + Math.sin(ang) * (8 + 16 * t));
            int ex = (int) (cx + Math.cos(ang) * (14 + 26 * t));
            int ey = (int) (cy + Math.sin(ang) * (14 + 26 * t));
            g.drawLine(sx, sy, ex, ey);
        }
    }
}
