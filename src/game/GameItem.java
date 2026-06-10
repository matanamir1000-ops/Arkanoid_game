package game;

/**
 * Interface for any object that can be added to the game.
 */
public interface GameItem {
    /**
     * Adds the item to the given game.
     *
     * @param game the game to add the item to.
     */
    void addToGame(Game game);
}