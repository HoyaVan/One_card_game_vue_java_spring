package com.onecardgametest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

@DisplayName("OneCardGame CardFactory Tests - Card creation and factory operations")
class OneCardGameCardFactoryTest {

    @Test
    @DisplayName("CardFactory should create normal cards correctly")
    void testCardFactory_NormalCard() {
        Card card = CardFactory.createCard(5, "RED");
        assertNotNull(card, "Should create card");
        assertEquals(5, card.getRank(), "Should have correct rank");
    }

    @Test
    @DisplayName("CardFactory should create special cards correctly")
    void testCardFactory_SpecialCards() {
        Card numTwo = CardFactory.createCard(Card.TWO_NUMBER, "RED");
        Card ace = CardFactory.createCard(Card.ACE_NUMBER, "RED");
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        Card numSeven = CardFactory.createCard(7, "RED");
        
        assertTrue(numTwo instanceof onecardgame.cards.NumTwoCard);
        assertTrue(ace instanceof onecardgame.cards.AceCard);
        assertTrue(joker instanceof onecardgame.cards.JokerCard);
        assertTrue(numSeven instanceof onecardgame.cards.NumSevenCard);
    }

    @Test
    @DisplayName("CardFactory should throw exception for invalid rank")
    void testCardFactory_InvalidRank() {
        assertThrows(IllegalArgumentException.class, () -> {
            CardFactory.createCard(100, "RED");
        });
    }

    @Test
    @DisplayName("CardFactory should throw exception for invalid shape")
    void testCardFactory_InvalidShape() {
        assertThrows(IllegalArgumentException.class, () -> {
            CardFactory.createCard(5, "INVALID");
        });
    }

    @Test
    @DisplayName("Game should handle all card ranks from Ace to King")
    void testCardRanks_AllRanks() {
        for (int rank = Card.ACE_NUMBER; rank <= Card.KING_NUMBER; rank++) {
            Card card = CardFactory.createCard(rank, "RED");
            assertNotNull(card, "Should create card for rank " + rank);
            assertEquals(rank, card.getRank(), "Card should have correct rank");
        }
    }

    @Test
    @DisplayName("Game should handle all card shapes")
    void testCardShapes_AllShapes() {
        String[] shapes = {"RED", "BLACK", Card.CARD_SHAPE_ANY};
        for (String shape : shapes) {
            Card card = CardFactory.createCard(5, shape);
            assertNotNull(card, "Should create card for shape " + shape);
        }
    }

    @Test
    @DisplayName("Game should handle Joker card with any shape")
    void testJokerCard_AnyShape() {
        Card joker1 = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        Card joker2 = CardFactory.createCard(Card.JOKER_NUMBER, "RED");
        assertNotNull(joker1);
        assertNotNull(joker2);
    }

    @Test
    @DisplayName("Game should handle NumSeven card shape change")
    void testNumSevenCard_ShapeChange() {
        Card numSeven = CardFactory.createCard(7, "RED");
        assertTrue(numSeven instanceof onecardgame.cards.NumSevenCard);
        onecardgame.cards.NumSevenCard sevenCard = (onecardgame.cards.NumSevenCard) numSeven;
        Card changed = sevenCard.withShape("BLACK");
        assertEquals("BLACK", changed.getShape(), "Shape should change");
    }

    @Test
    @DisplayName("Game should handle Face card additional play capability")
    void testFaceCard_AdditionalPlay() {
        Card jack = CardFactory.createCard(Card.JACK_NUMBER, "RED");
        Card queen = CardFactory.createCard(Card.QUEEN_NUMBER, "RED");
        Card king = CardFactory.createCard(Card.KING_NUMBER, "RED");
        
        assertTrue(jack instanceof onecardgame.cards.FaceCard);
        assertTrue(queen instanceof onecardgame.cards.FaceCard);
        assertTrue(king instanceof onecardgame.cards.FaceCard);
    }
}

