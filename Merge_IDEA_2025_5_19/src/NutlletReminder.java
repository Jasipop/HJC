//package Merge;

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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NutlletReminder extends Application {
    private static final Color PRIMARY_COLOR = Color.rgb(202, 182, 244);
    private static final Color BACKGROUND_COLOR = Color.rgb(255, 212, 236, 0.33);
    private static final Color TEXT_COLOR = Color.BLACK;

    private List<Reminder> reminders = new ArrayList<>();
    private double totalIncome = 0;
    private double totalExpense = 0;

    @Override
    public void start(Stage primaryStage) {
        loadData();
        calculateBalance();
        BorderPane root = new BorderPane();

        root.setTop(createHeader());
        root.setCenter(createMainContent(primaryStage));
        root.setBottom(createBottomNav(primaryStage));

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Nutllet - Reminders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean isReminderSection = false;
            boolean isTransactionSection = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("类型,提醒名字,提醒类别,金额范围,备注")) {
                    isReminderSection = true;
                    continue;
                }
                if (line.contains("----------------------微信支付账单明细列表--------------------")) {
                    isReminderSection = false;
                    isTransactionSection = true;
                    continue;
                }
                if (line.contains("交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注")) {
                    continue;
                }

                if (isReminderSection && !line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        Reminder reminder = new Reminder();
                        reminder.name = parts[1].replace("\"", "");
                        reminder.type = parts[2].replace("\"", "");
                        String amountRange = parts[3].replace("\"", "").replace("¥", "");
                        String[] amounts = amountRange.split("-");
                        reminder.minAmount = Double.parseDouble(amounts[0]);
                        reminder.maxAmount = Double.parseDouble(amounts[1]);
                        reminder.remark = parts[4].replace("\"", "");
                        reminders.add(reminder);
                    }
                }

                if (isTransactionSection && !line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        String type = parts[4].replace("\"", "");
                        String amount = parts[5].replace("\"", "").replace("¥", "");
                        double value = Double.parseDouble(amount);
                        if (type.equals("收入")) {
                            totalIncome += value;
                        } else if (type.equals("支出")) {
                            totalExpense += value;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateBalance() {
        double balance = totalIncome - totalExpense;
        for (Reminder reminder : reminders) {
            double avgAmount = (reminder.minAmount + reminder.maxAmount) / 2;
            if (reminder.type.equals("For expense monitoring")) {
                if (balance < 0) {
                    reminder.progress = 100;
                    reminder.progressText = "This month's quota has been used up to 100%";
                } else {
                    reminder.progress = Math.min(100, (balance / avgAmount) * 100);
                    reminder.progressText = String.format("This month's quota has been used up to %.1f%%", reminder.progress);
                }
            } else if (reminder.type.equals("For savings")) {
                if (balance < 0) {
                    reminder.progress = 0;
                    reminder.progressText = "Monthly progress 0%";
                } else {
                    reminder.progress = Math.min(100, (balance / avgAmount) * 100);
                    reminder.progressText = String.format("Monthly progress %.1f%%", reminder.progress);
                }
            }
        }
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

    private VBox createMainContent(Stage primaryStage) {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        mainContent.setAlignment(Pos.TOP_CENTER);

        for (Reminder reminder : reminders) {
            Button reminderButton = createReminderButton(reminder, primaryStage);
            mainContent.getChildren().add(reminderButton);
        }
        Button addReminderButton = new Button("Add New Reminder");

        stylePrimaryButton(addReminderButton);

        addReminderButton.setOnAction(e -> {
            try {
                new NutlletAddNewReminder().start(new Stage());
                primaryStage.close(); // 关闭当前页面
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainContent.getChildren().add(addReminderButton);
        return mainContent;
    }

    private Button createReminderButton(Reminder reminder, Stage primaryStage) {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        content.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        Label title = new Label(reminder.name);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(TEXT_COLOR);

        HBox progressBarContainer = new HBox();
        progressBarContainer.setBackground(new Background(new BackgroundFill(
                Color.LIGHTGRAY, new CornerRadii(6), Insets.EMPTY)));
        progressBarContainer.setPrefHeight(12);
        progressBarContainer.setPrefWidth(400);

        Region progressBar = new Region();
        progressBar.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, new CornerRadii(6), Insets.EMPTY)));
        progressBar.setPrefHeight(12);
        progressBar.setPrefWidth(reminder.progress * 4); // 400 * 百分比
        progressBarContainer.getChildren().add(progressBar);

        Label description = new Label(reminder.progressText);
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        description.setTextFill(TEXT_COLOR);
        content.getChildren().addAll(title, progressBarContainer, description);

        Button button = new Button();
        button.setGraphic(content);
        button.setBackground(Background.EMPTY);
        button.setPrefWidth(400);
        button.setStyle("-fx-cursor: pointer;");

        button.setOnAction(e -> {
            try {
                NutlletAddNewReminder addNewReminder = new NutlletAddNewReminder();
                addNewReminder.setReminderData(reminder);
                addNewReminder.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
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