package sprites;

import biuoop.DrawSurface;
import biuoop.KeyboardSensor;
import collision.Collidable;
import game.Game;
import game.GameItem;
import game.Sprite;
import geometry.Point;
import geometry.Rectangle;
import geometry.Velocity;

import java.awt.Color;

/**
 * The Paddle is the player in the game.
 * It is a rectangle that is controlled by the arrow keys, and moves according to the player key presses.
 * The paddle also performs a smooth screen-wrap (Pac-Man effect) at the inner left and right borders,
 * delegating the off-screen slice's drawing and collisions to a transient PaddleGhost collaborator.
 */
public class Paddle implements Sprite, Collidable, GameItem {
    // --- Movement and Screen Constants ---
    private static final int STEP = 7;
    private static final int SCREEN_WIDTH = 800;
    private static final int BORDER_THICKNESS = 20;
    private static final int INNER_LEFT = BORDER_THICKNESS;
    private static final int INNER_RIGHT = SCREEN_WIDTH - BORDER_THICKNESS;
    private static final int INNER_WIDTH = INNER_RIGHT - INNER_LEFT;

    // --- Physics Constants (for the hit method) ---
    private static final int NUM_OF_REGIONS = 5;
    private static final int REGION_1_ANGLE = 300;
    private static final int REGION_2_ANGLE = 330;
    private static final int REGION_4_ANGLE = 30;
    private static final int REGION_5_ANGLE = 60;
    private static final int REGION_2_BOUNDARY = 2;
    private static final int REGION_3_BOUNDARY = 3;
    private static final int REGION_4_BOUNDARY = 4;
    private static final double COMPARISON_THRESHOLD = 0.000001;

    private biuoop.KeyboardSensor keyboard;
    private Rectangle shape;
    private Color color;
    private Game game;
    private PaddleGhost ghost;

    /**
     * Constructor for the Paddle.
     *
     * @param keyboard the keyboard sensor to read user key presses.
     * @param shape    the rectangle shape of the paddle.
     * @param color    the color of the paddle.
     */
    public Paddle(biuoop.KeyboardSensor keyboard, Rectangle shape, Color color) {
        this.keyboard = keyboard;
        this.shape = new Rectangle(shape.getUpperLeft(), shape.getWidth(), shape.getHeight());
        this.color = color;
    }

    /**
     * Moves the paddle one step to the left.
     * Movement is unbounded here; wrap behavior (visual splitting and
     * snap-on-full-crossover) is delegated to {@link #handleScreenWrap()}.
     */
    public void moveLeft() {
        double nextX = this.shape.getUpperLeft().getX() - STEP;
        this.shape = new Rectangle(new Point(nextX, this.shape.getUpperLeft().getY()),
                this.shape.getWidth(), this.shape.getHeight());
        this.handleScreenWrap();
    }

    /**
     * Moves the paddle one step to the right.
     * Movement is unbounded here; wrap behavior (visual splitting and
     * snap-on-full-crossover) is delegated to {@link #handleScreenWrap()}.
     */
    public void moveRight() {
        double nextX = this.shape.getUpperLeft().getX() + STEP;
        this.shape = new Rectangle(new Point(nextX, this.shape.getUpperLeft().getY()),
                this.shape.getWidth(), this.shape.getHeight());
        this.handleScreenWrap();
    }

    /**
     * Notifies the paddle that time has passed.
     * Checks if the left or right keys are pressed and moves the paddle accordingly.
     */
    @Override
    public void timePassed() {
        if (this.keyboard.isPressed(KeyboardSensor.LEFT_KEY)) {
            this.moveLeft();
        }
        if (this.keyboard.isPressed(KeyboardSensor.RIGHT_KEY)) {
            this.moveRight();
        }
    }

    /**
     * Draws the paddle on the given DrawSurface, clipped to the inner play area.
     *
     * @param surface the surface to draw on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        Rectangle visible = this.visibleSlice();
        if (visible != null) {
            visible.drawOn(surface, this.color);
        }
    }

    /**
     * Returns the paddle's collision rectangle, clipped to the inner play area.
     *
     * @return the visible-slice Rectangle of the paddle on this side.
     */
    @Override
    public Rectangle getCollisionRectangle() {
        Rectangle visible = this.visibleSlice();
        if (visible == null) {
            return new Rectangle(this.shape.getUpperLeft(), 0, this.shape.getHeight());
        }
        return visible;
    }

    /**
     * Notifies the paddle that a collision occurred and calculates the new velocity.
     * The paddle is divided into 5 regions, each returning a different bounce angle.
     * @param hitter the ball that hit the paddle.
     * @param collisionPoint  the point where the collision occurred.
     * @param currentVelocity the velocity of the object hitting the paddle.
     * @return the new velocity after the collision.
     */
    @Override
    public Velocity hit(Ball hitter, Point collisionPoint, Velocity currentVelocity) {
        if (Math.abs(collisionPoint.getY() - this.shape.getUpperLeft().getY()) < COMPARISON_THRESHOLD) {
            double regionWidth = this.shape.getWidth() / NUM_OF_REGIONS;
            double speed = Math.sqrt(currentVelocity.getDx() * currentVelocity.getDx()
                    + currentVelocity.getDy() * currentVelocity.getDy());
            double delta = collisionPoint.getX() - this.shape.getUpperLeft().getX();

            if (delta < regionWidth) {
                return Velocity.fromAngleAndSpeed(REGION_1_ANGLE, speed);
            } else if (delta < REGION_2_BOUNDARY * regionWidth) {
                return Velocity.fromAngleAndSpeed(REGION_2_ANGLE, speed);
            } else if (delta < REGION_3_BOUNDARY * regionWidth) {
                return new Velocity(currentVelocity.getDx(), Math.abs(currentVelocity.getDy()) * -1);
            } else if (delta < REGION_4_BOUNDARY * regionWidth) {
                return Velocity.fromAngleAndSpeed(REGION_4_ANGLE, speed);
            } else {
                return Velocity.fromAngleAndSpeed(REGION_5_ANGLE, speed);
            }
        }

        double dx = currentVelocity.getDx();
        double dy = currentVelocity.getDy();

        if (Math.abs(collisionPoint.getX() - this.shape.getUpperLeft().getX()) < COMPARISON_THRESHOLD
                || Math.abs(collisionPoint.getX()
                - (this.shape.getUpperLeft().getX() + this.shape.getWidth())) < COMPARISON_THRESHOLD) {
            dx *= -1;
        }

        if (Math.abs(collisionPoint.getY()
                - (this.shape.getUpperLeft().getY() + this.shape.getHeight())) < COMPARISON_THRESHOLD) {
            dy *= -1;
        }

        return new Velocity(dx, dy);
    }

    /**
     * Adds the paddle to the game.
     *
     * @param game the game object.
     */
    @Override
    public void addToGame(Game game) {
        this.game = game;
        game.addSprite(this);
        game.addCollidable(this);
    }

    // ==========================================================================
    // Smooth Screen-Wrap Helpers
    // ==========================================================================

    /**
     * Maintains the smooth screen-wrap invariants after a movement tick.
     */
    private void handleScreenWrap() {
        double x = this.shape.getUpperLeft().getX();
        double y = this.shape.getUpperLeft().getY();
        double w = this.shape.getWidth();
        double h = this.shape.getHeight();
        if (x + w <= INNER_LEFT) {
            this.shape = new Rectangle(new Point(x + INNER_WIDTH, y), w, h);
            this.destroyGhost();
            return;
        }
        if (x >= INNER_RIGHT) {
            this.shape = new Rectangle(new Point(x - INNER_WIDTH, y), w, h);
            this.destroyGhost();
            return;
        }

        if (x < INNER_LEFT) {
            double ghostWidth = INNER_LEFT - x;
            double ghostX = INNER_RIGHT - ghostWidth;
            this.refreshGhost(new Rectangle(new Point(ghostX, y), ghostWidth, h));
            return;
        }
        if (x + w > INNER_RIGHT) {
            double ghostWidth = (x + w) - INNER_RIGHT;
            this.refreshGhost(new Rectangle(new Point(INNER_LEFT, y), ghostWidth, h));
            return;
        }

        this.destroyGhost();
    }

    /**
     * Ensures a PaddleGhost exists with the given rectangle.
     *
     * @param rect the rectangle the ghost slice should occupy this tick.
     */
    private void refreshGhost(Rectangle rect) {
        if (this.ghost == null) {
            this.ghost = new PaddleGhost(rect);
            if (this.game != null) {
                this.game.addSprite(this.ghost);
                this.game.addCollidable(this.ghost);
            }
        } else {
            this.ghost.setRectangle(rect);
        }
    }

    /**
     * Removes the active ghost (if any) from the game and clears the reference.
     */
    private void destroyGhost() {
        if (this.ghost != null) {
            if (this.game != null) {
                this.game.removeSprite(this.ghost);
                this.game.removeCollidable(this.ghost);
            }
            this.ghost = null;
        }
    }

    /**
     * Computes the visible portion of the paddle on this side of the screen.
     *
     * @return the clipped Rectangle on this side, or null if none.
     */
    private Rectangle visibleSlice() {
        double x = this.shape.getUpperLeft().getX();
        double y = this.shape.getUpperLeft().getY();
        double w = this.shape.getWidth();
        double h = this.shape.getHeight();

        double leftX = Math.max(x, INNER_LEFT);
        double rightX = Math.min(x + w, INNER_RIGHT);
        double clippedWidth = rightX - leftX;
        if (clippedWidth <= 0) {
            return null;
        }
        return new Rectangle(new Point(leftX, y), clippedWidth, h);
    }
    /**
     * Transient collaborator that renders and serves collisions for the
     * paddle's overflow slice on the opposite side of the screen during
     * a smooth screen-wrap.
     */
    private class PaddleGhost implements Collidable, Sprite {
        private Rectangle rect;
        /**
         * Constructs a new ghost slice with the given rectangle.
         *
         * @param rect the rectangle representing the ghost's bounds.
         */
        PaddleGhost(Rectangle rect) {
            this.rect = rect;
        }

        /**
         * Updates the ghost's rectangle in place.
         *
         * @param newRect the new rectangle for the ghost slice.
         */
        void setRectangle(Rectangle newRect) {
            this.rect = newRect;
        }

        /**
         * Returns the ghost's collision rectangle.
         *
         * @return the current ghost Rectangle.
         */
        @Override
        public Rectangle getCollisionRectangle() {
            return new Rectangle(this.rect.getUpperLeft(),
                    this.rect.getWidth(), this.rect.getHeight());
        }

        /**
         * Handles a collision against the ghost slice by translating the
         * collision point into the parent paddle's logical coordinate frame
         * and delegating to the parent's hit().
         *
         * @param hitter the ball that hit the ghost slice.
         * @param collisionPoint  the point where the collision occurred (ghost frame).
         * @param currentVelocity the velocity of the colliding object before impact.
         * @return the new velocity after the hit.
         */
        @Override
        public Velocity hit(Ball hitter, Point collisionPoint, Velocity currentVelocity) {
            double translatedX = collisionPoint.getX();
            if (Paddle.this.shape.getUpperLeft().getX() < INNER_LEFT) {
                translatedX -= INNER_WIDTH;
            } else {
                translatedX += INNER_WIDTH;
            }
            Point translatedPoint = new Point(translatedX, collisionPoint.getY());
            return Paddle.this.hit(hitter, translatedPoint, currentVelocity);
        }

        /**
         * Draws the ghost slice on the given DrawSurface using the parent
         * paddle's color.
         *
         * @param surface the surface to draw on.
         */
        @Override
        public void drawOn(DrawSurface surface) {
            this.rect.drawOn(surface, Paddle.this.color);
        }

        /**
         * The ghost is a passive observer;
         * lifecycle updates are pushed by the
         * parent paddle, so timePassed is intentionally empty.
         */
        @Override
        public void timePassed() {
            // Intentionally empty: ghost state is driven by the parent paddle.
        }
    }
}