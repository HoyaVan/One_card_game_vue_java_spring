package com.onecardgametest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.OneCardGame;

@DisplayName("OneCardGame Deck Tests - Deck operations")
class OneCardGameDeckTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("Deck should have correct initial size")
    void testDeck_InitialSize() {
        GameState gameState = game.getGameState();
        assertEquals(54, gameState.getDeckSize(), "Deck should have 54 cards (52 + 2 jokers)");
    }

    @Test
    @DisplayName("Drawing card should reduce deck size")
    void testDeck_DrawingReducesSize() {
        GameState gameState = game.getGameState();
        int initialSize = gameState.getDeckSize();
        gameState.drawFromDeck();
        assertEquals(initialSize - 1, gameState.getDeckSize(), "Deck should decrease by 1");
    }

    @Test
    @DisplayName("Deck should throw exception when empty")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testDeck_EmptyDeckThrowsException() {
        GameState gameState = game.getGameState();
        // Draw all cards (with safety limit to prevent infinite loop)
        int maxIterations = 60; // Deck has 54 cards max, so 60 is safe
        int iterations = 0;
        while (gameState.getDeckSize() > 0 && iterations < maxIterations) {
            gameState.drawFromDeck();
            iterations++;
        }
        assertThrows(IllegalStateException.class, () -> {
            gameState.drawFromDeck();
        });
    }

    @Test
    @DisplayName("Game should maintain deck size consistency after multiple draws")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testDeckConsistency_MultipleDraws() {
        game.setup();
        GameState gameState = game.getGameState();
        int initialDeckSize = gameState.getDeckSize();
        
        for (int i = 0; i < 5 && gameState.getDeckSize() > 0 && !game.isGameOver(); i++) {
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
        }
        
        assertTrue(gameState.getDeckSize() <= initialDeckSize, "Deck should not grow");
    }
}

