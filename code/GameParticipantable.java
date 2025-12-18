// Week 4: Interfaces
public interface GameParticipantable
{
    int takeTurn(DeckAndPlayerHands game, ActionCards gamePlay, int accumulatedDraws, boolean isInitialTurn);
}
