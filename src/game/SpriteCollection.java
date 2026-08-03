package game;

import biuoop.DrawSurface;
import java.util.ArrayList;

/**
 * The SpriteCollection class holds a collection of Sprite objects.
 * It provides methods to add new sprites and to notify all sprites
 * that time has passed or that they should draw themselves.
 * <p>
 * The collection is itself a Sprite. Drawing all of its members is the same
 * operation as drawing one thing, so anything that can display a sprite can
 * display a whole board without knowing it is looking at a collection.
 * </p>
 */
public class SpriteCollection implements Sprite {
    private java.util.List<Sprite> sprites;

    /**
     * Constructs a new, empty SpriteCollection.
     */
    public SpriteCollection() {
        this.sprites = new ArrayList<Sprite>();
    }

    /**
     * Adds a new sprite to the collection.
     *
     * @param sprite the sprite to add.
     */
    public void addSprite(Sprite sprite) {
        this.sprites.add(sprite);
    }

    /**
     * Removes the given sprite from the collection.
     *
     * @param sprite the sprite to remove.
     */
    public void removeSprite(Sprite sprite) {
        this.sprites.remove(sprite);
    }

    /**
     * Calls the timePassed() method on all sprites in the collection.
     */
    public void notifyAllTimePassed() {
        for (Sprite sprite : new ArrayList<Sprite>(this.sprites)) {
            sprite.timePassed();
        }
    }

    /**
     * Calls the drawOn() method on all sprites in the collection.
     *
     * @param surface the drawing surface to draw the sprites on.
     */
    public void drawAllOn(DrawSurface surface) {
        for (Sprite sprite : new ArrayList<Sprite>(this.sprites)) {
            sprite.drawOn(surface);
        }
    }

    /**
     * Draws the collection as a single sprite, by drawing every member.
     *
     * @param surface the drawing surface to draw on.
     */
    @Override
    public void drawOn(DrawSurface surface) {
        this.drawAllOn(surface);
    }

    /**
     * Advances the collection as a single sprite, by advancing every member.
     */
    @Override
    public void timePassed() {
        this.notifyAllTimePassed();
    }
}