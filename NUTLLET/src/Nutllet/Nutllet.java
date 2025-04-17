package Nutllet;

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

class Discover extends Application {
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("Discover Page"), 1366, 768));
        stage.show();
    }
}

class Settings extends Application {
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("Settings Page"), 1366, 768));
        stage.show();
    }
}

public class Nutllet extends Application {
    private static final Color PRIMARY_PURPLE = Color.rgb(128, 100, 228);
    private static final Color LIGHT_PURPLE_BG = Color.rgb(245, 241, 255);
    private static final Color DARK_NAV_BG = Color.rgb(40, 40, 40);
    private static final Color NAV_HOVER = Color.rgb(70, 70, 70);
    private static final Color NAV_SELECTED = Color.rgb(90, 90, 90);

    private Map<String, Double> categoryTotals = new HashMap<>();
    private ObservableList<String> transactionItems = FXCollections.observableArrayList();
    private double totalExpenditure;
    private HostServices hostServices;
    
    @Override
    public void start(Stage primaryStage) {
    	String csvFileName = "微信支付账单(20250413-20250414)——【解压密码可在微信支付公众号查看】.csv";
        List<Expense> expenses = loadExpensesFromCSV(csvFileName);
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
        Label balanceValue = new Label(String.format("¥ %.2f", totalExpenditure));
        balanceValue.setStyle("-fx-text-fill: #333333; -fx-font-size: 32px; -fx-font-weight: bold;");
        balanceBox.getChildren().addAll(balanceTitle, balanceValue);
        
        leftPanel.getChildren().addAll(
            balanceBox, 
            createPieChart(),
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
        
        ListView<String> transactionList = new ListView<>(transactionItems);
        transactionList.setCellFactory(lv -> new TransactionCell());
        transactionList.setStyle("-fx-border-color: #e6e6e6; -fx-background-color: transparent;");
        
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
    private Circle dot1, dot2;
    private TextArea aiContent;

    private VBox createAIConsumptionSection() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30, 50, 30, 50));
        container.setBackground(new Background(new BackgroundFill(
            Color.web("#F5F1FF"), new CornerRadii(10), Insets.EMPTY
        )));
        container.setAlignment(Pos.TOP_CENTER);

        // 标题样式强化
        Label title = new Label("AI Consumption Analysis");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_PURPLE);
        title.setPadding(new Insets(0, 0, 15, 0));

        // 支持多行居中的内容区域
        aiContent = new TextArea();
        aiContent.setEditable(false);
        aiContent.setWrapText(true);
        aiContent.setStyle("-fx-background-color: transparent; " +
                         "-fx-text-fill: #666666; " +
                         "-fx-font-size: 14px; " +
                         "-fx-font-family: 'Segoe UI'; " +
                         "-fx-text-alignment: center;");  // 文本居中
        aiContent.setPrefHeight(180);
        aiContent.setMouseTransparent(true);  // 禁用文本选择
        aiContent.setText(aiContents.get(currentAIPage));

        // 控制容器布局优化
        VBox controlContainer = new VBox(20);
        controlContainer.setAlignment(Pos.CENTER);
        
        // 增强分页指示器
        HBox pagination = new HBox(15);
        pagination.setAlignment(Pos.CENTER);
        dot1 = createInteractiveDot(0);
        dot2 = createInteractiveDot(1);
        pagination.getChildren().addAll(dot1, dot2);

        // 带图标的功能按钮
        Button actionButton = createActionButton();
        
        controlContainer.getChildren().addAll(actionButton, pagination);
        container.getChildren().addAll(title, aiContent, controlContainer);

        return container;
    }

    private Circle createInteractiveDot(int pageIndex) {
        Circle dot = new Circle(6);
        dot.setFill(pageIndex == currentAIPage ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        dot.setStroke(Color.web("#999999"));
        dot.setStrokeWidth(0.5);
        dot.setCursor(Cursor.HAND);
        
        dot.setOnMouseClicked(e -> {
            currentAIPage = pageIndex;
            updateAIContent();
        });
        return dot;
    }

    private Button createActionButton() {
        SVGPath arrowIcon = new SVGPath();
        arrowIcon.setContent("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        arrowIcon.setFill(Color.WHITE);

        HBox btnContent = new HBox(8, new Label("View Details"), arrowIcon);
        btnContent.setAlignment(Pos.CENTER);

        Button button = new Button();
        button.setGraphic(btnContent);
        button.setStyle("-fx-background-color: #8064E4; " +
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
    
    private void switchAIPage(int page) {
        currentAIPage = page;
        aiContent.setText(aiContents.get(currentAIPage));
        dot1.setFill(currentAIPage == 0 ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        dot2.setFill(currentAIPage == 1 ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        
        // 动态样式调整
        if(currentAIPage == 1) {
            aiContent.setStyle("-fx-text-fill: #666666; -fx-font-size: 15px;");
        } else {
            aiContent.setStyle("-fx-text-fill: #333333; -fx-font-size: 14px;");
        }
    }
    private void updateAIContent() {
        aiContent.setText(aiContents.get(currentAIPage));
        dot1.setFill(currentAIPage == 0 ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        dot2.setFill(currentAIPage == 1 ? PRIMARY_PURPLE : Color.web("#D8D8D8"));
        
        // 动态调整内容格式
        if(currentAIPage == 1) {
            aiContent.setStyle("-fx-text-fill: #666666; " +
                             "-fx-font-size: 15px; " +
                             "-fx-font-family: 'Segoe UI'; " +
                             "-fx-text-alignment: center;");
        } else {
            aiContent.setStyle("-fx-text-fill: #333333; " +
                             "-fx-font-size: 14px;");
        }
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
                btn.setStyle("-fx-background-color: white; -fx-text-fill: #8064E4; -fx-border-radius: 3;");
            } else {
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
        System.out.println("触发功能: " + buttonType);
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
        String color = isActive ? "#8064E4" : "#7f8c8d";
        
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
                    (getIndex() % 2 == 0 ? "#8064E4" : "#333333") + 
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
    
    private void handleNavClick(String item) {
        System.out.println("Navigation switching: " + item);
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