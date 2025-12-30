package onecardgame;

import java.util.Scanner;

import onecardgame.cards.Card;

public class OneCardGame {
    private static final String GAME_STATE_FILE = "game_state.txt";
    private final GameState gameState;
    private final PlayRule gameRule;
    private final Scanner scanner;
    private final GameParticipantable player;
    private final GameParticipantable ai;
    private final Dealer dealer;

    public OneCardGame() {
        this.gameRule = new PlayRule();
        this.gameState = new GameState();
        this.scanner = new Scanner(System.in);
        this.player = new Player(scanner);
        this.ai = new AIPlayer();
        this.dealer = new Dealer(player, ai);
    }

    /**
     * Starts and runs the game.
     */
    public void start() {
        // Set the initial card on the table
        try {
            Card initialCard = gameState.getDeck().drawCard();
            gameRule.setInitialCard(initialCard, gameState);
        } catch (IllegalStateException e) {
            GameMessages.displayErrorFormatted(GameMessages.CANNOT_START_GAME, e.getMessage());
            return;
        }

        // Game loop
        while (!gameState.isAIWinner() && !gameState.isPlayerWinner() &&
               !gameState.isAILoser() && !gameState.isPlayerLoser())
        {
            // Only show game state before player's turn (not before AI turn)
            if (dealer.getCurrentPlayerIndex() == 0) {
                GameMessages.display(gameState.toString());
            }
            
            // Execute next turn - dealer handles turn order and isInitialTurn
            boolean gameContinues = dealer.executeNextTurn(gameState, gameRule);
            
            if (!gameContinues) {
                break; // Game over
            }
        }
        
        gameState.printFinalPlayerHands();
        
        // Week 8: Paths, Files, Scanner, Streaming
        // Save game state to a file at the end of the game
        gameState.writeFinalHandsToFile(GAME_STATE_FILE);
    }

    /**
     * Entry point for the application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(final String[] args) {
        final OneCardGame game;
        
        game = new OneCardGame();
        game.start();
    }
}
