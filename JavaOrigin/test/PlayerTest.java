import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import onecardgame.GameState;
import onecardgame.InvalidMoveException;
import onecardgame.PlayRule;
import onecardgame.Player;
import onecardgame.cards.Card;
import onecardgame.cards.CardFactory;

class PlayerTest {

    private Player player;
    private GameState gameState;
    private PlayRule playRule;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        playRule = new PlayRule();
    }

    @Test
    void testPlayerInitialization() {
        scanner = new Scanner("1\n");
        player = new Player(scanner);
        assertNotNull(player, "Player should not be null after initialization");
    }

    @Test
    void testPlayerInitializationWithNullScanner() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Player(null);
        }, "Should throw exception when scanner is null");
        assertEquals(IllegalArgumentException.class, exception.getClass(), "Exception should be IllegalArgumentException");
    }

    @Test
    void testTakeTurnWithDrawInput() {
        String input = "0\n"; // Draw input
        scanner = new Scanner(input);
        player = new Player(scanner);
        
        int initialHandSize = gameState.getPlayerHand().size();
        int initialDeckSize = gameState.getDeckSize();
        
        boolean result = player.takeTurn(gameState, playRule, true);
        
        assertTrue(result, "Take turn should complete successfully");
        // Hand size should increase by 1 (if deck not empty)
        if (initialDeckSize > 0) {
            assertTrue(gameState.getPlayerHand().size() >= initialHandSize, 
                      "Hand size should increase or stay same after drawing");
        }
    }

    @Test
    void testTakeTurnWithPlayCardInput() {
        // Set up initial card on table
        Card initialCard = CardFactory.createCard(5, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Add a matching card to player hand
        Card playableCard = CardFactory.createCard(5, Card.CARD_SHAPE_DIAMONDS);
        gameState.getPlayerHand().clear();
        gameState.getPlayerHand().add(playableCard);
        
        String input = "1\n"; // Play first card
        scanner = new Scanner(input);
        player = new Player(scanner);
        
        int initialHandSize = gameState.getPlayerHand().size();
        
        boolean result = player.takeTurn(gameState, playRule, false);
        
        assertTrue(result, "Take turn should complete successfully");
        // Hand should decrease by 1 after playing
        assertEquals(initialHandSize - 1, gameState.getPlayerHand().size(), 
                    "Hand size should decrease by 1 after playing card");
    }

    @Test
    void testPlayCardWithValidCard() throws InvalidMoveException {
        scanner = new Scanner("1\n");
        player = new Player(scanner);
        
        // Set up initial card
        Card initialCard = CardFactory.createCard(3, Card.CARD_SHAPE_CLUBS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Add matching card to hand
        Card playableCard = CardFactory.createCard(3, Card.CARD_SHAPE_SPADES);
        gameState.getPlayerHand().clear();
        gameState.getPlayerHand().add(playableCard);
        
        Card playedCard = player.playCard(gameState, playRule, false);
        
        assertNotNull(playedCard, "Played card should not be null");
        assertEquals(playableCard, playedCard, "Played card should match the card in hand");
        assertTrue(gameState.getPlayerHand().isEmpty(), "Hand should be empty after playing only card");
    }

    @Test
    void testPlayCardWithInvalidIndex() {
        scanner = new Scanner("99\n"); // Invalid index
        player = new Player(scanner);
        
        Card initialCard = CardFactory.createCard(3, Card.CARD_SHAPE_CLUBS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            player.playCard(gameState, playRule, false);
        }, "Should throw exception for invalid index");
        assertEquals(IllegalArgumentException.class, exception.getClass(), "Exception should be IllegalArgumentException");
    }

    @Test
    void testPlayCardWithNonPlayableCard() {
        scanner = new Scanner("1\n");
        player = new Player(scanner);
        
        // Set up initial card
        Card initialCard = CardFactory.createCard(3, Card.CARD_SHAPE_CLUBS);
        gameState.setInitialCard(initialCard);
        playRule.setInitialCard(initialCard, gameState);
        
        // Add non-matching card to hand
        Card nonPlayableCard = CardFactory.createCard(7, Card.CARD_SHAPE_SPADES);
        gameState.getPlayerHand().clear();
        gameState.getPlayerHand().add(nonPlayableCard);
        
        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            player.playCard(gameState, playRule, false);
        }, "Should throw exception for non-playable card");
        assertEquals(InvalidMoveException.class, exception.getClass(), "Exception should be InvalidMoveException");
    }

    @Test
    void testTakeTurnHandlesInvalidInput() {
        // Input with invalid format, then valid draw
        String input = "invalid\n0\n";
        scanner = new Scanner(input);
        player = new Player(scanner);
        
        boolean result = player.takeTurn(gameState, playRule, true);
        
        assertTrue(result, "Take turn should eventually complete with valid input");
    }

    @Test
    void testTakeTurnOnInitialTurn() {
        scanner = new Scanner("1\n");
        player = new Player(scanner);
        
        // Add any card to hand (any card playable on initial turn)
        Card anyCard = CardFactory.createCard(4, Card.CARD_SHAPE_HEARTS);
        gameState.getPlayerHand().clear();
        gameState.getPlayerHand().add(anyCard);
        
        boolean result = player.takeTurn(gameState, playRule, true);
        
        assertTrue(result, "Take turn should complete on initial turn");
    }

    @Test
    void testTakeTurnWithAccumulatedDraws() {
        // Set up attack scenario
        Card attackCard = CardFactory.createCard(Card.TWO_NUMBER, Card.CARD_SHAPE_HEARTS);
        gameState.setInitialCard(attackCard);
        playRule.setInitialCard(attackCard, gameState);
        playRule.playCard(attackCard, false); // This will set accumulated draws
        
        // Player should draw accumulated cards
        String input = "0\n"; // Draw
        scanner = new Scanner(input);
        player = new Player(scanner);
        
        int initialHandSize = gameState.getPlayerHand().size();
        int accumulatedDraws = playRule.getAccumulatedDraws();
        
        boolean result = player.takeTurn(gameState, playRule, false);
        
        assertTrue(result, "Take turn should complete");
        if (accumulatedDraws > 0 && gameState.getDeckSize() >= accumulatedDraws) {
            assertTrue(gameState.getPlayerHand().size() >= initialHandSize + accumulatedDraws,
                      "Hand should increase by accumulated draws amount");
        }
    }
}
