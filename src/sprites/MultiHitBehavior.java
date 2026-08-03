package sprites;

import java.awt.Color;

/**
 * A tough block that survives several hits and shows its wear.
 * <p>
 * The remaining hit points pick a colour out of a palette, so the player can
 * read how close a block is to breaking without any extra display machinery.
 * The palette is indexed so that its first entry is the last hit -- the colour
 * a block is wearing just before it breaks.
 * </p>
 * <p>
 * This behaviour is why the colour-matching rule had to go. Under the old rule
 * a ball adopted a block's colour on the first hit and then passed straight
 * through it, so a second hit could never register and a tough block could
 * never break.
 * </p>
 */
public class MultiHitBehavior implements BlockBehavior {
    private static final int POINTS_PER_HIT = 5;
    private static final int KILL_BONUS = 10;

    private final Color[] palette;
    private int hitPoints;

    /**
     * Constructor.
     *
     * @param hitPoints how many hits the block survives; must be at least 1.
     * @param palette   colours from most damaged to least; the entry at index
     *                  i is worn when i + 1 hit points remain. May be empty, in
     *                  which case the block keeps its own colour throughout.
     * @throws IllegalArgumentException if hitPoints is less than 1.
     */
    public MultiHitBehavior(int hitPoints, Color[] palette) {
        if (hitPoints < 1) {
            throw new IllegalArgumentException("hitPoints must be at least 1, but was " + hitPoints);
        }
        this.hitPoints = hitPoints;
        this.palette = new Color[palette.length];
        System.arraycopy(palette, 0, this.palette, 0, palette.length);
    }

    @Override
    public void registerHit(Ball hitter) {
        if (this.hitPoints > 0) {
            this.hitPoints--;
        }
    }

    @Override
    public boolean isDestroyed() {
        return this.hitPoints <= 0;
    }

    /**
     * Picks the colour for the damage taken so far.
     * <p>
     * The index is clamped rather than trusted: a palette shorter than the hit
     * point count is a reasonable thing for a level to declare, and it should
     * degrade to the toughest colour rather than throw mid-frame.
     * </p>
     *
     * @param baseColor the block's declared colour.
     * @return the colour to draw with this frame.
     */
    @Override
    public Color displayColor(Color baseColor) {
        if (this.palette.length == 0 || this.hitPoints < 1) {
            return baseColor;
        }
        int index = this.hitPoints - 1;
        if (index >= this.palette.length) {
            index = this.palette.length - 1;
        }
        return this.palette[index];
    }

    /**
     * Every hit scores, and the one that breaks the block scores extra.
     *
     * @return the points for the most recent hit.
     */
    @Override
    public int pointsForLastHit() {
        if (this.isDestroyed()) {
            return POINTS_PER_HIT + KILL_BONUS;
        }
        return POINTS_PER_HIT;
    }

    @Override
    public boolean isBreakable() {
        return true;
    }
}
