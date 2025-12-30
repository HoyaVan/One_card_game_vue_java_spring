package onecardgame;

import onecardgame.cards.Card;
import java.util.List;

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
    String message
) {
    public record CardInfo(
        int index,
        String displayName,
        String rank,
        String shape
    ) {
        public static CardInfo fromCard(Card card, int index) {
            return new CardInfo(
                index,
                card.toString(),
                String.valueOf(card.getRank()),
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
        
        return new GameStateDTO(
            playerHandInfo,
            gameState.getAiHand().size(),
            lastCardInfo,
            gameState.getDeckSize(),
            gameRule.getAccumulatedDraws(),
            dealer.getCurrentPlayerIndex() == 0,
            gameState.isAIWinner() || gameState.isPlayerWinner() || 
            gameState.isAILoser() || gameState.isPlayerLoser(),
            winner,
            ""
        );
    }
}

