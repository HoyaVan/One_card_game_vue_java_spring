package wordgame;

import java.util.Scanner;

public class GameValidator
{
    private static final String YES_RESPONSE = "y";
    private static final String NO_RESPONSE = "n";

    public static boolean validateReplay(final Scanner scanner)
    {
        if (scanner == null)
        {
            throw new IllegalArgumentException("Scanner cannot be null");
        }

        while (true)
        {
            final String input;

            System.out.print("Do you want to play again? (y/n): ");
            input = scanner.nextLine().trim().toLowerCase();

            if (input.equals(YES_RESPONSE))
            {
                return true;
            }

            if (input.equals(NO_RESPONSE))
            {
                return false;
            }

            System.out.println("Please type 'y' for yes or 'n' for no.");
        }
    }

    public static boolean validateAnswer(final String userAnswer, 
                                        final String correctAnswer)
    {
        if (userAnswer == null || correctAnswer == null)
        {
            return false;
        }

        return userAnswer.equalsIgnoreCase(correctAnswer);
    }
}
