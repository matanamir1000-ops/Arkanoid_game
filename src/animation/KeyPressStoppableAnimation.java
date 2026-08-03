package animation;

import biuoop.DrawSurface;
import biuoop.KeyboardSensor;

/**
 * Decorator that ends any animation when a chosen key is pressed.
 * <p>
 * The decorated animation keeps full control of what is drawn; this wrapper
 * only overrides the stopping condition. That keeps screens such as PauseScreen
 * free of keyboard logic entirely.
 * </p>
 * <p>
 * The waitingForRelease guard exists because biuoop reports only that a key is
 * down, never that it was released. Without it, the key press that dismissed
 * one screen would still be held when the next screen opened and would dismiss
 * that one on its very first frame. The flag starts true, meaning "a key may
 * still be held over from the previous screen"; it is only cleared once the key
 * is observed to be up, and only then can a press stop this animation.
 * </p>
 */
public class KeyPressStoppableAnimation implements Animation {
    private final KeyboardSensor sensor;
    private final String key;
    private final Animation animation;
    private boolean waitingForRelease;
    private boolean stop;

    /**
     * Constructor.
     *
     * @param sensor    the keyboard to poll.
     * @param key       the key that dismisses the animation.
     * @param animation the animation being wrapped.
     */
    public KeyPressStoppableAnimation(KeyboardSensor sensor, String key, Animation animation) {
        this.sensor = sensor;
        this.key = key;
        this.animation = animation;
        this.waitingForRelease = true;
        this.stop = false;
    }

    @Override
    public void doOneFrame(DrawSurface d) {
        this.animation.doOneFrame(d);
        if (this.sensor.isPressed(this.key)) {
            if (!this.waitingForRelease) {
                this.stop = true;
            }
        } else {
            this.waitingForRelease = false;
        }
    }

    @Override
    public boolean shouldStop() {
        return this.stop || this.animation.shouldStop();
    }
}
