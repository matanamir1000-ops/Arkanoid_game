package animation;

import biuoop.DrawSurface;

import java.awt.Color;

/**
 * The screen shown while the game is paused.
 * <p>
 * shouldStop always returns false: this screen has no opinion about when it
 * ends. It is always wrapped in a KeyPressStoppableAnimation, which owns the
 * dismissal condition.
 * </p>
 * <p>
 * The background is repainted every frame because the runner hands out a fresh
 * surface each time; without the fill, the light default backdrop would swallow
 * the white text.
 * </p>
 */
public class PauseScreen implements Animation {
    private static final int MESSAGE_X = 130;
    private static final int MESSAGE_Y = 300;
    private static final int MESSAGE_FONT_SIZE = 32;

    @Override
    public void doOneFrame(DrawSurface d) {
        d.setColor(Color.BLACK);
        d.fillRectangle(0, 0, d.getWidth(), d.getHeight());
        d.setColor(Color.WHITE);
        d.drawText(MESSAGE_X, MESSAGE_Y, "paused -- press space to continue", MESSAGE_FONT_SIZE);
    }

    @Override
    public boolean shouldStop() {
        return false;
    }
}
