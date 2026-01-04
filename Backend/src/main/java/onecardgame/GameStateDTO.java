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
    boolean isGameOver,
    String winner,
    String message,
    List<Integer> playableCardIndices,  // Indices of playable cards in playerHand
    List<CardInfo> usedCardPile  // All cards in the used card pile (for stacking visualization)
) {
    public record CardInfo(
        int id,
        int index,
        String rank,
        String shape
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
        
        public static CardInfo fromCard(Card card, int index) {
            // Generate unique ID: combination of index, rank, and shape
            // This ensures uniqueness even if multiple cards have same rank/shape
            // Using hash of rank + shape to create stable ID
            int id = index * 10000 + Math.abs((card.getRank() * 100 + card.getShape().hashCode()) % 10000);
            return new CardInfo(
                id,
                index,
                rankToString(card.getRank()),
                card.getShape()
            );
        }
    }
    
    public static GameStateDTO fromGameState(GameState gameState, PlayRule gameRule, Dealer dealer) {
        List<Card> playerHand = gameState.getPlayerHand();
        List<CardInfo> playerHandInfo = new java.util.ArrayList<>();
        for (int i = 0; i < playerHand.size(); i++) {
            playerHandInfo.add(CardInfo.fromCard(playerHand.get(i), i));
        }
        
        CardInfo lastCardInfo = gameState.getLastUsedCard() != null 
            ? CardInfo.fromCard(gameState.getLastUsedCard(), -1)
            : null;
        
        // Get all cards from used card pile for stacking visualization
        List<Card> usedCards = gameRule.getUsedCards();
        List<CardInfo> usedCardPileInfo = new java.util.ArrayList<>();
        for (int i = 0; i < usedCards.size(); i++) {
            usedCardPileInfo.add(CardInfo.fromCard(usedCards.get(i), i));
        }
        
        String winner = null;
        if (gameState.isPlayerWinner()) {
            winner = "PLAYER";
        } else if (gameState.isAIWinner()) {
            winner = "AI";
        } else if (gameState.isPlayerLoser()) {
            winner = "AI";
        } else if (gameState.isAILoser()) {
            winner = "PLAYER";
        }
        
        // Calculate playable card indices using backend logic
        List<Integer> playableIndices = new java.util.ArrayList<>();
        boolean isPlayerTurn = dealer.getCurrentPlayerIndex() == 0;
        boolean isInitialTurn = gameState.getLastUsedCard() == null;
        
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
            gameState.isAIWinner() || gameState.isPlayerWinner() || 
            gameState.isAILoser() || gameState.isPlayerLoser(),
            winner,
            "",
            playableIndices,
            usedCardPileInfo
        );
    }
}

