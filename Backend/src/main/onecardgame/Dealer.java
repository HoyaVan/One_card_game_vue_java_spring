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
     * @param gameRule The play rules manager
     * @return true if the game should continue, false if a winner or loser was found
     */
    public boolean executeNextTurn(final GameState gameState, 
                                    final PlayRule gameRule, 
                                    final String action) {

        GameParticipantable currentPlayer = participants.get(currentPlayerIndex);
        currentPlayer.takeTurn(gameState, gameRule, isInitialTurn, action);
        isInitialTurn = false;
        currentPlayerIndex = (currentPlayerIndex + NEXT_PLAYER_INCREMENT) % participants.size();
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
}
