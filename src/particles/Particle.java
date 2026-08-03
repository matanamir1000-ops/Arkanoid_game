package particles;

import biuoop.DrawSurface;
import game.Sprite;
import geometry.Point;
import geometry.Velocity;
import graphics.ColorFade;

import java.awt.Color;

/**
 * One short-lived speck thrown out by an effect.
 * <p>
 * Position and velocity are kept as doubles even though DrawSurface only
 * accepts integer coordinates. If they were stored as ints, any particle
 * drifting slower than one pixel per frame would round its movement to zero and
 * hang motionless in the air -- which is most of them, near the end of their
 * lives.
 * </p>
 * <p>
 * A particle fades by moving toward the background colour rather than by
 * becoming transparent, because DrawSurface has no alpha channel.
 * </p>
 */
public class Particle implements Sprite {
    private static final double GRAVITY = 0.05;
    private static final double DRAG = 0.99;

    private final Color baseColor;
    private final Color fadeTarget;
    private final int maxLife;
    private final int size;
    private double x;
    private double y;
    private double dx;
    private double dy;
    private int life;

    /**
     * Constructor.
     *
     * @param start      where the particle is emitted from.
     * @param velocity   how far it travels each frame.
     * @param life       how many frames the particle lives; must be at least 1.
     * @param baseColor  the colour when newly emitted.
     * @param fadeTarget the colour it fades toward, normally the backdrop.
     * @param size       the diameter to draw, in pixels.
     * @throws IllegalArgumentException if life is less than 1.
     */
    public Particle(Point start, Velocity velocity, int life,
                    Color baseColor, Color fadeTarget, int size) {
        if (life < 1) {
            throw new IllegalArgumentException("life must be at least 1, but was " + life);
        }
        this.x = start.getX();
        this.y = start.getY();
        this.dx = velocity.getDx();
        this.dy = velocity.getDy();
        this.life = life;
        this.maxLife = life;
        this.baseColor = baseColor;
        this.fadeTarget = fadeTarget;
        this.size = size;
    }

    /**
     * Whether this particle has burned out and should be reaped.
     *
     * @return true once the particle has no life left.
     */
    public boolean isDead() {
        return this.life <= 0;
    }

    /**
     * Moves the particle, slows it, and ages it by one frame.
     */
    @Override
    public void timePassed() {
        this.x += this.dx;
        this.y += this.dy;
        this.dy += GRAVITY;
        this.dx *= DRAG;
        this.dy *= DRAG;
        this.life--;
    }

    /**
     * Draws the particle, faded in proportion to how much of its life is spent.
     *
     * @param surface the surface to draw on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        double spent = 1.0 - (this.life / (double) this.maxLife);
        surface.setColor(ColorFade.towards(this.baseColor, this.fadeTarget, spent));
        surface.fillCircle((int) this.x, (int) this.y, this.size);
    }
}
