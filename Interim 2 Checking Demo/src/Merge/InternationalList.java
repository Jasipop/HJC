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

public class InternationalList extends Application {

    private final List<HBox> allItems = new ArrayList<>();
    private VBox itemsContainer;
    private List<String[]> csvData = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane rootplus = new BorderPane();
        StackPane root = new StackPane();

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

        Text title = new Text("International Transaction Items");
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

        itemsContainer = new VBox();  // 去掉前面的VBox类型声明
        itemsContainer.setSpacing(10);
        itemsContainer.setPadding(new Insets(10));
        itemsContainer.setAlignment(Pos.CENTER);

        try (BufferedReader reader = new BufferedReader(new FileReader("international.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    csvData.add(parts); // 存储CSV数据
                    String foreignCurrency = parts[0];
                    Double foreignAmount = Double.valueOf(parts[1]);
                    Double localAmount = Double.valueOf(parts[2]);
                    String date = parts[3];
                    HBox item = createItem(foreignCurrency, foreignAmount, localAmount, date, csvData.size() - 1);
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
                new International().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(mainLayout, addButton);
        rootplus.setCenter(mainLayout); // 不再是 root，直接是 mainLayout

        // Bottom Navigation Bar
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠"); // 🏠
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍"); // 🔍
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙"); // ⚙️

        homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn); // 从右到左
        rootplus.setBottom(navBar);
        // 用一个 StackPane 包住 rootplus 和 addButton，使按钮浮动
        StackPane outerStack = new StackPane();

// 把 rootplus 放进去（它含 mainLayout 和底部导航）
        outerStack.getChildren().addAll(rootplus, addButton);

// 设置按钮对齐在右下角，并适当边距
        StackPane.setAlignment(addButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 50, 100, 0)); // 距离右边30px，底部30px

        Scene scene = new Scene(outerStack, 1366, 768);

        primaryStage.setTitle("Reimbursements");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private void deleteItem(int index) {
        // 创建确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Sure you want to delete this transaction?");
        alert.setContentText("Deletion will not be recovered");

        ButtonType buttonTypeYes = new ButtonType("Confirm");
        ButtonType buttonTypeNo = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                // 3. 先更新CSV文件再刷新界面
                csvData.remove(index);

                try (FileWriter writer = new FileWriter("international.csv")) {
                    for (String[] parts : csvData) {
                        writer.write(String.join(",", parts) + "\n");
                    }

                    // 4. 直接更新界面而不重新读取文件
                    allItems.remove(index);
                    itemsContainer.getChildren().remove(index);

                    // 5. 重新设置所有条目的索引
                    for (int i = 0; i < allItems.size(); i++) {
                        HBox item = allItems.get(i);
                        // 找到删除按钮并更新其事件处理
                        for (javafx.scene.Node node : item.getChildren()) {
                            if (node instanceof HBox) {
                                for (javafx.scene.Node btnNode : ((HBox) node).getChildren()) {
                                    if (btnNode instanceof Button && ((Button) btnNode).getText().equals("×")) {
                                        Button deleteBtn = (Button) btnNode;
                                        final int newIndex = i;
                                        deleteBtn.setOnAction(e -> deleteItem(newIndex));
                                    }
                                }
                            }
                        }
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Successful Operation");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Transaction deleted successfully");
                    successAlert.showAndWait();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private HBox createItem(String foreignCurrency, Double foreignAmount, Double localAmount, String date, int index) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(15);
        itemBox.setPadding(new Insets(15));
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
        itemBox.setMaxWidth(800);

        Text localAmountLabel = new Text("-" + localAmount);
        localAmountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        localAmountLabel.setFill(Color.RED);

        HBox foreignCurrencyBox = new HBox();
        foreignCurrencyBox.setSpacing(10);
        foreignCurrencyBox.setAlignment(Pos.CENTER_LEFT);

        VBox foreignAmountBox = new VBox();
        foreignAmountBox.setSpacing(5);
        foreignAmountBox.setAlignment(Pos.CENTER_LEFT);

        Text currencyLabel = new Text(foreignCurrency);
        currencyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        currencyLabel.setFill(Color.web("#2c3e50"));

        Text amountLabel = new Text(String.valueOf(foreignAmount));
        amountLabel.setFont(Font.font("Arial", 16));
        amountLabel.setFill(Color.web("#2c3e50"));

        foreignCurrencyBox.getChildren().addAll(currencyLabel, amountLabel);

        Text dateLabel = new Text(date);
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setFill(Color.web("#7f8c8d"));

        foreignAmountBox.getChildren().addAll(foreignCurrencyBox, dateLabel);

        ToggleButton starButton = createStarToggleButton();

        // 添加删除按钮
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 20px; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> deleteItem(index));

        HBox buttonsBox = new HBox(10, starButton, deleteButton);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        itemBox.getChildren().addAll(localAmountLabel, foreignAmountBox, buttonsBox);
        HBox.setHgrow(foreignAmountBox, Priority.ALWAYS);

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