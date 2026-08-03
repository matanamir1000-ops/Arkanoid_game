import game.GameLevel;

/**
 * The main entry point for the Arkanoid game.
 */
public class Ass5Game {
    /**
     * Main entry point to start the game.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        GameLevel game = new GameLevel();
        game.initialize();
        game.run();
    }
}
