package powerups;

import game.GameLevel;

import java.awt.Color;

/**
 * Arms the paddle with a limited number of laser shots.
 * <p>
 * Shots are granted rather than a mode being switched on, so two pickups give
 * twice the ammunition instead of the second one doing nothing. The paddle
 * fires them; this only hands them over.
 * </p>
 */
public class LaserPaddle implements PowerUp {
    private static final Color CAPSULE_COLOR = Color.RED;
    private static final String SYMBOL = "L";
    private static final int SHOTS_GRANTED = 12;

    @Override
    public void apply(GameLevel level) {
        level.getPaddle().grantLaserShots(SHOTS_GRANTED);
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
