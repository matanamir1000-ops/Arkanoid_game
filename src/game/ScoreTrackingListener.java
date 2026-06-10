package game;

import collision.HitListener;
import sprites.Ball;
import sprites.Block;

/**
 * A HitListener that updates a score counter when blocks are hit and removed.
 */
public class ScoreTrackingListener implements HitListener {
    private final Counter currentScore;
    private static final int POINTS_PER_BLOCK = 5;

    /**
     * Constructs a new ScoreTrackingListener.
     *
     * @param scoreCounter the counter to be updated when a hit occurs.
     */
    public ScoreTrackingListener(Counter scoreCounter) {
        this.currentScore = scoreCounter;
    }

    /**
     * This method is called whenever the beingHit object is hit.
     * Increases the score counter by 5 points.
     *
     * @param beingHit the block that was hit.
     * @param hitter   the ball that hit the block.
     */
    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        currentScore.increase(POINTS_PER_BLOCK);
    }
}