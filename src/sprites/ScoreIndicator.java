package sprites;

import biuoop.DrawSurface;
import game.Counter;
import game.Game;
import game.GameItem;
import game.Sprite;
import geometry.Rectangle;

import java.awt.Color;

/**
 * A Sprite that sits at the top of the screen and indicates the current score.
 * It observes a score counter and draws its value within a specified rectangular region.
 */
public class ScoreIndicator implements Sprite, GameItem {
    private final Counter score;
    private final Rectangle rect;

    private static final int FONT_SIZE = 15;
    private static final int TEXT_SHIFT_LEFT = 30;
    private static final int TEXT_SHIFT_DOWN = 5;

    /**
     * Constructs a new ScoreIndicator.
     *
     * @param score the score counter to track and display.
     * @param rect  the visual bounds where the score will be displayed.
     */
    public ScoreIndicator(Counter score, Rectangle rect) {
        this.score = score;
        this.rect = rect;
    }

    /**
     * Draws the score indicator, including its background and the text,
     * on the given DrawSurface.
     *
     * @param d the DrawSurface to draw on.
     */
    @Override
    public void drawOn(DrawSurface d) {
        this.rect.drawOn(d, Color.LIGHT_GRAY);

        int centerX = (int) (this.rect.getUpperLeft().getX() + (this.rect.getWidth() / 2));
        int centerY = (int) (this.rect.getUpperLeft().getY() + (this.rect.getHeight() / 2));

        d.setColor(Color.BLACK);
        d.drawText(centerX - TEXT_SHIFT_LEFT, centerY + TEXT_SHIFT_DOWN,
                "Score: " + this.score.getValue(), FONT_SIZE);
    }

    /**
     * Notifies the sprite that a unit of time has passed.
     * The ScoreIndicator has no animated state, so this method is intentionally empty.
     */
    @Override
    public void timePassed() {
        // Intentionally empty.
    }

    /**
     * Adds the ScoreIndicator to the game as a sprite.
     *
     * @param g the game environment to add this sprite to.
     */
    public void addToGame(Game g) {
        g.addSprite(this);
    }
}