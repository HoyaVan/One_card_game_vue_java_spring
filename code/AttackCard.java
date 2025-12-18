public class AttackCard extends Card {
    private int punishment; // Number of cards the opponent must draw

    public AttackCard(int number, String shape, int punishment) {
        super(number, shape);
        this.punishment = punishment;
    }

    public int getPunishment() {
        return punishment;
    }

    public int getPunishmentValue(Card card) {
        if (card instanceof Attackable) {
            return ((Attackable) card).getPunishment();
        }
        return 0; // Default: no punishment
    }

    // Week 2: Inheritance, Polymorphism
    @Override
    public void applyEffect(DeckAndPlayerHands game, ActionCards gamePlay) {
        System.out.println("Attack card played! Opponent must draw " + punishment + " cards unless blocked.");
        // The main game loop handles the drawing logic and blocking mechanics
    }

    @Override
    public String toString() {
        return super.toString() + " (Attack)";
    }
}
