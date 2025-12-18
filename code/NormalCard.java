public class NormalCard extends Card {
    public NormalCard(int number, String shape) {
        super(number, shape);
    }

    // Week 2: Inheritance, Polymorphism
    @Override
    public void applyEffect(DeckAndPlayerHands game, ActionCards gamePlay) {
        System.out.println("Normal card (" + toString() + ") played. No special effect.");
        // No special effects for normal cards
    }
}
