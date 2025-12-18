// Week 3: Abstract Class
public abstract class Card {
    private final int number; // 1-10, 11 for J, 12 for Q, 13 for K, or -1 for Joker
    private final String shape; // Hearts, Diamonds, Clubs, Spades

    public Card(final int number,
                final String shape) {
        this.number = number;
        this.shape = shape;
    }

    public int getNumber() {
        return number;
    }

    public String getShape() {
        return shape;
    }

    // Week 3: Final Method
    // Checks if this card matches another card
    public final boolean matches(Card otherCard) {
        if (this instanceof JokerCard) {
            return true; // Joker always matches
        }
        if (otherCard instanceof JokerCard) {
            return true; // Any card matches a Joker
        }
        return this.shape.equals(otherCard.getShape()) || this.number == otherCard.getNumber();
    }

    // Abstract method to be implemented by subclasses for special effects
    public abstract void applyEffect(DeckAndPlayerHands game, ActionCards gamePlay);

    @Override
    public String toString() {
        switch (number) {
            case 1:  return "Ace of " + shape;
            case 11: return "J of " + shape;
            case 12: return "Q of " + shape;
            case 13: return "K of " + shape;
            case -1: return "Joker";
            default: return number + " of " + shape;
        }
    }
}
