package geometry;

/**
 * Represents a line segment in a 2D coordinate system.
 * A line segment is defined by a starting point and an ending point.
 */
public class Line {
    private Point start;
    private Point end;
    private static final double COMPARISON_THRESHOLD = 0.000001;
    private static final double DIVISOR_HALF = 2.0;

    /**
     * Constructs a new Line with the specified start and end points.
     *
     * @param start the starting point of the line
     * @param end   the ending point of the line
     */
    public Line(Point start, Point end) {
        this.start = new Point(start.getX(), start.getY());
        this.end = new Point(end.getX(), end.getY());
    }

    /**
     * Constructs a new Line with the specified coordinates.
     *
     * @param x1 the x coordinate of the start point
     * @param y1 the y coordinate of the start point
     * @param x2 the x coordinate of the end point
     * @param y2 the y coordinate of the end point
     */
    public Line(double x1, double y1, double x2, double y2) {
        this.start = new Point(x1, y1);
        this.end = new Point(x2, y2);
    }

    /**
     * Calculates the length of the line segment.
     *
     * @return the length of the line
     */
    public double length() {
        return this.start.distance(this.end);
    }

    /**
     * Calculates the middle point of the line segment.
     *
     * @return a new Point representing the exact middle of the line
     */
    public Point middle() {
        double middleX = (this.start.getX() + this.end.getX()) / DIVISOR_HALF;
        double middleY = (this.start.getY() + this.end.getY()) / DIVISOR_HALF;
        return new Point(middleX, middleY);
    }

    /**
     * Returns the starting point of the line.
     *
     * @return a copy of the start point
     */
    public Point start() {
        return new Point(this.start.getX(), this.start.getY());
    }

    /**
     * Returns the ending point of the line.
     *
     * @return a copy of the end point
     */
    public Point end() {
        return new Point(this.end.getX(), this.end.getY());
    }

    /**
     * Calculates the orientation of three points to determine their geometric turn.
     * This helper method is used to find which side of a line (formed by p1 and p2).
     * the third point (p3) is located on, using the cross-product logic.
     *
     * @param p1 the starting point of the reference line
     * @param p2 the ending point of the reference line
     * @param p3 the point to check against the reference line
     * @return a positive number if p3 is on one side,
     * a negative number if p3 is on the opposite side,
     * or 0 if all three points are collinear.
     */
    private static double orientation(Point p1, Point p2, Point p3) {
        return (p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
                - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY());
    }

    /**
     * Checks if point q lies bounded within the line segment 'pr'.
     * This method assumes that points p, q, and r are already collinear.
     *
     * @param p the first end-point of the segment
     * @param q the middle point to check
     * @param r the second end-point of the segment
     * @return true if q is physically on the segment pr, false otherwise
     */
    private static boolean onSegment(Point p, Point q, Point r) {
        double minX = Math.min(p.getX(), r.getX());
        double maxX = Math.max(p.getX(), r.getX());
        double minY = Math.min(p.getY(), r.getY());
        double maxY = Math.max(p.getY(), r.getY());

        boolean xInRange = (q.getX() >= minX - COMPARISON_THRESHOLD)
                && (q.getX() <= maxX + COMPARISON_THRESHOLD);
        boolean yInRange = (q.getY() >= minY - COMPARISON_THRESHOLD)
                && (q.getY() <= maxY + COMPARISON_THRESHOLD);

        return xInRange && yInRange;
    }

    /**
     * Checks if this line segment intersects with another line segment.
     *
     * @param other the other line to check intersection with
     * @return true if the lines intersect, false otherwise
     */
    public boolean isIntersecting(Line other) {
        // Find orientations for general and special cases
        double otherSideStart = orientation(this.start, this.end, other.start);
        double otherSideEnd = orientation(this.start, this.end, other.end);

        double mySideEnd = orientation(other.start, other.end, this.end);
        double mySideStart = orientation(other.start, other.end, this.start);

        // Handle collinear edge cases: check if endpoints lie exactly on the other segment
        if (Math.abs(otherSideStart) < COMPARISON_THRESHOLD && onSegment(this.start, other.start, this.end)) {
            return true;
        }
        if (Math.abs(otherSideEnd) < COMPARISON_THRESHOLD && onSegment(this.start, other.end, this.end)) {
            return true;
        }
        if (Math.abs(mySideEnd) < COMPARISON_THRESHOLD && onSegment(other.start, this.end, other.end)) {
            return true;
        }
        if (Math.abs(mySideStart) < COMPARISON_THRESHOLD && onSegment(other.start, this.start, other.end)) {
            return true;
        }

        // General case: lines intersect if endpoints are on strictly opposite sides
        return ((otherSideStart > 0 && otherSideEnd < 0) || (otherSideStart < 0 && otherSideEnd > 0))
                && ((mySideStart > 0 && mySideEnd < 0) || (mySideStart < 0 && mySideEnd > 0));
    }

    /**
     * Checks if both given lines intersect with this line segment.
     *
     * @param other1 the first line to check
     * @param other2 the second line to check
     * @return true if this line intersects with both other1 and other2, false otherwise
     */
    public boolean isIntersecting(Line other1, Line other2) {
        return this.isIntersecting(other1) && this.isIntersecting(other2);
    }

    /**
     * Calculates the exact intersection point of this line with another line.
     * Uses Cramer's rule to find the intersection without risking division by zero.
     *
     * @param other the other line to intersect with.
     * @return the intersection Point, or null if the lines do not intersect,
     * or if they overlap (infinite intersection points).
     */
    public Point intersectionWith(Line other) {
        // Guard clause: if the lines don't intersect at all, return null immediately
        if (!this.isIntersecting(other)) {
            return null;
        }

        // Extract coefficients A, B, and C for our line's equation (Ax + By = C)
        double myA = this.end.getY() - this.start.getY();
        double myB = this.start.getX() - this.end.getX();
        double myC = myA * this.start.getX() + myB * this.start.getY();

        // Extract coefficients A, B, and C for the other line's equation
        double otherA = other.end.getY() - other.start.getY();
        double otherB = other.start.getX() - other.end.getX();
        double otherC = otherA * other.start.getX() + otherB * other.start.getY();

        // Calculate the determinant to check for parallelism or collinearity
        double det = myA * otherB - otherA * myB;

        if (Math.abs(det) < COMPARISON_THRESHOLD) {
            if (this.start.equals(this.end) && onSegment(other.start, this.start, other.end)) {
                return this.start();
            }
            if (other.start.equals(other.end) && onSegment(this.start, other.start, this.end)) {
                return other.start();
            }
            // Handle collinear lines that touch exactly at one shared endpoint
            if (this.start.equals(other.start) && onSegment(this.end, this.start, other.end)) {
                return this.start();
            }
            if (this.end.equals(other.start) && onSegment(this.start, this.end, other.end)) {
                return this.end();
            }
            if (this.start.equals(other.end) && onSegment(this.end, this.start, other.start)) {
                return this.start();
            }
            if (this.end.equals(other.end) && onSegment(this.start, this.end, other.start)) {
                return this.end();
            }
            // Lines overlap completely or are parallel, returning null
            return null;
        }

        // Linear algebra calculation: extract the exact intersection coordinates
        double intersectX = (myC * otherB - otherC * myB) / det;
        double intersectY = (myA * otherC - otherA * myC) / det;
        return new Point(intersectX, intersectY);
    }

    /**
     * Calculates the closest intersection point of this line to its start point
     * with the given rectangle.
     *
     * @param rect the rectangle to check for intersections.
     * @return the closest intersection Point to the start of the line, or null
     * if there are no intersections.
     */
    public Point closestIntersectionToStartOfLine(Rectangle rect) {
        java.util.List<Point> intersectionPoints = rect.intersectionPoints(this);
        if (intersectionPoints.isEmpty()) {
            return null;
        }

        double minDistance = intersectionPoints.get(0).distance(this.start);
        Point minP = intersectionPoints.get(0);

        for (Point p : intersectionPoints) {
            double distanceToStart = this.start.distance(p);
            if (distanceToStart < minDistance) {
                minDistance = distanceToStart;
                minP = p;
            }
        }

        return new Point(minP.getX(), minP.getY());
    }

    /**
     * Checks if this line is identical to another line.
     * Lines are considered equal if they connect the exact same two points,
     * regardless of the direction they were drawn.
     *
     * @param other the other line to compare with
     * @return true if the lines are geometrically equal, false otherwise
     */
    public boolean equals(Line other) {
        if (other == null) {
            return false;
        }
        boolean sameDirection = this.start.equals(other.start) && this.end.equals(other.end);
        boolean oppositeDirection = this.start.equals(other.end) && this.end.equals(other.start);
        return sameDirection || oppositeDirection;
    }
}