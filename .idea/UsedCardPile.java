import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class UsedCardPile implements Iterable<Card> {
    private List<Card> usedCards;

    public UsedCardPile() {
        this.usedCards = new ArrayList<>();
    }

    public void addCard(Card card) {
        usedCards.add(card);
    }

    @Override
    public Iterator<Card> iterator() {
        return usedCards.iterator();
    }
}
