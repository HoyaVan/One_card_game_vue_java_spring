package com.onecardgametest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.OneCardGame;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

@DisplayName("OneCardGame Win/Lose Tests - Win and lose conditions")
class OneCardGameWinLoseTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("Game should be over when player wins")
    void testGameOver_PlayerWins() {
        game.setup();
        GameState gameState = game.getGameState();
        
        // Clear player hand to simulate win
        gameState.getPlayerHand().clear();
        
        assertTrue(game.isGameOver(), "Game should be over when player wins");
        assertTrue(gameState.isPlayerWinner(), "Player should be winner");
    }

    @Test
    @DisplayName("Game should be over when AI wins")
    void testGameOver_AIWins() {
        game.setup();
        GameState gameState = game.getGameState();
        
        // Clear AI hand to simulate win
        gameState.getAiHand().clear();
        
        assertTrue(game.isGameOver(), "Game should be over when AI wins");
        assertTrue(gameState.isAIWinner(), "AI should be winner");
    }

    @Test
    @DisplayName("Game should be over when player loses (too many cards)")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testGameOver_PlayerLoses() {
        game.setup();
        GameState gameState = game.getGameState();
        
        // Add cards to player hand to simulate loss (18+ cards)
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        for (int i = gameState.getPlayerHand().size(); i < 18 && iterations < maxIterations; i++) {
            Card card = CardFactory.createCard(1, "RED");
            gameState.getPlayerHand().add(card);
            iterations++;
        }
        
        assertTrue(game.isGameOver(), "Game should be over when player loses");
        assertTrue(gameState.isPlayerLoser(), "Player should be loser");
    }

    @Test
    @DisplayName("Game should be over when AI loses (too many cards)")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testGameOver_AILoses() {
        game.setup();
        GameState gameState = game.getGameState();
        
        // Add cards to AI hand to simulate loss (18+ cards)
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        for (int i = gameState.getAiHand().size(); i < 18 && iterations < maxIterations; i++) {
            Card card = CardFactory.createCard(1, "RED");
            gameState.getAiHand().add(card);
            iterations++;
        }
        
        assertTrue(game.isGameOver(), "Game should be over when AI loses");
        assertTrue(gameState.isAILoser(), "AI should be loser");
    }

    @Test
    @DisplayName("Player wins when hand is empty")
    void testWinCondition_PlayerEmptyHand() {
        game.setup();
        GameState gameState = game.getGameState();
        gameState.getPlayerHand().clear();
        assertTrue(gameState.isPlayerWinner(), "Player should win with empty hand");
        assertTrue(game.isGameOver(), "Game should be over");
    }

    @Test
    @DisplayName("AI wins when hand is empty")
    void testWinCondition_AIEmptyHand() {
        game.setup();
        GameState gameState = game.getGameState();
        gameState.getAiHand().clear();
        assertTrue(gameState.isAIWinner(), "AI should win with empty hand");
        assertTrue(game.isGameOver(), "Game should be over");
    }

    @Test
    @DisplayName("Player loses when hand has 18+ cards")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testLoseCondition_PlayerTooManyCards() {
        game.setup();
        GameState gameState = game.getGameState();
        // Add cards to reach 18 (with safety limit)
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        while (gameState.getPlayerHand().size() < 18 && iterations < maxIterations) {
            gameState.getPlayerHand().add(CardFactory.createCard(1, "RED"));
            iterations++;
        }
        assertTrue(gameState.isPlayerLoser(), "Player should lose with 18+ cards");
        assertTrue(game.isGameOver(), "Game should be over");
    }

    @Test
    @DisplayName("AI loses when hand has 18+ cards")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testLoseCondition_AITooManyCards() {
        game.setup();
        GameState gameState = game.getGameState();
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        while (gameState.getAiHand().size() < 18 && iterations < maxIterations) {
            gameState.getAiHand().add(CardFactory.createCard(1, "RED"));
            iterations++;
        }
        assertTrue(gameState.isAILoser(), "AI should lose with 18+ cards");
        assertTrue(game.isGameOver(), "Game should be over");
    }

    @Test
    @DisplayName("Player does not lose with exactly 17 cards")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testLoseCondition_Player17Cards() {
        game.setup();
        GameState gameState = game.getGameState();
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        while (gameState.getPlayerHand().size() < 17 && iterations < maxIterations) {
            gameState.getPlayerHand().add(CardFactory.createCard(1, "RED"));
            iterations++;
        }
        assertFalse(gameState.isPlayerLoser(), "Player should not lose with 17 cards");
    }

    @Test
    @DisplayName("Game should detect both players as winners simultaneously")
    void testWinCondition_BothPlayersWin() {
        game.setup();
        GameState gameState = game.getGameState();
        gameState.getPlayerHand().clear();
        gameState.getAiHand().clear();
        assertTrue(gameState.isPlayerWinner() && gameState.isAIWinner(), "Both should win");
        assertTrue(game.isGameOver(), "Game should be over");
    }

    @Test
    @DisplayName("Game should handle edge case of exactly 18 cards")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testComprehensive_Exactly18Cards() {
        game.setup();
        GameState gameState = game.getGameState();
        // Add exactly 18 cards (with safety limit)
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        while (gameState.getPlayerHand().size() < 18 && iterations < maxIterations) {
            gameState.getPlayerHand().add(CardFactory.createCard(1, "RED"));
            iterations++;
        }
        assertTrue(gameState.isPlayerLoser(), "Exactly 18 cards should cause loss");
    }

    @Test
    @DisplayName("Game should handle simultaneous win and lose conditions")
    @org.junit.jupiter.api.Timeout(10) // Prevent infinite loop
    void testComprehensive_SimultaneousWinLose() {
        game.setup();
        GameState gameState = game.getGameState();
        // Player wins, AI loses
        gameState.getPlayerHand().clear();
        int maxIterations = 20; // Safety limit
        int iterations = 0;
        while (gameState.getAiHand().size() < 18 && iterations < maxIterations) {
            gameState.getAiHand().add(CardFactory.createCard(1, "RED"));
            iterations++;
        }
        assertTrue(gameState.isPlayerWinner(), "Player should win");
        assertTrue(gameState.isAILoser(), "AI should lose");
        assertTrue(game.isGameOver(), "Game should be over");
    }
}

