import java.util.Scanner;

import numbergame.NumberGame;
import onecardgame.OneCardGame;

public class Main
{
    public static void main(final String[] args)
    {
        final Scanner playgame;
        String userInput;

        playgame = new Scanner(System.in);

        System.out.println("Welcome to the game land!!");

        while (true)
        {
            System.out.println(System.lineSeparator() +
                    // "Press W : Word game" +
                    // System.lineSeparator() +
                    "Press N : Number game" +
                    System.lineSeparator() +
                    "Press M : One card game" +
                    System.lineSeparator() +
                    "Press Q : Quit the game" +
                    System.lineSeparator() +
                    "Enter your choice:");
            userInput = playgame.nextLine();
            
            try
            {
                validateGameStartInput(userInput);
                switch (userInput.toUpperCase())
                {
                    // case "W":
                        // WordGame.playTriviaGame();
                    case "N":
                        NumberGame.main(args); // Launch NumberGame
                        break;
                    case "M":
                        OneCardGame.main(args); // Launch OneCardGame
                        break;
                    case "Q":
                        System.out.println("Thanks for playing! Goodbye!");
                        return;
                    default:
                        System.out.println("Unexpected error.");
                }
            }
            catch (IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    static void validateGameStartInput(final String userInput)
    {
        final String validInputs;
        final String validInputGuideline;

        validInputs = "wWnNmMqQ";
        validInputGuideline = "Invalid input! Please enter a valid game:" +
                System.lineSeparator() + "W : word game" +
                System.lineSeparator() + "N : number game" +
                System.lineSeparator() + "M : One card game" +
                System.lineSeparator() + "Q : quit the game";

        if (userInput == null || userInput.isBlank())
        {
            throw new IllegalArgumentException(validInputGuideline);
        }

        if (userInput.length() != 1 || !validInputs.contains(userInput))
        {
            throw new IllegalArgumentException(validInputGuideline);
        }
    }


}
