package geometry;

import biuoop.DrawSurface;

import java.util.ArrayList;

/**
 * Represents a 2D rectangle defined by an upper-left point, width, and height.
 */
public class Rectangle {
    private final Point upperLeft;
    private final double width;
    private final double height;

    /**
     * Creates a new rectangle with a specified location, width, and height.
     *
     * @param upperLeft the upper-left point of the rectangle.
     * @param width     the width of the rectangle.
     * @param height    the height of the rectangle.
     */
    public Rectangle(Point upperLeft, double width, double height) {
        this.upperLeft = new Point(upperLeft.getX(), upperLeft.getY());
        this.width = width;
        this.height = height;
    }

    /**
     * Computes the intersection points of a given line with the rectangle.
     *
     * @param line the line to check for intersections.
     * @return a (possibly empty) List of intersection points with the specified line.
     */
    public java.util.List<Point> intersectionPoints(Line line) {
        java.util.List<Point> intersectionPoints = new java.util.ArrayList<Point>();
        java.util.List<Line> recLines = this.recLines();

        for (Line recLine : recLines) {
            Point intersectionPoint = recLine.intersectionWith(line);
            if (intersectionPoint != null) {
                boolean alreadyExists = false;
                for (Point p : intersectionPoints) {
                    if (p.equals(intersectionPoint)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    intersectionPoints.add(intersectionPoint);
                }
            }
        }
        return intersectionPoints;
    }

    /**
     * Draws the rectangle on a given surface with a fill color and black border.
     *
     * @param surface the surface to draw on.
     * @param color   the color to fill the rectangle with.
     */
    public void drawOn(DrawSurface surface, java.awt.Color color) {
        // Draw the inside
        surface.setColor(color);
        surface.fillRectangle((int) this.upperLeft.getX(), (int) this.upperLeft.getY(),
                (int) this.width, (int) this.height);

        // Draw the border
        surface.setColor(java.awt.Color.BLACK);
        surface.drawRectangle((int) this.upperLeft.getX(), (int) this.upperLeft.getY(),
                (int) this.width, (int) this.height);
    }

    /**
     * Generates a list of the four lines that make up the rectangle's borders.
     *
     * @return a list of lines representing the rectangle's edges.
     */
    private java.util.List<Line> recLines() {
        java.util.List<Line> lines = new java.util.ArrayList<Line>();

        if (this.width == 0 && this.height == 0) {
            return lines;
        }

        Point upperRight = new Point(this.upperLeft.getX() + this.width, this.upperLeft.getY());
        Point bottomRight = new Point(this.upperLeft.getX() + this.width, this.upperLeft.getY() + this.height);
        Point bottomLeft = new Point(this.upperLeft.getX(), this.upperLeft.getY() + this.height);

        lines.add(new Line(this.upperLeft, upperRight));
        lines.add(new Line(upperRight, bottomRight));
        lines.add(new Line(bottomRight, bottomLeft));
        lines.add(new Line(bottomLeft, this.upperLeft));

        return lines;
    }

    /**
     * Gets the width of the rectangle.
     *
     * @return the width of the rectangle.
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Gets the height of the rectangle.
     *
     * @return the height of the rectangle.
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * Gets the upper-left point of the rectangle.
     *
     * @return a new Point representing the upper-left corner of the rectangle.
     */
    public Point getUpperLeft() {
        return new Point(this.upperLeft.getX(), this.upperLeft.getY());
    }
    /**
     * Checks if a given point is strictly inside the rectangle.
     *
     * @param p the point to check.
     * @return true if the point is inside, false otherwise.
     */
    public boolean contains(Point p) {
        double minX = this.upperLeft.getX();
        double maxX = minX + this.width;
        double minY = this.upperLeft.getY();
        double maxY = minY + this.height;

        return (p.getX() > minX && p.getX() < maxX && p.getY() > minY && p.getY() < maxY);
    }
}