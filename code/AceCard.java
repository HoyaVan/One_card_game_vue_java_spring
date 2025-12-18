public class AceCard extends Card implements Attackable {
    private final int punishment = 3;

    public AceCard(String shape) {
        super(1, shape); // 1 represents Ace
    }

    @Override
    public void applyEffect(DeckAndPlayerHands game, ActionCards gamePlay) {
        System.out.println("Ace card (" + toString() + ") played! Blocks an attack or initiates an attack for 3 cards!");
        // Logic for blocking or initiating an attack
    }

    @Override
    public int getPunishment() {
        return punishment;
    }
}
