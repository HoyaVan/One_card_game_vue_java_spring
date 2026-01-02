package com;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.OneCardGame;

@DisplayName("OneCardGame Basic Tests - Initialization and Setup")
class OneCardGameBasicTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("Game should initialize with all components")
    void testGameInitialization() {
        assertNotNull(game.getGameState(), "GameState should not be null");
        assertNotNull(game.getGameRule(), "PlayRule should not be null");
        assertNotNull(game.getDealer(), "Dealer should not be null");
    }

    @Test
    @DisplayName("Game should not be over after initialization")
    void testGameNotOverAfterInit() {
        assertFalse(game.isGameOver(), "Game should not be over after initialization");
    }

    @Test
    @DisplayName("setup should initialize game properly")
    void testSetup() {
        game.setup();
        GameState gameState = game.getGameState();
        
        assertNotNull(gameState.getLastUsedCard(), 
                "Initial card should be set after setup");
        assertEquals(7, gameState.getPlayerHand().size(), 
                "Player should have 7 cards after setup");
        assertEquals(7, gameState.getAiHand().size(), 
                "AI should have 7 cards after setup");
    }

    @Test
    @DisplayName("GameState should be accessible")
    void testGetGameState() {
        assertNotNull(game.getGameState(), "GameState should be accessible");
    }

    @Test
    @DisplayName("PlayRule should be accessible")
    void testGetGameRule() {
        assertNotNull(game.getGameRule(), "PlayRule should be accessible");
    }

    @Test
    @DisplayName("Dealer should be accessible")
    void testGetDealer() {
        assertNotNull(game.getDealer(), "Dealer should be accessible");
    }

    @Test
    @DisplayName("setup should set initial card on table")
    void testSetup_SetsInitialCard() {
        game.setup();
        assertNotNull(game.getGameState().getLastUsedCard(), "Initial card should be set");
    }

    @Test
    @DisplayName("setup should deal exactly 7 cards to each player")
    void testSetup_DealsCorrectNumberOfCards() {
        game.setup();
        GameState gameState = game.getGameState();
        assertEquals(7, gameState.getPlayerHand().size(), "Player should have 7 cards");
        assertEquals(7, gameState.getAiHand().size(), "AI should have 7 cards");
    }

    @Test
    @DisplayName("setup should reduce deck size by 15 cards (1 initial + 7*2)")
    void testSetup_ReducesDeckSize() {
        GameState gameState = game.getGameState();
        int initialDeckSize = gameState.getDeckSize();
        game.setup();
        int finalDeckSize = gameState.getDeckSize();
        assertEquals(15, initialDeckSize - finalDeckSize, "Deck should lose 15 cards");
    }

    @Test
    @DisplayName("setup can be called multiple times without error")
    void testSetup_MultipleCalls() {
        game.setup();
        game.setup(); // Should not throw exception
        assertNotNull(game.getGameState().getLastUsedCard());
    }

    @Test
    @DisplayName("GameState should have non-null hands after initialization")
    void testGameState_HandsNotNull() {
        GameState gameState = game.getGameState();
        assertNotNull(gameState.getPlayerHand(), "Player hand should not be null");
        assertNotNull(gameState.getAiHand(), "AI hand should not be null");
    }

    @Test
    @DisplayName("Dealer should start with player index 0")
    void testDealer_InitialPlayerIndex() {
        assertEquals(0, game.getDealer().getCurrentPlayerIndex(), "Dealer should start with player");
    }

    @Test
    @DisplayName("PlayRule should initialize with zero accumulated draws")
    void testPlayRule_InitialAccumulatedDraws() {
        assertEquals(0, game.getGameRule().getAccumulatedDraws(), "Accumulated draws should start at 0");
    }

    @Test
    @DisplayName("Game should handle isGameOver check before and after setup")
    void testIsGameOver_BeforeAndAfterSetup() {
        assertFalse(game.isGameOver(), "Should not be over before setup");
        game.setup();
        // After setup, game should not be over unless someone wins/loses
        assertFalse(game.isGameOver() || 
                   game.getGameState().getPlayerHand().isEmpty() ||
                   game.getGameState().getAiHand().isEmpty(),
                   "Should not be over immediately after setup");
    }

    @Test
    @DisplayName("Game should handle getters returning non-null values")
    void testGetters_NonNullValues() {
        assertNotNull(game.getGameState());
        assertNotNull(game.getGameRule());
        assertNotNull(game.getDealer());
        
        game.setup();
        assertNotNull(game.getGameState().getLastUsedCard());
        assertNotNull(game.getGameState().getPlayerHand());
        assertNotNull(game.getGameState().getAiHand());
    }
}

