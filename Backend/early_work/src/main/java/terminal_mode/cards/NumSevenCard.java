package terminal_mode.cards;

public class NumSevenCard extends Card {
    private static final int SEVEN_NUMBER = 7;

    public NumSevenCard(final int number, final String shape) {
        super(number, shape);
    }

    /**
     * Creates a new NumSevenCard with a different shape.
     * Used when the shape-change effect is applied.
     * 
     * @param newShape The new shape to apply
     * @return A new NumSevenCard with the specified shape
     */
    public NumSevenCard withShape(final String newShape) {
        return new NumSevenCard(SEVEN_NUMBER, newShape);
    }
}


