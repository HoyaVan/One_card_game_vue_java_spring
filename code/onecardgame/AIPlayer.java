package onecardgame;

import java.util.Random;
import onecardgame.cards.*;

public class AIPlayer implements GameParticipantable {
    private static final int AI_PLAY_PROBABILITY_DENOMINATOR = 4;
    private static final int AI_PLAY_PROBABILITY_NUMERATOR = 3;
    private static final int MIN_INDEX = 0;

    private final Random random = new Random();
    
    @Override
    public boolean takeTurn(final GameState gameState, 
                            final PlayRule gameRule, 
                            final boolean isInitialTurn) {
        final int accumulatedDraws;
        GameMessages.display(GameMessages.AI_TURN);
        
      
        
        accumulatedDraws = gameRule.getAccumulatedDraws();
            if (accumulatedDraws > MIN_INDEX) {
            handleAccumulatedDraws(gameState, gameRule, accumulatedDraws);
        } else {
            drawSingleCard(gameState);
            
            if (shouldPlayCard(gameRule)) {
                tryPlayCard(gameState, gameRule, isInitialTurn);
            }
            
            tryBlockJoker(gameState, gameRule);
        }
        
        return true;
    }
    
    private void handleAccumulatedDraws(final GameState gameState, final PlayRule gameRule, final int accumulatedDraws) {
        GameMessages.displayFormatted(GameMessages.AI_DREW_ACCUMULATED_CARDS, accumulatedDraws);
        for (int i = MIN_INDEX; i < accumulatedDraws; i++) {
            try {
                gameState.getAiHand().add(gameState.drawFromDeck());
            } catch (IllegalStateException e) {
                GameMessages.display(GameMessages.DECK_EMPTY_NO_MORE_CARDS);
                break;
            }
        }
        GameMessages.display(GameMessages.AI_DREW_ALL_CARDS);
        gameRule.resetAccumulatedDraws();
    }
    
    private void drawSingleCard(final GameState gameState) {
        try {
            gameState.getAiHand().add(gameState.drawFromDeck());
            GameMessages.display(GameMessages.AI_DREW_CARD);
        } catch (IllegalStateException e) {
            GameMessages.display(GameMessages.DECK_EMPTY_NO_CARD_DRAWN);
        }
    }
    
    private boolean shouldPlayCard(final PlayRule gameRule) {
        int currentAccumulatedDraws = gameRule.getAccumulatedDraws();
        return currentAccumulatedDraws == MIN_INDEX
                || random.nextInt(AI_PLAY_PROBABILITY_DENOMINATOR) < AI_PLAY_PROBABILITY_NUMERATOR;
    }
    
    private void tryPlayCard(final GameState gameState, 
                             final PlayRule gameRule, 
                             final boolean isInitialTurn) {
        Card aiCard = gameRule.findPlayableCard(gameState.getAiHand(), 
                                                gameState.getLastUsedCard(), 
                                                isInitialTurn);
        
        if (aiCard == null) {
            return;
        }
        
        int cardIndex = gameState.getAiHand().indexOf(aiCard);
        gameRule.playCard(gameState.getAiHand(), cardIndex);
        GameMessages.displayFormatted(GameMessages.AI_PLAYED_CARD, aiCard);
        
        displayAttackMessage(gameRule, aiCard);
        
        if (aiCard instanceof FaceCard) {
            handleFaceCardEffect(gameState, gameRule, isInitialTurn);
        } else {
            GameMessages.display(GameMessages.AI_TURN_OVER + System.lineSeparator());
        }
    }
    
    private void displayAttackMessage(final PlayRule gameRule, final Card card) {
        int currentAccumulatedDraws = gameRule.getAccumulatedDraws();
        if (currentAccumulatedDraws > 0 
                && card instanceof Attackable) {
            GameMessages.displayFormatted(GameMessages.AI_ATTACK, currentAccumulatedDraws);
        }
    }
    
    private void handleFaceCardEffect(final GameState gameState, final PlayRule gameRule, final boolean isInitialTurn) {
        Card lastCard = gameState.getLastUsedCard();
        Card additionalCard = gameRule.findPlayableCard(gameState.getAiHand(), lastCard, isInitialTurn);
        
        if (additionalCard != null) {
            int additionalIndex = gameState.getAiHand().indexOf(additionalCard);
            gameRule.playCard(gameState.getAiHand(), additionalIndex);
            GameMessages.displayFormatted(GameMessages.AI_PLAYED_ANOTHER_CARD, additionalCard);
        } else {
            GameMessages.display(GameMessages.AI_NO_ADDITIONAL_CARD);
        }
    }
    
    private void tryBlockJoker(final GameState gameState, final PlayRule gameRule) {
        if (!(gameState.getLastUsedCard() instanceof JokerCard)) {
            return;
        }
        
        for (Card card : gameState.getAiHand()) {
            if (card instanceof JokerCard) {
                int cardIndex = gameState.getAiHand().indexOf(card);
                gameRule.playCard(gameState.getAiHand(), cardIndex);
                GameMessages.display(GameMessages.AI_BLOCKED_JOKER);
                return;
            }
        }
    }

    @Override
    public Card playCard(final GameState gameState, 
                        final PlayRule gameRule, 
                        final boolean isInitialTurn) throws InvalidMoveException {

        final Card cardToPlay;
        final int cardIndex;

        cardToPlay = gameRule.findPlayableCard(gameState.getAiHand(), 
                                                gameState.getLastUsedCard(), 
                                                isInitialTurn);

        if (cardToPlay == null) {
            return null;
        }

        cardIndex = gameState.getAiHand().indexOf(cardToPlay);
        gameRule.playCard(gameState.getAiHand(), cardIndex);

        return cardToPlay;
    }
}
