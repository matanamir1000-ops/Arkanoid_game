package levels;

import powerups.ExtraBall;
import powerups.LaserPaddle;
import powerups.PaddleExpansion;
import powerups.PowerUp;
import powerups.SlowBall;
import sprites.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * The parts of a level definition that are the same however the level is laid
 * out.
 * <p>
 * Every level needs the playfield size in order to place anything, every level
 * counts its own breakable blocks the same way, and most levels offer the same
 * power-ups. A concrete level is left with the things that actually make it
 * that level: its name, its blocks, its backdrop, and how the player is
 * equipped to face it.
 * </p>
 */
public abstract class AbstractLevel implements LevelInformation {
    private static final int BORDER_THICKNESS = 20;

    private final int screenWidth;
    private final int screenHeight;

    /**
     * Constructor.
     *
     * @param screenWidth  the playfield width, in pixels.
     * @param screenHeight the playfield height, in pixels.
     */
    protected AbstractLevel(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * The playfield width.
     *
     * @return the width in pixels.
     */
    protected int screenWidth() {
        return this.screenWidth;
    }

    /**
     * The playfield height.
     *
     * @return the height in pixels.
     */
    protected int screenHeight() {
        return this.screenHeight;
    }

    /**
     * How thick the playfield borders are.
     * <p>
     * A level needs this to keep its blocks clear of the walls. It is stated
     * here rather than in each level so that the levels cannot drift apart from
     * one another.
     * </p>
     *
     * @return the border thickness in pixels.
     */
    protected int borderThickness() {
        return BORDER_THICKNESS;
    }

    /**
     * Counts the blocks that clearing this level actually requires.
     * <p>
     * Derived from the blocks themselves rather than written down separately,
     * and that is the point. A hand-written count that disagrees with the layout
     * either ends the level while blocks are still standing or leaves it
     * unwinnable forever, and nothing would report which. Asking each block
     * whether it can be broken makes the two impossible to disagree, and a level
     * full of steel needs no special case anywhere.
     * </p>
     * <p>
     * This builds a throwaway set of blocks purely to count them, which is
     * exactly what the freshness contract on blocks() makes safe: the list
     * counted here is a different set of objects from the one the level is
     * played with, but it is the same layout, so the count is the right count.
     * It happens once per level.
     * </p>
     *
     * @return how many blocks must be destroyed.
     */
    @Override
    public int numberOfBlocksToRemove() {
        int breakable = 0;
        for (Block block : this.blocks()) {
            if (block.isBreakable()) {
                breakable++;
            }
        }
        return breakable;
    }

    /**
     * The usual set of power-ups.
     * <p>
     * A level wanting a different selection overrides this; one wanting none
     * returns an empty list.
     * </p>
     *
     * @return a fresh list of the power-ups available in this level.
     */
    @Override
    public List<PowerUp> availablePowerUps() {
        List<PowerUp> available = new ArrayList<>();
        available.add(new ExtraBall());
        available.add(new PaddleExpansion());
        available.add(new LaserPaddle());
        available.add(new SlowBall());
        return available;
    }
}
