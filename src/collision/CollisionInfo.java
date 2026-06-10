package collision;

import geometry.Point;

/**
 * Holds information about a collision, including the exact point of impact
 * and the collidable object involved.
 */
public class CollisionInfo {
    private Point collisionPoint;
    private Collidable collisionObject;

    /**
     * Constructs a new CollisionInfo with a given collision point and object.
     *
     * @param collisionPoint  the point where the collision occurs.
     * @param collisionObject the object involved in the collision.
     */
    public CollisionInfo(Point collisionPoint, Collidable collisionObject) {
        this.collisionObject = collisionObject;
        this.collisionPoint = new Point(collisionPoint.getX(), collisionPoint.getY());
    }

    /**
     * Returns the exact point at which the collision occurs.
     *
     * @return a new Point representing the collision location.
     */
    public Point collisionPoint() {
        return new Point(this.collisionPoint.getX(), this.collisionPoint.getY());
    }

    /**
     * Returns the collidable object involved in the collision.
     *
     * @return the collidable object.
     */
    public Collidable collisionObject() {
        return this.collisionObject;
    }
}