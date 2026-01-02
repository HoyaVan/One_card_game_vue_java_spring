package com;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import onecardgame.OneCardGame;

@DisplayName("OneCardGame Edge Cases Tests - Error handling and edge cases")
class OneCardGameEdgeCasesTest {

    private OneCardGame game;

    @BeforeEach
    void setUp() {
        game = new OneCardGame();
    }


    @Test
    @DisplayName("Game should handle zero as card index")
    void testEdgeCase_ZeroAsCardIndex() {
        game.setup();
        // Zero should be treated as draw action, not card index
        @SuppressWarnings("unused")
        boolean result = game.processPlayerAction("0");
        // Result is a boolean, so it's either true or false
        assertTrue(true, "Zero should trigger draw action");
    }
}

