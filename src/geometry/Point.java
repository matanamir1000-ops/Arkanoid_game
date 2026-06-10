package geometry;

/**
 * Represents a point in a 2D coordinate system.
 */
public class Point {
    private final double x;
    private final double y;
    private static final double COMPARISON_THRESHOLD = 0.000001;
    /**
     * Constructs a new point with the specified x and y coordinates.
     *
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates the distance between this point and another point.
     *
     * @param other the other point to measure the distance to
     * @return the distance between this point and the other point
     */
    public double distance(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Checks if this point is equal to another point.
     * Points are considered equal if they have the exact same x and y coordinates.
     *
     * @param other the other point to compare with
     * @return true if the points are equal, false otherwise
     */
    public boolean equals(Point other) {
        if (other == null) {
            return false;
        }
        return (Math.abs(this.x - other.x) < COMPARISON_THRESHOLD
                && Math.abs(this.y - other.y) < COMPARISON_THRESHOLD);
    }

    /**
     * Returns the x coordinate of this point.
     *
     * @return the x coordinate
     */
    public double getX() {
        return this.x;
    }

    /**
     * Returns the y coordinate of this point.
     *
     * @return the y coordinate
     */
    public double getY() {
        return this.y;
    }

}