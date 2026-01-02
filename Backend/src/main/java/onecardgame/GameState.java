package onecardgame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import onecardgame.cards.Card;

/**
 * Manages the game state including player hands, deck, and winner checking.
 * Handles file I/O for game state persistence.
 */
public class GameState {
    private static final int MINIMUM_CARDS_FOR_DEAL = 15;
    private static final int INITIAL_CARDS_TO_DEAL = 7;
    private static final int INDEX_OFFSET = 1;
    private static final int MIN_INDEX = 0;
    private static final String CARD_NUMBER_FORMAT = "%d. %s%n";

    private final Deck deck;
    private final List<Card> playerHand;
    private final List<Card> aiHand;
    private Card lastUsedCard; // The current card on the table

    public GameState() {
        this.deck = new Deck();
        this.playerHand = new ArrayList<>();
        this.aiHand = new ArrayList<>();
        // Note: Initial card is drawn in setup() BEFORE dealing hands
        // This ensures the first card is drawn from the shuffled deck first
    }

    /**
     * Deals initial cards to players.
     * Called after the first card is drawn in setup().
     */
    public void dealInitialCards() {
        GameValidator.validateDeckSize(deck.size(), MINIMUM_CARDS_FOR_DEAL);
        GameValidator.validateDeckNotEmpty(deck.size());

        for (int i = 0; i < INITIAL_CARDS_TO_DEAL; i++) {
            addCardToHand(playerHand);
            addCardToHand(aiHand);
        }
    }

    /**
     * Draws a card from the deck and adds it to the specified hand.
     * 
     * @param hand The hand to add the card to
     * @throws IllegalStateException if the deck is empty during deal
     */
    private void addCardToHand(final List<Card> hand) {
        hand.add(deck.drawCard());
    }

    /**
     * Draws a card from the deck.
     * If deck goes below 1 card, reshuffles the used card pile (except last card) and adds to deck.
     * Note: This method requires PlayRule to be passed separately for reshuffling.
     * 
     * @param gameRule The PlayRule instance (needed for reshuffling)
     * @return The card drawn from the deck
     * @throws IllegalStateException if the deck is empty and cannot be reshuffled
     */
    public Card drawFromDeck(PlayRule gameRule) {
        // Reshuffle if deck has less than 1 card
        if (deck.size() < 1) {
            reshuffleDeck(gameRule);
        }
        return deck.drawCard();
    }
    
    /**
     * Draws a card from the deck (legacy method for backward compatibility).
     * Note: This will throw exception if reshuffling is needed. Use drawFromDeck(PlayRule) instead.
     * 
     * @return The card drawn from the deck
     * @throws IllegalStateException if the deck is empty
     */
    public Card drawFromDeck() {
        return deck.drawCard();
    }
    
    /**
     * Reshuffles the used card pile (except the last card) and adds them to the deck.
     * The last card in the used pile remains as the lastUsedCard.
     * 
     * @param gameRule The PlayRule instance to access used card pile
     */
    public void reshuffleDeck(PlayRule gameRule) {
        // Get all cards from used pile except the last one
        List<Card> cardsToReshuffle = gameRule.getUsedCardPileCardsExceptLast();
        if (cardsToReshuffle.isEmpty()) {
            throw new IllegalStateException("Cannot reshuffle - no cards available");
        }
        
        // Shuffle and add to deck
        java.util.Collections.shuffle(cardsToReshuffle);
        deck.addCards(cardsToReshuffle);
        
        // Remove reshuffled cards from used pile (keep only last card)
        gameRule.removeCardsFromPileExceptLast();
    }
    
    /**
     * Checks if deck needs reshuffling before drawing a certain number of cards.
     * 
     * @param cardsToDraw Number of cards that need to be drawn
     * @return true if reshuffling is needed, false otherwise
     */
    public boolean needsReshuffle(int cardsToDraw) {
        return deck.size() < cardsToDraw;
    }
    
    /**
     * Reshuffles deck if needed (when deck < 1 or when trying to draw more than available).
     * This is a public method that can be called before drawing multiple cards.
     * 
     * @param gameRule The PlayRule instance (needed for reshuffling)
     */
    public void reshuffleDeckIfNeeded(PlayRule gameRule) {
        if (deck.size() < 1) {
            reshuffleDeck(gameRule);
        }
    }

    public Card removeFromPlayerHand(final int index) {
        GameValidator.validateHandNotEmpty(playerHand.size());
        return playerHand.remove(index);
    }

    public Card removeFromAIHand(final int index) {
        GameValidator.validateHandNotEmpty(aiHand.size());
        return aiHand.remove(index);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        // Player's hand
        if (playerHand == null || playerHand.isEmpty()) {
            result.append(GameMessages.PLAYERS_HAND_EMPTY);
        } else {
            result.append(GameMessages.PLAYERS_HAND);
            for (int i = MIN_INDEX; i < playerHand.size(); i++) {
                result.append(String.format(CARD_NUMBER_FORMAT, i + INDEX_OFFSET, playerHand.get(i).toString()));
            }
        }
        result.append(System.lineSeparator()); // Newline after player hand

        // AI's hand size
        if (aiHand == null) {
            result.append(GameMessages.AI_HAND_NOT_INITIALIZED);
        } else {
            result.append(String.format(GameMessages.AI_HAS_CARDS, aiHand.size()));
        }
        result.append(System.lineSeparator()); // Newline after AI cards

        // Current card on the table
        if (lastUsedCard != null) {
            result.append(String.format(GameMessages.CURRENT_CARD_ON_TABLE, lastUsedCard));
        }
        result.append(System.lineSeparator()); // Newline after current card

        return result.toString();
    }

    public void printFinalPlayerHands() {
        StringBuilder result = new StringBuilder(GameMessages.GAME_OVER + System.lineSeparator());

        // Player's final hand
        result.append(GameMessages.PLAYERS_FINAL_HAND);
        if (playerHand == null || playerHand.isEmpty()) {
            result.append(GameMessages.PLAYER_WON_NO_CARDS);
        } else if (isPlayerLoser()) {
            result.append(GameMessages.PLAYER_LOST_TOO_MANY_CARDS);
            for (int i = MIN_INDEX; i < playerHand.size(); i++) {
                result.append(String.format(CARD_NUMBER_FORMAT, i + INDEX_OFFSET, playerHand.get(i).toString()));
            }
        } else {
            for (int i = MIN_INDEX; i < playerHand.size(); i++) {
                result.append(String.format(CARD_NUMBER_FORMAT, i + INDEX_OFFSET, playerHand.get(i).toString()));
            }
        }

        // AI's final hand
        result.append(GameMessages.AI_FINAL_HAND);
        if (aiHand == null || aiHand.isEmpty()) {
            result.append(GameMessages.AI_WON_NO_CARDS);
        } else if (isAILoser()) {
            result.append(GameMessages.AI_LOST_TOO_MANY_CARDS);
            for (int i = MIN_INDEX; i < aiHand.size(); i++) {
                result.append(String.format(CARD_NUMBER_FORMAT, i + INDEX_OFFSET, aiHand.get(i).toString()));
            }
        } else {
            for (int i = MIN_INDEX; i < aiHand.size(); i++) {
                result.append(String.format(CARD_NUMBER_FORMAT, i + INDEX_OFFSET, aiHand.get(i).toString()));
            }
        }

        // Print the result
        GameMessages.display(result.toString());
    }

    public void writeFinalHandsToFile(final String fileName) {
        Path filePath = Paths.get(fileName);
        List<String> lines = new ArrayList<>();

        // Add Game Over header
        lines.add(GameMessages.GAME_OVER);

        // Add Player's final hand
        lines.add(GameMessages.PLAYERS_FINAL_HAND.trim());
        if (playerHand == null || playerHand.isEmpty()) {
            lines.add(GameMessages.PLAYER_HAS_NO_CARDS);
        } else if (isPlayerLoser()) {
            lines.add(GameMessages.PLAYER_LOST_TOO_MANY_CARDS.trim());
            for (int i = MIN_INDEX; i < playerHand.size(); i++) {
                lines.add(String.format("%d. %s", i + INDEX_OFFSET, playerHand.get(i).toString()));
            }
        } else {
            for (int i = MIN_INDEX; i < playerHand.size(); i++) {
                lines.add(String.format("%d. %s", i + INDEX_OFFSET, playerHand.get(i).toString()));
            }
        }

        // Add AI's final hand
        lines.add(GameMessages.AI_FINAL_HAND.trim());
        if (aiHand == null || aiHand.isEmpty()) {
            lines.add(GameMessages.AI_HAS_NO_CARDS);
        } else if (isAILoser()) {
            lines.add(GameMessages.AI_LOST_TOO_MANY_CARDS.trim());
            for (int i = MIN_INDEX; i < aiHand.size(); i++) {
                lines.add(String.format("%d. %s", i + INDEX_OFFSET, aiHand.get(i).toString()));
            }
        } else {
            for (int i = MIN_INDEX; i < aiHand.size(); i++) {
                lines.add(String.format("%d. %s", i + INDEX_OFFSET, aiHand.get(i).toString()));
            }
        }

        lines.add(GameMessages.END_OF_GAME);

        // Write to file
        try {
            Files.write(filePath, lines);
            GameMessages.displayFormatted(GameMessages.FINAL_HANDS_WRITTEN_TO_FILE, fileName);
        } catch (IOException e) {
            GameMessages.displayErrorFormatted(GameMessages.ERROR_WRITING_TO_FILE, e.getMessage());
        }
    }

    // Getters and Setters
    /**
     * Sets the initial card on the table at the start of the game.
     * 
     * @param initialCard The card to set as the initial card
     */
    public void setInitialCard(final Card initialCard) {
        GameValidator.validateCardNotNull(initialCard);
        if (initialCard != null) {
            this.lastUsedCard = initialCard;
            GameMessages.displayFormatted(GameMessages.INITIAL_CARD_ON_TABLE, initialCard);
            GameMessages.display(""); // Newline after initial card message
        }

    }

    /**
     * Gets the last used card (current card on the table).
     * 
     * @return The last used card, or null if no card has been played yet
     */
    public Card getLastUsedCard() {
        return lastUsedCard;
    }

    /**
     * Sets the last used card (current card on the table).
     * 
     * @param card The card to set as the last used card
     */
    public void setLastUsedCard(final Card card) {
        this.lastUsedCard = card;
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getAiHand() {
        return aiHand;
    }

    /**
     * Gets the deck for initial card setup.
     * Note: This exposes internal state but is needed for initial game setup.
     * 
     * @return The deck
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Gets the number of cards remaining in the deck.
     * 
     * @return The size of the deck
     */
    public int getDeckSize() {
        return deck.size();
    }

    // Validation and check methods
    public boolean isAIWinner() {
        return aiHand != null && aiHand.isEmpty();
    }

    public boolean isPlayerWinner() {
        return playerHand != null && playerHand.isEmpty();
    }

    public boolean isAILoser() {
        return aiHand != null && aiHand.size() >= 18;
    }

    public boolean isPlayerLoser() {
        return playerHand != null && playerHand.size() >= 18;
    }
}
