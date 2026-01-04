package com.onecardgametest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.OneCardGame;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

@DisplayName("OneCardGame State Tests - GameState operations")
class OneCardGameStateTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("GameState should track last used card correctly")
    void testGameState_LastUsedCard() {
        game.setup();
        Card initialCard = game.getGameState().getLastUsedCard();
        assertNotNull(initialCard, "Should have last used card after setup");
    }

    @Test
    @DisplayName("GameState should allow setting last used card")
    void testGameState_SetLastUsedCard() {
        GameState gameState = game.getGameState();
        Card testCard = CardFactory.createCard(5, "RED");
        gameState.setLastUsedCard(testCard);
        assertEquals(testCard, gameState.getLastUsedCard(), "Should set last used card");
    }

    @Test
    @DisplayName("GameState should provide access to hands")
    void testGameState_HandAccess() {
        GameState gameState = game.getGameState();
        assertNotNull(gameState.getPlayerHand(), "Should provide player hand");
        assertNotNull(gameState.getAiHand(), "Should provide AI hand");
    }

    @Test
    @DisplayName("GameState should remove cards from hands correctly")
    void testGameState_RemoveFromHand() {
        game.setup();
        GameState gameState = game.getGameState();
        int initialSize = gameState.getPlayerHand().size();
        if (initialSize > 0) {
            gameState.removeFromPlayerHand(0);
            assertEquals(initialSize - 1, gameState.getPlayerHand().size(), "Should remove card");
        }
    }
}

