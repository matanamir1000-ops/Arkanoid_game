package game;

import biuoop.DrawSurface;

/**
 * A Sprite is a game object that can be drawn to the screen
 * and can be notified that time has passed.
 */
public interface Sprite {

    /**
     * Draws the sprite to the screen.
     *
     * @param surface the drawing surface to draw the sprite on.
     */
    void drawOn(DrawSurface surface);

    /**
     * Notifies the sprite that a unit of time has passed.
     */
    void timePassed();
}