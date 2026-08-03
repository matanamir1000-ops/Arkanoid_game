package sprites;

import biuoop.DrawSurface;
import game.GameItem;
import game.GameLevel;
import game.Sprite;
import geometry.Rectangle;

import java.awt.Color;

/**
 * A Sprite that shows the current level's name in the top strip.
 * <p>
 * The strip's background is painted by a separate sprite, so this indicator
 * draws only its own text. It is anchored to the left edge because there is no
 * way to measure rendered text width through DrawSurface -- anchoring to the
 * right would mean guessing how wide the name will be, and a long name would
 * run off the screen.
 * </p>
 */
public class LevelNameIndicator implements Sprite, GameItem {
    private static final int FONT_SIZE = 15;
    private static final int INSET_FROM_LEFT = 15;
    private static final int TEXT_SHIFT_DOWN = 5;

    private final String levelName;
    private final Rectangle rect;

    /**
     * Constructs a new LevelNameIndicator.
     *
     * @param levelName the name to display.
     * @param rect      the strip the text is drawn inside.
     */
    public LevelNameIndicator(String levelName, Rectangle rect) {
        this.levelName = levelName;
        this.rect = rect;
    }

    @Override
    public void drawOn(DrawSurface d) {
        int x = (int) this.rect.getUpperLeft().getX() + INSET_FROM_LEFT;
        int y = (int) (this.rect.getUpperLeft().getY() + (this.rect.getHeight() / 2)) + TEXT_SHIFT_DOWN;

        d.setColor(Color.BLACK);
        d.drawText(x, y, "Level Name: " + this.levelName, FONT_SIZE);
    }

    /**
     * The level name never changes while the level is being played, so there is
     * nothing to update each tick.
     */
    @Override
    public void timePassed() {
        // Intentionally empty.
    }

    @Override
    public void addToGame(GameLevel game) {
        game.addSprite(this);
    }
}
