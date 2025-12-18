import java.util.ArrayList;
import java.util.List;

public class ActionCards {
    private List<Card> usedCards;
    private Card lastUsedCard;
    private int accumulatedDraws; // Tracks the number of accumulated draws

    public int getAccumulatedDraws() {
        return accumulatedDraws;
    }

    public void resetAccumulatedDraws() {
        accumulatedDraws = 0; // Reset accumulated draws to 0
    }

    // Week 6: Generics Methods
    public <T extends Card> List<T> getCardsOfType(List<Card> cards, Class<T> type) {
        return cards.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    public ActionCards() {
        this.usedCards = new ArrayList<>();
        this.accumulatedDraws = 0; // Initialize accumulated draws
    }

    // Add the playCard method to handle playing a card
    public void playCard(Card card, boolean isInitialTurn, DeckAndPlayerHands deckAndPlayerHands)
    {
        usedCards.add(card); // Add the card to the used cards pile
        lastUsedCard = card; // Update the last used card

        if (!isInitialTurn) {
            if (card instanceof AttackCard) {
                int punishment = getPunishmentValue(card);
                accumulatedDraws += punishment; // Track accumulated draws locally
                System.out.println("DEBUG: Accumulated draws increased by " + punishment);
            }
            card.applyEffect(deckAndPlayerHands, this); // Pass the required arguments
        }
    }

    // Overloaded method to play a card from a hand by index
    public void playCard(List<Card> hand, int index, DeckAndPlayerHands deckAndPlayerHands) {
        if (index < 0 || index >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index.");
        }

        Card card = hand.get(index); // Get the card without removing it

        // Validate the card before playing
        try {
            if (isCardPlayable(card, lastUsedCard, false)) {
                hand.remove(index); // Remove the card only if it's valid
                playCard(card, false, deckAndPlayerHands); // Pass the necessary arguments
            } else {
                throw new InvalidMoveException("This card cannot be played.");
            }
        } catch (InvalidMoveException e) {
            System.out.println("DEBUG: " + e.getMessage());
        }
    }

    public List<Card> getUsedCards() {
        return usedCards;
    }

    public Card getLastUsedCard() {
        // Return the last card in the usedCards list
        if (usedCards.isEmpty()) {
            return null; // No card has been played yet
        }
        return lastUsedCard;
    }

    public boolean isCardPlayable(Card cardToPlay, Card lastUsedCard, boolean isInitialTurn) throws InvalidMoveException
    {
        System.out.println("DEBUG: Checking if card is playable - Card: " + cardToPlay + ", Last Card: " + lastUsedCard);
        System.out.println("DEBUG: Is Initial Turn: " + isInitialTurn);
        if (lastUsedCard == null || isInitialTurn) {
            System.out.println("DEBUG: Any card is playable as no card is on the table.");
            return true; // Any card is playable if no card is on the table
        }

        if (lastUsedCard instanceof AttackCard || lastUsedCard instanceof AceCard || lastUsedCard instanceof JokerCard) {
            if (cardToPlay instanceof AttackCard && cardToPlay.getNumber() == lastUsedCard.getNumber()) {
                System.out.println("DEBUG: Attack card blocks another attack card.");
                return true; // Attack card blocks the same-number attack card
            }
            if (cardToPlay instanceof AceCard && cardToPlay.getShape().equals(lastUsedCard.getShape())) {
                System.out.println("DEBUG: Ace blocks another Ace with the same shape.");
                return true; // Ace blocks another Ace with the same shape
            }
            if (cardToPlay instanceof JokerCard) {
                System.out.println("DEBUG: Joker blocks any attack.");
                return true; // Joker blocks any attack
            }
            throw new InvalidMoveException("Invalid move. The card can't protect from Attack.");
        }

        if (cardToPlay.getNumber() == lastUsedCard.getNumber() ||
                cardToPlay.getShape().equals(lastUsedCard.getShape())) {
            System.out.println("DEBUG: Normal move is valid.");
            return true; // Valid normal move
        }

        throw new InvalidMoveException("Invalid move. Card does not match.");
    }

    // Find a playable card from the given hand
    public Card findPlayableCard(List<Card> hand, Card lastUsedCard, boolean isInitialTurn) {
        return hand.stream()
                .filter(card ->
                {
                    try {
                        return isCardPlayable(card, lastUsedCard, isInitialTurn);
                    } catch (InvalidMoveException e) {
                        System.out.println("DEBUG: Skipping card " + card + " due to: " + e.getMessage());
                        return false; // Ignore invalid moves during this check
                    }
                })
                .findFirst() // Return the first playable card
                .orElse(null); // Return null if no playable card is found
    }

    // Week 7: Functional Interfaces, Lambdas, Method References
    // Add to the list if the card is playable, return the list of playable cards
    public List<Card> getPlayableCards(List<Card> hand, Card lastUsedCard, boolean isInitialTurn) {
        return hand.stream()
                .filter(card -> {
                    try {
                        return isCardPlayable(card, lastUsedCard, isInitialTurn);
                    } catch (InvalidMoveException e) {
                        return false; // Ignore invalid cards
                    }
                })
                .toList();
    }


    public void playCardWithoutEffect(Card card) {
        if (card != null) {
            usedCards.add(card); // Add the card to the usedCards list
            lastUsedCard = card; // Update the last used card
        }
    }

    // Add the getPunishmentValue method here
    public int getPunishmentValue(Card card) {
        if (card instanceof AttackCard) {
            return ((AttackCard) card).getPunishment();
        } else if (card instanceof AceCard) {
            return 3;
        } else if (card instanceof JokerCard) {
            return 5;
        }
        return 0; // Default: no punishment
    }

}
