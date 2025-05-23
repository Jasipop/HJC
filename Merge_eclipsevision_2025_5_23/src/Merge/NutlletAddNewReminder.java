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
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class NutlletAddNewReminder extends Application {
    private static final Color PRIMARY_COLOR = Color.rgb(202, 182, 244);
    private static final Color BACKGROUND_COLOR = Color.rgb(255, 212, 236, 0.33);
    private static final Color TEXT_COLOR = Color.BLACK;

    private TextField reminderNameField;
    private ComboBox<String> reminderTypeDropdown;
    private TextField minQuotaField;
    private TextField maxQuotaField;
    private TextArea remarkArea;
    private Reminder currentReminder;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        root.setTop(createHeader());
        root.setCenter(createFormContent(primaryStage));
        root.setBottom(createBottomNav(primaryStage));

        // 使用 ScrollPane 包装 root
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // 修改 Scene 初始化
        Scene scene = new Scene(scrollPane, 1366, 768);
        primaryStage.setTitle("Nutllet - Add New Reminder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Add New Reminder");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(title);
        return header;
    }

    private VBox createFormContent(Stage primaryStage) {
        VBox formContent = new VBox(20);
        formContent.setPadding(new Insets(20));
        formContent.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        formContent.setAlignment(Pos.TOP_CENTER);

        // Question 1: Reminder's Name
        VBox question1 = createQuestionWithTextField("1. Your new reminder's name?");

        // Question 2: Dropdown for Savings/Expense Monitoring
        VBox question2 = createQuestionWithDropdown(
                "2. This reminder is for savings or expense monitoring?",
                "For savings", "For expense monitoring"
        );

        // Question 3: Month Quota Range
        VBox question3 = createQuestionWithNumberRange(
                "3. What do you want your reminder's quota to be?"
        );

        // Question 4: Remark with "Done" Button
        VBox question4 = createQuestionWithTextAreaAndButtons("4. Remark", primaryStage);

        formContent.getChildren().addAll(question1, question2, question3, question4);
        return formContent;
    }

    private VBox createQuestionWithTextField(String questionText) {
        VBox questionBox = new VBox(10);
        Label questionLabel = new Label(questionText);
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        questionLabel.setTextFill(TEXT_COLOR);

        reminderNameField = new TextField();
        reminderNameField.setPromptText("Enter your answer here...");
        reminderNameField.setPrefWidth(400);

        questionBox.getChildren().addAll(questionLabel, reminderNameField);
        return questionBox;
    }

    private VBox createQuestionWithDropdown(String questionText, String... options) {
        VBox questionBox = new VBox(10);
        Label questionLabel = new Label(questionText);
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        questionLabel.setTextFill(TEXT_COLOR);

        reminderTypeDropdown = new ComboBox<>();
        reminderTypeDropdown.getItems().addAll(options);
        reminderTypeDropdown.setPromptText("Select an option");
        reminderTypeDropdown.setPrefWidth(400);

        questionBox.getChildren().addAll(questionLabel, reminderTypeDropdown);
        return questionBox;
    }

    private VBox createQuestionWithNumberRange(String questionText) {
        VBox questionBox = new VBox(10);
        Label questionLabel = new Label(questionText);
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        questionLabel.setTextFill(TEXT_COLOR);

        HBox numberRangeBox = new HBox(10);
        numberRangeBox.setAlignment(Pos.CENTER_LEFT);

        minQuotaField = new TextField();
        minQuotaField.setPromptText("Min");
        minQuotaField.setPrefWidth(80);

        Label separator = new Label("～");
        separator.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        separator.setTextFill(TEXT_COLOR);

        maxQuotaField = new TextField();
        maxQuotaField.setPromptText("Max");
        maxQuotaField.setPrefWidth(80);

        numberRangeBox.getChildren().addAll(minQuotaField, separator, maxQuotaField);
        questionBox.getChildren().addAll(questionLabel, numberRangeBox);
        return questionBox;
    }

    private VBox createQuestionWithTextAreaAndButtons(String questionText, Stage primaryStage) {
        VBox questionBox = new VBox(20);
        questionBox.setAlignment(Pos.TOP_LEFT);

        // Question Label
        Label questionLabel = new Label(questionText);
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        questionLabel.setTextFill(TEXT_COLOR);

        remarkArea = new TextArea();
        remarkArea.setPromptText("Enter your remarks here...");
        remarkArea.setPrefWidth(400);
        remarkArea.setPrefHeight(100);

        // Buttons Container
        VBox buttonsContainer = new VBox(10);
        buttonsContainer.setAlignment(Pos.CENTER);

        // Return Button
        Button returnButton = new Button("Return");
        stylePrimaryButton(returnButton);
        returnButton.setStyle("-fx-background-color: #95a5a6; " + returnButton.getStyle());

        // Done Button
        Button doneButton = new Button("Done");
        stylePrimaryButton(doneButton);

        // 设置 Return 按钮事件
        returnButton.setOnAction(e -> {
            try {
                new NutlletReminder().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 设置 Done 按钮事件
        doneButton.setOnAction(e -> {
            saveReminderData();
            try {
                new NutlletReminder().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonsContainer.getChildren().addAll(returnButton, doneButton);
        questionBox.getChildren().addAll(questionLabel, remarkArea, buttonsContainer);
        return questionBox;
    }

    public void setReminderData(Reminder reminder) {
        this.currentReminder = reminder;
        reminderNameField.setText(reminder.name);
        reminderTypeDropdown.setValue(reminder.type);
        minQuotaField.setText(String.valueOf(reminder.minAmount));
        maxQuotaField.setText(String.valueOf(reminder.maxAmount));
        remarkArea.setText(reminder.remark);
    }

    private void saveReminderData() {
        try {
            String reminderName = reminderNameField.getText();
            String reminderType = reminderTypeDropdown.getValue();
            String minQuota = minQuotaField.getText();
            String maxQuota = maxQuotaField.getText();
            String remark = remarkArea.getText();
            // 构建要保存的数据行
            String dataLine = String.format("\"Reminder\",\"%s\",\"%s\",\"¥%s-%s\",\"%s\"",
                    reminderName, reminderType, minQuota, maxQuota, remark);

            // 读取现有文件内容
            File file = new File("deals.csv");
            StringBuilder content = new StringBuilder();
            boolean foundInsertionPoint = false;
            boolean isReminderSection = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 在"微信支付账单明细列表"行之后插入新数据
                    if (line.contains("Type,Reminder name,Reminder Category,Amount Range,Note")) {
                        isReminderSection = true;
                        content.append(line).append("\n");
                        continue;
                    }
                    if (line.contains("----------------------WeChat Payment Statement Details List--------------------")) {
                        isReminderSection = false;
                        content.append(line).append("\n");
                        continue;
                    }

                    if (isReminderSection && !foundInsertionPoint) {
                        content.append(dataLine).append("\n");
                        foundInsertionPoint = true;
                    }
                    content.append(line).append("\n");
                }
            }

            // 写入更新后的内容
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 显示错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Save failed");
            alert.setContentText("The reminder data cannot be saved：" + e.getMessage());
            alert.showAndWait();
        }
    }

    private HBox createBottomNav(Stage primaryStage) {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // 创建底部导航按钮
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