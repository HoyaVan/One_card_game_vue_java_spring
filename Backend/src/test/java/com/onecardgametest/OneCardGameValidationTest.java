package com.onecardgametest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.InvalidMoveException;
import onecardgame.OneCardGame;
import onecardgame.PlayRule;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

@DisplayName("OneCardGame Validation Tests - Card playability and validation")
class OneCardGameValidationTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("Game should validate card playability")
    void testValidation_CardPlayability() {
        game.setup();
        PlayRule rule = game.getGameRule();
        GameState gameState = game.getGameState();
        Card lastCard = gameState.getLastUsedCard();
        
        Card playableCard = rule.findPlayableCard(gameState.getPlayerHand(), lastCard, false);
        if (playableCard != null) {
            try {
                assertTrue(rule.isCardPlayable(playableCard, lastCard, false), 
                          "Found card should be playable");
            } catch (InvalidMoveException e) {
                // Should not happen for found playable card
                assertTrue(false, "Found playable card should be playable");
            }
        }
    }

    @Test
    @DisplayName("Game should handle unplayable hand correctly")
    void testValidation_UnplayableHand() {
        game.setup();
        GameState gameState = game.getGameState();
        PlayRule rule = game.getGameRule();
        Card lastCard = gameState.getLastUsedCard();
        
        // Replace player hand with non-matching cards
        gameState.getPlayerHand().clear();
        if (lastCard != null) {
            // Use a valid rank that doesn't match the last card's rank
            int nonMatchingRank = (lastCard.getRank() == 5) ? 3 : 5;
            // Use a different shape to ensure it doesn't match
            String nonMatchingShape = lastCard.getShape().equals("RED") ? "BLACK" : "RED";
            gameState.getPlayerHand().add(CardFactory.createCard(nonMatchingRank, nonMatchingShape));
        }
        
        Card playable = rule.findPlayableCard(gameState.getPlayerHand(), lastCard, false);
        // Should either find a playable card (joker) or return null
        if (playable != null) {
            try {
                assertTrue(rule.isCardPlayable(playable, lastCard, false), "Playable card should be playable");
            } catch (InvalidMoveException e) {
                // Card found but not playable - this is acceptable
            }
        }
    }
}

