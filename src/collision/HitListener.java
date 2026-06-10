package collision;

import sprites.Ball;
import sprites.Block;

/**
 * The HitListener interface should be implemented by objects that want to be notified
 * of hit events.
 */
public interface HitListener {

    /**
     * This method is called whenever the beingHit object is hit.
     *
     * @param beingHit the object that was hit.
     * @param hitter   the Ball that's doing the hitting.
     */
    void hitEvent(Block beingHit, Ball hitter);
}