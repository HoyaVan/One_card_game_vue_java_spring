import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import onecardgame.DeckAndPlayerHands;
import onecardgame.cards.Card;

public class DeckAndPlayerHandsTest {

    @Test
    public void testDeckInitialization() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();
        List<Card> deck = game.getDeck();

        // Test that the deck has 52 cards initially
        assertNotNull(deck, "Deck should not be null after initialization");
        assertEquals(39, deck.size(), "Deck should have 39 cards after dealing hands");
    }

    @Test
    public void testInitialHandsAndCurrentCard() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();

        // Check player hand
        List<Card> playerHand = game.getPlayerHand();
        assertNotNull(playerHand, "Player hand should not be null after initialization");
        assertEquals(7, playerHand.size(), "Player hand should have 7 cards initially");

        // Check AI hand
        List<Card> aiHand = game.getAiHand();
        assertNotNull(aiHand, "AI hand should not be null after initialization");
        assertEquals(7, aiHand.size(), "AI hand should have 7 cards initially");

        // Check current card on table
        Card currentCard = game.getCurrentCardOnTable();
        assertNotNull(currentCard, "Current card on the table should not be null after initialization");
    }

    @Test
    public void testDrawCardsForHuman() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();
        int initialDeckSize = game.getDeckSize();
        int initialPlayerHandSize = game.getPlayerHand().size();

        game.drawCardsForHuman(3);

        // Validate deck size
        assertEquals(initialDeckSize - 3, game.getDeckSize(), "Deck size should decrease by 3 after drawing");
        // Validate player hand size
        assertEquals(initialPlayerHandSize + 3, game.getPlayerHand().size(), "Player hand size should increase by 3 after drawing");
    }

    @Test
    public void testDrawCardsForAI() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();
        int initialDeckSize = game.getDeckSize();
        int initialAIHandSize = game.getAiHand().size();

        game.drawCardsForAI(2);

        // Validate deck size
        assertEquals(initialDeckSize - 2, game.getDeckSize(), "Deck size should decrease by 2 after AI draws cards");
        // Validate AI hand size
        assertEquals(initialAIHandSize + 2, game.getAiHand().size(), "AI hand size should increase by 2 after drawing");
    }

    @Test
    public void testPlayCardForHuman() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();
        int initialPlayerHandSize = game.getPlayerHand().size();

        // Play the first card
        Card playedCard = game.getPlayerHand().get(0);
        game.playCardForHuman(0);

        // Validate current card on table
        assertEquals(playedCard, game.getCurrentCardOnTable(), "Played card should become the current card on the table");
        // Validate player hand size
        assertEquals(initialPlayerHandSize - 1, game.getPlayerHand().size(), "Player hand size should decrease by 1 after playing a card");
    }

    @Test
    public void testPlayCardForAI() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();
        int initialAIHandSize = game.getAiHand().size();

        // Play the first card
        Card playedCard = game.getAiHand().get(0);
        game.playCardForAI(0);

        // Validate current card on table
        assertEquals(playedCard, game.getCurrentCardOnTable(), "Played card should become the current card on the table");
        // Validate AI hand size
        assertEquals(initialAIHandSize - 1, game.getAiHand().size(), "AI hand size should decrease by 1 after playing a card");
    }

    @Test
    public void testIsAIWinner() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();

        // Simulate AI has no cards
        game.getAiHand().clear();

        assertTrue(game.isAIWinner(), "AI should be declared the winner if its hand is empty");
    }

    @Test
    public void testIsPlayerWinner() {
        DeckAndPlayerHands game = new DeckAndPlayerHands();

        // Simulate player has no cards
        game.getPlayerHand().clear();

        assertTrue(game.isPlayerWinner(), "Player should be declared the winner if their hand is empty");
    }

    @Test
    public void testWriteFinalHandsToFile() throws IOException
    {
        DeckAndPlayerHands game = new DeckAndPlayerHands();
        String fileName = "test_game_state.txt";

        // Write final hands to file
        game.writeFinalHandsToFile(fileName);

        // Validate file contents
        Path filePath = Paths.get(fileName);
        assertTrue(Files.exists(filePath), "File should be created");
        List<String> lines = Files.readAllLines(filePath);

        assertFalse(lines.isEmpty(), "File should not be empty");
        assertTrue(lines.get(0).contains("Game Over!"), "File should contain game over message");
    }
}
