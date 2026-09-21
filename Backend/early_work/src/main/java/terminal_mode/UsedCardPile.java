package terminal_mode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import terminal_mode.cards.Card;

class UsedCardPile implements Iterable<Card> {
    private final List<Card> usedCards;

    public UsedCardPile() {
        this.usedCards = new ArrayList<>();
    }

    public void addCard(final Card card) {
        usedCards.add(card);
    }

    public Card getLastCard() {
        if (usedCards.isEmpty()) {
            return null;
        }
        return usedCards.get(usedCards.size() - 1);
    }

    public boolean isEmpty() {
        return usedCards.isEmpty();
    }

    public List<Card> getCards() {
        return new ArrayList<>(usedCards); // Return a copy to prevent external modification
    }

    @Override
    public Iterator<Card> iterator() {
        return usedCards.iterator();
    }
}
