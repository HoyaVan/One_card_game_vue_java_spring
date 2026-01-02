package com;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import onecardgame.OneCardGame;
import wordgame.WordGame;

@RestController
@RequestMapping("/games")
public class MainControl{

    private final OneCardGame oneCardGame;
    private final WordGame wordGame;
    private final GameScoreRepository scoreRepository;
    // private final NumberGame numberGame;

    public MainControl(GameScoreRepository scoreRepository, OneCardGame oneCardGame, WordGame wordGame) {
        this.scoreRepository = scoreRepository;
        this.oneCardGame = oneCardGame;
        this.wordGame = wordGame;
        // this.numberGame = new NumberGame();
    }
     
    public record PlayRequest(
        @NotBlank String action,
        String message
    ) {}
    public record PlayResponse(
        GameType gameType,
        boolean ok,
        String message
    ) {}
    public record ScoreRequest(
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score must be non-negative")
        Integer score,
        String playerName
    ) {}
    public record ScoreResponse(
        Long id,
        GameType gameType,
        Integer score,
        String playerName,
        String createdAt,
        String message
    ) {}

    @PostMapping("play/{gameType}") // POST /games/ONECARDGAME/play
    public ResponseEntity<PlayResponse> play(
            @PathVariable GameType gameType,
            @Valid @RequestBody PlayRequest request
    ) {
        String action = request.action().trim().toUpperCase();

        try {
            String message = switch (gameType) {
                case ONECARDGAME -> {
                    // OneCardGame now uses processPlayerAction for turn-based gameplay
                    // Note: Use /games/onecard/play endpoint for better REST API design
                    boolean continues = oneCardGame.processPlayerAction(action);
                    yield continues ? "Turn completed" : "Game over";
                }
    
                case NUMBERGAME -> throw new IllegalArgumentException(
                        "Number Game requires JavaFX - cannot be started via REST API"
                );
    
                case WORDGAME -> wordGame.start(action);
            };

            return ResponseEntity.ok(new PlayResponse(gameType, true, message));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new PlayResponse(gameType, false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new PlayResponse(gameType, false, "Server error"));
        }
    }

    /**
     * Save a game score to the database.
     * POST /games/score/ONECARDGAME
     * Body: { "score": 100, "playerName": "Player1" }
     */
    @PostMapping("/score/{gameType}")
    public ResponseEntity<ScoreResponse> saveScore(
            @PathVariable GameType gameType,
            @Valid @RequestBody ScoreRequest request
    ) {
        try {
            GameScore gameScore = new GameScore(
                gameType,
                request.score(),
                request.playerName() != null ? request.playerName() : "Anonymous"
            );
            
            GameScore savedScore = scoreRepository.save(gameScore);
            
            ScoreResponse response = new ScoreResponse(
                savedScore.getId(),
                savedScore.getGameType(),
                savedScore.getScore(),
                savedScore.getPlayerName(),
                savedScore.getCreatedAt().toString(),
                "Score saved successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ScoreResponse(null, gameType, request.score(), 
                    request.playerName(), null, "Error saving score: " + e.getMessage()));
        }
    }

    /**
     * Get all scores for a specific game type, ordered by score descending.
     * GET /games/score/ONECARDGAME
     */
    @GetMapping("/score/{gameType}")
    public ResponseEntity<List<ScoreResponse>> getScoresByGameType(
            @PathVariable GameType gameType,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<GameScore> scores = scoreRepository.findTopScoresByGameType(gameType, pageable);
            
            List<ScoreResponse> responses = scores.stream()
                .map(score -> new ScoreResponse(
                    score.getId(),
                    score.getGameType(),
                    score.getScore(),
                    score.getPlayerName(),
                    score.getCreatedAt().toString(),
                    null
                ))
                .toList();
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all scores (global leaderboard), ordered by score descending.
     * GET /games/score
     */
    @GetMapping("/score")
    public ResponseEntity<List<ScoreResponse>> getAllScores(
            @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            List<GameScore> scores = scoreRepository.findAllByOrderByScoreDesc();
            
            // Limit results
            List<ScoreResponse> responses = scores.stream()
                .limit(limit)
                .map(score -> new ScoreResponse(
                    score.getId(),
                    score.getGameType(),
                    score.getScore(),
                    score.getPlayerName(),
                    score.getCreatedAt().toString(),
                    null
                ))
                .toList();
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
