package game;

import collision.HitListener;
import sprites.Ball;
import sprites.Block;

/**
 * A BallRemover is in charge of removing balls from the game, as well as keeping count
 * of the number of balls that remain.
 */
public class BallRemover implements HitListener {
    private final Game game;
    private final Counter remainingBalls;

    /**
     * Constructor for the BallRemover.
     *
     * @param game           the game from which balls will be removed
     * @param remainingBalls the counter tracking the number of remaining balls
     */
    public BallRemover(Game game, Counter remainingBalls) {
        this.game = game;
        this.remainingBalls = remainingBalls;
    }

    /**
     * Balls that hit the "death region" block should be removed from the game.
     * This method removes the ball from the game and decreases the balls counter.
     *
     * @param beingHit the block that was hit (the death region)
     * @param hitter   the ball that hit the block and needs to be removed
     */
    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        hitter.removeFromGame(this.game);
        this.remainingBalls.decrease(1);
    }
}