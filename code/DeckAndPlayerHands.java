import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;


public class DeckAndPlayerHands {
    private List<Card> deck;
    private List<Card> playerHand;
    private List<Card> aiHand;
    private Card currentCardOnTable;

    public DeckAndPlayerHands() {
        this.deck = new ArrayList<>();
        this.playerHand = new ArrayList<>();
        this.aiHand = new ArrayList<>();
        initializeDeck();
        dealInitialCards();
    }

    private void initializeDeck() {
        String[] shapes = {"Hearts", "Diamonds", "Clubs", "Spades"};
        for (String shape : shapes) {
            for (int number = 1; number <= 13; number++) { // Ace to King
                deck.add(CardFactory.createCard(number, shape));
            }
        }
        // Add Joker cards
        deck.add(CardFactory.createCard(-1, "Any"));
        deck.add(CardFactory.createCard(-1, "Any"));
        Collections.shuffle(deck); // Shuffle the deck
    }

    private void dealInitialCards() {
        if (deck == null || deck.size() < 15) {
            throw new IllegalStateException("Not enough cards in the deck to deal initial hands.");
        }
        for (int i = 0; i < 7; i++) {
            playerHand.add(deck.removeFirst());
            aiHand.add(deck.removeFirst());
        }
        // Set the initial card on the table
        setInitialCurrentCard();
    }

    private void setInitialCurrentCard() {
        if (deck == null || deck.isEmpty()) {
            throw new IllegalStateException("The deck is empty. Cannot set the initial current card.");
        }

        currentCardOnTable = deck.remove(0); // Draw the first card from the deck
        System.out.println("The initial card on the table is: " + currentCardOnTable);
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getAiHand() {
        return aiHand;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public Card getCurrentCardOnTable() {
        return currentCardOnTable;
    }

    public void setCurrentCardOnTable(Card currentCardOnTable) {
        this.currentCardOnTable = currentCardOnTable;
    }

    public void drawCardsForHuman(int numberOfCards) {
        if (deck == null || deck.isEmpty()) {
            throw new IllegalStateException("The deck is empty. No cards can be drawn.");
        }

        // Ensure there are enough cards in the deck to draw
        int cardsToDraw = Math.min(numberOfCards, deck.size());
        for (int i = 0; i < cardsToDraw; i++) {
            playerHand.add(deck.remove(0)); // Draw the top card
        }

        System.out.println("Player drew " + cardsToDraw + " card(s).");
    }

    public void drawCardsForAI(int numberOfCards) {
        if (deck == null || deck.isEmpty()) {
            throw new IllegalStateException("The deck is empty. No cards can be drawn.");
        }

        // Ensure there are enough cards in the deck to draw
        int cardsToDraw = Math.min(numberOfCards, deck.size());
        for (int i = 0; i < cardsToDraw; i++) {
            aiHand.add(deck.remove(0)); // Draw the top card
        }

        System.out.println("AI drew " + cardsToDraw + " card(s).");
    }

    public void playCardForHuman(int index) {
        if (playerHand == null || playerHand.isEmpty()) {
            throw new IllegalStateException("Player's hand is empty. No card can be played.");
        }
        if (index < 0 || index >= playerHand.size()) {
            throw new IllegalArgumentException("Invalid index. Please select a valid card index.");
        }

        // Remove the card from the player's hand and set it as the current card on the table
        currentCardOnTable = playerHand.remove(index);
        System.out.println("Player played: " + currentCardOnTable);
    }

    public void playCardForAI(int index) {
        if (aiHand == null || aiHand.isEmpty()) {
            throw new IllegalStateException("AI's hand is empty. No card can be played.");
        }
        if (index < 0 || index >= aiHand.size()) {
            throw new IllegalArgumentException("Invalid index. Please select a valid card index.");
        }

        // Remove the card from the AI's hand and set it as the current card on the table
        currentCardOnTable = aiHand.remove(index);
        System.out.println("AI played: " + currentCardOnTable);
    }

    public Card drawFromDeck() throws DeckEmptyException {
        if (deck.isEmpty()) {
            throw new DeckEmptyException("The deck is empty!");
        }
        return deck.remove(0);
    }

    public Card removeFromPlayerHand(int index) throws DeckEmptyException {
        if (playerHand.isEmpty()) {
            throw new DeckEmptyException("The player hand is empty!");
        }
        return playerHand.remove(index);
    }

    public Card removeFromAIHand(int index) throws DeckEmptyException {
        if (aiHand.isEmpty()) {
            throw new DeckEmptyException("The player hand is empty!");
        }
        return aiHand.remove(index);
    }

    public int getDeckSize() {
        return deck.size();
    }

    public boolean isAIWinner() {
        return aiHand != null && aiHand.isEmpty();
    }

    // Check if the human player has won
    public boolean isPlayerWinner() {
        return playerHand != null && playerHand.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        // Player's hand
        if (playerHand == null || playerHand.isEmpty()) {
            result.append("Player's hand is empty.\n");
        } else {
            result.append("Player's hand:\n");
            for (int i = 0; i < playerHand.size(); i++) {
                result.append(i + 1)
                        .append(". ")
                        .append(playerHand.get(i).toString())
                        .append("\n");
            }
        }

        // AI's hand size
        if (aiHand == null) {
            result.append("AI's hand is not initialized.\n");
        } else {
            result.append("AI has ")
                    .append(aiHand.size())
                    .append(" cards.\n");
        }

        // Current card on the table
        result.append("Current card on the table: ")
                .append(currentCardOnTable != null ? currentCardOnTable.toString() : "None")
                .append("\n");

        return result.toString();
    }

    public void printFinalPlayerHands() {
        StringBuilder result = new StringBuilder("Game Over!\n");

        // Player's final hand
        result.append("Player's final hand:\n");
        if (playerHand == null || playerHand.isEmpty()) {
            result.append("Player won, they have no cards left.\n");
        } else {
            for (int i = 0; i < playerHand.size(); i++) {
                result.append(i + 1)
                        .append(". ")
                        .append(playerHand.get(i).toString())
                        .append("\n");
            }
        }

        // AI's final hand
        result.append("AI's final hand:\n");
        if (aiHand == null || aiHand.isEmpty()) {
            result.append("AI won, they have no cards left.\n");
        } else {
            for (int i = 0; i < aiHand.size(); i++) {
                result.append(i + 1)
                        .append(". ")
                        .append(aiHand.get(i).toString())
                        .append("\n");
            }
        }

        // Print the result
        System.out.println(result.toString());
    }

    public void writeFinalHandsToFile(String fileName) {
        Path filePath = Paths.get(fileName);
        List<String> lines = new ArrayList<>();

        // Add Game Over header
        lines.add("Game Over!");

        // Add Player's final hand
        lines.add("Player's final hand:");
        if (playerHand == null || playerHand.isEmpty()) {
            lines.add("Player has no cards left.");
        } else {
            for (int i = 0; i < playerHand.size(); i++) {
                lines.add((i + 1) + ". " + playerHand.get(i).toString());
            }
        }

        // Add AI's final hand
        lines.add("AI's final hand:");
        if (aiHand == null || aiHand.isEmpty()) {
            lines.add("AI has no cards left.");
        } else {
            for (int i = 0; i < aiHand.size(); i++) {
                lines.add((i + 1) + ". " + aiHand.get(i).toString());
            }
        }

        lines.add("End of game.");

        // Write to file
        try {
            Files.write(filePath, lines);
            System.out.println("Final hands written to file: " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}
