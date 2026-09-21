package onecardgame;

import java.util.List;
import onecardgame.cards.Card;

/**
 * Data Transfer Object for game state - used for JSON responses to Vue frontend.
 * Contains all information needed to display the game state.
 */
public record GameStateDTO(
    List<CardInfo> playerHand,
    int aiHandSize,
    CardInfo lastUsedCard,
    int deckSize,
    int accumulatedDraws,
    boolean isPlayerTurn,
    boolean isInitialTurn,  // Whether this is the initial turn (attack rules don't apply)
    boolean isGameOver,
    String winner,
    String gameOverReason,
    String message,
    List<Integer> playableCardIndices,  // Indices of playable cards in playerHand
    List<CardInfo> usedCardPile  // All cards in the used card pile (for stacking visualization)
) {
    public record CardInfo(
        int id,
        int index,
        String rank,
        String shape,
        boolean isInitialCard  // Whether this card is the initial card (should be treated as normal card)
    ) {
        /**
         * Converts card rank number to string format matching frontend image file names.
         * - 1 -> "ace"
         * - 11 -> "jack"
         * - 12 -> "queen"
         * - 13 -> "king"
         * - -1 -> "joker"
         * - 2-10 -> number as string
         */
        private static String rankToString(int rank) {
            return switch (rank) {
                case Card.ACE_NUMBER -> "ace";
                case Card.JACK_NUMBER -> "jack";
                case Card.QUEEN_NUMBER -> "queen";
                case Card.KING_NUMBER -> "king";
                case Card.JOKER_NUMBER -> "joker";
                default -> String.valueOf(rank);
            };
        }
        
        public static CardInfo fromCard(Card card, int index, boolean isInitialCard) {
            // Generate unique ID: combination of index, rank, and shape
            // This ensures uniqueness even if multiple cards have same rank/shape
            // Using hash of rank + shape to create stable ID
            int id = index * 10000 + Math.abs((card.getRank() * 100 + card.getShape().hashCode()) % 10000);
            return new CardInfo(
                id,
                index,
                rankToString(card.getRank()),
                card.getShape(),
                isInitialCard
            );
        }
        
        // Overloaded method for backward compatibility (defaults to false)
        public static CardInfo fromCard(Card card, int index) {
            return fromCard(card, index, false);
        }
    }
    
    public static GameStateDTO fromGameState(GameState gameState, PlayRule gameRule, Dealer dealer) {
        List<Card> playerHand = gameState.getPlayerHand();
        List<CardInfo> playerHandInfo = new java.util.ArrayList<>();
        for (int i = 0; i < playerHand.size(); i++) {
            playerHandInfo.add(CardInfo.fromCard(playerHand.get(i), i));
        }
        
        // Get all cards from used card pile for stacking visualization
        // The first card (index 0) is always the initial card
        List<Card> usedCards = gameRule.getUsedCards();
        List<CardInfo> usedCardPileInfo = new java.util.ArrayList<>();
        for (int i = 0; i < usedCards.size(); i++) {
            boolean isInitialCard = (i == 0); // First card in pile is always the initial card
            usedCardPileInfo.add(CardInfo.fromCard(usedCards.get(i), i, isInitialCard));
        }
        
        // Determine if lastUsedCard is the initial card
        // The initial card is the first card in the used card pile (index 0)
        // If there's only one card in the pile, the lastUsedCard IS the initial card
        Card lastCard = gameState.getLastUsedCard();
        boolean isLastCardInitial = (lastCard != null && usedCards.size() == 1);
        
        CardInfo lastCardInfo = lastCard != null 
            ? CardInfo.fromCard(lastCard, -1, isLastCardInitial)
            : null;
        
        String winner = null;
        String gameOverReason = null;
        if (gameState.isPlayerWinner()) {
            winner = "PLAYER";
            gameOverReason = "PLAYER_EMPTY_CARDS";
        } else if (gameState.isAIWinner()) {
            winner = "AI";
            gameOverReason = "AI_EMPTY_CARDS";
        } else if (gameState.isPlayerLoser()) {
            winner = "AI";
            gameOverReason = "PLAYER_MAX_CARDS";
        } else if (gameState.isAILoser()) {
            winner = "PLAYER";
            gameOverReason = "AI_MAX_CARDS";
        }
        
        // Calculate playable card indices using backend logic
        List<Integer> playableIndices = new java.util.ArrayList<>();
        boolean isPlayerTurn = dealer.getCurrentPlayerIndex() == 0;
        boolean isInitialTurn = dealer.isInitialTurn(); // Use dealer's isInitialTurn flag
        
        if (isPlayerTurn && lastCardInfo != null) {
            Card lastUsedCard = gameState.getLastUsedCard();
            List<Card> playableCards = gameRule.getPlayableCards(playerHand, lastUsedCard, isInitialTurn);
            
            // Convert playable cards to their indices in the player hand
            for (Card playableCard : playableCards) {
                int index = playerHand.indexOf(playableCard);
                if (index >= 0) {
                    playableIndices.add(index);
                }
            }
        } else if (isPlayerTurn && isInitialTurn) {
            // If it's the initial turn, all cards are playable
            for (int i = 0; i < playerHand.size(); i++) {
                playableIndices.add(i);
            }
        }
        
        return new GameStateDTO(
            playerHandInfo,
            gameState.getAiHand().size(),
            lastCardInfo,
            gameState.getDeckSize(),
            gameRule.getAccumulatedDraws(),
            isPlayerTurn,
            isInitialTurn,
            gameState.isAIWinner() || gameState.isPlayerWinner() || 
            gameState.isAILoser() || gameState.isPlayerLoser(),
            winner,
            gameOverReason,
            "",
            playableIndices,
            usedCardPileInfo
        );
    }
}

