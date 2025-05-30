package Merge;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReimbursementList extends Application {

    private final List<HBox> allItems = new ArrayList<>();
    private VBox itemsContainer;
    private List<String[]> csvData = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        BorderPane rootplus = new BorderPane();

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

        Text title = new Text("Reimbursements Items");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(750);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 4; -fx-padding: 10 15; -fx-min-height: 40px; -fx-font-size: 16px;");
        searchField.setAlignment(Pos.CENTER);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #7f8c8d;");

        HBox searchBox = new HBox(10, searchIcon, searchField);
        searchBox.setPrefWidth(800);
        searchBox.setAlignment(Pos.CENTER);

        itemsContainer = new VBox();
        itemsContainer.setSpacing(10);
        itemsContainer.setPadding(new Insets(10));
        itemsContainer.setAlignment(Pos.CENTER);

        try (BufferedReader reader = new BufferedReader(new FileReader("reimbursements.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    csvData.add(parts); // Store the CSV data
                    String category = parts[0];
                    String detail = parts[4];
                    String amount = parts[3];
                    String date = parts[2];
                    HBox item = createItem(category, detail, amount, date, csvData.size() - 1);
                    allItems.add(item);
                    itemsContainer.getChildren().add(item);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(600);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String keyword = newValue.toLowerCase().trim();
            itemsContainer.getChildren().clear();
            for (HBox item : allItems) {
                boolean matched = false;
                for (javafx.scene.Node node : item.getChildren()) {
                    if (node instanceof Text t && t.getText().toLowerCase().contains(keyword)) {
                        matched = true;
                        break;
                    }
                    if (node instanceof VBox || node instanceof HBox) {
                        matched = matched || searchTextNodesRecursively(node, keyword);
                    }
                }
                if (matched) {
                    itemsContainer.getChildren().add(item);
                }
            }
        });

        mainLayout.getChildren().addAll(titleBox, searchBox, scrollPane);

        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-color: " + Color.web("#855FAF").toString().replace("0x", "#") +
                "; -fx-text-fill: white; -fx-font-size: 27px; -fx-font-weight: bold;");
        addButton.setShape(new Circle(30));
        addButton.setMinSize(60, 60);
        addButton.setMaxSize(60, 60);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.3), addButton);
        addButton.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);
            scaleTransition.play();
        });
        addButton.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });

        StackPane.setAlignment(addButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 20, 20, 0));

        addButton.setOnAction(e -> {
            try {
                new ReimbursementAddNew().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(mainLayout, addButton);
        rootplus.setCenter(root);

        // Bottom Navigation Bar
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠");
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙");

        homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);
        rootplus.setBottom(navBar);

        Scene scene = new Scene(rootplus, 1366, 768);
        primaryStage.setTitle("Reimbursements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void deleteItem(int index) {
        // 创建确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Sure you want to delete this reimbursement?");
        alert.setContentText("Deletion will not be recovered");

        // 自定义对话框按钮（中文）
        ButtonType buttonTypeYes = new ButtonType("Confirm");
        ButtonType buttonTypeNo = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // 显示对话框并等待用户选择
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                // 从内存数据中移除
                csvData.remove(index);

                // 更新CSV文件
                try (FileWriter writer = new FileWriter("reimbursements.csv")) {
                    for (String[] parts : csvData) {
                        writer.write(String.join(",", parts) + "\n");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // 刷新界面
                refreshUI();

                // 添加删除成功提示
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Successful Operation");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Claims Records Deleted");
                successAlert.showAndWait();
            }
        });
    }

    private void refreshUI() {
        allItems.clear();
        itemsContainer.getChildren().clear();

        // 重新从CSV文件加载数据
        try (BufferedReader reader = new BufferedReader(new FileReader("reimbursements.csv"))) {
            String line;
            csvData.clear();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    csvData.add(parts);
                    HBox item = createItem(parts[0], parts[4], parts[3], parts[2], csvData.size() - 1);
                    allItems.add(item);
                    itemsContainer.getChildren().add(item);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    // Helper method with emoji
    private Button createNavButtonWithEmoji(String label, String emoji) {
        VBox btnContainer = new VBox();
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(2);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        btnContainer.getChildren().addAll(emojiLabel, textLabel);

        Button button = new Button();
        button.setPrefWidth(456);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        return button;
    }
    private Button createNavButton(String label) {
        Button button = new Button(label);
        button.setPrefWidth(456); // 1366 / 3
        button.setPrefHeight(60);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: #7f8c8d;" +
                        "-fx-border-color: transparent;"
        );
        return button;
    }

    private HBox createItem(String category, String detail, String amount, String date, int index) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(15);
        itemBox.setPadding(new Insets(15));
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
        itemBox.setMaxWidth(800);

        Text amountLabel = new Text("-" + amount);
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        amountLabel.setFill(Color.RED);

        HBox categoryDetailBox = new HBox();
        categoryDetailBox.setSpacing(10);
        categoryDetailBox.setAlignment(Pos.CENTER_LEFT);

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(5);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Text categoryLabel = new Text(category);
        categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        categoryLabel.setFill(Color.web("#2c3e50"));

        Text detailLabel = new Text(detail);
        detailLabel.setFont(Font.font("Arial", 16));
        detailLabel.setFill(Color.web("#2c3e50"));

        categoryDetailBox.getChildren().addAll(categoryLabel, detailLabel);

        Text dateLabel = new Text(date);
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setFill(Color.web("#7f8c8d"));

        detailsBox.getChildren().addAll(categoryDetailBox, dateLabel);

        ToggleButton starButton = createStarToggleButton();

        // Create delete button
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 20px; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> deleteItem(index));

        HBox buttonsBox = new HBox(10, starButton, deleteButton);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        itemBox.getChildren().addAll(amountLabel, detailsBox, buttonsBox);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        return itemBox;
    }
    private ToggleButton createStarToggleButton() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setStyle("-fx-background-color: transparent; -fx-padding: 5;");

        SVGPath star = new SVGPath();
        star.setContent("M12,17.27L18.18,21l-1.64-7.03L22,9.24l-7.19-0.61L12,2L9.19,8.63L2,9.24l5.46,4.73L5.82,21L12,17.27z");

        star.setFill(Color.web("#bdc3c7"));
        star.setStroke(Color.web("#bdc3c7"));

        toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                star.setFill(Color.web("#FFD700"));
                star.setStroke(Color.web("#FFD700"));
            } else {
                star.setFill(Color.web("#bdc3c7"));
                star.setStroke(Color.web("#bdc3c7"));
            }
        });

        toggleButton.setGraphic(star);
        return toggleButton;
    }

    private boolean searchTextNodesRecursively(javafx.scene.Node node, String keyword) {
        if (node instanceof Text t && t.getText().toLowerCase().contains(keyword)) {
            return true;
        } else if (node instanceof Pane pane) {
            for (javafx.scene.Node child : pane.getChildren()) {
                if (searchTextNodesRecursively(child, keyword)) return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        launch(args);
    }
}