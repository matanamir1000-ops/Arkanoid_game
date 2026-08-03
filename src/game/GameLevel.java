package game;

import animation.Animation;
import animation.AnimationRunner;
import animation.CountdownAnimation;
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
import particles.BallSource;
import particles.BallTrail;
import particles.ParticleHitListener;
import particles.ParticleSystem;
import particles.RadialBurstEmitter;
import particles.TrailEmitter;
import sprites.Ball;
import sprites.Block;
import sprites.LevelNameIndicator;
import sprites.LivesIndicator;
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
public class GameLevel implements Animation, BallSource {
    private static final String PAUSE_KEY_LOWER = "p";
    private static final String PAUSE_KEY_UPPER = "P";

    private static final int BORDER_THICKNESS = 20;
    private static final int TOP_STRIP_HEIGHT = 20;
    private static final Color TOP_STRIP_COLOR = Color.LIGHT_GRAY;
    private static final int TOP_STRIP_SECTIONS = 3;

    private static final double COUNTDOWN_SECONDS = 2;
    private static final int COUNTDOWN_FROM = 3;

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
    private final Counter score;
    private final Counter lives;
    private final List<Block> blocks;
    private final List<Ball> balls;
    private final ParticleSystem particles;
    private SpriteCollection sprites;
    private GameEnvironment environment;
    private Counter remainingBlocks;
    private Counter remainingBalls;
    private BlockRemover blockRemover;
    private ScoreTrackingListener scoreTracker;
    private Paddle paddle;

    /**
     * Constructor for a new GameLevel.
     * Initializes the internal collections and stores the injected collaborators.
     * <p>
     * The score and lives counters are passed in rather than created here because
     * they outlive the level: they belong to the session and must carry across
     * level transitions. The level only reads and adds to them.
     * </p>
     *
     * @param levelInfo    the definition of what this level contains.
     * @param runner       the runner that will drive this level's frames.
     * @param keyboard     the keyboard shared by the paddle and the pause screen.
     * @param screenWidth  the playfield width, in pixels.
     * @param screenHeight the playfield height, in pixels.
     * @param score        the session-wide score counter.
     * @param lives        the session-wide lives counter, for display only.
     */
    public GameLevel(LevelInformation levelInfo, AnimationRunner runner, KeyboardSensor keyboard,
                     int screenWidth, int screenHeight, Counter score, Counter lives) {
        this.levelInfo = levelInfo;
        this.runner = runner;
        this.keyboard = keyboard;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.score = score;
        this.lives = lives;
        this.blocks = new ArrayList<>();
        this.balls = new ArrayList<>();
        this.particles = new ParticleSystem();
        this.sprites = new SpriteCollection();
        this.environment = new GameEnvironment();
        this.remainingBlocks = new Counter(0);
        this.remainingBalls = new Counter(0);
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
     * This builds everything that survives losing a ball -- the backdrop, the
     * indicators, the borders, the blocks and the paddle. The balls are not
     * created here: they belong to a single turn, and playOneTurn creates a
     * fresh set each time it is called.
     * </p>
     * <p>
     * The backdrop is added straight to the sprite collection before anything
     * else so that it is drawn underneath every other sprite each frame.
     * </p>
     */
    public void initialize() {
        this.addSprite(this.levelInfo.getBackground());
        this.addSprite(new Background(TOP_STRIP_COLOR, 0, 0, this.screenWidth, TOP_STRIP_HEIGHT));
        List<GameItem> items = new ArrayList<>();
        this.createListenersAndIndicators(items);
        this.createBorders(items);
        this.createBlocks(items);
        this.createPaddle(items);
        this.createEffects(items);

        for (GameItem item : items) {
            item.addToGame(this);
        }
    }

    /**
     * Registers the visual effects that sit on top of the level.
     * <p>
     * Added last, and that ordering is the whole point: sprites are drawn in
     * registration order, so anything registered after this would be painted
     * over the particles instead of under them.
     * </p>
     *
     * @param items the in-progress list of GameItems to append the effects to.
     */
    private void createEffects(List<GameItem> items) {
        items.add(new BallTrail(this, this.particles, new TrailEmitter(this.levelInfo.backdropColor())));
        items.add(this.particles);
    }

    /**
     * Creates the shared block/score listeners and the three top-strip indicators.
     * The BlockRemover and ScoreTrackingListener are stored on the GameLevel instance so
     * createBlocks can attach the same instances to every gameplay block.
     * <p>
     * The strip is divided into three equal sections and each indicator is given
     * one of them. Splitting the space is what keeps them from overlapping:
     * DrawSurface offers no way to measure rendered text, so an indicator cannot
     * know how much room it needs and must instead be told where it may draw.
     * </p>
     *
     * @param items the in-progress list of GameItems to append the indicators to.
     */
    private void createListenersAndIndicators(List<GameItem> items) {
        this.blockRemover = new BlockRemover(this, this.remainingBlocks);
        this.scoreTracker = new ScoreTrackingListener(this.score);

        int sectionWidth = this.screenWidth / TOP_STRIP_SECTIONS;
        Rectangle livesArea = new Rectangle(new Point(0, 0), sectionWidth, TOP_STRIP_HEIGHT);
        Rectangle scoreArea = new Rectangle(new Point(sectionWidth, 0), sectionWidth, TOP_STRIP_HEIGHT);
        Rectangle nameArea = new Rectangle(new Point(2 * sectionWidth, 0), sectionWidth, TOP_STRIP_HEIGHT);

        items.add(new LivesIndicator(this.lives, livesArea));
        items.add(new ScoreIndicator(this.score, scoreArea));
        items.add(new LevelNameIndicator(this.levelInfo.levelName(), nameArea));
    }

    /**
     * Creates the four playfield borders: top, two sides, and the invisible
     * death-region at the bottom. The death-region block is wired with a
     * BallRemover so any ball that crosses it is removed from the game.
     *
     * @param items the in-progress list of GameItems to append the borders to.
     */
    private void createBorders(List<GameItem> items) {
        items.add(new Block(new Rectangle(new Point(0, TOP_STRIP_HEIGHT),
                this.screenWidth, BORDER_THICKNESS), Color.LIGHT_GRAY));

        Block deathRegion = new Block(new Rectangle(new Point(0, this.screenHeight - BORDER_THICKNESS),
                this.screenWidth, BORDER_THICKNESS), Color.BLUE);
        BallRemover ballRemover = new BallRemover(this, this.remainingBalls);
        deathRegion.addHitListener(ballRemover);
        items.add(deathRegion);

        int sideBordersStartY = TOP_STRIP_HEIGHT + BORDER_THICKNESS;
        int sideBordersHeight = this.screenHeight - TOP_STRIP_HEIGHT - (2 * BORDER_THICKNESS);

        items.add(new Block(new Rectangle(new Point(0, sideBordersStartY),
                BORDER_THICKNESS, sideBordersHeight), Color.LIGHT_GRAY));
        items.add(new Block(new Rectangle(new Point(this.screenWidth - BORDER_THICKNESS, sideBordersStartY),
                BORDER_THICKNESS, sideBordersHeight), Color.LIGHT_GRAY));
    }

    /**
     * Creates a fresh set of balls for one turn, stacked above the paddle.
     * <p>
     * The velocity list is the single source of truth for how many balls there
     * are. LevelInformation also exposes numberOfBalls(), but the two could
     * disagree, and only one of them can actually be honoured -- a ball with no
     * velocity is not a ball. Where the balls start is a playfield concern, so
     * it is decided here: they are stacked upwards from just above the paddle so
     * that they do not begin inside one another.
     * </p>
     */
    private void createBallsOnTopOfPaddle() {
        List<Velocity> velocities = this.levelInfo.initialBallVelocities();
        int centreX = this.screenWidth / 2;

        for (int i = 0; i < velocities.size(); i++) {
            Point start = new Point(centreX, BALL_START_Y - i * BALL_STACK_GAP);
            Ball ball = new Ball(start, BALL_RADIUS, BALL_COLOR);
            ball.setVelocity(velocities.get(i));
            ball.addToGame(this);
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
        this.paddle = new Paddle(this.keyboard, paddleRect, PADDLE_COLOR, this.levelInfo.paddleSpeed());
        items.add(this.paddle);
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
        ExplosionListener explosions = new ExplosionListener(this);
        ParticleHitListener burst = new ParticleHitListener(this.particles,
                new RadialBurstEmitter(this.levelInfo.backdropColor()));
        for (Block block : this.levelInfo.blocks()) {
            block.addHitListener(this.blockRemover);
            block.addHitListener(this.scoreTracker);
            block.addHitListener(explosions);
            block.addHitListener(burst);
            this.blocks.add(block);
            items.add(block);
        }
        this.remainingBlocks.increase(this.levelInfo.numberOfBlocksToRemove());
    }

    /**
     * The gameplay blocks still standing in this level.
     * <p>
     * A copy, and necessarily so: the caller is an explosion, and damaging a
     * neighbour can destroy it and remove it from this very list while the
     * caller is still walking it.
     * </p>
     * <p>
     * Only the level's own blocks are listed. The borders and the death region
     * are blocks too, but they were never added here, so an explosion cannot
     * reach them -- which matters, because damaging the death region would
     * destroy the ball that set the explosion off.
     * </p>
     *
     * @return a copy of the level's surviving blocks.
     */
    public List<Block> getBlocks() {
        return new ArrayList<>(this.blocks);
    }

    /**
     * The balls currently in play.
     * <p>
     * A copy, for the same reason the block list is copied: a caller walking
     * this list may cause a ball to be removed from it.
     * </p>
     *
     * @return a copy of the level's live balls.
     */
    @Override
    public List<Ball> getBalls() {
        return new ArrayList<>(this.balls);
    }

    /**
     * Takes a ball into play.
     * <p>
     * Called from Ball.addToGame, so that creating a ball is a single call that
     * cannot be half-done. Anything that spawns a ball -- the start of a turn,
     * or a power-up -- gets a ball that is drawn, trailed and counted, without
     * having to remember three separate registrations.
     * </p>
     *
     * @param ball the ball entering play.
     */
    public void addBall(Ball ball) {
        this.balls.add(ball);
        this.remainingBalls.increase(1);
    }

    /**
     * Forgets a ball that has left the game.
     * <p>
     * Called from Ball.removeFromGame together with the sprite removal, so that
     * a ball leaves both collections in one operation.
     * </p>
     *
     * @param ball the ball to forget.
     */
    public void removeBall(Ball ball) {
        this.balls.remove(ball);
    }

    /**
     * Forgets a block that has left the game.
     * <p>
     * Called from Block.removeFromGame together with the sprite and collidable
     * removals, so that a block leaves all three collections in one operation
     * and no caller can drop it from some of them but not others.
     * </p>
     * <p>
     * Adding is deliberately not symmetric with this. Block.addToGame registers
     * a block as a sprite and a collidable but never as a gameplay block,
     * because the borders and the death region go through that same method and
     * must not become gameplay blocks: an explosion that could reach the death
     * region would destroy the very ball that set it off. Only createBlocks
     * knows which blocks are the level's own, so only createBlocks registers
     * them. This method is a no-op for everything else, which is exactly what
     * makes it safe to call unconditionally from removeFromGame.
     * </p>
     *
     * @param block the block to forget; ignored if it was never a gameplay block.
     */
    public void removeBlock(Block block) {
        this.blocks.remove(block);
    }

    /**
     * Play one turn: reset the paddle, launch a fresh set of balls, count the
     * player in, and then run frames until the turn ends.
     * <p>
     * A turn ends when the last ball is lost or the last block is cleared. It is
     * GameFlow, not the level, that decides what that means -- whether a life is
     * spent, whether the level is over, whether the session is over. The level
     * only knows how to play one turn of itself.
     * </p>
     * <p>
     * The countdown is run before this level is handed to the runner, so the
     * board sits frozen on screen while the player gets ready.
     * </p>
     */
    public void playOneTurn() {
        this.particles.clear();
        this.paddle.resetPosition();
        this.createBallsOnTopOfPaddle();
        this.runner.run(new CountdownAnimation(COUNTDOWN_SECONDS, COUNTDOWN_FROM, this.sprites));
        this.runner.run(this);
    }

    /**
     * Whether every block that had to be destroyed has been.
     * <p>
     * Callers are told the level's state rather than handed a counter to judge
     * for themselves, so "cleared" is defined in exactly one place.
     * </p>
     *
     * @return true once the level has been cleared.
     */
    public boolean isCleared() {
        return this.remainingBlocks.getValue() <= 0;
    }

    /**
     * Whether the current turn ended with no balls left in play.
     *
     * @return true once every ball of this turn has been lost.
     */
    public boolean isTurnLost() {
        return this.remainingBalls.getValue() <= 0;
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
        return this.isCleared() || this.isTurnLost();
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