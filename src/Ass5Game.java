import animation.AnimationRunner;
import animation.KeyPressStoppableAnimation;
import biuoop.DialogManager;
import biuoop.GUI;
import biuoop.KeyboardSensor;
import game.GameFlow;
import highscores.HighScoresAnimation;
import highscores.HighScoresTable;
import highscores.ScoreInfo;
import levels.Level1Staircase;
import levels.Level2DirectHit;
import levels.Level3WideEasy;
import levels.Level4SteelRain;
import levels.Level5Demolition;
import levels.LevelInformation;

import java.io.File;
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
 * <p>
 * Recording the score also happens here rather than inside the session, because
 * it is the only place that already owns both the window the name is asked
 * through and the file the table lives in.
 * </p>
 */
public class Ass5Game {
    private static final String WINDOW_TITLE = "Arkanoid";
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int FRAMES_PER_SECOND = 60;

    // One life, not three. Three made a session drag: losing a ball costs the
    // player time rather than tension, since the blocks already destroyed stay
    // destroyed. Extra balls are the better way to be generous, and they are a
    // level's decision rather than the session's.
    private static final int STARTING_LIVES = 1;

    private static final String HIGH_SCORES_FILE = "highscores.txt";
    private static final int HIGH_SCORES_KEPT = 10;

    /**
     * Main entry point to start the game.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        GUI gui = new GUI(WINDOW_TITLE, SCREEN_WIDTH, SCREEN_HEIGHT);
        AnimationRunner runner = new AnimationRunner(gui, FRAMES_PER_SECOND);
        KeyboardSensor keyboard = gui.getKeyboardSensor();

        List<LevelInformation> levels = new ArrayList<>();
        levels.add(new Level1Staircase(SCREEN_WIDTH, SCREEN_HEIGHT));
        levels.add(new Level2DirectHit(SCREEN_WIDTH, SCREEN_HEIGHT));
        levels.add(new Level3WideEasy(SCREEN_WIDTH, SCREEN_HEIGHT));
        levels.add(new Level4SteelRain(SCREEN_WIDTH, SCREEN_HEIGHT));
        levels.add(new Level5Demolition(SCREEN_WIDTH, SCREEN_HEIGHT));

        GameFlow flow = new GameFlow(runner, keyboard, SCREEN_WIDTH, SCREEN_HEIGHT, STARTING_LIVES);
        flow.runLevels(levels);

        showHighScores(gui.getDialogManager(), runner, keyboard, flow.getScore());
        gui.close();
    }

    /**
     * Records the session's score if it earned a place, then shows the table.
     *
     * @param dialogs  used to ask for a name; the whole window is not needed.
     * @param runner   the runner that drives the table screen.
     * @param keyboard the keyboard that dismisses it.
     * @param score    the score just achieved.
     */
    private static void showHighScores(DialogManager dialogs, AnimationRunner runner,
                                       KeyboardSensor keyboard, int score) {
        File file = new File(HIGH_SCORES_FILE);
        HighScoresTable table = HighScoresTable.loadFromFile(file, HIGH_SCORES_KEPT);

        if (table.getRank(score) <= table.size()) {
            String name = dialogs.showQuestionDialog("High score", "What is your name?", "Anonymous");
            table.add(new ScoreInfo(name, score));
            table.save(file);
        }

        runner.run(new KeyPressStoppableAnimation(
                keyboard, KeyboardSensor.SPACE_KEY, new HighScoresAnimation(table)));
    }
}
