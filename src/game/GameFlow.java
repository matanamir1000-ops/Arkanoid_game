package game;

import animation.AnimationRunner;
import biuoop.KeyboardSensor;
import levels.LevelInformation;

import java.util.List;

/**
 * Runs a whole session: a sequence of levels, a score and a stock of lives.
 * <p>
 * This is the class that knows what losing a ball costs and what finishing a
 * level is worth. A GameLevel knows only how to play one turn of itself and
 * when that turn has ended; everything that spans turns or spans levels is
 * decided here.
 * </p>
 * <p>
 * The score and lives counters live here because they outlive any single level.
 * They are handed to each level by reference, which is what lets the on-screen
 * indicators keep showing the right numbers across a level transition without
 * anything being copied.
 * </p>
 */
public class GameFlow {
    private static final int LEVEL_CLEAR_BONUS = 100;

    private final AnimationRunner runner;
    private final KeyboardSensor keyboard;
    private final int screenWidth;
    private final int screenHeight;
    private final Counter score;
    private final Counter lives;

    /**
     * Constructor.
     *
     * @param runner        the runner that drives every animation in the session.
     * @param keyboard      the keyboard shared by every level.
     * @param screenWidth   the playfield width, in pixels.
     * @param screenHeight  the playfield height, in pixels.
     * @param startingLives how many lives the player begins with.
     */
    public GameFlow(AnimationRunner runner, KeyboardSensor keyboard,
                    int screenWidth, int screenHeight, int startingLives) {
        this.runner = runner;
        this.keyboard = keyboard;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.score = new Counter(0);
        this.lives = new Counter(startingLives);
    }

    /**
     * Plays the given levels in order until they are all cleared or the player
     * runs out of lives.
     * <p>
     * Each level is played turn by turn. A turn that ends with no balls left
     * costs a life; a turn that ends with no blocks left clears the level and
     * earns the completion bonus. The loop condition asks about lives rather
     * than about balls, because balls only exist while a turn is being played --
     * before the first turn there are none, and a loop that waited for them
     * would never start.
     * </p>
     * <p>
     * Clearing is checked before a life is charged, and that order matters. With
     * several balls in play the last block can be destroyed on the same frame
     * the last ball is lost, so both conditions can be true at once. Asking the
     * level whether it is cleared first means a level that was won is never also
     * billed for a life -- and never reported as a loss because the life it was
     * wrongly charged happened to be the final one.
     * </p>
     *
     * @param levels the levels to play, in order.
     */
    public void runLevels(List<LevelInformation> levels) {
        for (LevelInformation levelInfo : levels) {
            GameLevel level = new GameLevel(levelInfo, this.runner, this.keyboard,
                    this.screenWidth, this.screenHeight, this.score, this.lives);
            level.initialize();

            while (this.lives.getValue() > 0 && !level.isCleared()) {
                level.playOneTurn();
                if (!level.isCleared()) {
                    this.lives.decrease(1);
                }
            }

            if (!level.isCleared()) {
                System.out.println("Game Over.\nYour score is: " + this.score.getValue());
                return;
            }
            this.score.increase(LEVEL_CLEAR_BONUS);
        }
        System.out.println("You Win!\nYour score is: " + this.score.getValue());
    }
}
