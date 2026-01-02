package onecardgame.cards;

public class CardFactory {
    
    private CardFactory() {
        // Private constructor to prevent instantiation
    }
    
    public static Card createCard(final int rank, final String shape) throws IllegalArgumentException {
        
        // Validate card parameters
        CardValidator.validateCardParameters(rank, shape);
        
        // Week 7. lambda
        return switch (rank)
        {
            case Card.TWO_NUMBER -> new NumTwoCard(rank, shape); // NumTwo card: punishment: draw 2 cards
            case Card.ACE_NUMBER -> new AceCard(shape); // Ace card: punishment: draw ACE_PUNISHMENT cards
            case 7 -> new NumSevenCard(rank, shape); // NumSeven card: changes shape when played
            case Card.JACK_NUMBER -> new FaceCard(Card.JACK_NUMBER, shape); // Jack: allows additional play
            case Card.QUEEN_NUMBER -> new FaceCard(Card.QUEEN_NUMBER, shape); // Queen: allows additional play
            case Card.KING_NUMBER -> new FaceCard(Card.KING_NUMBER, shape); // King: allows additional play
            case Card.JOKER_NUMBER -> new JokerCard(); // Joker card: punishment: draw JOKER_PUNISHMENT cards
            default -> new NormalCard(rank, shape); // Normal cards (3-10, excluding 7)
        };
    }
}
