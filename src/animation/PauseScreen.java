package animation;

import java.awt.Color;

/**
 * The screen shown while the game is paused.
 */
public class PauseScreen extends MessageScreen {

    /**
     * Constructor.
     */
    public PauseScreen() {
        super(Color.WHITE);
    }

    @Override
    protected String message() {
        return "paused -- press space to continue";
    }
}
