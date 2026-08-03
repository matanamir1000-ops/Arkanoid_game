package sprites;

import biuoop.DrawSurface;
import game.GameItem;
import game.GameLevel;
import game.Sprite;
import geometry.Rectangle;

import java.awt.Color;

/**
 * A read-only line of text drawn inside its own section of the top strip.
 * <p>
 * Every indicator in the game does the same thing: it is given a rectangle it
 * may draw inside, and each frame it writes one line of text there. Only the
 * text differs, so that is the single thing a subclass supplies.
 * </p>
 * <p>
 * The text is anchored to the left of its section. DrawSurface offers no way to
 * measure rendered text, so an indicator cannot know how wide its line will be;
 * anchoring to the left is a position that can be checked by reading it, while
 * anchoring to the right would be a guess about the font.
 * </p>
 * <p>
 * The strip background is painted by a separate sprite, so indicators draw text
 * only and never cover one another.
 * </p>
 */
public abstract class TextIndicator implements Sprite, GameItem {
    private static final int FONT_SIZE = 15;
    private static final int INSET_FROM_LEFT = 15;
    private static final int TEXT_SHIFT_DOWN = 5;
    private static final Color TEXT_COLOR = Color.BLACK;

    private final Rectangle rect;

    /**
     * Constructor.
     *
     * @param rect the section of the strip this indicator may draw inside.
     */
    protected TextIndicator(Rectangle rect) {
        this.rect = rect;
    }

    /**
     * The line to display this frame.
     * <p>
     * Called once per frame rather than stored, so an indicator that watches a
     * Counter always shows its current value.
     * </p>
     *
     * @return the text to draw.
     */
    protected abstract String text();

    @Override
    public void drawOn(DrawSurface d) {
        int x = (int) this.rect.getUpperLeft().getX() + INSET_FROM_LEFT;
        int y = (int) (this.rect.getUpperLeft().getY() + (this.rect.getHeight() / 2)) + TEXT_SHIFT_DOWN;

        d.setColor(TEXT_COLOR);
        d.drawText(x, y, this.text(), FONT_SIZE);
    }

    /**
     * An indicator reads its source fresh every frame, so it has no state of its
     * own to advance.
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
