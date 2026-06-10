package geometry;

/**
 * Velocity specifies the change in position on the 'x' and the 'y' axes.
 */
public class Velocity {
    private final double dx;
    private final double dy;

    /**
     * Constructs a new Velocity with the specified dx and dy.
     *
     * @param dx the change in position on the x-axis.
     * @param dy the change in position on the y-axis.
     */
    public Velocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Creates a new Velocity instance based on a given angle and speed.
     * Assuming up is angle 0.
     *
     * @param angle the direction of the velocity in degrees.
     * @param speed the magnitude of the velocity.
     * @return a new Velocity object representing the speed and direction.
     */
    public static Velocity fromAngleAndSpeed(double angle, double speed) {
        double dx = speed * Math.sin(Math.toRadians(angle));
        double dy = -speed * Math.cos(Math.toRadians(angle));
        return new Velocity(dx, dy);
    }

    /**
     * Returns the change in position on the x-axis.
     *
     * @return the dx value.
     */
    public double getDx() {
        return this.dx;
    }

    /**
     * Returns the change in position on the y-axis.
     *
     * @return the dy value.
     */
    public double getDy() {
        return this.dy;
    }

    /**
     * Takes a point with position (x,y) and returns a new point
     * with position (x+dx, y+dy).
     *
     * @param point the initial point.
     * @return a new Point after applying the velocity.
     */
    public Point applyToPoint(Point point) {
        return new Point(point.getX() + dx, point.getY() + dy);
    }
}