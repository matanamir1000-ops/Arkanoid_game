package levels;

import biuoop.DrawSurface;
import game.Background;
import game.Sprite;
import graphics.ColorFade;

import java.awt.Color;

/**
 * A grid of faint lines that drifts slowly downward.
 * <p>
 * The drift is achieved by moving where the lines start rather than by moving
 * the lines themselves: the offset walks from zero to one cell and wraps, which
 * is indistinguishable from an endlessly scrolling grid and needs no state
 * beyond a single counter.
 * </p>
 */
public class GridBackground implements Sprite {
    private static final int CELL_SIZE = 40;
    private static final double LINE_FADE = 0.82;
    private static final int DRIFT_PERIOD = 3;

    private final Background sky;
    private final Color lineColor;
    private final int width;
    private final int height;
    private int frame;

    /**
     * Constructor.
     *
     * @param skyColor  the colour behind the grid.
     * @param lineColor the colour of the grid lines before fading.
     * @param width     the playfield width.
     * @param height    the playfield height.
     */
    public GridBackground(Color skyColor, Color lineColor, int width, int height) {
        this.sky = new Background(skyColor, 0, 0, width, height);
        this.lineColor = ColorFade.towards(lineColor, skyColor, LINE_FADE);
        this.width = width;
        this.height = height;
        this.frame = 0;
    }

    @Override
    public void drawOn(DrawSurface surface) {
        this.sky.drawOn(surface);

        int offset = (this.frame / DRIFT_PERIOD) % CELL_SIZE;
        surface.setColor(this.lineColor);

        for (int x = 0; x <= this.width; x += CELL_SIZE) {
            surface.drawLine(x, 0, x, this.height);
        }
        for (int y = offset; y <= this.height; y += CELL_SIZE) {
            surface.drawLine(0, y, this.width, y);
        }
    }

    @Override
    public void timePassed() {
        this.frame++;
    }
}
