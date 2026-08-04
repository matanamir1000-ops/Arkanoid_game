package levels;

import geometry.Velocity;
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

    // How far off vertical the innermost pair of balls is launched, and how much
    // further out each pair after it goes. The minimum must stay above zero: it
    // is the whole of what keeps fannedVelocities from launching a ball straight
    // up, which is a launch no level may make. See initialBallVelocities on
    // LevelInformation for why.
    private static final double MIN_LEAN_DEGREES = 15;
    private static final double LEAN_STEP_DEGREES = 14;

    // The lean grows with every pair, so a large enough fan would eventually
    // reach horizontal and then point downward, launching balls into the death
    // region. 15 + 5 * 14 = 85 degrees, so eleven is the most balls this may be
    // asked for. No level comes near it; the largest fan in the game is seven.
    private static final int MAX_FANNED_BALLS = 11;

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
     * How many balls the level starts with.
     * <p>
     * Derived from the launch velocities rather than written down separately,
     * for the same reason the block count is derived from the blocks. One ball
     * is created per velocity, so a hand-written number here would be a second
     * answer to a question that already has one, and only the velocity list can
     * be honoured -- a ball with no velocity is not a ball. Deriving it means a
     * level changes its ball count in exactly one place.
     * </p>
     *
     * @return the ball count.
     */
    @Override
    public final int numberOfBalls() {
        return this.initialBallVelocities().size();
    }

    /**
     * Launch velocities for a fan of balls, leaning alternately right and left
     * of vertical.
     * <p>
     * The balls come out in pairs, the first pair leaning MIN_LEAN_DEGREES
     * either side of vertical and each pair after it LEAN_STEP_DEGREES further
     * out. An odd count leaves the outermost ball unpaired, leaning right.
     * </p>
     * <p>
     * This is the easy way for a level to honour the rule that no ball may be
     * launched straight up, which LevelInformation.initialBallVelocities()
     * states and explains. It is a convenience, not a guarantee: a level that
     * builds its own velocities can still break the rule, and nothing here can
     * stop it. What this does promise is that every angle it produces is at
     * least MIN_LEAN_DEGREES off vertical, so a level that asks for its balls
     * here cannot get the stalemate by accident. Four levels used to each carry
     * their own copy of that reasoning.
     * </p>
     *
     * @param count how many balls to launch, at most MAX_FANNED_BALLS.
     * @param speed how fast each of them travels, in pixels per frame.
     * @return the launch velocities, innermost pair first.
     * @throws IllegalArgumentException if count exceeds MAX_FANNED_BALLS, where
     *                                  the fan would have spread so wide that
     *                                  the outermost balls launched downward.
     */
    protected List<Velocity> fannedVelocities(int count, double speed) {
        if (count > MAX_FANNED_BALLS) {
            throw new IllegalArgumentException(
                    "a fan of " + count + " balls would spread past horizontal; the most is " + MAX_FANNED_BALLS);
        }

        List<Velocity> velocities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int pairIndex = i / 2;
            double lean = MIN_LEAN_DEGREES + pairIndex * LEAN_STEP_DEGREES;
            if (i % 2 == 0) {
                velocities.add(Velocity.fromAngleAndSpeed(lean, speed));
            } else {
                velocities.add(Velocity.fromAngleAndSpeed(-lean, speed));
            }
        }
        return velocities;
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
    public final int numberOfBlocksToRemove() {
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
