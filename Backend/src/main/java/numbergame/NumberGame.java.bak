package numbergame;

import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NumberGame extends Application
{
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
    
    private int[] randomNumbers; // Array to hold the random numbers
    private int currentIndex = 0; // Index of the next number to place

    @Override
    public void start(final Stage primaryStage)
    {
        final Label buttonSetupLabel;
        final GridPane gridPane;
        final int rows;
        final int columns;
        final VBox vBox;
        final Scene scene;

        generateRandomNumber();

        // Label to display the next number
        buttonSetupLabel = new Label("  Select a slot." + System.lineSeparator() + "  Next number: " + randomNumbers[currentIndex]);
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
                        // Place the next random number
                        button.setText(String.valueOf(randomNumbers[currentIndex]));
                        currentIndex++; // Move to the next number

                        // Check if all numbers are placed
                        if (currentIndex < randomNumbers.length) {
                            buttonSetupLabel.setText("  Next number: " + randomNumbers[currentIndex]);
                        } else {
                            buttonSetupLabel.setText("All numbers placed! Game over.");
                        }
                    } else {
                        // Ignore clicks on already occupied squares
                        System.out.println("Square already occupied. Click ignored.");
                    }
                });
                gridPane.add(button, col, row); // Add button to grid
            }
        }

        // Use a VBox to stack the label and GridPane
        vBox = new VBox(VBOX_GAP); // Vertical gap
        vBox.getChildren().addAll(buttonSetupLabel, gridPane); // Add label and gridPane to VBox

        // Create a scene and add the GridPane
        scene = new Scene(vBox, SCENE_WIDTH, SCENE_HEIGHT);

        // Set up the stage
        primaryStage.setTitle(TOTAL_NUMBERS + "-Number Challenge");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void generateRandomNumber(){
        final Random random;

        random = new Random();
        randomNumbers = new int[TOTAL_NUMBERS];

        System.out.println("Randomly selected integers between " + RANDOM_MIN + " and " + RANDOM_MAX + ":");

        for (int i = MIN_INDEX; i < TOTAL_NUMBERS; i++)
        {
            randomNumbers[i] = random.nextInt(RANDOM_MAX) + RANDOM_MIN;
        }
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}
