package com.onecardgametest;

import onecardgame.AIPlayer;
import onecardgame.Dealer;
import onecardgame.GameParticipantable;
import onecardgame.GameState;
import onecardgame.PlayRule;
import onecardgame.Player;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;
import onecardgame.cards.JokerCard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class JokerTurnLogicTest {

    private GameState gameState;
    private PlayRule gameRule;
    private Dealer dealer;
    private AIPlayer aiPlayer;
    private Player player;

    @BeforeEach
    public void setUp() {
        gameRule = new PlayRule();
        gameState = new GameState();
        player = new Player();
        aiPlayer = new AIPlayer();
        dealer = new Dealer(player, aiPlayer);
    }

    @Test
    public void testAttackerCanPlayAnyCardAfterJokerPenalty() throws onecardgame.InvalidMoveException {
        // Setup: Player Turn
        // Player plays Joker
        dealer.setInitialTurn(false);

        // P1 Hand: Joker
        List<Card> playerHand = gameState.getPlayerHand();
        playerHand.clear();
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, "BLACK");
        playerHand.add(joker);
        // Add a "follow up" card that definitely doesn't match the Joker (if strict
        // matching)
        // But Joker is wild, so it should match.
        Card fiveSpades = CardFactory.createCard(5, Card.CARD_SHAPE_SPADES);
        playerHand.add(fiveSpades);

        // P2 (AI) Hand: Non-defense cards (e.g. 5 of Hearts, 6 of Diamonds)
        List<Card> aiHand = gameState.getAiHand();
        aiHand.clear();
        Card fiveHearts = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        aiHand.add(fiveHearts);

        // Initial Table: 5 of Clubs
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_CLUBS);
        gameRule.setInitialCard(initialCard, gameState);

        // 1. Player plays Joker (Index 0)
        gameRule.playCard(playerHand, 0);
        gameState.setLastUsedCard(gameRule.getLastUsedCard());
        // Verify accumulated draws
        assertEquals(5, gameRule.getAccumulatedDraws(), "Joker should add 5 accumulated draws");

        // Advance Turn to AI
        dealer.advanceToNextPlayer();
        assertEquals(1, dealer.getCurrentPlayerIndex(), "Turn should be AI");

        // 2. AI tries to defend but fails (no defense cards)
        aiPlayer.takeTurn(gameState, gameRule, dealer, false, null);

        // Verify AI drew cards
        assertTrue(aiHand.size() > 1, "AI should have drawn accumulated cards");
        assertEquals(0, gameRule.getAccumulatedDraws(), "Accumulated draws should be reset after AI draws");

        // Advance Turn back to Player
        dealer.advanceToNextPlayer();
        assertEquals(0, dealer.getCurrentPlayerIndex(), "Turn should be back to Player");

        // 3. Player plays 5 of Spades on Joker
        Card lastCard = gameState.getLastUsedCard();
        assertTrue(lastCard instanceof JokerCard, "Last card should be Joker");

        // Player tries to play 5 of Spades (Index 0 now, since Joker removed)
        Card cardToPlay = playerHand.get(0);
        assertEquals(5, cardToPlay.getRank());

        assertDoesNotThrow(() -> {
            gameRule.isCardPlayable(cardToPlay, lastCard, false);
        }, "5 of Spades should be playable on Joker after penalty is cleared");

        boolean playable = false;
        try {
            playable = gameRule.isCardPlayable(cardToPlay, lastCard, false);
        } catch (Exception e) {
        }

        assertTrue(playable, "isCardPlayable should return true for ANY card on Joker if not under attack");
    }
}
