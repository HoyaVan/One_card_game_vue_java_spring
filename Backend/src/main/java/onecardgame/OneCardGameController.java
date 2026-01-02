package onecardgame;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import onecardgame.cards.Card;

@RestController
@RequestMapping("/games/onecard")
public class OneCardGameController {

    private final OneCardGame game = new OneCardGame();

    public record ActionRequest(@NotBlank String action) {}
    
    public record GameResponse(
            GameStateDTO gameState,
            boolean ok,
            String message,
            java.util.List<GameEvent> events  // Events that happened in this turn (for Vue animations)
    ) {
        public GameResponse(GameStateDTO gameState, boolean ok, String message) {
            this(gameState, ok, message, java.util.Collections.emptyList());
        }
    }

    @PostMapping("/setup")
    public ResponseEntity<GameResponse> setup() {
        GameState gameState = game.getGameState();
        
        // Create events for dealer actions (setup sequence)
        java.util.List<GameEvent> events = new java.util.ArrayList<>();
        
        // Event 1: Cards dealt (happens in GameState constructor - 7 cards to each player)
        int playerCards = gameState.getPlayerHand().size();
        int aiCards = gameState.getAiHand().size();
        events.add(GameEvent.cardsDealt(playerCards, aiCards));
        
        // Event 2: Initial card placed (happens in setup())
        game.setup();
        Card initialCard = gameState.getLastUsedCard();
        if (initialCard != null) {
            events.add(GameEvent.initialCardPlaced(initialCard));
        }
        
        // Event 3: Setup complete
        events.add(GameEvent.gameSetupComplete());
        
        // Event 4: Player's turn starts
        events.add(GameEvent.turnStarted("PLAYER"));
        
        GameStateDTO dto = GameStateDTO.fromGameState(
            gameState, 
            game.getGameRule(), 
            game.getDealer()
        );
        return ResponseEntity.ok(new GameResponse(dto, true, "Game setup complete", events));
    }

    /**
     * Processes a player action.
     * Action formats:
     * - "0" - Draw a card (turn ends automatically, AI plays)
     * - "1" - Play single card at index 1 (turn does NOT end, press "End Turn" button)
     * - "1,2,3" - Play multiple cards at indices 1,2,3 if they match by rank (turn does NOT end)
     *   - Supports: any numbers, J (Jack), Q (Queen), K (King), Ace, Joker
     *   - All cards must have the same rank (number or face value) to be dropped together
     *   - If multiple Attackable cards are dropped, punishment accumulates:
     *     * NumTwoCard: 2+2+2=6 draws for 3 cards
     *     * AceCard: 3+3+3=9 draws for 3 cards
     *     * JokerCard: 5+5+5=15 draws for 3 cards
     * - "SHAPE:1" - Select shape for NumSevenCard
     * 
     * Vue frontend sends: 
     * - { "action": "1" } to play card at index 1
     * - { "action": "1,2,3" } to drop multiple matching cards
     * - { "action": "0" } to draw
     */
    @PostMapping("/play")
    public ResponseEntity<GameResponse> play(@Valid @RequestBody ActionRequest request) {
        try {
            String action = request.action().trim();
            boolean isDraw = action.equals("0");
            
            // Store state before action to detect what happened
            Card lastCardBefore = game.getGameState().getLastUsedCard();
            int playerHandSizeBefore = game.getGameState().getPlayerHand().size();
            int accumulatedDrawsBefore = game.getGameRule().getAccumulatedDraws();
            boolean wasUnderAttack = game.getGameRule().wasLastCardAttack();
            
            boolean valid = game.processPlayerAction(action);
            
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            
            // Build events list for Vue to animate
            java.util.List<GameEvent> events = new java.util.ArrayList<>();
            
            if (!valid || game.isGameOver()) {
                String winner = dto.winner();
                String message = winner != null ? "Game over! Winner: " + winner : "Game over!";
                events.add(GameEvent.gameOver(winner != null ? winner : "UNKNOWN"));
                return ResponseEntity.ok(new GameResponse(dto, false, message, events));
            }
            
            // Detect what happened: card played or drawn
            if (isDraw) {
                int cardsDrawn = playerHandSizeBefore < game.getGameState().getPlayerHand().size() 
                    ? game.getGameState().getPlayerHand().size() - playerHandSizeBefore 
                    : 1; // Default to 1 if can't detect
                events.add(GameEvent.drewCard("PLAYER", cardsDrawn, game.getGameRule().getAccumulatedDraws()));
                events.add(GameEvent.turnEnded("PLAYER"));
                // Note: AI turn will be handled by separate endpoint for step-by-step visualization
                return ResponseEntity.ok(new GameResponse(dto, true, 
                    "Card drawn. Call /ai-turn to see AI's move.", events));
            } else {
                // Card was played - detect if it's attack or defense
                Card newLastCard = game.getGameState().getLastUsedCard();
                if (newLastCard != null && !newLastCard.equals(lastCardBefore)) {
                    boolean isMultiple = action.contains(",");
                    
                    // Check if it's an attack card or defense card
                    if (game.getGameRule().isAttackCard(newLastCard)) {
                        // Check if it was played as defense (was under attack and accumulatedDraws reset)
                        boolean wasDefense = wasUnderAttack && 
                                           game.getGameRule().getAccumulatedDraws() == 0 &&
                                           accumulatedDrawsBefore > 0;
                        
                        if (wasDefense) {
                            // Defense card played - show shield visualization
                            events.add(GameEvent.playedDefenseCard(
                                "PLAYER",
                                newLastCard, 
                                lastCardBefore, 
                                game.getGameRule().getAccumulatedDraws()
                            ));
                        } else {
                            // Attack card played - show arrow/line visualization
                            int punishment = game.getGameRule().getPunishmentValue(newLastCard);
                            events.add(GameEvent.playedAttackCard(
                                "PLAYER",
                                newLastCard, 
                                punishment, 
                                game.getGameRule().getAccumulatedDraws()
                            ));
                        }
                    } else {
                        // Normal card played
                        if (isMultiple) {
                            int count = action.split(",").length;
                            events.add(GameEvent.playedMultipleCards("PLAYER", count, game.getGameRule().getAccumulatedDraws()));
                        } else {
                            events.add(GameEvent.playedCard("PLAYER", newLastCard, game.getGameRule().getAccumulatedDraws()));
                        }
                    }
                }
                return ResponseEntity.ok(new GameResponse(dto, true, 
                    "Card played. Press 'End Turn' to complete your turn.", events));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            return ResponseEntity.badRequest()
                .body(new GameResponse(dto, false, e.getMessage()));
        } catch (Exception e) {
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            return ResponseEntity.internalServerError()
                .body(new GameResponse(dto, false, "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Executes AI turn step-by-step for visualization.
     * Vue should call this after player ends their turn to see AI's move animated.
     */
    @PostMapping("/ai-turn")
    public ResponseEntity<GameResponse> executeAITurn() {
        try {
            if (game.isGameOver()) {
                GameStateDTO dto = GameStateDTO.fromGameState(
                    game.getGameState(), 
                    game.getGameRule(), 
                    game.getDealer()
                );
                String winner = dto.winner();
                java.util.List<GameEvent> events = java.util.List.of(
                    GameEvent.gameOver(winner != null ? winner : "UNKNOWN")
                );
                return ResponseEntity.ok(new GameResponse(dto, false, "Game over!", events));
            }
            
            // Only allow if it's AI's turn
            if (game.getDealer().getCurrentPlayerIndex() != 1) {
                throw new IllegalStateException("Not AI's turn");
            }
            
            // Store state before AI action
            Card lastCardBefore = game.getGameState().getLastUsedCard();
            int aiHandSizeBefore = game.getGameState().getAiHand().size();
            int accumulatedDrawsBefore = game.getGameRule().getAccumulatedDraws();
            boolean wasUnderAttack = game.getGameRule().wasLastCardAttack();
            
            // Execute AI turn
            game.getDealer().executeNextTurn(game.getGameState(), game.getGameRule(), null);
            
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            
            // Build events list
            java.util.List<GameEvent> events = new java.util.ArrayList<>();
            events.add(GameEvent.turnStarted("AI"));
            
            // Detect what AI did
            Card newLastCard = game.getGameState().getLastUsedCard();
            int aiHandSizeAfter = game.getGameState().getAiHand().size();
            
            if (newLastCard != null && !newLastCard.equals(lastCardBefore)) {
                // AI played a card - check if attack or defense
                if (game.getGameRule().isAttackCard(newLastCard)) {
                    // Check if it was played as defense
                    boolean wasDefense = wasUnderAttack && 
                                       game.getGameRule().getAccumulatedDraws() == 0 &&
                                       accumulatedDrawsBefore > 0;
                    
                    if (wasDefense) {
                        // Defense card played - show shield visualization
                        events.add(GameEvent.playedDefenseCard(
                            "AI",
                            newLastCard, 
                            lastCardBefore, 
                            game.getGameRule().getAccumulatedDraws()
                        ));
                    } else {
                        // Attack card played - show arrow/line visualization
                        int punishment = game.getGameRule().getPunishmentValue(newLastCard);
                        events.add(GameEvent.playedAttackCard(
                            "AI",
                            newLastCard, 
                            punishment, 
                            game.getGameRule().getAccumulatedDraws()
                        ));
                    }
                } else {
                    // Normal card played
                    events.add(GameEvent.playedCard("AI", newLastCard, game.getGameRule().getAccumulatedDraws()));
                }
            } else if (aiHandSizeAfter > aiHandSizeBefore) {
                // AI drew card(s)
                int cardsDrawn = aiHandSizeAfter - aiHandSizeBefore;
                events.add(GameEvent.drewCard("AI", cardsDrawn, game.getGameRule().getAccumulatedDraws()));
            }
            
            events.add(GameEvent.turnEnded("AI"));
            events.add(GameEvent.turnStarted("PLAYER"));
            
            if (game.isGameOver()) {
                String winner = dto.winner();
                events.add(GameEvent.gameOver(winner != null ? winner : "UNKNOWN"));
                return ResponseEntity.ok(new GameResponse(dto, false, "Game over!", events));
            }
            
            return ResponseEntity.ok(new GameResponse(dto, true, "AI turn completed. Your turn!", events));
        } catch (IllegalStateException e) {
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            return ResponseEntity.badRequest()
                .body(new GameResponse(dto, false, e.getMessage()));
        } catch (Exception e) {
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            return ResponseEntity.internalServerError()
                .body(new GameResponse(dto, false, "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Ends the current player's turn and triggers AI turn.
     * This should be called after /play when the player presses the "End Turn" button.
     */
    @PostMapping("/end-turn")
    public ResponseEntity<GameResponse> endTurn() {
        try {
            boolean continues = game.endTurn();
            
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            
            if (!continues) {
                String winner = dto.winner();
                String message = winner != null ? "Game over! Winner: " + winner : "Game over!";
                return ResponseEntity.ok(new GameResponse(dto, false, message));
            }
            
            return ResponseEntity.ok(new GameResponse(dto, true, "Turn completed. AI has played."));
        } catch (IllegalStateException e) {
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            return ResponseEntity.badRequest()
                .body(new GameResponse(dto, false, e.getMessage()));
        } catch (Exception e) {
            GameStateDTO dto = GameStateDTO.fromGameState(
                game.getGameState(), 
                game.getGameRule(), 
                game.getDealer()
            );
            return ResponseEntity.internalServerError()
                .body(new GameResponse(dto, false, "Error: " + e.getMessage()));
        }
    }

    // One "tick" / one step (for AI turns or game progression)
    @PostMapping("/step")
    public ResponseEntity<GameResponse> step() {
        boolean continues = game.step();

        GameStateDTO dto = GameStateDTO.fromGameState(
            game.getGameState(), 
            game.getGameRule(), 
            game.getDealer()
        );

        if (!continues) {
            String winner = dto.winner();
            String message = winner != null ? "Game over! Winner: " + winner : "Game over!";
            return ResponseEntity.ok(new GameResponse(dto, false, message));
        }
        return ResponseEntity.ok(new GameResponse(dto, true, "Game continues"));
    }

    // Read-only (Vue refresh)
    @GetMapping("/state")
    public ResponseEntity<GameResponse> state() {
        GameStateDTO dto = GameStateDTO.fromGameState(
            game.getGameState(), 
            game.getGameRule(), 
            game.getDealer()
        );
        return ResponseEntity.ok(new GameResponse(dto, true, "Current state"));
    }
}
