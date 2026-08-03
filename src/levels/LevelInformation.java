package levels;

import game.Sprite;
import geometry.Velocity;
import sprites.Block;

import java.util.List;

/**
 * The complete definition of one level.
 * <p>
 * A level definition is pure data about what the level contains: how many balls
 * and how fast, how wide and quick the paddle is, what the backdrop looks like
 * and which blocks are laid out. It knows nothing about running a game -- no
 * counters, no listeners, no drawing loop. GameLevel reads a LevelInformation
 * and builds a playable level from it.
 * </p>
 * <p>
 * This is the seam that turns "the game" into "a sequence of levels": adding a
 * level means writing one new implementation of this interface, and changing no
 * existing code.
 * </p>
 */
public interface LevelInformation {

    /**
     * The number of balls the player starts the level with.
     *
     * @return the ball count.
     */
    int numberOfBalls();

    /**
     * The launch velocity of each starting ball.
     * <p>
     * The list holds exactly {@link #numberOfBalls()} entries, one per ball.
     * </p>
     *
     * @return the initial velocities, in ball order.
     */
    List<Velocity> initialBallVelocities();

    /**
     * How many pixels the paddle moves per key-press frame.
     *
     * @return the paddle step size.
     */
    int paddleSpeed();

    /**
     * The width of the paddle in pixels.
     *
     * @return the paddle width.
     */
    int paddleWidth();

    /**
     * The name shown in the top strip while this level is played.
     *
     * @return the level name.
     */
    String levelName();

    /**
     * The backdrop sprite, drawn beneath everything else.
     *
     * @return the background sprite.
     */
    Sprite getBackground();

    /**
     * The blocks that make up this level.
     * <p>
     * <b>Every call must return a new list of newly constructed Blocks.</b>
     * GameLevel mutates the blocks it receives -- it attaches hit listeners to
     * them, and later it will track how many hits each has taken. If a level
     * returned a cached list, replaying the level after losing a life would
     * hand back the half-destroyed blocks from the previous attempt.
     * </p>
     *
     * @return a fresh list of fresh blocks.
     */
    List<Block> blocks();

    /**
     * How many blocks must be destroyed to clear the level.
     * <p>
     * This is deliberately separate from {@code blocks().size()}. A level may
     * contain blocks that are never meant to be destroyed -- decorative or
     * indestructible ones -- and those must not be counted towards the win
     * condition.
     * </p>
     *
     * @return the number of blocks that must be removed.
     */
    int numberOfBlocksToRemove();
}
