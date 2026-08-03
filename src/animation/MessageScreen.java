package animation;

import biuoop.DrawSurface;

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
    private static final int MINIMUM_INSET = 10;

    /**
     * Rough width of one character as a fraction of the font size. DrawSurface
     * exposes no way to measure rendered text, so centring has to be estimated.
     * Every centred string in this hierarchy goes through this one constant,
     * which is what makes the estimate tunable in a single place.
     */
    private static final double CHARACTER_WIDTH_RATIO = 0.55;

    private final Color textColor;

    /**
     * Constructor.
     *
     * @param textColor the colour to draw the message in.
     */
    protected MessageScreen(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * The line this screen displays.
     *
     * @return the message text.
     */
    protected abstract String message();

    @Override
    public void doOneFrame(DrawSurface d) {
        d.setColor(BACKGROUND_COLOR);
        d.fillRectangle(0, 0, d.getWidth(), d.getHeight());

        String text = this.message();
        d.setColor(this.textColor);
        d.drawText(this.centredX(d.getWidth(), text), d.getHeight() / 2, text, FONT_SIZE);
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

    /**
     * Estimates the x coordinate that centres the given text.
     * <p>
     * The result never starts off the left edge. It cannot promise the whole
     * string fits: a message long enough to overflow will run off the right
     * instead, which at least keeps its beginning readable.
     * </p>
     *
     * @param surfaceWidth the width of the surface being drawn on.
     * @param text         the text about to be drawn.
     * @return the left edge to start drawing from.
     */
    private int centredX(int surfaceWidth, String text) {
        double estimatedWidth = text.length() * FONT_SIZE * CHARACTER_WIDTH_RATIO;
        int x = (int) ((surfaceWidth - estimatedWidth) / 2);
        if (x < MINIMUM_INSET) {
            return MINIMUM_INSET;
        }
        return x;
    }
}
