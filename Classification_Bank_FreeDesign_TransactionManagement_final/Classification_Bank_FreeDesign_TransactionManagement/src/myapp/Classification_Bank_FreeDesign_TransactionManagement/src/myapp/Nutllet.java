package myapp;// package Nutllet;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Nutllet extends Application {
    private static final Color PRIMARY_PURPLE = Color.rgb(128, 100, 228);
    private static final Color LIGHT_PURPLE_BG = Color.rgb(245, 241, 255);
    private static final Color DARK_NAV_BG = Color.rgb(40, 40, 40);
    private static final Color NAV_HOVER = Color.rgb(70, 70, 70);
    private static final Color NAV_SELECTED = Color.rgb(90, 90, 90);

    private Map<String, Double> categoryTotals = new HashMap<>();
    private ObservableList<String> transactionItems = FXCollections.observableArrayList();
    private double totalExpenditure;

    @Override
    public void start(Stage primaryStage) {
        List<Expense> expenses = loadExpensesFromCSV("D:\\微信支付账单(20250413-20250414)——【解压密码可在微信支付公众号查看】\\微信支付账单(20250413-20250414)——【解压密码可在微信支付公众号查看】.csv");
        processData(expenses);

        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());

        // 修改底部布局结构
        StackPane bottomContainer = new StackPane();
        bottomContainer.getChildren().addAll(
                createBottomNavigation(),
                createFloatingButtons()
        );
        root.setBottom(bottomContainer);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("NUTLLET - Financial Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private List<Expense> loadExpensesFromCSV(String filePath) {
        List<Expense> expenses = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isDataSection = false;
            List<String> headers = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.contains("微信支付账单明细列表")) {
                    isDataSection = true;
                    headers = Arrays.asList(br.readLine().split(","));
                    continue;
                }

                if (isDataSection && !line.trim().isEmpty()) {
                    Map<String, String> record = parseCSVLine(line, headers);
                    if ("支出".equals(record.get("收/支")) && "支付成功".equals(record.get("当前状态"))) {
                        Expense expense = new Expense(
                                LocalDateTime.parse(record.get("交易时间"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                Double.parseDouble(record.get("金额(元)").replace("¥", "").trim()),
                                record.get("交易对方"),
                                record.get("商品")
                        );
                        expenses.add(expense);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    private Map<String, String> parseCSVLine(String line, List<String> headers) {
        String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        Map<String, String> record = new HashMap<>();
        for (int i = 0; i < headers.size() && i < values.length; i++) {
            String value = values[i].replaceAll("^\"|\"$", "").trim();
            record.put(headers.get(i), value);
        }
        return record;
    }

    private void processData(List<Expense> expenses) {
        // 分类统计
        categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        this::categorizeExpense,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        // 计算总支出
        totalExpenditure = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // 格式化交易记录
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy");
        transactionItems.setAll(expenses.stream()
                .sorted(Comparator.comparing(Expense::getTransactionTime).reversed())
                .map(e -> String.format("%s • %s - ¥%.2f - %s",
                        e.getTransactionTime().format(timeFormatter),
                        categorizeExpense(e),
                        e.getAmount(),
                        e.getTransactionTime().format(dateFormatter)))
                .collect(Collectors.toList()));
    }

    private String categorizeExpense(Expense expense) {
        String counterpart = expense.getCounterpart().toLowerCase();
        String product = expense.getProduct().toLowerCase();

        if (counterpart.contains("美团") || counterpart.contains("食堂") || product.contains("餐")) {
            return "Catering";
        } else if (counterpart.contains("滴滴") || counterpart.contains("加油站")) {
            return "Traffic";
        } else if (counterpart.contains("电影院") || product.contains("游戏")) {
            return "Entertainment";
        } else if (counterpart.contains("超市") || product.contains("日用品")) {
            return "Living";
        } else {
            return "Other";
        }
    }

    private HBox createHeader() {
        HBox leftPanel = new HBox(10);
        Label title = new Label("NUTLLET");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label edition = new Label("Personal Edition");
        edition.setFont(Font.font("Segoe UI", 12));
        edition.setTextFill(Color.rgb(255, 255, 255, 0.6));

        leftPanel.getChildren().addAll(title, edition);
        leftPanel.setAlignment(Pos.CENTER_LEFT);

        HBox rightPanel = new HBox(15);
        String[] buttons = {"Syncing", "Enterprise Edition", "Logout"};
        Arrays.stream(buttons).forEach(btnText -> {
            Button btn = new Button(btnText);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                    (btnText.equals("Enterprise Edition") ? PRIMARY_PURPLE.toString().replace("0x", "#") : "white") + ";");
            if(btnText.equals("Enterprise Edition")) {
                btn.setStyle("-fx-text-fill: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                        "; -fx-background-color: white; -fx-border-radius: 3;");
            }
            rightPanel.getChildren().add(btn);
        });
        rightPanel.setAlignment(Pos.CENTER_RIGHT);
        rightPanel.setPadding(new Insets(0, 0, 0, 20));

        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_PURPLE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(12, 20, 12, 20));
        header.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        return header;
    }

    private HBox createFloatingButtons() {
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(0, 30, 80, 0)); // 调整bottom值控制垂直位置

        // 创建语音输入按钮
        Button voiceBtn = new Button("🎤 Voice Input");
        styleFloatingButton(voiceBtn);

        // 创建手动输入按钮
        Button manualBtn = new Button("✏️ Manual Input");
        styleFloatingButton(manualBtn);

        buttonContainer.getChildren().addAll(voiceBtn, manualBtn);
        return buttonContainer;
    }

    // 新增方法：设置浮动按钮样式
    private void styleFloatingButton(Button btn) {
        btn.setStyle("-fx-background-color: #8064E4;" +       // 按钮背景色
                "-fx-text-fill: white;" +                  // 文字颜色
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 20px;" +          // 圆角半径
                "-fx-padding: 10 20 10 20;" +             // 内边距
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.2, 0, 2);"); // 阴影效果

        // 悬停效果
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #6A50C2;" +  // 悬停时颜色加深
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 10 20 10 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.3, 0, 3);"
        ));

        // 离开效果
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #8064E4;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 10 20 10 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.2, 0, 2);"
        ));
    }

    // 修改后的底部导航栏（保持原有功能）
    private HBox createBottomNavigation() {
        HBox navBar = new HBox();
        navBar.setBackground(new Background(new BackgroundFill(
                DARK_NAV_BG, CornerRadii.EMPTY, Insets.EMPTY)));
        navBar.setPadding(new Insets(15));
        navBar.setAlignment(Pos.CENTER);
        navBar.setSpacing(60);

        String[][] navItems = {
                {"HOME", "M2 12l10-9 10 9v12H2z", "M12 5l-7 7h4v5h6v-5h4z"},
                {"STAR", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"},
                {"SETTINGS", "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"}
        };

        for (String[] item : navItems) {
            VBox navButton = createNavItem(item[0], item[1]);
            navBar.getChildren().add(navButton);
        }

        return navBar;
    }

    private VBox createNavItem(String title, String svgPath) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.WHITE);
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);

        Label label = new Label(title);
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px;");

        StackPane clickArea = new StackPane(container);
        clickArea.setStyle("-fx-cursor: hand; -fx-padding: 10px;");
        clickArea.setOnMouseClicked(e -> handleNavClick(title));

        clickArea.hoverProperty().addListener((obs, oldVal, isHovering) -> {
            container.setBackground(isHovering ?
                    new Background(new BackgroundFill(NAV_HOVER, new CornerRadii(5), Insets.EMPTY)) :
                    Background.EMPTY);
        });

        clickArea.setOnMousePressed(e ->
                container.setBackground(new Background(new BackgroundFill(NAV_SELECTED, new CornerRadii(5), Insets.EMPTY))));

        container.getChildren().addAll(icon, label);
        return container;
    }

    private void handleNavClick(String item) {
        System.out.println("Navigation switching: " + item);
    }

    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.55);

        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label balanceTitle = new Label("Total expenditure");
        balanceTitle.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        Label balanceValue = new Label(String.format("¥ %.2f", totalExpenditure));
        balanceValue.setStyle("-fx-text-fill: #333333; -fx-font-size: 32px; -fx-font-weight: bold;");
        VBox balanceBox = new VBox(5, balanceTitle, balanceValue);

        leftPanel.getChildren().addAll(
                balanceBox,
                createPieChart(),
                createProgressSection(),
                createButtonPanel()
        );

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20, 20, 20, 0));
        rightPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search transaction records...");
        searchField.setPrefWidth(200);

        searchBox.getChildren().addAll(
                new Label("Recent consumption"),
                new Region(),
                searchField
        );
        HBox.setHgrow(searchBox.getChildren().get(1), Priority.ALWAYS);

        ListView<String> transactionList = new ListView<>();
        transactionList.setItems(transactionItems);
        transactionList.setCellFactory(lv -> new TransactionCell());
        transactionList.setStyle("-fx-border-color: #e6e6e6;");

        rightPanel.getChildren().addAll(searchBox, transactionList);
        VBox.setVgrow(transactionList, Priority.ALWAYS);

        splitPane.getItems().addAll(leftPanel, rightPanel);
        return splitPane;
    }

    private PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.getData().addAll(
                categoryTotals.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
        chart.setLegendVisible(false);
        chart.setPrefSize(350, 250);
        chart.setStyle("-fx-border-color: #f0f0f0;");
        return chart;
    }

    private VBox createProgressSection() {
        VBox progressBox = new VBox(15);
        progressBox.setPadding(new Insets(10, 0, 0, 0));

        String[] items = {
                "loan repayment:0.2:20%",
                "Monthly budget:0.65:65%",
                "Current plan:0.3:30%"
        };

        Arrays.stream(items).forEach(item -> {
            String[] parts = item.split(":");

            VBox container = new VBox(8);
            container.setPadding(new Insets(0, 15, 0, 5));

            HBox labelRow = new HBox();
            labelRow.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label(parts[0]);
            titleLabel.setStyle("-fx-text-fill: #333333;-fx-font-family: 'Segoe UI';-fx-font-weight: bold;-fx-font-size: 14px;");

            Label percentLabel = new Label(parts[2]);
            percentLabel.setStyle("-fx-text-fill: #666666;-fx-font-family: 'Segoe UI';-fx-font-size: 13px;");
            HBox.setMargin(percentLabel, new Insets(0, 0, 0, 10));

            StackPane progressContainer = new StackPane();
            progressContainer.setStyle("-fx-background-color: #F5F1FF;-fx-pref-height: 20px;-fx-border-radius: 10;");

            ProgressBar progressBar = new ProgressBar(Double.parseDouble(parts[1]));
            progressBar.setStyle("-fx-accent: #8064E4;-fx-background-color: transparent;-fx-pref-width: 400px;-fx-pref-height: 20px;");

            progressContainer.getChildren().add(progressBar);

            labelRow.getChildren().addAll(titleLabel, percentLabel);
            container.getChildren().addAll(labelRow, progressContainer);
            progressBox.getChildren().add(container);
        });

        return progressBox;
    }

    private HBox createButtonPanel() {
        Button modifyBtn = new Button("Modification reminder");
        modifyBtn.setStyle("-fx-background-color: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-text-fill: white; -fx-padding: 8 20;");

        Button detailsBtn = new Button("more details");
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-border-color: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-border-radius: 3; -fx-padding: 8 20;");

        HBox buttonBox = new HBox(15, modifyBtn, detailsBtn);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        return buttonBox;
    }

    class TransactionCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String[] parts = item.split(" - ");
                String[] timeCat = parts[0].split(" • ");

                Label timeLabel = new Label(timeCat[0]);
                timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

                Label category = new Label(timeCat[1]);
                category.setStyle("-fx-text-fill: " +
                        (getIndex() % 2 == 0 ? PRIMARY_PURPLE.toString().replace("0x", "#") : "#333333") +
                        "; -fx-font-weight: bold;");

                VBox timeBox = new VBox(2, timeLabel, category);
                Label amount = new Label(parts[1]);
                amount.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");

                Label date = new Label(parts[2]);
                date.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

                Region spacer = new Region();
                HBox content = new HBox(20, timeBox, spacer, amount, date);
                HBox.setHgrow(spacer, Priority.ALWAYS);

                VBox container = new VBox(content);
                container.setBackground(new Background(new BackgroundFill(
                        getIndex() % 2 == 0 ? Color.rgb(250, 250, 250) : Color.WHITE,
                        CornerRadii.EMPTY, Insets.EMPTY)));
                container.setPadding(new Insets(8, 15, 8, 15));
                container.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

                setGraphic(container);
            }
        }
    }

    class Expense {
        private LocalDateTime transactionTime;
        private double amount;
        private String counterpart;
        private String product;

        public Expense(LocalDateTime transactionTime, double amount, String counterpart, String product) {
            this.transactionTime = transactionTime;
            this.amount = amount;
            this.counterpart = counterpart;
            this.product = product;
        }

        public LocalDateTime getTransactionTime() { return transactionTime; }
        public double getAmount() { return amount; }
        public String getCounterpart() { return counterpart; }
        public String getProduct() { return product; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}