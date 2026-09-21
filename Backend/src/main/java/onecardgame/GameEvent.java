package onecardgame;

import onecardgame.cards.Card;

/**
 * Represents a single game event/action that happened during a turn.
 * Used to provide step-by-step information to Vue frontend for animations.
 */
public record GameEvent(
    EventType type,
    String actor,  // "PLAYER" or "AI"
    String description,
    Card cardPlayed,  // null if no card was played
    Integer cardsDrawn,  // number of cards drawn, null if none
    Integer accumulatedDraws  // current accumulated draws after this event
) {
    public enum EventType {
        CARD_PLAYED,           // Normal card played
        ATTACK_CARD_PLAYED,    // Attack card played (NumTwo, Ace, Joker) - show arrow/line
        DEFENSE_CARD_PLAYED,   // Defense card played - show shield
        CARD_DRAWN,
        MULTIPLE_CARDS_PLAYED,
        TURN_STARTED,
        TURN_ENDED,
        TURN_KEPT,             // Turn was kept (e.g., after face card) - show transition animation
        GAME_OVER,
        CARDS_DEALT,           // Dealer dealt cards to players
        INITIAL_CARD_PLACED,    // Dealer placed initial card on table
        GAME_SETUP_COMPLETE,   // Game setup finished, ready to play
        NUMSEVEN_SHAPE_SELECTION_REQUIRED  // NumSevenCard played - player must select a shape
    }
    
    // Generic methods that work for both PLAYER and AI
    public static GameEvent playedCard(String actor, Card card, int accumulatedDraws) {
        String actorName = actor.equals("PLAYER") ? "Player" : "AI";
        return new GameEvent(
            EventType.CARD_PLAYED,
            actor,
            actorName + " played " + card,
            card,
            null,
            accumulatedDraws
        );
    }
    
    public static GameEvent drewCard(String actor, int cardsDrawn, int accumulatedDraws) {
        String actorName = actor.equals("PLAYER") ? "Player" : "AI";
        return new GameEvent(
            EventType.CARD_DRAWN,
            actor,
            actorName + " drew " + cardsDrawn + " card(s)",
            null,
            cardsDrawn,
            accumulatedDraws
        );
    }
    
    public static GameEvent playedMultipleCards(String actor, int count, int accumulatedDraws) {
        String actorName = actor.equals("PLAYER") ? "Player" : "AI";
        return new GameEvent(
            EventType.MULTIPLE_CARDS_PLAYED,
            actor,
            actorName + " played " + count + " cards",
            null,
            null,
            accumulatedDraws
        );
    }
    
    // Convenience methods for backward compatibility (can be removed later)
    public static GameEvent playerPlayedCard(Card card, int accumulatedDraws) {
        return playedCard("PLAYER", card, accumulatedDraws);
    }
    
    public static GameEvent playerDrewCard(int cardsDrawn, int accumulatedDraws) {
        return drewCard("PLAYER", cardsDrawn, accumulatedDraws);
    }
    
    public static GameEvent playerPlayedMultipleCards(int count, int accumulatedDraws) {
        return playedMultipleCards("PLAYER", count, accumulatedDraws);
    }
    
    public static GameEvent aiPlayedCard(Card card, int accumulatedDraws) {
        return playedCard("AI", card, accumulatedDraws);
    }
    
    public static GameEvent aiDrewCard(int cardsDrawn, int accumulatedDraws) {
        return drewCard("AI", cardsDrawn, accumulatedDraws);
    }
    
    public static GameEvent turnStarted(String actor) {
        return new GameEvent(
            EventType.TURN_STARTED,
            actor,
            actor + "'s turn started",
            null,
            null,
            null
        );
    }
    
    public static GameEvent turnEnded(String actor) {
        return new GameEvent(
            EventType.TURN_ENDED,
            actor,
            actor + "'s turn ended",
            null,
            null,
            null
        );
    }
    
    public static GameEvent turnKept(String actor) {
        return new GameEvent(
            EventType.TURN_KEPT,
            actor,
            actor + "'s turn continues",
            null,
            null,
            null
        );
    }
    
    public static GameEvent gameOver(String winner) {
        return gameOver(winner, null);
    }

    public static GameEvent gameOver(String winner, String reason) {
        return new GameEvent(
            EventType.GAME_OVER,
            winner,
            "Game over! Winner: " + winner + (reason != null ? "; Reason: " + reason : ""),
            null,
            null,
            null
        );
    }
    
    // Dealer events (for setup and dealing animations)
    public static GameEvent cardsDealt(int playerCards, int aiCards) {
        return new GameEvent(
            EventType.CARDS_DEALT,
            "DEALER",
            "Dealer dealt " + playerCards + " cards to player and " + aiCards + " cards to AI",
            null,
            playerCards + aiCards, // Total cards dealt
            null
        );
    }
    
    public static GameEvent initialCardPlaced(Card card) {
        return new GameEvent(
            EventType.INITIAL_CARD_PLACED,
            "DEALER",
            "Dealer placed initial card: " + card,
            card,
            null,
            null
        );
    }
    
    public static GameEvent gameSetupComplete() {
        return new GameEvent(
            EventType.GAME_SETUP_COMPLETE,
            "DEALER",
            "Game setup complete. Ready to play!",
            null,
            null,
            null
        );
    }
    
    // Attack card events (for arrow/line visualization) - generic for both PLAYER and AI
    public static GameEvent playedAttackCard(String actor, Card card, int punishment, int accumulatedDraws) {
        String actorName = actor.equals("PLAYER") ? "Player" : "AI";
        return new GameEvent(
            EventType.ATTACK_CARD_PLAYED,
            actor,
            actorName + " attacked with " + card + " (punishment: " + punishment + ")",
            card,
            null,
            accumulatedDraws
        );
    }
    
    // Defense card events (for shield visualization) - generic for both PLAYER and AI
    public static GameEvent playedDefenseCard(String actor, Card defendingCard, Card attackingCard, int accumulatedDraws) {
        String actorName = actor.equals("PLAYER") ? "Player" : "AI";
        return new GameEvent(
            EventType.DEFENSE_CARD_PLAYED,
            actor,
            actorName + " defended against " + attackingCard + " with " + defendingCard,
            defendingCard,
            null,
            accumulatedDraws
        );
    }
    
    // Convenience methods for backward compatibility (can be removed later)
    public static GameEvent playerPlayedAttackCard(Card card, int punishment, int accumulatedDraws) {
        return playedAttackCard("PLAYER", card, punishment, accumulatedDraws);
    }
    
    public static GameEvent aiPlayedAttackCard(Card card, int punishment, int accumulatedDraws) {
        return playedAttackCard("AI", card, punishment, accumulatedDraws);
    }
    
    public static GameEvent playerPlayedDefenseCard(Card defendingCard, Card attackingCard, int accumulatedDraws) {
        return playedDefenseCard("PLAYER", defendingCard, attackingCard, accumulatedDraws);
    }
    
    public static GameEvent aiPlayedDefenseCard(Card defendingCard, Card attackingCard, int accumulatedDraws) {
        return playedDefenseCard("AI", defendingCard, attackingCard, accumulatedDraws);
    }
    
    // NumSevenCard shape selection event
    public static GameEvent numSevenShapeSelectionRequired(Card numSevenCard) {
        return new GameEvent(
            EventType.NUMSEVEN_SHAPE_SELECTION_REQUIRED,
            "PLAYER",
            "Select a shape for the 7 card",
            numSevenCard,
            null,
            null
        );
    }
}

