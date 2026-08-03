package sprites;

import geometry.Rectangle;

/**
 * Shows the name of the level being played.
 * <p>
 * Unlike the other indicators this one watches nothing: a level's name is fixed
 * for as long as the level exists.
 * </p>
 */
public class LevelNameIndicator extends TextIndicator {
    private final String levelName;

    /**
     * Constructs a new LevelNameIndicator.
     *
     * @param levelName the name to display.
     * @param rect      the section of the top strip this indicator draws inside.
     */
    public LevelNameIndicator(String levelName, Rectangle rect) {
        super(rect);
        this.levelName = levelName;
    }

    @Override
    protected String text() {
        return "Level Name: " + this.levelName;
    }
}
