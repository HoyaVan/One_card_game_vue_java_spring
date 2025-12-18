import java.util.Random;

public class AIPlayer implements GameParticipantable {
    @Override
    public int takeTurn(DeckAndPlayerHands game, ActionCards gamePlay, int accumulatedDraws, boolean isInitialTurn) {
        System.out.println("AI's turn...");
        Random random = new Random();

        // If AI decides to draw or has no playable cards
        if (accumulatedDraws > 0) {
            System.out.println("AI decided to draw all accumulated cards: " + accumulatedDraws + " cards.");
            for (int i = 0; i < accumulatedDraws; i++) {
                try {
                    game.getAiHand().add(game.drawFromDeck());
                } catch (DeckEmptyException e) {
                    System.out.println("The deck is empty. No more cards can be drawn.");
                    break;
                }
            }
            System.out.println("AI drew all cards and skipped its turn.");
            return accumulatedDraws = 0;
        }
        else
        {
            try
            {
                game.getAiHand().add(game.drawFromDeck());
                System.out.println("AI drew a card and skipped its turn.");
            }

            catch (DeckEmptyException e)
            {
                System.out.println("The deck is empty. No card drawn.");
            }
        }

        // Decide whether to play or draw (3:1 ratio)
        boolean shouldPlay = accumulatedDraws == 0 || random.nextInt(4) < 3;

        if (shouldPlay) {
            Card aiCard = gamePlay.findPlayableCard(game.getAiHand(), gamePlay.getLastUsedCard(), isInitialTurn);
            if (aiCard != null) {
//                gamePlay.playCard(game.getAiHand(), game.getAiHand().indexOf(aiCard));
                System.out.println("AI played: " + aiCard);

                if (aiCard instanceof AttackCard || aiCard instanceof AceCard || aiCard instanceof JokerCard) {
                    accumulatedDraws += gamePlay.getPunishmentValue(aiCard);
                    System.out.println("AI attack! Accumulated draws by attack cards: " + accumulatedDraws + " cards.");
                }

                if (aiCard instanceof FaceCard) {
                    // Trigger the effect for FaceCard
                    aiCard.applyEffect(game, gamePlay);

                    // Allow the AI to play another card of the same shape
                    Card additionalCard = gamePlay.findPlayableCard(
                            game.getAiHand(),
                            gamePlay.getLastUsedCard(), // Pass the last used card correctly
                            isInitialTurn // Pass the isInitialTurn flag only if applicable
                    );

                    if (additionalCard != null) {
//                        gamePlay.playCard(game.getAiHand(), game.getAiHand().indexOf(additionalCard));
                        System.out.println("AI played another card of the same shape: " + additionalCard);
                    } else {
                        System.out.println("AI has no additional card to play of the same shape.");
                    }
                } else {
                    System.out.println("AI's turn is over." + System.lineSeparator());
                }

                return accumulatedDraws;
            }
        }

        // AI blocks Joker with Joker
        if (gamePlay.getLastUsedCard() instanceof JokerCard) {
            for (Card card : game.getAiHand()) {
                if (card instanceof JokerCard) {
//                    gamePlay.playCard(game.getAiHand(), game.getAiHand().indexOf(card));
                    System.out.println("AI blocked the Joker with its own Joker!");
                    return accumulatedDraws;
                }
            }
        }

        return accumulatedDraws;
    }
}
