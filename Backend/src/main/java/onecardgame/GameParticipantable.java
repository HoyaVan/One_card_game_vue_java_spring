package onecardgame;

import onecardgame.cards.Card;

// Week 4: Interfaces
public interface GameParticipantable
{
    /**
     * Takes a turn in the game.
     * 
     * @param gameState The game state containing hands and deck
     * @param gameRule The play rules manager (contains accumulated draws state)
     * @param dealer The dealer managing card drawing and game flow
     * @param isInitialTurn Whether this is the initial turn of the game
     * @param action The action from the player (card index as string, "0" for draw, or shape selection)
     * @return true if the turn ended normally, false if the game should continue
     */
    boolean takeTurn(
        final GameState gameState, 
        final PlayRule gameRule,
        final Dealer dealer,
        final boolean isInitialTurn,
        final String action
    );
    
    /**
     * Plays a card from the participant's hand.
     * Each participant implements their own logic for selecting and playing a card.
     * 
     * @param gameState The game state containing hands and deck
     * @param gameRule The game rules manager for card validation and state updates
     * @param isInitialTurn Whether this is the initial turn of the game
     * @return The card that was played, or null if no card was played
     * @throws InvalidMoveException if the selected card cannot be played
     */
    Card playCard(
        final GameState gameState, 
        final PlayRule gameRule, 
        final boolean isInitialTurn
    ) throws InvalidMoveException;
}
