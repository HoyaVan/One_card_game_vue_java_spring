package onecardgame;

import onecardgame.cards.Card;

public class OneCardGame {

    private final GameState gameState;
    private final PlayRule gameRule;
    private final GameParticipantable player;
    private final GameParticipantable ai;
    private final Dealer dealer;

    public OneCardGame() {

        this.gameRule = new PlayRule();
        this.gameState = new GameState();
        this.player = new Player(null); 
        this.ai = new AIPlayer();
        this.dealer = new Dealer(player, ai);
    }

    public void setup() {
        // Step 1: Draw first card BEFORE dealing hands (this becomes the first used card)
        Card initialCard = gameState.drawFromDeck(); // Uses legacy method - deck is full, no reshuffle needed
        gameRule.setInitialCard(initialCard, gameState);
        
        // Step 2: Deal initial hands to players from remaining deck
        gameState.dealInitialCards();
        
        // First player must match the initial card, so isInitialTurn remains false
    }

    public boolean isGameOver() {
        // Game cannot be over if it hasn't been set up yet
        if (gameState.getLastUsedCard() == null) {
            return false;
        }
        return gameState.isAIWinner() || gameState.isPlayerWinner()
            || gameState.isAILoser()  || gameState.isPlayerLoser();
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public PlayRule getGameRule() {
        return gameRule;
    }
    
    public Dealer getDealer() {
        return dealer;
    }

    /**
     * Processes a player action (card play or draw).
     * - If action is a DRAW ("0"), the turn automatically ends and AI plays.
     * - If action is a CARD PLAY, the turn does NOT end yet - player must call endTurn().
     * Returns true if action was valid and executed, false otherwise.
     * 
     * @return true if action was valid, false if game is over
     */
    public boolean processPlayerAction(String action) {
        // Check if game has been set up (lastUsedCard must exist)
        if (gameState.getLastUsedCard() == null) {
            return false;
        }
        
        if (isGameOver()) {
            return false;
        }
        
        // Only process if it's player's turn (index 0)
        if (dealer.getCurrentPlayerIndex() != 0) {
            throw new IllegalStateException("Not player's turn");
        }
        
        // Normalize action: treat null, empty string, or strings that parse to 0 as draw action ("0")
        String normalizedAction;
        if (action == null || action.trim().isEmpty()) {
            normalizedAction = "0";
        } else {
            String trimmed = action.trim();
            // Check if the trimmed string represents zero (e.g., "0", "00", "000")
            try {
                if (Integer.parseInt(trimmed) == 0) {
                    normalizedAction = "0";
                } else {
                    normalizedAction = trimmed;
                }
            } catch (NumberFormatException e) {
                // Not a number, use as-is (could be "SHAPE:1" or other special formats)
                normalizedAction = trimmed;
            }
        }
        
        // Check if this is a draw action (drawing automatically ends turn)
        boolean isDrawAction = normalizedAction.equals("0");
        
        // Execute player's action
        player.takeTurn(gameState, gameRule, dealer, dealer.isInitialTurn(), normalizedAction);
        
        // If game ended immediately after player action, return false
        if (isGameOver()) {
            return false;
        }
        
        // If player drew a card, advance to AI turn (but don't execute AI turn yet)
        // Vue will call /ai-turn endpoint separately for step-by-step visualization
        if (isDrawAction) {
            // Advance to next player (AI)
            dealer.advanceToNextPlayer();
            
            // If game is over after player's turn, return false
            if (isGameOver()) {
                return false;
            }
            // Note: AI turn will be executed via separate /ai-turn endpoint for visualization
        }
        
        return !isGameOver();
    }
    
    /**
     * Ends the current player's turn and executes AI turn if game continues.
     * This should be called after processPlayerAction() when player presses "End Turn" button.
     * Returns true if game continues, false if game is over.
     */
    public boolean endTurn() {
        // Check if game has been set up (lastUsedCard must exist)
        if (gameState.getLastUsedCard() == null) {
            throw new IllegalStateException("Cannot end turn - game not set up");
        }
        
        if (isGameOver()) {
            return false;
        }
        
        // Only allow ending turn if it's currently player's turn
        if (dealer.getCurrentPlayerIndex() != 0) {
            throw new IllegalStateException("Cannot end turn - not player's turn");
        }
        
        // Advance to next player (AI)
        dealer.advanceToNextPlayer();
        
        // If game is over after player's turn, return false
        if (isGameOver()) {
            return false;
        }
        
        // Note: AI turn will be executed via separate /ai-turn endpoint for step-by-step visualization
        // This allows Vue to animate: "Player turn ended" → "AI thinking" → "AI action"
        
        return !isGameOver();
    }
    
    /**
     * Executes one step of the game (for turn-based gameplay).
     * Used by REST API for step-by-step game progression.
     */
    public boolean step() {
        if (isGameOver()) {
            return false;
        }
        
        // If it's player's turn, wait for action from REST API
        // This method is mainly for AI turns
        if (dealer.getCurrentPlayerIndex() == 1) {
            dealer.executeNextTurn(gameState, gameRule, null);
        }
        
        return !isGameOver();
    }
}
