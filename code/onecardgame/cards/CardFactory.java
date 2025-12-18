package onecardgame.cards;

public class CardFactory {
    
    private CardFactory() {
        // Private constructor to prevent instantiation
    }
    
    public static Card createCard(final int number, final String shape) throws IllegalArgumentException {
        
        // Validate card parameters
        CardValidator.validateCardParameters(number, shape);
        
        // Week 7. lambda
        return switch (number)
        {
            case Card.TWO_NUMBER -> new NumTwoCard(number, shape); // NumTwo card: punishment: draw 2 cards
            case Card.ACE_NUMBER -> new AceCard(shape); // Ace card: punishment: draw ACE_PUNISHMENT cards
            case Card.JACK_NUMBER -> new FaceCard(Card.JACK_NUMBER, shape); // Jack: allows additional play
            case Card.QUEEN_NUMBER -> new FaceCard(Card.QUEEN_NUMBER, shape); // Queen: allows additional play
            case Card.KING_NUMBER -> new FaceCard(Card.KING_NUMBER, shape); // King: allows additional play
            case Card.JOKER_NUMBER -> new JokerCard(); // Joker card: punishment: draw JOKER_PUNISHMENT cards
            default -> new NormalCard(number, shape); // Normal cards (3-10)
        };
    }
}
