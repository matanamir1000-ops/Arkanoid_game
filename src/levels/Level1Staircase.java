package levels;

import game.Sprite;
import geometry.Velocity;
import sprites.Block;
import sprites.BlockFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * The opening level: six rows of blocks arranged as a descending staircase.
 * <p>
 * This is the layout the game has always had, moved out of GameLevel and
 * expressed as data. It opens the game, so it is tuned to teach the controls
 * rather than to test them: a small volley at the slowest pace any level uses,
 * against a wide staircase that is hard to miss.
 * </p>
 */
public class Level1Staircase extends AbstractLevel {
    private static final String LEVEL_NAME = "Staircase";

    private static final int BALL_COUNT = 3;
    private static final double BALL_SPEED = 6;

    private static final int PADDLE_SPEED = 8;
    private static final int PADDLE_WIDTH = 150;

    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final long STAR_SEED = 1L;

    private static final int BLOCKS_IN_FIRST_ROW = 12;
    private static final int BLOCK_WIDTH = 50;
    private static final int BLOCK_HEIGHT = 20;
    private static final int BLOCKS_START_Y = 150;
    private static final Color[] ROW_COLORS = {
        Color.GRAY, Color.RED, Color.YELLOW, Color.CYAN, Color.PINK, Color.GREEN,
    };

    /**
     * Constructs the level for a playfield of the given size.
     * <p>
     * The dimensions are passed in rather than declared here so that the whole
     * program keeps a single owner for them -- the class that opens the window.
     * The staircase is right-aligned against the right border, so it has to know
     * how wide the playfield is.
     * </p>
     *
     * @param screenWidth  the playfield width, in pixels.
     * @param screenHeight the playfield height, in pixels.
     */
    public Level1Staircase(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
    }

    @Override
    public List<Velocity> initialBallVelocities() {
        return this.fannedVelocities(BALL_COUNT, BALL_SPEED);
    }

    @Override
    public int paddleSpeed() {
        return PADDLE_SPEED;
    }

    @Override
    public int paddleWidth() {
        return PADDLE_WIDTH;
    }

    @Override
    public String levelName() {
        return LEVEL_NAME;
    }

    /**
     * A fresh backdrop each call.
     * <p>
     * The starfield animates, so handing out a shared instance would let two
     * levels drive the same twinkle counter. The seed is fixed, so the layout is
     * the same every time this level is played.
     * </p>
     *
     * @return the backdrop for this level.
     */
    @Override
    public Sprite getBackground() {
        return new StarfieldBackground(BACKGROUND_COLOR, this.screenWidth(), this.screenHeight(), STAR_SEED);
    }

    @Override
    public Color backdropColor() {
        return BACKGROUND_COLOR;
    }

    /**
     * Builds the staircase: the top row is the widest and each row below it is
     * one block shorter, all right-aligned against the right border.
     *
     * @return a fresh list of fresh blocks.
     */
    @Override
    public List<Block> blocks() {
        List<Block> staircase = new ArrayList<>();
        int rightEdge = this.screenWidth() - this.borderThickness();

        for (int row = 0; row < ROW_COLORS.length; row++) {
            for (int column = 0; column < BLOCKS_IN_FIRST_ROW - row; column++) {
                double x = rightEdge - (column + 1) * BLOCK_WIDTH;
                double y = BLOCKS_START_Y + row * BLOCK_HEIGHT;
                staircase.add(BlockFactory.plain(x, y, BLOCK_WIDTH, BLOCK_HEIGHT, ROW_COLORS[row]));
            }
        }
        return staircase;
    }
}
