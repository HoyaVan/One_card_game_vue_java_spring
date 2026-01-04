package com.onecardgametest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.OneCardGame;
import onecardgame.PlayRule;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

@DisplayName("OneCardGame Card Rules Tests - Card matching, special cards, rules")
class OneCardGameCardRulesTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }

    @Test
    @DisplayName("Cards with same rank should match")
    void testCardMatching_SameRank() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card card1 = CardFactory.createCard(5, "RED");
        Card card2 = CardFactory.createCard(5, "BLACK");
        assertTrue(rule.matches(card1, card2), "Same rank should match");
    }

    @Test
    @DisplayName("Cards with same shape should match")
    void testCardMatching_SameShape() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card card1 = CardFactory.createCard(3, "RED");
        Card card2 = CardFactory.createCard(7, "RED");
        assertTrue(rule.matches(card1, card2), "Same shape should match");
    }

    @Test
    @DisplayName("Joker should match any card")
    void testCardMatching_JokerMatchesAny() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        Card anyCard = CardFactory.createCard(5, "RED");
        assertTrue(rule.matches(joker, anyCard), "Joker should match any card");
        assertTrue(rule.matches(anyCard, joker), "Any card should match joker");
    }

    @Test
    @DisplayName("Cards with different rank and shape should not match")
    void testCardMatching_DifferentRankAndShape() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card card1 = CardFactory.createCard(3, "RED");
        Card card2 = CardFactory.createCard(7, "BLACK");
        assertFalse(rule.matches(card1, card2), "Different rank and shape should not match");
    }

    @Test
    @DisplayName("NumTwo card should be attack card")
    void testSpecialCards_NumTwoIsAttack() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card numTwo = CardFactory.createCard(Card.TWO_NUMBER, "RED");
        assertTrue(rule.isAttackCard(numTwo), "NumTwo should be attack card");
    }

    @Test
    @DisplayName("Ace card should be attack card")
    void testSpecialCards_AceIsAttack() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card ace = CardFactory.createCard(Card.ACE_NUMBER, "RED");
        assertTrue(rule.isAttackCard(ace), "Ace should be attack card");
    }

    @Test
    @DisplayName("Joker card should be attack card")
    void testSpecialCards_JokerIsAttack() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        assertTrue(rule.isAttackCard(joker), "Joker should be attack card");
    }

    @Test
    @DisplayName("NumSeven card should allow shape change")
    void testSpecialCards_NumSevenShapeChange() {
        game.setup();
        Card numSeven = CardFactory.createCard(7, "RED");
        assertTrue(numSeven instanceof onecardgame.cards.NumSevenCard, "Should be NumSevenCard");
    }

    @Test
    @DisplayName("Face cards (Jack, Queen, King) should allow additional play")
    void testSpecialCards_FaceCardsAllowAdditionalPlay() {
        game.setup();
        Card jack = CardFactory.createCard(Card.JACK_NUMBER, "RED");
        Card queen = CardFactory.createCard(Card.QUEEN_NUMBER, "RED");
        Card king = CardFactory.createCard(Card.KING_NUMBER, "RED");
        assertTrue(jack instanceof onecardgame.cards.FaceCard);
        assertTrue(queen instanceof onecardgame.cards.FaceCard);
        assertTrue(king instanceof onecardgame.cards.FaceCard);
    }

    @Test
    @DisplayName("NumTwo should have punishment of 2")
    void testSpecialCards_NumTwoPunishment() {
        Card numTwo = CardFactory.createCard(Card.TWO_NUMBER, "RED");
        assertTrue(numTwo instanceof onecardgame.cards.NumTwoCard);
        onecardgame.cards.NumTwoCard twoCard = (onecardgame.cards.NumTwoCard) numTwo;
        assertEquals(2, twoCard.getPunishment(), "NumTwo should punish with 2 draws");
    }

    @Test
    @DisplayName("Joker should have punishment of 5")
    void testSpecialCards_JokerPunishment() {
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        assertTrue(joker instanceof onecardgame.cards.JokerCard);
        onecardgame.cards.JokerCard jokerCard = (onecardgame.cards.JokerCard) joker;
        assertEquals(5, jokerCard.getPunishment(), "Joker should punish with 5 draws");
    }

    @Test
    @DisplayName("Accumulated draws should reset after drawing")
    void testAccumulatedDraws_ResetAfterDraw() {
        game.setup();
        PlayRule rule = game.getGameRule();
        // Set accumulated draws
        rule.playCard(CardFactory.createCard(Card.TWO_NUMBER, "RED"), false);
        assertTrue(rule.getAccumulatedDraws() > 0, "Should have accumulated draws");
        rule.resetAccumulatedDraws();
        assertEquals(0, rule.getAccumulatedDraws(), "Should reset to 0");
    }

    @Test
    @DisplayName("Multiple attack cards should accumulate draws")
    void testAccumulatedDraws_MultipleAttacks() {
        game.setup();
        PlayRule rule = game.getGameRule();
        Card lastCard = CardFactory.createCard(5, "RED");
        rule.playCard(lastCard, false);
        
        Card attack1 = CardFactory.createCard(Card.TWO_NUMBER, "RED");
        rule.playCard(attack1, false);
        int firstAccumulation = rule.getAccumulatedDraws();
        
        Card attack2 = CardFactory.createCard(Card.TWO_NUMBER, "BLACK");
        rule.playCard(attack2, false);
        assertTrue(rule.getAccumulatedDraws() > firstAccumulation, "Should accumulate more");
    }

    @Test
    @DisplayName("Game should handle all special card types in sequence")
    void testComprehensive_AllSpecialCards() {
        game.setup();
        
        // Test each special card type exists or can be created
        Card numTwo = CardFactory.createCard(Card.TWO_NUMBER, "RED");
        Card ace = CardFactory.createCard(Card.ACE_NUMBER, "RED");
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        Card numSeven = CardFactory.createCard(7, "RED");
        Card jack = CardFactory.createCard(Card.JACK_NUMBER, "RED");
        
        assertNotNull(numTwo);
        assertNotNull(ace);
        assertNotNull(joker);
        assertNotNull(numSeven);
        assertNotNull(jack);
    }
}

