package onecardgame.cards;

public class NumTwoCard extends Card implements Attackable {
    private static final int PUNISHMENT_VALUE = 2;

    private final int punishmentValue;
 
    public NumTwoCard(final int rank, final String shape) {
        super(rank, shape);
        this.punishmentValue = PUNISHMENT_VALUE;
    }

    @Override
    public int getPunishment() {
        return punishmentValue;
    }

    // Week 2: Inheritance, Polymorphism - either attack or defend
    @Override
    public String toString() {
        return super.toString();
    }
}
