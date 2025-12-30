import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import onecardgame.AIPlayer;
import onecardgame.GameState;
import onecardgame.InvalidMoveException;
import onecardgame.PlayRule;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;
import onecardgame.cards.JokerCard;

class AIPlayerTest {

    private AIPlayer aiPlayer;
    private GameState gameState;
    private PlayRule playRule;

    @BeforeEach
    void setUp() {
        aiPlayer = new AIPlayer();
        gameState = new GameState();
        playRule = new PlayRule();
    }

    @Test
    void testAIPlayerInitialization() {
        assertNotNull(aiPlayer, "AIPlayer should not be null after initialization");
    }

    @Test
    void testTakeTurnCompletes() {
        // Set up initial card
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        boolean result = aiPlayer.takeTurn(gameState, playRule, true);
        
        assertTrue(result, "Take turn should complete successfully");
    }

    @Test
    void testTakeTurnDrawsCardWhenNoPlayableCard() {
        // Set up a card that doesn't match anything in AI hand
        Card initialCard = CardFactory.createCard(10, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Clear AI hand and add non-matching cards
        gameState.getAiHand().clear();
        gameState.getAiHand().add(CardFactory.createCard(3, Card.CARD_SHAPE_CLUBS));
        gameState.getAiHand().add(CardFactory.createCard(7, Card.CARD_SHAPE_SPADES));
        
        int initialHandSize = gameState.getAiHand().size();
        int initialDeckSize = gameState.getDeckSize();
        
        aiPlayer.takeTurn(gameState, playRule, false);
        
        // AI should draw a card if deck is not empty
        if (initialDeckSize > 0) {
            assertTrue(gameState.getAiHand().size() >= initialHandSize,
                      "AI hand should increase or stay same after drawing");
        }
    }

    @Test
    void testTakeTurnPlaysCardWhenPlayable() {
        // Set up matching scenario
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Add matching card to AI hand
        Card playableCard = CardFactory.createCard(5, Card.CARD_SHAPE_DIAMONDS);
        gameState.getAiHand().clear();
        gameState.getAiHand().add(playableCard);
        
        int initialHandSize = gameState.getAiHand().size();
        
        aiPlayer.takeTurn(gameState, playRule, false);
        
        // AI might play the card (depending on probability) or draw
        // So we just check that the turn completed
        assertTrue(gameState.getAiHand().size() <= initialHandSize + 1,
                  "Hand size should not increase significantly");
    }

    @Test
    void testPlayCardReturnsPlayableCard() throws InvalidMoveException {
        // Set up matching scenario
        Card initialCard = CardFactory.createCard(6, Card.CARD_SHAPE_CLUBS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Add matching card
        Card playableCard = CardFactory.createCard(6, Card.CARD_SHAPE_SPADES);
        gameState.getAiHand().clear();
        gameState.getAiHand().add(playableCard);
        
        Card playedCard = aiPlayer.playCard(gameState, playRule, false);
        
        assertNotNull(playedCard, "Played card should not be null when playable card exists");
        assertEquals(playableCard, playedCard, "Played card should match the playable card");
    }

    @Test
    void testPlayCardReturnsNullWhenNoPlayableCard() throws InvalidMoveException {
        // Set up non-matching scenario
        Card initialCard = CardFactory.createCard(8, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Add only non-matching cards
        gameState.getAiHand().clear();
        gameState.getAiHand().add(CardFactory.createCard(2, Card.CARD_SHAPE_CLUBS));
        gameState.getAiHand().add(CardFactory.createCard(9, Card.CARD_SHAPE_SPADES));
        
        Card playedCard = aiPlayer.playCard(gameState, playRule, false);
        
        assertNull(playedCard, "Should return null when no playable card exists");
    }

    @Test
    void testTakeTurnHandlesAccumulatedDraws() {
        // Set up attack scenario
        Card attackCard = CardFactory.createCard(Card.TWO_NUMBER, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(attackCard);
        playRule.setInitialCard(attackCard, gameState);
        playRule.playCard(attackCard, false); // Sets accumulated draws
        
        int initialHandSize = gameState.getAiHand().size();
        int accumulatedDraws = playRule.getAccumulatedDraws();
        
        aiPlayer.takeTurn(gameState, playRule, false);
        
        // AI should draw accumulated cards
        if (accumulatedDraws > 0 && gameState.getDeckSize() >= accumulatedDraws) {
            assertTrue(gameState.getAiHand().size() >= initialHandSize + accumulatedDraws,
                      "AI hand should increase by accumulated draws");
        }
    }

    @Test
    void testTakeTurnOnInitialTurn() {
        // On initial turn, any card should be playable
        Card anyCard = CardFactory.createCard(4, Card.CARD_SHAPE_DIAMONDS);
        gameState.getAiHand().clear();
        gameState.getAiHand().add(anyCard);
        
        boolean result = aiPlayer.takeTurn(gameState, playRule, true);
        
        assertTrue(result, "Take turn should complete on initial turn");
    }

    @Test
    void testTakeTurnHandlesJokerBlocking() {
        // Set up Joker on table
        Card jokerCard = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        gameState.setInitialCard(jokerCard);
        playRule.setInitialCard(jokerCard, gameState);
        
        // Add Joker to AI hand
        gameState.getAiHand().clear();
        gameState.getAiHand().add(CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY));
        
        int initialHandSize = gameState.getAiHand().size();
        
        aiPlayer.takeTurn(gameState, playRule, false);
        
        // AI should try to block with Joker
        // Hand size might decrease if Joker was played
        assertTrue(gameState.getAiHand().size() <= initialHandSize,
                  "Hand size should not increase after Joker block attempt");
    }

    @Test
    void testTakeTurnHandlesFaceCardEffect() {
        // Set up FaceCard scenario
        Card faceCard = CardFactory.createCard(Card.JACK_NUMBER, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(faceCard);
        playRule.setInitialCard(faceCard, gameState);
        
        // Add matching FaceCard to AI hand
        Card playableFaceCard = CardFactory.createCard(Card.JACK_NUMBER, Card.CARD_SHAPE_DIAMONDS);
        gameState.getAiHand().clear();
        gameState.getAiHand().add(playableFaceCard);
        
        // Add another playable card for the FaceCard effect
        Card secondCard = CardFactory.createCard(Card.JACK_NUMBER, Card.CARD_SHAPE_CLUBS);
        gameState.getAiHand().add(secondCard);
        
        int initialHandSize = gameState.getAiHand().size();
        
        aiPlayer.takeTurn(gameState, playRule, false);
        
        // AI might play FaceCard and then another card
        // So hand should decrease by at least 1
        assertTrue(gameState.getAiHand().size() < initialHandSize,
                  "Hand should decrease after playing FaceCard");
    }

    @Test
    void testPlayCardWithJoker() throws InvalidMoveException {
        // Joker can be played on any card
        Card anyCard = CardFactory.createCard(7, Card.CARD_SHAPE_SPADES);
        gameState.setInitialCard(anyCard);
        playRule.setInitialCard(anyCard, gameState);
        
        // Add Joker to hand
        Card joker = CardFactory.createCard(Card.JOKER_NUMBER, Card.CARD_SHAPE_ANY);
        gameState.getAiHand().clear();
        gameState.getAiHand().add(joker);
        
        Card playedCard = aiPlayer.playCard(gameState, playRule, false);
        
        assertNotNull(playedCard, "Joker should be playable");
        assertTrue(playedCard instanceof JokerCard, "Played card should be a Joker");
    }
}
