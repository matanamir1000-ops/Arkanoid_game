import animation.AnimationRunner;
import biuoop.GUI;
import game.GameFlow;
import levels.Level1Staircase;
import levels.LevelInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * The main entry point for the Arkanoid game.
 * <p>
 * This class is the composition root: it owns the window and the animation
 * runner for the whole session, builds the list of levels to play, and hands
 * everything to GameFlow. Nothing further down the object graph constructs a
 * GUI, and nothing else decides which levels exist.
 * </p>
 */
public class Ass5Game {
    private static final String WINDOW_TITLE = "Arkanoid";
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int FRAMES_PER_SECOND = 60;
    private static final int STARTING_LIVES = 3;

    /**
     * Main entry point to start the game.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        GUI gui = new GUI(WINDOW_TITLE, SCREEN_WIDTH, SCREEN_HEIGHT);
        AnimationRunner runner = new AnimationRunner(gui, FRAMES_PER_SECOND);

        List<LevelInformation> levels = new ArrayList<>();
        levels.add(new Level1Staircase(SCREEN_WIDTH, SCREEN_HEIGHT));

        GameFlow flow = new GameFlow(runner, gui.getKeyboardSensor(),
                SCREEN_WIDTH, SCREEN_HEIGHT, STARTING_LIVES);
        flow.runLevels(levels);
        gui.close();
    }
}
