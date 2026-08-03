package powerups;

import game.GameLevel;
import geometry.Point;
import geometry.Velocity;
import sprites.Ball;

import java.awt.Color;

/**
 * Puts another ball into play.
 * <p>
 * The new ball is launched from the middle of the playfield rather than from
 * the paddle, so that catching this while the paddle is at the edge does not
 * immediately drop the new ball down the side.
 * </p>
 */
public class ExtraBall implements PowerUp {
    private static final Color CAPSULE_COLOR = Color.WHITE;
    private static final String SYMBOL = "B";
    private static final int BALL_RADIUS = 5;
    private static final int LAUNCH_Y = 400;
    private static final double LAUNCH_DX = 3;
    private static final double LAUNCH_DY = -4;

    @Override
    public void apply(GameLevel level) {
        Ball ball = new Ball(new Point(level.getScreenWidth() / 2.0, LAUNCH_Y),
                BALL_RADIUS, Color.WHITE);
        ball.setVelocity(new Velocity(LAUNCH_DX, LAUNCH_DY));
        ball.addToGame(level);
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
