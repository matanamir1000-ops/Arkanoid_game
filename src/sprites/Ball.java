package sprites;

import biuoop.DrawSurface;
import collision.Collidable;
import collision.CollisionInfo;
import collision.GameEnvironment;
import game.GameLevel;
import game.GameItem;
import game.Sprite;
import geometry.Geometry;
import geometry.Line;
import geometry.Point;
import geometry.Rectangle;
import geometry.Velocity;

import java.awt.Color;

/**
 * Represents a 2D bouncing ball that is also a Sprite.
 */
public class Ball implements Sprite, GameItem {
    private Point center;
    private final int radius;
    private Color color;
    private Velocity velocity;
    private GameEnvironment environment;

    /**
     * Constructor for a Ball with a center point, radius, and color.
     *
     * @param center the center point of the ball.
     * @param radius the radius of the ball.
     * @param color  the color of the ball.
     */
    public Ball(Point center, int radius, Color color) {
        this.center = new Point(center.getX(), center.getY());
        this.radius = radius;
        this.color = color;
        this.velocity = new Velocity(0, 0);
    }

    /**
     * Gets the x-coordinate of the ball's center.
     *
     * @return the x-coordinate as an integer.
     */
    public int getX() {
        return (int) this.center.getX();
    }

    /**
     * Gets the y-coordinate of the ball's center.
     *
     * @return the y-coordinate as an integer.
     */
    public int getY() {
        return (int) this.center.getY();
    }

    /**
     * Places the ball at a new position, with no collision handling at all.
     * <p>
     * Protected because it is the seam a projectile subclass needs: a laser
     * bolt travels in a straight line and is consumed by whatever it strikes,
     * so it must move without the bouncing that moveOneStep performs. Nothing
     * outside the hierarchy may move a ball, which is why this is not public.
     * </p>
     *
     * @param newCenter where the ball should now be.
     */
    protected void moveCenterTo(Point newCenter) {
        this.center = new Point(newCenter.getX(), newCenter.getY());
    }

    /**
     * Sets the velocity of the ball.
     *
     * @param v the new velocity.
     */
    public void setVelocity(Velocity v) {
        this.velocity = new Velocity(v.getDx(), v.getDy());
    }

    /**
     * Sets the velocity of the ball using dx/dy.
     *
     * @param dx the x-axis change.
     * @param dy the y-axis change.
     */
    public void setVelocity(double dx, double dy) {
        this.velocity = new Velocity(dx, dy);
    }

    /**
     * Sets the color of the ball.
     *
     * @param color the new color of the ball
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets the current velocity of the ball.
     *
     * @return a new Velocity object representing the ball's current velocity.
     */
    public Velocity getVelocity() {
        return new Velocity(this.velocity.getDx(), this.velocity.getDy());
    }

    /**
     * Gets the color of the ball.
     *
     * @return the color of the ball
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Draws the ball on the given surface.
     *
     * @param surface the surface to draw on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        surface.setColor(this.color);
        surface.fillCircle(this.getX(), this.getY(), this.radius);
    }

    /**
     * Notifies the ball that time has passed.
     */
    @Override
    public void timePassed() {
        this.moveOneStep();
    }

    /**
     * Computes the expected trajectory of the ball for the current step.
     * The trajectory is a line starting at the current center and ending
     * at the center plus the current velocity.
     *
     * @return a Line representing the ball's trajectory.
     */
    private Line computeTrajectory() {
        Point start = new Point(this.center.getX(), this.center.getY());
        double endX = this.center.getX() + this.velocity.getDx();
        double endY = this.center.getY() + this.velocity.getDy();
        Point end = new Point(endX, endY);
        return new Line(start, end);
    }

    /**
     * Moves the ball one step based on its velocity and the game environment.
     * If a collision is expected, the ball is moved to the hit point and
     * its velocity is updated accordingly.
     * <p>
     * Private, and that matters. This depends on the environment the ball is
     * handed when it joins a level, so a projectile subclass that does not join
     * as a ball has no environment to consult. Exposed, it would let any caller
     * holding a Ball reference reach that state on a subclass where it was
     * never set. Movement is reached only through timePassed, which a subclass
     * overrides wholesale.
     * </p>
     */
    private void moveOneStep() {
        // 1. Check and escape if swallowed
        this.applyDeepSwallowGuard();

        // 2. Compute trajectory and find collisions
        Line trajectory = this.computeTrajectory();
        CollisionInfo collisionInfo = this.environment.getClosestCollision(trajectory);

        // 3. Move accordingly
        if (collisionInfo == null) {
            this.center = new Point(this.center.getX() + this.velocity.getDx(),
                    this.center.getY() + this.velocity.getDy());
        } else {
            this.applyCollisionOffset(collisionInfo);
        }
    }

    /**
     * Handles the exact positioning and velocity update after a collision.
     *
     * @param collisionInfo the collision information.
     */
    private void applyCollisionOffset(CollisionInfo collisionInfo) {
        Point collisionPoint = collisionInfo.collisionPoint();
        Velocity newVelocity = collisionInfo.collisionObject().hit(this, collisionPoint, this.getVelocity());
        Rectangle rectangle = collisionInfo.collisionObject().getCollisionRectangle();

        double newCenterX = collisionPoint.getX();
        double newCenterY = collisionPoint.getY();

        if (Math.abs(collisionPoint.getX() - rectangle.getUpperLeft().getX()) < Geometry.epsilon()) {
            newCenterX = collisionPoint.getX() - this.radius;
        } else if (Math.abs(collisionPoint.getX()
                - (rectangle.getUpperLeft().getX() + rectangle.getWidth())) < Geometry.epsilon()) {
            newCenterX = collisionPoint.getX() + this.radius;
        }

        if (Math.abs(collisionPoint.getY() - rectangle.getUpperLeft().getY()) < Geometry.epsilon()) {
            newCenterY = collisionPoint.getY() - this.radius;
        } else if (Math.abs(collisionPoint.getY()
                - (rectangle.getUpperLeft().getY() + rectangle.getHeight())) < Geometry.epsilon()) {
            newCenterY = collisionPoint.getY() + this.radius;
        }

        this.setVelocity(newVelocity);
        this.center = new Point(newCenterX, newCenterY);
    }

    /**
     * Checks if the ball is trapped inside a collidable and ejects it upward if so.
     */
    private void applyDeepSwallowGuard() {
        for (Collidable collidable : this.environment.getCollidables()) {
            Rectangle rectangle = collidable.getCollisionRectangle();

            // The ball asks the rectangle if it contains the center point
            if (rectangle.contains(this.center)) {
                this.center = new Point(this.center.getX(), rectangle.getUpperLeft().getY() - this.radius);
                this.setVelocity(this.velocity.getDx(), -Math.abs(this.velocity.getDy()));
                break;
            }
        }
    }

    /**
     * Adds the ball to the game by registering it as a sprite
     * and setting its game environment.
     *
     * @param game the game to add the ball to.
     */
    @Override
    public void addToGame(GameLevel game) {
        game.addSprite(this);
        game.addBall(this);
        this.environment = game.getEnvironment();
    }
    /**
     * Removes this ball from the specified game.
     * <p>
     * The ball leaves both the sprite collection and the level's list of balls
     * in one operation, so that no caller can drop it from one and not the
     * other. A ball still listed after it had stopped being drawn would go on
     * being trailed, slowed and counted as though it were in play.
     * </p>
     *
     * @param g the game from which the ball should be removed
     */
    public void removeFromGame(GameLevel g) {
        g.removeSprite(this);
        g.removeBall(this);
    }
}