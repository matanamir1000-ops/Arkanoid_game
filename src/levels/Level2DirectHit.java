package levels;

import game.Sprite;
import geometry.Velocity;
import powerups.PowerUp;
import sprites.Block;
import sprites.BlockFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * One block, and a pair of balls to find it with.
 * <p>
 * Deliberately austere: no power-ups, because a level that lasts a few seconds
 * has nowhere to spend them, and a capsule falling from the only block would
 * arrive after the level was already over.
 * </p>
 */
public class Level2DirectHit extends AbstractLevel {
    private static final String LEVEL_NAME = "Direct Hit";
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final long STAR_SEED = 2L;

    private static final int PADDLE_SPEED = 8;
    private static final int PADDLE_WIDTH = 120;

    private static final int BALL_COUNT = 2;
    private static final double BALL_SPEED = 6;

    private static final int TARGET_WIDTH = 40;
    private static final int TARGET_HEIGHT = 40;
    private static final int TARGET_Y = 140;

    /**
     * Constructor.
     *
     * @param screenWidth  the playfield width.
     * @param screenHeight the playfield height.
     */
    public Level2DirectHit(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
    }

    /**
     * Two balls, both climbing, leaning to opposite sides. Missing the target
     * with one still leaves the other in play, which keeps a level this bare
     * from turning on a single throw.
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
        return new StarfieldBackground(BACKGROUND_COLOR, this.screenWidth(), this.screenHeight(), STAR_SEED);
    }

    @Override
    public Color backdropColor() {
        return BACKGROUND_COLOR;
    }

    /**
     * No power-ups in a level this short.
     *
     * @return an empty list.
     */
    @Override
    public List<PowerUp> availablePowerUps() {
        return new ArrayList<>();
    }

    @Override
    public List<Block> blocks() {
        List<Block> only = new ArrayList<>();
        only.add(BlockFactory.plain((this.screenWidth() - TARGET_WIDTH) / 2.0, TARGET_Y,
                TARGET_WIDTH, TARGET_HEIGHT, Color.RED));
        return only;
    }
}
