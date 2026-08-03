package highscores;

/**
 * One entry in the high score table: a name and what they scored.
 * <p>
 * Immutable. An entry that has been recorded is a fact about something that
 * already happened, and nothing should be able to edit it afterwards.
 * </p>
 * <p>
 * The entry also owns the line format it is stored as. Keeping the format with
 * the data means the name can be cleaned once, in the constructor, so an entry
 * is never observable in a state it could not be written in -- the name shown
 * on screen this session is always the name that will be read back next time.
 * </p>
 */
public class ScoreInfo {
    private static final String SEPARATOR = "|";
    private static final String FALLBACK_NAME = "Anonymous";

    private final String name;
    private final int score;

    /**
     * Constructor.
     *
     * @param name  the player's name; cleaned of anything that would corrupt
     *              the stored format, and replaced if blank or absent.
     * @param score what they scored.
     */
    public ScoreInfo(String name, int score) {
        this.name = clean(name);
        this.score = score;
    }

    /**
     * The player's name.
     *
     * @return the name, already safe to store.
     */
    public String getName() {
        return this.name;
    }

    /**
     * What they scored.
     *
     * @return the score.
     */
    public int getScore() {
        return this.score;
    }

    /**
     * This entry as a single stored line.
     *
     * @return the line to write.
     */
    public String toLine() {
        return this.name + SEPARATOR + this.score;
    }

    /**
     * Reads an entry back from a stored line.
     * <p>
     * Returns null rather than throwing for anything that is not an entry. A
     * truncated last line is what a crash during a save leaves behind, and one
     * bad line should cost the player one score rather than all of them.
     * </p>
     *
     * @param line the line as stored.
     * @return the entry, or null if the line is not one.
     */
    public static ScoreInfo fromLine(String line) {
        int split = line.lastIndexOf(SEPARATOR);
        if (split < 0) {
            return null;
        }
        String storedName = line.substring(0, split);
        String storedScore = line.substring(split + 1).trim();
        if (storedName.isEmpty() || storedScore.isEmpty()) {
            return null;
        }
        try {
            return new ScoreInfo(storedName, Integer.parseInt(storedScore));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Strips the characters that would corrupt the stored format.
     * <p>
     * A name containing the separator or a line break would be read back as a
     * different entry, or as two.
     * </p>
     *
     * @param name the name as entered; may be null if a dialog was cancelled.
     * @return the name, safe to store.
     */
    private static String clean(String name) {
        if (name == null) {
            return FALLBACK_NAME;
        }
        String cleaned = name.replace(SEPARATOR, " ").replace("\n", " ").replace("\r", " ").trim();
        if (cleaned.isEmpty()) {
            return FALLBACK_NAME;
        }
        return cleaned;
    }
}
