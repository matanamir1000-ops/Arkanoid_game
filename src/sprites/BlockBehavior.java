package sprites;

import java.awt.Color;

/**
 * How a block responds to being hit.
 * <p>
 * A block does not know whether it is fragile, tough or indestructible. It
 * holds one of these and delegates to it, so a new kind of block is a new
 * implementation of this interface and no change to Block at all.
 * </p>
 * <p>
 * This is deliberately composition rather than a Block hierarchy. Two reasons
 * decide it. Block.hit holds the edge-threshold reflection maths, and that must
 * exist in exactly one place -- a subclass would either duplicate it or call
 * super.hit, which already notifies the listeners before the subclass has had
 * the chance to decide whether it survived. And behaviours combine: a block
 * that is both tough and explosive is a MultiHitBehavior plus a blast radius,
 * not a fourth class for every pairing.
 * </p>
 * <p>
 * A behaviour owns only the block's own state. Anything that happens to the
 * rest of the world when a block is hit belongs in a HitListener, which is the
 * idiom the game already uses for exactly that.
 * </p>
 */
public interface BlockBehavior {

    /**
     * Records that the block has been hit once.
     *
     * @param hitter the ball that struck the block.
     */
    void registerHit(Ball hitter);

    /**
     * Whether the block has taken all the damage it can survive.
     *
     * @return true once the block should be removed from the game.
     */
    boolean isDestroyed();

    /**
     * Whether this block can ever be destroyed at all.
     * <p>
     * Different from isDestroyed, which asks whether it has been. This asks
     * whether it could be, and it is the same answer for the whole life of the
     * block. A level counts these to know how many blocks clearing it requires,
     * so a level containing indestructible blocks cannot accidentally declare a
     * target it can never reach.
     * </p>
     * <p>
     * <b>A behaviour that answers false here must answer false from
     * isDestroyed() forever.</b> The two are read by different callers: the
     * level counts this one to set its target, and BlockRemover reads the other
     * to decide what to remove. A block that was never counted but is removed
     * anyway decrements a target it was never part of, and the level ends with
     * blocks still standing.
     * </p>
     *
     * @return true if destroying this block is possible.
     */
    boolean isBreakable();

    /**
     * The colour to draw the block in right now.
     * <p>
     * Given the block's own colour, a behaviour may return it unchanged or
     * replace it -- a tough block uses this to show how much damage it has
     * taken.
     * </p>
     *
     * @param baseColor the block's declared colour.
     * @return the colour to draw with this frame.
     */
    Color displayColor(Color baseColor);

    /**
     * What the hit just registered is worth.
     * <p>
     * Read after registerHit, so a behaviour that pays a bonus for the killing
     * blow can tell that this hit was the last one.
     * </p>
     *
     * @return the points to award for the most recent hit.
     */
    int pointsForLastHit();
}
