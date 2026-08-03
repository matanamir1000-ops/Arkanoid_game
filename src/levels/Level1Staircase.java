package levels;

import game.Background;
import game.Sprite;
import geometry.Velocity;
import powerups.ExtraBall;
import powerups.LaserPaddle;
import powerups.PaddleExpansion;
import powerups.PowerUp;
import powerups.SlowBall;
import sprites.Block;
import sprites.BlockFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * The opening level: six rows of blocks arranged as a descending staircase.
 * <p>
 * This is the layout the game has always had, moved out of GameLevel and
 * expressed as data. Behaviour is unchanged.
 * </p>
 */
public class Level1Staircase implements LevelInformation {
    private static final String LEVEL_NAME = "Staircase";

    private static final int NUMBER_OF_BALLS = 3;
    private static final int BALL_SPEED_X = 4;
    private static final int BALL_SPEED_Y = -4;

    // No ball may be launched perfectly vertically. The paddle's centre region
    // reflects a ball straight back the way it came, preserving dx -- so a ball
    // with dx == 0 that meets a motionless paddle bounces between the paddle and
    // the top border forever, and the level can never end. Every other bounce in
    // the game preserves or flips dx but never zeroes it, so giving the third
    // ball a slight drift at launch is enough to rule the stalemate out for good.
    private static final int BALL_DRIFT_X = 2;

    private static final int PADDLE_SPEED = 7;
    private static final int PADDLE_WIDTH = 150;

    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final int BORDER_THICKNESS = 20;

    private static final int BLOCKS_IN_FIRST_ROW = 12;
    private static final int BLOCK_WIDTH = 50;
    private static final int BLOCK_HEIGHT = 20;
    private static final int BLOCKS_START_Y = 150;
    private static final Color[] ROW_COLORS = {
        Color.GRAY, Color.RED, Color.YELLOW, Color.CYAN, Color.PINK, Color.GREEN,
    };

    private final int screenWidth;
    private final int screenHeight;

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
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public int numberOfBalls() {
        return NUMBER_OF_BALLS;
    }

    @Override
    public List<Velocity> initialBallVelocities() {
        List<Velocity> velocities = new ArrayList<>();
        velocities.add(new Velocity(BALL_SPEED_X, BALL_SPEED_Y));
        velocities.add(new Velocity(-BALL_SPEED_X, BALL_SPEED_Y));
        velocities.add(new Velocity(BALL_DRIFT_X, BALL_SPEED_Y));
        return velocities;
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

    @Override
    public Sprite getBackground() {
        return new Background(BACKGROUND_COLOR, 0, 0, this.screenWidth, this.screenHeight);
    }

    @Override
    public Color backdropColor() {
        return BACKGROUND_COLOR;
    }

    @Override
    public List<PowerUp> availablePowerUps() {
        List<PowerUp> available = new ArrayList<>();
        available.add(new ExtraBall());
        available.add(new PaddleExpansion());
        available.add(new LaserPaddle());
        available.add(new SlowBall());
        return available;
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
        int rightEdge = this.screenWidth - BORDER_THICKNESS;

        for (int row = 0; row < ROW_COLORS.length; row++) {
            for (int column = 0; column < BLOCKS_IN_FIRST_ROW - row; column++) {
                double x = rightEdge - (column + 1) * BLOCK_WIDTH;
                double y = BLOCKS_START_Y + row * BLOCK_HEIGHT;
                staircase.add(BlockFactory.plain(x, y, BLOCK_WIDTH, BLOCK_HEIGHT, ROW_COLORS[row]));
            }
        }
        return staircase;
    }

    /**
     * Every block in this level is breakable and counts towards the win
     * condition, so the answer is simply how many there are: 57.
     * <p>
     * A level that gains indestructible blocks must stop answering this way and
     * return the breakable count instead -- that is the whole reason the two are
     * separate questions.
     * </p>
     *
     * @return the number of blocks that must be removed.
     */
    @Override
    public int numberOfBlocksToRemove() {
        return this.blocks().size();
    }
}
