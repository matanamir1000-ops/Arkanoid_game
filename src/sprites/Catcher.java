package sprites;

import geometry.Rectangle;

/**
 * Something that can catch a falling object.
 * <p>
 * A falling power-up holds one of these rather than holding a Paddle, which
 * makes it structurally incapable of asking what it is falling onto. There is
 * no type to check, because the only thing it was ever given is the question it
 * is allowed to ask.
 * </p>
 * <p>
 * The question is asked of the catcher rather than answered by measuring it
 * from outside, and that matters here. A paddle's collision rectangle is only
 * its visible slice, so during a screen wrap the wrapped half is invisible to
 * any external overlap test and a catch there would silently fail. Only the
 * paddle knows about both halves of itself.
 * </p>
 */
public interface Catcher {

    /**
     * Whether this catcher currently overlaps the given area.
     *
     * @param area the falling object's bounds.
     * @return true if the object should be considered caught.
     */
    boolean catches(Rectangle area);
}
