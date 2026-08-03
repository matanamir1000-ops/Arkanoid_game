package particles;

import biuoop.DrawSurface;
import game.GameItem;
import game.GameLevel;
import game.Sprite;
import geometry.Point;
import sprites.Ball;

/**
 * Leaves a wake of particles behind every ball in play.
 * <p>
 * The trail watches the balls rather than a ball owning a trail. Ball is not
 * modified at all by this: it has no idea it is being followed, and it needs no
 * reference to a particle system or to the level. Inverting the dependency this
 * way is what keeps the effect entirely optional -- a level with no trail
 * simply never creates one.
 * </p>
 * <p>
 * One trail serves the whole level rather than one per ball. A per-ball trail
 * would still be registered as a sprite after its ball had been lost, and would
 * go on emitting at the position where the ball died for the rest of the turn.
 * Reading the level's live balls each frame means a ball's wake stops exactly
 * when the ball does, with nothing to clean up.
 * </p>
 * <p>
 * This sprite draws nothing itself. It exists for its timePassed hook, which is
 * the per-frame moment it needs in order to sample where the balls now are. The
 * particles it emits are drawn by the particle system, which is registered
 * after every other sprite so that they appear on top.
 * </p>
 */
public class BallTrail implements Sprite, GameItem {
    private final BallSource level;
    private final ParticleSystem system;
    private final ParticleEmitter emitter;

    /**
     * Constructor.
     * <p>
     * The level arrives as a BallSource rather than as a GameLevel, so this
     * class is given the one question it needs to ask and no way to reach
     * anything else.
     * </p>
     *
     * @param level   the source of balls that should leave a wake.
     * @param system  the system the trail particles go into.
     * @param emitter decides the shape of the trail.
     */
    public BallTrail(BallSource level, ParticleSystem system, ParticleEmitter emitter) {
        this.level = level;
        this.system = system;
        this.emitter = emitter;
    }

    /**
     * Emits this frame's worth of trail behind each live ball.
     */
    @Override
    public void timePassed() {
        for (Ball ball : this.level.getBalls()) {
            this.emitter.emitAt(this.system,
                    new Point(ball.getX(), ball.getY()), ball.getColor());
        }
    }

    /**
     * Draws nothing: the particles this trail emits belong to the particle
     * system, which draws them itself.
     *
     * @param surface the surface that would be drawn on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        // Intentionally empty: emitted particles are drawn by the ParticleSystem.
    }

    @Override
    public void addToGame(GameLevel game) {
        game.addSprite(this);
    }
}
