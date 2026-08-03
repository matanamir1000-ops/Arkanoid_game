package sprites;

import geometry.Point;
import geometry.Rectangle;

import java.awt.Color;

/**
 * Builds the kinds of block a level can contain.
 * <p>
 * Level definitions are meant to read as data. Without this, every level would
 * repeat the same behaviour construction inline and its blocks() method would
 * grow past the point where the layout is still visible in it.
 * </p>
 */
public final class BlockFactory {

    /**
     * Not instantiable: this class is a set of factories, not a thing.
     */
    private BlockFactory() {
    }

    /**
     * An ordinary block, destroyed by one hit.
     *
     * @param x      the left edge.
     * @param y      the top edge.
     * @param width  the block width.
     * @param height the block height.
     * @param color  the block colour.
     * @return the new block.
     */
    public static Block plain(double x, double y, double width, double height, Color color) {
        return new Block(rectangle(x, y, width, height), color);
    }

    /**
     * A tough block that takes several hits and changes colour as it wears.
     *
     * @param x         the left edge.
     * @param y         the top edge.
     * @param width     the block width.
     * @param height    the block height.
     * @param color     the block colour, used if the palette runs out.
     * @param hitPoints how many hits the block survives.
     * @param palette   colours from most damaged to least.
     * @return the new block.
     */
    public static Block multiHit(double x, double y, double width, double height,
                                 Color color, int hitPoints, Color[] palette) {
        return new Block(rectangle(x, y, width, height), color, new MultiHitBehavior(hitPoints, palette));
    }

    /**
     * A block that can never be destroyed.
     *
     * @param x      the left edge.
     * @param y      the top edge.
     * @param width  the block width.
     * @param height the block height.
     * @param color  the block colour.
     * @return the new block.
     */
    public static Block steel(double x, double y, double width, double height, Color color) {
        return new Block(rectangle(x, y, width, height), color, new SteelBehavior());
    }

    /**
     * A block that damages its neighbours when it is destroyed.
     * <p>
     * Exploding is independent of toughness, so any behaviour may be combined
     * with a blast: a block can be both hard to break and destructive when it
     * finally goes.
     * </p>
     *
     * @param x           the left edge.
     * @param y           the top edge.
     * @param width       the block width.
     * @param height      the block height.
     * @param color       the block colour.
     * @param behavior    how the block responds to being hit.
     * @param blastRadius how far the explosion reaches, in pixels.
     * @return the new block.
     */
    public static Block exploding(double x, double y, double width, double height,
                                  Color color, BlockBehavior behavior, double blastRadius) {
        return new Block(rectangle(x, y, width, height), color, behavior, blastRadius);
    }

    /**
     * Builds the block's shape.
     *
     * @param x      the left edge.
     * @param y      the top edge.
     * @param width  the block width.
     * @param height the block height.
     * @return the rectangle.
     */
    private static Rectangle rectangle(double x, double y, double width, double height) {
        return new Rectangle(new Point(x, y), width, height);
    }
}
