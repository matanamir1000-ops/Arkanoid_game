package particles;

import collision.HitListener;
import sprites.Ball;
import sprites.Block;

/**
 * Throws a burst of particles where a block was destroyed.
 * <p>
 * Purely cosmetic, and deliberately built as a listener so that it is: it
 * observes the same hit event that removal and scoring observe, and removing it
 * from a level takes away the effect and nothing else. No other class knows it
 * exists.
 * </p>
 * <p>
 * It fires only when the block actually breaks. A tough block hit for the first
 * time should not shower the screen as though it had shattered.
 * </p>
 */
public class ParticleHitListener implements HitListener {
    private final ParticleSystem system;
    private final ParticleEmitter emitter;

    /**
     * Constructor.
     *
     * @param system  the system the burst goes into.
     * @param emitter decides the shape of the burst.
     */
    public ParticleHitListener(ParticleSystem system, ParticleEmitter emitter) {
        this.system = system;
        this.emitter = emitter;
    }

    @Override
    public void hitEvent(Block beingHit, Ball hitter) {
        if (!beingHit.isDestroyed()) {
            return;
        }
        this.emitter.emitAt(this.system, beingHit.getCenter(), beingHit.getDisplayColor());
    }
}
