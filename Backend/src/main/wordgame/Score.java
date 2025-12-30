package wordgame;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Score {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final int FIRST_ATTEMPT_SCORE = 2;
    private static final int SECOND_ATTEMPT_SCORE = 1;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private LocalDateTime dateTimePlayed;
    private int numGamesPlayed;
    private int numCorrectFirstAttempt;
    private int numCorrectSecondAttempt;
    private int numIncorrectAttempts;
    private int totalScore;

    public Score() {
        this(LocalDateTime.now(), 0, 0, 0, 0, 0);
    }

    public Score(final LocalDateTime dateTimePlayed,
                 final int numGamesPlayed,
                 final int numCorrectFirstAttempt,
                 final int numCorrectSecondAttempt,
                 final int numIncorrectAttempts,
                 final int totalScore)
    {
        this.dateTimePlayed = dateTimePlayed;
        this.numGamesPlayed = numGamesPlayed;
        this.numCorrectFirstAttempt = numCorrectFirstAttempt;
        this.numCorrectSecondAttempt = numCorrectSecondAttempt;
        this.numIncorrectAttempts = numIncorrectAttempts;
        this.totalScore = totalScore;
    }

    // Increment games played
    public void incrementTotalGamesPlayed()
    {
        numGamesPlayed++;
    }

    // Record first attempt
    public void recordFirstAttempt(final boolean isCorrect) {
        if (isCorrect) {
            numCorrectFirstAttempt++;
            totalScore += FIRST_ATTEMPT_SCORE;
        }
    }

    // Record second attempt
    public void recordSecondAttempt(final boolean isCorrect) {
        if (isCorrect) {
            numCorrectSecondAttempt++;
            totalScore += SECOND_ATTEMPT_SCORE;
        }
        else
        {
            numIncorrectAttempts++;
        }
    }

    public int getScore()
    {
        return totalScore;
    }

    public double getAverageScore() {
        return  numGamesPlayed > 0 ? (double) totalScore / numGamesPlayed : 0.0;
    }

    public LocalDateTime getDateTimePlayed() {
        return dateTimePlayed;
    }


    public static Score parseHighScoreFromFile(final String filePath) {
        if (filePath == null)
        {
            throw new IllegalArgumentException("File path cannot be null");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            LocalDateTime dateTimePlayed = null;
            double maxAverageScore = 0.0;
            Score highScore = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Date and Time:")) {
                    String dateTimeString = line.split(": ", 2)[1].trim();
                    dateTimePlayed = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                }

                if (line.startsWith("Average Score:")) {
                    double averageScore = Double.parseDouble(line.split(": ", 2)[1].split(" ")[0]);

                    if (averageScore > maxAverageScore) {
                        maxAverageScore = averageScore;

                        // Create or update the high score object
                        highScore = new Score(dateTimePlayed,0,0,0,0,0);
                        highScore.dateTimePlayed = dateTimePlayed; // Assign parsed date
                        highScore.totalScore = (int) averageScore; // Simulate based on average score
                        highScore.numGamesPlayed = 1; // Default to 1 game for simplicity
                    }
                }
            }

            return highScore;

        } catch (IOException | NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }

        return null; // Return null if parsing fails or file is empty
    }

    public void add(final Score other) {
        if (other == null)
        {
            throw new IllegalArgumentException("Score cannot be null");
        }

        this.numGamesPlayed += other.numGamesPlayed;
        this.numCorrectFirstAttempt += other.numCorrectFirstAttempt;
        this.numCorrectSecondAttempt += other.numCorrectSecondAttempt;
        this.numIncorrectAttempts += other.numIncorrectAttempts;
        this.totalScore += other.totalScore;
    }

    

    public static List<Score> readScoresFromFile(final String SCORE_FILE) throws IOException {
        if (SCORE_FILE == null)
        {
            throw new IllegalArgumentException("Score file path cannot be null");
        }

        List<Score> scores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_FILE))) {
            String line;
            Score score = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Date and Time:")) {
                    score = new Score(LocalDateTime.now(), 0, 0, 0, 0, 0);
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.dateTimePlayed = LocalDateTime.parse(parts[1], DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                    }
                } else if (line.startsWith("Games Played:")) {
                    parseGamesPlayed(score, line);
                } else if (line.startsWith("Correct First Attempts:")) {
                    parseCorrectFirstAttempts(score, line);
                } else if (line.startsWith("Correct Second Attempts:")) {
                    parseCorrectSecondAttempts(score, line);
                } else if (line.startsWith("Incorrect Attempts:")) {
                    parseIncorrectAttempts(score, line);
                } else if (line.startsWith("Total Score:")) {
                    parseTotalScore(score, line);
                } else if (line.trim().isEmpty() && score != null) {
                    scores.add(score); // Add score to the list at the end of a block
                    score = null;
                }
            }

            // Add the last score if the file does not end with a blank line
            if (score != null) {
                scores.add(score);
            }
        }

        return scores;
    }

    public void writeToFile(final BufferedWriter writer) throws IOException {
        if (writer == null)
        {
            throw new IllegalArgumentException("BufferedWriter cannot be null");
        }

        writer.write("Date and Time: " + dateTimePlayed.format(FORMATTER) + System.lineSeparator());
        writer.write("Games Played: " + numGamesPlayed + System.lineSeparator());
        writer.write("Correct First Attempts: " + numCorrectFirstAttempt + System.lineSeparator());
        writer.write("Correct Second Attempts: " + numCorrectSecondAttempt + System.lineSeparator());
        writer.write("Incorrect Attempts:" + numIncorrectAttempts + System.lineSeparator());
        writer.write("Total Score: " + totalScore + " points" + System.lineSeparator());
        writer.write(String.format("Average Score: %.2f points/game", getAverageScore()));
        writer.write(System.lineSeparator());
        writer.write(System.lineSeparator()); // Add a blank line to separate scores
    }

    @Override
    public String toString() {
        return "Date and Time: " + dateTimePlayed.format(FORMATTER) +
                "\nGames Played: " + numGamesPlayed +
                "\nCorrect First Attempts: " + numCorrectFirstAttempt +
                "\nCorrect Second Attempts: " + numCorrectSecondAttempt +
                "\nIncorrect Attempts: " + numIncorrectAttempts +
                "\nScore: " + totalScore + " points" +
                "\n";
    }

    public void printRoundStats() {
        System.out.println(numGamesPlayed + " word games played");
        System.out.println(numCorrectFirstAttempt + " correct answers on the first attempt");
        System.out.println(numCorrectSecondAttempt + " Correct answers on second attempt");
        System.out.println(numIncorrectAttempts + " Incorrect Attempts on two attempts each");
    }

    public static void appendScoreToFile(final Score score, final String SCORE_FILE) {
        if (score == null)
        {
            throw new IllegalArgumentException("Score cannot be null");
        }

        if (SCORE_FILE == null)
        {
            throw new IllegalArgumentException("Score file path cannot be null");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_FILE, true))) {
            score.writeToFile(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseGamesPlayed(final Score score, final String line)
    {
        if (score == null)
        {
            return;
        }

        String[] parts = line.split(": ", 2);
        if (parts.length == 2)
        {
            score.numGamesPlayed = Integer.parseInt(parts[1]);
        }
    }

    private static void parseCorrectFirstAttempts(final Score score, final String line)
    {
        if (score == null)
        {
            return;
        }

        String[] parts = line.split(": ", 2);
        if (parts.length == 2)
        {
            score.numCorrectFirstAttempt = Integer.parseInt(parts[1]);
        }
    }

    private static void parseCorrectSecondAttempts(final Score score, final String line)
    {
        if (score == null)
        {
            return;
        }

        String[] parts = line.split(": ", 2);
        if (parts.length == 2)
        {
            score.numCorrectSecondAttempt = Integer.parseInt(parts[1]);
        }
    }

    private static void parseIncorrectAttempts(final Score score, final String line)
    {
        if (score == null)
        {
            return;
        }

        String[] parts = line.split(": ", 2);
        if (parts.length == 2)
        {
            score.numIncorrectAttempts = Integer.parseInt(parts[1]);
        }
    }

    private static void parseTotalScore(final Score score, final String line)
    {
        if (score == null)
        {
            return;
        }

        String[] parts = line.split(": ", 2);
        if (parts.length == 2)
        {
            score.totalScore = Integer.parseInt(parts[1].split(" ")[0]); // Extract numeric score
        }
    }
}
