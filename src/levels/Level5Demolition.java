package levels;

import game.Sprite;
import geometry.Velocity;
import powerups.LaserPaddle;
import powerups.PaddleExpansion;
import powerups.PowerUp;
import sprites.Block;
import sprites.BlockFactory;
import sprites.PlainBehavior;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * The finale: armoured blocks packed around charges that set each other off.
 * <p>
 * This is the level the block design was built for. A block here can be tough
 * and explosive at once, which needs no combined class because toughness is a
 * behaviour and a blast is a radius -- the two are simply both present.
 * </p>
 */
public class Level5Demolition extends AbstractLevel {
    private static final String LEVEL_NAME = "Demolition";
    private static final Color BACKGROUND_COLOR = new Color(30, 8, 8);
    private static final Color GRID_COLOR = new Color(180, 60, 40);

    private static final int PADDLE_SPEED = 9;
    private static final int PADDLE_WIDTH = 130;

    private static final int BALL_COUNT = 3;
    private static final double BALL_SPEED = 6.5;
    private static final double FIRST_ANGLE = -35;
    private static final double ANGLE_STEP = 35;

    private static final int COLUMNS = 10;
    private static final int ROWS = 5;
    private static final int BLOCK_WIDTH = 60;
    private static final int BLOCK_HEIGHT = 26;
    private static final int GRID_TOP = 120;
    private static final int GRID_LEFT = 90;

    private static final int ARMOUR_HITS = 3;
    private static final double BLAST_RADIUS = 95;
    private static final int CHARGE_EVERY = 7;
    private static final Color CHARGE_COLOR = Color.ORANGE;
    private static final Color[] ARMOUR_PALETTE = {
        new Color(120, 40, 40), new Color(180, 80, 60), new Color(230, 140, 90),
    };

    /**
     * Constructor.
     *
     * @param screenWidth  the playfield width.
     * @param screenHeight the playfield height.
     */
    public Level5Demolition(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
    }

    @Override
    public int numberOfBalls() {
        return BALL_COUNT;
    }

    @Override
    public List<Velocity> initialBallVelocities() {
        List<Velocity> velocities = new ArrayList<>();
        for (int i = 0; i < BALL_COUNT; i++) {
            velocities.add(Velocity.fromAngleAndSpeed(FIRST_ANGLE + i * ANGLE_STEP, BALL_SPEED));
        }
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
        return new GridBackground(BACKGROUND_COLOR, GRID_COLOR, this.screenWidth(), this.screenHeight());
    }

    @Override
    public Color backdropColor() {
        return BACKGROUND_COLOR;
    }

    /**
     * No extra balls here.
     * <p>
     * Chain explosions already clear large areas at once; adding balls on top of
     * that turns the last level into a formality.
     * </p>
     *
     * @return the power-ups this level offers.
     */
    @Override
    public List<PowerUp> availablePowerUps() {
        List<PowerUp> available = new ArrayList<>();
        available.add(new PaddleExpansion());
        available.add(new LaserPaddle());
        return available;
    }

    /**
     * Builds the field: mostly armoured blocks, with a charge every so often.
     * <p>
     * The charges are spaced by a number that shares no factor with the row
     * width, so they land in a different column on each row rather than forming
     * a vertical line that detonates all at once.
     * </p>
     *
     * @return a fresh list of fresh blocks.
     */
    @Override
    public List<Block> blocks() {
        List<Block> field = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                double x = GRID_LEFT + column * BLOCK_WIDTH;
                double y = GRID_TOP + row * BLOCK_HEIGHT;

                if ((row * COLUMNS + column) % CHARGE_EVERY == 0) {
                    field.add(BlockFactory.exploding(x, y, BLOCK_WIDTH, BLOCK_HEIGHT,
                            CHARGE_COLOR, new PlainBehavior(), BLAST_RADIUS));
                } else {
                    field.add(BlockFactory.multiHit(x, y, BLOCK_WIDTH, BLOCK_HEIGHT,
                            ARMOUR_PALETTE[0], ARMOUR_HITS, ARMOUR_PALETTE));
                }
            }
        }
        return field;
    }
}
