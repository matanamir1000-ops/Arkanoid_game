package particles;

import geometry.Point;
import geometry.Velocity;

import java.awt.Color;
import java.util.Random;

/**
 * Drops a small number of slow, short-lived particles at a point.
 * <p>
 * Used for a moving object's wake, so it is called every frame rather than once
 * per event. That is why it emits so few and why they die so quickly: the
 * effect comes from the trail being continuously replenished, and a generous
 * emitter would fill the whole particle budget within a second.
 * </p>
 */
public class TrailEmitter implements ParticleEmitter {
    private static final int PARTICLES_PER_CALL = 1;
    private static final double DRIFT = 0.6;
    private static final int MIN_LIFE = 8;
    private static final int LIFE_SPREAD = 8;
    private static final int PARTICLE_SIZE = 2;

    private final Random random;
    private final Color fadeTarget;

    /**
     * Constructor.
     *
     * @param fadeTarget the colour particles fade toward, normally the backdrop.
     */
    public TrailEmitter(Color fadeTarget) {
        this.random = new Random();
        this.fadeTarget = fadeTarget;
    }

    @Override
    public void emitAt(ParticleSystem system, Point where, Color color) {
        for (int i = 0; i < PARTICLES_PER_CALL; i++) {
            double driftX = (this.random.nextDouble() - 0.5) * DRIFT;
            double driftY = (this.random.nextDouble() - 0.5) * DRIFT;
            int life = MIN_LIFE + this.random.nextInt(LIFE_SPREAD);
            system.emit(new Particle(where, new Velocity(driftX, driftY),
                    life, color, this.fadeTarget, PARTICLE_SIZE));
        }
    }
}
