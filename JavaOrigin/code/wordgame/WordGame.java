package wordgame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WordGame
{
    private static final String INPUT_DIRECTORY_PATH = "./src";
    private static final String OUTPUT_DIRECTORY_PATH = "./output";
    private static final String OUTPUT_SCORE_FILE = "./output/score.txt";
    private static final int MAX_ROUNDS = 10;
    private static final int MAX_ATTEMPTS = 2;
    private static final int QUESTION_TYPES = 3;
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static void playTriviaGame()
    {
        final Scanner scanUserQuizAnswer = new Scanner(System.in);
        final Scanner scanReplayResponse = new Scanner(System.in);
        final Random random = new Random();
        final Map<String, Country> countries;
        final List<Country> countryList;
        final List<Score> scores = new ArrayList<>();
        final World world;
        Score fileHighScore;

        // Ensure the output file, directory exists
        setupOutputEnvironment();

        // Reload the file to find the high score
        fileHighScore = Score.parseHighScoreFromFile(OUTPUT_SCORE_FILE);

        world = World.loadWorldMap(INPUT_DIRECTORY_PATH);
        countries = world.getCountries();
        countryList = new ArrayList<>(countries.values());

        Score cumulativeScore = new Score(); // Cumulative score for the entire session

        boolean playAgain;

        do
        {
            Score currentScore = new Score();
            currentScore.incrementTotalGamesPlayed();

            for (int round = 0; round < MAX_ROUNDS; round++)
            {
                int questionType = random.nextInt(QUESTION_TYPES) + 1; // Randomly select question type (1, 2, or 3)
                Country country = countryList.get(random.nextInt(countryList.size())); // Randomly select a country

                switch (questionType)
                {
                    case 1:
                        askCapitalCityToCountry(scanUserQuizAnswer, currentScore, country);
                        break;
                    case 2:
                        askCountryToCapitalCity(scanUserQuizAnswer, currentScore, country);
                        break;
                    case 3:
                        askFactToCountry(scanUserQuizAnswer, currentScore, country);
                        break;
                    default:
                        System.out.println("Invalid question type!");
                }
            }

            // Add the current score to the list
            scores.add(currentScore);

            // Merge current game's stats into the cumulative score
            cumulativeScore.add(currentScore);

            // Print stats for the current round
            cumulativeScore.printRoundStats();

            if (cumulativeScore.getAverageScore() > (fileHighScore != null ? fileHighScore.getAverageScore() : 0))
            {
                System.out.printf("CONGRATULATIONS! You are the new high score with an average of %.2f points per game!%n",
                        cumulativeScore.getAverageScore());
                fileHighScore = cumulativeScore;
            }
            else
            {
                System.out.printf("You did not beat the high score of %.2f points from %s.%n",
                        fileHighScore.getAverageScore(),
                        fileHighScore.getDateTimePlayed().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
            }

            System.lineSeparator();

            // Ask the user if they want to play again
            playAgain = GameValidator.validateReplay(scanReplayResponse);

        } while (playAgain);

        // Print cumulative stats for the session
        System.out.println("\nFinal Stats for the Entire Session:");

        System.out.println(cumulativeScore);

        // Save cumulative stats to the file
        Score.appendScoreToFile(cumulativeScore, OUTPUT_SCORE_FILE);

        // Calculate the session's high score
        Score sessionHighScore = scores.stream()
                .max(Comparator.comparingDouble(Score::getAverageScore))
                .orElse(null);

        // Compare session high score with the file high score
        if (fileHighScore == null || sessionHighScore.getAverageScore() > fileHighScore.getAverageScore())
        {
            System.out.printf(
                    "CONGRATULATIONS! You are the new high score with an average of %.2f points per game; the previous record was %.2f points per game on %s.%n",
                    sessionHighScore.getAverageScore(),
                    fileHighScore != null ? fileHighScore.getAverageScore() : 0.0,
                    fileHighScore != null ? fileHighScore.getDateTimePlayed().format(DateTimeFormatter.ofPattern(
                            DATE_TIME_PATTERN)) : "N/A");
        }
        else
        {
            System.out.printf("You did not beat the high score of %.2f points per game from %s.%n",
                    fileHighScore.getAverageScore(),
                    fileHighScore.getDateTimePlayed().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        }

        // printout the game info
        if (fileHighScore != null)
        {
            System.lineSeparator();
        }

        System.out.println("Thank you for playing! Goodbye.");
    }

    private static void askCapitalCityToCountry(final Scanner scanUserQuizAnswer, final Score scoreTracker, final Country country)
    {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
        {
            System.out.println("What country has the capital city " + country.getCapitalCityName() + "?");
            String answer = scanUserQuizAnswer.nextLine().trim();

            if (GameValidator.validateAnswer(answer, country.getName()))
            {
                if (attempt == 0)
                {
                    System.out.println("Correct!");
                    scoreTracker.recordFirstAttempt(true);
                }
                else
                {
                    System.out.println("Correct!");
                    scoreTracker.recordSecondAttempt(true);
                }
                break;
            }
            else
            {
                if (attempt == 0)
                {
                    scoreTracker.recordFirstAttempt(false);
                    System.out.println("Incorrect! You have one more guess.");
                }
                else
                {
                    scoreTracker.recordSecondAttempt(false);
                    System.out.println("Incorrect! The correct answer is " + country.getName() + ".");
                }
            }
        }
    }

    private static void askCountryToCapitalCity(final Scanner scanUserQuizAnswer, final Score scoreTracker, final Country country)
    {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
        {
            System.out.println("What is the capital city of " + country.getName() + "?");
            String answer = scanUserQuizAnswer.nextLine().trim();

            if (GameValidator.validateAnswer(answer, country.getCapitalCityName()))
            {
                if (attempt == 0)
                {
                    System.out.println("Correct!");
                    scoreTracker.recordFirstAttempt(true);
                }
                else
                {
                    System.out.println("Correct!");
                    scoreTracker.recordSecondAttempt(true);
                }
                break;
            }
            else
            {
                if (attempt == 0)
                {
                    scoreTracker.recordFirstAttempt(false);
                    System.out.println("Incorrect! You have one more guess.");
                }
                else
                {
                    scoreTracker.recordSecondAttempt(false);
                    System.out.println("Incorrect! The correct answer is " + country.getCapitalCityName() + ".");
                }
            }
        }
    }

    private static void askFactToCountry(final Scanner scanUserQuizAnswer, final Score scoreTracker, final Country country)
    {
        String[] facts = country.getFacts();
        final Random random = new Random();
        String randomFact = facts[random.nextInt(facts.length)];
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
        {
            System.out.println("Which country does this describe? \"" + randomFact + "\"");
            String answer = scanUserQuizAnswer.nextLine().trim();

            if (GameValidator.validateAnswer(answer, country.getName()))
            {
                if (attempt == 0)
                {
                    System.out.println("Correct!");
                    scoreTracker.recordFirstAttempt(true);
                }
                else
                {
                    System.out.println("Correct!");
                    scoreTracker.recordSecondAttempt(true);
                }
                break;
            }
            else
            {
                if (attempt == 0)
                {
                    scoreTracker.recordFirstAttempt(false);
                    System.out.println("Incorrect! You have one more guess.");
                }
                else
                {
                    scoreTracker.recordSecondAttempt(false);
                    System.out.println("Incorrect! The correct answer is " + country.getName() + ".");
                }
            }
        }
    }


    public static void setupOutputEnvironment()
    {
        try
        {
            Path outputFilePath = Paths.get(OUTPUT_SCORE_FILE);
            Path outputDirPath = Paths.get(OUTPUT_DIRECTORY_PATH);

            // Check if the directory exists
            if (Files.notExists(outputDirPath))
            {
                // Create the directory
                Files.createDirectories(outputDirPath);
                System.out.println("Output directory created: " + outputDirPath.toAbsolutePath());
            }
            else
            {
                System.out.println("Output directory already exists: " + outputDirPath.toAbsolutePath());
            }

            // Check if the file exists
            if (Files.notExists(outputFilePath))
            {
                // Create the file
                Files.createFile(outputFilePath);
                System.out.println("Output file created: " + outputFilePath.toAbsolutePath());
            }
            else
            {
                System.out.println("Output file already exists: " + outputFilePath.toAbsolutePath());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
