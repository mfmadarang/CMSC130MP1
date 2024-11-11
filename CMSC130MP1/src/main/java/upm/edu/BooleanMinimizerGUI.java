package upm.edu;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;

import java.util.Arrays;

public class BooleanMinimizerGUI extends Application {

    private static final String DARK_BG = "#1a1a1a";
    private static final String DARKER_BG = "#141414";
    private static final String ACCENT = "#6366f1";
    private static final String TEXT_COLOR = "#e2e8f0";
    private static final String SECONDARY_TEXT = "#94a3b8";

    @Override
    public void start(Stage primaryStage) {
        // Create main container with padding
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(35));
        mainContainer.setStyle(
                "-fx-background-color: " + DARK_BG + ";" +
                        "-fx-background-radius: 20;"
        );

        // Header with modern font
        Label titleLabel = new Label("Boolean Function Minimizer");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 28));
        titleLabel.setStyle(
                "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );

        // Input container with glass-morphism effect
        VBox inputContainer = new VBox(20);
        inputContainer.setStyle(
                "-fx-background-color: " + DARKER_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);"
        );
        inputContainer.setPrefWidth(550);

        // Minterms input
        Label mintermsLabel = new Label("Minterms");
        mintermsLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 14));
        mintermsLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        TextField mintermsInput = new TextField();
        mintermsInput.setPromptText("Enter minterms (e.g., 1,3,7,8)");
        styleTextField(mintermsInput);

        // Variables input
        Label variablesLabel = new Label("Variables");
        variablesLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 14));
        variablesLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");
        TextField variablesInput = new TextField();
        variablesInput.setPromptText("Enter variables (e.g., A,B,C)");
        styleTextField(variablesInput);

        // Buttons container
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10, 0, 5, 0));

        Button solveButton = new Button("Solve");
        Button clearButton = new Button("Clear");

        // Add hover effect to buttons
        solveButton.setOnMouseEntered(e -> solveButton.setStyle(getButtonStyle(true, true)));
        solveButton.setOnMouseExited(e -> solveButton.setStyle(getButtonStyle(true, false)));
        clearButton.setOnMouseEntered(e -> clearButton.setStyle(getButtonStyle(false, true)));
        clearButton.setOnMouseExited(e -> clearButton.setStyle(getButtonStyle(false, false)));

        styleButton(solveButton, true);
        styleButton(clearButton, false);

        buttonContainer.getChildren().addAll(solveButton, clearButton);

        // Output area with custom styling
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Minimized Boolean Expression will appear here");
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(4);
        styleTextArea(outputArea);

        // Error label with modern styling
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444;");
        errorLabel.setFont(Font.font("Inter", FontWeight.MEDIUM, 12));

        // Footer with names and modern styling
        Label footerLabel = new Label("Created by: Madarang & Sahagon");
        footerLabel.setStyle(
                "-fx-text-fill: " + SECONDARY_TEXT + ";" +
                        "-fx-padding: 15 0 0 0;" +
                        "-fx-font-family: 'Inter';" +
                        "-fx-font-size: 12px;"
        );
        footerLabel.setAlignment(Pos.CENTER);

        // Add all elements to input container
        inputContainer.getChildren().addAll(
                mintermsLabel, mintermsInput,
                variablesLabel, variablesInput,
                buttonContainer
        );

        // Add all elements to main container
        mainContainer.getChildren().addAll(
                titleLabel,
                inputContainer,
                outputArea,
                errorLabel,
                footerLabel
        );

        // Button actions
        solveButton.setOnAction(e -> {
            errorLabel.setText("");
            outputArea.clear();

            String mintermsText = mintermsInput.getText().trim();
            String variablesText = variablesInput.getText().trim();

            if (!mintermsText.matches("^(\\d+)(,\\d+)*$")) {
                errorLabel.setText("Error: Minterms must be integers separated by commas.");
                return;
            }

            String[] mintermsArrayStr = mintermsText.split(",");
            int[] minterms = Arrays.stream(mintermsArrayStr).mapToInt(Integer::parseInt).toArray();

            int maxMinterm = Arrays.stream(minterms).max().orElse(0);
            int bitLength = (int) Math.ceil(Math.log(maxMinterm + 1) / Math.log(2));

            if (!variablesText.matches("^[A-Za-z](,[A-Za-z])*$")) {
                errorLabel.setText("Error: Variables must be letters separated by commas.");
                return;
            }

            String[] variables = variablesText.split(",");
            if (variables.length != bitLength) {
                errorLabel.setText("Error: Number of variables must match required bit length of minterms.");
                return;
            }

            try {
                String minimizedExpression = QuineMcCluskey.minimize(minterms, variables);
                outputArea.setText("Minimized Boolean Expression:\n" + minimizedExpression);
            } catch (Exception ex) {
                errorLabel.setText("An error occurred during minimization.");
            }
        });

        clearButton.setOnAction(e -> {
            mintermsInput.clear();
            variablesInput.clear();
            outputArea.clear();
            errorLabel.setText("");
        });

        // Scene setup with dark theme
        Scene scene = new Scene(mainContainer);
        scene.setFill(Color.valueOf(DARK_BG));
        primaryStage.setTitle("Boolean Function Minimizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void styleTextField(TextField textField) {
        textField.setStyle(
                "-fx-background-color: " + DARKER_BG + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #3f3f46;" +
                        "-fx-border-radius: 10;" +
                        "-fx-text-fill: " + TEXT_COLOR + ";" +
                        "-fx-prompt-text-fill: " + SECONDARY_TEXT + ";" +
                        "-fx-padding: 12;" +
                        "-fx-font-family: 'Inter';"
        );
    }

    private String getButtonStyle(boolean isPrimary, boolean isHover) {
        String baseColor = isPrimary ? ACCENT : "#4b5563";
        String hoverColor = isPrimary ? "#818cf8" : "#6b7280";

        return "-fx-background-color: " + (isHover ? hoverColor : baseColor) + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 12 24;" +
                "-fx-font-family: 'Inter';" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;";
    }

    private void styleButton(Button button, boolean isPrimary) {
        button.setStyle(getButtonStyle(isPrimary, false));

        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setSpread(0.1);
        button.setEffect(shadow);
    }

    private void styleTextArea(TextArea textArea) {
        textArea.setStyle(
                "-fx-control-inner-background: #0f172a;" +     // Darker blue-gray background
                        "-fx-background-color: #0f172a;" +             // Same as inner background
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #3f3f46;" +
                        "-fx-border-radius: 10;" +
                        "-fx-text-fill: #38bdf8;" +                    // Bright blue text
                        "-fx-prompt-text-fill: #94a3b8;" +
                        "-fx-padding: 10;" +
                        "-fx-font-family: 'Inter';"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}