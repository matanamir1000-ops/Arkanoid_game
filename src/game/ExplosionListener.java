package game;

import collision.HitListener;
import geometry.Point;
import sprites.Ball;
import sprites.Block;

/**
 * Damages the neighbours of a block that explodes when destroyed.
 * <p>
 * An explosion is something that happens to the rest of the world, so it is a
 * listener rather than a block behaviour. Behaviours own only a block's own
 * state; anything that reaches outside the block belongs here, which is the
 * idiom the game already uses for removal and scoring.
 * </p>
 * <p>
 * Chains come for free. Damaging a neighbour runs that neighbour's own
 * listeners, and this listener is one of them, so an explosion that destroys an
 * explosive block sets it off in turn. No chaining code exists anywhere.
 * </p>
 */
public class ExplosionListener implements HitListener {

    /**
     * How many explosions deep a single chain may go.
     * <p>
     * This is a gameplay limit, not what makes the recursion terminate.
     * Termination is already guaranteed: every damaged neighbour either leaves
     * the block list or is steel, which never explodes, so the list a chain can
     * reach shrinks with every step and is finite to begin with. The cap exists
     * so that one lucky shot into a dense field of explosive blocks does not
     * silently clear half the level.
     * </p>
     */
    private static final int MAX_CHAIN_DEPTH = 4;

    private final GameLevel level;
    private int depth;

    /**
     * Constructor.
     * <p>
     * One instance is shared by every explosive block in a level, which is what
     * lets the chain depth be a plain field rather than global state.
     * </p>
     *
     * @param level the level whose blocks may be caught in the blast.
     */
    public ExplosionListener(GameLevel level) {
        this.level = level;
        this.depth = 0;
    }

    /**
     * Damages every surviving block within the blast radius.
     * <p>
     * Blocks that do not explode are the overwhelming majority, and they leave
     * here immediately: a blast radius of zero is what "does not explode" means,
     * so no type is ever inspected.
     * </p>
     * <p>
     * The block list is iterated as a copy, because damaging a neighbour can
     * destroy it and remove it from that very list while this loop is running.
     * </p>
     *
     * @param beingHit the block that was hit.
     * @param hitter   the ball responsible, credited with the chain damage too.
     */
    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        if (!beingHit.isDestroyed()) {
            return;
        }
        double radius = beingHit.getBlastRadius();
        if (radius <= 0) {
            return;
        }
        if (this.depth >= MAX_CHAIN_DEPTH) {
            return;
        }

        this.depth++;
        try {
            Point centre = beingHit.getCenter();
            for (Block other : this.level.getBlocks()) {
                if (!other.isDestroyed() && centre.distance(other.getCenter()) <= radius) {
                    other.applyDamage(hitter);
                }
            }
        } finally {
            this.depth--;
        }
    }
}
