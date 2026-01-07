package com.onecardgametest;

import onecardgame.*;
import onecardgame.cards.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AceDefenseTest {

    private GameState gameState;
    private PlayRule playRule;

    @BeforeEach
    public void setUp() {
        playRule = new PlayRule();
        gameState = new GameState();
        // Clear hands
        gameState.getPlayerHand().clear();
        gameState.getAiHand().clear();
    }

    @Test
    public void testAceCannotDefendAgainstJokerEvenIfJokerDefended() {
        // 1. Initial Card is something neutral
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        playRule.setInitialCard(initialCard, gameState);

        // 2. AI attacks with Ace of Hearts (3 draws)
        AceCard aiAce = new AceCard(Card.CARD_SHAPE_HEARTS);
        playRule.playCard(aiAce, false);
        assertEquals(3, playRule.getAccumulatedDraws());

        // 3. Player defends with Joker
        JokerCard playerJoker = new JokerCard();
        // This is a defense (Joker on Ace)
        assertTrue(playRule.isDefenseCard(playerJoker, aiAce));
        playRule.playCard(playerJoker, false);

        // WITH THE FIX: Stacking happens or at least Joker's 5 takes over.
        System.out.println("After Joker defense: Accumulated Draws = " + playRule.getAccumulatedDraws());
        assertTrue(playRule.getAccumulatedDraws() >= 5, "Accumulated draws should be at least 5 after Joker defense");

        // 4. AI tries to play ANOTHER Ace on the Joker
        AceCard aiAce2 = new AceCard(Card.CARD_SHAPE_DIAMONDS);
        List<Card> hand = gameState.getAiHand();
        hand.add(aiAce2);

        // Now it MUST fail because 3 (Ace) < 5 or 8 (Joker target)
        try {
            boolean isPlayable = playRule.isCardPlayable(aiAce2, playerJoker, false);
            assertFalse(isPlayable, "Ace (3) should NOT be playable on Joker (draws >= 5)");
        } catch (InvalidMoveException e) {
            System.out.println("Correctly blocked Ace on Joker: " + e.getMessage());
        }
    }

    @Test
    public void testAceCannotDefendAgainstJoker() {
        // Setup scenarios
        // 1. Initial Card is something neutral
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        playRule.setInitialCard(initialCard, gameState);

        // 2. Player plays Joker (Attack 5)
        JokerCard joker = new JokerCard();
        playRule.playCard(joker, false);

        System.out.println("After Joker: Accumulated Draws = " + playRule.getAccumulatedDraws());
        assertEquals(5, playRule.getAccumulatedDraws(), "Joker should add 5 draws");

        // 3. AI Turn try to play Ace
        AceCard ace = new AceCard(Card.CARD_SHAPE_HEARTS); // Ace Hearts (Attack 3)
        List<Card> hand = gameState.getAiHand();
        hand.add(ace);

        // Check if Ace is playable
        try {
            boolean isPlayable = playRule.isCardPlayable(ace, joker, false);
            System.out.println("Is Ace playable on Joker? " + isPlayable);
            assertFalse(isPlayable, "Ace (Punishment 3) should NOT be playable on Joker (Punishment 5) when draws > 0");
        } catch (InvalidMoveException e) {
            System.out.println("Correctly caught InvalidMoveException: " + e.getMessage());
        }

        // Also verify getPlayableCards filters it out
        List<Card> playableCards = playRule.getPlayableCards(hand, joker, false);
        System.out.println("Playable cards count: " + playableCards.size());
        assertTrue(playableCards.isEmpty(), "Ace should not be in playable cards list");
    }
}
