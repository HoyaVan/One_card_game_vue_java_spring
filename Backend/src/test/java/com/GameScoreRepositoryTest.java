package com;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("GameScoreRepository Integration Tests")
class GameScoreRepositoryTest {

    @Autowired
    private GameScoreRepository repository;

    private GameScore score1;
    private GameScore score2;
    private GameScore score3;
    private GameScore score4;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        // Create test scores
        score1 = new GameScore(GameType.ONECARDGAME, 150, "Player1");
        score1.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        score2 = new GameScore(GameType.ONECARDGAME, 100, "Player2");
        score2.setCreatedAt(LocalDateTime.now().minusHours(1));
        
        score3 = new GameScore(GameType.WORDGAME, 200, "Player1");
        score3.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        
        score4 = new GameScore(GameType.ONECARDGAME, 50, "Player3");
        score4.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Save should persist GameScore")
    void testSave() {
        GameScore saved = repository.save(score1);
        
        assertNotNull(saved.getId(), "Saved score should have an ID");
        assertEquals(score1.getGameType(), saved.getGameType());
        assertEquals(score1.getScore(), saved.getScore());
        assertEquals(score1.getPlayerName(), saved.getPlayerName());
    }

    @Test
    @DisplayName("FindById should retrieve saved GameScore")
    void testFindById() {
        GameScore saved = repository.save(score1);
        Long id = saved.getId();
        
        GameScore found = repository.findById(id).orElse(null);
        
        assertNotNull(found, "Score should be found");
        assertEquals(score1.getGameType(), found.getGameType());
        assertEquals(score1.getScore(), found.getScore());
        assertEquals(score1.getPlayerName(), found.getPlayerName());
    }

    @Test
    @DisplayName("findByGameTypeOrderByScoreDesc should return scores ordered by score")
    void testFindByGameTypeOrderByScoreDesc() {
        repository.save(score1); // 150
        repository.save(score2); // 100
        repository.save(score4); // 50
        
        List<GameScore> scores = repository.findByGameTypeOrderByScoreDesc(GameType.ONECARDGAME);
        
        assertEquals(3, scores.size(), "Should return 3 scores");
        assertEquals(150, scores.get(0).getScore(), "First score should be highest");
        assertEquals(100, scores.get(1).getScore(), "Second score should be middle");
        assertEquals(50, scores.get(2).getScore(), "Third score should be lowest");
    }

    @Test
    @DisplayName("findByGameTypeOrderByScoreDesc should only return scores for specified game type")
    void testFindByGameTypeOrderByScoreDesc_FiltersByGameType() {
        repository.save(score1); // ONECARDGAME
        repository.save(score2); // ONECARDGAME
        repository.save(score3); // WORDGAME
        
        List<GameScore> oneCardScores = repository.findByGameTypeOrderByScoreDesc(GameType.ONECARDGAME);
        List<GameScore> wordGameScores = repository.findByGameTypeOrderByScoreDesc(GameType.WORDGAME);
        
        assertEquals(2, oneCardScores.size(), "Should return 2 ONECARDGAME scores");
        assertEquals(1, wordGameScores.size(), "Should return 1 WORDGAME score");
        assertEquals(GameType.ONECARDGAME, oneCardScores.get(0).getGameType());
        assertEquals(GameType.WORDGAME, wordGameScores.get(0).getGameType());
    }

    @Test
    @DisplayName("findByPlayerNameOrderByScoreDesc should return scores for player")
    void testFindByPlayerNameOrderByScoreDesc() {
        repository.save(score1); // Player1, 150
        repository.save(score3); // Player1, 200
        
        List<GameScore> scores = repository.findByPlayerNameOrderByScoreDesc("Player1");
        
        assertEquals(2, scores.size(), "Should return 2 scores for Player1");
        assertEquals(200, scores.get(0).getScore(), "Should be ordered by score desc");
        assertEquals(150, scores.get(1).getScore());
        assertEquals("Player1", scores.get(0).getPlayerName());
        assertEquals("Player1", scores.get(1).getPlayerName());
    }

    @Test
    @DisplayName("findTopScoresByGameType should return paginated results")
    void testFindTopScoresByGameType() {
        repository.save(score1); // 150
        repository.save(score2); // 100
        repository.save(score4); // 50
        
        Pageable pageable = PageRequest.of(0, 2);
        List<GameScore> scores = repository.findTopScoresByGameType(GameType.ONECARDGAME, pageable);
        
        assertEquals(2, scores.size(), "Should return 2 scores (page size)");
        assertEquals(150, scores.get(0).getScore(), "Should be ordered by score desc");
        assertEquals(100, scores.get(1).getScore());
    }

    @Test
    @DisplayName("findTopScoresByGameType should respect page size")
    void testFindTopScoresByGameType_PageSize() {
        repository.save(score1);
        repository.save(score2);
        repository.save(score4);
        
        Pageable pageable = PageRequest.of(0, 1);
        List<GameScore> scores = repository.findTopScoresByGameType(GameType.ONECARDGAME, pageable);
        
        assertEquals(1, scores.size(), "Should return only 1 score");
        assertEquals(150, scores.get(0).getScore(), "Should be the highest score");
    }

    @Test
    @DisplayName("findAllByOrderByScoreDesc should return all scores ordered by score")
    void testFindAllByOrderByScoreDesc() {
        repository.save(score3); // 200
        repository.save(score1); // 150
        repository.save(score2); // 100
        repository.save(score4); // 50
        
        List<GameScore> scores = repository.findAllByOrderByScoreDesc();
        
        assertEquals(4, scores.size(), "Should return all 4 scores");
        assertEquals(200, scores.get(0).getScore(), "Should be ordered by score desc");
        assertEquals(150, scores.get(1).getScore());
        assertEquals(100, scores.get(2).getScore());
        assertEquals(50, scores.get(3).getScore());
    }

    @Test
    @DisplayName("findAllByOrderByScoreDesc should work across all game types")
    void testFindAllByOrderByScoreDesc_AllGameTypes() {
        repository.save(score1); // ONECARDGAME, 150
        repository.save(score3); // WORDGAME, 200
        
        List<GameScore> scores = repository.findAllByOrderByScoreDesc();
        
        assertEquals(2, scores.size());
        assertEquals(200, scores.get(0).getScore(), "WORDGAME score should be first");
        assertEquals(150, scores.get(1).getScore(), "ONECARDGAME score should be second");
    }

    @Test
    @DisplayName("Delete should remove GameScore")
    void testDelete() {
        GameScore saved = repository.save(score1);
        Long id = saved.getId();
        
        repository.delete(saved);
        
        assertFalse(repository.findById(id).isPresent(), "Score should be deleted");
    }

    @Test
    @DisplayName("Count should return correct number of scores")
    void testCount() {
        assertEquals(0, repository.count(), "Should start with 0 scores");
        
        repository.save(score1);
        repository.save(score2);
        
        assertEquals(2, repository.count(), "Should have 2 scores");
    }

    @Test
    @DisplayName("Empty repository should return empty lists")
    void testEmptyRepository() {
        List<GameScore> scores = repository.findAllByOrderByScoreDesc();
        assertTrue(scores.isEmpty(), "Should return empty list");
        
        List<GameScore> oneCardScores = repository.findByGameTypeOrderByScoreDesc(GameType.ONECARDGAME);
        assertTrue(oneCardScores.isEmpty(), "Should return empty list");
    }

    @Test
    @DisplayName("CreatedAt should be set automatically on save")
    void testCreatedAtAutoSet() {
        GameScore score = new GameScore(GameType.ONECARDGAME, 100, "Player");
        score.setCreatedAt(null); // Clear if set
        
        GameScore saved = repository.save(score);
        
        assertNotNull(saved.getCreatedAt(), "CreatedAt should be set automatically");
    }
}



