package com.onecardgametest;

import onecardgame.PlayRule;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the logic used by GameStateDTO to determine playable cards.
 * Specifically checks if getPlayableCards returns valid cards after a Joker
 * penalty is cleared.
 */
public class JokerFrontendLogicTest {

    @Test
    public void testGetPlayableCardsAfterJokerPenaltyCleared() {
        // Setup
        PlayRule playRule = new PlayRule();

        // Create a Joker card (Red Joker)
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, "RED");

        // Treat it as the last used card
        // Note: We don't need a GameState object for this test as getPlayableCards only
        // needs the card and hand

        // Ensure accumulated draws is 0 (penalty cleared)
        assertEquals(0, playRule.getAccumulatedDraws(), "Accumulated draws should start at 0");

        // Create a player hand with a 5 of Spades (definitely not matching Joker by
        // normal rules)
        List<Card> hand = new ArrayList<>();
        Card fiveSpades = CardFactory.createCard(5, "Spades");
        hand.add(fiveSpades);

        // Execute logic similar to GameStateDTO
        // boolean isInitialTurn = dealer.isInitialTurn(); // Assume false for mid-game
        List<Card> playableCards = playRule.getPlayableCards(hand, joker, false);

        // Assertions
        assertNotNull(playableCards, "Playable cards list should not be null");
        assertFalse(playableCards.isEmpty(), "Playable cards list should not be empty after Joker penalty is cleared");
        assertTrue(playableCards.contains(fiveSpades), "5 of Spades should be playable on Joker after penalty cleared");

        System.out.println("Test passed: 5 of Spades is considered playable on Joker when accumulatedDraws is 0.");
    }
}
