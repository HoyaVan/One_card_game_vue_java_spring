package numbergame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NumberGame2 extends Application {
    private int[] randomNumbers; // Array to hold the random numbers
    private int currentIndex = 0; // Index of the next number to place
    private List<Integer> gridValues; // Holds current grid values
    private int gamesPlayed = 0; // Total games played
    private int wins = 0; // Total wins
    private int losses = 0; // Total losses
    private int totalPlacements = 0; // Total successful placements

    @Override
    public void start(final Stage primaryStage) {
        generateRandomNumbers();
        gridValues = new ArrayList<>();

        Label buttonSetupLabel = new Label("Next number: " + randomNumbers[currentIndex]);
        buttonSetupLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: blue; -fx-alignment: center;");

        // Create a GridPane
        final GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Number of rows and columns
        int rows = 5;
        int columns = 4;

        // Create buttons and add to GridPane
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                Button button = new Button("[]");
                button.setMinWidth(100); // Set minimum width
                button.setMinHeight(50); // Set minimum height

                // Add click event handler
                button.setOnAction(event -> {
                    if (button.getText().equals("[]")) {
                        int currentNumber = randomNumbers[currentIndex];

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

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(buttonSetupLabel, gridPane);

        Scene scene = new Scene(vBox, 500, 400);

        primaryStage.setTitle("20-Number Challenge");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean isPlacementValid(int currentNumber) {
        // Ensure numbers are placed in ascending order
        for (int value : gridValues) {
            if (currentNumber < value) {
                return false;
            }
        }
        return true;
    }

    private void generateRandomNumbers() {
        Random random = new Random();
        randomNumbers = new int[20];

        for (int i = 0; i < 20; i++) {
            randomNumbers[i] = random.nextInt(1000) + 1;
        }
    }

    private void showAlert(String title, String content, Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.setContentText("Do you want to retry?");
        Button retryButton = new Button("Retry");
        retryButton.setOnAction(e -> restartGame(primaryStage));
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> showScore(primaryStage));
        alert.showAndWait();
    }

    private void restartGame(Stage primaryStage) {
        currentIndex = 0;
        gridValues.clear();
        generateRandomNumbers();
        start(primaryStage);
    }

    private void showScore(Stage primaryStage) {
        Alert scoreAlert = new Alert(Alert.AlertType.INFORMATION);
        scoreAlert.setTitle("Game Stats");
        int averagePlacements = gamesPlayed > 0 ? totalPlacements / gamesPlayed : 0;
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
