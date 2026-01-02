package onecardgame;

import java.util.List;
import java.util.Random;

import onecardgame.cards.Attackable;
import onecardgame.cards.Card;
import onecardgame.cards.FaceCard;
import onecardgame.cards.JokerCard;
import onecardgame.cards.NumSevenCard;

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
    
    // Multiple card dropping probabilities
    private static final int MULTIPLE_CARDS_NO_ATTACK_PERCENT = 90;
    private static final int MULTIPLE_CARDS_HAND_SIZE_THRESHOLD = 10;
    private static final int MULTIPLE_CARDS_WITH_ATTACK_LEFT_PERCENT = 95;
    
    // Strategic shape changing probabilities
    private static final int STRATEGIC_SHAPE_CHANGE_MULTIPLE_SHAPES_PERCENT = 75;
    private static final int STRATEGIC_SHAPE_CHANGE_MATCHING_SHAPE_PERCENT = 90;

    private final Random random = new Random();

    @Override
    public boolean takeTurn(final GameState gameState,
                            final PlayRule gameRule,
                            final Dealer dealer,
                            final boolean isInitialTurn,
                            final String action) {
        // AI ignores action parameter - it makes its own decisions
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
                    handleAccumulatedDraws(gameState, gameRule, dealer, accumulatedDraws);
                }
            } else {
                // No attackable card or decided not to defend, draw accumulated cards
                handleAccumulatedDraws(gameState, gameRule, dealer, accumulatedDraws);
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
                drawSingleCard(gameState, gameRule, dealer);
            }
        }
        return true;
    }

    private void handleAccumulatedDraws(final GameState gameState, final PlayRule gameRule, final Dealer dealer, final int accumulatedDraws) {
        GameMessages.displayFormatted(GameMessages.AI_DREW_ACCUMULATED_CARDS, accumulatedDraws);
        
        int cardsDrawn = dealer.drawCards(gameState, gameRule, gameState.getAiHand(), accumulatedDraws);
        
        if (cardsDrawn < accumulatedDraws) {
            GameMessages.display(GameMessages.DECK_EMPTY_NO_MORE_CARDS);
        }
        
        GameMessages.display(GameMessages.AI_DREW_ALL_CARDS);
        GameMessages.display(""); // Newline after AI message
        gameRule.resetAccumulatedDraws();
    }

    private void drawSingleCard(final GameState gameState, final PlayRule gameRule, final Dealer dealer) {
        if (dealer.drawCard(gameState, gameRule, gameState.getAiHand())) {
            GameMessages.display(GameMessages.AI_DREW_CARD);
            GameMessages.display(""); // Newline after AI message
        } else {
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
                .anyMatch(Attackable.class::isInstance);
        
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
        
        // First, check if AI should drop multiple matching cards
        List<Integer> multipleCardIndices = findMultipleMatchingCards(gameState, gameRule, isInitialTurn);
        if (multipleCardIndices != null && !multipleCardIndices.isEmpty() && 
            shouldDropMultipleCards(gameState, gameRule, multipleCardIndices)) {
            // Drop multiple matching cards
            dropMultipleCards(gameState, gameRule, multipleCardIndices, isInitialTurn);
            return;
        }
        
        // Check if AI has a playable NumSevenCard and decide whether to use it
        NumSevenCard numSevenCard = findPlayableNumSevenCard(gameState, gameRule, isInitialTurn);
        final Card aiCard;
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
            if (aiCard instanceof NumSevenCard numSeven) {
                // Check AI hand size before removing (to see if there's one card left besides NumSevenCard)
                final int aiHandSizeBeforeRemoval = gameState.getAiHand().size();
                // Remove card from hand first
                gameState.getAiHand().remove(cardIndex);
                // Strategically select a new shape based on game state
                final String newShape = selectRandomShape(gameState, lastUsedCard, aiHandSizeBeforeRemoval);
                // Play the card with new shape
                gameRule.playNumSevenCard(numSeven, newShape, isInitialTurn);
                // Create a new card with the new shape for display
                final NumSevenCard cardWithNewShape = numSeven.withShape(newShape);
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
            if (card instanceof NumSevenCard numSevenCard) {
                try {
                    if (gameRule.isCardPlayable(card, lastUsedCard, isInitialTurn)) {
                        return numSevenCard;
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
        final int playerHandSize = gameState.getPlayerHand().size();
        final List<Card> aiHand = gameState.getAiHand();
        
        // Don't use 7 card if lastUsedCard is null or Joker (any card can be played)
        if (lastUsedCard == null || lastUsedCard instanceof JokerCard) {
            return false;
        }
        
        // Rule 1: If non-AI player has less than 3 cards left, and NumSevenCard is playable, 
        // use it to change shape (70% chance)
        if (playerHandSize < 3) {
            // Check if NumSevenCard is playable (depends on lastUsedCard)
            NumSevenCard playableNumSeven = findPlayableNumSevenCard(gameState, gameRule, isInitialTurn);
            if (playableNumSeven != null) {
                // 70% chance to use NumSevenCard strategically
                if (random.nextInt(PERCENTAGE_BASE) < NUMSEVEN_STRATEGIC_SHAPE_DIFFERENT_PERCENT) {
                    return true; // Use NumSevenCard to change to beneficial shape
                }
            }
        }
        
        // NEW INDEPENDENT RULE: Strategic shape changing when opponent is close to winning
        // This is separate from Rule 1 above
        if (playerHandSize == ONE_CARD_LEFT || playerHandSize == TWO_CARDS_LEFT) {
            // Count distinct shapes in AI hand
            long distinctShapes = aiHand.stream()
                .map(Card::getShape)
                .distinct()
                .count();
            
            // Check if AI has one shape matching last used card
            boolean hasMatchingShape = aiHand.stream()
                .anyMatch(card -> card.getShape().equals(lastUsedCard.getShape()));
            
            // Check if NumSevenCard is playable
            NumSevenCard playableNumSeven = findPlayableNumSevenCard(gameState, gameRule, isInitialTurn);
            if (playableNumSeven != null) {
                if (distinctShapes > 1) {
                    // AI has multiple shapes - 75% chance to use 7 to change shape strategically
                    if (random.nextInt(PERCENTAGE_BASE) < STRATEGIC_SHAPE_CHANGE_MULTIPLE_SHAPES_PERCENT) {
                        return true; // Use NumSevenCard to mess up opponent
                    }
                } else if (hasMatchingShape) {
                    // AI has one shape matching last card - 90% chance to use 7 to change shape
                    if (random.nextInt(PERCENTAGE_BASE) < STRATEGIC_SHAPE_CHANGE_MATCHING_SHAPE_PERCENT) {
                        return true; // Use NumSevenCard to mess up opponent
                    }
                }
            }
        }
        
        // EXISTING RULES: Original NumSevenCard probability logic (independent from above)
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
     * ALL rules use beneficial shape selection (most common shape 80%, second most 20%).
     * This ensures AI always picks a shape that maximizes its chances to play cards next turn.
     * 
     * @param gameState The game state
     * @param lastUsedCard The last used card on the table
     * @param aiHandSizeBeforeRemoval The AI hand size before removing NumSevenCard
     * @return The selected shape (always beneficial for AI)
     */
    private String selectRandomShape(final GameState gameState, 
                                     final Card lastUsedCard,
                                     final int aiHandSizeBeforeRemoval) {
        final int playerHandSize = gameState.getPlayerHand().size();
        final int aiHandSize = gameState.getAiHand().size();
        final List<Card> aiHand = gameState.getAiHand();
        final String currentShape = lastUsedCard != null ? lastUsedCard.getShape() : null;
        
        // Rule 1: If non-AI player has less than 3 cards left, change to beneficial shape
        if (playerHandSize < 3 && currentShape != null) {
            // Count cards by shape (excluding NumSevenCard and current shape)
            java.util.Map<String, Long> shapeCounts = new java.util.HashMap<>();
            for (Card card : aiHand) {
                if (card instanceof NumSevenCard || card.getShape().equals(currentShape)) {
                    continue;
                }
                String shape = card.getShape();
                shapeCounts.put(shape, shapeCounts.getOrDefault(shape, 0L) + 1);
            }
            if (!shapeCounts.isEmpty()) {
                List<java.util.Map.Entry<String, Long>> sortedShapes = shapeCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .toList();
                if (!sortedShapes.isEmpty()) {
                    // 80% chance to pick most common shape, 20% chance to pick second most
                    if (sortedShapes.size() == 1 || random.nextInt(PERCENTAGE_BASE) < 80) {
                        return sortedShapes.get(0).getKey();
                    } else {
                        return sortedShapes.get(1).getKey();
                    }
                }
            }
            return selectDifferentShape(currentShape);
        }
        
        // Rule 2: If AI has one card left (besides NumSevenCard), choose same shape (90% chance)
        // Check BEFORE removal: if hand had 2 cards (NumSevenCard + 1 other), then after removal there's 1 left
        if (aiHandSizeBeforeRemoval == TWO_CARDS_LEFT && aiHandSize == ONE_CARD_LEFT) {
            final Card otherCard = gameState.getAiHand().get(MIN_INDEX);
            if (otherCard != null && random.nextInt(PERCENTAGE_BASE) < NUMSEVEN_STRATEGIC_SHAPE_SAME_PERCENT) {
                // Select the same shape as the other card (beneficial for AI)
                return otherCard.getShape();
            }
        }
        
        // Rule 3: If opponent has 1-2 cards left and we're using NumSevenCard strategically,
        // change shape to beneficial shape
        if ((playerHandSize == ONE_CARD_LEFT || playerHandSize == TWO_CARDS_LEFT) && currentShape != null) {
            // Count cards by shape (excluding NumSevenCard and current shape)
            java.util.Map<String, Long> shapeCounts = new java.util.HashMap<>();
            for (Card card : aiHand) {
                if (card instanceof NumSevenCard || card.getShape().equals(currentShape)) {
                    continue;
                }
                String shape = card.getShape();
                shapeCounts.put(shape, shapeCounts.getOrDefault(shape, 0L) + 1);
            }
            if (!shapeCounts.isEmpty()) {
                List<java.util.Map.Entry<String, Long>> sortedShapes = shapeCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .toList();
                if (!sortedShapes.isEmpty()) {
                    // 80% chance to pick most common shape, 20% chance to pick second most
                    if (sortedShapes.size() == 1 || random.nextInt(PERCENTAGE_BASE) < 80) {
                        return sortedShapes.get(0).getKey();
                    } else {
                        return sortedShapes.get(1).getKey();
                    }
                }
            }
            return selectDifferentShape(currentShape);
        }
        
        // Default: ALL rules should be beneficial - select beneficial shape (not random)
        if (currentShape != null) {
            // Count cards by shape (excluding NumSevenCard and current shape)
            java.util.Map<String, Long> shapeCounts = new java.util.HashMap<>();
            for (Card card : aiHand) {
                if (card instanceof NumSevenCard || card.getShape().equals(currentShape)) {
                    continue;
                }
                String shape = card.getShape();
                shapeCounts.put(shape, shapeCounts.getOrDefault(shape, 0L) + 1);
            }
            if (!shapeCounts.isEmpty()) {
                List<java.util.Map.Entry<String, Long>> sortedShapes = shapeCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .toList();
                if (!sortedShapes.isEmpty()) {
                    // 80% chance to pick most common shape, 20% chance to pick second most
                    if (sortedShapes.size() == 1 || random.nextInt(PERCENTAGE_BASE) < 80) {
                        return sortedShapes.get(0).getKey();
                    } else {
                        return sortedShapes.get(1).getKey();
                    }
                }
            }
            return selectDifferentShape(currentShape);
        }
        
        // Fallback: randomly select a shape (only if no lastUsedCard)
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
    
    /**
     * Finds groups of matching cards by rank that can be dropped together.
     * Returns the indices of cards to drop, or null if no matching group found.
     */
    private List<Integer> findMultipleMatchingCards(final GameState gameState,
                                                    final PlayRule gameRule,
                                                    final boolean isInitialTurn) {
        final Card lastUsedCard = gameState.getLastUsedCard();
        final List<Card> aiHand = gameState.getAiHand();
        
        // Group cards by rank
        java.util.Map<Integer, List<Integer>> cardsByRank = new java.util.HashMap<>();
        for (int i = 0; i < aiHand.size(); i++) {
            Card card = aiHand.get(i);
            // Skip NumSevenCard - can't be dropped with other cards
            if (card == null || card instanceof NumSevenCard) {
                continue;
            }
            
            int rank = card.getRank();
            cardsByRank.computeIfAbsent(rank, k -> new java.util.ArrayList<>()).add(i);
        }
        
        // Find the best group of matching cards that are playable
        List<Integer> bestGroup = null;
        int bestGroupSize = 0;
        
        for (java.util.Map.Entry<Integer, List<Integer>> entry : cardsByRank.entrySet()) {
            List<Integer> indices = entry.getValue();
            if (indices.size() >= 2) { // Need at least 2 cards to drop multiple
                // Check if first card in group is playable
                Card firstCard = aiHand.get(indices.get(0));
                try {
                    if (gameRule.isCardPlayable(firstCard, lastUsedCard, isInitialTurn)) {
                        // This group is playable - check if it's better than current best
                        if (indices.size() > bestGroupSize) {
                            bestGroup = selectStrategicCards(indices, aiHand, lastUsedCard, gameRule);
                            bestGroupSize = bestGroup.size();
                        }
                    }
                } catch (InvalidMoveException e) {
                    // Not playable, skip
                }
            }
        }
        
        return bestGroup;
    }
    
    /**
     * Strategically selects which matching cards to drop based on lastUsedCard, accumulated draws, and probability.
     * Returns indices sorted so the most strategic card is dropped last.
     * 
     * @param matchingIndices Indices of matching cards to choose from
     * @param aiHand The AI's hand
     * @param lastUsedCard The last card played (for strategic decisions)
     * @param gameRule The play rules manager (to check accumulated draws)
     * @return Strategically selected card indices
     */
    private List<Integer> selectStrategicCards(List<Integer> matchingIndices,
                                                List<Card> aiHand,
                                                Card lastUsedCard,
                                                PlayRule gameRule) {
        if (matchingIndices.size() <= 2) {
            return matchingIndices; // Small group, drop all
        }
        
        final int accumulatedDraws = gameRule.getAccumulatedDraws();
        final int aiHandSize = aiHand.size();
        List<Integer> selectedIndices = new java.util.ArrayList<>();
        List<Integer> attackCards = new java.util.ArrayList<>();
        List<Integer> normalCards = new java.util.ArrayList<>();
        
        // Separate attack cards from normal cards
        for (Integer index : matchingIndices) {
            Card card = aiHand.get(index);
            if (card == null) {
                continue;
            }
            if (card instanceof Attackable) {
                attackCards.add(index);
            } else {
                normalCards.add(index);
            }
        }
        
        // DEFENSIVE RULES: Check if AI is under attack and needs to defend
        boolean preferAttackCards = false;
        boolean mustDefend = false;
        
        // Rule 0: If AI has < 3 cards OR > 10 cards, always prefer attack cards for defense (100%)
        if ((aiHandSize < 3 || aiHandSize > MULTIPLE_CARDS_HAND_SIZE_THRESHOLD) && 
            lastUsedCard instanceof Attackable attackingCard && accumulatedDraws > 0) {
            final int attackingPunishment = attackingCard.getPunishment();
            // Filter attack cards to only those with >= punishment value
            List<Integer> validDefenseCards = new java.util.ArrayList<>();
            for (Integer index : attackCards) {
                Card card = aiHand.get(index);
                if (card instanceof Attackable defendingCard) {
                    if (defendingCard.getPunishment() >= attackingPunishment) {
                        validDefenseCards.add(index);
                    }
                }
            }
            if (!validDefenseCards.isEmpty()) {
                mustDefend = true;
                preferAttackCards = true;
                attackCards = validDefenseCards; // Use only valid defense cards
            }
            // If no valid defense cards, fall through to normal strategy (can't defend)
        }
        // Rule 1: If accumulated draws > 6, 100% defend with higher/same tier attack card
        else if (lastUsedCard instanceof Attackable attackingCard && accumulatedDraws > 6) {
            final int attackingPunishment = attackingCard.getPunishment();
            // Filter attack cards to only those with >= punishment value
            List<Integer> validDefenseCards = new java.util.ArrayList<>();
            for (Integer index : attackCards) {
                Card card = aiHand.get(index);
                if (card instanceof Attackable defendingCard) {
                    if (defendingCard.getPunishment() >= attackingPunishment) {
                        validDefenseCards.add(index);
                    }
                }
            }
            if (!validDefenseCards.isEmpty()) {
                mustDefend = true;
                preferAttackCards = true;
                attackCards = validDefenseCards; // Use only valid defense cards
            }
            // If no valid defense cards, fall through to normal strategy (can't defend)
        }
        // Rule 2: If accumulated draws > 2, 70% chance to defend with attack card
        else if (lastUsedCard instanceof Attackable attackingCard && accumulatedDraws > 2) {
            preferAttackCards = random.nextInt(PERCENTAGE_BASE) < 70;
            final int attackingPunishment = attackingCard.getPunishment();
            // Filter for valid defense cards (>= punishment)
            if (preferAttackCards) {
                List<Integer> validDefenseCards = new java.util.ArrayList<>();
                for (Integer index : attackCards) {
                    Card card = aiHand.get(index);
                    if (card instanceof Attackable defendingCard) {
                        if (defendingCard.getPunishment() >= attackingPunishment) {
                            validDefenseCards.add(index);
                        }
                    }
                }
                if (!validDefenseCards.isEmpty()) {
                    attackCards = validDefenseCards; // Use only valid defense cards
                } else {
                    preferAttackCards = false; // No valid defense cards, fall back to normal strategy
                }
            }
        }
        
        // If not defending, use normal strategic decision based on lastUsedCard
        if (!mustDefend && !preferAttackCards) {
            if (lastUsedCard != null) {
                // If last card was an attack card, 70% chance to prefer dropping attack cards
                // This continues the attack chain
                if (lastUsedCard instanceof Attackable) {
                    preferAttackCards = random.nextInt(PERCENTAGE_BASE) < 70;
                }
                // If last card was normal, prefer normal cards (80% chance) to maintain flow
                else {
                    preferAttackCards = random.nextInt(PERCENTAGE_BASE) < 20; // 20% chance to switch to attack
                }
            }
        }
        
        // Strategy: Drop cards based on preference and probability
        if (!attackCards.isEmpty() && !normalCards.isEmpty()) {
            if (preferAttackCards) {
                // Prefer attack cards: drop normal first, then attack (attack becomes lastUsedCard)
                selectedIndices.addAll(normalCards);
                selectedIndices.addAll(attackCards);
            } else {
                // Prefer normal cards: drop attack first, then normal (normal becomes lastUsedCard)
                selectedIndices.addAll(attackCards);
                selectedIndices.addAll(normalCards);
            }
        } else if (!attackCards.isEmpty()) {
            // Only attack cards - drop all (up to 5)
            selectedIndices.addAll(attackCards.size() <= 5 ? attackCards : attackCards.subList(0, 5));
        } else {
            // Only normal cards - drop all (up to 5)
            selectedIndices.addAll(normalCards.size() <= 5 ? normalCards : normalCards.subList(0, 5));
        }
        
        return selectedIndices;
    }
    
    /**
     * Determines if AI should drop multiple matching cards based on probabilities.
     */
    private boolean shouldDropMultipleCards(final GameState gameState,
                                            final PlayRule gameRule,
                                            List<Integer> matchingIndices) {
        final List<Card> aiHand = gameState.getAiHand();
        final int aiHandSize = aiHand.size();
        
        // Count attack cards in hand (excluding the ones we're about to drop)
        long attackCardsRemaining = aiHand.stream()
            .filter(card -> card instanceof Attackable)
            .filter(card -> !matchingIndices.contains(aiHand.indexOf(card)))
            .count();
        
        // Rule 1: 100% if AI has more than 10 cards
        if (aiHandSize > MULTIPLE_CARDS_HAND_SIZE_THRESHOLD) {
            return true;
        }
        
        // Rule 2: 90% if no attack cards available (after dropping matching cards)
        if (attackCardsRemaining == 0) {
            return random.nextInt(PERCENTAGE_BASE) < MULTIPLE_CARDS_NO_ATTACK_PERCENT;
        }
        
        // Rule 3: 95% if less than 10 cards AND has attack cards leftover
        if (aiHandSize <= MULTIPLE_CARDS_HAND_SIZE_THRESHOLD && attackCardsRemaining > 0) {
            return random.nextInt(PERCENTAGE_BASE) < MULTIPLE_CARDS_WITH_ATTACK_LEFT_PERCENT;
        }
        
        return false;
    }
    
    /**
     * Drops multiple matching cards at once.
     * Cards are dropped in strategic order: normal cards first, then attack cards last.
     * The last card played becomes the lastUsedCard, so we want attack cards last.
     */
    private void dropMultipleCards(final GameState gameState,
                                  final PlayRule gameRule,
                                  List<Integer> matchingIndices,
                                  final boolean isInitialTurn) {
        final List<Card> aiHand = gameState.getAiHand();
        
        // Separate cards into normal and attack, preserving original indices
        List<Integer> normalCardIndices = new java.util.ArrayList<>();
        List<Integer> attackCardIndices = new java.util.ArrayList<>();
        
        for (Integer index : matchingIndices) {
            Card card = aiHand.get(index);
            if (card instanceof Attackable) {
                attackCardIndices.add(index);
            } else {
                normalCardIndices.add(index);
            }
        }
        
        // Build play order: normal cards first, then attack cards last
        // This ensures attack cards become the lastUsedCard for maximum effect
        List<Integer> playOrder = new java.util.ArrayList<>();
        playOrder.addAll(normalCardIndices);
        playOrder.addAll(attackCardIndices);
        
        // Sort in descending order to avoid index shifting when removing
        playOrder.sort((a, b) -> Integer.compare(b, a));
        
        Card lastCardPlayed = null;
        int cardsPlayed = 0;
        
        // Play cards in reverse order (highest index first) to avoid index shifting
        for (Integer index : playOrder) {
            Card card = aiHand.get(index);
            lastCardPlayed = card; // Track the last card played (lowest index = last in play order)
            
            try {
                gameRule.playCard(aiHand, index);
                GameMessages.displayFormatted(GameMessages.AI_PLAYED_CARD, card);
                cardsPlayed++;
            } catch (InvalidMoveException e) {
                // Shouldn't happen, but handle gracefully
                continue;
            }
        }
        
        // Update last used card (the last card played becomes the lastUsedCard)
        if (lastCardPlayed != null) {
            gameState.setLastUsedCard(gameRule.getLastUsedCard());
            
            // Display attack message if attack card was dropped
            if (lastCardPlayed instanceof Attackable) {
                displayAttackMessage(gameRule, lastCardPlayed);
            }
        }
        
        if (cardsPlayed > 1) {
            GameMessages.displayFormatted("AI dropped %d matching cards.%n", cardsPlayed);
        }
        
        GameMessages.display(GameMessages.AI_TURN_OVER + System.lineSeparator());
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
