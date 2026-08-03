package graphics;

import biuoop.DrawSurface;

import java.awt.Color;

/**
 * Draws text with a neon glow.
 * <p>
 * DrawSurface offers one way to draw text and no way to blur, shadow or blend
 * it, so the glow is built the only way available: the same string is drawn
 * several times at small pixel offsets, dimmest and furthest out first, with
 * the bright core last on top. The result reads as a halo because each ring is
 * closer to the backdrop than the one inside it.
 * </p>
 * <p>
 * Centring is an estimate. There is no text-measurement API at all, so the
 * width has to be guessed from the character count. Every centred string in the
 * game routes through centredX so that the guess lives in one place and can be
 * corrected in one place -- left-anchored text, such as the top-strip
 * indicators, deliberately does not go through it and does not need to.
 * </p>
 */
public final class TextRenderer {
    private static final double HALO_FADE = 0.7;
    private static final double MID_FADE = 0.35;
    private static final int HALO_OFFSET = 3;
    private static final int HALO_DIAGONAL = 2;
    private static final int MID_OFFSET = 1;
    private static final double CHARACTER_WIDTH_RATIO = 0.55;
    private static final int MINIMUM_INSET = 10;

    /**
     * Not instantiable: this is a way of drawing, not a thing.
     */
    private TextRenderer() {
    }

    /**
     * Draws glowing text.
     * <p>
     * Costs roughly eleven drawText calls per string. That is affordable for
     * headings and scores; it is not affordable per particle, which is why
     * nothing in the particle system uses it.
     * </p>
     *
     * @param d          the surface to draw on.
     * @param x          the left edge of the text.
     * @param y          the text baseline.
     * @param text       the string to draw.
     * @param fontSize   the size to draw at.
     * @param core       the bright colour at the centre.
     * @param background the colour the halo fades toward.
     */
    public static void drawGlowing(DrawSurface d, int x, int y, String text, int fontSize,
                                   Color core, Color background) {
        Color halo = ColorFade.towards(core, background, HALO_FADE);
        Color mid = ColorFade.towards(core, background, MID_FADE);

        d.setColor(halo);
        d.drawText(x - HALO_OFFSET, y, text, fontSize);
        d.drawText(x + HALO_OFFSET, y, text, fontSize);
        d.drawText(x, y - HALO_OFFSET, text, fontSize);
        d.drawText(x, y + HALO_OFFSET, text, fontSize);
        d.drawText(x - HALO_DIAGONAL, y - HALO_DIAGONAL, text, fontSize);
        d.drawText(x + HALO_DIAGONAL, y - HALO_DIAGONAL, text, fontSize);
        d.drawText(x - HALO_DIAGONAL, y + HALO_DIAGONAL, text, fontSize);
        d.drawText(x + HALO_DIAGONAL, y + HALO_DIAGONAL, text, fontSize);

        d.setColor(mid);
        d.drawText(x - MID_OFFSET, y, text, fontSize);
        d.drawText(x + MID_OFFSET, y, text, fontSize);
        d.drawText(x, y - MID_OFFSET, text, fontSize);
        d.drawText(x, y + MID_OFFSET, text, fontSize);

        d.setColor(core);
        d.drawText(x, y, text, fontSize);
    }

    /**
     * Estimates the x coordinate that centres the given text.
     * <p>
     * The result never starts off the left edge. It cannot promise the whole
     * string fits: a message long enough to overflow runs off the right
     * instead, which at least keeps its beginning readable.
     * </p>
     *
     * @param surfaceWidth the width of the surface being drawn on.
     * @param text         the text about to be drawn.
     * @param fontSize     the size it will be drawn at.
     * @return the left edge to start drawing from.
     */
    public static int centredX(int surfaceWidth, String text, int fontSize) {
        double estimatedWidth = text.length() * fontSize * CHARACTER_WIDTH_RATIO;
        int x = (int) ((surfaceWidth - estimatedWidth) / 2);
        if (x < MINIMUM_INSET) {
            return MINIMUM_INSET;
        }
        return x;
    }
}
