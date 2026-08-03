package geometry;

/**
 * Shared geometric utilities and tolerances.
 *
 * <p>Floating point coordinates accumulate rounding error as they are added and
 * multiplied, so two values that are conceptually the same are rarely bit-for-bit
 * equal. Every comparison in this project therefore tests whether the difference
 * between two values falls below a shared tolerance rather than testing for exact
 * equality.</p>
 *
 * <p>The tolerance is exposed through a static accessor rather than a public
 * constant field so that no class needs a visible field to share it.</p>
 */
public final class Geometry {
    /**
     * The tolerance below which two coordinates are treated as identical.
     */
    private static final double EPSILON = 0.000001;

    /**
     * Prevents instantiation of this utility class.
     */
    private Geometry() {
    }

    /**
     * Returns the shared comparison tolerance used for all floating point
     * coordinate comparisons in the game.
     *
     * @return the comparison tolerance.
     */
    public static double epsilon() {
        return EPSILON;
    }
}
