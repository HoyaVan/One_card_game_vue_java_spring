import java.util.Scanner;

public class OneCardGame {

    public static void main(final String[] args) throws DeckEmptyException
    {
        Scanner scanner = new Scanner(System.in);

        DeckAndPlayerHands deckAndPlayerCards = new DeckAndPlayerHands();
        ActionCards gamePlay = new ActionCards();

        int cardsToDraw = 1;

        // Create participants
        GameParticipantable player = new Player(scanner);
        GameParticipantable ai = new AIPlayer();

        boolean isInitialTurn = true; // The first card starts as the "initial card"

        // Game loop
        while (!deckAndPlayerCards.isAIWinner() && !deckAndPlayerCards.isPlayerWinner())
        {
            System.out.println(deckAndPlayerCards);
            // Player's turn
            cardsToDraw = player.takeTurn(deckAndPlayerCards, gamePlay, cardsToDraw, isInitialTurn);
            // After the first card is played, it's no longer the initial card
            isInitialTurn = false;
            // Check if game is over after player's turn
            if (deckAndPlayerCards.isPlayerWinner()) break;
            // AI's turn
            cardsToDraw = ai.takeTurn(deckAndPlayerCards, gamePlay, cardsToDraw, isInitialTurn);
            // Check if game is over after AI's turn
            if (deckAndPlayerCards.isAIWinner() ) break;
        }

        deckAndPlayerCards.printFinalPlayerHands();

//      Week 8: Paths, Files, Scanner, Streaming
//      Save game state to a file at the end of the game
        deckAndPlayerCards.writeFinalHandsToFile("game_state.txt");

        scanner.close();
    }
}
