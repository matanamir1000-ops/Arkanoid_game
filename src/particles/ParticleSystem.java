package particles;

import biuoop.DrawSurface;
import game.GameItem;
import game.GameLevel;
import game.Sprite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds every live particle and draws them all.
 * <p>
 * Particles are deliberately not registered in the game's SpriteCollection.
 * There can be hundreds of them appearing and dying every second, and a
 * particle that removed itself from a collection while that collection was
 * being iterated is exactly the hazard the rest of the game works to avoid.
 * Instead one ParticleSystem is registered, it owns its particles privately,
 * and it reaps the dead ones with an explicit iterator -- the one place in the
 * game where removal during iteration is not merely safe but intended.
 * </p>
 * <p>
 * Keeping them here also puts the population limit somewhere it can actually be
 * enforced.
 * </p>
 */
public class ParticleSystem implements Sprite, GameItem {

    /**
     * The population ceiling.
     * <p>
     * Beyond this, emit does nothing. Dropping the newest rather than the
     * oldest is deliberate: a chain of explosions emits far more than this in a
     * few frames, and discarding the oldest would erase the original burst that
     * started the chain while the player was still watching it.
     * </p>
     */
    private static final int MAX_PARTICLES = 400;

    private final List<Particle> particles;

    /**
     * Constructor.
     */
    public ParticleSystem() {
        this.particles = new ArrayList<>();
    }

    /**
     * Adds a particle, unless the system is already full.
     *
     * @param particle the particle to add.
     */
    public void emit(Particle particle) {
        if (this.particles.size() >= MAX_PARTICLES) {
            return;
        }
        this.particles.add(particle);
    }

    /**
     * How many particles are currently alive.
     *
     * @return the live particle count.
     */
    public int count() {
        return this.particles.size();
    }

    /**
     * Discards every particle at once.
     * <p>
     * Needed between turns. A level survives losing a ball, and so would the
     * particles left over from the moment it was lost -- and because the
     * countdown deliberately draws the board without advancing it, they would
     * hang frozen in mid-air through the whole of the next count-in.
     * </p>
     */
    public void clear() {
        this.particles.clear();
    }

    /**
     * Advances every particle and reaps the ones that have burned out.
     */
    @Override
    public void timePassed() {
        Iterator<Particle> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.timePassed();
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }

    /**
     * Draws every live particle.
     *
     * @param surface the surface to draw on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        for (Particle particle : this.particles) {
            particle.drawOn(surface);
        }
    }

    /**
     * Registers the system as a sprite.
     * <p>
     * It must be added last, after every other sprite, so that particles are
     * drawn on top of the board rather than underneath it.
     * </p>
     *
     * @param game the level to add this system to.
     */
    @Override
    public void addToGame(GameLevel game) {
        game.addSprite(this);
    }
}
