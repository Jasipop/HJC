package Merge;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UI_1 extends Application {

    private TableView<BudgetData> tableView;
    private BarChart<String, Number> barChart1;
    private BarChart<String, Number> barChart2;
    private final List<BudgetData> dataList = new ArrayList<>();
    private static final String CSV_FILE = "deals.csv";

    @Override
    public void start(Stage primaryStage) {
        BorderPane mainLayout = new BorderPane();

        // 创建主内容容器
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #FFF0F5;");

        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(10);
        contentGrid.setVgap(10);
        contentGrid.setPadding(new Insets(20));
        contentGrid.setStyle("-fx-background-color: #FFF0F5;");

        // 标题
        Label titleLabel = new Label("Localized Budgeting");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.PURPLE);
        titleLabel.setAlignment(Pos.CENTER);
        contentGrid.add(titleLabel, 0, 0, 2, 1);

        // 左上角输入区域
        VBox leftTopBox = createLeftTopBox();
        contentGrid.add(leftTopBox, 0, 1);

        // 右上角数据表格
        VBox dataDisplayBox = createDataDisplayBox();
        contentGrid.add(dataDisplayBox, 1, 1);

        // 底部图表区域
        HBox chartsBox = createChartsBox();
        contentGrid.add(chartsBox, 0, 2, 2, 1);

        // 将内容网格放入滚动面板
        scrollPane.setContent(contentGrid);

        // 底部导航栏
        HBox navBar = createNavigationBar(primaryStage);
        mainLayout.setBottom(navBar);
        mainLayout.setCenter(scrollPane);

        initializeData(); // 先加载默认数据

        Scene scene = new Scene(mainLayout, 1366, 768);
        primaryStage.setTitle("Localized Budgeting");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createNavigationBar(Stage primaryStage) {
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
        return navBar;
    }

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

    private VBox createLeftTopBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #FFF0F5; -fx-border-color: #FFC0CB; -fx-border-width: 2px;");

        ComboBox<String> festivalComboBox = new ComboBox<>();
        festivalComboBox.getItems().addAll(
                "Spring Festival",
                "Dragon Boat Festival",
                "Mid-Autumn Festival",
                "Christmas",
                "Harvest Day",
                "Others"
        );
        festivalComboBox.setPrefWidth(200);
        festivalComboBox.setEditable(true);

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        TextField incomeField = new TextField("0");
        TextField expensesField = new TextField("0");
        TextArea notesArea = new TextArea();

        box.getChildren().addAll(
                createLabel("Festival Selection *", Color.PURPLE),
                festivalComboBox,
                createNoteLabel("Choose the festival and set your preferred budget"),
                createLabel("Festival Date Range *", Color.PURPLE),
                createDateRangeBox(startDatePicker, endDatePicker),
                createNoteLabel("Choose the time range you will receive the budget"),
                createAmountBox(incomeField, expensesField),
                createNoteLabel("Enter the amount value"),
                createLabel("Notes", Color.PURPLE),
                notesArea,
                createToolbar(festivalComboBox, startDatePicker, endDatePicker, incomeField, expensesField, notesArea)
        );

        return box;
    }

    private HBox createToolbar(ComboBox<String> comboBox, DatePicker startDate, DatePicker endDate,
                               TextField income, TextField expenses, TextArea notes) {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> handleSave(comboBox, startDate, endDate, income, expenses, notes));

        toolbar.getChildren().add(saveBtn);
        return toolbar;
    }

    private VBox createDataDisplayBox() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        tableView = new TableView<>();

        // 表格列定义
        TableColumn<BudgetData, String> festivalCol = new TableColumn<>("Festival");
        festivalCol.setCellValueFactory(c -> c.getValue().festivalProperty());

        TableColumn<BudgetData, String> dateCol = new TableColumn<>("Date Range");
        dateCol.setCellValueFactory(c -> c.getValue().dateRangeProperty());

        TableColumn<BudgetData, Number> incomeCol = new TableColumn<>("Income");
        incomeCol.setCellValueFactory(c -> c.getValue().incomeProperty());

        TableColumn<BudgetData, Number> expensesCol = new TableColumn<>("Expenses");
        expensesCol.setCellValueFactory(c -> c.getValue().expensesProperty());

        TableColumn<BudgetData, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(c -> c.getValue().notesProperty());
        notesCol.setPrefWidth(200);

        tableView.getColumns().addAll(festivalCol, dateCol, incomeCol, expensesCol, notesCol);

        // 删除按钮
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            BudgetData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dataList.remove(selected);
                refreshDataDisplay(); 
            } else {
                showAlert("Please select a row to delete!");
            }
        });

        // 导入/导出按钮，放在删除按钮右边
        Button importBtn = new Button("Import bill");
        importBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        importBtn.setOnAction(e -> handleImportBill());

        Button exportBtn = new Button("Export bill");
        exportBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        exportBtn.setOnAction(e -> handleExportBill());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #009688; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> refreshDataDisplay());

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.getChildren().addAll(deleteBtn, importBtn, exportBtn, refreshBtn);

        VBox container = new VBox(10);
        container.getChildren().addAll(
                createLabel("Budget Data", Color.PURPLE, 16),
                tableView,
                btnBox
        );

        return container;
    }

    private HBox createChartsBox() {
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.setPadding(new Insets(20));

        // 收入支出对比图（垂直）
        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis yAxis1 = new NumberAxis();
        barChart1 = new BarChart<>(xAxis1, yAxis1);
        barChart1.setTitle("Income vs Expenses Comparison");
        xAxis1.setLabel("Festival");
        yAxis1.setLabel("Amount");
        barChart1.setCategoryGap(20);
        barChart1.setPrefSize(600, 400);

        // 收入-支出差值图（垂直）
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        barChart2 = new BarChart<>(xAxis2, yAxis2);
        barChart2.setTitle("Income - Expenses");
        xAxis2.setLabel("Festival");
        yAxis2.setLabel("Net Income");
        barChart2.setCategoryGap(20);
        barChart2.setPrefSize(600, 400);

        chartsBox.getChildren().addAll(barChart1, barChart2);
        return chartsBox;
    }

    // CSV操作相关方法
    private void saveDataToCSV() {
        // 使用本地临时文件存储UI数据，而不是直接操作deals.csv
        String tempFile = "ui_budget_data.csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("Festival,Income,Expenses,StartDate,EndDate,Notes\n");
            for (BudgetData data : dataList) {
                String notes = data.getNotes().replace("\"", "\"\"");
                if (notes.contains(",") || notes.contains("\"")) {
                    notes = "\"" + notes + "\"";
                }
                String line = String.format("%s,%d,%d,%s,%s,%s",
                        data.getFestival(),
                        data.getIncome(),
                        data.getExpenses(),
                        data.getStartDate(),
                        data.getEndDate(),
                        notes);
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            showAlert("Failed to save data to temporary file!");
        }
    }

    private void loadDataFromCSV() {
        dataList.clear();
        Map<String, BudgetData> aggregatedDataMap = new HashMap<>(); // 用于聚合数据

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            boolean isFirstLine = true; 
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; 
                    continue;
                }

                String[] values = line.split(",", -1); 

                if (values.length > 1 && "Festival Budget".equals(values[1])) {
                    if (values.length > 5) { 
                        String festivalName = values[2]; 
                        String csvDate = values[0];     
                        int csvAmount = 0;
                        try {
                            String amountStr = values[5].replaceAll("[^0-9]", "");
                            if (!amountStr.isEmpty()) {
                                csvAmount = Integer.parseInt(amountStr);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略解析错误
                        }
                        
                        String incomeOrExpenseType = values[4];
                        String csvNotes = (values.length > 10 && values[10] != null) ? values[10] : "None.";

                        BudgetData budgetEntry = aggregatedDataMap.get(festivalName);

                        if (budgetEntry == null) {
                            int currentIncome = 0;
                            int currentExpenses = 0;
                            if ("Income".equals(incomeOrExpenseType)) {
                                currentIncome = csvAmount;
                            } else if ("Expenditure".equals(incomeOrExpenseType)) {
                                currentExpenses = csvAmount;
                            }
                            budgetEntry = new BudgetData(
                                festivalName,
                                currentIncome,
                                currentExpenses,
                                csvDate, // startDate
                                csvDate, // endDate
                                csvNotes
                            );
                            aggregatedDataMap.put(festivalName, budgetEntry);
                        } else {
                            if ("Income".equals(incomeOrExpenseType)) {
                                budgetEntry.incomeProperty().set(budgetEntry.getIncome() + csvAmount);
                            } else if ("Expenditure".equals(incomeOrExpenseType)) {
                                budgetEntry.expensesProperty().set(budgetEntry.getExpenses() + csvAmount);
                            }
                            // 注意：日期和备注采用首次遇到的信息，此处不更新
                        }
                    }
                }
            }
            dataList.addAll(aggregatedDataMap.values());
            refreshDataDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 数据初始化
    private void initializeData() {
        // 尝试从本地临时数据文件加载
        String tempFile = "ui_budget_data.csv";
        boolean loaded = false;
        
        if (new File(tempFile).exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                dataList.clear();
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        String festival = parts[0];
                        int income = Integer.parseInt(parts[1]);
                        int expenses = Integer.parseInt(parts[2]);
                        String startDate = parts[3];
                        String endDate = parts[4];
                        String notes = parts[5].replace("\"\"", "\"");
                        if (notes.startsWith("\"") && notes.endsWith("\"")) {
                            notes = notes.substring(1, notes.length() - 1);
                        }
                        dataList.add(new BudgetData(festival, income, expenses, startDate, endDate, notes));
                    }
                }
                loaded = !dataList.isEmpty();
            } catch (IOException | NumberFormatException e) {
                // 读取临时文件失败，继续后续处理
            }
        }
        
        // 如果没有从临时文件加载数据，尝试从CSV加载
        if (!loaded) {
            loadDataFromCSV();
        }
        
        // 如果仍然没有数据，加载默认数据
        if (dataList.isEmpty()) {
            dataList.addAll(Arrays.asList(
                new BudgetData("Spring Festival", 3000, 1900, "2024-02-10", "2024-02-17", ""),
                new BudgetData("Dragon Boat Festival", 500, 800, "2024-06-10", "2024-06-12", ""),
                new BudgetData("Mid-Autumn Festival", 700, 750, "2024-09-15", "2024-09-17", ""),
                new BudgetData("Christmas", 1000, 700, "2024-12-25", "2024-12-26", ""),
                new BudgetData("Harvest Day", 500, 800, "2024-10-01", "2024-10-07", "")
            ));
            saveDataToCSV(); // 保存默认数据到临时文件
        }
        
        refreshDataDisplay();
    }

    private void updateCharts(List<String> festivals) {
        // 只统计当前表格中实际存在的节日
        updateChartCategories(festivals);

        // 清除现有数据
        barChart1.getData().clear();
        barChart2.getData().clear();

        // 按节日分组统计收入和支出
        Map<String, Double> festivalIncome = dataList.stream()
            .collect(Collectors.groupingBy(
                BudgetData::getFestival,
                Collectors.summingDouble(BudgetData::getIncome)
            ));
        Map<String, Double> festivalExpenses = dataList.stream()
            .collect(Collectors.groupingBy(
                BudgetData::getFestival,
                Collectors.summingDouble(BudgetData::getExpenses)
            ));

        // 创建收入柱状图数据
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenditure");
        XYChart.Series<String, Number> netSeries = new XYChart.Series<>();
        netSeries.setName("Net income");
        for (String festival : festivals) {
            double income = festivalIncome.getOrDefault(festival, 0.0);
            double expenses = festivalExpenses.getOrDefault(festival, 0.0);
            incomeSeries.getData().add(new XYChart.Data<>(festival, income));
            expensesSeries.getData().add(new XYChart.Data<>(festival, expenses));
            double net = income - expenses;
            XYChart.Data<String, Number> netData = new XYChart.Data<>(festival, net);
            netSeries.getData().add(netData);
        }
        barChart1.getData().addAll(incomeSeries, expensesSeries);
        barChart2.getData().add(netSeries);
        // 设置净收入柱状图颜色
        for (XYChart.Data<String, Number> data : netSeries.getData()) {
            double value = data.getYValue().doubleValue();
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    if (value >= 0) {
                        newNode.setStyle("-fx-bar-fill: #2196F3;"); // 蓝色
                    } else {
                        newNode.setStyle("-fx-bar-fill: #e74c3c;"); // 红色
                    }
                }
            });
            if (data.getNode() != null) {
                if (value >= 0) {
                    data.getNode().setStyle("-fx-bar-fill: #2196F3;");
                } else {
                    data.getNode().setStyle("-fx-bar-fill: #e74c3c;");
                }
            }
        }
    }

    private void handleSave(ComboBox<String> festivalComboBox,
                            DatePicker startDatePicker,
                            DatePicker endDatePicker,
                            TextField incomeField,
                            TextField expensesField,
                            TextArea notesArea) {
        try {
            String festival = festivalComboBox.getValue();
            if (festival == null || festival.trim().isEmpty()) {
                showAlert("Festival cannot be empty!");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
                showAlert("Invalid date range!");
                return;
            }

            int income = Integer.parseInt(incomeField.getText());
            int expenses = Integer.parseInt(expensesField.getText());
            String notes = notesArea.getText().isEmpty() ? "None." : notesArea.getText();

            BudgetData newData = new BudgetData(
                    festival,
                    income,
                    expenses,
                    startDate.toString(),
                    endDate.toString(),
                    notes
            );

            boolean exists = dataList.stream()
                    .anyMatch(d -> d.getFestival().equalsIgnoreCase(festival));

            if (exists) {
                dataList.replaceAll(d ->
                        d.getFestival().equalsIgnoreCase(festival) ? newData : d);
            } else {
                dataList.add(newData);
            }

            refreshDataDisplay(); 
            saveDataToCSV(); // 只保存到临时文件，不影响deals.csv

            // 重置输入字段
            festivalComboBox.setValue(null);
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now());
            incomeField.setText("0");
            expensesField.setText("0");
            notesArea.clear();

        } catch (NumberFormatException e) {
            showAlert("Invalid number format in income/expenses!");
        } catch (Exception e) {
            showAlert("Error saving data: " + e.getMessage());
        }
    }

    private void refreshDataDisplay() {
        tableView.getItems().setAll(FXCollections.observableArrayList(dataList));

        // 统一在此处计算festivals列表，确保是最新且唯一的来源
        List<String> festivals = dataList.stream()
                .map(BudgetData::getFestival)
                .distinct()
                .collect(Collectors.toList());

        updateChartCategories(festivals); // 用最新的festivals更新轴
        updateCharts(festivals); // 将最新的festivals传递给updateCharts
    }

    private void updateChartCategories(List<String> festivals) {
        CategoryAxis xAxis1 = (CategoryAxis) barChart1.getXAxis();
        if (xAxis1.getCategories() != null) {
            xAxis1.getCategories().setAll(festivals);
        } else {
            xAxis1.setCategories(FXCollections.observableArrayList(festivals));
        }

        CategoryAxis xAxis2 = (CategoryAxis) barChart2.getXAxis();
        if (xAxis2.getCategories() != null) {
            xAxis2.getCategories().setAll(festivals);
        } else {
            xAxis2.setCategories(FXCollections.observableArrayList(festivals));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper方法
    private Label createLabel(String text, Color color) {
        return createLabel(text, color, 14);
    }

    private Label createLabel(String text, Color color, int size) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setFont(Font.font("Arial", FontWeight.BOLD, size));
        return label;
    }

    private Label createNoteLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 12));
        label.setTextFill(Color.GRAY);
        return label;
    }

    private HBox createDateRangeBox(DatePicker start, DatePicker end) {
        HBox box = new HBox(10);
        start.setPrefWidth(150);
        end.setPrefWidth(150);
        box.getChildren().addAll(
                new Label("Start Date:"), start,
                new Label("End Date:"), end
        );
        return box;
    }

    private HBox createAmountBox(TextField income, TextField expenses) {
        HBox box = new HBox(10);
        income.setPrefWidth(150);
        expenses.setPrefWidth(150);
        box.getChildren().addAll(
                createLabel("Income", Color.PURPLE), income,
                createLabel("Expenses", Color.PURPLE), expenses
        );
        return box;
    }

    // 账单导入处理
    private void handleImportBill() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select billing file");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // 复制文件到程序目录
                Files.copy(file.toPath(), new File(CSV_FILE).toPath(), 
                    StandardCopyOption.REPLACE_EXISTING);
                
                // 重新加载数据
                dataList.clear(); // 清除现有数据
                loadDataFromCSV(); // 从CSV重新加载
                showInfo("Bill imported successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Import failed：" + e.getMessage());
            }
        }
    }

    // 账单导出处理
    private void handleExportBill() {
        try {
            File csvFile = new File(CSV_FILE);
            List<String> otherLines = new ArrayList<>(); // 非节日预算数据
            List<String> header = new ArrayList<>();
            // 1. 读取原有deals.csv，分离表头、非节日预算数据
            if (csvFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String line;
                    boolean isHeader = true;
                    while ((line = reader.readLine()) != null) {
                        if (isHeader) {
                            header.add(line);
                            isHeader = false;
                            continue;
                        }
                        if (!line.contains("Festival Budget")) {
                            otherLines.add(line);
                        }
                    }
                }
            }
            // 2. 生成当前UI中的节日预算数据行
            List<String> exportLines = new ArrayList<>();
            for (BudgetData data : dataList) {
                String tradeTime = data.getStartDate();
                String tradeType = "Festival Budget";
                String tradeTarget = data.getFestival();
                String product = ""; // 商品,默认为空
                String payType = "Other"; // 支付方式,默认为其他
                String status = "Exported"; // 当前状态,默认为已导出
                String tradeNo = ""; // 交易单号,默认为空
                String merchantNo = ""; // 商户单号,默认为空
                String notes = data.getNotes() == null || data.getNotes().equals("None.") ? "" : data.getNotes();
                String finalNotes = notes.replace(",", " "); // 替换备注中的逗号，避免CSV格式问题

                // 如果有收入，则导出一行收入数据
                if (data.getIncome() > 0) {
                    String incomeOrExpense = "Income";
                    int amount = data.getIncome();
                    exportLines.add(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s",
                            tradeTime, tradeType, tradeTarget, product, incomeOrExpense, 
                            amount, payType, status, tradeNo, merchantNo, 
                            finalNotes));
                }

                // 如果有支出，则导出一行支出数据
                if (data.getExpenses() > 0) {
                    String incomeOrExpense = "Expenditure";
                    int amount = data.getExpenses();
                    exportLines.add(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s",
                            tradeTime, tradeType, tradeTarget, product, incomeOrExpense, 
                            amount, payType, status, tradeNo, merchantNo, 
                            finalNotes));
                }
            }
            // 3. 合并表头+非节日预算数据+当前UI节日预算数据，整体写回deals.csv
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(CSV_FILE, false), "UTF-8"))) {
                if (!header.isEmpty()) {
                    writer.write(header.get(0) + "\n");
                } else {
                    writer.write("Transaction Time,Transaction Type,Counterparty,Product,Income/Expense,Amount (Yuan),Payment Method,Current Status,Transaction Number,Merchant Number,Note\n");
                }
                for (String line : otherLines) {
                    writer.write(line + "\n");
                }
                for (String line : exportLines) {
                    writer.write(line + "\n");
                }
            }
            showInfo("Bill exported successfully! deals.csv has been synchronized with the current data。");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("current CSV path: " + CSV_FILE);
            showAlert("Bill export failed：" + ex.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prompt");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class BudgetData {
        private final SimpleStringProperty festival;
        private final SimpleIntegerProperty income;
        private final SimpleIntegerProperty expenses;
        private final SimpleStringProperty startDate;
        private final SimpleStringProperty endDate;
        private final SimpleStringProperty dateRange;
        private final SimpleStringProperty notes;

        public BudgetData(String festival, int income, int expenses,
                          String startDate, String endDate, String notes) {
            this.festival = new SimpleStringProperty(festival);
            this.income = new SimpleIntegerProperty(income);
            this.expenses = new SimpleIntegerProperty(expenses);
            this.startDate = new SimpleStringProperty(startDate);
            this.endDate = new SimpleStringProperty(endDate);
            this.dateRange = new SimpleStringProperty(startDate + " - " + endDate);
            this.notes = new SimpleStringProperty(notes.isEmpty() ? "None." : notes);
        }

        // Property getters
        public SimpleStringProperty festivalProperty() { return festival; }
        public SimpleIntegerProperty incomeProperty() { return income; }
        public SimpleIntegerProperty expensesProperty() { return expenses; }
        public SimpleStringProperty dateRangeProperty() { return dateRange; }
        public SimpleStringProperty notesProperty() { return notes; }

        // Value getters
        public String getFestival() { return festival.get(); }
        public int getIncome() { return income.get(); }
        public int getExpenses() { return expenses.get(); }
        public String getStartDate() { return startDate.get(); }
        public String getEndDate() { return endDate.get(); }
        public String getNotes() { return notes.get(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}