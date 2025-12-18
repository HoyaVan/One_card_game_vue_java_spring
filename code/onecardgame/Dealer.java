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
        this.participants = new ArrayList<>(List.of(participants));
        this.currentPlayerIndex = INITIAL_PLAYER_INDEX;
        this.isInitialTurn = true;
    }

    /**
     * Executes the next turn in the game.
     * 
     * @param gameState The game state
     * @param gameRule The play rules manager
     * @return true if the game should continue, false if a winner was found
     */
    public boolean executeNextTurn(final GameState gameState, final PlayRule gameRule) {
        if (participants.isEmpty()) {
            return false;
        }

        GameParticipantable currentPlayer = participants.get(currentPlayerIndex);
        
        // Execute the turn
        currentPlayer.takeTurn(gameState, gameRule, isInitialTurn);
        
        // After first turn, it's no longer initial
        isInitialTurn = false;
        
        // Move to next player
        currentPlayerIndex = (currentPlayerIndex + NEXT_PLAYER_INCREMENT) % participants.size();
        
        // Check if game is over
        return !gameState.isAIWinner() && !gameState.isPlayerWinner();
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
