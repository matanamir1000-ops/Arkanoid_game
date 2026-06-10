package game;

import biuoop.DrawSurface;
import java.awt.Color;

/**
 * A background sprite that fills the screen with a solid color.
 */
public class Background implements Sprite {
    private final Color color;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * Constructor for the Background.
     *
     * @param color  the background color.
     * @param x      the starting X coordinate.
     * @param y      the starting Y coordinate.
     * @param width  the width of the background.
     * @param height the height of the background.
     */
    public Background(Color color, int x, int y, int width, int height) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawOn(DrawSurface surface) {
        surface.setColor(this.color);
        surface.fillRectangle(this.x, this.y, this.width, this.height);
    }

    @Override
    public void timePassed() {
        // Intentionally empty: the background sprite has no per-tick state.
    }
}