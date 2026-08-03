package animation;

import biuoop.DrawSurface;
import biuoop.GUI;
import biuoop.Sleeper;

/**
 * Drives an Animation at a fixed frame rate.
 * <p>
 * This is the inversion of control at the heart of the animation framework:
 * instead of every screen writing its own while-loop, each screen implements
 * Animation and hands itself to the runner. The timing logic lives here once.
 * </p>
 * <p>
 * The runner is reentrant. An animation may call run() from inside its own
 * doOneFrame to layer a screen on top of itself -- the pause screen does
 * exactly that. This is safe because there is no threading; the inner loop
 * simply runs to completion before the outer frame finishes.
 * </p>
 */
public class AnimationRunner {
    private GUI gui;
    private int framesPerSecond;
    private Sleeper sleeper;

    /**
     * Constructor.
     *
     * @param gui             the window that supplies drawing surfaces.
     * @param framesPerSecond the target frame rate.
     */
    public AnimationRunner(GUI gui, int framesPerSecond) {
        this.gui = gui;
        this.framesPerSecond = framesPerSecond;
        this.sleeper = new Sleeper();
    }

    /**
     * Runs the given animation until it reports that it should stop.
     *
     * @param animation the animation to drive.
     */
    public void run(Animation animation) {
        int millisecondsPerFrame = 1000 / this.framesPerSecond;

        while (!animation.shouldStop()) {
            long startTime = System.currentTimeMillis();
            DrawSurface d = this.gui.getDrawSurface();
            animation.doOneFrame(d);
            this.gui.show(d);

            long usedTime = System.currentTimeMillis() - startTime;
            long milliSecondLeftToSleep = millisecondsPerFrame - usedTime;
            if (milliSecondLeftToSleep > 0) {
                this.sleeper.sleepFor(milliSecondLeftToSleep);
            }
        }
    }
}
