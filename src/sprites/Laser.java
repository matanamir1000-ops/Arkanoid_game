package sprites;

import game.GameLevel;
import geometry.Point;



import java.awt.Color;

/**
 * A bolt fired upward from the paddle, consumed by the first block it strikes.
 * <p>
 * A laser is a Ball, and genuinely so: it is a small projectile with a
 * position, a colour and a direction of travel. That is not a convenience --
 * HitListener.hitEvent takes the Ball that caused the hit, so anything that can
 * damage a block has to be one. The alternative was passing null and hoping no
 * listener ever dereferenced it, which is a landmine for whoever adds the next
 * listener.
 * </p>
 * <p>
 * It never bounces, so it does not use the inherited movement: it walks itself
 * straight up and is consumed on contact. It is never counted in the level's
 * remaining balls, and it only ever travels away from the death region, so
 * losing a laser can never be mistaken for losing a ball.
 * </p>
 */
public class Laser extends Ball {
    private static final int RADIUS = 2;
    private static final Color BOLT_COLOR = Color.RED;
    private static final int SPEED = 9;

    private GameLevel level;

    /**
     * Constructor.
     * <p>
     * The level arrives through addToGame rather than here, so that the level a
     * bolt travels through is by construction the level it was added to.
     * </p>
     *
     * @param start where the bolt is fired from.
     */
    public Laser(Point start) {
        super(start, RADIUS, BOLT_COLOR);
    }

    /**
     * Registers the bolt as a sprite only.
     * <p>
     * Deliberately not the inherited behaviour. A Ball entering play is counted
     * toward the balls the player has left, and a laser is not that: firing one
     * must not make the level think another ball is in play, and a bolt hitting
     * a block must not read as a ball being lost. It is also never listed as a
     * live ball, so a power-up that slows the balls will not slow the lasers.
     * </p>
     *
     * @param game the level to add this bolt to.
     */
    @Override
    public void addToGame(GameLevel game) {
        this.level = game;
        game.addSprite(this);
    }

    /**
     * Advances the bolt and resolves whatever it has reached.
     * <p>
     * The block list is walked as a copy, because damaging a block removes it
     * from that list while this loop is running.
     * </p>
     */
    @Override
    public void timePassed() {
        this.moveCenterTo(new Point(this.getX(), this.getY() - SPEED));

        Point tip = new Point(this.getX(), this.getY());
        for (Block block : this.level.getBlocks()) {
            if (block.getCollisionRectangle().contains(tip)) {
                block.applyDamage(this);
                this.expire();
                return;
            }
        }

        if (this.getY() <= 0) {
            this.expire();
        }
    }

    /**
     * Takes the bolt out of the game.
     * <p>
     * Ball.removeFromGame drops it from the sprite collection and the level's
     * ball list, and deliberately does not touch the remaining-ball counter --
     * which is exactly right here, because a spent laser is not a lost life.
     * </p>
     */
    private void expire() {
        this.removeFromGame(this.level);
    }
}
