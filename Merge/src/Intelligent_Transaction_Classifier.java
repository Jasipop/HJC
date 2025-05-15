//package myapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Intelligent_Transaction_Classifier extends Application {

    public static class Transaction {
        private final String date;
        private final String description;
        private String amount;
        private String category;

        public Transaction(String date, String description, String amount, String category) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.category = category;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    private PieChart pieChart;
    private ObservableList<Transaction> data;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private double totalAmount;
    private HostServices hostServices;

    @Override
    public void start(Stage primaryStage) {
        this.hostServices = getHostServices();
        initializeData();
        TableView<Transaction> table = createTableView();
        VBox tableCard = createTableCard(table);
        setupPieChart();
        updatePieChart();
        VBox pieCard = createPieCard();
        VBox rightPanel = createRightPanel(pieCard);
        HBox content = createMainContent(tableCard, rightPanel);
        ScrollPane scrollPane = createScrollPane(content);
        VBox mainLayout = createMainLayout(scrollPane);

        Scene scene = new Scene(mainLayout,1366, 768);
        primaryStage.setTitle("AI Transaction Classifier");
        primaryStage.setScene(scene);

        setupSizeBindings(tableCard, rightPanel, mainLayout);
        primaryStage.show();
        // Bottom Navigation Bar
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠"); // 🏠
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍"); // 🔍
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙"); // ⚙

        homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(settingsBtn, discoverBtn, homeBtn); // 从右到左
        mainLayout.getChildren().add(navBar);
    }

    private void initializeData() {
        data = FXCollections.observableArrayList();
        totalAmount = 0.0;

        try {
//            Path path = Paths.get("src/deals.csv");
            Path path = Paths.get("deals.csv");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)
            );


            String line;

            while ((line = reader.readLine()) != null && !line.startsWith("交易时间")) {}

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (fields.length < 6) continue;

                String rawDate = fields[0].replace("\"", "").trim();
                String rawGoods = fields[3].replace("\"", "").trim(); // 商品字段
                String rawCounterparty = fields[2].replace("\"", "").trim(); // 交易对方字段

                // 判断是否应替换为交易对方
                boolean isFallbackToCounterparty = rawGoods.equals("/") || rawGoods.matches("^\\d+$");

                String rawDescription = isFallbackToCounterparty ? rawCounterparty : rawGoods;

                String rawAmount = fields[5].replace("\"", "").replace("¥", "").trim();
                if (rawAmount.isEmpty()) continue;

                String category;
                if (rawGoods.equals("/")) {
                    category = "Utilities & Transfer";
                } else {
                    category = autoClassify(rawDescription);
                }

                data.add(new Transaction(rawDate, rawDescription, rawAmount, category));
                totalAmount += Double.parseDouble(rawAmount);
            }


            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            // fallback to default static data if CSV fails
            System.out.println("Failed to read CSV, using default data");
            data.add(new Transaction("2025-04-01", "Fallback Coffee", "35", "Food & Beverage"));
            totalAmount = 35;
        }
    }


    private String autoClassify(String description) {
        description = description.toLowerCase();

        if (description.matches(".*(滴滴|出行|一卡通|交通卡|先乘后付).*")) return "Transportation";
        if (description.matches(".*(美团|饿了么|肉夹馍|米粉|饭|餐|海鲜|先购后付|汤|砂锅|鸡架|买菜|茶|手工粉|果汁|餐费|大众点评).*")) return "Food & Beverage";
        if (description.matches(".*(apple|spotify|爱奇艺|优酷|bilibili|定制游|签证).*")) return "Entertainment";
        if (description.matches(".*(gym|健身|运动).*")) return "Fitness";
        if (description.matches(".*(coursera|课程|学习|教育|培训).*")) return "Education";
        if (description.matches(".*(京东|淘宝|购物|uniqlo|h&m|amazon).*")) return "Shopping";
        if (description.matches(".*(水费|电费|燃气|账单|云服务|WPS|会员|汽油|apple).*")) return "Utilities & Transfer";

        return "Uncategorized"; // 默认
    }


    private TableView<Transaction> createTableView() {
        TableView<Transaction> table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 14px; -fx-table-cell-size: 40px;");
        table.setPrefHeight(360);

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        descCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAmount()));
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        categoryCol.setStyle("-fx-alignment: CENTER;");
        categoryCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(
                    "Food & Beverage", "Shopping", "Transportation",
                    "Entertainment", "Education", "Fitness", "Utilities & Transfer"));

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    comboBox.setOnAction(e -> {
                        Transaction transaction = getTableView().getItems().get(getIndex());
                        String newCategory = comboBox.getValue();
                        if (!newCategory.equals(transaction.getCategory())) {
                            transaction.setCategory(newCategory);
                            updatePieChart(); // ✅ 仅在分类更改时更新饼图
                        }
                    });
                    setGraphic(comboBox);
                }
            }
        });


        table.getColumns().addAll(dateCol, descCol, amountCol, categoryCol);
        return table;
    }

    private VBox createTableCard(TableView<Transaction> table) {
        VBox tableCard = new VBox();
        Label tableTitle = new Label("Classification Details");
        tableTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web("#6c757d"));

        Label totalAmountLabel = new Label("Total Amount: ¥" + df.format(totalAmount));
        totalAmountLabel.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 14));
        totalAmountLabel.setTextFill(Color.web("#6c757d"));

        Label instruction = new Label("For the results of the system's automatic classification, you can manually edit them in the category column of the table!");
        instruction.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 12));
        instruction.setTextFill(Color.web("#a0a0a0"));

        VBox summaryBox = new VBox(5, totalAmountLabel, instruction);
        summaryBox.setPadding(new Insets(10, 0, 0, 0));

        ScrollPane tableScrollPane = new ScrollPane(table);
        tableScrollPane.setFitToWidth(true);
        tableScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tableScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tableScrollPane.setPrefHeight(370);
        tableScrollPane.setStyle("-fx-background-color: transparent;");

        tableCard.getChildren().addAll(tableTitle, tableScrollPane, summaryBox);
        tableCard.setPadding(new Insets(15));
        tableCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);" +
                        "-fx-max-width: 780;" +
                        "-fx-pref-width: 780;"
        );
        return tableCard;
    }

    private void setupPieChart() {
        pieChart = new PieChart();
        pieChart.setStyle("-fx-title-fill: #4a148c; -fx-font-weight: bold;");
        pieChart.setTitle("Spending Breakdown");
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setLabelsVisible(true);
        pieChart.setPrefSize(400, 300);
    }

    private void setupSliceEffects(PieChart.Data slice) {
        Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> slice.getNode().setOpacity(1)),
                new KeyFrame(Duration.millis(200), e -> slice.getNode().setOpacity(0.6)),
                new KeyFrame(Duration.millis(400), e -> slice.getNode().setOpacity(1))
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        //调用 blinkTimeline.play() 开始闪烁。

        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-font-size: 12px; -fx-font-family: 'Microsoft YaHei';");

        slice.getNode().setOnMouseEntered(e -> {
            blinkTimeline.play();
            slice.getNode().setScaleX(1.1);
            slice.getNode().setScaleY(1.1);

            double categoryAmount = slice.getPieValue();
            double percentage = (categoryAmount / totalAmount) * 100;
            tooltip.setText(String.format(
                    "%s\nAmount: ¥%s\nRatio: %s%%",
                    slice.getName(),
                    df.format(categoryAmount),
                    df.format(percentage)
            ));
            Tooltip.install(slice.getNode(), tooltip);
        });

        slice.getNode().setOnMouseExited(e -> {
            blinkTimeline.stop();// 停止闪烁动画
            slice.getNode().setOpacity(1);
            slice.getNode().setScaleX(1);
            slice.getNode().setScaleY(1);
            Tooltip.uninstall(slice.getNode(), tooltip);
        });
    }

    private VBox createPieCard() {
        VBox pieCard = new VBox(pieChart);
        pieCard.setPadding(new Insets(15));
        pieCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);"
        );
        return pieCard;
    }

    private VBox createRightPanel(VBox pieCard) {
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        Label suggestionTitle = new Label("🔍 AI Insights");
        suggestionTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        suggestionTitle.setTextFill(Color.web("#6c757d"));

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #cab6f4; -fx-text-fill: white; -fx-font-weight: bold;");
        updateBtn.setOnAction(e -> {
            StringBuilder params = new StringBuilder();
            for (Transaction t : data) {
                params.append(String.format("date=%s&desc=%s&amount=%s&category=%s&",
                        t.getDate(), t.getDescription(), t.getAmount(), t.getCategory()));
            }
            hostServices.showDocument("https://www.deepseek.com/analysis?" + params.toString());
        });

        titleBox.getChildren().addAll(suggestionTitle, updateBtn);

        Label insight1 = new Label("• Food & Beverage expenses account for ~34% of your total spending.");
        Label insight2 = new Label("• Shopping is the largest expense. Consider reviewing your purchases.");
        Label insight3 = new Label("• You may set weekly limits or enable alerts to manage spending better.");

        for (Label label : new Label[]{insight1, insight2, insight3}) {
            label.setFont(Font.font("Microsoft YaHei", 14));
            label.setTextFill(Color.web("#63b006"));
            label.setWrapText(true);
            label.setMaxWidth(400);
            label.setPadding(new Insets(2, 0, 2, 0));
        }

        VBox suggestionContent = new VBox(8, titleBox, insight1, insight2, insight3);
        suggestionContent.setPadding(new Insets(15));
        suggestionContent.setStyle(pieCard.getStyle());
        suggestionContent.setMinHeight(180);

        VBox rightPanel = new VBox(15, pieCard, suggestionContent);
        rightPanel.setMinWidth(450);
        rightPanel.setPrefWidth(450);
        return rightPanel;
    }

    private HBox createMainContent(VBox tableCard, VBox rightPanel) {
        HBox content = new HBox(20, tableCard, rightPanel);
        content.setPadding(new Insets(20));
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        return content;
    }

    private ScrollPane createScrollPane(HBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: white;");
        return scrollPane;
    }

    private VBox createMainLayout(ScrollPane scrollPane) {
        Label pageTitle = new Label("Intelligent Transaction Classification");
        pageTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 32));
        pageTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("AI-powered transaction analysis and classification");
        subtitle.setFont(Font.font("Microsoft YaHei", 16));
        subtitle.setTextFill(Color.WHITE);

        VBox titleBox = new VBox(5, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: #e6d5ff;");
        titleBox.setPadding(new Insets(25, 0, 25, 0));

        VBox contentWrapper = new VBox(scrollPane);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(20, 0, 20, 0));
        contentWrapper.setStyle("-fx-background-color: white;");

        VBox mainLayout = new VBox(0);
        mainLayout.getChildren().addAll(titleBox, contentWrapper);
        mainLayout.setStyle("-fx-background-color: white;");
        mainLayout.setAlignment(Pos.TOP_CENTER);


        return mainLayout;
    }

    private void setupSizeBindings(VBox tableCard, VBox rightPanel, VBox mainLayout) {
        tableCard.prefWidthProperty().bind(mainLayout.widthProperty().multiply(0.6).subtract(40));
        rightPanel.prefWidthProperty().bind(mainLayout.widthProperty().multiply(0.4).subtract(40));
    }

    private double calculateCategoryAmount(String category) {
        return data.stream()
                .filter(t -> t.getCategory().equals(category))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();
    }

    private void updatePieChartData() {
        pieChart.getData().setAll(
                createPieData("Food & Beverage"),
                createPieData("Shopping"),
                createPieData("Transportation"),
                createPieData("Entertainment"),
                createPieData("Education"),
                createPieData("Fitness")
        );
    }

    private PieChart.Data createPieData(String category) {
        double value = calculateCategoryAmount(category);
        return new PieChart.Data(category, value);
    }

    private void updatePieChart() {
        pieChart.getData().clear();

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : data) {
            String category = t.getCategory();
            double amount = Double.parseDouble(t.getAmount());
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);
            setupSliceEffects(slice);
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

    public static void main(String[] args) {
        launch(args);
    }
}