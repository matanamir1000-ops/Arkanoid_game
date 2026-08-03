package game;

import animation.Animation;
import animation.AnimationRunner;
import animation.KeyPressStoppableAnimation;
import animation.PauseScreen;
import biuoop.DrawSurface;
import biuoop.GUI;
import biuoop.KeyboardSensor;
import collision.Collidable;
import collision.GameEnvironment;
import collision.HitListener;
import geometry.Point;
import geometry.Rectangle;
import sprites.Ball;
import sprites.Block;
import sprites.Paddle;
import sprites.ScoreIndicator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game environment, sprites, and gameplay frame of a single level.
 * <p>
 * The level is an Animation: it knows how to draw and advance one frame and
 * when it is finished, but the timing loop that calls it lives in the injected
 * AnimationRunner. The window is injected too rather than constructed here, so
 * that a session running several levels reuses one window instead of opening a
 * new one per level.
 * </p>
 */
public class GameLevel implements Animation {
    private SpriteCollection sprites;
    private GameEnvironment environment;
    private GUI gui;
    private AnimationRunner runner;
    private KeyboardSensor keyboard;
    private boolean running;
    private Counter remainingBlocks;
    private Counter remainingBalls;
    private Counter score;
    private BlockRemover blockRemover;
    private ScoreTrackingListener scoreTracker;
    // Constants for screen dimensions
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int BORDER_THICKNESS = 20;

    // --- Background color constants ---
    private static final int BACKGROUND_ORIGIN_X = 0;
    private static final int BACKGROUND_ORIGIN_Y = 0;
    private static final java.awt.Color BACKGROUND_COLOR = java.awt.Color.BLACK;

    // --- Game Item Setup Constants ---
    private static final int BALL_START_X = 400;
    private static final int BALL_1_START_Y = 500;
    private static final int BALL_2_START_Y = 480;
    private static final int BALL_3_START_Y = 460;
    private static final int BALL_RADIUS = 5;
    private static final int BALL_SPEED_X = 4;
    private static final int BALL_SPEED_Y = -4;
    private static final int PADDLE_START_X = 350;
    private static final int PADDLE_START_Y = 560;
    private static final int PADDLE_WIDTH = 150;
    private static final int PADDLE_HEIGHT = 10;
    private static final int LEVEL_CLEAR_BONUS = 100;


    // --- Block Pattern Constants ---
    private static final int BLOCKS_IN_FIRST_ROW = 12;
    private static final int BLOCK_WIDTH = 50;
    private static final int BLOCK_HEIGHT = 20;
    private static final int BLOCKS_START_Y = 150;
    private static final int SCORE_INDICATOR_HEIGHT = 20;

    /**
     * Constructor for a new GameLevel.
     * Initializes the internal collections and stores the injected session-wide
     * collaborators.
     *
     * @param runner   the runner that will drive this level's frames.
     * @param keyboard the keyboard shared by the paddle and the pause screen.
     * @param gui      the window this level draws into.
     */
    public GameLevel(AnimationRunner runner, KeyboardSensor keyboard, GUI gui) {
        this.runner = runner;
        this.keyboard = keyboard;
        this.gui = gui;
        this.running = false;
        this.sprites = new SpriteCollection();
        this.environment = new GameEnvironment();
        this.remainingBlocks = new Counter(0);
        this.remainingBalls = new Counter(0);
        this.score = new Counter(0);
    }

    /**
     * Adds a collidable object to the game environment.
     *
     * @param collidable the collidable to add.
     */
    public void addCollidable(Collidable collidable) {
        this.environment.addCollidable(collidable);
    }

    /**
     * Adds a sprite to the sprite collection.
     *
     * @param sprite the sprite to add.
     */
    public void addSprite(Sprite sprite) {
        this.sprites.addSprite(sprite);
    }

    /**
     * Initialize a new game: create the background, Blocks, Ball, Paddle and borders.
     * Delegates each concern to a focused helper and then registers every GameItem
     * polymorphically in a single pass.
     */
    public void initialize() {
        this.createBackground();
        List<GameItem> items = new ArrayList<>();
        this.createListenersAndIndicators(items);
        this.createBorders(items);
        items.addAll(this.createBlockPattern(this.blockRemover, this.scoreTracker));
        this.createBalls(items);
        this.createPaddle(items);

        for (GameItem item : items) {
            item.addToGame(this);
        }
    }

    /**
     * Adds the solid background sprite directly to the sprite collection.
     * The background is registered first so that it is drawn underneath every
     * other sprite each frame.
     */
    private void createBackground() {
        this.addSprite(new Background(BACKGROUND_COLOR, BACKGROUND_ORIGIN_X,
                BACKGROUND_ORIGIN_Y, SCREEN_WIDTH, SCREEN_HEIGHT));
    }

    /**
     * Creates the shared block/score listeners and the on-screen score indicator.
     * The BlockRemover and ScoreTrackingListener are stored on the GameLevel instance so
     * createBlockPattern can attach the same instances to every gameplay block.
     *
     * @param items the in-progress list of GameItems to append the indicator to.
     */
    private void createListenersAndIndicators(List<GameItem> items) {
        this.blockRemover = new BlockRemover(this, this.remainingBlocks);
        this.scoreTracker = new ScoreTrackingListener(this.score);
        Rectangle scoreRect = new Rectangle(new Point(0, 0), SCREEN_WIDTH, SCORE_INDICATOR_HEIGHT);
        items.add(new ScoreIndicator(this.score, scoreRect));
    }

    /**
     * Creates the four playfield borders: top, two sides, and the invisible
     * death-region at the bottom. The death-region block is wired with a
     * BallRemover so any ball that crosses it is removed from the game.
     *
     * @param items the in-progress list of GameItems to append the borders to.
     */
    private void createBorders(List<GameItem> items) {
        items.add(new Block(new Rectangle(new Point(0, SCORE_INDICATOR_HEIGHT),
                SCREEN_WIDTH, BORDER_THICKNESS), Color.LIGHT_GRAY));

        Block deathRegion = new Block(new Rectangle(new Point(0, SCREEN_HEIGHT - BORDER_THICKNESS),
                SCREEN_WIDTH, BORDER_THICKNESS), Color.BLUE);
        BallRemover ballRemover = new BallRemover(this, this.remainingBalls);
        deathRegion.addHitListener(ballRemover);
        items.add(deathRegion);

        int sideBordersStartY = SCORE_INDICATOR_HEIGHT + BORDER_THICKNESS;
        int sideBordersHeight = SCREEN_HEIGHT - SCORE_INDICATOR_HEIGHT - (2 * BORDER_THICKNESS);

        items.add(new Block(new Rectangle(new Point(0, sideBordersStartY),
                BORDER_THICKNESS, sideBordersHeight), Color.LIGHT_GRAY));
        items.add(new Block(new Rectangle(new Point(SCREEN_WIDTH - BORDER_THICKNESS, sideBordersStartY),
                BORDER_THICKNESS, sideBordersHeight), Color.LIGHT_GRAY));
    }

    /**
     * Creates the three starting balls with their fixed launch velocities and
     * increments the remainingBalls counter for each.
     *
     * @param items the in-progress list of GameItems to append the balls to.
     */
    private void createBalls(List<GameItem> items) {
        Ball ball = new Ball(new Point(BALL_START_X, BALL_1_START_Y), BALL_RADIUS, Color.WHITE);
        ball.setVelocity(BALL_SPEED_X, BALL_SPEED_Y);
        items.add(ball);
        this.remainingBalls.increase(1);

        Ball ball2 = new Ball(new Point(BALL_START_X, BALL_2_START_Y), BALL_RADIUS, Color.WHITE);
        ball2.setVelocity(-BALL_SPEED_X, BALL_SPEED_Y);
        items.add(ball2);
        this.remainingBalls.increase(1);

        Ball ball3 = new Ball(new Point(BALL_START_X, BALL_3_START_Y), BALL_RADIUS, Color.WHITE);
        ball3.setVelocity(0, BALL_SPEED_Y);
        items.add(ball3);
        this.remainingBalls.increase(1);
    }

    /**
     * Creates the player-controlled paddle bound to the GUI's keyboard sensor.
     *
     * @param items the in-progress list of GameItems to append the paddle to.
     */
    private void createPaddle(List<GameItem> items) {
        Rectangle paddleRect = new Rectangle(new Point(PADDLE_START_X, PADDLE_START_Y),
                PADDLE_WIDTH, PADDLE_HEIGHT);
        items.add(new Paddle(this.keyboard, paddleRect, java.awt.Color.ORANGE));
    }

    /**
     * Creates a staircase pattern of blocks.
     *
     * @param remover the listener that removes a block from the game once it is hit.
     * @param tracker the listener that awards points once a block is hit.
     * @return a list of blocks representing the pattern.
     */
    private List<Block> createBlockPattern(HitListener remover, HitListener tracker) {
        List<Block> rowOfBlocks = new ArrayList<>();
        int rightEdge = SCREEN_WIDTH - BORDER_THICKNESS;

        Color[] rowColors = {Color.GRAY, Color.RED, Color.YELLOW, Color.CYAN, Color.PINK, Color.GREEN};
        for (int i = 0; i < rowColors.length; i++) {
            for (int j = 0; j < BLOCKS_IN_FIRST_ROW - i; j++) {
                double x = rightEdge - (j + 1) * BLOCK_WIDTH;
                double y = BLOCKS_START_Y + i * BLOCK_HEIGHT;
                Block newBlock = new Block(new Rectangle(new Point(x, y), BLOCK_WIDTH, BLOCK_HEIGHT), rowColors[i]);
                newBlock.addHitListener(remover);
                newBlock.addHitListener(tracker);
                this.remainingBlocks.increase(1);
                rowOfBlocks.add(newBlock);
            }
        }
        return rowOfBlocks;
    }

    /**
     * Run the level -- hand this level to the runner and report the outcome.
     * <p>
     * The timing loop that used to live here now lives in AnimationRunner. What
     * remains is the part that is genuinely about this level: arming it, and
     * deciding what the terminal state means once the runner returns.
     * </p>
     */
    public void run() {
        this.running = true;
        this.runner.run(this);

        if (this.remainingBlocks.getValue() <= 0) {
            this.score.increase(LEVEL_CLEAR_BONUS);
            System.out.println("You Win!\nYour score is: " + this.score.getValue());
        } else {
            System.out.println("Game Over.\nYour score is: " + this.score.getValue());
        }
        this.gui.close();
    }

    /**
     * Draws and advances the level by one frame.
     * <p>
     * Pausing is handled by running a second animation from inside this frame.
     * The runner is reentrant, so the pause screen simply takes over the window
     * until it is dismissed and then this frame finishes normally.
     * </p>
     *
     * @param d the surface to draw this frame on.
     */
    @Override
    public void doOneFrame(DrawSurface d) {
        this.sprites.drawAllOn(d);
        this.sprites.notifyAllTimePassed();

        if (this.keyboard.isPressed("p") || this.keyboard.isPressed("P")) {
            this.runner.run(new KeyPressStoppableAnimation(
                    this.keyboard, KeyboardSensor.SPACE_KEY, new PauseScreen()));
        }

        if (this.remainingBlocks.getValue() <= 0 || this.remainingBalls.getValue() <= 0) {
            this.running = false;
        }
    }

    @Override
    public boolean shouldStop() {
        return !this.running;
    }

    /**
     * Removes a collidable from the game's environment.
     *
     * @param collidable the collidable to remove.
     */
    public void removeCollidable(Collidable collidable) {
        this.environment.removeCollidable(collidable);
    }

    /**
     * Removes a sprite from the game's sprite collection.
     *
     * @param sprite the sprite to remove.
     */
    public void removeSprite(Sprite sprite) {
        this.sprites.removeSprite(sprite);
    }

    /**
     * Gets the game environment.
     *
     * @return the current game environment.
     */
    public GameEnvironment getEnvironment() {
        return this.environment;
    }
}