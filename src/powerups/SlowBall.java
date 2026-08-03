package powerups;

import game.GameLevel;
import geometry.Velocity;
import sprites.Ball;

import java.awt.Color;

/**
 * Slows every ball currently in play.
 * <p>
 * Speed is scaled rather than set, so a ball that has already been slowed once
 * gets slower again instead of snapping back to a fixed pace. A floor stops
 * repeated pickups from bringing the game to a standstill.
 * </p>
 */
public class SlowBall implements PowerUp {
    private static final Color CAPSULE_COLOR = Color.CYAN;
    private static final String SYMBOL = "S";
    private static final double SLOW_FACTOR = 0.7;
    private static final double MINIMUM_SPEED = 2.0;

    @Override
    public void apply(GameLevel level) {
        for (Ball ball : level.getBalls()) {
            Velocity current = ball.getVelocity();
            double dx = current.getDx() * SLOW_FACTOR;
            double dy = current.getDy() * SLOW_FACTOR;
            if (Math.sqrt(dx * dx + dy * dy) < MINIMUM_SPEED) {
                continue;
            }
            ball.setVelocity(new Velocity(dx, dy));
        }
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
