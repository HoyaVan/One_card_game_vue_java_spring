package onecardgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import onecardgame.cards.*;

/**
 * Manages the deck of cards for the One Card game.
 * Handles deck creation, shuffling, and drawing cards.
 */
public class Deck implements Drawable {
    private static final String[] CARD_SHAPES = {
        Card.CARD_SHAPE_HEARTS,
        Card.CARD_SHAPE_DIAMONDS,
        Card.CARD_SHAPE_CLUBS,
        Card.CARD_SHAPE_SPADES
    };

    private static final int NUMBER_OF_JOKERS = 2;
    private static final int MIN_INDEX = 0;
    
    private final List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
        initializeDeck();
    }

    /**
     * Initializes a standard deck with 52 cards (Ace-King in 4 suits) plus {NUMBER_OF_JOKERS} Jokers.
     */
    private void initializeDeck() {
        for (String shape : CARD_SHAPES) {
            for (int rank = Card.ACE_NUMBER; rank <= Card.KING_NUMBER; rank++) { // Ace to King
                cards.add(CardFactory.createCard(rank, shape));
            }
        }
        // Add Joker cards - one red, one black
        cards.add(CardFactory.createCard(Card.JOKER_NUMBER, "RED"));
        cards.add(CardFactory.createCard(Card.JOKER_NUMBER, "BLACK"));
        Collections.shuffle(cards); // Shuffle the deck
    }

    /**
     * Draws a card from the top of the deck.
     * 
     * @return The card drawn from the deck
     * @throws IllegalStateException if the deck is empty
     */
    @Override
    public Card drawCard() {
        GameValidator.validateDeckNotEmpty(cards.size());
        return cards.remove(MIN_INDEX);
    }

    /**
     * Gets the number of cards remaining in the deck.
     * 
     * @return The size of the deck
     */
    @Override
    public int size() {
        return cards.size();
    }

    /**
     * Checks if the deck is empty.
     * 
     * @return true if the deck is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Gets the deck as a list (for initial card setup).
     * Note: This exposes internal state but is needed for initial game setup.
     * 
     * @return The list of cards in the deck
     */
    public List<Card> getCards() {
        return cards;
    }
    
    /**
     * Adds cards to the deck (used when reshuffling used card pile).
     * Cards are added to the bottom of the deck (end of list).
     * 
     * @param cardsToAdd List of cards to add to the deck
     */
    public void addCards(List<Card> cardsToAdd) {
        if (cardsToAdd != null && !cardsToAdd.isEmpty()) {
            cards.addAll(cardsToAdd);
        }
    }
}
