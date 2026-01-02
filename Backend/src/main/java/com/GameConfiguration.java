package com;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import onecardgame.OneCardGame;
import wordgame.WordGame;

@Configuration
public class GameConfiguration {

    @Bean
    public OneCardGame oneCardGame() {
        return new OneCardGame();
    }

    @Bean
    public WordGame wordGame() {
        return new WordGame();
    }
}



