package collision;

import geometry.Point;
import geometry.Rectangle;
import geometry.Velocity;
import sprites.Ball;

/**
 * The Collidable interface is used by things that can be collided with.
 */
public interface Collidable {

    /**
     * Returns the "collision shape" of the object.
     *
     * @return a Rectangle representing the collision shape.
     */
    Rectangle getCollisionRectangle();

    /**
     * Notifies the object that we collided with it at collisionPoint with a given velocity.
     * The return is the new velocity expected after the hit (based on the force the object inflicted on us).
     *
     * @param hitter          the ball that hit this collidable.
     * @param collisionPoint  the point where the collision occurred.
     * @param currentVelocity the velocity of the colliding object before impact.
     * @return the new velocity expected after the hit.
     */
    Velocity hit(Ball hitter, Point collisionPoint, Velocity currentVelocity);
}