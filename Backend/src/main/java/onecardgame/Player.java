package onecardgame;

import onecardgame.cards.AceCard;
import onecardgame.cards.Attackable;
import onecardgame.cards.Card;
import onecardgame.cards.FaceCard;
import onecardgame.cards.JokerCard;
import onecardgame.cards.NumSevenCard;
import onecardgame.cards.NumTwoCard;

public class Player implements GameParticipantable {
    private static final String INPUT_DRAW = "0";
    private static final int INDEX_OFFSET = 1;
    private static final int MIN_INDEX = 0;
    
    // For shape selection when playing NumSevenCard (temporary storage)
    private String pendingShapeSelection;

    public Player(final Object ignored) {
        // No longer needs Scanner - actions come from REST API
        this.pendingShapeSelection = null;
    }

    // Week 2. Exception Handling
    @Override
    public boolean takeTurn(final GameState gameState, 
                            final PlayRule gameRule,
                            final Dealer dealer,
                            final boolean isInitialTurn,
                            final String action) {
        boolean validMove = false;
        // Track if we're in REST API mode (action provided) vs console mode (action is null)
        final boolean isRestApiMode = action != null;

        while (!validMove) {
            final int accumulatedDraws;
            final boolean hasAttackableCards;
            try {
                accumulatedDraws = gameRule.getAccumulatedDraws();
                if (accumulatedDraws > MIN_INDEX) {
                    // Check if player has any attackable cards with sufficient punishment to defend
                    final Card lastUsedCard = gameState.getLastUsedCard();
                    if (lastUsedCard instanceof Attackable attackingCard) {
                        final int attackingPunishment = attackingCard.getPunishment();
                        hasAttackableCards = gameState.getPlayerHand().stream()
                                .filter(card -> card instanceof Attackable)
                                .anyMatch(card -> {
                                    Attackable attackableCard = (Attackable) card;
                                    return attackableCard.getPunishment() >= attackingPunishment;
                                });
                    } else {
                        hasAttackableCards = false;
                    }
                    
                    if (!hasAttackableCards) {
                        // No attackable cards with sufficient punishment, automatically draw accumulated cards
                        GameMessages.display(GameMessages.PLAYER_NO_DEFENSE_CARDS);
                        validMove = handleDrawTurn(gameState, gameRule, dealer);
                        continue; // Skip the rest and exit the loop
                    }
                    
                    // Has attackable cards with sufficient punishment, show defense options
                    displayDefenseOptions(gameState, accumulatedDraws);
                }

                // Use action from REST API instead of reading from Scanner
                final String input = (action != null) ? action.trim() : INPUT_DRAW;

                if (isDrawInput(input)) {
                    validMove = handleDrawTurn(gameState, gameRule, dealer);
                } else {
                    validMove = handlePlayTurn(gameState, gameRule, isInitialTurn, input);
                }
            } catch (NumberFormatException e) {
                GameMessages.display(GameMessages.INVALID_INPUT_NUMBER_OR_DRAW);
                GameMessages.display(""); // Newline after error message
                // In REST API mode, don't retry - fail immediately to prevent infinite loop
                if (isRestApiMode) {
                    throw new IllegalArgumentException("Invalid input: " + (action != null ? action : "null"));
                }
                // In console mode, continue loop to read new input
            } catch (IllegalArgumentException e) {
                GameMessages.display(e.getMessage());
                GameMessages.display(""); // Newline after error message
                // In REST API mode, don't retry - fail immediately to prevent infinite loop
                if (isRestApiMode) {
                    throw e; // Re-throw to propagate error to processPlayerAction
                }
                // In console mode, continue loop to read new input
            } catch (InvalidMoveException e) {
                GameMessages.display(e.getMessage());
                GameMessages.display(""); // Newline after error message
                // In REST API mode, don't retry - fail immediately to prevent infinite loop
                // Wrap in IllegalArgumentException so controller can catch it
                if (isRestApiMode) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
                // In console mode, continue loop to read new input
            } catch (IndexOutOfBoundsException e) {
                GameMessages.displayFormatted(GameMessages.INVALID_INDEX_RANGE, gameState.getPlayerHand().size());
                GameMessages.display(""); // Newline after error message
                // In REST API mode, don't retry - fail immediately to prevent infinite loop
                if (isRestApiMode) {
                    throw new IllegalArgumentException("Invalid card index: " + (action != null ? action : "null"));
                }
                // In console mode, continue loop to read new input
            }
        }
        return true; // Turn completed successfully
    }
    
    @Override
    public Card playCard(final GameState gameState, 
                        final PlayRule gameRule, 
                        final boolean isInitialTurn) throws InvalidMoveException {
        // This method is not used in REST API flow - takeTurn handles everything
        throw new UnsupportedOperationException("playCard not used in REST API - use takeTurn with action");
    }
    
    // Helper method for REST API - extracts card index from action
    private int parseCardIndexFromAction(String action) throws InvalidMoveException {
        try {
            return Integer.parseInt(action) - INDEX_OFFSET;
        } catch (NumberFormatException e) {
            throw new InvalidMoveException("Invalid card index: " + action);
        }
    }
    
    // Legacy method kept for compatibility - not used in REST flow
    private Card playCardLegacy(final GameState gameState, 
                        final PlayRule gameRule, 
                        final boolean isInitialTurn,
                        final String action) throws InvalidMoveException {
        final int index;
        final Card card;
        
        try {
            index = parseCardIndexFromAction(action);
            
            GameValidator.validateHandIndex(index, gameState.getPlayerHand().size());
            
            card = gameState.getPlayerHand().get(index);
            
            // Validate the card is playable
            final Card lastCard = gameState.getLastUsedCard();
            GameValidator.validateCardPlayable(card, 
                                            lastCard, 
                                            isInitialTurn, 
                                            gameRule);
            
            // Play the card
            gameRule.playCard(gameState.getPlayerHand(), index);
            
            return card;
        } catch (NumberFormatException e) {
            throw new InvalidMoveException(GameMessages.INVALID_INPUT_NUMBER);
        }
    }

    // Removed - now uses action parameter from REST API

    private boolean isDrawInput(final String input) {
        return INPUT_DRAW.equals(input);
    }

    private boolean handleDrawTurn(final GameState gameState,
                                   final PlayRule gameRule,
                                   final Dealer dealer) {
        final int accumulatedDraws;

        accumulatedDraws = gameRule.getAccumulatedDraws();

        if (accumulatedDraws > MIN_INDEX) {
            drawAccumulatedCards(gameState, gameRule, dealer, accumulatedDraws);
        } else {
            drawSingleCard(gameState, gameRule, dealer);
        }

        return true; // Turn always ends after drawing
    }

    private boolean handlePlayTurn(final GameState gameState,
                                   final PlayRule gameRule,
                                   final boolean isInitialTurn,
                                   final String input) throws InvalidMoveException {
        // Check if multiple cards are being dropped (format: "1,2,3" or "1,2,3,4")
        if (input.contains(",")) {
            return handleMultipleCardsPlay(gameState, gameRule, isInitialTurn, input);
        }
        
        // Single card play (original logic)
        final int index;
        final Card cardToPlay;
        final int accumulatedDraws;
        final Card lastUsedCard;
        final int currentAccumulatedDraws;

        index = Integer.parseInt(input) - INDEX_OFFSET;
        GameValidator.validateHandIndex(index, gameState.getPlayerHand().size());

        cardToPlay = gameState.getPlayerHand().get(index);

        // When under attack, only Attackable cards can be played (or draw)
        accumulatedDraws = gameRule.getAccumulatedDraws();
        if (accumulatedDraws > MIN_INDEX && !(cardToPlay instanceof Attackable)) {
            throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
        }

        // Check if the card is playable
        lastUsedCard = gameState.getLastUsedCard();
        GameValidator.validateCardPlayable(cardToPlay,
                                           lastUsedCard,
                                           isInitialTurn,
                                           gameRule);

        // Handle NumSevenCard shape change effect
        if (cardToPlay instanceof NumSevenCard) {
            // Remove card from hand first
            gameState.getPlayerHand().remove(index);
            // Get shape from action (format: "SHAPE:1" or just use action if it's a shape code)
            String newShape;
            if (input.startsWith("SHAPE:")) {
                String shapeCode = input.substring(6);
                newShape = mapShapeCode(shapeCode);
            } else {
                // Try to parse as shape code directly
                newShape = mapShapeCode(input);
            }
            // Play the card with new shape
            gameRule.playNumSevenCard((NumSevenCard) cardToPlay, newShape, isInitialTurn);
            // Create a new card with the new shape for display
            final NumSevenCard cardWithNewShape = ((NumSevenCard) cardToPlay).withShape(newShape);
            gameState.setLastUsedCard(cardWithNewShape);
            GameMessages.display(""); // Newline before "You played" message
            GameMessages.displayFormatted(GameMessages.PLAYER_PLAYED_CARD, cardToPlay);
            GameMessages.displayFormatted(GameMessages.NUMSEVEN_SHAPE_CHANGE, newShape);
        } else {
            // Play the card - this will update accumulatedDraws in PlayRule
            gameRule.playCard(gameState.getPlayerHand(), index);
            // Update the last used card in game state (get from pile after playing)
            gameState.setLastUsedCard(gameRule.getLastUsedCard());
            GameMessages.display(""); // Newline before "You played" message
            GameMessages.displayFormatted(GameMessages.PLAYER_PLAYED_CARD, cardToPlay);
        }

        // Display accumulated punishment (already updated in PlayRule)
        currentAccumulatedDraws = gameRule.getAccumulatedDraws();
        GameValidator.validateDefenseMove(currentAccumulatedDraws, cardToPlay);

        // Handle FaceCard logic
        if (cardToPlay instanceof FaceCard) {
            GameMessages.display(GameMessages.FACECARD_EFFECT_APPLIED);
            // Display updated game state (hand) after FaceCard is played
            GameMessages.display(gameState.toString());
            return false; // Allow the player to play another card
        }

        return true; // End turn for other cards
    }
    
    /**
     * Handles playing multiple cards at once if they match by rank (number or face value).
     * Format: "1,2,3" for card indices 1, 2, 3.
     * Supports: any numbers, J (Jack), Q (Queen), K (King), Ace, Joker.
     * If multiple Attackable cards (NumTwoCard, AceCard, JokerCard) are dropped, punishment accumulates.
     */
    private boolean handleMultipleCardsPlay(final GameState gameState,
                                           final PlayRule gameRule,
                                           final boolean isInitialTurn,
                                           final String input) throws InvalidMoveException {
        // Parse multiple indices
        String[] indices = input.split(",");
        if (indices.length < 2) {
            throw new InvalidMoveException("Multiple cards must be specified with comma-separated indices");
        }
        
        // Parse and validate all indices
        int[] cardIndices = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            try {
                cardIndices[i] = Integer.parseInt(indices[i].trim()) - INDEX_OFFSET;
                GameValidator.validateHandIndex(cardIndices[i], gameState.getPlayerHand().size());
            } catch (NumberFormatException e) {
                throw new InvalidMoveException("Invalid card index: " + indices[i]);
            }
        }
        
        // Get all cards to play
        Card[] cardsToPlay = new Card[cardIndices.length];
        for (int i = 0; i < cardIndices.length; i++) {
            cardsToPlay[i] = gameState.getPlayerHand().get(cardIndices[i]);
        }
        
        // Validate all cards have the same rank (number/face value)
        // This works for: numbers (1-13), J (11), Q (12), K (13), Ace (1), Joker (-1)
        int firstRank = cardsToPlay[0].getRank();
        for (int i = 1; i < cardsToPlay.length; i++) {
            if (cardsToPlay[i].getRank() != firstRank) {
                throw new InvalidMoveException("All dropped cards must have the same rank (number or face value)");
            }
        }
        
        // Check if first card is playable (all should be playable if first is)
        final Card lastUsedCard = gameState.getLastUsedCard();
        final int accumulatedDraws = gameRule.getAccumulatedDraws();
        
        // When under attack, only Attackable cards can be played
        if (accumulatedDraws > MIN_INDEX) {
            for (Card card : cardsToPlay) {
                if (!(card instanceof Attackable)) {
                    throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
                }
            }
        }
        
        // Validate first card is playable
        GameValidator.validateCardPlayable(cardsToPlay[0], lastUsedCard, isInitialTurn, gameRule);
        
        // Play all cards (must play in reverse order to maintain correct indices)
        // Sort indices in descending order to avoid index shifting issues
        java.util.Arrays.sort(cardIndices);
        for (int i = cardIndices.length - 1; i >= 0; i--) {
            Card card = cardsToPlay[i];
            
            // NumSevenCard cannot be played in multiples
            if (card instanceof NumSevenCard) {
                throw new InvalidMoveException("NumSevenCard cannot be played with other cards");
            }
            
            // Play the card - this will update accumulatedDraws in PlayRule
            gameRule.playCard(gameState.getPlayerHand(), cardIndices[i]);
            GameMessages.display(""); // Newline before "You played" message
            GameMessages.displayFormatted(GameMessages.PLAYER_PLAYED_CARD, card);
        }
        
        // Update the last used card (last card played)
        gameState.setLastUsedCard(gameRule.getLastUsedCard());
        
        // Display accumulated punishment for Attackable cards (NumTwoCard, AceCard, JokerCard)
        int currentAccumulatedDraws = gameRule.getAccumulatedDraws();
        if (cardsToPlay[0] instanceof Attackable && cardsToPlay.length > 1) {
            // Multiple Attackable cards: punishment accumulates
            // NumTwoCard: 2 + 2 + 2 = 6 for 3 cards
            // AceCard: 3 + 3 + 3 = 9 for 3 cards
            // JokerCard: 5 + 5 + 5 = 15 for 3 cards
            String cardType = cardsToPlay[0] instanceof NumTwoCard ? "NumTwo" :
                             cardsToPlay[0] instanceof AceCard ? "Ace" :
                             cardsToPlay[0] instanceof JokerCard ? "Joker" : "Attackable";
            GameMessages.displayFormatted("Dropped %d %s cards. Accumulated draws: %d", 
                cardsToPlay.length, cardType, currentAccumulatedDraws);
        }
        
        GameValidator.validateDefenseMove(currentAccumulatedDraws, cardsToPlay[0]);
        
        // Handle FaceCard logic (if any card is a FaceCard: Jack, Queen, King)
        for (Card card : cardsToPlay) {
            if (card instanceof FaceCard) {
                GameMessages.display(GameMessages.FACECARD_EFFECT_APPLIED);
                GameMessages.display(gameState.toString());
                return false; // Allow the player to play another card
            }
        }
        
        return true; // Cards played, turn can be ended manually
    }

    private void displayDefenseOptions(final GameState gameState,
                                       final int accumulatedDraws) {
        GameMessages.displayFormatted(GameMessages.PLAYER_UNDER_ATTACK, accumulatedDraws);
        GameMessages.display(GameMessages.PLAYER_DEFENSE_OPTIONS_HEADER);

        final Card lastUsedCard = gameState.getLastUsedCard();
        if (lastUsedCard instanceof Attackable attackingCard) {
            final int attackingPunishment = attackingCard.getPunishment();
            
            for (int i = MIN_INDEX; i < gameState.getPlayerHand().size(); i++) {
                final Card card;
                card = gameState.getPlayerHand().get(i);
                if (card instanceof Attackable defendingCard) {
                    // Only show cards with punishment >= attacking card's punishment
                    if (defendingCard.getPunishment() >= attackingPunishment) {
                        GameMessages.displayFormatted("%d. %s", i + INDEX_OFFSET, card);
                    }
                }
            }
        }
    }

    private void drawAccumulatedCards(final GameState gameState, 
                                      final PlayRule gameRule, 
                                      final Dealer dealer,
                                      final int accumulatedDraws) {
        GameMessages.displayFormatted(GameMessages.PLAYER_DREW_ACCUMULATED_CARDS, accumulatedDraws);
        
        int cardsDrawn = dealer.drawCards(gameState, gameRule, gameState.getPlayerHand(), accumulatedDraws);
        
        if (cardsDrawn < accumulatedDraws) {
            GameMessages.display(GameMessages.DECK_EMPTY_NO_MORE_CARDS);
        }
        
        gameRule.resetAccumulatedDraws(); // Reset accumulated draws in PlayRule
    }

    private void drawSingleCard(final GameState gameState, final PlayRule gameRule, final Dealer dealer) {
        if (dealer.drawCard(gameState, gameRule, gameState.getPlayerHand())) {
            GameMessages.display(GameMessages.PLAYER_DREW_CARD);
        } else {
            GameMessages.display(GameMessages.DECK_EMPTY_NO_CARD_DRAWN);
        }
    }

    private String selectNewShape() {
        // Shape selection now comes from action parameter
        // Format: "SHAPE:1" or "SHAPE:2" etc.
        if (pendingShapeSelection != null) {
            String shape = pendingShapeSelection;
            pendingShapeSelection = null;
            return shape;
        }
        // Default fallback - should not happen in REST API flow
        return Card.CARD_SHAPE_HEARTS;
    }
    
    public void setPendingShapeSelection(String shape) {
        this.pendingShapeSelection = shape;
    }
    
    private String mapShapeCode(String code) {
        return switch (code) {
            case "1" -> Card.CARD_SHAPE_HEARTS;
            case "2" -> Card.CARD_SHAPE_DIAMONDS;
            case "3" -> Card.CARD_SHAPE_CLUBS;
            case "4" -> Card.CARD_SHAPE_SPADES;
            default -> Card.CARD_SHAPE_HEARTS; // Default fallback
        };
    }
}
