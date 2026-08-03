package sprites;

import biuoop.DrawSurface;
import collision.Collidable;
import collision.HitListener;
import collision.HitNotifier;
import game.GameLevel;
import game.GameItem;
import game.Sprite;
import geometry.Geometry;
import geometry.Point;
import geometry.Rectangle;
import geometry.Velocity;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a block in the game, which is a collidable object.
 */
public class Block implements Collidable, Sprite, GameItem, HitNotifier {
    private final Rectangle shape;
    private final java.awt.Color color;
    private final List<HitListener> hitListeners;
    /**
     * Constructor for a Block with a given rectangle shape and color.
     *
     * @param rectangle the shape of the block.
     * @param color     the color of the block.
     */
    public Block(Rectangle rectangle, java.awt.Color color) {
        this.shape = new Rectangle(rectangle.getUpperLeft(),
                rectangle.getWidth(),
                rectangle.getHeight());
        this.color = color;
        this.hitListeners = new ArrayList<>();
    }

    /**
     * Notifies all registered HitListeners about a hit event.
     * Uses a defensive copy of the listeners list to allow safe removal of listeners during dispatch.
     *
     * @param hitter the ball that hit this block.
     */
    private void notifyHit(Ball hitter) {
        // Make a copy of the hitListeners before iterating over them.
        List<HitListener> listeners = new ArrayList<HitListener>(this.hitListeners);
        // Notify all listeners about a hit event:
        for (HitListener hl : listeners) {
            hl.hitEvent(this, hitter);
        }
    }

    /**
     * Gets the collision rectangle of the block.
     *
     * @return a new Rectangle identical to the block's shape.
     */
    @Override
    public Rectangle getCollisionRectangle() {
        return new Rectangle(this.shape.getUpperLeft(),
                this.shape.getWidth(), this.shape.getHeight());
    }

    /**
     * Notifies the block that a collision occurred and calculates the new velocity.
     *
     * <p>Every collision notifies the registered listeners. The block does not
     * inspect the hitter before doing so, which leaves each listener free to
     * decide what a hit means for it.</p>
     *
     * @param hitter          the ball that hit this block.
     * @param collisionPoint  the point where the collision occurred.
     * @param currentVelocity the velocity of the object hitting the block.
     * @return the new velocity after the collision.
     */
    @Override
    public Velocity hit(Ball hitter, Point collisionPoint, Velocity currentVelocity) {
        double dx = currentVelocity.getDx();
        double dy = currentVelocity.getDy();

        if (Math.abs(collisionPoint.getX() - this.shape.getUpperLeft().getX()) < Geometry.epsilon()
                || Math.abs(collisionPoint.getX()
                - (this.shape.getUpperLeft().getX() + this.shape.getWidth())) < Geometry.epsilon()) {
            dx *= -1;
        }

        if (Math.abs(collisionPoint.getY() - this.shape.getUpperLeft().getY()) < Geometry.epsilon()
                || Math.abs(collisionPoint.getY()
                - (this.shape.getUpperLeft().getY() + this.shape.getHeight())) < Geometry.epsilon()) {
            dy *= -1;
        }

        this.notifyHit(hitter);

        return new Velocity(dx, dy);
    }
    /**
     * Adds a HitListener to the list of listeners to hit events.
     *
     * @param hl the HitListener to add
     */
    @Override
    public void addHitListener(HitListener hl) {
        this.hitListeners.add(hl);
    }

    /**
     * Removes a HitListener from the list of listeners to hit events.
     *
     * @param hl the HitListener to remove
     */
    @Override
    public void removeHitListener(HitListener hl) {
        this.hitListeners.remove(hl);
    }
    /**
     * Draws the block on the given DrawSurface.
     *
     * @param surface the surface to draw on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        this.shape.drawOn(surface, this.color);
    }

    /**
     * Notifies the block that time has passed.
     * Currently, blocks do nothing when time passes.
     */
    @Override
    public void timePassed() {
        // Currently do nothing.
    }

    /**
     * Adds the block to the game as both a sprite and a collidable.
     *
     * @param game the game to add the block to.
     */
    @Override
    public void addToGame(GameLevel game) {
        game.addSprite(this);
        game.addCollidable(this);
    }

    /**
     * Removes this block from the game by removing it from both the
     * collidable environment and the sprite collection.
     *
     * @param game the game from which this block will be removed
     */
    public void removeFromGame(GameLevel game) {
        game.removeCollidable(this);
        game.removeSprite(this);
    }
}