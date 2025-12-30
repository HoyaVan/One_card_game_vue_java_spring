import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import onecardgame.AIPlayer;
import onecardgame.Dealer;
import onecardgame.GameParticipantable;
import onecardgame.GameState;
import onecardgame.PlayRule;
import onecardgame.Player;

class DealerTest {

    private Dealer dealer;
    private GameState gameState;
    private PlayRule playRule;
    private GameParticipantable player;
    private GameParticipantable ai;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        playRule = new PlayRule();
        // Provide enough input for multiple Player turns (draw input "0" is safest)
        player = new Player(new Scanner("0\n0\n0\n0\n0\n0\n0\n0\n0\n0\n"));
        ai = new AIPlayer();
        dealer = new Dealer(player, ai);
    }

    @Test
    void testDealerInitialization() {
        assertNotNull(dealer, "Dealer should not be null after initialization");
        assertEquals(0, dealer.getCurrentPlayerIndex(), "Initial player index should be 0");
        assertTrue(dealer.isInitialTurn(), "Should be initial turn at start");
    }

    @Test
    void testDealerInitializationWithMultipleParticipants() {
        GameParticipantable player1 = new Player(new Scanner("0\n0\n0\n"));
        GameParticipantable player2 = new Player(new Scanner("0\n0\n0\n"));
        GameParticipantable ai1 = new AIPlayer();
        
        Dealer multiDealer = new Dealer(player1, player2, ai1);
        assertNotNull(multiDealer, "Dealer should not be null");
        assertEquals(0, multiDealer.getCurrentPlayerIndex(), "Initial player index should be 0");
    }

    @Test
    void testExecuteNextTurnRotatesPlayers() {
        int initialIndex = dealer.getCurrentPlayerIndex();
        
        // Execute first turn
        dealer.executeNextTurn(gameState, playRule);
        int secondIndex = dealer.getCurrentPlayerIndex();
        
        // Should have moved to next player
        assertNotEquals(initialIndex, secondIndex, "Player index should change after turn");
        assertEquals(1, secondIndex, "Should be second player (index 1)");
    }

    @Test
    void testExecuteNextTurnWrapsAround() {
        // Execute turns to go through all players
        dealer.executeNextTurn(gameState, playRule); // Player 0 -> Player 1
        dealer.executeNextTurn(gameState, playRule); // Player 1 -> Player 0 (wraps)
        
        assertEquals(0, dealer.getCurrentPlayerIndex(), "Should wrap around to first player");
    }

    @Test
     void testIsInitialTurnBecomesFalseAfterFirstTurn() {
        assertTrue(dealer.isInitialTurn(), "Should be initial turn at start");
        
        dealer.executeNextTurn(gameState, playRule);
        
        assertFalse(dealer.isInitialTurn(), "Should not be initial turn after first turn");
    }

    @Test
    void testExecuteNextTurnReturnsTrueWhenGameContinues() {
        // Game should continue if no winner
        boolean shouldContinue = dealer.executeNextTurn(gameState, playRule);
        
        assertTrue(shouldContinue, "Game should continue when no winner");
    }

    @Test
    void testExecuteNextTurnReturnsFalseWhenPlayerWins() {
        // Clear player hand to simulate win
        gameState.getPlayerHand().clear();
        
        boolean shouldContinue = dealer.executeNextTurn(gameState, playRule);
        
        assertFalse(shouldContinue, "Game should end when player wins");
    }

    @Test
    void testExecuteNextTurnReturnsFalseWhenAIWins() {
        // Clear AI hand to simulate win
        gameState.getAiHand().clear();
        
        boolean shouldContinue = dealer.executeNextTurn(gameState, playRule);
        
        assertFalse(shouldContinue, "Game should end when AI wins");
    }

    @Test
    void testGetCurrentPlayerIndex() {
        assertEquals(0, dealer.getCurrentPlayerIndex(), "Initial index should be 0");
        
        dealer.executeNextTurn(gameState, playRule);
        assertEquals(1, dealer.getCurrentPlayerIndex(), "Index should be 1 after one turn");
    }

    @Test
    void testExecuteNextTurnWithEmptyParticipants() {
        Dealer emptyDealer = new Dealer();
        boolean shouldContinue = emptyDealer.executeNextTurn(gameState, playRule);
        
        assertFalse(shouldContinue, "Should return false with no participants");
    }

    @Test
    void testMultipleTurnsMaintainState() {
        // Execute several turns
        for (int i = 0; i < 5; i++) {
            dealer.executeNextTurn(gameState, playRule);
        }
        
        // After 5 turns with 2 players, should be at index 1 (5 % 2 = 1)
        assertEquals(1, dealer.getCurrentPlayerIndex(), "Should maintain correct player index after multiple turns");
        assertFalse(dealer.isInitialTurn(), "Should not be initial turn after multiple turns");
    }
}
