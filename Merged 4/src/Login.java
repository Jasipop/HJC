//package Nutllet;

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

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Login extends Application {
    private Stage loginStage;
    private Scene loginScene;

    private Map<String, String> userCredentials = new HashMap<>();
    private static final String CREDENTIALS_FILE = "user_credentials.csv";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.loginStage = primaryStage;
        loadCredentials();

        primaryStage.setTitle("Login System");
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #FFD4EC54;");

        // Login title
        Label titleLabel = new Label("Login");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#855FAF"));

        // Instruction text
        Label instructionLabel = new Label("Enter your access details below");
        instructionLabel.setFont(Font.font("Arial", 16));
        instructionLabel.setTextFill(Color.web("#666666"));

        // Form container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);
        formContainer.setPadding(new Insets(30, 40, 30, 40));
        formContainer.setStyle("-fx-background-color: rgba(237,223,248,0.88); -fx-border-radius: 5; -fx-background-radius: 5;");

        // Username field
        Label usernameLabel = new Label("Enter your username");
        usernameLabel.setFont(Font.font("Arial", 14));
        usernameLabel.setTextFill(Color.web("#333333"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Password field
        Label passwordLabel = new Label("Enter your password");
        passwordLabel.setFont(Font.font("Arial", 14));
        passwordLabel.setTextFill(Color.web("#333333"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // Login button
        Button loginButton = new Button("Log in");
        loginButton.setStyle("-fx-background-color: #855faf; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 40px;");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        // Signup button
        Button signupButton = new Button("Click to sign up");
        signupButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 150px; -fx-pref-height: 40px;");
        signupButton.setOnAction(e -> showRegistrationForm());


        // Forgot password link
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot your password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        forgotPasswordLink.setOnAction(e -> showAlert("Password Recovery", "Please contact support to reset your password."));

        // Add components to form
        formContainer.getChildren().addAll(
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                loginButton, signupButton,
                forgotPasswordLink
        );

        // Add components to main container
        mainContainer.getChildren().addAll(titleLabel, instructionLabel, formContainer);

        // Set scene
        loginScene = new Scene(mainContainer);  // 将场景保存到成员变量
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
            showAlert("Success", "Login successful!");

            // 启动主界面
            Stage nutlletStage = new Stage();
            try {
                new Nutllet().start(nutlletStage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 关闭当前登录窗口
            loginStage.close();

        } else {
            showAlert("Error", "Invalid username or password.");
        }
    }

    private void handleSignup(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        if (userCredentials.containsKey(username)) {
            showAlert("Error", "Username already exists.");
        } else {
            userCredentials.put(username, password);
            saveCredentials();
            showAlert("Success", "Account created successfully!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCredentials() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showRegistrationForm() {
        // 创建注册页面容器
        VBox regMainContainer = new VBox(20);
        regMainContainer.setAlignment(Pos.CENTER);
        regMainContainer.setPadding(new Insets(20));
        regMainContainer.setStyle("-fx-background-color: #FFD4EC54;");

        // 注册标题
        Label regTitleLabel = new Label("Sign Up");
        regTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        regTitleLabel.setTextFill(Color.web("#855FAF"));

        // 注册说明
        Label regInstruction = new Label("Create your new account");
        regInstruction.setFont(Font.font("Arial", 16));
        regInstruction.setTextFill(Color.web("#666666"));

        // 注册表单容器
        VBox regFormContainer = new VBox(15);
        regFormContainer.setAlignment(Pos.CENTER);
        regFormContainer.setMaxWidth(400);
        regFormContainer.setPadding(new Insets(30, 40, 30, 40));
        regFormContainer.setStyle("-fx-background-color: rgba(237,223,248,0.88); -fx-border-radius: 5; -fx-background-radius: 5;");

        // 用户名输入
        Label regUserLabel = new Label("Choose a username");
        regUserLabel.setFont(Font.font("Arial", 14));
        regUserLabel.setTextFill(Color.web("#333333"));

        TextField regUsernameField = new TextField();
        regUsernameField.setPromptText("Username (4-20 characters)");
        regUsernameField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // 密码输入
        Label regPassLabel = new Label("Create password");
        regPassLabel.setFont(Font.font("Arial", 14));
        regPassLabel.setTextFill(Color.web("#333333"));

        PasswordField regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password (min 8 characters)");
        regPasswordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // 确认密码
        Label confirmPassLabel = new Label("Confirm password");
        confirmPassLabel.setFont(Font.font("Arial", 14));
        confirmPassLabel.setTextFill(Color.web("#333333"));

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        confirmPasswordField.setStyle("-fx-pref-height: 40; -fx-font-size: 14;");

        // 注册按钮
        Button registerButton = new Button("Create Account");
        registerButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 200px; -fx-pref-height: 40px;");

        // 返回登录链接
        Hyperlink backToLoginLink = new Hyperlink("Back to Login");
        backToLoginLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");

        // 组装注册表单
        regFormContainer.getChildren().addAll(
                regUserLabel, regUsernameField,
                regPassLabel, regPasswordField,
                confirmPassLabel, confirmPasswordField,
                registerButton, backToLoginLink
        );

        // 组装主容器
        regMainContainer.getChildren().addAll(regTitleLabel, regInstruction, regFormContainer);

        // 创建新场景
        Scene regScene = new Scene(regMainContainer, 1366, 768);

        // 注册按钮事件处理
        registerButton.setOnAction(e -> {
            String username = regUsernameField.getText().trim();
            String password = regPasswordField.getText().trim();
            String confirmPass = confirmPasswordField.getText().trim();

            if (!password.equals(confirmPass)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }

            handleSignup(username, password); // 复用原有的注册逻辑
            loginStage.setScene(loginScene); // 返回登录页
        });

        // 返回登录链接事件
        backToLoginLink.setOnAction(e ->
                loginStage.setScene(loginScene)  // 使用保存的登录场景
        );

        // 切换场景
        loginStage.setScene(regScene);
    }

}