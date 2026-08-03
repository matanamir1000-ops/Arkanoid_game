package highscores;

import animation.Animation;
import biuoop.DrawSurface;
import graphics.TextRenderer;

import java.awt.Color;
import java.util.List;

/**
 * Shows the high score table.
 * <p>
 * Like the other standalone screens it never ends on its own; it is wrapped in
 * a KeyPressStoppableAnimation, which owns the dismissal condition.
 * </p>
 */
public class HighScoresAnimation implements Animation {
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TITLE_COLOR = Color.YELLOW;
    private static final Color ENTRY_COLOR = Color.WHITE;
    private static final Color HINT_COLOR = Color.GRAY;
    private static final int TITLE_FONT_SIZE = 34;
    private static final int ENTRY_FONT_SIZE = 20;
    private static final int HINT_FONT_SIZE = 15;
    private static final int TITLE_Y = 80;
    private static final int FIRST_ENTRY_Y = 150;
    private static final int NAME_X = 160;
    private static final int SCORE_X = 520;
    private static final int HINT_Y = 560;

    private final HighScoresTable table;

    /**
     * Constructor.
     *
     * @param table the table to display.
     */
    public HighScoresAnimation(HighScoresTable table) {
        this.table = table;
    }

    @Override
    public void doOneFrame(DrawSurface d) {
        d.setColor(BACKGROUND_COLOR);
        d.fillRectangle(0, 0, d.getWidth(), d.getHeight());

        String title = "High Scores";
        TextRenderer.drawGlowing(d, TextRenderer.centredX(d.getWidth(), title, TITLE_FONT_SIZE),
                TITLE_Y, title, TITLE_FONT_SIZE, TITLE_COLOR, BACKGROUND_COLOR);

        List<ScoreInfo> entries = this.table.getHighScores();
        d.setColor(ENTRY_COLOR);
        int spacing = this.entrySpacing();
        for (int i = 0; i < entries.size(); i++) {
            int y = FIRST_ENTRY_Y + i * spacing;
            d.drawText(NAME_X, y, (i + 1) + ". " + entries.get(i).getName(), ENTRY_FONT_SIZE);
            d.drawText(SCORE_X, y, String.valueOf(entries.get(i).getScore()), ENTRY_FONT_SIZE);
        }

        if (entries.isEmpty()) {
            d.drawText(NAME_X, FIRST_ENTRY_Y, "No scores yet.", ENTRY_FONT_SIZE);
        }

        d.setColor(HINT_COLOR);
        d.drawText(NAME_X, HINT_Y, "press space to continue", HINT_FONT_SIZE);
    }

    /**
     * How far apart the rows sit.
     * <p>
     * Derived from how many entries the table keeps rather than fixed, so the
     * list always fits between the first row and the hint at the bottom. How
     * many entries there are is decided in another file entirely, and a fixed
     * spacing would silently run the last rows off the screen if that number
     * were ever raised.
     * </p>
     *
     * @return the vertical gap between rows, in pixels.
     */
    private int entrySpacing() {
        return (HINT_Y - FIRST_ENTRY_Y) / Math.max(1, this.table.size());
    }

    /**
     * The table screen never ends on its own; the wrapper decides.
     *
     * @return always false.
     */
    @Override
    public boolean shouldStop() {
        return false;
    }
}
