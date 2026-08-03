package animation;

import biuoop.DrawSurface;
import geometry.Point;
import particles.FireworkEmitter;
import particles.ParticleEmitter;
import particles.ParticleSystem;

import java.awt.Color;
import java.util.Random;

/**
 * The screen shown when every level has been cleared.
 * <p>
 * The score is taken as a plain number rather than as the live Counter. By the
 * time this screen exists the session is over and the score cannot change
 * again, so an int states the truth about it.
 * </p>
 * <p>
 * The fireworks reuse the particle system wholesale. This screen owns its own,
 * unconnected to any level, which is what lets the same machinery decorate a
 * screen that has no game behind it.
 * </p>
 */
public class WinScreen extends MessageScreen {
    private static final Color[] FIREWORK_COLORS = {
        Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GREEN,
    };
    private static final int FRAMES_BETWEEN_LAUNCHES = 18;
    private static final int LAUNCH_MARGIN = 120;
    private static final int LAUNCH_BAND = 260;

    private final int score;
    private final ParticleSystem fireworks;
    private final ParticleEmitter emitter;
    private final Random random;

    /**
     * Constructor.
     *
     * @param score the final score to display.
     */
    public WinScreen(int score) {
        super(Color.GREEN);
        this.score = score;
        this.fireworks = new ParticleSystem();
        this.emitter = new FireworkEmitter(Color.BLACK);
        this.random = new Random();
    }

    @Override
    protected String message() {
        return "You Win! Your score is " + this.score;
    }

    /**
     * Launches a firework every so often and advances the ones already flying.
     *
     * @param d     the surface being drawn on.
     * @param frame how many frames this screen has been shown for.
     */
    @Override
    protected void drawBehindMessage(DrawSurface d, int frame) {
        if (frame % FRAMES_BETWEEN_LAUNCHES == 0) {
            int x = LAUNCH_MARGIN + this.random.nextInt(d.getWidth() - 2 * LAUNCH_MARGIN);
            int y = LAUNCH_MARGIN + this.random.nextInt(LAUNCH_BAND);
            Color color = FIREWORK_COLORS[this.random.nextInt(FIREWORK_COLORS.length)];
            this.emitter.emitAt(this.fireworks, new Point(x, y), color);
        }
        this.fireworks.timePassed();
        this.fireworks.drawOn(d);
    }
}
