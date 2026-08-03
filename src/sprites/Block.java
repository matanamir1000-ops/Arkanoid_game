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
 * <p>
 * Every block in the game is this one class. What makes a block fragile, tough
 * or indestructible is the BlockBehavior it holds, and what makes it explosive
 * is a blast radius greater than zero. Nothing ever has to ask a block what
 * kind of block it is; it is asked what state it is in, or told to take damage.
 * </p>
 */
public class Block implements Collidable, Sprite, GameItem, HitNotifier {
    private static final double NO_BLAST = 0;

    private final Rectangle shape;
    private final java.awt.Color color;
    private final List<HitListener> hitListeners;
    private final BlockBehavior behavior;
    private final double blastRadius;

    /**
     * Constructor for an ordinary block, destroyed by a single hit.
     *
     * @param rectangle the shape of the block.
     * @param color     the color of the block.
     */
    public Block(Rectangle rectangle, java.awt.Color color) {
        this(rectangle, color, new PlainBehavior(), NO_BLAST);
    }

    /**
     * Constructor for a block with a chosen behaviour and no blast.
     *
     * @param rectangle the shape of the block.
     * @param color     the color of the block.
     * @param behavior  how the block responds to being hit.
     */
    public Block(Rectangle rectangle, java.awt.Color color, BlockBehavior behavior) {
        this(rectangle, color, behavior, NO_BLAST);
    }

    /**
     * Constructor for a block with a chosen behaviour and a blast radius.
     *
     * @param rectangle   the shape of the block.
     * @param color       the color of the block.
     * @param behavior    how the block responds to being hit.
     * @param blastRadius how far this block damages its neighbours when it is
     *                    destroyed; zero for a block that does not explode.
     * @throws IllegalArgumentException if blastRadius is negative, which would
     *                                  silently read as "does not explode".
     */
    public Block(Rectangle rectangle, java.awt.Color color, BlockBehavior behavior, double blastRadius) {
        if (blastRadius < 0) {
            throw new IllegalArgumentException("blastRadius cannot be negative, but was " + blastRadius);
        }
        this.shape = new Rectangle(rectangle.getUpperLeft(),
                rectangle.getWidth(),
                rectangle.getHeight());
        this.color = color;
        this.hitListeners = new ArrayList<>();
        this.behavior = behavior;
        this.blastRadius = blastRadius;
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

        this.applyDamage(hitter);

        return new Velocity(dx, dy);
    }

    /**
     * Damages the block without a physical collision.
     * <p>
     * This is the same thing a bounce does to a block, minus the bounce. It
     * exists so that a neighbouring explosion can hurt a block that nothing
     * actually touched, and so that the damage still runs through the listeners
     * -- which is what lets one explosion set off the next without any special
     * chaining code.
     * </p>
     *
     * @param hitter the ball responsible for the damage.
     */
    public void applyDamage(Ball hitter) {
        this.behavior.registerHit(hitter);
        this.notifyHit(hitter);
    }

    /**
     * Whether this block has taken all the damage it can survive.
     *
     * @return true once the block should be removed from the game.
     */
    public boolean isDestroyed() {
        return this.behavior.isDestroyed();
    }

    /**
     * What the most recent hit on this block was worth.
     * <p>
     * This is a snapshot of the last hit, not a property of the block. It has
     * no meaning before the block has been hit at all, and its value changes as
     * damage accumulates -- a tough block pays more for the blow that breaks it
     * than for the ones before.
     * </p>
     *
     * @return the points to award for the hit that has just been registered.
     */
    public int pointsForLastHit() {
        return this.behavior.pointsForLastHit();
    }

    /**
     * Whether this block can ever be destroyed.
     * <p>
     * Fixed for the life of the block, unlike isDestroyed. A level counts these
     * to work out how many blocks clearing it requires, which is what stops a
     * level declaring a target that includes blocks nothing can break.
     * </p>
     *
     * @return true if destroying this block is possible.
     */
    public boolean isBreakable() {
        return this.behavior.isBreakable();
    }

    /**
     * How far this block damages its neighbours when destroyed.
     *
     * @return the blast radius in pixels; zero if the block does not explode.
     */
    public double getBlastRadius() {
        return this.blastRadius;
    }

    /**
     * The centre of the block, used to measure distance to its neighbours.
     *
     * @return the block's centre point.
     */
    public Point getCenter() {
        return this.shape.getCenter();
    }

    /**
     * The colour the block is currently showing.
     * <p>
     * This is the behaviour's answer, not the block's declared colour, so a
     * tough block reports the shade it has worn down to. It is what the block
     * would draw itself with this frame, which is what an effect wanting to
     * match the block's appearance needs.
     * </p>
     *
     * @return the colour this block draws with right now.
     */
    public java.awt.Color getDisplayColor() {
        return this.behavior.displayColor(this.color);
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
        this.shape.drawOn(surface, this.getDisplayColor());
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
        game.removeBlock(this);
    }
}