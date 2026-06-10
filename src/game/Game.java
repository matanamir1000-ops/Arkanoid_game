package game;

import biuoop.DrawSurface;
import biuoop.GUI;
import biuoop.Sleeper;
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
 * The Game class manages the game environment, sprites, and animation loop.
 */
public class Game {
    private SpriteCollection sprites;
    private GameEnvironment environment;
    private GUI gui;
    private Sleeper sleeper;
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
    private static final int BALL_SPEED_X = 5;
    private static final int BALL_SPEED_Y = -5;
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
     * Constructor for a new Game.
     * Initializes the internal collections.
     */
    public Game() {
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
        this.gui = new GUI("Arkanoid", SCREEN_WIDTH, SCREEN_HEIGHT);
        this.sleeper = new Sleeper();
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
     * The BlockRemover and ScoreTrackingListener are stored on the Game instance so
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
                SCREEN_WIDTH, BORDER_THICKNESS), Color.RED);
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
        biuoop.KeyboardSensor sensor = this.gui.getKeyboardSensor();
        Rectangle paddleRect = new Rectangle(new Point(PADDLE_START_X, PADDLE_START_Y),
                PADDLE_WIDTH, PADDLE_HEIGHT);
        items.add(new Paddle(sensor, paddleRect, java.awt.Color.ORANGE));
    }

    /**
     * Creates a staircase pattern of blocks.
     *
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
                newBlock.setRecolorsHitter(true);
                newBlock.addHitListener(remover);
                newBlock.addHitListener(tracker);
                this.remainingBlocks.increase(1);
                rowOfBlocks.add(newBlock);
            }
        }
        return rowOfBlocks;
    }

    // === BEGIN INSTRUCTOR-PROVIDED run() — DO NOT EDIT (CLAUDE.md mandate) ===
    /**
     * Run the game -- start the animation loop.
     */
    public void run() {
        int framesPerSecond = 60;
        int millisecondsPerFrame = 1000 / framesPerSecond;

        while (true) {
            if (this.remainingBlocks.getValue() <= 0) {
                this.score.increase(LEVEL_CLEAR_BONUS);
                System.out.println("You Win!\nYour score is: " + this.score.getValue());
                this.gui.close();
                return;
            }
            if (this.remainingBalls.getValue() <= 0) {
                System.out.println("Game Over.\nYour score is: " + this.score.getValue());
                this.gui.close();
                return;
            }
            long startTime = System.currentTimeMillis();
            DrawSurface d = this.gui.getDrawSurface();
            d.setColor(Color.black);
            this.sprites.drawAllOn(d);
            this.gui.show(d);
            this.sprites.notifyAllTimePassed();

            long usedTime = System.currentTimeMillis() - startTime;
            long milliSecondLeftToSleep = millisecondsPerFrame - usedTime;
            if (milliSecondLeftToSleep > 0) {
                this.sleeper.sleepFor(milliSecondLeftToSleep);
            }
        }
    }
    // === END INSTRUCTOR-PROVIDED run() ===

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