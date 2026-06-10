package game;

/**
 * A simple class used for counting things.
 * It holds an integer value and provides methods to increase, decrease,
 * and get the current count.
 */
public class Counter {
    private int counter;

    /**
     * Constructor for a Counter.
     *
     * @param counter the initial value of the counter.
     */
    public Counter(int counter) {
        this.counter = counter;
    }

    /**
     * Adds a given number to the current count.
     *
     * @param number the number to add.
     */
    public void increase(int number) {
        this.counter += number;
    }

    /**
     * Subtracts a given number from the current count.
     *
     * @param number the number to subtract.
     */
    public void decrease(int number) {
        this.counter -= number;
    }

    /**
     * Gets the current count value.
     *
     * @return the current count.
     */
    public int getValue() {
        return this.counter;
    }
}