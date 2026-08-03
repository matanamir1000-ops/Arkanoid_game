package sprites;

import game.Counter;
import geometry.Rectangle;

/**
 * Shows the current score.
 * <p>
 * It reads a Counter it does not own. The counter belongs to GameFlow and is
 * shared by reference with the listener that adds to it, which is what keeps
 * the display correct without the two knowing about each other.
 * </p>
 */
public class ScoreIndicator extends TextIndicator {
    private final Counter score;

    /**
     * Constructs a new ScoreIndicator.
     *
     * @param score the score counter to display.
     * @param rect  the section of the top strip this indicator draws inside.
     */
    public ScoreIndicator(Counter score, Rectangle rect) {
        super(rect);
        this.score = score;
    }

    @Override
    protected String text() {
        return "Score: " + this.score.getValue();
    }
}
