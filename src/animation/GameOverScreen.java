package animation;

import biuoop.DrawSurface;
import graphics.ColorFade;

import java.awt.Color;
import java.util.Random;

/**
 * The screen shown when the player runs out of lives.
 * <p>
 * The score is taken as a plain number rather than as the live Counter. By the
 * time this screen exists the session is over and the score cannot change
 * again, so an int states the truth about it.
 * </p>
 * <p>
 * Behind the message, a wall of blocks crumbles away a few at a time. The
 * layout is seeded so it is the same wall every time, and the collapse order is
 * fixed for the same reason: a game-over screen that rearranged itself would
 * read as a glitch.
 * </p>
 */
public class GameOverScreen extends MessageScreen {
    private static final int COLUMNS = 16;
    private static final int ROWS = 5;
    private static final int BLOCK_WIDTH = 48;
    private static final int BLOCK_HEIGHT = 22;
    private static final int WALL_TOP = 70;
    private static final int WALL_LEFT = 16;
    private static final int FRAMES_BETWEEN_CRUMBLES = 9;
    private static final double WALL_FADE = 0.55;

    /**
     * Fixes the order the wall falls apart in. Any constant would do; what
     * matters is that it is a constant, so the collapse is the same every time
     * and does not quietly change if the wall is ever resized.
     */
    private static final long CRUMBLE_SEED = 20260614L;

    private final int score;
    private final boolean[] standing;
    private final int[] crumbleOrder;
    private int fallen;

    /**
     * Constructor.
     *
     * @param score the final score to display.
     */
    public GameOverScreen(int score) {
        super(Color.RED);
        this.score = score;
        this.standing = new boolean[COLUMNS * ROWS];
        this.crumbleOrder = new int[COLUMNS * ROWS];
        this.fallen = 0;

        for (int i = 0; i < this.standing.length; i++) {
            this.standing[i] = true;
            this.crumbleOrder[i] = i;
        }
        this.shuffleCrumbleOrder();
    }

    @Override
    protected String message() {
        return "Game Over. Your score is " + this.score;
    }

    /**
     * Draws the wall and knocks another block out of it every so often.
     *
     * @param d     the surface being drawn on.
     * @param frame how many frames this screen has been shown for.
     */
    @Override
    protected void drawBehindMessage(DrawSurface d, int frame) {
        if (frame % FRAMES_BETWEEN_CRUMBLES == 0 && this.fallen < this.crumbleOrder.length) {
            this.standing[this.crumbleOrder[this.fallen]] = false;
            this.fallen++;
        }

        Color brick = ColorFade.towards(Color.DARK_GRAY, backgroundColor(), WALL_FADE);
        d.setColor(brick);
        for (int i = 0; i < this.standing.length; i++) {
            if (!this.standing[i]) {
                continue;
            }
            int x = WALL_LEFT + (i % COLUMNS) * BLOCK_WIDTH;
            int y = WALL_TOP + (i / COLUMNS) * BLOCK_HEIGHT;
            d.fillRectangle(x, y, BLOCK_WIDTH - 2, BLOCK_HEIGHT - 2);
        }
    }

    /**
     * Fixes the order the wall falls apart in.
     * <p>
     * Seeded, so the collapse is the same every time. The shuffle is written
     * out rather than delegated because sorting a random key would be the same
     * work with more machinery.
     * </p>
     */
    private void shuffleCrumbleOrder() {
        Random random = new Random(CRUMBLE_SEED);
        for (int i = this.crumbleOrder.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = this.crumbleOrder[i];
            this.crumbleOrder[i] = this.crumbleOrder[j];
            this.crumbleOrder[j] = swap;
        }
    }
}
