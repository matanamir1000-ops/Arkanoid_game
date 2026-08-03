package powerups;

import game.GameLevel;

import java.awt.Color;

/**
 * Makes the paddle wider.
 * <p>
 * The paddle enforces its own maximum, so repeated pickups stop mattering
 * rather than breaking anything. That limit is not cosmetic: a paddle as wide
 * as the playfield overlaps both screen edges at once, and the wrap logic
 * cannot represent a paddle that is off both sides simultaneously.
 * </p>
 */
public class PaddleExpansion implements PowerUp {
    private static final Color CAPSULE_COLOR = Color.ORANGE;
    private static final String SYMBOL = "W";
    private static final int WIDTH_INCREASE = 40;

    @Override
    public void apply(GameLevel level) {
        level.getPaddle().expand(WIDTH_INCREASE);
    }

    @Override
    public Color getColor() {
        return CAPSULE_COLOR;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }
}
