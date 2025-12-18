package onecardgame;

import java.util.Scanner;
import onecardgame.cards.Card;
import onecardgame.cards.Attackable;

/**
 * Utility class for validating game operations.
 * Provides static methods to validate deck operations, hand indices, and state conditions.
 */
public final class GameValidator {
    private static final int MIN_INDEX = 0;

    private GameValidator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates that the deck has enough cards for the specified operation.
     * 
     * @param deckSize The current size of the deck
     * @param minimumRequired The minimum number of cards required
     * @throws IllegalStateException if the deck doesn't have enough cards
     */
    public static void validateDeckSize(final int deckSize, final int minimumRequired) {

        if (deckSize < minimumRequired) {
            throw new IllegalStateException(GameMessages.NOT_ENOUGH_CARDS_TO_DEAL);
        }

    }

    /**
     * Validates that the deck is not empty before drawing a card.
     * 
     * @param deckSize The current size of the deck
     * @throws IllegalStateException if the deck is empty
     */
    public static void validateDeckNotEmpty(final int deckSize) {

        if (deckSize == MIN_INDEX) {
            throw new IllegalStateException(GameMessages.DECK_EMPTY);
        }

    }

    /**
     * Validates that a hand index is within valid bounds.
     * 
     * @param index The index to validate
     * @param handSize The size of the hand
     * @throws IllegalArgumentException if the index is out of bounds
     */
    public static void validateHandIndex(final int index, 
                                        final int handSize) {

        if (index < MIN_INDEX || index >= handSize) {
            throw new IllegalArgumentException(GameMessages.INVALID_CARD_INDEX_ERROR);
        }

    }

    /**
     * Validates that a hand is not empty before removing a card.
     * 
     * @param handSize The size of the hand
     * @throws IllegalStateException if the hand is empty
     */
    public static void validateHandNotEmpty(final int handSize) {

        if (handSize == MIN_INDEX) {
            throw new IllegalStateException(GameMessages.PLAYER_HAND_EMPTY);
        }

    }

    /**
     * Validates both hand index and that the hand is not empty.
     * 
     * @param index The index to validate
     * @param handSize The size of the hand
     * @throws IllegalArgumentException if the index is out of bounds
     * @throws IllegalStateException if the hand is empty
     */
    public static void validateHandIndexAndNotEmpty(final int index, 
                                                    final int handSize) {
        validateHandIndex(index, handSize);
        validateHandNotEmpty(handSize);
    }

    /**
     * Validates that a card is not null.
     * 
     * @param card The card to validate
     * @throws IllegalArgumentException if the card is null
     */
    public static void validateCardNotNull(final Card card) {

        if (card == null) {
            throw new IllegalArgumentException(GameMessages.INVALID_CARD_NULL_ERROR);
        }

    }

    /**
     * Validates that a scanner is not null.
     * 
     * @param scanner The scanner to validate
     * @throws IllegalArgumentException if the scanner is null
     */
    public static void validateScannerNotNull(final Scanner scanner) {

        if (scanner == null) {
            throw new IllegalArgumentException(GameMessages.INVALID_SCANNER_NULL_ERROR);
        }

    }

    /**
     * Validates that a card is playable based on game rules.
     * 
     * @param cardToPlay The card to be played
     * @param lastUsedCard The last card that was played
     * @param isInitialTurn Whether this is the initial turn
     * @param gameRule The game rule instance to check playability
     * @throws InvalidMoveException if the card cannot be played
     */
    public static void validateCardPlayable(final Card cardToPlay, 
                                           final Card lastUsedCard, 
                                           final boolean isInitialTurn,
                                           final PlayRule gameRule) throws InvalidMoveException {

        if (!gameRule.isCardPlayable(cardToPlay, 
                                     lastUsedCard, 
                                     isInitialTurn)) {
            throw new InvalidMoveException(GameMessages.INVALID_MOVE_CARD_DOES_NOT_MATCH);
        }

    }

    /**
     * Validates that a card shape is valid for deck initialization.
     * 
     * @param shape The card shape to validate
     * @throws IllegalArgumentException if the shape is null or empty
     */
    public static void validateCardShape(final String shape) {

        if (shape == null || shape.isEmpty()) {
            throw new IllegalArgumentException(GameMessages.INVALID_CARD_SHAPE_ERROR);
        }
    }

    /**
     * Validates that a defense move is valid.
     * 
     * @param currentAccumulatedDraws The current accumulated draws
     * @param cardToPlay The card to play
     * @throws InvalidMoveException if the defense move is invalid
     */
    public static void validateDefenseMove(final int currentAccumulatedDraws, final Card cardToPlay) {
        if (currentAccumulatedDraws > MIN_INDEX && cardToPlay instanceof Attackable) {
            GameMessages.displayFormatted(GameMessages.ACCUMULATED_PUNISHMENT, currentAccumulatedDraws);
        }
    }
}