package com;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GameScore Entity Tests")
class GameScoreTest {

    private GameScore gameScore;

    @BeforeEach
    void setUp() {
        gameScore = new GameScore();
    }

    @Test
    @DisplayName("Default constructor should create empty GameScore")
    void testDefaultConstructor() {
        assertNotNull(gameScore, "GameScore should be created");
        assertNull(gameScore.getId(), "Id should be null initially");
        assertNull(gameScore.getGameType(), "GameType should be null initially");
        assertNull(gameScore.getScore(), "Score should be null initially");
        assertNull(gameScore.getPlayerName(), "PlayerName should be null initially");
    }

    @Test
    @DisplayName("Parameterized constructor should set all fields")
    void testParameterizedConstructor() {
        GameScore score = new GameScore(GameType.ONECARDGAME, 100, "TestPlayer");
        
        assertEquals(GameType.ONECARDGAME, score.getGameType(), 
                "GameType should be set");
        assertEquals(100, score.getScore(), "Score should be set");
        assertEquals("TestPlayer", score.getPlayerName(), 
                "PlayerName should be set");
        assertNotNull(score.getCreatedAt(), 
                "CreatedAt should be set automatically");
    }

    @Test
    @DisplayName("Setter and getter for id should work")
    void testIdSetterAndGetter() {
        gameScore.setId(1L);
        assertEquals(1L, gameScore.getId(), "Id should be set correctly");
    }

    @Test
    @DisplayName("Setter and getter for gameType should work")
    void testGameTypeSetterAndGetter() {
        gameScore.setGameType(GameType.ONECARDGAME);
        assertEquals(GameType.ONECARDGAME, gameScore.getGameType(), 
                "GameType should be set correctly");
        
        gameScore.setGameType(GameType.WORDGAME);
        assertEquals(GameType.WORDGAME, gameScore.getGameType(), 
                "GameType should be updated correctly");
        
        gameScore.setGameType(GameType.NUMBERGAME);
        assertEquals(GameType.NUMBERGAME, gameScore.getGameType(), 
                "GameType should be updated correctly");
    }

    @Test
    @DisplayName("Setter and getter for score should work")
    void testScoreSetterAndGetter() {
        gameScore.setScore(100);
        assertEquals(100, gameScore.getScore(), 
                "Score should be set correctly");
        
        gameScore.setScore(0);
        assertEquals(0, gameScore.getScore(), 
                "Score should handle zero value");
        
        gameScore.setScore(9999);
        assertEquals(9999, gameScore.getScore(), 
                "Score should handle large values");
    }

    @Test
    @DisplayName("Setter and getter for playerName should work")
    void testPlayerNameSetterAndGetter() {
        gameScore.setPlayerName("Player1");
        assertEquals("Player1", gameScore.getPlayerName(), 
                "PlayerName should be set correctly");
        
        gameScore.setPlayerName("Anonymous");
        assertEquals("Anonymous", gameScore.getPlayerName(), 
                "PlayerName should be updated correctly");
        
        gameScore.setPlayerName(null);
        assertNull(gameScore.getPlayerName(), 
                "PlayerName should handle null value");
    }

    @Test
    @DisplayName("Setter and getter for createdAt should work")
    void testCreatedAtSetterAndGetter() {
        LocalDateTime now = LocalDateTime.now();
        gameScore.setCreatedAt(now);
        assertEquals(now, gameScore.getCreatedAt(), 
                "CreatedAt should be set correctly");
        
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        gameScore.setCreatedAt(future);
        assertEquals(future, gameScore.getCreatedAt(), 
                "CreatedAt should be updated correctly");
    }

    @Test
    @DisplayName("CreatedAt should be set automatically in constructor")
    void testCreatedAtAutoSet() {
        GameScore score = new GameScore(GameType.ONECARDGAME, 100, "Player");
        LocalDateTime createdAt = score.getCreatedAt();
        
        assertNotNull(createdAt, "CreatedAt should be set automatically");
        
        // Should be very close to now (within 1 second)
        LocalDateTime now = LocalDateTime.now();
        assertTrue(createdAt.isBefore(now.plusSeconds(1)) && 
                   createdAt.isAfter(now.minusSeconds(1)), 
                "CreatedAt should be approximately now");
    }

    @Test
    @DisplayName("GameScore should handle all game types")
    void testAllGameTypes() {
        GameScore score1 = new GameScore(GameType.ONECARDGAME, 100, "Player");
        assertEquals(GameType.ONECARDGAME, score1.getGameType());
        
        GameScore score2 = new GameScore(GameType.WORDGAME, 200, "Player");
        assertEquals(GameType.WORDGAME, score2.getGameType());
        
        GameScore score3 = new GameScore(GameType.NUMBERGAME, 300, "Player");
        assertEquals(GameType.NUMBERGAME, score3.getGameType());
    }

    @Test
    @DisplayName("GameScore should handle edge case scores")
    void testEdgeCaseScores() {
        GameScore score1 = new GameScore(GameType.ONECARDGAME, 0, "Player");
        assertEquals(0, score1.getScore());
        
        GameScore score2 = new GameScore(GameType.ONECARDGAME, Integer.MAX_VALUE, "Player");
        assertEquals(Integer.MAX_VALUE, score2.getScore());
    }

    @Test
    @DisplayName("GameScore should handle empty player name")
    void testEmptyPlayerName() {
        GameScore score = new GameScore(GameType.ONECARDGAME, 100, "");
        assertEquals("", score.getPlayerName());
    }

    @Test
    @DisplayName("GameScore should handle long player names")
    void testLongPlayerName() {
        String longName = "A".repeat(100);
        GameScore score = new GameScore(GameType.ONECARDGAME, 100, longName);
        assertEquals(longName, score.getPlayerName());
    }

    @Test
    @DisplayName("Multiple GameScore instances should be independent")
    void testMultipleInstances() {
        GameScore score1 = new GameScore(GameType.ONECARDGAME, 100, "Player1");
        GameScore score2 = new GameScore(GameType.WORDGAME, 200, "Player2");
        
        assertNotEquals(score1.getGameType(), score2.getGameType());
        assertNotEquals(score1.getScore(), score2.getScore());
        assertNotEquals(score1.getPlayerName(), score2.getPlayerName());
    }
}



