package com;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for GameScore entity.
 * Provides CRUD operations and custom queries for game scores.
 */
@Repository
public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    /**
     * Find all scores for a specific game type, ordered by score descending (highest first).
     */
    List<GameScore> findByGameTypeOrderByScoreDesc(GameType gameType);

    /**
     * Find all scores for a specific player name, ordered by score descending.
     */
    List<GameScore> findByPlayerNameOrderByScoreDesc(String playerName);

    /**
     * Find top N scores for a specific game type.
     */
    @Query("SELECT gs FROM GameScore gs WHERE gs.gameType = :gameType ORDER BY gs.score DESC")
    List<GameScore> findTopScoresByGameType(GameType gameType, org.springframework.data.domain.Pageable pageable);

    /**
     * Find all scores ordered by score descending (global leaderboard).
     */
    List<GameScore> findAllByOrderByScoreDesc();
}

