package terminal_mode;

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
        this.participants = new ArrayList<>(List.of(participants));
        this.currentPlayerIndex = INITIAL_PLAYER_INDEX;
        this.isInitialTurn = false; // Set to false since initial card is placed before first turn
    }

    /**
     * Executes the next turn in the game.
     * 
     * @param gameState The game state
     * @param gameRule The play rules manager
     * @return true if the game should continue, false if a winner or loser was found
     */
    public boolean executeNextTurn(final GameState gameState, final PlayRule gameRule) {
        if (participants.isEmpty()) {
            return false;
        }

        // Check if game is already over before executing turn
        if (gameState.isAIWinner() || gameState.isPlayerWinner() || 
            gameState.isAILoser() || gameState.isPlayerLoser()) {
            return false;
        }

        GameParticipantable currentPlayer = participants.get(currentPlayerIndex);
        
        // Execute the turn
        currentPlayer.takeTurn(gameState, gameRule, isInitialTurn);
        
        // After first turn, it's no longer initial
        isInitialTurn = false;
        
        // Move to next player
        currentPlayerIndex = (currentPlayerIndex + NEXT_PLAYER_INCREMENT) % participants.size();
        
        // Check if game is over after the turn (winner or loser)
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
}
