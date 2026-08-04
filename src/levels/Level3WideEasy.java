package levels;

import game.Sprite;
import geometry.Velocity;
import sprites.Block;
import sprites.BlockFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A single wide row, a very wide paddle, and a spread of balls.
 * <p>
 * The balls fan out rather than starting parallel, so the row comes apart from
 * several places at once.
 * </p>
 */
public class Level3WideEasy extends AbstractLevel {
    private static final String LEVEL_NAME = "Wide Easy";
    private static final Color BACKGROUND_COLOR = new Color(12, 24, 48);
    private static final Color GRID_COLOR = new Color(90, 140, 200);

    private static final int PADDLE_SPEED = 8;
    private static final int PADDLE_WIDTH = 280;

    private static final int BALL_COUNT = 5;
    private static final double BALL_SPEED = 6.5;

    private static final int BLOCK_WIDTH = 50;
    private static final int BLOCK_HEIGHT = 22;
    private static final int ROW_Y = 220;
    private static final Color[] BLOCK_COLORS = {
        Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PINK, Color.CYAN,
    };

    /**
     * Constructor.
     *
     * @param screenWidth  the playfield width.
     * @param screenHeight the playfield height.
     */
    public Level3WideEasy(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
    }

    /**
     * A broad fan, to match a paddle broad enough to gather it back in.
     *
     * @return the launch velocities.
     */
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

    @Override
    public Sprite getBackground() {
        return new GridBackground(BACKGROUND_COLOR, GRID_COLOR, this.screenWidth(), this.screenHeight());
    }

    @Override
    public Color backdropColor() {
        return BACKGROUND_COLOR;
    }

    @Override
    public List<Block> blocks() {
        List<Block> row = new ArrayList<>();
        int usable = this.screenWidth() - 2 * this.borderThickness();
        int count = usable / BLOCK_WIDTH;

        for (int i = 0; i < count; i++) {
            double x = this.borderThickness() + i * BLOCK_WIDTH;
            Color color = BLOCK_COLORS[i % BLOCK_COLORS.length];
            row.add(BlockFactory.plain(x, ROW_Y, BLOCK_WIDTH, BLOCK_HEIGHT, color));
        }
        return row;
    }
}
