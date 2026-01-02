package onecardgame;

import java.util.List;

import onecardgame.cards.AceCard;
import onecardgame.cards.Attackable;
import onecardgame.cards.Card;
import onecardgame.cards.JokerCard;
import onecardgame.cards.NumSevenCard;
import onecardgame.cards.NumTwoCard;

public class PlayRule {
    private static final int MIN_INDEX = 0;
    private static final String ATTACK_LABEL = " (Attack)";
    private static final String DEFEND_LABEL = " (Defend)";
    private static final int RESET_ACCUMULATED_DRAWS = 0;

    private final UsedCardPile usedCardPile;
    private int accumulatedDraws; // Tracks the number of accumulated draws

    public PlayRule() {
        this.usedCardPile = new UsedCardPile();
        this.accumulatedDraws = RESET_ACCUMULATED_DRAWS; // Initialize accumulated draws
    }
    
    /**
     * Gets all cards from used pile except the last one (for reshuffling).
     * The last card must remain as the lastUsedCard.
     * 
     * @return List of cards to reshuffle (excluding the last card)
     */
    public List<Card> getUsedCardPileCardsExceptLast() {
        List<Card> allCards = usedCardPile.getCards();
        if (allCards.size() <= 1) {
            return new java.util.ArrayList<>(); // No cards to reshuffle (only last card or empty)
        }
        // Return all cards except the last one
        return new java.util.ArrayList<>(allCards.subList(0, allCards.size() - 1));
    }
    
    /**
     * Removes all cards from used pile except the last one.
     * Used after reshuffling to keep only the lastUsedCard in the pile.
     */
    public void removeCardsFromPileExceptLast() {
        Card lastCard = usedCardPile.getLastCard();
        usedCardPile.clear();
        if (lastCard != null) {
            usedCardPile.addCard(lastCard); // Keep only the last card
        }
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
        isDefenseMode = isAttackCardPrivate(lastUsedCard);
        
        usedCardPile.addCard(card); // Add the card to the used cards pile

        if (!isInitialTurn) {
            // Check if this is a defense (attack card played after another attack card)
            if (isDefenseMode && isDefenseCardPrivate(card, lastUsedCard)) {
                // Defense successful - reset accumulated draws
                accumulatedDraws = RESET_ACCUMULATED_DRAWS;
                // Only display "Defended!" message (removed card + DEFEND_LABEL for cleaner output)
                GameMessages.display(GameMessages.DEFENDED);
            } else if (card instanceof Attackable attackable) {
                // Attack mode - accumulate draws (played after normal card or after opponent drew)
                int punishment = attackable.getPunishment();
                accumulatedDraws += punishment; // Track accumulated draws locally
                // GameMessages.display(card + ATTACK_LABEL); // Removed: don't show "(Attack)" label
                // GameMessages.displayFormatted(GameMessages.ACCUMULATED_DRAWS_INCREASED, punishment); // Commented out debug
            }
            // Week 2: Inheritance, Polymorphism - demonstrated through toString() and interface methods
        }
    }
    
    /**
     * Public method to check if a card is an attack card.
     * Used by controllers to determine event types for visualization.
     */
    public boolean isAttackCard(final Card card) {
        return card instanceof Attackable;
    }
    
    private boolean isAttackCardPrivate(final Card card) {
        return isAttackCard(card);
    }
    
    /**
     * Public method to check if the last played card was an attack card.
     * Used to determine if the next card played will be a defense.
     */
    public boolean wasLastCardAttack() {
        Card lastCard = getLastUsedCard();
        return lastCard != null && isAttackCard(lastCard) && accumulatedDraws > 0;
    }
    
    /**
     * Public method to check if a card played would be a defense against the last card.
     * Used by controllers to determine event types for visualization.
     */
    public boolean isDefenseCard(final Card card, final Card lastUsedCard) {
        if (lastUsedCard == null || !isAttackCard(lastUsedCard)) {
            return false;
        }
        return isDefenseCardPrivate(card, lastUsedCard);
    }
    
    private boolean isDefenseCardPrivate(final Card card, final Card lastUsedCard) {
        if (card instanceof AceCard && lastUsedCard instanceof AceCard) {
            return card.getShape().equals(lastUsedCard.getShape());
        }
        if (card instanceof JokerCard) {
            return true; // Joker can defend against any attack (including another Joker)
        }
        if (card instanceof NumTwoCard && lastUsedCard instanceof NumTwoCard) {
            // NumTwoCard defense requires both same rank AND same shape
            return card.getRank() == lastUsedCard.getRank() && 
                   card.getShape().equals(lastUsedCard.getShape());
        }
        return false;
    }

    // Overloaded method to play a card from a hand by index
    public void playCard(final List<Card> hand, final int index) throws InvalidMoveException {
        final Card cardToPlay;

        GameValidator.validateHandIndex(index, hand.size());

        cardToPlay = hand.get(index); // Get the card without removing it
        hand.remove(index); // Remove the card from hand
        
        playCard(cardToPlay, false); // Play the card (isInitialTurn is false since validation already happened)
    }
    
    /**
     * Plays a NumSevenCard with a new shape. Used when the shape-change effect is applied.
     * 
     * @param numSevenCard The NumSevenCard to play
     * @param newShape The new shape to apply
     * @param isInitialTurn Whether this is the initial turn
     */
    public void playNumSevenCard(final NumSevenCard numSevenCard, final String newShape, final boolean isInitialTurn) {
        final NumSevenCard cardWithNewShape = numSevenCard.withShape(newShape);
        playCard(cardWithNewShape, isInitialTurn);
    }

    public List<Card> getUsedCards() {
        return usedCardPile.getCards();
    }

    public Card getLastUsedCard() {
        // Return the last card from the used card pile
        return usedCardPile.getLastCard();
    }

    // Week 3: Final Method
    // Checks if two cards match (same number OR same shape, or if either is a Joker)
    // Ace cards match each other regardless of shape
    public final boolean matches(final Card card1, final Card card2) {
        if (card1 == null || card2 == null) {
            return false;
        }
        if (card1 instanceof JokerCard || card2 instanceof JokerCard) {
            return true; // Joker always matches
        }
        // Ace cards match each other regardless of shape
        if (card1 instanceof AceCard && card2 instanceof AceCard) {
            return true; // Any Ace matches any Ace
        }
        // Check shape match (same suit)
        if (card1.getShape().equals(card2.getShape())) {
            return true;
        }
        // Check rank match (but Ace doesn't match by rank with non-Ace cards)
        if (!(card1 instanceof AceCard) && !(card2 instanceof AceCard)) {
            return card1.getRank() == card2.getRank();
        }
        return false;
    }

    public boolean isCardPlayable(final Card cardToPlay, 
                                 final Card lastUsedCard, 
                                 final boolean isInitialTurn) throws InvalidMoveException
    {
        // GameMessages.displayFormatted(GameMessages.CHECKING_CARD_PLAYABLE, cardToPlay, lastUsedCard);
        // GameMessages.displayFormatted(GameMessages.IS_INITIAL_TURN, isInitialTurn);
        if (lastUsedCard == null || isInitialTurn) {
            // GameMessages.display(GameMessages.ANY_CARD_PLAYABLE);
            return true; // Any card is playable if no card is on the table
        }

        // Joker on table: any card can be played (matching rules don't apply)
        if (lastUsedCard instanceof JokerCard) {
            return true; // Any card is playable on a Joker
        }

        // Only require defense if we're currently under attack (accumulatedDraws > 0)
        // After drawing accumulated cards, accumulatedDraws is reset to 0, so normal matching cards are allowed
        if (lastUsedCard instanceof Attackable attackingCard && accumulatedDraws > MIN_INDEX) {
            // Defense card must be Attackable and have punishment >= attacking card's punishment
            if (!(cardToPlay instanceof Attackable defendingCard)) {
                throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
            }
            
            final int attackingPunishment = attackingCard.getPunishment();
            final int defendingPunishment = defendingCard.getPunishment();
            
            // Check punishment value: defending card must have >= punishment value
            if (defendingPunishment < attackingPunishment) {
                throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
            }
            
            // NumTwoCard defending: same rank required (punishment 2)
            if (cardToPlay instanceof NumTwoCard && 
                cardToPlay.getRank() == lastUsedCard.getRank()) {
                // GameMessages.display(GameMessages.ATTACK_CARD_BLOCKS);
                return true; // NumTwo blocks the same-number attack card (punishment >= required)
            }
            // AceCard defending: same shape required (punishment 3, can defend against NumTwoCard or AceCard)
            if (cardToPlay instanceof AceCard && 
                cardToPlay.getShape().equals(lastUsedCard.getShape())) {
                // GameMessages.display(GameMessages.ACE_BLOCKS_ACE);
                return true; // Ace blocks with same shape (punishment 3 >= 2 or 3)
            }
            // JokerCard: always works (punishment 5, can defend against anything)
            if (cardToPlay instanceof JokerCard) {
                // GameMessages.display(GameMessages.JOKER_BLOCKS_ATTACK);
                return true; // Joker blocks any attack (punishment 5 >= any other)
            }
            // GameMessages.display(GameMessages.DEBUG_CARD_CANNOT_PROTECT);
            // GameMessages.display(GameMessages.DEFEND_FAILED);
            throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
        }

        if (matches(cardToPlay, lastUsedCard)) {
            // GameMessages.display(GameMessages.NORMAL_MOVE_VALID);
            return true; // Valid normal move
        }

        // GameMessages.display(GameMessages.DEBUG_CARD_DOES_NOT_MATCH);
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
                        // GameMessages.displayFormatted(GameMessages.SKIPPING_CARD, card, e.getMessage());
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
