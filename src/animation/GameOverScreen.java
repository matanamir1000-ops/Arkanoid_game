package animation;

import java.awt.Color;

/**
 * The screen shown when the player runs out of lives.
 * <p>
 * The score is taken as a plain number rather than as the live Counter. By the
 * time this screen exists the session is over and the score cannot change
 * again, so an int states the truth about it -- and keeps this package free of
 * any dependency on the game package.
 * </p>
 */
public class GameOverScreen extends MessageScreen {
    private final int score;

    /**
     * Constructor.
     *
     * @param score the final score to display.
     */
    public GameOverScreen(int score) {
        super(Color.RED);
        this.score = score;
    }

    @Override
    protected String message() {
        return "Game Over. Your score is " + this.score;
    }
}
