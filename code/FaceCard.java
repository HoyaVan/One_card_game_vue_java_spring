public class FaceCard extends Card {
    // Week 1. Constructor Chaining
    public FaceCard(int number, String shape) {
        super(number, shape);
    }

    @Override
    public void applyEffect(DeckAndPlayerHands game, ActionCards gamePlay) {
        System.out.println("Face card (" + toString() + ") played! You can play another card of the same shape.");
        // Logic for allowing the player to play another card can be handled here
    }
}
