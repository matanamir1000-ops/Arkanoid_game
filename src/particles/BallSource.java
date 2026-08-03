package particles;

import sprites.Ball;

import java.util.List;

/**
 * Somewhere that balls currently in play can be read from.
 * <p>
 * A trail needs exactly one thing from the level: which balls are alive right
 * now. Declaring that as its own interface means the trail cannot reach the
 * rest of the level -- it has no way to remove a block, start a turn or touch
 * the environment, because none of that was ever offered to it.
 * </p>
 */
public interface BallSource {

    /**
     * The balls currently in play.
     *
     * @return a list safe to iterate while its contents may be removed.
     */
    List<Ball> getBalls();
}
