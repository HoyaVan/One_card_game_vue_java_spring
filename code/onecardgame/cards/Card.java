package onecardgame.cards;

// Week 3: Abstract Class
public abstract class Card {
    // Card rank constants
    public static final int ACE_NUMBER = 1;
    public static final int TWO_NUMBER = 2;
    public static final int JACK_NUMBER = 11;
    public static final int QUEEN_NUMBER = 12;
    public static final int KING_NUMBER = 13;
    public static final int JOKER_NUMBER = -1;

    // Card suit/shape constants
    public static final String CARD_SHAPE_HEARTS = "Hearts";
    public static final String CARD_SHAPE_DIAMONDS = "Diamonds";
    public static final String CARD_SHAPE_CLUBS = "Clubs";
    public static final String CARD_SHAPE_SPADES = "Spades";
    public static final String CARD_SHAPE_ANY = "Any";

    // Card display strings
    public static final String CARD_ACE_OF = "Ace of ";
    public static final String CARD_J_OF = "J of ";
    public static final String CARD_Q_OF = "Q of ";
    public static final String CARD_K_OF = "K of ";
    public static final String CARD_JOKER = "Joker";
    public static final String CARD_OF_FORMAT = "%d of %s";
    public static final String CARD_NUM_TWO_OF = "Num Two of ";

    private final int number;
    private final String shape;

    protected Card(final int number, final String shape) {
        CardValidator.validateCardParameters(number, shape);
        this.number = number;
        this.shape = shape;
    }

    public int getNumber() {
        return number;
    }

    public String getShape() {
        return shape;
    }

    // Week 2: Inheritance, Polymorphism - demonstrated through toString() override
    @Override
    public String toString() {
        return switch (number) {
            case TWO_NUMBER -> CARD_NUM_TWO_OF + shape;
            case ACE_NUMBER -> CARD_ACE_OF + shape;
            case JACK_NUMBER -> CARD_J_OF + shape;
            case QUEEN_NUMBER -> CARD_Q_OF + shape;
            case KING_NUMBER -> CARD_K_OF + shape;
            case JOKER_NUMBER -> CARD_JOKER;
            default -> String.format(CARD_OF_FORMAT, number, shape);
        };
    }
}
