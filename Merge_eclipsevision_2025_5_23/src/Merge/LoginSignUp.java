package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginSignUp extends Login {
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadCredentials();

        primaryStage.setTitle("Sign Up");
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);

        showSignUpScene();
    }

    private void showSignUpScene() {
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #FFD4EC54;");

        // Title
        Label titleLabel = new Label("Sign Up");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#855FAF"));

        // Instruction
        Label instructionLabel = new Label("Create your new account");
        instructionLabel.setFont(Font.font("Arial", 16));
        instructionLabel.setTextFill(Color.web("#666666"));

        // Form container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);
        formContainer.setPadding(new Insets(30, 40, 30, 40));
        formContainer.setStyle("-fx-background-color: rgba(237,223,248,0.88); -fx-border-radius: 5; -fx-background-radius: 5;");

        // Username field
        Label usernameLabel = new Label("Choose a username");
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.web("#333333"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (4-20 characters)");
        usernameField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Password field
        Label passwordLabel = new Label("Create password");
        passwordLabel.setFont(Font.font("Arial", 14));
        passwordLabel.setTextFill(Color.web("#333333"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 8 characters)");
        passwordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm password");
        confirmPasswordLabel.setFont(Font.font("Arial", 14));
        confirmPasswordLabel.setTextFill(Color.web("#333333"));

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        confirmPasswordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Register button
        Button registerButton = new Button("Create Account");
        registerButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 200px; -fx-pref-height: 40px;");
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPass = confirmPasswordField.getText().trim();

            if (!password.equals(confirmPass)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }

            int signupResult = handleSignup(username, password);  // 只调用一次

            if (signupResult == 1) {
                new Discover().start(new Stage());
                primaryStage.close();
            }
            else if (signupResult == 0) {
                new LoginSignUp().start(new Stage());
                primaryStage.close();
            }
        });

        // Back to login link
        Hyperlink backToLoginLink = new Hyperlink("Back to Login");
        backToLoginLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        backToLoginLink.setOnAction(e -> {
            primaryStage.close();
            new Login().start(new Stage());
        });

        // Add components to form
        formContainer.getChildren().addAll(
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                confirmPasswordLabel, confirmPasswordField,
                registerButton, backToLoginLink
        );

        // Add components to main container
        mainContainer.getChildren().addAll(titleLabel, instructionLabel, formContainer);

        // Create scene
        Scene signUpScene = new Scene(mainContainer);
        primaryStage.setScene(signUpScene);
        primaryStage.show();
    }

    private int handleSignup(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return 0;
        }

        if (username.length() < 4 || username.length() > 20) {
            showAlert("Error", "Username must be 4-20 characters long.");
            return 0;
        }

        if (password.length() < 8) {
            showAlert("Error", "Password must be at least 8 characters long.");
            return 0;
        }

        if (userCredentials.containsKey(username)) {
            showAlert("Error", "Username already exists.");
            return 0;
        } else {
            userCredentials.put(username, password);
            saveCredentials();
            showAlert("Success", "Account created successfully!");
            return 1;
        }
    }
}