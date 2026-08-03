package particles;

import geometry.Point;
import geometry.Velocity;

import java.awt.Color;
import java.util.Random;

/**
 * A large, slow, celebratory burst for the win screen.
 * <p>
 * Unlike a block burst, the directions here are randomised rather than spread
 * evenly. A firework is meant to look uneven, and several overlapping ragged
 * bursts read better than several identical rings.
 * </p>
 */
public class FireworkEmitter implements ParticleEmitter {
    private static final int COUNT = 26;
    private static final double MIN_SPEED = 0.8;
    private static final double SPEED_SPREAD = 3.0;
    private static final int MIN_LIFE = 30;
    private static final int LIFE_SPREAD = 30;
    private static final int PARTICLE_SIZE = 3;
    private static final double FULL_CIRCLE_DEGREES = 360.0;

    private final Random random;
    private final Color fadeTarget;

    /**
     * Constructor.
     *
     * @param fadeTarget the colour particles fade toward, normally the backdrop.
     */
    public FireworkEmitter(Color fadeTarget) {
        this.random = new Random();
        this.fadeTarget = fadeTarget;
    }

    @Override
    public void emitAt(ParticleSystem system, Point where, Color color) {
        for (int i = 0; i < COUNT; i++) {
            double angle = this.random.nextDouble() * FULL_CIRCLE_DEGREES;
            double speed = MIN_SPEED + this.random.nextDouble() * SPEED_SPREAD;
            int life = MIN_LIFE + this.random.nextInt(LIFE_SPREAD);
            system.emit(new Particle(where, Velocity.fromAngleAndSpeed(angle, speed),
                    life, color, this.fadeTarget, PARTICLE_SIZE));
        }
    }
}
