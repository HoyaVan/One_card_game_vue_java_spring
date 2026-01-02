package onecardgame;

import onecardgame.cards.Card;

/**
 * Interface for objects that can provide cards through drawing.
 * Represents any source from which cards can be drawn (e.g., deck, discard pile).
 */
public interface Drawable {
    /**
     * Draws a card from this drawable source.
     * 
     * @return The card drawn
     * @throws IllegalStateException if no cards are available to draw
     */
    Card drawCard();

    /**
     * Checks if this drawable source is empty (no cards available).
     * 
     * @return true if empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Gets the number of cards remaining in this drawable source.
     * 
     * @return The number of cards available
     */
    int size();
}
