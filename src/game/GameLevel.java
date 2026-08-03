package game;

import animation.Animation;
import animation.AnimationRunner;
import animation.KeyPressStoppableAnimation;
import animation.PauseScreen;
import biuoop.DrawSurface;
import biuoop.KeyboardSensor;
import collision.Collidable;
import collision.GameEnvironment;
import geometry.Point;
import geometry.Rectangle;
import geometry.Velocity;
import levels.LevelInformation;
import sprites.Ball;
import sprites.Block;
import sprites.LevelNameIndicator;
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
 * AnimationRunner. The level owns neither the window nor the screen dimensions;
 * both belong to whoever started the session, so that running several levels
 * reuses one window instead of opening a new one per level.
 * </p>
 * <p>
 * What the level contains is not decided here either. A LevelInformation
 * supplies the blocks, balls, paddle dimensions and backdrop; this class turns
 * that description into a running level by wiring in the counters, the listeners
 * and the playfield borders, which are the same for every level.
 * </p>
 */
public class GameLevel implements Animation {
    private static final String PAUSE_KEY_LOWER = "p";
    private static final String PAUSE_KEY_UPPER = "P";

    private static final int BORDER_THICKNESS = 20;
    private static final int SCORE_INDICATOR_HEIGHT = 20;
    private static final Color TOP_STRIP_COLOR = Color.LIGHT_GRAY;
    private static final int LEVEL_CLEAR_BONUS = 100;

    // --- Playfield defaults ---
    // These are the parts of a level that do NOT vary between levels, which is
    // exactly why they live here and not on LevelInformation. The level decides
    // how many balls there are and how fast; the playfield decides where they
    // start and what they look like. If a level ever needs a light backdrop,
    // BALL_COLOR is the first of these that will have to move to the level.
    private static final int BALL_RADIUS = 5;
    private static final Color BALL_COLOR = Color.WHITE;
    private static final int BALL_START_Y = 500;
    private static final int BALL_STACK_GAP = 20;
    private static final int PADDLE_START_Y = 560;
    private static final int PADDLE_HEIGHT = 10;
    private static final Color PADDLE_COLOR = Color.ORANGE;

    private final LevelInformation levelInfo;
    private final AnimationRunner runner;
    private final KeyboardSensor keyboard;
    private final int screenWidth;
    private final int screenHeight;
    private SpriteCollection sprites;
    private GameEnvironment environment;
    private Counter remainingBlocks;
    private Counter remainingBalls;
    private Counter score;
    private BlockRemover blockRemover;
    private ScoreTrackingListener scoreTracker;

    /**
     * Constructor for a new GameLevel.
     * Initializes the internal collections and stores the injected session-wide
     * collaborators.
     *
     * @param levelInfo    the definition of what this level contains.
     * @param runner       the runner that will drive this level's frames.
     * @param keyboard     the keyboard shared by the paddle and the pause screen.
     * @param screenWidth  the playfield width, in pixels.
     * @param screenHeight the playfield height, in pixels.
     */
    public GameLevel(LevelInformation levelInfo, AnimationRunner runner, KeyboardSensor keyboard,
                     int screenWidth, int screenHeight) {
        this.levelInfo = levelInfo;
        this.runner = runner;
        this.keyboard = keyboard;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
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
     * Initialize the level: read the definition, wire in the parts that are the
     * same for every level, and register every GameItem polymorphically in a
     * single pass.
     * <p>
     * The backdrop is added straight to the sprite collection before anything
     * else so that it is drawn underneath every other sprite each frame.
     * </p>
     */
    public void initialize() {
        this.addSprite(this.levelInfo.getBackground());
        this.addSprite(new Background(TOP_STRIP_COLOR, 0, 0, this.screenWidth, SCORE_INDICATOR_HEIGHT));
        List<GameItem> items = new ArrayList<>();
        this.createListenersAndIndicators(items);
        this.createBorders(items);
        this.createBlocks(items);
        this.createBalls(items);
        this.createPaddle(items);

        for (GameItem item : items) {
            item.addToGame(this);
        }
    }

    /**
     * Creates the shared block/score listeners and the two top-strip indicators.
     * The BlockRemover and ScoreTrackingListener are stored on the GameLevel instance so
     * createBlocks can attach the same instances to every gameplay block.
     * <p>
     * Both indicators draw text only; the strip they sit in is painted by its
     * own background sprite in initialize(), so the order they are added in does
     * not matter.
     * </p>
     *
     * @param items the in-progress list of GameItems to append the indicators to.
     */
    private void createListenersAndIndicators(List<GameItem> items) {
        this.blockRemover = new BlockRemover(this, this.remainingBlocks);
        this.scoreTracker = new ScoreTrackingListener(this.score);
        Rectangle topStrip = new Rectangle(new Point(0, 0), this.screenWidth, SCORE_INDICATOR_HEIGHT);
        items.add(new ScoreIndicator(this.score, topStrip));
        items.add(new LevelNameIndicator(this.levelInfo.levelName(), topStrip));
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
                this.screenWidth, BORDER_THICKNESS), Color.LIGHT_GRAY));

        Block deathRegion = new Block(new Rectangle(new Point(0, this.screenHeight - BORDER_THICKNESS),
                this.screenWidth, BORDER_THICKNESS), Color.BLUE);
        BallRemover ballRemover = new BallRemover(this, this.remainingBalls);
        deathRegion.addHitListener(ballRemover);
        items.add(deathRegion);

        int sideBordersStartY = SCORE_INDICATOR_HEIGHT + BORDER_THICKNESS;
        int sideBordersHeight = this.screenHeight - SCORE_INDICATOR_HEIGHT - (2 * BORDER_THICKNESS);

        items.add(new Block(new Rectangle(new Point(0, sideBordersStartY),
                BORDER_THICKNESS, sideBordersHeight), Color.LIGHT_GRAY));
        items.add(new Block(new Rectangle(new Point(this.screenWidth - BORDER_THICKNESS, sideBordersStartY),
                BORDER_THICKNESS, sideBordersHeight), Color.LIGHT_GRAY));
    }

    /**
     * Creates the starting balls described by the level and increments the
     * remainingBalls counter for each.
     * <p>
     * The velocity list is the single source of truth for how many balls there
     * are. LevelInformation also exposes numberOfBalls(), but the two could
     * disagree, and only one of them can actually be honoured -- a ball with no
     * velocity is not a ball. Where the balls start is a playfield concern, so
     * it is decided here: they are stacked upwards from just above the paddle so
     * that they do not begin inside one another.
     * </p>
     *
     * @param items the in-progress list of GameItems to append the balls to.
     */
    private void createBalls(List<GameItem> items) {
        List<Velocity> velocities = this.levelInfo.initialBallVelocities();
        int centreX = this.screenWidth / 2;

        for (int i = 0; i < velocities.size(); i++) {
            Point start = new Point(centreX, BALL_START_Y - i * BALL_STACK_GAP);
            Ball ball = new Ball(start, BALL_RADIUS, BALL_COLOR);
            ball.setVelocity(velocities.get(i));
            items.add(ball);
            this.remainingBalls.increase(1);
        }
    }

    /**
     * Creates the player-controlled paddle, sized and paced by the level and
     * centred horizontally on the playfield.
     *
     * @param items the in-progress list of GameItems to append the paddle to.
     */
    private void createPaddle(List<GameItem> items) {
        int paddleWidth = this.levelInfo.paddleWidth();
        int paddleX = (this.screenWidth - paddleWidth) / 2;
        Rectangle paddleRect = new Rectangle(new Point(paddleX, PADDLE_START_Y),
                paddleWidth, PADDLE_HEIGHT);
        items.add(new Paddle(this.keyboard, paddleRect, PADDLE_COLOR, this.levelInfo.paddleSpeed()));
    }

    /**
     * Takes the blocks the level defines and makes them part of a running game
     * by attaching the removal and scoring listeners to each one.
     * <p>
     * The win condition is read from the level rather than counted from the
     * list. Those two numbers are allowed to differ: a level may hold blocks
     * that are never meant to be destroyed, and clearing the level must not
     * wait for them. Today every block still gets a BlockRemover, so no block
     * actually survives a hit -- the guard that makes indestructible blocks real
     * arrives together with block behaviours, and this counter is the seam it
     * will use.
     * </p>
     *
     * @param items the in-progress list of GameItems to append the blocks to.
     */
    private void createBlocks(List<GameItem> items) {
        for (Block block : this.levelInfo.blocks()) {
            block.addHitListener(this.blockRemover);
            block.addHitListener(this.scoreTracker);
            items.add(block);
        }
        this.remainingBlocks.increase(this.levelInfo.numberOfBlocksToRemove());
    }

    /**
     * Run the level -- hand this level to the runner and report the outcome.
     * <p>
     * The timing loop that used to live here now lives in AnimationRunner. What
     * remains is the part that is genuinely about this level: handing itself
     * over, and deciding what the terminal state means once the runner returns.
     * </p>
     */
    public void run() {
        this.runner.run(this);

        if (this.remainingBlocks.getValue() <= 0) {
            this.score.increase(LEVEL_CLEAR_BONUS);
            System.out.println("You Win!\nYour score is: " + this.score.getValue());
        } else {
            System.out.println("Game Over.\nYour score is: " + this.score.getValue());
        }
    }

    /**
     * Draws and advances the level by one frame.
     *
     * @param d the surface to draw this frame on.
     */
    @Override
    public void doOneFrame(DrawSurface d) {
        this.sprites.drawAllOn(d);
        this.sprites.notifyAllTimePassed();
        this.handlePauseKey();
    }

    /**
     * Opens the pause screen if the pause key is held, and blocks here until it
     * is dismissed.
     * <p>
     * This runs a second animation from inside the current frame. The runner is
     * reentrant, so the pause screen takes over the window until dismissed and
     * then this frame finishes normally. One consequence is worth knowing: the
     * surface belonging to this frame was already drawn before the pause began,
     * and the outer runner shows it only after the pause ends, so a single stale
     * frame flashes on resume.
     * </p>
     */
    private void handlePauseKey() {
        if (this.keyboard.isPressed(PAUSE_KEY_LOWER) || this.keyboard.isPressed(PAUSE_KEY_UPPER)) {
            this.runner.run(new KeyPressStoppableAnimation(
                    this.keyboard, KeyboardSensor.SPACE_KEY, new PauseScreen()));
        }
    }

    /**
     * The level is over once every block is cleared or every ball is lost.
     * <p>
     * This is derived from the counters rather than stored in a flag, so there
     * is exactly one definition of "finished" and the object is valid from
     * construction onward. The runner evaluates it before each frame.
     * </p>
     *
     * @return true once the level has ended.
     */
    @Override
    public boolean shouldStop() {
        return this.remainingBlocks.getValue() <= 0 || this.remainingBalls.getValue() <= 0;
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