package onecardgame.cards;

import onecardgame.GameMessages;

/**
 * Utility class for validating card creation parameters.
 * Provides static methods to validate card numbers and shapes.
 */
public final class CardValidator {
    private CardValidator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates that the card rank is within valid range.
     * Valid range: ACE_NUMBER (1) to KING_NUMBER (13), or JOKER_NUMBER (-1)
     * 
     * @param rank The card rank to validate
     * @throws IllegalArgumentException if the rank is invalid
     */
    public static void validateCardNumber(final int rank) {

        if (rank != Card.JOKER_NUMBER && (rank < Card.ACE_NUMBER || rank > Card.KING_NUMBER)) {
            throw new IllegalArgumentException(GameMessages.INVALID_CARD_INDEX_ERROR);
        }

    }

    /**
     * Validates that the card shape is not null or empty.
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
     * Validates both card rank and shape.
     * 
     * @param rank The card rank to validate
     * @param shape The card shape to validate
     * @throws IllegalArgumentException if either parameter is invalid
     */
    public static void validateCardParameters(final int rank, final String shape) {
        validateCardNumber(rank);
        validateCardShape(shape);
    }
}
