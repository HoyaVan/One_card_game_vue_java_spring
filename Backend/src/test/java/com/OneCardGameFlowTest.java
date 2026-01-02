package com;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.OneCardGame;
import onecardgame.cards.Card;

@DisplayName("OneCardGame Flow Tests - Comprehensive game flow")
class OneCardGameFlowTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("Complete game flow: setup -> play -> end turn -> step")
    void testGameFlow_CompleteFlow() {
        game.setup();
        assertFalse(game.isGameOver(), "Game should not be over after setup");
        
        boolean playResult = game.processPlayerAction("0");
        assertTrue(playResult || game.isGameOver(), "Play should succeed or game over");
        
        if (!game.isGameOver()) {
            // Draw action automatically advances turn to AI, so use step() to execute AI turn
            // After AI turn, it should cycle back to player
            if (game.getDealer().getCurrentPlayerIndex() == 1) {
                boolean stepResult = game.step();
                assertTrue(stepResult || game.isGameOver(), "Step should succeed or game over");
            } else {
                // If it's still player's turn (shouldn't happen with draw), test endTurn
                boolean endResult = game.endTurn();
                assertTrue(endResult || game.isGameOver(), "End turn should succeed or game over");
            }
        }
    }

    @Test
    @DisplayName("Game should handle multiple complete turns")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testGameFlow_MultipleTurns() {
        game.setup();
        int turnCount = 0;
        while (!game.isGameOver() && turnCount < 10) {
            // Only process if it's player's turn
            if (game.getDealer().getCurrentPlayerIndex() == 0) {
                game.processPlayerAction("0");
                // Draw action automatically advances turn, so cycle back if needed
                if (!game.isGameOver() && game.getDealer().getCurrentPlayerIndex() == 1) {
                    game.step(); // Execute AI turn to cycle back to player
                }
            } else {
                // Not player's turn, break to avoid infinite loop
                break;
            }
            turnCount++;
        }
        assertTrue(turnCount > 0, "Should execute at least one turn");
    }

    @Test
    @DisplayName("Game should handle draw action followed by end turn")
    void testGameFlow_DrawThenEndTurn() {
        game.setup();
        game.processPlayerAction("0");
        if (!game.isGameOver()) {
            // After draw, turn should already be advanced, so endTurn might throw
            // This tests the state after draw
            assertTrue(game.getDealer().getCurrentPlayerIndex() == 1 || 
                      game.isGameOver(), "Turn should advance after draw");
        }
    }

    @Test
    @DisplayName("Game should maintain valid state throughout play")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testGameFlow_StateValidity() {
        game.setup();
        for (int i = 0; i < 5 && !game.isGameOver(); i++) {
            // Only process if it's player's turn
            if (game.getDealer().getCurrentPlayerIndex() == 0) {
                GameState gameState = game.getGameState();
                assertNotNull(gameState.getLastUsedCard(), "Should always have last used card");
                assertNotNull(gameState.getPlayerHand(), "Should always have player hand");
                assertNotNull(gameState.getAiHand(), "Should always have AI hand");
                game.processPlayerAction("0");
                // Draw action automatically advances turn, so cycle back if needed
                if (!game.isGameOver() && game.getDealer().getCurrentPlayerIndex() == 1) {
                    game.step(); // Execute AI turn to cycle back to player
                }
            } else {
                // Not player's turn, break to avoid infinite loop
                break;
            }
        }
    }

    @Test
    @DisplayName("Game should handle rapid successive actions")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testEdgeCase_RapidActions() {
        game.setup();
        for (int i = 0; i < 5 && !game.isGameOver(); i++) {
            // Only process if it's player's turn
            if (game.getDealer().getCurrentPlayerIndex() == 0) {
                game.processPlayerAction("0"); // Draw
                // Draw action automatically advances turn, so cycle back if needed
                if (!game.isGameOver() && game.getDealer().getCurrentPlayerIndex() == 1) {
                    game.step(); // Execute AI turn to cycle back to player
                }
            } else {
                // Not player's turn, break to avoid infinite loop
                break;
            }
        }
        assertNotNull(game.getGameState(), "GameState should still exist");
    }
    
    @Test
    @DisplayName("Game should maintain state consistency after multiple operations")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testEdgeCase_StateConsistency() {
        game.setup();
        GameState gameState = game.getGameState();
        int initialPlayerCards = gameState.getPlayerHand().size();
        int initialAiCards = gameState.getAiHand().size();
        
        // Only process if it's player's turn
        if (game.getDealer().getCurrentPlayerIndex() == 0) {
            game.processPlayerAction("0");
            // Draw action automatically advances turn, so execute AI turn to cycle back
            if (!game.isGameOver() && game.getDealer().getCurrentPlayerIndex() == 1) {
                game.step(); // Execute AI turn to cycle back to player
            }
        }
        
        // State should be consistent
        assertTrue(gameState.getPlayerHand().size() >= initialPlayerCards || 
                  gameState.getAiHand().size() != initialAiCards,
                  "State should change after actions");
    }

    @Test
    @DisplayName("Game should handle rapid setup and play sequence")
    void testRapidSequence_SetupAndPlay() {
        for (int i = 0; i < 3; i++) {
            game = new OneCardGame();
            game.setup();
            game.processPlayerAction("0");
            assertFalse(game.isGameOver() || game.getGameState().getPlayerHand().isEmpty(), 
                       "Game should be in valid state");
        }
    }
}

