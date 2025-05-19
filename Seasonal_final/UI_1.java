package myapp;

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
    private BarChart<Number, String> barChart2;
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

        loadDataFromCSV(); // 启动时自动加载deals.csv

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


        /*homeBtn.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });*/

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

        // 添加导入账单按钮
        Button importBtn = new Button("导入账单");
        importBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        importBtn.setOnAction(e -> handleImportBill());
        box.getChildren().add(importBtn);

        // 添加导出账单按钮
        Button exportBtn = new Button("导出账单");
        exportBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        exportBtn.setOnAction(e -> handleExportBill());
        box.getChildren().add(exportBtn);
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

        // 添加删除按钮
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            BudgetData selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dataList.remove(selected);
                refreshDataDisplay();
                saveDataToCSV();
            } else {
                showAlert("Please select a row to delete!");
            }
        });

        VBox container = new VBox(10);
        container.getChildren().addAll(
                createLabel("Budget Data", Color.PURPLE, 16),
                tableView,
                deleteBtn
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

        // 收支比例图（横向）
        NumberAxis xAxis2 = new NumberAxis();
        CategoryAxis yAxis2 = new CategoryAxis();
        barChart2 = new BarChart<>(xAxis2, yAxis2);
        barChart2.setTitle("Income/Expenses Ratio");
        xAxis2.setLabel("Ratio");
        yAxis2.setLabel("Festival");
        barChart2.setCategoryGap(20);
        barChart2.setPrefSize(600, 400);

        chartsBox.getChildren().addAll(barChart1, barChart2);
        return chartsBox;
    }

    // CSV操作相关方法
    private void saveDataToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
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
            showAlert("Failed to save data to CSV file!");
        }
    }

    private void loadDataFromCSV() {
        dataList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            boolean inData = false;
            while ((line = reader.readLine()) != null) {
                // 跳过无关行，直到遇到节日预算或表头
                if (!inData) {
                    if (line.contains("节日预算") || line.contains("Festival")) {
                        inData = true;
                    }
                    continue;
                }
                // 处理节日预算数据
                String[] values = line.split(",");
                // 兼容deals.csv和导出的格式
                if (values.length >= 6 && ("节日预算".equals(values[1]) || values[0].matches("\\d{4}-\\d{2}-\\d{2}"))) {
                    String festival = values[2];
                    String date = values[0];
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(values[5].replaceAll("[^\\d]", ""));
                    } catch (Exception ignore) {}
                    String notes = values.length > 10 ? values[10] : (values.length > 5 ? values[5] : "");
                    BudgetData data = new BudgetData(
                        festival,
                        "收入".equals(values[4]) ? amount : 0,
                        "支出".equals(values[4]) ? amount : 0,
                        date,
                        date,
                        notes
                    );
                    dataList.add(data);
                }
            }
            refreshDataDisplay();
            updateCharts();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading data: " + e.getMessage());
        }
    }

    private void updateCharts() {
        // 清除现有数据
        barChart1.getData().clear();
        barChart2.getData().clear();

        // 按节日分组统计收入
        Map<String, Double> festivalIncome = dataList.stream()
            .collect(Collectors.groupingBy(
                BudgetData::getFestival,
                Collectors.summingDouble(BudgetData::getIncome)
            ));

        // 创建收入柱状图数据
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("节日预算收入");
        
        festivalIncome.forEach((festival, income) -> 
            incomeSeries.getData().add(new XYChart.Data<>(festival, income))
        );

        barChart1.getData().add(incomeSeries);
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
            saveDataToCSV();

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

        List<String> festivals = dataList.stream()
                .map(BudgetData::getFestival)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        updateChartCategories(festivals);

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenses");
        XYChart.Series<Number, String> ratioSeries = new XYChart.Series<>();
        ratioSeries.setName("Ratio");

        dataList.forEach(data -> {
            String festival = data.getFestival();
            incomeSeries.getData().add(new XYChart.Data<>(festival, data.getIncome()));
            expensesSeries.getData().add(new XYChart.Data<>(festival, data.getExpenses()));
            double ratio = data.getIncome() / (double) data.getExpenses();
            ratioSeries.getData().add(new XYChart.Data<>(ratio, festival));
        });

        barChart1.getData().clear();
        barChart1.getData().addAll(incomeSeries, expensesSeries);

        barChart2.getData().clear();
        barChart2.getData().add(ratioSeries);
    }

    private void updateChartCategories(List<String> festivals) {
        CategoryAxis xAxis1 = (CategoryAxis) barChart1.getXAxis();
        xAxis1.setCategories(FXCollections.observableArrayList(festivals));

        CategoryAxis yAxis2 = (CategoryAxis) barChart2.getYAxis();
        yAxis2.setCategories(FXCollections.observableArrayList(festivals));
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
        fileChooser.setTitle("选择账单文件");
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
                loadDataFromCSV();
                showInfo("账单导入成功！");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("导入失败：" + e.getMessage());
            }
        }
    }

    // 账单导出处理
    private void handleExportBill() {
        String dealsFile = "deals.csv";
        boolean fileExists = new File(dealsFile).exists();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dealsFile, true), "UTF-8"))) {
            // 如果文件不存在，写表头
            if (!fileExists) {
                writer.write("交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注\n");
            }
            for (BudgetData data : dataList) {
                String tradeTime = data.getStartDate();
                String tradeType = "节日预算";
                String tradeTarget = data.getFestival();
                String product = "";
                String incomeOrExpense = data.getIncome() > 0 ? "收入" : "支出";
                int amount = data.getIncome() > 0 ? data.getIncome() : data.getExpenses();
                String payType = "其他";
                String status = "已导出";
                String tradeNo = "";
                String merchantNo = "";
                String notes = data.getNotes();
                writer.write(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s\n",
                        tradeTime, tradeType, tradeTarget, product, incomeOrExpense, amount, payType, status, tradeNo, merchantNo, notes.replace(",", " ")));
            }
        } catch (Exception ex) {
            showAlert("账单导出失败：" + ex.getMessage());
            return;
        }
        // 导出成功弹窗
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("导出成功");
        alert.setHeaderText(null);
        alert.setContentText("已成功导出");
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
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