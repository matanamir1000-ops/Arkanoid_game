package game;

import collision.HitListener;
import sprites.Ball;
import sprites.Block;

/**
 * A HitListener that updates a score counter when blocks are hit and removed.
 */
public class ScoreTrackingListener implements HitListener {
    private final Counter currentScore;

    /**
     * Constructs a new ScoreTrackingListener.
     *
     * @param scoreCounter the counter to be updated when a hit occurs.
     */
    public ScoreTrackingListener(Counter scoreCounter) {
        this.currentScore = scoreCounter;
    }

    /**
     * Awards whatever the hit was worth.
     * <p>
     * The block is asked what its own hit is worth rather than being paid a
     * fixed rate. That is what lets a tough block pay per hit and a bonus for
     * the last one, and a steel block pay nothing, without this class knowing
     * that either kind of block exists.
     * </p>
     *
     * @param beingHit the block that was hit.
     * @param hitter   the ball that hit the block.
     */
    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        this.currentScore.increase(beingHit.pointsForLastHit());
    }
}