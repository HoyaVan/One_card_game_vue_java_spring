import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Score {
    private LocalDateTime dateTimePlayed = LocalDateTime.now(); // Current game timestamp
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int numGamesPlayed;
    private int numCorrectFirstAttempt;
    private int numCorrectSecondAttempt;
    private int numIncorrectAttempts;
    private int totalScore;

    public Score() {
        this.dateTimePlayed = LocalDateTime.now(); // Sets the current timestamp
        this.numGamesPlayed = 0; // Initializes games played to 0
        this.numCorrectFirstAttempt = 0; // Initializes correct first attempts to 0
        this.numCorrectSecondAttempt = 0; // Initializes correct second attempts to 0
        this.numIncorrectAttempts = 0; // Initializes incorrect attempts to 0
        this.totalScore = 0; // Initializes total score to 0
    }

    public Score(LocalDateTime dateTimePlayed,
                 int numGamesPlayed,
                 int numCorrectFirstAttempt,
                 int numCorrectSecondAttempt,
                 int numIncorrectAttempts,
                 int totalScore)
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
    public void recordFirstAttempt(boolean isCorrect) {
        if (isCorrect) {
            numCorrectFirstAttempt++;
            totalScore += 2;
        }
    }

    // Record second attempt
    public void recordSecondAttempt(boolean isCorrect) {
        if (isCorrect) {
            numCorrectSecondAttempt++;
            totalScore ++;
        } else {
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

    public void printRoundStats() {
        System.out.println(numGamesPlayed + " word games played");
        System.out.println(numCorrectFirstAttempt + " correct answers on the first attempt");
        System.out.println(numCorrectSecondAttempt + " Correct answers on second attempt");
        System.out.println(numIncorrectAttempts + " Incorrect Attempts on two attempts each");
    }

    public static Score parseHighScoreFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            LocalDateTime dateTimePlayed = null;
            double maxAverageScore = 0.0;
            Score highScore = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Date and Time:")) {
                    String dateTimeString = line.split(": ", 2)[1].trim();
                    dateTimePlayed = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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

    public void add(Score other) {
        this.numGamesPlayed += other.numGamesPlayed;
        this.numCorrectFirstAttempt += other.numCorrectFirstAttempt;
        this.numCorrectSecondAttempt += other.numCorrectSecondAttempt;
        this.numIncorrectAttempts += other.numIncorrectAttempts;
        this.totalScore += other.totalScore;
    }

    public static void appendScoreToFile(Score score, String SCORE_FILE) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_FILE, true))) {
            score.writeToFile(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Score> readScoresFromFile(String SCORE_FILE) throws IOException {
        List<Score> scores = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_FILE))) {
            String line;
            Score score = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Date and Time:")) {
                    score = new Score();
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.dateTimePlayed = LocalDateTime.parse(parts[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                } else if (line.startsWith("Games Played:")) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.numGamesPlayed = Integer.parseInt(parts[1]);
                    }
                } else if (line.startsWith("Correct First Attempts:")) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.numCorrectFirstAttempt = Integer.parseInt(parts[1]);
                    }
                } else if (line.startsWith("Correct Second Attempts:")) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.numCorrectSecondAttempt = Integer.parseInt(parts[1]);
                    }
                } else if (line.startsWith("Incorrect Attempts:")) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.numIncorrectAttempts = Integer.parseInt(parts[1]);
                    }
                } else if (line.startsWith("Total Score:")) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        score.totalScore = Integer.parseInt(parts[1].split(" ")[0]); // Extract numeric score
                    }
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

    public void writeToFile(BufferedWriter writer) throws IOException {
        writer.write("Date and Time: " + dateTimePlayed.format(formatter) + System.lineSeparator());
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
        return "Date and Time: " + dateTimePlayed.format(formatter) +
                "\nGames Played: " + numGamesPlayed +
                "\nCorrect First Attempts: " + numCorrectFirstAttempt +
                "\nCorrect Second Attempts: " + numCorrectSecondAttempt +
                "\nIncorrect Attempts: " + numIncorrectAttempts +
                "\nScore: " + totalScore + " points" +
                "\n";
    }

}
