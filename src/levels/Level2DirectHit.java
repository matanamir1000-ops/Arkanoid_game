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
 * One block, one ball, straight up.
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

    private static final double BALL_SPEED_X = 1.5;
    private static final double BALL_SPEED_Y = -5;

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

    @Override
    public int numberOfBalls() {
        return 1;
    }

    /**
     * A single ball, launched with a slight lean.
     * <p>
     * Not straight up. A perfectly vertical ball meeting the centre of a
     * motionless paddle is reflected perfectly vertically again, and bounces
     * between the paddle and the ceiling until the player intervenes.
     * </p>
     *
     * @return the launch velocity.
     */
    @Override
    public List<Velocity> initialBallVelocities() {
        List<Velocity> velocities = new ArrayList<>();
        velocities.add(new Velocity(BALL_SPEED_X, BALL_SPEED_Y));
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
