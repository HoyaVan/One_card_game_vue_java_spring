package terminal_mode;

import java.util.List;
import java.util.Random;

import terminal_mode.cards.Attackable;
import terminal_mode.cards.Card;
import terminal_mode.cards.FaceCard;
import terminal_mode.cards.JokerCard;
import terminal_mode.cards.NumSevenCard;

public class AIPlayer implements GameParticipantable {

    private static final int MIN_INDEX = 0;
    private static final int PERCENTAGE_BASE = 100;
    private static final int DEFENSE_PROBABILITY_PERCENT = 95;
    private static final int ATTACKABLE_CARD_PLAY_PROBABILITY_PERCENT = 65;
    private static final int NUMSEVEN_PROBABILITY_WITH_MATCHING_SHAPED_CARDS_PERCENT = 30;
    private static final int NUMSEVEN_PROBABILITY_WITH_MATCHING_ATTACKABLE_CARDS_PERCENT = 60;
    private static final int NUMBER_OF_SHAPES = 4;
    private static final int SHAPE_CHOICE_HEARTS = 0;
    private static final int SHAPE_CHOICE_DIAMONDS = 1;
    private static final int SHAPE_CHOICE_CLUBS = 2;
    private static final int SHAPE_CHOICE_SPADES = 3;
    private static final int ONE_CARD_LEFT = 1;
    private static final int TWO_CARDS_LEFT = 2;
    private static final int NUMSEVEN_STRATEGIC_SHAPE_DIFFERENT_PERCENT = 70;
    private static final int NUMSEVEN_STRATEGIC_SHAPE_SAME_PERCENT = 90;

    private final Random random = new Random();

    @Override
    public boolean takeTurn(final GameState gameState,
            final PlayRule gameRule,
            final boolean isInitialTurn) {
        final int accumulatedDraws;
        GameMessages.display(GameMessages.AI_TURN);

        accumulatedDraws = gameRule.getAccumulatedDraws();
        if (accumulatedDraws > MIN_INDEX) {
            // Under attack: try to defend with attackable card (95% chance)
            // Must check punishment value: defending card must have >= punishment than attacking card
            final Card lastUsedCard = gameState.getLastUsedCard();
            Card attackableCard = null;
            
            if (lastUsedCard instanceof Attackable attackingCard) {
                final int attackingPunishment = attackingCard.getPunishment();
                List<Card> playableCards = gameRule.getPlayableCards(gameState.getAiHand(), 
                                                                      lastUsedCard, 
                                                                      isInitialTurn);
                // Filter for Attackable cards with sufficient punishment value
                attackableCard = playableCards.stream()
                        .filter(card -> {
                            if (card instanceof Attackable defendingCard) {
                                return defendingCard.getPunishment() >= attackingPunishment;
                            }
                            return false;
                        })
                        .findFirst()
                        .orElse(null);
            }
            
            if (attackableCard != null && random.nextInt(PERCENTAGE_BASE) < DEFENSE_PROBABILITY_PERCENT) {
                // Found an attackable card and 95% chance to defend - play it
                int cardIndex = gameState.getAiHand().indexOf(attackableCard);
                try {
                    // Display "AI played" message first, before playCard() displays defense messages
                    GameMessages.displayFormatted(GameMessages.AI_PLAYED_CARD, attackableCard);
                    gameRule.playCard(gameState.getAiHand(), cardIndex);
                    // Get the card from the pile after playing
                    gameState.setLastUsedCard(gameRule.getLastUsedCard());
                    displayAttackMessage(gameRule, attackableCard);
                    if (attackableCard instanceof FaceCard) {
                        handleFaceCardEffect(gameState, gameRule, isInitialTurn);
                    } else {
                        GameMessages.display(GameMessages.AI_TURN_OVER + System.lineSeparator());
                    }
                } catch (InvalidMoveException e) {
                    // Shouldn't happen, but if it does, draw accumulated cards
                    handleAccumulatedDraws(gameState, gameRule, accumulatedDraws);
                }
            } else {
                // No attackable card or decided not to defend, draw accumulated cards
                handleAccumulatedDraws(gameState, gameRule, accumulatedDraws);
            }
        } else {
            // Try to play a card first
            Card playableCard = gameRule.findPlayableCard(gameState.getAiHand(), 
                                                          gameState.getLastUsedCard(), 
                                                          isInitialTurn);
            if (playableCard != null && shouldPlayCard(gameState, gameRule, isInitialTurn)) {
                // Found a playable card and should play it
                tryPlayCard(gameState, gameRule, isInitialTurn);
                tryBlockJoker(gameState, gameRule);
            } else {
                // No playable card or shouldn't play, draw a card (turn ends after drawing)
                drawSingleCard(gameState);
            }
        }
        return true;
    }

    private void handleAccumulatedDraws(final GameState gameState, final PlayRule gameRule, final int accumulatedDraws) {
        GameMessages.displayFormatted(GameMessages.AI_DREW_ACCUMULATED_CARDS, accumulatedDraws);
        for (int i = MIN_INDEX; i < accumulatedDraws; i++) {
            try {
                gameState.getAiHand().add(gameState.drawFromDeck());
            } catch (IllegalStateException e) {
                GameMessages.display(GameMessages.DECK_EMPTY_NO_MORE_CARDS);
                break;
            }
        }
        GameMessages.display(GameMessages.AI_DREW_ALL_CARDS);
        GameMessages.display(""); // Newline after AI message
        gameRule.resetAccumulatedDraws();
    }

    private void drawSingleCard(final GameState gameState) {
        try {
            gameState.getAiHand().add(gameState.drawFromDeck());
            GameMessages.display(GameMessages.AI_DREW_CARD);
            GameMessages.display(""); // Newline after AI message
        } catch (IllegalStateException e) {
            GameMessages.display(GameMessages.DECK_EMPTY_NO_CARD_DRAWN);
            GameMessages.display(""); // Newline after error message
        }
    }

    private boolean shouldPlayCard(final GameState gameState, 
                                    final PlayRule gameRule, 
                                    final boolean isInitialTurn) {
        final int accumulatedDraws;
        final List<Card> playableCards;
        final boolean hasNormalCards;
        final boolean hasAttackableCards;
        
        accumulatedDraws = gameRule.getAccumulatedDraws();
        
        // 1. If under attack (accumulatedDraws > 0), 95% chance to defend
        if (accumulatedDraws > MIN_INDEX) {
            return random.nextInt(PERCENTAGE_BASE) < DEFENSE_PROBABILITY_PERCENT;
        }
        
        // Get all playable cards
        playableCards = gameRule.getPlayableCards(gameState.getAiHand(),
                                                   gameState.getLastUsedCard(),
                                                   isInitialTurn);
        
        if (playableCards.isEmpty()) {
            return false; // No playable cards
        }
        
        // Check for normal (non-attackable) cards
        hasNormalCards = playableCards.stream()
                .anyMatch(card -> !(card instanceof Attackable));
        
        // Check for attackable cards
        hasAttackableCards = playableCards.stream()
                .anyMatch(card -> card instanceof Attackable);
        
        // 2. If has normal matching cards, always play (100%)
        if (hasNormalCards) {
            return true;
        }
        
        // 3. If only has attackable cards, 65% chance to play
        if (hasAttackableCards) {
            return random.nextInt(PERCENTAGE_BASE) < ATTACKABLE_CARD_PLAY_PROBABILITY_PERCENT;
        }
        
        return false;
    }

    private void tryPlayCard(final GameState gameState,
            final PlayRule gameRule,
            final boolean isInitialTurn) {
        final Card lastUsedCard = gameState.getLastUsedCard();
        Card aiCard = null;
        
        // Check if AI has a playable NumSevenCard and decide whether to use it
        NumSevenCard numSevenCard = findPlayableNumSevenCard(gameState, gameRule, isInitialTurn);
        if (numSevenCard != null && shouldUseNumSevenCard(gameState, gameRule, isInitialTurn)) {
            // Use NumSevenCard
            aiCard = numSevenCard;
        } else {
            // Use normal card selection logic
            aiCard = gameRule.findPlayableCard(gameState.getAiHand(),
                    lastUsedCard,
                    isInitialTurn);
        }

        if (aiCard == null) {
            return;
        }

        int cardIndex = gameState.getAiHand().indexOf(aiCard);
        try {
            // Display "AI played" message first, before playCard() displays defense messages
            GameMessages.displayFormatted(GameMessages.AI_PLAYED_CARD, aiCard);
            
            // Handle NumSevenCard shape change effect
            if (aiCard instanceof NumSevenCard) {
                // Check AI hand size before removing (to see if there's one card left besides NumSevenCard)
                final int aiHandSizeBeforeRemoval = gameState.getAiHand().size();
                // Remove card from hand first
                gameState.getAiHand().remove(cardIndex);
                // Strategically select a new shape based on game state
                final String newShape = selectRandomShape(gameState, lastUsedCard, aiHandSizeBeforeRemoval);
                // Play the card with new shape
                gameRule.playNumSevenCard((NumSevenCard) aiCard, newShape, isInitialTurn);
                // Create a new card with the new shape for display
                final NumSevenCard cardWithNewShape = ((NumSevenCard) aiCard).withShape(newShape);
                gameState.setLastUsedCard(cardWithNewShape);
                GameMessages.displayFormatted(GameMessages.NUMSEVEN_SHAPE_CHANGE, newShape);
            } else {
                gameRule.playCard(gameState.getAiHand(), cardIndex);
                // Get the card from the pile after playing (in case the reference changed)
                gameState.setLastUsedCard(gameRule.getLastUsedCard());
            }
        } catch (InvalidMoveException e) {
            // This shouldn't happen since findPlayableCard already validated, but handle gracefully
            // GameMessages.displayFormatted(GameMessages.DEBUG_INVALID_MOVE, e.getMessage());
            return;
        }

        displayAttackMessage(gameRule, aiCard);

        if (aiCard instanceof FaceCard) {
            handleFaceCardEffect(gameState, gameRule, isInitialTurn);
        } else {
            GameMessages.display(GameMessages.AI_TURN_OVER + System.lineSeparator());
        }
    }
    
    /**
     * Finds a playable NumSevenCard in the AI's hand.
     * 
     * @param gameState The game state
     * @param gameRule The play rules manager
     * @param isInitialTurn Whether this is the initial turn
     * @return A playable NumSevenCard, or null if none found
     */
    private NumSevenCard findPlayableNumSevenCard(final GameState gameState,
                                                   final PlayRule gameRule,
                                                   final boolean isInitialTurn) {
        final Card lastUsedCard = gameState.getLastUsedCard();
        
        for (Card card : gameState.getAiHand()) {
            if (card instanceof NumSevenCard) {
                try {
                    if (gameRule.isCardPlayable(card, lastUsedCard, isInitialTurn)) {
                        return (NumSevenCard) card;
                    }
                } catch (InvalidMoveException e) {
                    // Not playable, continue searching
                }
            }
        }
        return null;
    }
    
    /**
     * Determines whether the AI should use a NumSevenCard based on probability rules.
     * 
     * @param gameState The game state
     * @param gameRule The play rules manager
     * @param isInitialTurn Whether this is the initial turn
     * @return true if NumSevenCard should be used, false otherwise
     */
    private boolean shouldUseNumSevenCard(final GameState gameState,
                                           final PlayRule gameRule,
                                           final boolean isInitialTurn) {
        final Card lastUsedCard = gameState.getLastUsedCard();
        
        // Don't use 7 card if lastUsedCard is null or Joker (any card can be played)
        if (lastUsedCard == null || lastUsedCard instanceof JokerCard) {
            return false;
        }
        
        final List<Card> playableCards = gameRule.getPlayableCards(gameState.getAiHand(),
                                                                    lastUsedCard,
                                                                    isInitialTurn);
        
        // Filter out NumSevenCard from playable cards to check other options
        final List<Card> otherPlayableCards = playableCards.stream()
                .filter(card -> !(card instanceof NumSevenCard))
                .toList();
        
        // Check for matching shaped cards (non-attackable) with the last used card
        final boolean hasMatchingShapedCards = otherPlayableCards.stream()
                .anyMatch(card -> {
                    if (card instanceof Attackable) {
                        return false; // Skip attackable cards for this check
                    }
                    // Check if card matches by shape with lastUsedCard
                    return card.getShape().equals(lastUsedCard.getShape());
                });
        
        // Check for matching attackable cards with the last used card
        final boolean hasMatchingAttackableCards = otherPlayableCards.stream()
                .anyMatch(card -> {
                    if (!(card instanceof Attackable)) {
                        return false;
                    }
                    // Check if attackable card matches by shape with lastUsedCard
                    return card.getShape().equals(lastUsedCard.getShape());
                });
        
        // Apply probability rules
        if (!hasMatchingShapedCards && !hasMatchingAttackableCards) {
            // Rule 1: No matching shaped cards → 100% use 7
            return true;
        } else if (hasMatchingShapedCards) {
            // Rule 2: Has matching shaped cards → 30% use 7
            return random.nextInt(PERCENTAGE_BASE) < NUMSEVEN_PROBABILITY_WITH_MATCHING_SHAPED_CARDS_PERCENT;
        } else if (hasMatchingAttackableCards) {
            // Rule 3: Has matching attackable cards → 60% use 7
            return random.nextInt(PERCENTAGE_BASE) < NUMSEVEN_PROBABILITY_WITH_MATCHING_ATTACKABLE_CARDS_PERCENT;
        }
        
        return false;
    }
    
    /**
     * Strategically selects a shape for NumSevenCard based on game state.
     * 
     * @param gameState The game state
     * @param lastUsedCard The last used card on the table
     * @param aiHandSizeBeforeRemoval The AI hand size before removing NumSevenCard
     * @return The selected shape
     */
    private String selectRandomShape(final GameState gameState, 
                                     final Card lastUsedCard,
                                     final int aiHandSizeBeforeRemoval) {
        final int playerHandSize = gameState.getPlayerHand().size();
        final int aiHandSize = gameState.getAiHand().size();
        
        // Rule 1: If non-AI player has one card left, use 7 and change to different shape (70% chance)
        if (playerHandSize == ONE_CARD_LEFT && lastUsedCard != null) {
            if (random.nextInt(PERCENTAGE_BASE) < NUMSEVEN_STRATEGIC_SHAPE_DIFFERENT_PERCENT) {
                // Select a different shape from the last used card
                return selectDifferentShape(lastUsedCard.getShape());
            }
        }
        
        // Rule 2: If AI has one card left (besides NumSevenCard), choose same shape (90% chance)
        // Check BEFORE removal: if hand had 2 cards (NumSevenCard + 1 other), then after removal there's 1 left
        if (aiHandSizeBeforeRemoval == TWO_CARDS_LEFT && aiHandSize == ONE_CARD_LEFT) {
            final Card otherCard = gameState.getAiHand().get(MIN_INDEX);
            if (otherCard != null && random.nextInt(PERCENTAGE_BASE) < NUMSEVEN_STRATEGIC_SHAPE_SAME_PERCENT) {
                // Select the same shape as the other card
                return otherCard.getShape();
            }
        }
        
        // Default: randomly select a shape
        return selectRandomShapeDefault();
    }
    
    /**
     * Selects a random shape (default behavior).
     * 
     * @return A randomly selected shape
     */
    private String selectRandomShapeDefault() {
        final int shapeChoice = random.nextInt(NUMBER_OF_SHAPES);
        return switch (shapeChoice) {
            case SHAPE_CHOICE_HEARTS -> Card.CARD_SHAPE_HEARTS;
            case SHAPE_CHOICE_DIAMONDS -> Card.CARD_SHAPE_DIAMONDS;
            case SHAPE_CHOICE_CLUBS -> Card.CARD_SHAPE_CLUBS;
            case SHAPE_CHOICE_SPADES -> Card.CARD_SHAPE_SPADES;
            default -> Card.CARD_SHAPE_HEARTS; // Fallback
        };
    }
    
    /**
     * Selects a shape that is different from the given shape.
     * 
     * @param currentShape The shape to avoid
     * @return A different shape
     */
    private String selectDifferentShape(final String currentShape) {
        final List<String> allShapes = List.of(
            Card.CARD_SHAPE_HEARTS,
            Card.CARD_SHAPE_DIAMONDS,
            Card.CARD_SHAPE_CLUBS,
            Card.CARD_SHAPE_SPADES
        );
        
        final List<String> differentShapes = allShapes.stream()
                .filter(shape -> !shape.equals(currentShape))
                .toList();
        
        if (differentShapes.isEmpty()) {
            return Card.CARD_SHAPE_HEARTS; // Fallback
        }
        
        final int randomIndex = random.nextInt(differentShapes.size());
        return differentShapes.get(randomIndex);
    }

    private void displayAttackMessage(final PlayRule gameRule, final Card card) {
        int currentAccumulatedDraws = gameRule.getAccumulatedDraws();
        if (currentAccumulatedDraws > 0
                && card instanceof Attackable) {
            GameMessages.displayFormatted(GameMessages.AI_ATTACK, currentAccumulatedDraws);
        }
    }

    private void handleFaceCardEffect(final GameState gameState, final PlayRule gameRule, final boolean isInitialTurn) {
        Card lastCard = gameState.getLastUsedCard();
        Card additionalCard = gameRule.findPlayableCard(gameState.getAiHand(), lastCard, isInitialTurn);

        if (additionalCard != null) {
            int additionalIndex = gameState.getAiHand().indexOf(additionalCard);
            try {
                gameRule.playCard(gameState.getAiHand(), additionalIndex);
                // Get the card from the pile after playing
                gameState.setLastUsedCard(gameRule.getLastUsedCard());
                GameMessages.displayFormatted(GameMessages.AI_PLAYED_ANOTHER_CARD, additionalCard);
            } catch (InvalidMoveException e) {
                // This shouldn't happen since findPlayableCard already validated, but handle gracefully
                GameMessages.displayFormatted(GameMessages.DEBUG_INVALID_MOVE, e.getMessage());
            }
        } else {
            GameMessages.display(GameMessages.AI_NO_ADDITIONAL_CARD);
        }
    }

    private void tryBlockJoker(final GameState gameState, final PlayRule gameRule) {
        if (!(gameState.getLastUsedCard() instanceof JokerCard)) {
            return;
        }

        for (Card card : gameState.getAiHand()) {
            if (card instanceof JokerCard) {
                int cardIndex = gameState.getAiHand().indexOf(card);
                try {
                    gameRule.playCard(gameState.getAiHand(), cardIndex);
                    gameState.setLastUsedCard(card);
                    GameMessages.display(GameMessages.AI_BLOCKED_JOKER);
                    return;
                } catch (InvalidMoveException e) {
                    // This shouldn't happen for a Joker, but handle gracefully
                    // GameMessages.displayFormatted(GameMessages.DEBUG_INVALID_MOVE, e.getMessage());
                    continue; // Try next card
                }
            }
        }
    }

    @Override
    public Card playCard(final GameState gameState,
            final PlayRule gameRule,
            final boolean isInitialTurn) throws InvalidMoveException {

        final Card cardToPlay;
        final int cardIndex;

        cardToPlay = gameRule.findPlayableCard(gameState.getAiHand(),
                gameState.getLastUsedCard(),
                isInitialTurn);

        if (cardToPlay == null) {
            return null;
        }

        cardIndex = gameState.getAiHand().indexOf(cardToPlay);
        gameRule.playCard(gameState.getAiHand(), cardIndex);
        // Get the card from the pile after playing
        gameState.setLastUsedCard(gameRule.getLastUsedCard());

        return cardToPlay;
    }
}
