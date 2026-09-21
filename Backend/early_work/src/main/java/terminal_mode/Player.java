package terminal_mode;

import java.util.Scanner;

import terminal_mode.cards.Attackable;
import terminal_mode.cards.Card;
import terminal_mode.cards.FaceCard;
import terminal_mode.cards.NumSevenCard;

public class Player implements GameParticipantable {
    private static final String INPUT_DRAW = "0";
    private static final int INDEX_OFFSET = 1;
    private static final int MIN_INDEX = 0;

    private final Scanner scanner;

    public Player(final Scanner scanner) {
        GameValidator.validateScannerNotNull(scanner);
        this.scanner = scanner;
    }

    // Week 2. Exception Handling
    @Override
    public boolean takeTurn(final GameState gameState, 
                            final PlayRule gameRule, 
                            final boolean isInitialTurn) {
        boolean validMove = false;

        while (!validMove) {
            final String input;
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
                        validMove = handleDrawTurn(gameState, gameRule);
                        continue; // Skip the rest and exit the loop
                    }
                    
                    // Has attackable cards with sufficient punishment, show defense options
                    displayDefenseOptions(gameState, accumulatedDraws);
                }

                input = readPlayerInput();

                if (isDrawInput(input)) {
                    validMove = handleDrawTurn(gameState, gameRule);
                } else {
                    validMove = handlePlayTurn(gameState, gameRule, isInitialTurn, input);
                }
            } catch (NumberFormatException e) {
                GameMessages.display(GameMessages.INVALID_INPUT_NUMBER_OR_DRAW);
                GameMessages.display(""); // Newline after error message
            } catch (IllegalArgumentException | InvalidMoveException e) {
                GameMessages.display(e.getMessage());
                GameMessages.display(""); // Newline after error message
            } catch (IndexOutOfBoundsException e) {
                GameMessages.displayFormatted(GameMessages.INVALID_INDEX_RANGE, gameState.getPlayerHand().size());
                GameMessages.display(""); // Newline after error message
            }
        }
        return true; // Turn completed successfully
    }
    
    @Override
    public Card playCard(final GameState gameState, 
                        final PlayRule gameRule, 
                        final boolean isInitialTurn) throws InvalidMoveException {
        final String input;
        final int index;
        final Card card;
        
        GameMessages.displayPrompt(GameMessages.SELECT_CARD_INDEX);
        input = scanner.nextLine();
        
        try {
            index = Integer.parseInt(input) - INDEX_OFFSET;
            
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

    private String readPlayerInput() {
        GameMessages.displayPrompt(GameMessages.SELECT_CARD_OR_DRAW);
        return scanner.nextLine();
    }

    private boolean isDrawInput(final String input) {
        return INPUT_DRAW.equals(input);
    }

    private boolean handleDrawTurn(final GameState gameState,
                                   final PlayRule gameRule) {
        final int accumulatedDraws;

        accumulatedDraws = gameRule.getAccumulatedDraws();

        if (accumulatedDraws > MIN_INDEX) {
            drawAccumulatedCards(gameState, gameRule, accumulatedDraws);
        } else {
            drawSingleCard(gameState);
        }

        return true; // Turn always ends after drawing
    }

    private boolean handlePlayTurn(final GameState gameState,
                                   final PlayRule gameRule,
                                   final boolean isInitialTurn,
                                   final String input) throws InvalidMoveException {
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
            // Prompt for new shape
            final String newShape = selectNewShape();
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
                                      final int accumulatedDraws) {
        GameMessages.displayFormatted(GameMessages.PLAYER_DREW_ACCUMULATED_CARDS, accumulatedDraws);
        
        for (int i = MIN_INDEX; i < accumulatedDraws; i++) {
            try {
                gameState.getPlayerHand().add(gameState.drawFromDeck());
            } catch (IllegalStateException e) {
                GameMessages.display(GameMessages.DECK_EMPTY_NO_MORE_CARDS);
                break;
            }
        }
        gameRule.resetAccumulatedDraws(); // Reset accumulated draws in PlayRule
    }

    private void drawSingleCard(final GameState gameState) {
        try {
            gameState.getPlayerHand().add(gameState.drawFromDeck());
            GameMessages.display(GameMessages.PLAYER_DREW_CARD);
        } catch (IllegalStateException e) {
            GameMessages.display(GameMessages.DECK_EMPTY_NO_CARD_DRAWN);
        }
    }

    private String selectNewShape() {
        while (true) {
            final String input;
            GameMessages.displayPrompt(GameMessages.SELECT_NEW_SHAPE);
            input = scanner.nextLine();
            
            switch (input) {
                case "1":
                    return Card.CARD_SHAPE_HEARTS;
                case "2":
                    return Card.CARD_SHAPE_DIAMONDS;
                case "3":
                    return Card.CARD_SHAPE_CLUBS;
                case "4":
                    return Card.CARD_SHAPE_SPADES;
                default:
                    GameMessages.display(GameMessages.INVALID_SHAPE_SELECTION);
                    GameMessages.display(""); // Newline after error
            }
        }
    }

    public static void getValidatedInput(final Scanner scanner, final String prompt) {
        while (true) {
            final String input;
            GameMessages.displayPrompt(prompt);
            input = scanner.nextLine();
            if (input.matches("\\d+")) { // Valid input: numbers (0 for draw, 1+ for card index)
                return;
            } else {
                GameMessages.display(GameMessages.INVALID_INPUT_CARD_INDEX_OR_DRAW);
            }
        }
    }
}
