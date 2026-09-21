package terminal_mode.cards;

public class JokerCard extends Card implements Attackable {
    private static final int PUNISHMENT_VALUE = 5;
    
    private final int punishmentValue;
    
    public JokerCard() {
        super(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        this.punishmentValue = PUNISHMENT_VALUE;
    }

    @Override
    public int getPunishment() {
        return punishmentValue;
    }

    // either attack or defend
    @Override
    public String toString() {
        return super.toString();
    }
}
