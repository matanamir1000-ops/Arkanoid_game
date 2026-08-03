package game;

import collision.HitListener;
import sprites.Ball;
import sprites.Block;

/**
 * A BlockRemover is in charge of removing blocks from the game, as well as keeping count
 * of the number of blocks that remain.
 */
public class BlockRemover implements HitListener {
    private final GameLevel game;
    private final Counter remainingBlocks;

    /**
     * Constructor for the BlockRemover.
     *
     * @param game            the game from which blocks will be removed
     * @param remainingBlocks the counter tracking the number of remaining blocks
     */
    public BlockRemover(GameLevel game, Counter remainingBlocks) {
        this.game = game;
        this.remainingBlocks = remainingBlocks;
    }

    /**
     * Removes a block once it has taken all the damage it can survive.
     * <p>
     * This one guard is the whole of the special-block system as far as removal
     * is concerned. A tough block reports itself undestroyed until its last hit
     * and a steel block never reports itself destroyed at all, so both are
     * handled here without this class knowing that either exists.
     * </p>
     *
     * @param beingHit the block that was hit
     * @param hitter   the ball that hit the block
     */
    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        if (!beingHit.isDestroyed()) {
            return;
        }
        beingHit.removeFromGame(this.game);
        beingHit.removeHitListener(this);
        this.remainingBlocks.decrease(1);
    }
}