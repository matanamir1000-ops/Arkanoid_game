package powerups;

import biuoop.DrawSurface;
import game.GameItem;
import game.GameLevel;
import game.Sprite;
import geometry.Point;
import geometry.Rectangle;
import sprites.Catcher;

import java.awt.Color;

/**
 * A capsule drifting down the screen, waiting to be caught.
 * <p>
 * There is one of these regardless of how many power-ups exist. What it does
 * when caught is the effect it carries, which it applies without ever knowing
 * which effect that is.
 * </p>
 * <p>
 * It holds a Catcher, not a Paddle, so it cannot ask what it is falling onto
 * even if it wanted to. It is also deliberately not a Collidable: a Collidable
 * gets struck by balls, and Collidable.hit must return a new Velocity, which is
 * a meaningless thing for a pickup to be asked for.
 * </p>
 */
public class FallingPowerUp implements Sprite, GameItem {
    private static final int WIDTH = 18;
    private static final int HEIGHT = 18;
    private static final double FALL_SPEED = 2.0;
    private static final int SYMBOL_FONT_SIZE = 14;
    private static final int SYMBOL_INSET_X = 5;
    private static final int SYMBOL_INSET_Y = 14;
    private static final Color SYMBOL_COLOR = Color.BLACK;

    private final PowerUp effect;
    private final Catcher catcher;
    private final int floorY;
    private double x;
    private double y;
    private GameLevel level;

    /**
     * Constructor.
     * <p>
     * The level is not passed here. It arrives through addToGame, which is the
     * moment the capsule actually joins a level -- taking it in both places
     * would mean two sources for one fact, agreeing only by the caller's care.
     * </p>
     *
     * @param effect  what happens when this is caught.
     * @param start   where the capsule appears.
     * @param catcher the thing that can catch it.
     * @param floorY  the height past which an uncaught capsule is discarded.
     */
    public FallingPowerUp(PowerUp effect, Point start, Catcher catcher, int floorY) {
        this.effect = effect;
        this.catcher = catcher;
        this.floorY = floorY;
        this.x = start.getX();
        this.y = start.getY();
    }

    /**
     * Falls one step, then either gets caught or falls off the bottom.
     */
    @Override
    public void timePassed() {
        this.y += FALL_SPEED;

        if (this.catcher.catches(this.bounds())) {
            this.effect.apply(this.level);
            this.level.removeSprite(this);
            return;
        }

        if (this.y > this.floorY) {
            this.level.removeSprite(this);
        }
    }

    @Override
    public void drawOn(DrawSurface surface) {
        this.bounds().drawOn(surface, this.effect.getColor());
        surface.setColor(SYMBOL_COLOR);
        surface.drawText((int) this.x + SYMBOL_INSET_X, (int) this.y + SYMBOL_INSET_Y,
                this.effect.getSymbol(), SYMBOL_FONT_SIZE);
    }

    @Override
    public void addToGame(GameLevel game) {
        this.level = game;
        game.addSprite(this);
    }

    /**
     * The capsule's current rectangle.
     *
     * @return the area the capsule occupies this frame.
     */
    private Rectangle bounds() {
        return new Rectangle(new Point(this.x, this.y), WIDTH, HEIGHT);
    }
}
