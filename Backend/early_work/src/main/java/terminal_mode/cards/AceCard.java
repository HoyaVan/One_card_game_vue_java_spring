package terminal_mode.cards;

public class AceCard extends Card implements Attackable {
    private static final int PUNISHMENT_VALUE = 3;

    private final int punishmentValue;

    public AceCard(final String shape) {
        super(Card.ACE_NUMBER, shape);
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
