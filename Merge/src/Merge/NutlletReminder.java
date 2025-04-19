package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class NutlletReminder extends Application {
    private static final Color PRIMARY_COLOR = Color.rgb(202, 182, 244);
    private static final Color BACKGROUND_COLOR = Color.rgb(255, 212, 236, 0.33);
    private static final Color TEXT_COLOR = Color.BLACK;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createBottomNav(primaryStage));

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Nutllet - Reminders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Reminders");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(title);
        return header;
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        mainContent.setAlignment(Pos.TOP_CENTER);

        Button reminder1 = createReminderButton("Loan Repayment Reminder", "Monthly progress 30%");
        Button reminder2 = createReminderButton("Pension Planning", "This month's quota has been used up to 70%.");
        Button addReminderButton = new Button("Add New Reminder");

        stylePrimaryButton(addReminderButton);

        addReminderButton.setOnAction(e -> {
            try {
                new NutlletAddNewReminder().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainContent.getChildren().addAll(reminder1, reminder2, addReminderButton);
        return mainContent;
    }

    private Button createReminderButton(String titleText, String descriptionText) {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        content.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_COLOR);

        Label description = new Label(descriptionText);
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        description.setTextFill(TEXT_COLOR);

        HBox progressBarContainer = new HBox();
        progressBarContainer.setBackground(new Background(new BackgroundFill(
                Color.LIGHTGRAY, new CornerRadii(6), Insets.EMPTY)));
        progressBarContainer.setPrefHeight(12);

        Region progressBar = new Region();
        progressBar.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, new CornerRadii(6), Insets.EMPTY)));
        progressBar.setPrefWidth(titleText.equals("Loan Repayment Reminder") ? 120 : 280);
        progressBarContainer.getChildren().add(progressBar);

        content.getChildren().addAll(title, progressBarContainer, description);

        Button button = new Button();
        button.setGraphic(content);
        button.setBackground(Background.EMPTY);
        button.setPrefWidth(400);
        button.setStyle("-fx-cursor: pointer;");
        return button;
    }

    private HBox createBottomNav(Stage primaryStage) {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // 创建导航按钮
        Button homeBtn = createNavButtonWithEmoji("Home", "🏠");
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙");

        // 设置按钮事件
        homeBtn.setOnAction(e -> {
            try {
                new Nutllet().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        discoverBtn.setOnAction(e -> {
            try {
                new Discover().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        settingsBtn.setOnAction(e -> {
            try {
                new Settings().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 调整按钮顺序（Home -> Discover -> Settings）
        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);
        return navBar;
    }

    private Button createNavButtonWithEmoji(String label, String emoji) {
        VBox btnContainer = new VBox();
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(4);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 18px;");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        btnContainer.getChildren().addAll(emojiLabel, textLabel);

        Button button = new Button();
        button.setPrefWidth(1366 / 3.0);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: transparent; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;"));

        return button;
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-text-fill: white; "
                + "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-padding: 12px 24px;"
                + "-fx-border-radius: 30px;"
                + "-fx-background-radius: 30px;"
                + "-fx-cursor: pointer;"
                + "-fx-font-weight: 500;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: white; "
                + "-fx-background-color: " + toHexString(PRIMARY_COLOR.darker()) + ";"
                + "-fx-padding: 12px 24px;"
                + "-fx-border-radius: 30px;"
                + "-fx-background-radius: 30px;"
                + "-fx-cursor: pointer;"
                + "-fx-font-weight: 500;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: white; "
                + "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-padding: 12px 24px;"
                + "-fx-border-radius: 30px;"
                + "-fx-background-radius: 30px;"
                + "-fx-cursor: pointer;"
                + "-fx-font-weight: 500;"));
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}