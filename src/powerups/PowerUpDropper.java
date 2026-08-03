package powerups;

import collision.HitListener;
import game.GameLevel;
import geometry.Point;
import sprites.Ball;
import sprites.Block;
import sprites.Catcher;

import java.util.List;
import java.util.Random;

/**
 * Occasionally drops a power-up where a block was destroyed.
 * <p>
 * A listener, like every other consequence of a block being hit. It fires only
 * when the block actually breaks, so a tough block does not shed a capsule
 * every time it is grazed.
 * </p>
 * <p>
 * Which power-up falls is drawn from a list it was handed. This class never
 * names a single one of them, so adding a power-up means adding it to that
 * list and changing nothing here.
 * </p>
 */
public class PowerUpDropper implements HitListener {
    private final GameLevel level;
    private final Catcher catcher;
    private final List<PowerUp> available;
    private final double dropChance;
    private final int floorY;
    private final Random random;

    /**
     * Constructor.
     *
     * @param level      the level capsules are dropped into.
     * @param catcher    the thing that can catch them.
     * @param available  the power-ups that may drop; one is chosen at random.
     * @param dropChance the probability, from 0 to 1, that a broken block drops one.
     * @param floorY     the height past which an uncaught capsule is discarded.
     */
    public PowerUpDropper(GameLevel level, Catcher catcher, List<PowerUp> available,
                          double dropChance, int floorY) {
        this.level = level;
        this.catcher = catcher;
        this.available = available;
        this.dropChance = dropChance;
        this.floorY = floorY;
        this.random = new Random();
    }

    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        if (!beingHit.isDestroyed() || this.available.isEmpty()) {
            return;
        }
        if (this.random.nextDouble() >= this.dropChance) {
            return;
        }
        PowerUp effect = this.available.get(this.random.nextInt(this.available.size()));
        Point centre = beingHit.getCenter();
        new FallingPowerUp(effect, centre, this.catcher, this.floorY).addToGame(this.level);
    }
}
