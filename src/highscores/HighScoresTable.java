package highscores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The best scores so far, kept in order and persisted to a file.
 * <p>
 * The table holds a fixed number of entries. Reading and writing it must never
 * be able to stop the game starting: a missing file, an unreadable file and a
 * corrupt file all degrade to an empty or partial table rather than to an
 * exception escaping into the caller.
 * </p>
 */
public class HighScoresTable {
    private final List<ScoreInfo> scores;
    private final int size;

    /**
     * Constructor.
     *
     * @param size how many entries the table keeps.
     */
    public HighScoresTable(int size) {
        this.scores = new ArrayList<>();
        this.size = size;
    }

    /**
     * Adds a score, keeping the table in order and within its size.
     * <p>
     * The insert is a plain loop rather than a sort, because a table of ten
     * entries is not worth a sort and the loop states the ordering rule where a
     * reader can see it.
     * </p>
     *
     * @param scoreInfo the entry to add; ignored if it does not make the table.
     */
    public void add(ScoreInfo scoreInfo) {
        int rank = this.getRank(scoreInfo.getScore());
        if (rank > this.size) {
            return;
        }
        this.scores.add(rank - 1, scoreInfo);
        if (this.scores.size() > this.size) {
            this.scores.remove(this.scores.size() - 1);
        }
    }

    /**
     * Where a given score would place.
     * <p>
     * A score equal to one already in the table ranks below it. Whoever got
     * there first keeps the higher place, which is the usual convention and
     * means an equal score never displaces an incumbent.
     * </p>
     *
     * @param score the score to rank.
     * @return the position it would take, counting from 1; a value greater than
     *         the table size means it does not make the table.
     */
    public int getRank(int score) {
        int rank = 1;
        for (ScoreInfo existing : this.scores) {
            if (score > existing.getScore()) {
                return rank;
            }
            rank++;
        }
        return rank;
    }

    /**
     * The entries, best first.
     *
     * @return a copy of the table.
     */
    public List<ScoreInfo> getHighScores() {
        return new ArrayList<>(this.scores);
    }

    /**
     * How many entries the table can hold.
     *
     * @return the table size.
     */
    public int size() {
        return this.size;
    }

    /**
     * Empties the table.
     */
    public void clear() {
        this.scores.clear();
    }

    /**
     * Loads the table from a file, replacing whatever it held.
     * <p>
     * A missing file is checked for rather than caught. Relying on the
     * exception would treat "no scores yet" and "cannot read this file" as the
     * same event, and both need to degrade the same way -- so both are handled
     * as an empty table, deliberately and visibly.
     * </p>
     * <p>
     * A malformed line is skipped on its own rather than abandoning the file. A
     * single truncated line at the end, which is what a crash during a save
     * leaves behind, should not cost the player every score they had.
     * </p>
     *
     * @param file the file to read.
     */
    public void load(File file) {
        this.clear();
        if (!file.exists()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                this.addParsedLine(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.err.println("Could not read high scores: " + e.getMessage());
        } finally {
            closeQuietly(reader);
        }
    }

    /**
     * Writes the table to a file.
     * <p>
     * A failure here is reported and swallowed. Not being able to record a
     * score is a disappointment; it is not a reason to end the player's session
     * with a stack trace.
     * </p>
     *
     * @param file the file to write.
     */
    public void save(File file) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            for (ScoreInfo info : this.scores) {
                writer.println(info.toLine());
            }
        } catch (IOException e) {
            System.err.println("Could not save high scores: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Reads a table from a file.
     *
     * @param file the file to read.
     * @param size how many entries the table keeps.
     * @return the loaded table, empty if the file is missing or unreadable.
     */
    public static HighScoresTable loadFromFile(File file, int size) {
        HighScoresTable table = new HighScoresTable(size);
        table.load(file);
        return table;
    }

    /**
     * Parses one stored line and adds it, ignoring anything malformed.
     * <p>
     * The line format belongs to ScoreInfo, which is the type that has to write
     * it. This method only decides what to do with a line that turns out not to
     * be an entry: skip it and keep reading.
     * </p>
     *
     * @param line the line as stored.
     */
    private void addParsedLine(String line) {
        ScoreInfo parsed = ScoreInfo.fromLine(line);
        if (parsed == null) {
            System.err.println("Skipping malformed high score line: " + line);
            return;
        }
        this.add(parsed);
    }

    /**
     * Closes a reader without letting the close itself throw.
     *
     * @param reader the reader to close; may be null if opening it failed.
     */
    private static void closeQuietly(BufferedReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not close high scores file: " + e.getMessage());
        }
    }
}
