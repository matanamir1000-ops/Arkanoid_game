package animation;

import biuoop.DrawSurface;
import graphics.ColorFade;
import graphics.TextRenderer;

import java.awt.Color;

/**
 * A full-screen animation that shows a single line of text and waits.
 * <p>
 * Pause, win and game-over are all the same screen with different words, so the
 * drawing lives here once and a subclass supplies only what it has to say.
 * </p>
 * <p>
 * shouldStop always returns false: none of these screens has an opinion about
 * when it ends. Each is wrapped in a KeyPressStoppableAnimation, which owns the
 * dismissal condition and keeps keyboard handling out of the screens entirely.
 * </p>
 * <p>
 * The background is repainted every frame because the runner hands out a fresh
 * surface each time; without the fill, the light default backdrop would swallow
 * the text.
 * </p>
 */
public abstract class MessageScreen implements Animation {
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final int FONT_SIZE = 32;
    private static final double PULSE_RATE = 0.06;
    private static final double PULSE_DEPTH = 0.45;

    private final Color textColor;
    private int frame;

    /**
     * Constructor.
     *
     * @param textColor the colour to draw the message in.
     */
    protected MessageScreen(Color textColor) {
        this.textColor = textColor;
        this.frame = 0;
    }

    /**
     * The line this screen displays.
     *
     * @return the message text.
     */
    protected abstract String message();

    /**
     * Anything a subclass wants drawn behind the message.
     * <p>
     * Called after the backdrop is laid down and before the text, so whatever a
     * subclass draws here appears behind the words rather than over them. Empty
     * by default: most screens are just a sentence.
     * </p>
     * <p>
     * The frame number is handed in rather than counted again by the subclass.
     * There is one clock on this screen and it lives here, so no subclass has to
     * remember to advance a second copy of it.
     * </p>
     *
     * @param d     the surface being drawn on.
     * @param frame how many frames this screen has been shown for.
     */
    protected void drawBehindMessage(DrawSurface d, int frame) {
        // Intentionally empty: a plain message screen has nothing behind its text.
    }

    /**
     * The colour the backdrop is painted in.
     * <p>
     * Overridable, like the message and the decoration, so that a screen wanting
     * a different backdrop is not the one thing a subclass cannot change.
     * </p>
     *
     * @return the backdrop colour, which effects should also fade toward.
     */
    protected Color backgroundColor() {
        return BACKGROUND_COLOR;
    }

    /**
     * A value that swings smoothly between 0 and 1 as the frames pass.
     * <p>
     * Used for the colour pulse rather than a size pulse. Centring is estimated
     * from the font size, so a size that changed every frame would make the text
     * jitter sideways.
     * </p>
     *
     * @param frame the current frame number.
     * @param rate  how fast the pulse swings.
     * @return a value from 0 to 1.
     */
    private static double pulse(int frame, double rate) {
        return (Math.sin(frame * rate) + 1.0) / 2.0;
    }

    /**
     * Draws the backdrop, any decoration, and the pulsing message.
     * <p>
     * The pulse is applied to the colour rather than to the font size. Centring
     * is estimated from the character count and the font size, so a size that
     * changed every frame would make the text jitter sideways.
     * </p>
     *
     * @param d the surface to draw this frame on.
     */
    @Override
    public void doOneFrame(DrawSurface d) {
        Color backdrop = this.backgroundColor();
        d.setColor(backdrop);
        d.fillRectangle(0, 0, d.getWidth(), d.getHeight());

        this.drawBehindMessage(d, this.frame);

        String text = this.message();
        double dim = pulse(this.frame, PULSE_RATE) * PULSE_DEPTH;
        Color pulsing = ColorFade.towards(this.textColor, backdrop, dim);

        TextRenderer.drawGlowing(d, TextRenderer.centredX(d.getWidth(), text, FONT_SIZE),
                d.getHeight() / 2, text, FONT_SIZE, pulsing, backdrop);
        this.frame++;
    }

    /**
     * A message screen never ends on its own.
     * <p>
     * Final because callers depend on it. Every one of these screens is wrapped
     * in a KeyPressStoppableAnimation that supplies the real stopping condition;
     * a subclass that returned true here would end itself before the wrapper
     * ever got the chance, and the wrapper would look broken.
     * </p>
     *
     * @return always false.
     */
    @Override
    public final boolean shouldStop() {
        return false;
    }
}
