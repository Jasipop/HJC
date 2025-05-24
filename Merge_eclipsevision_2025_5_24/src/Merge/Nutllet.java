package Merge;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.stage.Window;
import javafx.stage.Modality;
import java.time.format.DateTimeParseException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
    private TextArea aiContent;
    private VBox leftPanel;
    private VBox progressSection;
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
    
    private List<Reminder> loadReminders() {
        NutlletReminder reminderApp = new NutlletReminder();
        reminderApp.loadData(); // 加载原始数据
        reminderApp.calculateBalance(); // 计算进度数据
        return reminderApp.reminders;
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
        leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        leftPanel.setMaxWidth(Double.MAX_VALUE);

        VBox balanceBox = new VBox(5);
        Label balanceTitle = new Label("Total expenditure");
        balanceTitle.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        this.balanceValue = new Label(String.format("¥ %.2f", totalExpenditure));
        balanceValue.setStyle("-fx-text-fill: #333333; -fx-font-size: 32px; -fx-font-weight: bold;");
        balanceBox.getChildren().addAll(balanceTitle, balanceValue);
        
        this.pieChart = createPieChart();
        progressSection = createProgressSection(); // 初始化进度条部分
        
        leftPanel.getChildren().addAll(
            balanceBox,
            this.pieChart,
            progressSection,
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

    private VBox createAIConsumptionSection() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30, 50, 30, 50));
        container.setBackground(new Background(new BackgroundFill(
                Color.web("#F5F1FF"), new CornerRadii(10), Insets.EMPTY
        )));
        container.setAlignment(Pos.TOP_CENTER);

        // 标题部分
        Label title = new Label("AI consumption analysis");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_PURPLE);
        title.setPadding(new Insets(0, 0, 15, 0));

        // 分析内容区域
        this.aiContent = new TextArea();
        aiContent.setEditable(false);
        aiContent.setWrapText(true);
        aiContent.setStyle("-fx-background-color: white; " +
                "-fx-text-fill: #666666; " +
                "-fx-font-size: 14px; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;");
        aiContent.setPrefHeight(180);
        aiContent.setText("Initializing local AI analysis engine...");

        // 进度指示器
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        // 控制按钮
        Button analyzeBtn = new Button("Start analysis");
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
            aiContent.setText("Analyzing consumption data...");
            progress.setVisible(true);
            analyzeBtn.setDisable(true);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    Process process = new ProcessBuilder(
                            "ollama", "run", "qwen2:1.5b"
                    ).start();

                    // 构建输入
                    String prompt = "Please analyze the following consumption records and give professional advice in English：\n" + 
                                   loadCSVForAnalysis() + 
                                   "\nPlease reply in the following format and translate the answers into English:" +
                                   "\n1. summary of consumption trend (no more than 100 words)" +
                                   "\n2. three optimization suggestions" +
                                   "\n3. Risk warning (if any)";

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
                        aiContent.setText("Analysis failed: " + ex.getMessage());
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
        Button textInputBtn = createFloatingButton(
                "M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z", 
                "Text Input"
        );

        // 手动输入按钮
        Button manualBtn = createFloatingButton(
                "M3 17.46v3.04h3.04L17.46 9.62l-3.04-3.04L3 17.46zm18.72-12.33l-2.68 2.68-3.04-3.04 2.68-2.68c.4-.4 1.04-.4 1.44 0l1.6 1.6c.4.4.4 1.04 0 1.44z",
                "Manual Input"
        );

        buttonContainer.getChildren().addAll(textInputBtn, manualBtn);
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
        } else if ("Text Input".equals(buttonType)) {
            showTextInputDialog();
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
                        productField.getText(), "Expenditure", "Payment successful");
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
    
    private void handleTextInput(String text) {
    	Platform.runLater(() -> {
            aiContent.setText("Analyzing text input...\nPlease wait...");
            aiContent.setDisable(true);
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Process process = new ProcessBuilder(
                    "ollama", "run", "qwen2:1.5b"
                ).start();

                // 构建AI提示词
                String prompt = """
                	    STRICT JSON FORMAT REQUIRED! Please extract expense records from the text according to the following rules：
                	    1. time processing:
                	       - Format must beyyyy-MM-dd HH:mm:ss
                	       - If there is no definite time in the text, it is inferred by semantics (such as "yesterday" ->current date -1 day)
                	    2. amount processing:
                	       - Unified conversion to RMB (such as "100 yuan" ->100, "¥ 38.5" ->38.5)
                	    3. merchant identification:
                	       - Standardized name (such as "McDonald's" ->"McDonald's Restaurant")
                	    4. Product Description:
                	       - Retain key information and remove modifiers (such as "delicious hamburger" ->"hamburger")
                	    5. classification logic:
                	    -Catering (including restaurant/food)
                        -Transportation (including travel/refueling)
                        -Entertainment (including movies/games)
                        -Life (supermarket/daily necessities)
                        -Others

                	    Sample input: "it cost 125 yuan to order takeout in meituan on March 15, and didi 38 yuan to take a taxi on March 16"
                        Sample output:
                	    [
                	      {
                	        "transaction_time": "2025-03-15 18:00:00",
                	        "amount": 125.0,
                	        "counterpart": "meituan takeout",
                	        "product": "take out order",
                	        "category": "Catering"
                	      },
                	      {
                	        "transaction_time": "2025-03-16 09:30:00", 
                	        "amount": 38.0,
                	        "counterpart": "Didi trip",
                	        "product": "car Hailing",
                	        "category": "Transportation"
                	      }
                	    ]

                	    Please process the following inputs:
                	    """ + text;
                
                OutputStream stdin = process.getOutputStream();
                stdin.write(prompt.getBytes("UTF-8"));
                stdin.flush();
                stdin.close();

                // 读取AI响应
                InputStream stdout = process.getInputStream();
                StringBuilder response = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stdout.read(buffer)) != -1) {
                    response.append(new String(buffer, 0, bytesRead));
                }

                // 解析JSON响应
                List<Expense> newExpenses = parseAIResponse(response.toString());
                
                Platform.runLater(() -> {
                    if (!newExpenses.isEmpty()) {
                        confirmAndSaveExpenses(newExpenses);
                    } else {
                        aiContent.setText("No valid expenses found in the text.");
                    }
                    aiContent.setDisable(false);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    aiContent.setText("Analysis failed: " + ex.getMessage());
                    aiContent.setDisable(false);
                });
            }
        });
    }
    
    private void confirmAndSaveExpenses(List<Expense> newExpenses) {
        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Confirm Expense Records");
        confirmDialog.setHeaderText("Found the following expense entries, confirm to save?\\n(Editable before saving)");

        // 创建带可编辑列的表格
        TableView<Expense> tableView = new TableView<>();
        
        // 原有列定义保持不变...
        TableColumn<Expense, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cd -> 
            new SimpleStringProperty(cd.getValue().getTransactionTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        
        TableColumn<Expense, String> counterpartCol = new TableColumn<>("Counterparty");
        counterpartCol.setCellValueFactory(cd -> 
            new SimpleStringProperty(cd.getValue().getCounterpart()));
        
        TableColumn<Expense, Number> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cd -> 
            new SimpleDoubleProperty(cd.getValue().getAmount()));
        
        TableColumn<Expense, String> productCol = new TableColumn<>("Product Description");
        productCol.setCellValueFactory(cd -> 
            new SimpleStringProperty(cd.getValue().getProduct()));
        
        // 新增可编辑列
        TableColumn<Expense, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✎");
            {
                editBtn.setStyle("-fx-background-color: #E8EAF6; -fx-text-fill: #1A237E;");
                editBtn.setOnAction(e -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    showEditDialog(expense); // 弹出编辑对话框
                    tableView.refresh(); // 刷新表格显示
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        // 添加所有列到表格（包括原有列和新增的editCol）
        tableView.getColumns().addAll(timeCol, counterpartCol, amountCol, productCol, editCol);
        tableView.setItems(FXCollections.observableArrayList(newExpenses));
        tableView.setPrefHeight(300);

        // 添加反馈按钮
        Button feedbackBtn = new Button("Report Recognition Issue");
        feedbackBtn.setStyle("-fx-text-fill: #B71C1C; -fx-border-color: #D32F2F;");
        feedbackBtn.setOnAction(e -> collectFeedback(newExpenses));

        // 创建底部按钮容器
        HBox buttonBox = new HBox(15, feedbackBtn);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, tableView, buttonBox);
        content.setPrefWidth(700);
        
        confirmDialog.getDialogPane().setContent(content);
        confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            expenses.addAll(newExpenses);
            processData(expenses);
            saveExpensesToCSV("deals.csv");
            updateUI();
            aiContent.setText("Successfully saved " + newExpenses.size() + " new records!");
        }
    }

    // 新增的编辑对话框方法
    private void showEditDialog(Expense expense) {
        Dialog<Expense> editDialog = new Dialog<>();
        editDialog.setTitle("Edit consumption record");
        editDialog.setHeaderText("Correction of automatic identification results");

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TextField timeField = new TextField(expense.getTransactionTime().format(formatter));
        TextField counterpartField = new TextField(expense.getCounterpart());
        TextField productField = new TextField(expense.getProduct());
        TextField amountField = new TextField(String.valueOf(expense.getAmount()));

        grid.addRow(0, new Label("Time:"), timeField);
        grid.addRow(1, new Label("Counterparty:"), counterpartField);
        grid.addRow(2, new Label("Product:"), productField);
        grid.addRow(3, new Label("Amount:"), amountField);

        // 输入验证
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        Node saveButton = editDialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false); // 根据需要进行输入验证

        editDialog.getDialogPane().setContent(grid);
        editDialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    return new Expense(
                        LocalDateTime.parse(timeField.getText(), formatter),
                        Double.parseDouble(amountField.getText()),
                        counterpartField.getText(),
                        productField.getText(),
                        "Expenditure",
                        "Payment successful"
                    );
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid input format").show();
                }
            }
            return null;
        });

        Optional<Expense> result = editDialog.showAndWait();
        result.ifPresent(edited -> {
            expense.setTransactionTime(edited.getTransactionTime());
            expense.setAmount(edited.getAmount());
            expense.setCounterpart(edited.getCounterpart());
            expense.setProduct(edited.getProduct());
        });
    }

    // 新增的反馈收集方法
    private void collectFeedback(List<Expense> aiResults) {
        Dialog<Void> feedbackDialog = new Dialog<>();
        feedbackDialog.setTitle("Problem feedback");
        feedbackDialog.setHeaderText("Please describe the details of the identification error");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("For example, \"100 yuan\" is recognized as 10 yuan due to the wrong recognition of the amount unit ");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefSize(500, 200);

        Button submitBtn = new Button("Submit feedback");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitBtn.setOnAction(e -> {
            try {
                String logEntry = String.format(
                    "[%s] Original identification result:%s User feedback:%s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    aiResults.toString(),
                    feedbackArea.getText()
                );
                
                Files.write(Paths.get("ai_feedback.log"), 
                    logEntry.getBytes(), 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.APPEND);
                
                new Alert(Alert.AlertType.INFORMATION, "Feedback submitted successfully!").show();
                feedbackDialog.close();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Unable to save feedback：" + ex.getMessage()).show();
            }
        });

        VBox content = new VBox(15, feedbackArea, submitBtn);
        content.setPadding(new Insets(15));
        
        feedbackDialog.getDialogPane().setContent(content);
        feedbackDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        feedbackDialog.showAndWait();
    }
    
    private void showTextInputDialog() {
    	Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Text Input Analysis");
        dialog.setHeaderText("Please enter or paste the consumption record text");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Examples：\n"
            + "On March 15, it cost 125.5 yuan to buy daily necessities in the supermarket\n"
            + "At noon on March 16, the consumption of McDonald's was 38 yuan");
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 300);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // 在开始分析时设置提示
                aiContent.setText("Starting text analysis...");
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> handleTextInput(text));
    }

    // 新增AI响应解析方法
    private List<Expense> parseAIResponse(String response) {
        try {
            // 清理响应内容并提取JSON
            String jsonStr = response.substring(response.indexOf("["), response.lastIndexOf("]") + 1)
                    .replaceAll("\\\\\"", "")
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // 配置多种时间格式
            List<DateTimeFormatter> formatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy年M月d日 H时m分s秒"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")  // 西式格式
            );

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> data = mapper.readValue(jsonStr, new TypeReference<List<Map<String, Object>>>() {});

            return data.stream()
                .map(map -> {
                    try {
                        // 解析时间字段
                        String rawTime = map.getOrDefault("transaction_time", "").toString();
                        if (rawTime.isEmpty()) {
                            throw new RuntimeException("Missing transaction_time");
                        }

                        // 尝试多种格式解析
                        LocalDateTime parsedTime = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                parsedTime = LocalDateTime.parse(rawTime, formatter);
                                break;
                            } catch (DateTimeParseException ignored) {}
                        }

                        if (parsedTime == null) {
                            throw new RuntimeException("Unsupported time format: " + rawTime);
                        }

                        // 解析金额
                        double amount = 0.0;
                        Object amountObj = map.get("amount");
                        if (amountObj instanceof Number) {
                            amount = ((Number) amountObj).doubleValue();
                        } else if (amountObj != null) {
                            String amountStr = amountObj.toString()
                                .replace("¥", "")
                                .replace("￥", "")
                                .trim();
                            try {
                                amount = Double.parseDouble(amountStr);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Invalid amount format: " + amountStr);
                            }
                        }

                        // 构建Expense对象
                        return new Expense(
                            parsedTime,
                            amount,
                            map.getOrDefault("counterpart", "").toString(),
                            map.getOrDefault("product", "").toString(),
                            "Expenditure",
                            "Payment successful"
                        );

                    } catch (Exception e) {
                        System.err.println("Error parsing record: " + e.getMessage());
                        return null; // 返回null将在后续过滤
                    }
                })
                .filter(Objects::nonNull) // 过滤掉解析失败的记录
                .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    // 新增保存方法
    private void saveExpensesToCSV(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

            int headerIndex = -1;
            int dataEndIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (headerIndex == -1 && line.startsWith("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status")) {
                    headerIndex = i;
                }
                if (headerIndex != -1 && i > headerIndex && (line.startsWith("----------------------") || line.isEmpty())) {
                    dataEndIndex = i;
                    break;
                }
            }
            if (dataEndIndex == -1) dataEndIndex = lines.size();

            // 保留非商户消费的原始数据行
            List<String> existingDataLines = lines.subList(headerIndex + 1, dataEndIndex).stream()
                .filter(line -> {
                    String[] parts = line.split(",");
                    return parts.length > 1 && !parts[1].contains("Merchant Consumption");
                })
                .collect(Collectors.toList());

            // 生成当前所有有效数据行（包括新添加的）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<String> currentDataLines = expenses.stream()
                .map(e -> String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\"",
                    e.getTransactionTime().format(formatter),
                    "Merchant Consumption", // 强制类型为商户消费
                    e.getCounterpart(),
                    e.getProduct(),
                    e.getType(),
                    e.getAmount(),
                    "Change",
                    e.getStatus()))
                .collect(Collectors.toList());

            // 合并数据：保留原始非商户消费数据 + 当前所有商户消费数据
            List<String> mergedData = new ArrayList<>();
            mergedData.addAll(existingDataLines);
            mergedData.addAll(currentDataLines);

            // 构建新的文件内容
            List<String> newLines = new ArrayList<>();
            newLines.addAll(lines.subList(0, headerIndex + 1));
            newLines.addAll(mergedData);
            newLines.addAll(lines.subList(dataEndIndex, lines.size()));

            Files.write(Paths.get(filePath), newLines, StandardCharsets.UTF_8);

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Save failed: " + e.getMessage()).show();
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
        VBox newProgressSection = createProgressSection();
        int progressIndex = leftPanel.getChildren().indexOf(progressSection);
        if (progressIndex != -1) {
            leftPanel.getChildren().set(progressIndex, newProgressSection);
            progressSection = newProgressSection;
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

        List<Reminder> reminders = loadReminders().stream()
                .limit(3) // 仅取前三个提醒事项
                .collect(Collectors.toList());

        for (Reminder reminder : reminders) {
            VBox container = new VBox(8);
            container.setPadding(new Insets(0, 15, 0, 5));

            HBox labelRow = new HBox();
            labelRow.setAlignment(Pos.CENTER_LEFT);

            // 标题和金额范围
            Label titleLabel = new Label(reminder.name);
            titleLabel.setStyle("-fx-text-fill: #333333;-fx-font-family: 'Segoe UI';-fx-font-weight: bold;-fx-font-size: 14px;");

            Label amountLabel = new Label(String.format("¥%.0f-%.0f", 
                reminder.minAmount, 
                reminder.maxAmount));
            amountLabel.setStyle("-fx-text-fill: #666666;-fx-font-family: 'Segoe UI';-fx-font-size: 13px;");
            HBox.setMargin(amountLabel, new Insets(0, 0, 0, 10));

            // 进度条容器
            StackPane progressContainer = new StackPane();
            progressContainer.setStyle("-fx-background-color: #F5F1FF;-fx-pref-height: 20px;-fx-border-radius: 10;");

            // 动态进度条
            ProgressBar progressBar = new ProgressBar(reminder.progress / 100);
            progressBar.setStyle("-fx-accent: #855FAF;-fx-background-color: transparent;-fx-pref-width: 400px;-fx-pref-height: 20px;");

            // 进度描述
            Label progressLabel = new Label(reminder.progressText);
            progressLabel.setStyle("-fx-text-fill: #666666;-fx-font-family: 'Segoe UI';-fx-font-size: 13px;");

            // 组装组件
            labelRow.getChildren().addAll(titleLabel, amountLabel);
            progressContainer.getChildren().add(progressBar);
            container.getChildren().addAll(labelRow, progressContainer, progressLabel);
            progressBox.getChildren().add(container);
        }

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
                if (line.contains("WeChat Payment Statement Details List")) {
                    isDataSection = true;
                    headers = Arrays.asList(br.readLine().split(",")); // 读取标题行
                    continue;
                }

                if (isDataSection && !line.trim().isEmpty()) {
                    Map<String, String> record = parseCSVLine(line, headers);
                    
                    // 确保字段正确性
                    if ("Expenditure".equals(record.get("Income/Expense")) && 
                    	"Merchant Consumption".equals(record.get("Transaction Type"))&&
                        "Payment successful".equals(record.get("Current Status"))) {
                        
                        // 明确解析所有必要字段
                        LocalDateTime time = LocalDateTime.parse(
                            record.get("Transaction Time"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                        double amount = Double.parseDouble(
                            record.get("Amount (Yuan)").replace("¥", "").trim()
                        );
                        String counterpart = record.get("Counterparty");
                        String product = record.get("Product");
                        String type = record.get("Income/Expense");
                        String status = record.get("Current Status");

                        // 正确创建Expense对象并添加到列表
                        Expense expense = new Expense(
                            time, amount, counterpart, product, type, status
                        );
                        expenses.add(expense);
                    }
                }
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load data: " + e.getMessage()).show();
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
    	for (Expense expense : expenses) {
            if (expense.getCategory() == null) {
                String initialCategory = categorizeExpense(expense);
                expense.setCategory(initialCategory);
                if ("Other".equals(initialCategory)) {
                    triggerAIClassification(expense);
                }
            }
        }

        // 更新统计逻辑
        categoryTotals = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCategory(),
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
                        e.getCategory(),
                        e.getAmount(),
                        e.getTransactionTime().format(dateFormatter)))
                .collect(Collectors.toList()));
    }
    private void triggerAIClassification(Expense expense) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Process process = new ProcessBuilder("ollama", "run", "qwen2:1.5b").start();
                
                // 构建分类提示
                String prompt = String.format(
                	    "STRICT CLASSIFICATION TASK\n" +
                	    "Please classify the consumption records according to the following criteria (only return the name of the English category):\n" +
                	    "1. Catering - Catering consumption (restaurants, takeout, beverage stores)\n" +
                	    "2. Traffic - Transportation expenses (travel tools, cars, high-speed rail, aircraft, refueling)\n" +
                	    "3. Entertainment - Entertainment consumption (film and television, games, performances)\n" +
                	    "4. Living - Living consumption (supermarkets, daily necessities)\n" +
                	    "5. Periodic - Regular expenses (subscription, loan)\n" +
                	    "6. Social - Social expenses (transfer, red envelope)\n" +
                	    "7. Funds flow - Capital flow (financial management, repayment)\n" +
                	    "Examples：\n" +
                	    "Input: counterparty「Starbucks」，Product「large American」 → Catering\n" +
                	    "Input: counterparty「Airline company」，Product「Air ticket」 → Traffic\n\n" +
                	    "Please classify：\n" +
                	    "Counterparty：%s\n" +
                	    "Product：%s\n" +
                	    "ANSWER:",
                	    expense.getCounterpart(), 
                	    expense.getProduct()
                	);


                // 发送请求
                OutputStream stdin = process.getOutputStream();
                stdin.write(prompt.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
                stdin.close();

                // 读取响应
                InputStream stdout = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                String response = reader.lines().collect(Collectors.joining());
                
                // 解析并验证分类结果
                String aiCategory = parseAICategory(response);
                
                // 更新UI
                Platform.runLater(() -> {
                    expense.setCategory(aiCategory);
                    processData(expenses);
                    updateUI();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    expense.setCategory("Other");
                    updateUI();
                });
            }
        });
    }

    private String parseAICategory(String response) {
        // 提取第一个匹配的有效分类
        Pattern pattern = Pattern.compile(
            "(Traffic|Entertainment|Catering|Living|Periodic|Social|Funds flow)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1) : "Other";
    }
    private String categorizeExpense(Expense expense) {
        String counterpart = expense.getCounterpart().toLowerCase();
        String product = expense.getProduct().toLowerCase();

        if (counterpart.contains("meituan") || counterpart.contains("restaurant") || counterpart.contains("canteen") || product.contains("meal") || product.contains("tea") || counterpart.contains("nongfu spring")|| counterpart.contains("yuxi technology")) {
            return "Catering";
        } else if (counterpart.contains("didi") || counterpart.contains("petrochemical") || counterpart.contains("gasoline") || counterpart.contains("yikatong") || counterpart.contains("ctrip")|| counterpart.contains("air")) {
            return "Traffic";
        } else if (counterpart.contains("cinema") || product.contains("game") || counterpart.contains("rest") || counterpart.contains("apple")|| product.contains("apple")) {
            return "Entertainment";
        } else if (counterpart.contains("supermarket") || product.contains("daily necessities") || counterpart.contains("dingdong") || counterpart.contains("jd")) {
            return "Living";
        }else if (product.contains("member")) {
            return "Periodic";
        }else if (product.contains("transfer") || product.contains("/")) {
            return "Social";
        }else if (product.contains("bank")) {
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
        private String category;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
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
        public void setTransactionTime(LocalDateTime transactionTime) {
            this.transactionTime = transactionTime;
        }
        
        public void setAmount(double amount) {
            this.amount = amount;
        }
        
        public void setCounterpart(String counterpart) {
            this.counterpart = counterpart;
        }
        
        public void setProduct(String product) {
            this.product = product;
        }
        @Override
        public String toString() {
            return String.format("%s - %s - ¥%.2f", 
                transactionTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                counterpart, amount);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
    
}