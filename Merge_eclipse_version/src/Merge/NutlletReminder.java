package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

    public List<Reminder> reminders = new ArrayList<>();
    private double totalIncome = 0;
    private double totalExpense = 0;

    @Override
    public void start(Stage primaryStage) {
        loadData();
        calculateBalance();
        BorderPane root = new BorderPane();

        root.setTop(createHeader());
        
        // 创建ScrollPane包装主内容
        ScrollPane scrollPane = new ScrollPane(createMainContent(primaryStage));
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);
        
        root.setBottom(createBottomNav(primaryStage));

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Nutllet - Reminders");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("deals.csv"))) {
            String line;
            boolean isReminderSection = false;
            boolean isTransactionSection = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("Type,Reminder name,Reminder Category,Amount Range,Note")) {
                    isReminderSection = true;
                    continue;
                }
                if (line.contains("----------------------WeChat Payment Statement Details List--------------------")) {
                    isReminderSection = false;
                    isTransactionSection = true;
                    continue;
                }
                if (line.contains("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note")) {
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
                    	String transactionType = parts[1].replace("\"", "");
                        if ("Merchant Consumption".equals(transactionType) || "Pocket Money".equals(transactionType)) {
                        	String type = parts[4].replace("\"", "");
                            String amount = parts[5].replace("\"", "").replace("¥", "");
                            double value = Double.parseDouble(amount);
                            if (type.equals("Income")) {
                                totalIncome += value;
                            } else if (type.equals("Expenditure")) {
                                totalExpense += value;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calculateBalance() {
        double originalTotalIncome = totalIncome; // 保存原始总收入
        double originalTotalExpense = totalExpense; // 保存原始总支出
        double currentIncome = originalTotalIncome; // 动态扣除后的当前收入

        // 先处理储蓄类型提醒
        for (Reminder reminder : reminders) {
            if (reminder.type.equals("For savings")) {
                double avgAmount = (reminder.minAmount + reminder.maxAmount) / 2;
                // 使用动态更新的 currentIncome 计算余额
                double balance = currentIncome - originalTotalExpense;
                double progress = balance >= 0 ? Math.min(100, (balance / avgAmount) * 100) : 0;

                // 如果进度完成，扣除 maxAmount
                if (progress >= 100) {
                    currentIncome -= reminder.maxAmount; // 更新当前可用收入
                    progress = 100; // 强制显示为100%
                }
                else {
                	currentIncome = 0;
                }

                reminder.progress = progress;
                reminder.progressText = String.format("Savings progress：%.1f%%", progress);
            }
        }

        // 更新最终扣除后的总收入
        totalIncome = currentIncome;

        // 处理支出类型提醒（基于原始总支出，不受储蓄扣除影响）
        for (Reminder reminder : reminders) {
            if (reminder.type.equals("For expense monitoring")) {
                double avgAmount = (reminder.minAmount + reminder.maxAmount) / 2;
                reminder.progress = Math.min(100, (originalTotalExpense / avgAmount) * 100);
                reminder.progressText = String.format("Monthly expense has reached %.1f%% of the target", reminder.progress);
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
        mainContent.setMinWidth(400); // 设置最小宽度，确保内容不会被压缩

        for (Reminder reminder : reminders) {
            Button reminderButton = createReminderButton(reminder, primaryStage);
            mainContent.getChildren().add(reminderButton);
        }
        Button addReminderButton = new Button("Add New Reminder");

        stylePrimaryButton(addReminderButton);

        addReminderButton.setOnAction(e -> {
            try {
                new NutlletAddNewReminder().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainContent.getChildren().add(addReminderButton);
        return mainContent;
    }

    private Button createReminderButton(Reminder reminder, Stage primaryStage) {
        HBox mainContainer = new HBox(10);
        mainContainer.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        content.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        Label title = new Label(reminder.name);
        //title.setStyle("-fx-background-color: red;");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: black;");

        // 添加金额范围
        Label amountRange = new Label(String.format("Quota：¥%.0f-%.0f", reminder.minAmount, reminder.maxAmount));
        amountRange.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        amountRange.setStyle("-fx-text-fill: black;");
        
        // 添加备注
        Label remark = new Label("Remark：" + reminder.remark);
        remark.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        remark.setStyle("-fx-text-fill: black;");
        
        HBox progressBarContainer = new HBox();
        progressBarContainer.setBackground(new Background(new BackgroundFill(
                Color.LIGHTGRAY, new CornerRadii(6), Insets.EMPTY)));
        progressBarContainer.setPrefHeight(8);
        progressBarContainer.setMinHeight(8);
        progressBarContainer.setMaxHeight(8);
        progressBarContainer.setPrefWidth(400);

        Region progressBar = new Region();
        progressBar.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, new CornerRadii(6), Insets.EMPTY)));
        progressBar.setPrefHeight(8);
        progressBar.setMinHeight(8);
        progressBar.setMaxHeight(8);
        progressBar.setPrefWidth(reminder.progress * 4); // 400 * 百分比
        progressBarContainer.getChildren().add(progressBar);

        Label description = new Label(reminder.progressText);
        description.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        description.setStyle("-fx-text-fill: black;");
        content.getChildren().addAll(title, amountRange, remark, progressBarContainer, description);

        // 创建删除按钮
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-font-size: 24px; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #ffebeb; -fx-text-fill: #ff4d4d; -fx-font-size: 24px; -fx-font-weight: bold; -fx-cursor: hand;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-font-size: 24px; -fx-font-weight: bold; -fx-cursor: hand;"));

        // 设置删除按钮事件
        deleteButton.setOnAction(e -> {
            try {
                deleteReminder(reminder.name);
                // 重新加载页面
                new NutlletReminder().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainContainer.getChildren().addAll(content, deleteButton);
        return new Button("", mainContainer);
    }

    private void deleteReminder(String reminderName) {
        try {
            File file = new File("deals.csv");
            StringBuilder content = new StringBuilder();
            boolean isReminderSection = false;
            boolean foundReminder = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Type,Reminder name,Reminder Category,Amount Range,Note")) {
                        isReminderSection = true;
                        content.append(line).append("\n");
                        continue;
                    }
                    if (line.contains("WeChat Payment Statement Details List")) {
                        isReminderSection = false;
                        content.append(line).append("\n");
                        continue;
                    }

                    if (isReminderSection) {
                        // 检查是否是要删除的提醒事项
                        if (line.contains("\"" + reminderName + "\"")) {
                            foundReminder = true;
                            continue; // 跳过这一行，不添加到新内容中
                        }
                    }
                    content.append(line).append("\n");
                }
            }

            if (foundReminder) {
                // 写入更新后的内容
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(content.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Delete failed");
            alert.setContentText("Failed to delete reminder: " + e.getMessage());
            alert.showAndWait();
        }
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