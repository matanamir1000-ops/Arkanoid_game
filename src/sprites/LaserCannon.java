package sprites;

import biuoop.KeyboardSensor;
import game.GameLevel;
import geometry.Point;

/**
 * The paddle's weapon: a magazine and the rule for firing it.
 * <p>
 * Kept apart from the paddle because it is a second reason for the paddle to
 * change. How fast a weapon fires, how much ammunition it holds and what it
 * launches have nothing to do with moving left, bouncing a ball or catching a
 * capsule. The paddle owns one of these and forwards to it.
 * </p>
 */
public class LaserCannon {
    private int shots;
    private boolean waitingForRelease;

    /**
     * Constructor: a cannon starts empty.
     */
    public LaserCannon() {
        this.shots = 0;
        this.waitingForRelease = false;
    }

    /**
     * Adds ammunition.
     * <p>
     * Shots accumulate rather than replacing a previous grant, so a second
     * pickup is worth as much as the first.
     * </p>
     *
     * @param count how many bolts to add.
     */
    public void grantShots(int count) {
        this.shots += count;
    }

    /**
     * Fires one bolt if the key was pressed afresh and there is ammunition.
     * <p>
     * The release check is what makes this one shot per press. Without it a
     * held key would fire sixty times a second and empty the magazine in a
     * fifth of a second.
     * </p>
     *
     * @param keyboard the keyboard to read the fire key from.
     * @param muzzle   where a bolt would leave from.
     * @param level    the level the bolt travels through; ignored if null,
     *                 which is how a paddle not yet added to a game behaves.
     */
    public void fireIfRequested(KeyboardSensor keyboard, Point muzzle, GameLevel level) {
        if (!keyboard.isPressed(KeyboardSensor.SPACE_KEY)) {
            this.waitingForRelease = false;
            return;
        }
        if (this.waitingForRelease || this.shots <= 0 || level == null) {
            return;
        }
        this.waitingForRelease = true;
        this.shots--;
        new Laser(muzzle).addToGame(level);
    }
}
