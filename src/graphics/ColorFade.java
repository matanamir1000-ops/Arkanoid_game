package graphics;

import java.awt.Color;

/**
 * Blends one colour toward another.
 * <p>
 * This exists because DrawSurface has no alpha channel -- passing a colour with
 * transparency throws. Nothing in this game can actually fade out; the closest
 * available thing is to move a colour toward the background until it is
 * indistinguishable from it, which is what this does.
 * </p>
 * <p>
 * The trade-off is worth stating plainly: this only reads as fading against a
 * uniform backdrop. Over a patterned background, a fading particle approaches
 * the single colour it was told to approach rather than whatever happens to be
 * behind it, so the pattern shows through as hard pixels instead of blending.
 * That is the only behaviour this API permits.
 * </p>
 */
public final class ColorFade {
    private static final int MIN_CHANNEL = 0;
    private static final int MAX_CHANNEL = 255;

    /**
     * Not instantiable: this is a calculation, not a thing.
     */
    private ColorFade() {
    }

    /**
     * Interpolates linearly from one colour to another.
     *
     * @param from the colour at t = 0.
     * @param to   the colour at t = 1.
     * @param t    how far along, clamped to the range 0 to 1.
     * @return the blended colour, always built with the three-argument
     *         constructor because the four-argument one is unusable here.
     */
    public static Color towards(Color from, Color to, double t) {
        double amount = t;
        if (amount < 0) {
            amount = 0;
        } else if (amount > 1) {
            amount = 1;
        }
        int red = channel(from.getRed(), to.getRed(), amount);
        int green = channel(from.getGreen(), to.getGreen(), amount);
        int blue = channel(from.getBlue(), to.getBlue(), amount);
        return new Color(red, green, blue);
    }

    /**
     * Interpolates one colour channel and clamps it to a legal value.
     *
     * @param from   the channel value at t = 0.
     * @param to     the channel value at t = 1.
     * @param amount how far along, already clamped to 0 to 1.
     * @return the blended channel value.
     */
    private static int channel(int from, int to, double amount) {
        int value = (int) Math.round(from + (to - from) * amount);
        if (value < MIN_CHANNEL) {
            return MIN_CHANNEL;
        }
        if (value > MAX_CHANNEL) {
            return MAX_CHANNEL;
        }
        return value;
    }
}
