package collision;

import geometry.Line;
import geometry.Point;

import java.util.ArrayList;

/**
 * Represents the game environment containing all collidable objects.
 */
public class GameEnvironment {
    private java.util.List<Collidable> collidables;

    /**
     * Constructor for the GameEnvironment.
     * Initializes an empty list of collidable objects.
     */
    public GameEnvironment() {
        this.collidables = new ArrayList<Collidable>();
    }

    /**
     * Adds a given collidable object to the environment.
     *
     * @param collidable the collidable object to add.
     */
    public void addCollidable(Collidable collidable) {
        this.collidables.add(collidable);
    }

    /**
     * Returns a defensive copy of the collidables list.
     *
     * @return a new List containing the current collidables.
     */
    public java.util.List<Collidable> getCollidables() {
        return new ArrayList<Collidable>(this.collidables);
    }

    /**
     * Removes a given collidable object from the environment.
     *
     * @param collidable the collidable object to remove.
     */
    public void removeCollidable(Collidable collidable) {
        this.collidables.remove(collidable);
    }

    /**
     * Calculates and returns the closest collision that is going to occur for a given trajectory.
     *
     * @param trajectory the trajectory line of the moving object.
     * @return the CollisionInfo of the closest collision, or null if no collision occurs.
     */
    public CollisionInfo getClosestCollision(Line trajectory) {
        double minDistance = Double.MAX_VALUE;
        CollisionInfo closestCollision = null;

        for (Collidable collidable : this.collidables) {
            Point intersectionPoint = trajectory.closestIntersectionToStartOfLine(collidable.getCollisionRectangle());
            if (intersectionPoint != null) {
                double currentDistance = intersectionPoint.distance(trajectory.start());
                if (minDistance > currentDistance) {
                    minDistance = currentDistance;
                    closestCollision = new CollisionInfo(intersectionPoint, collidable);
                }
            }
        }

        return closestCollision;
    }
}