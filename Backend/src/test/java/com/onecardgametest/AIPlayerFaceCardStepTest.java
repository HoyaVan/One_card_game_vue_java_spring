package com.onecardgametest;

import onecardgame.*;
import onecardgame.cards.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AIPlayerFaceCardStepTest {

    private GameState gameState;
    private PlayRule gameRule;
    private Dealer dealer;
    private AIPlayer aiPlayer;
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        aiPlayer = new AIPlayer();
        dealer = new Dealer(player, aiPlayer); // Player is 0, AI is 1
        gameRule = new PlayRule();

        gameState = new GameState();
        // Clear hands and deck for precise testing
        gameState.getPlayerHand().clear();
        gameState.getAiHand().clear();
        gameState.getDeck().getCards().clear();

        // Set initial card so it's not the "initial turn" logic (unless we pass true)
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        gameRule.setInitialCard(initialCard, gameState);
        gameState.setLastUsedCard(initialCard);
    }

    @Test
    void testAIPlaysFaceCardAndReturnsImmediateley() {
        // Setup AI hand: Queen of Hearts (Face), Jack of Hearts (Face), 5 of Spades
        // (Normal)
        // Last card is 5 of Hearts. Queen of Hearts matches by Shape.
        Card qHearts = CardFactory.createCard(Card.QUEEN_NUMBER, Card.CARD_SHAPE_HEARTS); // 12
        Card jHearts = CardFactory.createCard(Card.JACK_NUMBER, Card.CARD_SHAPE_HEARTS); // 11
        Card sSpades = CardFactory.createCard(5, Card.CARD_SHAPE_SPADES); // 5

        gameState.getAiHand().add(qHearts);
        gameState.getAiHand().add(jHearts);
        gameState.getAiHand().add(sSpades);

        // Step 1: AI Turn
        // AI should see Q Hearts matches 5 Hearts (Shape). It should play Q Hearts.
        // It should NOT play J Hearts immediately.

        aiPlayer.takeTurn(gameState, gameRule, dealer, false, "");

        // Assert Q Hearts is played
        assertEquals(qHearts, gameState.getLastUsedCard(), "AI should have played Queen of Hearts");
        assertEquals(2, gameState.getAiHand().size(), "AI should have 2 cards left");
        assertTrue(gameState.getAiHand().contains(jHearts));
        assertTrue(gameState.getAiHand().contains(sSpades));

        // Step 2: AI Turn (Triggered again by loop/frontend)
        // AI should see J Hearts matches Q Hearts (Shape/Rank compatible). It should
        // play J Hearts.

        aiPlayer.takeTurn(gameState, gameRule, dealer, false, "");

        // Assert J Hearts is played
        assertEquals(jHearts, gameState.getLastUsedCard(), "AI should have played Jack of Hearts");
        assertEquals(1, gameState.getAiHand().size(), "AI should have 1 card left");
        assertTrue(gameState.getAiHand().contains(sSpades));

        // Step 3: AI Turn (Triggered again)
        // J Hearts is Face Card, so AI keeps turn.
        // Next card is 5 Spades. Last card is J Hearts.
        // Does 5 Spades match J Hearts? NO. Different rank (5 vs 11) and different
        // shape (Spades vs Hearts).
        // So AI should DRAW.

        // Ensure deck has cards to draw
        gameState.getDeck().getCards().add(CardFactory.createCard(8, Card.CARD_SHAPE_CLUBS));

        aiPlayer.takeTurn(gameState, gameRule, dealer, false, "");

        // Assert AI drew a card
        assertEquals(jHearts, gameState.getLastUsedCard(), "Last card should still be J Hearts");
        assertEquals(2, gameState.getAiHand().size(), "AI should have drawn a card (now 2 cards)");

        System.out.println("TEST_VERIFIED_STEP_BY_STEP");
    }

    @Test
    void testInvalidMoveBugFixed() {
        // Verify that playing non-matching card after Face Card is NOT allowed by
        // backend validation
        // This effectively tests Player.java validation logic indirectly via PlayRule

        Card kSpades = CardFactory.createCard(Card.KING_NUMBER, Card.CARD_SHAPE_SPADES);
        Card cClubs = CardFactory.createCard(6, Card.CARD_SHAPE_CLUBS);

        // Simulate K Spades on table
        gameState.setLastUsedCard(kSpades);

        // Try to validate 6 Clubs
        // Should throw InvalidMoveException
        Exception exception = assertThrows(InvalidMoveException.class, () -> {
            gameRule.isCardPlayable(cClubs, kSpades, false);
        });

        assertTrue(exception.getMessage().contains("match"), "Exception should complain about matching");

        System.out.println("TEST_VERIFIED_INVALID_MOVE");
    }
}
