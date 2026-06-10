import game.Game;

/**
 * ID - 218062719
 * The main entry point for the Arkanoid game (Assignment 5).
 */
public class Ass5Game {
    /**
     * Main entry point to start the game.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
        game.run();

    }
}
