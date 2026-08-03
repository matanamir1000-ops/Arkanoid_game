package powerups;

import game.GameLevel;

import java.awt.Color;

/**
 * What a power-up does to the level when it is caught.
 * <p>
 * Applying is polymorphic: whoever catches one tells it to act and never asks
 * which one it is. Adding a new power-up is a new implementation of this
 * interface and no change to the falling object, the dropper, or the paddle.
 * </p>
 */
public interface PowerUp {

    /**
     * Takes effect on the level that caught it.
     *
     * @param level the level to act on.
     */
    void apply(GameLevel level);

    /**
     * The colour the falling capsule is drawn in.
     *
     * @return the capsule colour.
     */
    Color getColor();

    /**
     * The single character drawn on the capsule to identify it.
     *
     * @return the capsule's letter.
     */
    String getSymbol();
}
