package levels;

import game.Sprite;
import geometry.Velocity;
import powerups.PowerUp;
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
     * <p>
     * This reports the size of {@link #initialBallVelocities()} rather than
     * deciding anything: one ball is created per velocity, so that list is where
     * the count is really set.
     * </p>
     *
     * @return the ball count.
     */
    int numberOfBalls();

    /**
     * The launch velocity of each starting ball. One ball is created per entry,
     * so the length of this list is the level's ball count.
     * <p>
     * <b>No velocity may point straight up.</b> The middle region of the paddle
     * reflects a ball by flipping its vertical direction and leaving the
     * horizontal one alone, so a ball launched with no horizontal movement keeps
     * none: it bounces between the paddle and the ceiling for as long as the
     * player leaves the paddle still, and the level can never be cleared.
     * Nothing else in the game ever brings a ball's horizontal movement to zero,
     * so ruling it out at launch rules it out entirely. Levels extending
     * AbstractLevel can get a set of velocities that already obey this from
     * fannedVelocities.
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
     * The dominant colour of the backdrop.
     * <p>
     * Effects need this because DrawSurface has no alpha channel: nothing can
     * fade out, so a particle fades by approaching the backdrop until it is
     * indistinguishable from it. That only works if it approaches the colour
     * the level actually draws, which is why the level is asked rather than a
     * constant kept somewhere else and hoped to match.
     * </p>
     *
     * @return the colour effects should fade toward.
     */
    java.awt.Color backdropColor();

    /**
     * The blocks that make up this level.
     * <p>
     * <b>Every call must return a new list of newly constructed Blocks.</b>
     * GameLevel mutates the blocks it receives -- it attaches hit listeners to
     * them, and later it will track how many hits each has taken. If a level
     * returned a cached list, replaying the level after losing a life would
     * hand back the half-destroyed blocks from the previous attempt.
     * </p>
     * <p>
     * <b>Every call must also build the same layout.</b> New objects, identical
     * arrangement. The win condition is counted from a separate call to this
     * method, so a level that laid its blocks out differently each time would
     * be played with one field and judged against another -- ending early, or
     * never ending, with nothing to say which. A level wanting variety must
     * decide its layout once, in its constructor, and build the same one here
     * every time.
     * </p>
     *
     * @return a fresh list of fresh blocks.
     */
    List<Block> blocks();

    /**
     * The power-ups a destroyed block in this level may drop.
     * <p>
     * <b>Every call must return a new list.</b> The same reasoning as blocks():
     * a level is replayed after a lost ball, and a cached list would be shared
     * between attempts.
     * </p>
     * <p>
     * An empty list means this level drops nothing, which is how a level opts
     * out entirely. Nothing anywhere has to be told that it has opted out.
     * </p>
     *
     * @return a fresh list of the power-ups available in this level.
     */
    List<PowerUp> availablePowerUps();

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
