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
     * @param gameState   The game state to update
     */
    public void setInitialCard(final Card initialCard,
            final GameState gameState) {
        if (initialCard != null) {
            usedCardPile.addCard(initialCard);
            gameState.setInitialCard(initialCard);
        }
    }

    // Add the playCard method to handle playing a card
    public void playCard(final Card card, final boolean isInitialTurn) {
        GameValidator.validateCardNotNull(card);

        final Card lastUsedCard;
        final boolean isDefenseMode;

        lastUsedCard = usedCardPile.getLastCard();
        // Defense mode: last card is attack card AND there are accumulated draws
        // (active attack)
        // This prevents ping-pong scenarios where defense resets draws but next attack
        // accumulates incorrectly
        isDefenseMode = isAttackCardPrivate(lastUsedCard) && accumulatedDraws > MIN_INDEX;

        usedCardPile.addCard(card); // Add the card to the used cards pile

        if (!isInitialTurn) {
            // Check if this is a defense (attack card played after another attack card
            // while under attack)
            if (isDefenseMode && isDefenseCardPrivate(card, lastUsedCard)) {
                // Defense successful - reset accumulated draws is NOT needed if we want
                // stacking,
                // but for now we just acknowledge the defense.
                // The next block will handle adding the new card's punishment.
                GameMessages.display(GameMessages.DEFENDED);
            }

            if (card instanceof Attackable attackable) {
                // Attack mode - accumulate draws.
                // This applies to both direct attacks and defensive counter-attacks.
                int punishment = attackable.getPunishment();
                accumulatedDraws += punishment;
            }
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
     * Public method to check if a card played would be a defense against the last
     * card.
     * Used by controllers to determine event types for visualization.
     */
    public boolean isDefenseCard(final Card card, final Card lastUsedCard) {
        if (lastUsedCard == null || !isAttackCard(lastUsedCard)) {
            return false;
        }
        return isDefenseCardPrivate(card, lastUsedCard);
    }

    private boolean isDefenseCardPrivate(final Card card, final Card lastUsedCard) {
        // Both cards must be Attackable
        if (!(card instanceof Attackable defendingCard) || !(lastUsedCard instanceof Attackable attackingCard)) {
            return false;
        }

        // Check punishment hierarchy: defending card must have >= punishment value
        // Hierarchy: 2 (punishment 2) < Ace (punishment 3) < Joker (punishment 5)
        // Same value cards can defend against each other (e.g., Ace vs Ace, 2 vs 2,
        // Joker vs Joker)
        final int attackingPunishment = attackingCard.getPunishment();
        final int defendingPunishment = defendingCard.getPunishment();

        // Defending card must have equal or higher punishment value
        if (defendingPunishment < attackingPunishment) {
            return false; // Lower value card cannot defend against higher value attack
        }

        // Now check specific card type rules (only if punishment is sufficient)
        // NumTwoCard defending: same rank AND same shape required (punishment 2)
        // Can defend against NumTwoCard (same value) with same rank and shape
        if (card instanceof NumTwoCard && lastUsedCard instanceof NumTwoCard) {
            return card.getRank() == lastUsedCard.getRank() &&
                    card.getShape().equals(lastUsedCard.getShape());
        }

        // AceCard defending: same shape required (punishment 3)
        // Can defend against NumTwoCard (if same shape) or AceCard (same value, if same
        // shape)
        if (card instanceof AceCard) {
            // Ace can defend against NumTwo (if same shape) or Ace (same value, if same
            // shape)
            if (lastUsedCard instanceof NumTwoCard || lastUsedCard instanceof AceCard) {
                return card.getShape().equals(lastUsedCard.getShape());
            }
            // Ace cannot defend against Joker (punishment 3 < 5)
            return false;
        }

        // JokerCard: can defend against anything (punishment 5 >= any other)
        // Can defend against 2, Ace, or Joker (same value)
        if (card instanceof JokerCard) {
            return true; // Joker can defend against any attack (2, Ace, or Joker)
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
     * Plays a NumSevenCard with a new shape. Used when the shape-change effect is
     * applied.
     * 
     * @param numSevenCard  The NumSevenCard to play
     * @param newShape      The new shape to apply
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
    // Checks if two cards match (same number OR same shape, or if either is a
    // Joker)
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
            final boolean isInitialTurn) throws InvalidMoveException {
        // GameMessages.displayFormatted(GameMessages.CHECKING_CARD_PLAYABLE,
        // cardToPlay, lastUsedCard);
        // GameMessages.displayFormatted(GameMessages.IS_INITIAL_TURN, isInitialTurn);
        if (lastUsedCard == null) {
            // GameMessages.display(GameMessages.ANY_CARD_PLAYABLE);
            return true; // Any card is playable if no card is on the table
        }

        // On initial turn, card must match the initial card by shape or rank
        if (isInitialTurn) {
            return matches(cardToPlay, lastUsedCard);
        }

        // NOTE: Face card effect is NOT handled here in isCardPlayable()
        // The face card "one more turn" effect is handled differently:
        // - When PLAYER plays a face card: Their turn is kept
        // (OneCardGame.processPlayerAction)
        // and they can play any card during that extra turn (handled by
        // frontend/backend turn management)
        // - When AI plays a face card: handleFaceCardEffect() immediately plays another
        // card
        // during AI's turn, so by the time it's player's turn, the face card is just a
        // normal card
        // - The face card effect ONLY benefits the player who played it, NOT the
        // opponent
        // - Therefore, we do NOT allow any card to be played just because a face card
        // is on the table
        // The opponent must follow normal matching rules

        // IMPORTANT: Check defense rules FIRST if under attack (accumulatedDraws > 0)
        // This must come BEFORE the Joker check, because when under attack by a Joker,
        // only valid defense cards (Joker or higher) can be played, not ANY card
        // After drawing accumulated cards, accumulatedDraws is reset to 0, so normal
        // matching cards are allowed
        if (lastUsedCard instanceof Attackable attackingCard && accumulatedDraws > MIN_INDEX) {
            // Defense card must be Attackable and have punishment >= attacking card's
            // punishment
            if (!(cardToPlay instanceof Attackable defendingCard)) {
                throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
            }

            final int attackingPunishment = attackingCard.getPunishment();
            final int defendingPunishment = defendingCard.getPunishment();

            // Check punishment hierarchy: defending card must have >= punishment value
            // Hierarchy: 2 (punishment 2) < Ace (punishment 3) < Joker (punishment 5)
            if (defendingPunishment < attackingPunishment) {
                throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
            }

            // Now check specific card type rules (only if punishment is sufficient)
            // NumTwoCard defending: same rank AND same shape required (punishment 2)
            if (cardToPlay instanceof NumTwoCard &&
                    lastUsedCard instanceof NumTwoCard &&
                    cardToPlay.getRank() == lastUsedCard.getRank() &&
                    cardToPlay.getShape().equals(lastUsedCard.getShape())) {
                return true; // NumTwo blocks the same-number AND same-shape attack card (punishment >=
                             // required)
            }

            // AceCard defending: same shape required (punishment 3, can defend against
            // NumTwoCard or AceCard)
            if (cardToPlay instanceof AceCard) {
                // Ace can defend against NumTwo (if same shape) or Ace (if same shape)
                // But NOT Joker (punishment check above already prevents this)
                if (lastUsedCard instanceof NumTwoCard || lastUsedCard instanceof AceCard) {
                    return cardToPlay.getShape().equals(lastUsedCard.getShape());
                }
                // If lastUsedCard is Joker, punishment check above should have failed, but be
                // explicit
                throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
            }

            // JokerCard: can defend against anything (punishment 5 >= any other)
            if (cardToPlay instanceof JokerCard) {
                return true; // Joker blocks any attack (2, Ace, or Joker)
            }

            // If we get here, the card type doesn't match any defense rules
            throw new InvalidMoveException(GameMessages.INVALID_MOVE_CANNOT_PROTECT);
        }

        // Joker on table:
        // If under attack (accumulatedDraws > 0), defense rules above would have been
        // checked and returned/throw.
        // If NOT under attack (accumulatedDraws == 0), the Joker acts as a wild card
        // (matches everything).
        // Therefore, we do NOT restrict plays to only JokerCards here.
        // The matches() method below handles the "Joker matches anything" logic.

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
                .filter(card -> {
                    try {
                        return isCardPlayable(card,
                                lastUsedCard,
                                isInitialTurn);
                    } catch (InvalidMoveException e) {
                        // GameMessages.displayFormatted(GameMessages.SKIPPING_CARD, card,
                        // e.getMessage());
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
