package numbergame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

public class NumberGame extends Application
{
    private int[] randomNumbers; // Array to hold the random numbers
    private int currentIndex = 0; // Index of the next number to place

    @Override
    public void start(final Stage primaryStage)
    {
        generateRandomNumber();

//        // Create starting page
//        // Create a label
//        final Label welcomeMessage = new Label("Welcome to the 20-number Challenge! Click \"start\"");
//        Label welcomeLabel = new Label("fill up the buttons with random numbers by clicking them");
//        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-alignment: center;"); // Style the label
//

        // Label to display the next number
        Label buttonSetupLabel = new Label("  Select a slot." + System.lineSeparator() + "  Next number: " + randomNumbers[currentIndex]);
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
        VBox vBox = new VBox(10); // Vertical gap of 10
        vBox.getChildren().addAll(buttonSetupLabel, gridPane); // Add label and gridPane to VBox

        // Create a scene and add the GridPane
        Scene scene = new Scene(vBox, 500, 400);

        // Set up the stage
        primaryStage.setTitle("20-Number Challenge");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void generateRandomNumber(){
        Random random = new Random();
        randomNumbers = new int[20];

        System.out.println("Randomly selected integers between 1 and 1000:");
        for (int i = 0; i < 20; i++)
        {
            randomNumbers[i] = random.nextInt(1000) + 1;
//            System.out.println(randomNumbers[i]);
        }
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}
