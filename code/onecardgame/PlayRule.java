package onecardgame;

import java.util.List;
import onecardgame.cards.*;

public class PlayRule {
    private static final String ATTACK_LABEL = " (Attack)";
    private static final String DEFEND_LABEL = " (Defend)";
    private static final int RESET_ACCUMULATED_DRAWS = 0;

    private final UsedCardPile usedCardPile;
    private int accumulatedDraws; // Tracks the number of accumulated draws

    public PlayRule() {
        this.usedCardPile = new UsedCardPile();
        this.accumulatedDraws = RESET_ACCUMULATED_DRAWS; // Initialize accumulated draws
    }

    public int getAccumulatedDraws() {
        return accumulatedDraws;
    }
    
    /**
     * Resets accumulated draws to 0. Should be called after a player draws cards.
     */
    public void resetAccumulatedDraws() {
        this.accumulatedDraws = RESET_ACCUMULATED_DRAWS;
    }

    /**
     * Sets the initial card on the table at the start of the game.
     * 
     * @param initialCard The card to set as the initial card
     * @param gameState The game state to update
     */
    public void setInitialCard(final Card initialCard, 
                            final GameState gameState) {
        if (initialCard != null) {
            usedCardPile.addCard(initialCard);
            gameState.setInitialCard(initialCard);
        }
    }

    // Add the playCard method to handle playing a card
    public void playCard(final Card card, final boolean isInitialTurn)
    {
        GameValidator.validateCardNotNull(card);
        
        final Card lastUsedCard;
        final boolean isDefenseMode;
        
        lastUsedCard = usedCardPile.getLastCard();
        isDefenseMode = isAttackCard(lastUsedCard);
        
        usedCardPile.addCard(card); // Add the card to the used cards pile

        if (!isInitialTurn) {
            // Check if this is a defense (attack card played after another attack card)
            if (isDefenseMode && isDefenseCard(card, lastUsedCard)) {
                // Defense successful - reset accumulated draws
                accumulatedDraws = RESET_ACCUMULATED_DRAWS;
                GameMessages.display(card + DEFEND_LABEL);
                GameMessages.display(GameMessages.DEFENDED);
            } else if (card instanceof Attackable attackable) {
                // Attack mode - accumulate draws (played after normal card or after opponent drew)
                int punishment = attackable.getPunishment();
                accumulatedDraws += punishment; // Track accumulated draws locally
                GameMessages.display(card + ATTACK_LABEL);
                GameMessages.displayFormatted(GameMessages.ACCUMULATED_DRAWS_INCREASED, punishment);
            }
            // Week 2: Inheritance, Polymorphism - demonstrated through toString() and interface methods
        }
    }
    
    private boolean isAttackCard(final Card card) {
        return card instanceof Attackable;
    }
    
    private boolean isDefenseCard(final Card card, final Card lastUsedCard) {
        if (card instanceof AceCard && lastUsedCard instanceof AceCard) {
            return card.getShape().equals(lastUsedCard.getShape());
        }
        if (card instanceof JokerCard) {
            return true; // Joker can defend against any attack
        }
        if (card instanceof NumTwoCard && lastUsedCard instanceof NumTwoCard) {
            return card.getNumber() == lastUsedCard.getNumber();
        }
        return false;
    }

    // Overloaded method to play a card from a hand by index
    public void playCard(final List<Card> hand, final int index) {
        final Card lastUsedCard;
        final Card cardToPlay;

        GameValidator.validateHandIndex(index, hand.size());

        cardToPlay = hand.get(index); // Get the card without removing it
        // Validate the card before playing
        try {
            lastUsedCard = usedCardPile.getLastCard();
            if (isCardPlayable(cardToPlay, lastUsedCard, false)) {
                hand.remove(index); // Remove the card only if it's valid
                playCard(cardToPlay, false); // Pass the necessary arguments
            } else {
                throw new InvalidMoveException(GameMessages.INVALID_MOVE_CARD_CANNOT_BE_PLAYED);
            }
        } catch (InvalidMoveException e) {
            GameMessages.displayFormatted(GameMessages.DEBUG_INVALID_MOVE, e.getMessage());
        }
    }

    public List<Card> getUsedCards() {
        return usedCardPile.getCards();
    }

    public Card getLastUsedCard() {
        // Return the last card from the used card pile
        return usedCardPile.getLastCard();
    }

    // Week 3: Final Method
    // Checks if two cards match (same number, or if either is a Joker)
    // Ace cards don't match by number
    public final boolean matches(final Card card1, final Card card2) {
        if (card1 == null || card2 == null) {
            return false;
        }
        if (card1 instanceof JokerCard || card2 instanceof JokerCard) {
            return true; // Joker always matches
        }
        // Check number match (but Ace doesn't match by number)
        if (!(card1 instanceof AceCard) && !(card2 instanceof AceCard)) {
            return card1.getNumber() == card2.getNumber();
        }
        return false;
    }

    public boolean isCardPlayable(final Card cardToPlay, 
                                 final Card lastUsedCard, 
                                 final boolean isInitialTurn) throws InvalidMoveException
    {
        GameMessages.displayFormatted(GameMessages.CHECKING_CARD_PLAYABLE, cardToPlay, lastUsedCard);
        GameMessages.displayFormatted(GameMessages.IS_INITIAL_TURN, isInitialTurn);
        if (lastUsedCard == null || isInitialTurn) {
            GameMessages.display(GameMessages.ANY_CARD_PLAYABLE);
            return true; // Any card is playable if no card is on the table
        }

        if (lastUsedCard instanceof Attackable) {
            if (cardToPlay instanceof NumTwoCard && 
                cardToPlay.getNumber() == lastUsedCard.getNumber()) {
                GameMessages.display(GameMessages.ATTACK_CARD_BLOCKS);
                return true; // Attack card blocks the same-number attack card
            }
            if (cardToPlay instanceof AceCard && 
                cardToPlay.getShape().equals(lastUsedCard.getShape())) {
                GameMessages.display(GameMessages.ACE_BLOCKS_ACE);
                return true; // Ace blocks another Ace with the same shape
            }
            if (cardToPlay instanceof JokerCard) {
                GameMessages.display(GameMessages.JOKER_BLOCKS_ATTACK);
                return true; // Joker blocks any attack
            }
            GameMessages.display(GameMessages.DEBUG_CARD_CANNOT_PROTECT);
            GameMessages.display(GameMessages.DEFEND_FAILED);
            throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
        }

        if (matches(cardToPlay, lastUsedCard)) {
            GameMessages.display(GameMessages.NORMAL_MOVE_VALID);
            return true; // Valid normal move
        }

        GameMessages.display(GameMessages.DEBUG_CARD_DOES_NOT_MATCH);
        throw new InvalidMoveException(GameMessages.INVALID_MOVE_CARD_DOES_NOT_MATCH);
    }

    // Find a playable card from the given hand
    public Card findPlayableCard(final List<Card> hand, 
                                final Card lastUsedCard, 
                                final boolean isInitialTurn) {
        return findPlayableCard(hand, 
                                lastUsedCard, 
                                isInitialTurn, 
                                Card.class);
    }

    // Week 6: Generics Methods
    // Find a playable card of a specific type from the given hand
    public <T extends Card> T findPlayableCard(final List<Card> hand, 
                                               final Card lastUsedCard, 
                                               final boolean isInitialTurn, 
                                               final Class<T> cardType) {
        return hand.stream()
                .filter(cardType::isInstance)
                .map(cardType::cast)
                .filter(card ->
                {
                    try {
                        return isCardPlayable(card, 
                                             lastUsedCard, 
                                             isInitialTurn);
                    } catch (InvalidMoveException e) {
                        GameMessages.displayFormatted(GameMessages.SKIPPING_CARD, card, e.getMessage());
                        return false; // Ignore invalid moves during this check
                    }
                })
                .findFirst() // Return the first playable card of the specified type
                .orElse(null); // Return null if no playable card is found
    }

    // Week 7: Functional Interfaces, Lambdas, Method References
    // Add to the list if the card is playable, return the list of playable cards
    public List<Card> getPlayableCards(final List<Card> hand, 
                                      final Card lastUsedCard, 
                                      final boolean isInitialTurn) {
        return getPlayableCards(hand, 
                                lastUsedCard, 
                                isInitialTurn, 
                                Card.class);
    }

    // Week 6: Generics Methods
    // Get all playable cards of a specific type from the given hand
    public <T extends Card> List<T> getPlayableCards(final List<Card> hand, 
                                                    final Card lastUsedCard, 
                                                    final boolean isInitialTurn, 
                                                    final Class<T> cardType) {
        GameValidator.validateCardNotNull(lastUsedCard);

        return hand.stream()
                .filter(cardType::isInstance)
                .map(cardType::cast)
                .filter(card -> {
                    try {
                        return isCardPlayable(card, 
                                             lastUsedCard, 
                                             isInitialTurn);
                    } catch (InvalidMoveException e) {
                        return false; // Ignore invalid cards
                    }
                })
                .toList();
    }


    public void playCardWithoutEffect(final Card card, 
                                    final GameState gameState) {
        GameValidator.validateCardNotNull(card);

        usedCardPile.addCard(card); // Add the card to the used card pile
        gameState.setLastUsedCard(card); // Update the last used card in game state
    }

    public int getPunishmentValue(final Card card) {
        if (card instanceof Attackable attackable) {
            return attackable.getPunishment();
        }
        return 0; // Default: no punishment
    }

}
