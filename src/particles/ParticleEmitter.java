package particles;

import geometry.Point;

import java.awt.Color;

/**
 * Decides what a particular effect throws into a particle system.
 * <p>
 * An emitter owns the shape of an effect -- how many particles, how fast, in
 * which directions, how long they live. Whoever triggers the effect knows only
 * where it happens and what colour it is, so a block burst, a ball trail and a
 * firework are the same call to different emitters.
 * </p>
 */
public interface ParticleEmitter {

    /**
     * Throws this effect's particles into the given system.
     *
     * @param system the system to emit into; it may silently refuse if full.
     * @param where  the point the effect happens at.
     * @param color  the colour the particles start out.
     */
    void emitAt(ParticleSystem system, Point where, Color color);
}
