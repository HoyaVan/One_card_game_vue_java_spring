package com;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.Dealer;
import onecardgame.GameState;
import onecardgame.OneCardGame;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

@DisplayName("OneCardGame Turn Tests - endTurn, step, turn management")
class OneCardGameTurnTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("endTurn should return false when game is over")
    void testEndTurn_GameOver() {
        game.setup();
        GameState gameState = game.getGameState();
        
        // Make game over
        gameState.getPlayerHand().clear();
        
        assertFalse(game.endTurn(), 
                "endTurn should return false when game is over");
    }

    @Test
    @DisplayName("endTurn should throw exception when not player's turn")
    void testEndTurn_NotPlayerTurn() {
        game.setup();
        // Manually set current player to AI
        game.getDealer().advanceToNextPlayer();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            game.endTurn();
        }, "Should throw IllegalStateException when not player's turn");
        
        assertNotNull(exception);
    }

    @Test
    @DisplayName("endTurn should advance to AI after player's turn")
    void testEndTurn_AdvancesToAI() {
        game.setup();
        game.endTurn();
        assertEquals(1, game.getDealer().getCurrentPlayerIndex(), "Should advance to AI");
    }

    @Test
    @DisplayName("endTurn should return true when game continues")
    void testEndTurn_ReturnsTrueWhenGameContinues() {
        game.setup();
        boolean result = game.endTurn();
        assertTrue(result || game.isGameOver(), "Should return true if game continues");
    }

    @Test
    @DisplayName("endTurn should return false when game is over")
    void testEndTurn_ReturnsFalseWhenGameOver() {
        game.setup();
        GameState gameState = game.getGameState();
        gameState.getPlayerHand().clear();
        assertFalse(game.endTurn(), "Should return false when game over");
    }

    @Test
    @DisplayName("endTurn should throw exception when called before setup")
    void testEndTurn_BeforeSetup() {
        assertThrows(IllegalStateException.class, () -> {
            game.endTurn();
        });
    }

    @Test
    @DisplayName("endTurn should handle player with no cards")
    void testEndTurn_PlayerNoCards() {
        game.setup();
        GameState gameState = game.getGameState();
        gameState.getPlayerHand().clear();
        assertFalse(game.endTurn(), "Should return false when player has no cards");
    }

    @Test
    @DisplayName("endTurn should handle multiple calls")
    void testEndTurn_MultipleCalls() {
        game.setup();
        // First endTurn should work
        boolean result1 = game.endTurn();
        assertTrue(result1 || game.isGameOver(), "First endTurn should work");
        
        // Second endTurn should throw exception (not player's turn)
        if (!game.isGameOver()) {
            assertThrows(IllegalStateException.class, () -> {
                game.endTurn();
            });
        }
    }

    @Test
    @DisplayName("endTurn should maintain state after endTurn when game continues")
    void testEndTurn_StateMaintenance() {
        game.setup();
        GameState gameState = game.getGameState();
        
        game.endTurn();
        
        if (!game.isGameOver()) {
            assertNotNull(gameState.getLastUsedCard(), "Last card should still exist");
        }
    }

    @Test
    @DisplayName("step should return false when game is over")
    void testStep_GameOver() {
        game.setup();
        GameState gameState = game.getGameState();
        
        // Make game over
        gameState.getPlayerHand().clear();
        
        assertFalse(game.step(), "step should return false when game is over");
    }

    @Test
    @DisplayName("step should return false when game is over")
    void testStep_ReturnsFalseWhenGameOver() {
        game.setup();
        GameState gameState = game.getGameState();
        gameState.getPlayerHand().clear();
        assertFalse(game.step(), "Should return false when game over");
    }

    @Test
    @DisplayName("step should return true when game continues")
    void testStep_ReturnsTrueWhenGameContinues() {
        game.setup();
        // Advance to AI turn
        game.getDealer().advanceToNextPlayer();
        boolean result = game.step();
        assertTrue(result || game.isGameOver(), "Should return true if game continues");
    }

    @Test
    @DisplayName("step should not execute when it's player's turn")
    void testStep_DoesNotExecuteOnPlayerTurn() {
        game.setup();
        int initialDeckSize = game.getGameState().getDeckSize();
        game.step(); // Should not execute AI turn
        assertEquals(initialDeckSize, game.getGameState().getDeckSize(), "Deck should not change");
    }

    @Test
    @DisplayName("step should handle AI with no playable cards")
    void testStep_AINoPlayableCards() {
        game.setup();
        GameState gameState = game.getGameState();
        // Clear AI hand and add non-matching cards
        gameState.getAiHand().clear();
        Card lastCard = gameState.getLastUsedCard();
        if (lastCard != null) {
            // Add cards that don't match (different rank and different shape)
            String nonMatchingShape = lastCard.getShape().equals("RED") ? "BLACK" : "RED";
            // Use a valid rank that doesn't match the last card's rank
            int nonMatchingRank = (lastCard.getRank() == 5) ? 3 : 5;
            gameState.getAiHand().add(CardFactory.createCard(nonMatchingRank, nonMatchingShape));
        }
        game.getDealer().advanceToNextPlayer();
        boolean result = game.step();
        assertTrue(result || game.isGameOver(), "Step should handle AI with no playable cards");
    }

    @Test
    @DisplayName("step should handle multiple calls")
    void testStep_MultipleCalls() {
        game.setup();
        game.getDealer().advanceToNextPlayer();
        
        for (int i = 0; i < 3 && !game.isGameOver(); i++) {
            boolean result = game.step();
            assertTrue(result || game.isGameOver(), "Step should return valid result");
        }
    }

    @Test
    @DisplayName("Dealer should advance to next player correctly")
    void testDealer_AdvanceToNextPlayer() {
        Dealer dealer = game.getDealer();
        assertEquals(0, dealer.getCurrentPlayerIndex(), "Should start at player");
        dealer.advanceToNextPlayer();
        assertEquals(1, dealer.getCurrentPlayerIndex(), "Should advance to AI");
        dealer.advanceToNextPlayer();
        assertEquals(0, dealer.getCurrentPlayerIndex(), "Should wrap to player");
    }

    @Test
    @DisplayName("Dealer should track initial turn correctly")
    void testDealer_InitialTurnTracking() {
        Dealer dealer = game.getDealer();
        assertFalse(dealer.isInitialTurn(), "Should not be initial turn after setup");
    }
}

