package onecardgame;

import java.util.List;

import onecardgame.cards.Card;
import onecardgame.cards.FaceCard;

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
     * - If action is a CARD PLAY, the turn automatically ends and AI plays.
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
        
        // Store last card before action to detect if face card was just played
        Card lastCardBefore = gameState.getLastUsedCard();
        
        // Execute player's action
        player.takeTurn(gameState, gameRule, dealer, dealer.isInitialTurn(), normalizedAction);
        
        // If game ended immediately after player action, return false
        if (isGameOver()) {
            return false;
        }
        
        // Check if a face card was just played (last card changed and is now a face card)
        // and player has no playable cards - must draw automatically but keep turn
        Card lastCardAfter = gameState.getLastUsedCard();
        boolean shouldKeepTurn = false;
        if (lastCardAfter != null && lastCardAfter != lastCardBefore && 
            lastCardAfter instanceof FaceCard) {
            // Face card was just played - check if player has any playable cards
            List<Card> playableCards = gameRule.getPlayableCards(
                gameState.getPlayerHand(), 
                lastCardAfter, 
                false // Not initial turn anymore
            );
            
            // If no playable cards, automatically draw a card but keep player's turn
            if (playableCards.isEmpty()) {
                // Player must draw a card after face card if no playable cards
                // But they get one more turn (no transition to AI)
                player.takeTurn(gameState, gameRule, dealer, false, "0");
                
                // Check if game ended after drawing
                if (isGameOver()) {
                    return false;
                }
                
                // Keep player's turn - don't advance to AI
                shouldKeepTurn = true;
            }
        }
        
        // After any action (draw or play cards), automatically advance to AI turn
        // UNLESS face card was played and player had to draw (then keep player's turn)
        if (!shouldKeepTurn) {
            // Vue will call /ai-turn endpoint separately for step-by-step visualization
            dealer.advanceToNextPlayer();
        }
        
        // If game is over after player's turn, return false
        if (isGameOver()) {
            return false;
        }
        // Note: AI turn will be executed via separate /ai-turn endpoint for visualization
        
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
