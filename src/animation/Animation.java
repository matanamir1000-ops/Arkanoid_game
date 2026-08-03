package animation;

import biuoop.DrawSurface;

/**
 * A single interactive screen that the AnimationRunner can drive.
 * <p>
 * An Animation owns the contents of one frame and the decision of when it is
 * finished. It does not own the timing, the drawing surface or the window --
 * those belong to the AnimationRunner, which supplies a fresh surface every
 * frame and shows it once doOneFrame returns.
 * </p>
 */
public interface Animation {

    /**
     * Draws and advances the animation by exactly one frame.
     *
     * @param d the surface to draw this frame on.
     */
    void doOneFrame(DrawSurface d);

    /**
     * Reports whether the animation has finished.
     *
     * @return true once the runner should stop calling doOneFrame.
     */
    boolean shouldStop();
}
