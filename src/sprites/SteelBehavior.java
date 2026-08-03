package sprites;

import java.awt.Color;

/**
 * A block that can never be destroyed.
 * <p>
 * Balls bounce off it exactly as they do off anything else -- that is Block's
 * job and this behaviour does not touch it. It simply never reports itself
 * destroyed, so BlockRemover never removes it and it never scores.
 * </p>
 * <p>
 * Nothing anywhere has to recognise a steel block for the win condition to stay
 * correct. A level counts only its breakable blocks in
 * numberOfBlocksToRemove(), so steel is excluded by never having been included.
 * </p>
 */
public class SteelBehavior implements BlockBehavior {
    private static final int NO_POINTS = 0;

    /**
     * Steel does not record damage, because it does not take any.
     *
     * @param hitter the ball that struck the block.
     */
    @Override
    public void registerHit(Ball hitter) {
        // Intentionally empty: steel is unaffected by being hit.
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public Color displayColor(Color baseColor) {
        return baseColor;
    }

    @Override
    public int pointsForLastHit() {
        return NO_POINTS;
    }
}
