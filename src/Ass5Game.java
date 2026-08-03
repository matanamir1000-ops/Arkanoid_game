import animation.AnimationRunner;
import biuoop.GUI;
import game.GameLevel;
import levels.Level1Staircase;

/**
 * The main entry point for the Arkanoid game.
 * <p>
 * This class is the composition root: it owns the window and the animation
 * runner for the whole session and injects them into the level. Nothing further
 * down the object graph constructs a GUI.
 * </p>
 */
public class Ass5Game {
    private static final String WINDOW_TITLE = "Arkanoid";
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int FRAMES_PER_SECOND = 60;

    /**
     * Main entry point to start the game.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        GUI gui = new GUI(WINDOW_TITLE, SCREEN_WIDTH, SCREEN_HEIGHT);
        AnimationRunner runner = new AnimationRunner(gui, FRAMES_PER_SECOND);
        GameLevel game = new GameLevel(new Level1Staircase(SCREEN_WIDTH, SCREEN_HEIGHT), runner,
                gui.getKeyboardSensor(), SCREEN_WIDTH, SCREEN_HEIGHT);
        game.initialize();
        game.run();
        gui.close();
    }
}
