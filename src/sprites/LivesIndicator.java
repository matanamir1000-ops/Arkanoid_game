package sprites;

import game.Counter;
import geometry.Rectangle;

/**
 * Shows how many lives the player has left.
 * <p>
 * It reads a Counter it does not own. The counter belongs to GameFlow and
 * survives across levels, which is what lets this indicator keep showing the
 * right number without knowing anything about level transitions.
 * </p>
 */
public class LivesIndicator extends TextIndicator {
    private final Counter lives;

    /**
     * Constructs a new LivesIndicator.
     *
     * @param lives the lives counter to display.
     * @param rect  the section of the top strip this indicator draws inside.
     */
    public LivesIndicator(Counter lives, Rectangle rect) {
        super(rect);
        this.lives = lives;
    }

    @Override
    protected String text() {
        return "Lives: " + this.lives.getValue();
    }
}
