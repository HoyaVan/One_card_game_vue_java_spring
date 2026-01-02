package com;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import onecardgame.OneCardGame;

@WebMvcTest(MainControl.class)
class MainControlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameScoreRepository scoreRepository; // Required by controller constructor, but not tested

    @MockitoBean
    private OneCardGame oneCardGame;

    @MockitoBean
    private wordgame.WordGame wordGame; // Required by controller constructor, but not tested

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MainControl.PlayRequest validPlayRequest;

    @BeforeEach
    void setUp() {
        validPlayRequest = new MainControl.PlayRequest("1", null);
    }

    @Test
    void testPlay_OneCardGame_Success() throws Exception {
        when(oneCardGame.processPlayerAction("1")).thenReturn(true);

        mockMvc.perform(post("/games/play/ONECARDGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameType").value("ONECARDGAME"))
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("Turn completed"));

        verify(oneCardGame, times(1)).processPlayerAction("1");
    }

    @Test
    void testPlay_OneCardGame_GameOver() throws Exception {
        when(oneCardGame.processPlayerAction("1")).thenReturn(false);

        mockMvc.perform(post("/games/play/ONECARDGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameType").value("ONECARDGAME"))
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("Game over"));

        verify(oneCardGame, times(1)).processPlayerAction("1");
    }

    @Test
    void testPlay_OneCardGame_WithDrawAction() throws Exception {
        MainControl.PlayRequest drawRequest = new MainControl.PlayRequest("0", null);
        when(oneCardGame.processPlayerAction("0")).thenReturn(true);

        mockMvc.perform(post("/games/play/ONECARDGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(oneCardGame, times(1)).processPlayerAction("0");
    }

    @Test
    void testPlay_OneCardGame_InvalidAction() throws Exception {
        when(oneCardGame.processPlayerAction(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid action"));

        mockMvc.perform(post("/games/play/ONECARDGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("Invalid action"));
    }

    @Test
    void testPlay_NumberGame_ThrowsException() throws Exception {
        mockMvc.perform(post("/games/play/NUMBERGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("Number Game requires JavaFX - cannot be started via REST API"));
    }

    @Test
    void testPlay_InvalidRequest_BlankAction() throws Exception {
        MainControl.PlayRequest invalidRequest = new MainControl.PlayRequest("", null);

        mockMvc.perform(post("/games/play/ONECARDGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPlay_ActionTrimmedAndUppercased() throws Exception {
        MainControl.PlayRequest requestWithSpaces = new MainControl.PlayRequest("  draw  ", null);
        when(oneCardGame.processPlayerAction("DRAW")).thenReturn(true);

        mockMvc.perform(post("/games/play/ONECARDGAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithSpaces)))
                .andExpect(status().isOk());

        verify(oneCardGame, times(1)).processPlayerAction("DRAW");
    }
}

