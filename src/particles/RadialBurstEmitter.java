package particles;

import geometry.Point;
import geometry.Velocity;

import java.awt.Color;
import java.util.Random;

/**
 * Throws particles outward in every direction, as when a block shatters.
 * <p>
 * Directions are spread evenly around the circle rather than drawn at random,
 * so a burst always looks like an explosion instead of occasionally clumping to
 * one side. Only the speed and lifetime are randomised, which is enough to stop
 * it looking mechanical.
 * </p>
 */
public class RadialBurstEmitter implements ParticleEmitter {
    private static final int DEFAULT_COUNT = 14;
    private static final double MIN_SPEED = 1.2;
    private static final double SPEED_SPREAD = 2.2;
    private static final int MIN_LIFE = 18;
    private static final int LIFE_SPREAD = 22;
    private static final int PARTICLE_SIZE = 2;
    private static final double FULL_CIRCLE_DEGREES = 360.0;

    private final Random random;
    private final Color fadeTarget;
    private final int count;

    /**
     * Constructor.
     *
     * @param fadeTarget the colour particles fade toward, normally the backdrop.
     * @param count      how many particles a burst throws.
     */
    public RadialBurstEmitter(Color fadeTarget, int count) {
        this.random = new Random();
        this.fadeTarget = fadeTarget;
        this.count = count;
    }

    /**
     * Constructor using the default particle count.
     *
     * @param fadeTarget the colour particles fade toward.
     */
    public RadialBurstEmitter(Color fadeTarget) {
        this(fadeTarget, DEFAULT_COUNT);
    }

    @Override
    public void emitAt(ParticleSystem system, Point where, Color color) {
        for (int i = 0; i < this.count; i++) {
            double angle = FULL_CIRCLE_DEGREES * i / this.count;
            double speed = MIN_SPEED + this.random.nextDouble() * SPEED_SPREAD;
            int life = MIN_LIFE + this.random.nextInt(LIFE_SPREAD);
            system.emit(new Particle(where, Velocity.fromAngleAndSpeed(angle, speed),
                    life, color, this.fadeTarget, PARTICLE_SIZE));
        }
    }
}
