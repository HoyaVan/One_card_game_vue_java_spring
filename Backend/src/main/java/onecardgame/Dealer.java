package onecardgame;

import java.util.ArrayList;
import java.util.List;

/**
 * Acts as the dealer for the One Card game.
 * Manages turn order, game flow, and coordinates between participants.
 */
public class Dealer {
    private static final int INITIAL_PLAYER_INDEX = 0;
    private static final int NEXT_PLAYER_INCREMENT = 1;

    private final List<GameParticipantable> participants;
    private int currentPlayerIndex;
    private boolean isInitialTurn;

    public Dealer(final GameParticipantable... participants) {

        if (participants == null || participants.length == 0) {
            throw new IllegalArgumentException("Dealer must have participants");
        }

        this.participants = new ArrayList<>(List.of(participants));
        this.currentPlayerIndex = INITIAL_PLAYER_INDEX;
        this.isInitialTurn = false; // Set to false since initial card is placed before first turn
    }

    /**
     * Executes the next turn in the game.
     * 
     * @param gameState The game state
     * @param gameRule  The play rules manager
     * @return true if the game should continue, false if a winner or loser was
     *         found
     */
    public boolean executeNextTurn(final GameState gameState,
            final PlayRule gameRule,
            final String action) {
        // Store last card before turn to detect if face card was played
        onecardgame.cards.Card lastCardBefore = gameState.getLastUsedCard();

        GameParticipantable currentPlayer = participants.get(currentPlayerIndex);
        currentPlayer.takeTurn(gameState, gameRule, this, isInitialTurn, action);
        isInitialTurn = false;

        // Check if a face card was just played (last card changed and is now a face
        // card)
        // When a face card is played, the current player gets one more turn
        // BUT: Face card effect does NOT apply if it's the initial card
        onecardgame.cards.Card lastCardAfter = gameState.getLastUsedCard();
        boolean shouldKeepTurn = false;

        // Check if a card was played (last card changed)
        if (lastCardAfter != null && lastCardAfter != lastCardBefore &&
                lastCardAfter instanceof onecardgame.cards.FaceCard) {
            // Face card was played - current player gets one more turn
            shouldKeepTurn = true;
        }

        // Only advance to next player if turn should not be kept
        if (!shouldKeepTurn) {
            currentPlayerIndex = (currentPlayerIndex + NEXT_PLAYER_INCREMENT) % participants.size();
        }

        return !gameState.isAIWinner() && !gameState.isPlayerWinner() &&
                !gameState.isAILoser() && !gameState.isPlayerLoser();
    }

    /**
     * Gets the current player index.
     * 
     * @return The current player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Checks if this is the initial turn.
     * 
     * @return true if this is the initial turn, false otherwise
     */
    public boolean isInitialTurn() {
        return isInitialTurn;
    }

    /**
     * Sets the initial turn flag.
     * Used after game setup to mark the first player turn as initial.
     * 
     * @param isInitialTurn true if this is the initial turn, false otherwise
     */
    public void setInitialTurn(boolean isInitialTurn) {
        this.isInitialTurn = isInitialTurn;
    }

    /**
     * Advances to the next player without executing a turn.
     * Used when player explicitly ends their turn.
     */
    public void advanceToNextPlayer() {
        isInitialTurn = false;
        currentPlayerIndex = (currentPlayerIndex + NEXT_PLAYER_INCREMENT) % participants.size();
    }

    /**
     * Gets the current player (for direct access).
     */
    public GameParticipantable getCurrentPlayer() {
        return participants.get(currentPlayerIndex);
    }

    /**
     * Draws a single card and adds it to the specified hand.
     * Handles reshuffling automatically if deck is empty.
     * 
     * @param gameState The game state
     * @param gameRule  The play rules manager
     * @param hand      The hand to add the card to
     * @return true if card was drawn successfully, false if deck is empty and
     *         cannot reshuffle
     */
    public boolean drawCard(final GameState gameState,
            final PlayRule gameRule,
            final List<onecardgame.cards.Card> hand) {
        try {
            hand.add(gameState.drawFromDeck(gameRule));
            return true;
        } catch (IllegalStateException e) {
            return false; // Deck empty and cannot reshuffle
        }
    }

    /**
     * Draws multiple cards and adds them to the specified hand.
     * Handles reshuffling automatically when deck runs out.
     * Draws available cards first, then reshuffles and draws remainder.
     * 
     * @param gameState     The game state
     * @param gameRule      The play rules manager
     * @param hand          The hand to add cards to
     * @param numberOfCards Number of cards to draw
     * @return Number of cards actually drawn (may be less than requested if deck
     *         cannot be reshuffled)
     */
    public int drawCards(final GameState gameState,
            final PlayRule gameRule,
            final List<onecardgame.cards.Card> hand,
            final int numberOfCards) {
        int cardsDrawn = 0;
        int cardsRemaining = numberOfCards;

        // Draw cards in batches: first from current deck, then reshuffle and draw
        // remainder
        while (cardsDrawn < numberOfCards) {
            int cardsAvailableInDeck = gameState.getDeckSize();

            // If deck has cards, draw as many as possible (up to what's needed)
            if (cardsAvailableInDeck > 0) {
                int cardsToDrawNow = Math.min(cardsRemaining, cardsAvailableInDeck);
                for (int i = 0; i < cardsToDrawNow; i++) {
                    if (drawCard(gameState, gameRule, hand)) {
                        cardsDrawn++;
                        cardsRemaining--;
                    } else {
                        // Cannot draw more cards
                        return cardsDrawn;
                    }
                }
            }

            // If still need more cards, reshuffle and continue
            if (cardsRemaining > 0 && gameState.getDeckSize() < 1) {
                try {
                    gameState.reshuffleDeckIfNeeded(gameRule);
                } catch (IllegalStateException e) {
                    // Cannot reshuffle - no more cards available
                    return cardsDrawn;
                }
            } else if (cardsRemaining > 0 && gameState.getDeckSize() == 0) {
                // No more cards available even after reshuffling attempt
                return cardsDrawn;
            }
        }

        return cardsDrawn;
    }
}
