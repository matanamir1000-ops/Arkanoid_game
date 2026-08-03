package levels;

import game.Sprite;
import geometry.Velocity;
import sprites.Block;
import sprites.BlockFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Rows of ordinary blocks divided by columns of steel.
 * <p>
 * The steel is the point of this level. It never breaks, never scores, and is
 * never counted toward clearing the level -- and none of that required a single
 * class anywhere to recognise a steel block. It is a Block like any other,
 * holding a behaviour that answers "no" when asked whether it can be broken.
 * </p>
 */
public class Level4SteelRain extends AbstractLevel {
    private static final String LEVEL_NAME = "Steel Rain";
    private static final Color BACKGROUND_COLOR = new Color(20, 20, 26);
    private static final long STAR_SEED = 4L;

    private static final int PADDLE_SPEED = 7;
    private static final int PADDLE_WIDTH = 140;

    private static final int BALL_COUNT = 2;
    private static final double BALL_SPEED = 6;
    private static final double LEFT_ANGLE = -40;
    private static final double RIGHT_ANGLE = 40;

    private static final int COLUMNS = 12;
    private static final int ROWS = 4;
    private static final int BLOCK_WIDTH = 55;
    private static final int BLOCK_HEIGHT = 24;
    private static final int GRID_TOP = 130;
    private static final int GRID_LEFT = 60;
    private static final int STEEL_EVERY = 4;
    private static final Color STEEL_COLOR = new Color(140, 140, 150);
    private static final Color[] ROW_COLORS = {
        Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.GREEN,
    };

    /**
     * Constructor.
     *
     * @param screenWidth  the playfield width.
     * @param screenHeight the playfield height.
     */
    public Level4SteelRain(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
    }

    @Override
    public int numberOfBalls() {
        return BALL_COUNT;
    }

    @Override
    public List<Velocity> initialBallVelocities() {
        List<Velocity> velocities = new ArrayList<>();
        velocities.add(Velocity.fromAngleAndSpeed(LEFT_ANGLE, BALL_SPEED));
        velocities.add(Velocity.fromAngleAndSpeed(RIGHT_ANGLE, BALL_SPEED));
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
        return new StarfieldBackground(BACKGROUND_COLOR, this.screenWidth(), this.screenHeight(), STAR_SEED);
    }

    @Override
    public Color backdropColor() {
        return BACKGROUND_COLOR;
    }

    @Override
    public List<Block> blocks() {
        List<Block> grid = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                double x = GRID_LEFT + column * BLOCK_WIDTH;
                double y = GRID_TOP + row * BLOCK_HEIGHT;
                if (column % STEEL_EVERY == STEEL_EVERY - 1) {
                    grid.add(BlockFactory.steel(x, y, BLOCK_WIDTH, BLOCK_HEIGHT, STEEL_COLOR));
                } else {
                    grid.add(BlockFactory.plain(x, y, BLOCK_WIDTH, BLOCK_HEIGHT, ROW_COLORS[row]));
                }
            }
        }
        return grid;
    }
}
