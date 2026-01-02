package numbergame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NumberGame2 extends Application {
    // Game configuration constants
    private static final int MIN_INDEX = 0;
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLUMNS = 4;
    private static final int TOTAL_NUMBERS = 20;
    private static final int RANDOM_MIN = 1;
    private static final int RANDOM_MAX = 1000;
    
    // UI layout constants
    private static final int GRID_HORIZONTAL_GAP = 10;
    private static final int GRID_VERTICAL_GAP = 10;
    private static final int GRID_PADDING = 20;
    private static final int VBOX_GAP = 10;
    private static final int BUTTON_MIN_WIDTH = 100;
    private static final int BUTTON_MIN_HEIGHT = 50;
    private static final int SCENE_WIDTH = 500;
    private static final int SCENE_HEIGHT = 400;
    private static final int FONT_SIZE = 16;
    

    // Statistics constants
    private static final int INITIAL_COUNT = 0;
    
    private int[] randomNumbers; // Array to hold the random numbers
    private int currentIndex = 0; // Index of the next number to place
    private List<Integer> gridValues; // Holds current grid values
    private int gamesPlayed = 0; // Total games played
    private int wins = 0; // Total wins
    private int losses = 0; // Total losses
    private int totalPlacements = 0; // Total successful placements

    @Override
    public void start(final Stage primaryStage) {
        final Label buttonSetupLabel;
        final GridPane gridPane;
        final int rows;
        final int columns;
        final VBox vBox;
        final Scene scene;

        generateRandomNumbers();
        gridValues = new ArrayList<>();

        buttonSetupLabel = new Label("Next number: " + randomNumbers[currentIndex]);
        buttonSetupLabel.setStyle("-fx-font-size: " + FONT_SIZE + "px; -fx-text-fill: blue; -fx-alignment: center;");

        // Create a GridPane
        gridPane = new GridPane();
        gridPane.setHgap(GRID_HORIZONTAL_GAP);
        gridPane.setVgap(GRID_VERTICAL_GAP);
        gridPane.setStyle("-fx-padding: " + GRID_PADDING + "; -fx-alignment: center;");

        // Number of rows and columns
        rows = GRID_ROWS;
        columns = GRID_COLUMNS;

        // Create buttons and add to GridPane
        for (int row = MIN_INDEX; row < rows; row++) {
            for (int col = MIN_INDEX; col < columns; col++) {
                final Button button;
                
                button = new Button("[]");
                button.setMinWidth(BUTTON_MIN_WIDTH); // Set minimum width
                button.setMinHeight(BUTTON_MIN_HEIGHT); // Set minimum height

                // Add click event handler
                button.setOnAction(event -> {
                    if (button.getText().equals("[]")) {
                        final int currentNumber;
                        
                        currentNumber = randomNumbers[currentIndex];

                        // Check if the placement is valid
                        if (isPlacementValid(currentNumber)) {
                            button.setText(String.valueOf(currentNumber));
                            gridValues.add(currentNumber);
                            currentIndex++;
                            totalPlacements++;

                            // Check if all numbers are placed
                            if (currentIndex < randomNumbers.length) {
                                buttonSetupLabel.setText("Next number: " + randomNumbers[currentIndex]);
                            } else {
                                wins++;
                                gamesPlayed++;
                                showAlert("You Win!", "Congratulations! You placed all numbers correctly.", primaryStage);
                            }
                        } else {
                            losses++;
                            gamesPlayed++;
                            showAlert("Game Over", "Invalid placement! You lost. Try again?", primaryStage);
                        }
                    } else {
                        System.out.println("Square already occupied. Click ignored.");
                    }
                });
                gridPane.add(button, col, row); // Add button to grid
            }
        }

        vBox = new VBox(VBOX_GAP);
        vBox.getChildren().addAll(buttonSetupLabel, gridPane);

        scene = new Scene(vBox, SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.setTitle(TOTAL_NUMBERS + "-Number Challenge");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean isPlacementValid(final int currentNumber) {
        // Ensure numbers are placed in ascending order
        for (final int value : gridValues) {
            if (currentNumber < value) {
                return false;
            }
        }
        return true;
    }

    private void generateRandomNumbers() {
        final Random random;
        
        random = new Random();
        randomNumbers = new int[TOTAL_NUMBERS];

        for (int i = MIN_INDEX; i < TOTAL_NUMBERS; i++) {
            randomNumbers[i] = random.nextInt(RANDOM_MAX) + RANDOM_MIN;
        }
    }

    private void showAlert(final String title, final String content, final Stage primaryStage) {
        final Alert alert;
        final Button retryButton;
        final Button quitButton;
        
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.setContentText("Do you want to retry?");
        retryButton = new Button("Retry");
        retryButton.setOnAction(e -> restartGame(primaryStage));
        quitButton = new Button("Quit");
        quitButton.setOnAction(e -> showScore(primaryStage));
        alert.showAndWait();
    }

    private void restartGame(final Stage primaryStage) {
        currentIndex = INITIAL_COUNT;
        gridValues.clear();
        generateRandomNumbers();
        start(primaryStage);
    }

    private void showScore(final Stage primaryStage) {
        final Alert scoreAlert;
        final int averagePlacements;
        
        scoreAlert = new Alert(Alert.AlertType.INFORMATION);
        scoreAlert.setTitle("Game Stats");
        averagePlacements = gamesPlayed > INITIAL_COUNT ? totalPlacements / gamesPlayed : INITIAL_COUNT;
        scoreAlert.setHeaderText("Score Summary");
        scoreAlert.setContentText("Games Played: " + gamesPlayed + "\nWins: " + wins + "\nLosses: " + losses +
                "\nTotal Placements: " + totalPlacements + "\nAverage Placements: " + averagePlacements);
        scoreAlert.showAndWait();
        primaryStage.close();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
