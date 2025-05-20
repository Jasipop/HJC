// package Merge;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
import javafx.scene.Cursor;
import javafx.application.HostServices;
import javafx.scene.Node;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Nutllet extends Application {
    private static final Color PRIMARY_PURPLE = Color.rgb(133, 95, 175);
    private static final Color LIGHT_PURPLE_BG = Color.rgb(245, 241, 255);
    private static final Color DARK_NAV_BG = Color.rgb(40, 40, 40);
    private static final Color NAV_HOVER = Color.rgb(70, 70, 70);
    private static final Color NAV_SELECTED = Color.rgb(90, 90, 90);

    private Map<String, Double> categoryTotals = new HashMap<>();
    private ObservableList<String> transactionItems = FXCollections.observableArrayList();
    private double totalExpenditure;
    private HostServices hostServices;
    private List<Expense> expenses = new ArrayList<>();
    private List<Expense> sortedExpenses = new ArrayList<>();
    private PieChart pieChart;
    private Label balanceValue;

    @Override
    public void start(Stage primaryStage) {
        String csvFileName = "deals.csv";
        loadExpensesFromCSV(csvFileName);
        processData(expenses);
        this.hostServices = getHostServices();
        BorderPane root = new BorderPane();
        root.setTop(createHeader());

        ScrollPane mainScroll = new ScrollPane(createMainContent());
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: white;");
        root.setCenter(mainScroll);

        root.setBottom(createBottomNavigation());

        // 创建浮动按钮容器
        HBox floatButtons = createFloatButtons();

        // 使用StackPane叠加布局
        StackPane rootContainer = new StackPane();
        rootContainer.getChildren().addAll(root, floatButtons);
        StackPane.setAlignment(floatButtons, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(floatButtons, new Insets(620, 20, 70, 0));

        Scene scene = new Scene(rootContainer, 1366, 768);
        primaryStage.setTitle("NUTLLET - Financial Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setMaxWidth(Double.MAX_VALUE); // 允许内容扩展
        mainContent.setFillWidth(true); // 启用填充宽度

        SplitPane splitPane = new SplitPane();
        splitPane.setMaxWidth(Double.MAX_VALUE); // 分割面板填满宽度

        // 设置左右面板的宽度约束
        VBox left = createLeftPanel();
        VBox right = createRightPanel();
        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);

        splitPane.getItems().addAll(left, right);
        splitPane.setDividerPosition(0, 0.55);

        // AI部分添加宽度约束
        VBox aiSection = createAIConsumptionSection();
        aiSection.setMaxWidth(Double.MAX_VALUE);

        mainContent.getChildren().addAll(splitPane, aiSection);
        return mainContent;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        leftPanel.setMaxWidth(Double.MAX_VALUE); // 允许扩展
        // 余额部分
        VBox balanceBox = new VBox(5);
        Label balanceTitle = new Label("Total expenditure");
        balanceTitle.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        this.balanceValue = new Label(String.format("¥ %.2f", totalExpenditure));
        balanceValue.setStyle("-fx-text-fill: #333333; -fx-font-size: 32px; -fx-font-weight: bold;");
        balanceBox.getChildren().addAll(balanceTitle, balanceValue);
        this.pieChart = createPieChart();
        leftPanel.getChildren().addAll(
                balanceBox,
                this.pieChart,
                createProgressSection(),
                createButtonPanel()
        );
        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        rightPanel.setMaxWidth(Double.MAX_VALUE); // 允许扩展

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Label searchLabel = new Label("Recent consumption");
        searchLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold; -fx-font-size: 16px;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search transaction records...");
        searchField.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 5 10;");
        searchField.setPrefWidth(250);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchLabel, spacer, searchField);

        // FilteredList 用于过滤列表
        FilteredList<String> filteredData = new FilteredList<>(transactionItems, s -> true);

        ListView<String> transactionList = new ListView<>(filteredData);
        transactionList.setCellFactory(lv -> new TransactionCell());
        transactionList.setStyle("-fx-border-color: #e6e6e6; -fx-background-color: transparent;");

        // 设置“无匹配项”时的占位文本
        transactionList.setPlaceholder(new Label("No relevant records"));

        // 搜索框监听器
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return item.toLowerCase().contains(lowerCaseFilter);
            });
        });

        rightPanel.getChildren().addAll(searchBox, transactionList);
        VBox.setVgrow(transactionList, Priority.ALWAYS);
        return rightPanel;
    }

    private int currentAIPage = 0;
    private final List<String> aiContents = Arrays.asList(
            "On the next period, you may need to be extra vigilant to avoid over-spending on food and drink consuming.\n\n" +
                    "This could involve:\n" +
                    "• Reducing the frequency of dining out at expensive restaurants\n" +
                    "• Opting for home-cooked meals to control ingredients quality\n" +
                    "• Planning meals in advance with detailed shopping lists\n" +
                    "• Choosing tap water over bottled beverages\n\n",

            "Financial Advice\n\n" +
                    "According to the analysis of your consumption habits, catering expenses account for 28%. " +
                    "It is recommended to optimize the catering consumption structure.\n\n" +
                    "Currently 35% of the budget remains, and you may consider transferring part of the funds " +
                    "to a financial management account to earn returns."
    );
    //    private Circle dot1, dot2;
    private StackPane dot1, dot2;

    private TextArea aiContent;

    private VBox createAIConsumptionSection() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30, 50, 30, 50));
        container.setBackground(new Background(new BackgroundFill(
                Color.web("#F5F1FF"), new CornerRadii(10), Insets.EMPTY
        )));
        container.setAlignment(Pos.TOP_CENTER);

        // 标题部分
        Label title = new Label("AI 消费分析");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_PURPLE);
        title.setPadding(new Insets(0, 0, 15, 0));

        // 分析内容区域
        TextArea aiContent = new TextArea();
        aiContent.setEditable(false);
        aiContent.setWrapText(true);
        aiContent.setStyle("-fx-background-color: white; " +
                "-fx-text-fill: #666666; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;");
        aiContent.setPrefHeight(180);
        aiContent.setText("正在初始化本地AI分析引擎...");

        // 进度指示器
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        // 控制按钮
        Button analyzeBtn = new Button("开始分析");
        analyzeBtn.setStyle("-fx-background-color: #855FAF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 30; " +
                "-fx-background-radius: 20;");

        // 布局容器
        StackPane contentPane = new StackPane(aiContent, progress);
        VBox controls = new VBox(15, analyzeBtn);
        controls.setAlignment(Pos.CENTER);

        container.getChildren().addAll(title, contentPane, controls);

        // 分析按钮事件
        analyzeBtn.setOnAction(e -> {
            aiContent.setText("正在分析消费数据...");
            progress.setVisible(true);
            analyzeBtn.setDisable(true);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    Process process = new ProcessBuilder(
                            "ollama", "run", "qwen2:1.5b"
                    ).start();

                    // 构建输入
                    String prompt = "请分析以下消费记录，用中文给出专业建议：\n" +
                            loadCSVForAnalysis() +
                            "\n请按以下格式回复：" +
                            "\n1. 消费趋势总结（不超过100字）" +
                            "\n2. 三条优化建议（每条带●符号）" +
                            "\n3. 风险预警（如有）";

                    OutputStream stdin = process.getOutputStream();
                    stdin.write(prompt.getBytes());
                    stdin.flush();
                    stdin.close();

                    // 读取输出
                    InputStream stdout = process.getInputStream();
                    StringBuilder analysis = new StringBuilder();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = stdout.read(buffer)) != -1) {
                        analysis.append(new String(buffer, 0, bytesRead));
                    }

                    // 处理结果
                    String formatted = formatAnalysis(analysis.toString());

                    Platform.runLater(() -> {
                        aiContent.setText(formatted);
                        progress.setVisible(false);
                        analyzeBtn.setDisable(false);
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        aiContent.setText("分析失败: " + ex.getMessage());
                        progress.setVisible(false);
                        analyzeBtn.setDisable(false);
                    });
                }
            });
        });

        return container;
    }

    private String loadCSVForAnalysis() {
        return expenses.stream()
                .map(e -> String.format("[%s] %s - ¥%.2f (%s)",
                        e.getTransactionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        e.getCounterpart(),
                        e.getAmount(),
                        e.getProduct()))
                .collect(Collectors.joining("\n"));
    }

    private String formatAnalysis(String raw) {
        // 基础格式清理
        return raw.replaceAll("(?m)^\\s*\\d+\\.?", "\n●")
                .replaceAll("\n+", "\n")
                .replaceAll("(\\p{Lu}):", "\n$1：")
                .trim();
    }

    private StackPane createInteractiveDot(int pageIndex) {
        Circle dot = new Circle(6);
        dot.setFill(pageIndex == currentAIPage ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        dot.setStroke(Color.web("#999999"));
        dot.setStrokeWidth(0.5);

        StackPane clickableDot = new StackPane(dot);
        clickableDot.setPadding(new Insets(8));  // 扩大点击区域
        clickableDot.setCursor(Cursor.HAND);
        clickableDot.setOnMouseClicked(e -> {
            if(pageIndex != currentAIPage) {
                Platform.runLater(() -> switchAIPage(pageIndex));
            }
        });
        return clickableDot;
    }

    private Button createActionButton() {
        SVGPath arrowIcon = new SVGPath();
        arrowIcon.setContent("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        arrowIcon.setFill(Color.WHITE);

        HBox btnContent = new HBox(8, new Label("View Details"), arrowIcon);
        btnContent.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(btnContent);
        button.setStyle("-fx-background-color: #855FAF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 30; " +
                "-fx-background-radius: 20;");

        // 修改点击事件为打开网页
        button.setOnAction(e -> {
            hostServices.showDocument("https://chat.deepseek.com");
        });

        return button;
    }

    // 修改后的分页切换方法
    private void switchAIPage(int page) {
        currentAIPage = page;
        aiContent.setText(aiContents.get(currentAIPage));

        // 更新分页点颜色（通过访问StackPane中的圆形）
        updateDotColor(dot1, 0);
        updateDotColor(dot2, 1);

        // 动态样式调整
        String styleBase = "-fx-background-color: transparent; -fx-font-family: 'Segoe UI';";
        aiContent.setStyle(styleBase + (currentAIPage == 1 ?
                "-fx-text-fill: #666666; -fx-font-size: 15px;" :
                "-fx-text-fill: #333333; -fx-font-size: 14px;"));
    }

    private void updateDotColor(StackPane dotPane, int targetPage) {
        Circle dot = (Circle) dotPane.getChildren().get(0);
        dot.setFill(currentAIPage == targetPage ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
    }

    // 保留其他原有方法（完整实现）
    private HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(PRIMARY_PURPLE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(12, 20, 12, 20));

        HBox leftPanel = new HBox(10);
        Label title = new Label("NUTLLET");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label edition = new Label("Personal Edition");
        edition.setFont(Font.font("Segoe UI", 12));
        edition.setTextFill(Color.rgb(255, 255, 255, 0.6));
        leftPanel.getChildren().addAll(title, edition);

        HBox rightPanel = new HBox(15);
        String[] buttons = {"Syncing", "Enterprise Edition", "Logout"};
        for (String btnText : buttons) {
            Button btn = new Button(btnText);
            if (btnText.equals("Enterprise Edition")) {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #855FAF; -fx-border-radius: 3;");
                btn.setOnAction(e -> {
                    try {
                        new NutlletEnterprise().start(new Stage());
                        ((Stage) btn.getScene().getWindow()).close(); // 关闭当前页面
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
            else if (btnText.equals("Syncing")) {
                // 设置Syncing按钮的样式和点击事件
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
                btn.setOnAction(e -> {
                    // 刷新数据并更新UI
                    loadExpensesFromCSV("deals.csv");
                    processData(expenses);
                    updateUI();
                });
            }else if (btnText.equals("Logout")) {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #855FAF; -fx-border-radius: 3;");
                btn.setOnAction(e -> {
                    try {
                        new Login().start(new Stage());
                        ((Stage) btn.getScene().getWindow()).close(); // 关闭当前页面
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            }

            rightPanel.getChildren().add(btn);
        }

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        header.getChildren().addAll(leftPanel, rightPanel);
        return header;
    }

    // 新增浮动按钮创建方法
    private HBox createFloatButtons() {
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        // 语音输入按钮
        Button voiceBtn = createFloatingButton(
                "M12 3v10.28c-.6-.34-1.3-.55-2-.55-2.2 0-4 1.8-4 4s1.8 4 4 4 4-1.8 4-4V3h2v10.28c-.6-.34-1.3-.55-2-.55-2.2 0-4 1.8-4 4s1.8 4 4 4 4-1.8 4-4V3h2v18h-2v-2.07c-1.4 1.1-3.2 1.8-5 1.8-3.3 0-6-2.7-6-6V3h2z",
                "Voice Input"
        );

        // 手动输入按钮
        Button manualBtn = createFloatingButton(
                "M3 17.46v3.04h3.04L17.46 9.62l-3.04-3.04L3 17.46zm18.72-12.33l-2.68 2.68-3.04-3.04 2.68-2.68c.4-.4 1.04-.4 1.44 0l1.6 1.6c.4.4.4 1.04 0 1.44z",
                "Manual Input"
        );

        buttonContainer.getChildren().addAll(voiceBtn, manualBtn);
        return buttonContainer;
    }

    private Button createFloatingButton(String svgPath, String text) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.rgb(64, 64, 64));
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #404040; -fx-font-size: 12px; -fx-font-weight: 500;");

        HBox buttonContent = new HBox(8, icon, label);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setPadding(new Insets(8, 15, 8, 15));

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setStyle("-fx-background-color: #E6E6FA; " +
                "-fx-background-radius: 25; " +
                "-fx-border-radius: 25; " +
                "-fx-border-color: #D0D0D0; " +
                "-fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.1, 0, 2);");
        button.setOnAction(e -> handleFloatButtonClick(text));

        return button;
    }

    private void handleFloatButtonClick(String buttonType) {
        if ("Manual Input".equals(buttonType)) {
            showManualInputDialog();
        } else {
            System.out.println("触发功能: " + buttonType);
        }
    }
    private void showManualInputDialog() {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Manually enter consumption records");
        dialog.setHeaderText("Please enter detailed consumption information");

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // 创建表单字段
        TextField timeField = new TextField();
        timeField.setPromptText("YYYY-MM-DD HH:mm:ss");
        TextField counterpartField = new TextField();
        TextField productField = new TextField();
        TextField amountField = new TextField();

        grid.add(new Label("Time of transaction:"), 0, 0);
        grid.add(timeField, 1, 0);
        grid.add(new Label("Counterparty:"), 0, 1);
        grid.add(counterpartField, 1, 1);
        grid.add(new Label("Product Description:"), 0, 2);
        grid.add(productField, 1, 2);
        grid.add(new Label("Amount (Yuan):"), 0, 3);
        grid.add(amountField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        // 输入验证
        ChangeListener<String> inputValidator = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                boolean isValid = !timeField.getText().isEmpty()
                        && !counterpartField.getText().isEmpty()
                        && !productField.getText().isEmpty()
                        && !amountField.getText().matches(".*[^0-9.].*");
                confirmButton.setDisable(!isValid);
            }
        };

        timeField.textProperty().addListener(inputValidator);
        counterpartField.textProperty().addListener(inputValidator);
        productField.textProperty().addListener(inputValidator);
        amountField.textProperty().addListener(inputValidator);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                try {
                    LocalDateTime time = LocalDateTime.parse(timeField.getText(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    double amount = Double.parseDouble(amountField.getText());
                    return new Expense(time, amount, counterpartField.getText(),
                            productField.getText(), "支出", "支付成功");
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid input format").show();
                }
            }
            return null;
        });

        Optional<Expense> result = dialog.showAndWait();
        result.ifPresent(expense -> {
            expenses.add(expense);
            processData(expenses);
            updateUI();
            saveExpensesToCSV("deals.csv"); // 新增保存方法
        });
    }
    // 新增保存方法
    private void saveExpensesToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 写入文件头
            writer.write("微信支付账单明细 微信昵称：[Q·ð 起始时间：[2025-04-10 09:40:16] 终止时间：[2025-04-18 19:55:33]");
            writer.newLine();
            writer.write("----------------------微信支付账单明细列表--------------------");
            writer.newLine();
            writer.write("交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注");
            writer.newLine();

            // 写入所有记录
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Expense expense : expenses) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"¥%.2f\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                        expense.getTransactionTime().format(formatter),
                        "商户消费",
                        expense.getCounterpart(),
                        expense.getProduct(),
                        expense.getType(),
                        expense.getAmount(),
                        "零钱",
                        expense.getStatus(),
                        "", "", ""
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "保存失败: " + e.getMessage()).show();
        }
    }

    // 更新UI的方法
    private void updateUI() {
        // 安全检测
        if (pieChart != null) {
            pieChart.getData().clear();
            pieChart.getData().addAll(categoryTotals.entrySet().stream()
                    .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList()));
        }

        if (balanceValue != null) {
            balanceValue.setText(String.format("¥ %.2f", totalExpenditure));
        }
    }

    // 修改后的底部导航栏方法
    private HBox createBottomNavigation() {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        // 创建带图标的导航按钮
        Button homeBtn = createNavButtonWithEmoji("Home", "🏠");
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙");

        // 设置Home按钮初始颜色
        setButtonColor(homeBtn, true);  // 紫色
        setButtonColor(discoverBtn, false); // 默认灰色
        setButtonColor(settingsBtn, false); // 默认灰色

        // 事件处理（保持原有逻辑）
        homeBtn.setOnAction(e -> handleNavigation(homeBtn, new Nutllet()));
        discoverBtn.setOnAction(e -> handleNavigation(discoverBtn, new Discover()));
        settingsBtn.setOnAction(e -> handleNavigation(settingsBtn, new Settings()));

        // 从右到左排列按钮
        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);

        // 设置按钮等宽
        HBox.setHgrow(settingsBtn, Priority.ALWAYS);
        HBox.setHgrow(discoverBtn, Priority.ALWAYS);
        HBox.setHgrow(homeBtn, Priority.ALWAYS);

        return navBar;
    }

    private void handleNavigation(Button sourceButton, Application app) {
        try {
            app.start(new Stage());
            ((Stage) sourceButton.getScene().getWindow()).close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Button createNavButtonWithEmoji(String label, String emoji) {
        VBox btnContainer = new VBox();
        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(2);

        // 表情符号标签
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        // 文本标签
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 14px;");

        btnContainer.getChildren().addAll(emojiLabel, textLabel);

        // 按钮样式设置
        Button button = new Button();
        button.setPrefWidth(456);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        // 悬停效果
        button.hoverProperty().addListener((obs, oldVal, isHovering) -> {
            // 获取按钮的激活状态
            Boolean isActive = (Boolean) button.getUserData();

            // 如果是激活状态，不改变背景色
            if (isActive != null && isActive) return;

            // 非激活按钮处理悬停效果
            String bgColor = isHovering ? "#f8f9fa" : "white";
            button.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: transparent;");
        });

        return button;
    }

    // 设置按钮颜色状态
    private void setButtonColor(Button button, boolean isActive) {
        VBox container = (VBox) button.getGraphic();
        String color = isActive ? "#855FAF" : "#7f8c8d";

        // 存储激活状态到按钮属性
        button.setUserData(isActive); // 新增

        // 设置文字颜色
        for (Node node : container.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: " + color + ";");
            }
        }

        // 强制设置激活按钮背景色
        String bgColor = isActive ? "#f8f9fa" : "white"; // 移除悬停判断
        button.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: transparent;");
    }

    private PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.getData().addAll(
                categoryTotals.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
        chart.setLegendVisible(false);
        chart.setStyle("-fx-border-color: #f0f0f0;");
        chart.setPrefSize(350, 250);
        return chart;
    }

    class TransactionCell extends ListCell<String> {
        private final HBox container;
        private final Label timeLabel;
        private final Label categoryLabel;
        private final Label amountLabel;
        private final Label dateLabel;
        private final Button deleteButton;

        public TransactionCell() {
            super();

            // 初始化UI组件
            timeLabel = new Label();
            timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

            categoryLabel = new Label();
            categoryLabel.setStyle("-fx-font-weight: bold;");

            VBox timeBox = new VBox(2, timeLabel, categoryLabel);

            amountLabel = new Label();
            amountLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");

            dateLabel = new Label();
            dateLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

            deleteButton = new Button("×");
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-weight: bold;");
            deleteButton.setVisible(false);
            deleteButton.setOnAction(e -> handleDelete());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            container = new HBox(20, timeBox, spacer, amountLabel, dateLabel, deleteButton);
            container.setPadding(new Insets(8, 15, 8, 15));
            container.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250), CornerRadii.EMPTY, Insets.EMPTY)));

            // 鼠标悬停显示删除按钮
            setOnMouseEntered(e -> deleteButton.setVisible(true));
            setOnMouseExited(e -> deleteButton.setVisible(false));
        }

        private void handleDelete() {
            int index = getIndex();
            if (index < 0 || index >= sortedExpenses.size()) return;

            // 确认对话框
            Expense toRemove = sortedExpenses.get(index);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Entry");
            alert.setHeaderText("Are you sure you want to delete this record?");
            alert.setContentText(String.format("%s - ¥%.2f", toRemove.getProduct(), toRemove.getAmount()));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 从原始数据中移除并更新
                expenses.remove(toRemove);
                processData(expenses);
                saveExpensesToCSV("deals.csv");
                updateUI();
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                // 解析数据显示
                String[] parts = item.split(" - ");
                String[] timeCat = parts[0].split(" • ");

                timeLabel.setText(timeCat[0]);
                categoryLabel.setText(timeCat[1]);
                categoryLabel.setStyle("-fx-text-fill: " + (getIndex() % 2 == 0 ? "#855FAF" : "#333333"));
                amountLabel.setText(parts[1]);
                dateLabel.setText(parts[2]);

                // 设置交替背景色
                BackgroundFill bgFill = new BackgroundFill(
                        getIndex() % 2 == 0 ? Color.rgb(250, 250, 250) : Color.WHITE,
                        CornerRadii.EMPTY, Insets.EMPTY
                );
                container.setBackground(new Background(bgFill));

                setGraphic(container);
            }
        }
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
            progressBar.setStyle("-fx-accent: #855FAF;-fx-background-color: transparent;-fx-pref-width: 400px;-fx-pref-height: 20px;");

            progressContainer.getChildren().add(progressBar);

            labelRow.getChildren().addAll(titleLabel, percentLabel);
            container.getChildren().addAll(labelRow, progressContainer);
            progressBox.getChildren().add(container);
        });

        return progressBox;
    }

    private void handleNavClick(String item) {
        System.out.println("Navigation switching: " + item);
    }

    private HBox createButtonPanel() {
        Button modifyBtn = new Button("Modification reminder");
        modifyBtn.setStyle("-fx-background-color: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-text-fill: white; -fx-padding: 8 20;");
        modifyBtn.setOnAction(e -> {
            try {
                new NutlletAddNewReminder().start(new Stage());
                ((Stage) modifyBtn.getScene().getWindow()).close(); // 关闭当前页面
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button detailsBtn = new Button("more details");
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " +
                PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-border-color: " + PRIMARY_PURPLE.toString().replace("0x", "#") +
                "; -fx-border-radius: 3; -fx-padding: 8 20;");
        detailsBtn.setOnAction(e -> {
            try {
                new NutlletReminder().start(new Stage());
                ((Stage) detailsBtn.getScene().getWindow()).close(); // 关闭当前页面
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox buttonBox = new HBox(15, modifyBtn, detailsBtn);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        return buttonBox;
    }

    private void loadExpensesFromCSV(String filePath) {
        expenses.clear(); // 使用已初始化的成员变量
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isDataSection = false;
            List<String> headers = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.contains("微信支付账单明细列表")) {
                    isDataSection = true;
                    headers = Arrays.asList(br.readLine().split(",")); // 读取标题行
                    continue;
                }

                if (isDataSection && !line.trim().isEmpty()) {
                    Map<String, String> record = parseCSVLine(line, headers);

                    // 确保字段正确性
                    if ("支出".equals(record.get("收/支")) &&
                            "支付成功".equals(record.get("当前状态"))) {

                        // 明确解析所有必要字段
                        LocalDateTime time = LocalDateTime.parse(
                                record.get("交易时间"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                        double amount = Double.parseDouble(
                                record.get("金额(元)").replace("¥", "").trim()
                        );
                        String counterpart = record.get("交易对方");
                        String product = record.get("商品");
                        String type = record.get("收/支");
                        String status = record.get("当前状态");

                        // 正确创建Expense对象并添加到列表
                        Expense expense = new Expense(
                                time, amount, counterpart, product, type, status
                        );
                        expenses.add(expense);
                    }
                }
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "加载数据失败: " + e.getMessage()).show();
        }
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

        sortedExpenses = expenses.stream()
                .sorted(Comparator.comparing(Expense::getTransactionTime).reversed())
                .collect(Collectors.toList());

        transactionItems.setAll(sortedExpenses.stream()
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

        if (counterpart.contains("美团") || counterpart.contains("食堂") || product.contains("餐") || product.contains("茶") || counterpart.contains("农夫山泉")|| counterpart.contains("岳西科技")) {
            return "Catering";
        } else if (counterpart.contains("滴滴") || counterpart.contains("加油站") || counterpart.contains("石油") || counterpart.contains("北京一卡通") || counterpart.contains("携程")) {
            return "Traffic";
        } else if (counterpart.contains("电影院") || product.contains("游戏") || counterpart.contains("休息") || counterpart.contains("Apple")|| product.contains("apple")) {
            return "Entertainment";
        } else if (counterpart.contains("超市") || product.contains("日用品") || counterpart.contains("叮咚") || counterpart.contains("京东到家")) {
            return "Living";
        }else if (product.contains("会员")) {
            return "Periodic";
        }else if (product.contains("转账") || product.contains("/")) {
            return "Social";
        }else if (product.contains("银行")) {
            return "Funds flow";
        }else {
            return "Other";
        }
    }

    class Expense {
        private LocalDateTime transactionTime;
        private double amount;
        private String counterpart;
        private String product;
        private String type;
        private String status;

        public Expense(LocalDateTime transactionTime, double amount, String counterpart,
                       String product, String type, String status) {
            this.transactionTime = transactionTime;
            this.amount = amount;
            this.counterpart = counterpart;
            this.product = product;
            this.type = type;
            this.status = status;
        }
        public LocalDateTime getTransactionTime() { return transactionTime; }
        public double getAmount() { return amount; }
        public String getCounterpart() { return counterpart; }
        public String getProduct() { return product; }
        // 补充getter方法
        public String getType() { return type; }
        public String getStatus() { return status; }
    }
    public static void main(String[] args) {
        launch(args);
    }
}