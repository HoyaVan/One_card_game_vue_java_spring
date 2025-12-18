public class CardFactory {
    public static Card createCard(int number, String shape) {
        // Week 7. lambda
        return switch (number)
        {
            case 2 -> new AttackCard(2, shape, 2); // Attack card: 2, punishment: draw 2 cards
            case 1 -> new AceCard(shape); // Ace card: punishment: draw 3 cards
            case 11 -> new FaceCard(11, shape); // Jack: allows additional play
            case 12 -> new FaceCard(12, shape); // Queen: allows additional play
            case 13 -> new FaceCard(13, shape); // King: allows additional play
            case -1 -> new JokerCard(); // Joker card: punishment: draw 5 cards
            default -> new NormalCard(number, shape); // Normal cards (3-10)
        };
    }
}
