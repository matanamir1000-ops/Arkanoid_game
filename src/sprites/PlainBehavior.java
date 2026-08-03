package sprites;

import java.awt.Color;

/**
 * An ordinary block: one hit destroys it.
 * <p>
 * This is what every block in the game behaved like before block behaviours
 * existed, so it is also the behaviour a block gets when none is specified.
 * </p>
 */
public class PlainBehavior implements BlockBehavior {
    private static final int POINTS_PER_BLOCK = 5;

    private boolean destroyed;

    /**
     * Constructor.
     */
    public PlainBehavior() {
        this.destroyed = false;
    }

    @Override
    public void registerHit(Ball hitter) {
        this.destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return this.destroyed;
    }

    @Override
    public Color displayColor(Color baseColor) {
        return baseColor;
    }

    @Override
    public int pointsForLastHit() {
        return POINTS_PER_BLOCK;
    }

    @Override
    public boolean isBreakable() {
        return true;
    }
}
