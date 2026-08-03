package animation;

import biuoop.DrawSurface;
import game.Sprite;

import java.awt.Color;

/**
 * The 3-2-1 countdown shown before each turn begins.
 * <p>
 * The level is drawn beneath the numbers but never advanced: this animation
 * calls drawAllOn and deliberately not notifyAllTimePassed, so the player sees
 * exactly the board they are about to play, frozen, while the count runs down.
 * </p>
 * <p>
 * Timing is taken from the wall clock rather than by counting frames. The runner
 * targets 60 frames per second but does not guarantee them -- a slow frame makes
 * a frame-counted countdown drift, whereas the clock stays honest.
 * </p>
 * <p>
 * <b>A countdown is single-use.</b> Its clock starts on the first frame and is
 * never rewound, so running the same instance twice would finish instantly the
 * second time. Construct a fresh one for each turn. The alternative -- resetting
 * the clock from inside shouldStop() -- would make a question that the runner
 * asks before every frame quietly change the object's state, which is a worse
 * trade than a documented single use.
 * </p>
 */
public class CountdownAnimation implements Animation {
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int FONT_SIZE = 60;
    private static final Color TEXT_COLOR = Color.WHITE;

    private final int countFrom;
    private final long millisecondsPerNumber;
    private final Sprite gameScreen;
    private boolean started;
    private long startTime;

    /**
     * Constructor.
     *
     * @param numOfSeconds how long the whole countdown lasts.
     * @param countFrom    the first number displayed; it counts down to 1.
     * @param gameScreen   the level's board, drawn frozen behind the numbers.
     * @throws IllegalArgumentException if countFrom is not at least 1.
     */
    public CountdownAnimation(double numOfSeconds, int countFrom, Sprite gameScreen) {
        if (countFrom < 1) {
            throw new IllegalArgumentException("countFrom must be at least 1, but was " + countFrom);
        }
        this.countFrom = countFrom;
        this.millisecondsPerNumber = (long) (numOfSeconds * MILLISECONDS_PER_SECOND / countFrom);
        this.gameScreen = gameScreen;
        this.started = false;
    }

    @Override
    public void doOneFrame(DrawSurface d) {
        if (!this.started) {
            this.started = true;
            this.startTime = System.currentTimeMillis();
        }
        this.gameScreen.drawOn(d);

        int elapsedNumbers = (int) (this.elapsedMilliseconds() / this.millisecondsPerNumber);
        int showing = this.countFrom - elapsedNumbers;
        if (showing < 1) {
            showing = 1;
        }

        d.setColor(TEXT_COLOR);
        d.drawText(d.getWidth() / 2, d.getHeight() / 2, String.valueOf(showing), FONT_SIZE);
    }

    /**
     * The countdown ends only once the last number has had its full share of the
     * time.
     * <p>
     * Deciding this here rather than inside doOneFrame matters: the runner asks
     * this question before every frame, so the final number is never cut short
     * and no blank frame is ever drawn after the count ends.
     * </p>
     *
     * @return true once the whole countdown duration has elapsed.
     */
    @Override
    public boolean shouldStop() {
        return this.started && this.elapsedMilliseconds() >= this.countFrom * this.millisecondsPerNumber;
    }

    /**
     * How long the countdown has been running.
     *
     * @return milliseconds since the first frame.
     */
    private long elapsedMilliseconds() {
        return System.currentTimeMillis() - this.startTime;
    }
}
