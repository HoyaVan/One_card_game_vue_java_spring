import java.util.Scanner;

public class Player implements GameParticipantable {
    private final Scanner scanner;

    public Player(Scanner scanner) {
        this.scanner = scanner;
    }

    // Week 2. Exception Handling
    @Override
    public int takeTurn(DeckAndPlayerHands game, ActionCards gamePlay, int numberOfCardsToDraw, boolean isInitialTurn) {
        boolean validMove = false;

        while (!validMove) {
            try {
                System.out.print("Select a card index to play or type 'draw': ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("draw")) {
                    // Handle draw logic
                    if (numberOfCardsToDraw > 0) {
                        System.out.println("You drew all accumulated cards: " + numberOfCardsToDraw + " cards.");
                        for (int i = 0; i < numberOfCardsToDraw; i++) {
                            try {
                                game.drawCardsForHuman(numberOfCardsToDraw);
                            } catch (DeckEmptyException e) {
                                System.out.println("The deck is empty. No more cards can be drawn.");
                                break;
                            }
                        }
                        numberOfCardsToDraw = 0; // Reset accumulated draws
                        validMove = true; // End turn
                    } else {
                        try {
                            game.getPlayerHand().add(game.drawFromDeck());
                            System.out.println("You drew a card and skipped your turn.");
                            validMove = true; // End turn
                        } catch (DeckEmptyException e) {
                            System.out.println("The deck is empty. No card drawn.");
                            validMove = true; // End turn
                        }
                    }
                } else
                {
                    int index = Integer.parseInt(input) - 1;
                    Card card = game.getPlayerHand().get(index);

                    // Check if the card is playable
                    if (isInitialTurn)
                    {
                        // If it's the first turn, get the last card from the deck
                        Card lastDeckCard = game.getDeck().get(game.getDeckSize() - 1);
                        if (!gamePlay.isCardPlayable(card, lastDeckCard, isInitialTurn))
                        {
                            throw new InvalidMoveException("Invalid move. Card does not match.");
                        }
                    } else {
                        if (!gamePlay.isCardPlayable(card, gamePlay.getLastUsedCard(), isInitialTurn))
                        {
                            throw new InvalidMoveException("Invalid move. Card does not match.");
                        }
                    }

                    // Play the card
                    //gamePlay.playCard(game.getPlayerHand(), index);
                    System.out.println("You played: " + card);

                    // Handle punishment cards
                    if (card instanceof AttackCard || card instanceof AceCard || card instanceof JokerCard) {
                        accumulatedDraws += gamePlay.getPunishmentValue(card);
                        System.out.println("Accumulated punishment: " + accumulatedDraws + " cards.");
                    }

                    // Handle FaceCard logic
                    if (card instanceof FaceCard) {
                        card.applyEffect(game, gamePlay);
                        System.out.println("FaceCard effect applied. You can play another card of the same shape.");
                        validMove = false; // Allow the player to play another card
                    } else {
                        validMove = true; // End turn for other cards
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number or type 'draw'.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Invalid index. Please select a card index between 1 and " + game.getPlayerHand().size());
            } catch (InvalidMoveException e) {
                System.out.println(e.getMessage());
            }
        }
        return accumulatedDraws; // Return updated punishment count
    }

    public static void getValidatedInput(Scanner scanner, String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine();
            if (input.matches("\\d+|draw")) { // Valid input: numbers or "draw"
                return;
            } else {
                System.out.println("Invalid input. Please enter a card index or 'draw'.");
            }
        }
    }
}
