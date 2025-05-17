package myapp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction_Management_System extends Application {
    private HBox chartContent;
    public static class Transaction {
        private final String date;
        private final String name;
        private final String amount;
        private String type; // 改为非final以便修改

        public Transaction(String date, String name, String amount, String type) {
            this.date = date;
            this.name = name;
            this.amount = amount;
            this.type = type;
        }

        // 新增setter方法
        public void setType(String type) { this.type = type; }
        public String getDate() { return date; }
        public String getName() { return name; }
        public String getAmount() { return amount; }
        public String getType() { return type; }

    }

    private ObservableList<Transaction> transactions;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private PieChart typeChart;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void start(Stage primaryStage) {
        initializeData();
        setupTypeChart();

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();
        HBox mainContent = createMainContent(leftPanel, rightPanel);
        VBox mainLayout = createMainLayout(mainContent);

        // Set background color for the entire scene
        mainLayout.setStyle("-fx-background-color: #f8f0ff;");

        Scene scene = new Scene(mainLayout, 1366, 768);
        primaryStage.setTitle("Transaction Management");
        primaryStage.setScene(scene);
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
            try { new Nutllet.Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
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
        transactions = FXCollections.observableArrayList();
        String csvFile = "src/deals.csv";
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // 跳过前三行
            br.readLine();
            br.readLine();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].replaceAll("^\"|\"$", "").trim();
                }

                if (fields.length < 11) continue;

                String fullDate = fields[0];
                String altDesc = fields[1];
                String desc = fields[2];
                String amountWithSymbol = fields[5];

                if (desc.equals("/") || desc.matches("\\d+")) {
                    desc = altDesc;
                }

                String date = fullDate.split(" ")[0];
                String amount = amountWithSymbol.replaceAll("[¥¥,]", "").trim();

                try {
                    Double.parseDouble(amount);
                    // 初始类型设为Online
                    transactions.add(new Transaction(date, desc, amount, "Online"));
                } catch (NumberFormatException e) {
                    System.err.println("无效金额格式: " + amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private VBox createLeftPanel() {
        TableView<Transaction> table = new TableView<>(transactions);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: transparent;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-table-header-border-color: transparent;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;"
        );
        table.setPrefHeight(350);
        table.setFixedCellSize(40);
        table.setPrefWidth(560);  // 增加表格宽度

        // Add hover effect to table rows
        table.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("删除");
            deleteItem.setStyle("-fx-font-family: 'Microsoft YaHei';");
            deleteItem.setOnAction(event -> {
                Transaction selected = row.getItem();
                if (selected != null) {
                    transactions.remove(selected);
                    updateStatsAndChart();
                }
            });

            contextMenu.getItems().add(deleteItem);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setStyle("-fx-background-color: transparent;");
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f8f0ff;");
                }
            });
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: transparent;");
                }
            });

            return row;
        });

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        dateCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Transaction, String> nameCol = new TableColumn<>("Transaction Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty("¥" + cell.getValue().getAmount()));
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        typeCol.setStyle("-fx-alignment: CENTER;");
        typeCol.setCellFactory(column -> {
            return new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.equals("Online")) {
                            setStyle("-fx-text-fill: #6200ee; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #03dac6; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
        typeCol.setCellFactory(ComboBoxTableCell.forTableColumn("Online", "Offline"));

        typeCol.setOnEditCommit(event -> {
            Transaction transaction = event.getRowValue();
            transaction.setType(event.getNewValue());
            updateStatsAndChart(); // 改为调用综合更新方法
        });

        typeCol.setStyle("-fx-alignment: CENTER;");
        table.getColumns().addAll(dateCol, nameCol, amountCol, typeCol);

        Label title = new Label("Transaction Entry");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#6c757d"));

        Button addButton = new Button("Add Transaction");
        addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #e6d5ff, #cab6f4);" +
                        "-fx-text-fill: #4a4a4a;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 12 30;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );

        addButton.setOnMouseEntered(e ->
                addButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #cab6f4, #e6d5ff);" +
                                "-fx-text-fill: #4a4a4a;" +
                                "-fx-font-family: 'Microsoft YaHei';" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 12 30;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.5, 0, 4);"
                )
        );

        addButton.setOnMouseExited(e ->
                addButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #e6d5ff, #cab6f4);" +
                                "-fx-text-fill: #4a4a4a;" +
                                "-fx-font-family: 'Microsoft YaHei';" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 12 30;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
                )
        );

        addButton.setOnAction(e -> showAddTransactionDialog());

        VBox leftPanel = new VBox(15);
        leftPanel.getChildren().addAll(title, table, addButton);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);"
        );
        leftPanel.setMinWidth(600);
        leftPanel.setMaxWidth(600);
        leftPanel.setPrefHeight(480);
        leftPanel.setMinHeight(480);
        leftPanel.setMaxHeight(480);
        leftPanel.setAlignment(Pos.TOP_CENTER);

        // 移除外部的padding
        VBox wrapper = new VBox(leftPanel);
        wrapper.setAlignment(Pos.TOP_CENTER);

        return wrapper;
    }
    private void updateStatsAndChart() {
        updateTypeChart();
        refreshStatsBox();
    }

    private void refreshStatsBox() {
        VBox newStatsBox = createStatsBox();
        newStatsBox.setMaxWidth(220);
        newStatsBox.setPrefWidth(220);

        // 强制刷新UI组件
        Platform.runLater(() -> {
            chartContent.getChildren().set(1, newStatsBox);
            typeChart.getData().clear();
            updateTypeChart();
        });
    }
    private VBox createRightPanel() {
        Label chartTitle = new Label("Transaction Analysis");
        chartTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        chartTitle.setTextFill(Color.web("#6c757d"));

        VBox statsBox = createStatsBox();
        statsBox.setMaxWidth(220);
        statsBox.setPrefWidth(220);

        typeChart.setPrefSize(300, 300);
        typeChart.setMinSize(300, 300);
        typeChart.setMaxSize(300, 300);

        chartContent = new HBox(20);  // 初始化成员变量
        chartContent.setAlignment(Pos.CENTER);
        chartContent.getChildren().addAll(typeChart, createStatsBox());

        HBox buttonBox = createActionButtons();
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.setMaxWidth(560);

        VBox chartBox = new VBox(15);
        chartBox.getChildren().addAll(chartTitle, chartContent, buttonBox);
        chartBox.setPadding(new Insets(20));
        chartBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);"
        );
        chartBox.setMinWidth(600);
        chartBox.setMaxWidth(600);
        chartBox.setPrefHeight(480);
        chartBox.setMinHeight(480);
        chartBox.setMaxHeight(480);
        chartBox.setAlignment(Pos.TOP_CENTER);

        VBox rightPanel = new VBox(0);
        rightPanel.getChildren().add(chartBox);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        return rightPanel;
    }

    private VBox createStatsBox() {
        VBox statsBox = new VBox(10);  // 减小间距
        statsBox.setStyle(
                "-fx-padding: 15;" +  // 减小内边距
                        "-fx-background-color: linear-gradient(to bottom right, #f8f0ff, #e6d5ff);" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.5, 0, 2);"
        );

        double totalAmount = transactions.stream()
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double onlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Online"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double offlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Offline"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        Label titleLabel = new Label("Statistics");
        titleLabel.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #4a4a4a;"
        );

        // 添加异常处理
        double avg = transactions.isEmpty() ? 0 : totalAmount / transactions.size();

        // 使用更精确的货币格式化
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.CHINA);
        VBox totalBox = createStatsItemBox("Total Amount", fmt.format(totalAmount), "💰");
        VBox onlineBox = createStatsItemBox("Online Total", fmt.format(onlineAmount), "🌐");
        VBox offlineBox = createStatsItemBox("Offline Total", fmt.format(offlineAmount), "🏪");
        VBox avgBox = createStatsItemBox("Average Amount", fmt.format(avg), "📊");

        statsBox.getChildren().addAll(titleLabel, totalBox, onlineBox, offlineBox, avgBox);

        return statsBox;
    }

    private VBox createStatsItemBox(String title, String value, String emoji) {
        VBox itemBox = new VBox(3);  // 减小间距
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8;");  // 减小内边距

        HBox emojiBox = new HBox(5);
        emojiBox.setAlignment(Pos.CENTER_LEFT);
        Label emojiLabel = new Label(emoji);
        Label titleLabel = new Label(title);
        emojiLabel.setStyle("-fx-font-size: 16px;");  // 减小emoji大小
        titleLabel.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-text-fill: #6c757d;"
        );
        emojiBox.getChildren().addAll(emojiLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +  // 减小字体大小
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #4a4a4a;"
        );

        itemBox.getChildren().addAll(emojiBox, valueLabel);
        return itemBox;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button exportButton = new Button("Export Data");
        exportButton.setStyle(
                "-fx-background-color: #e6d5ff;" +
                        "-fx-text-fill: #4a4a4a;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 100;" +
                        "-fx-max-width: 100;"
        );
        exportButton.setOnAction(e -> exportData());

        Button filterButton = new Button("Filter");
        filterButton.setStyle(
                "-fx-background-color: #e6d5ff;" +
                        "-fx-text-fill: #4a4a4a;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 100;" +
                        "-fx-max-width: 100;"
        );
        filterButton.setOnAction(e -> showFilterDialog());

        buttonBox.getChildren().addAll(exportButton, filterButton);
        return buttonBox;
    }

    private void exportData() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export Data");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Choose Export Format");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        Button csvButton = new Button("Export as CSV");
        Button excelButton = new Button("Export as Excel");
        Button pdfButton = new Button("Export as PDF");

        // 设置按钮样式和事件处理
        String buttonStyle = "-fx-background-color: #e6d5ff; -fx-text-fill: #4a4a4a; -fx-font-size: 14px; " +
                "-fx-padding: 8 20; -fx-background-radius: 8; -fx-min-width: 200;";
        
        csvButton.setStyle(buttonStyle);
        excelButton.setStyle(buttonStyle);
        pdfButton.setStyle(buttonStyle);

        csvButton.setOnAction(e -> {
            // 导出CSV逻辑
            exportToCSV();
            dialog.close();
        });

        excelButton.setOnAction(e -> {
            // 导出Excel逻辑
            exportToExcel();
            dialog.close();
        });

        pdfButton.setOnAction(e -> {
            // 导出PDF逻辑
            exportToPDF();
            dialog.close();
        });

        content.getChildren().addAll(titleLabel, csvButton, excelButton, pdfButton);

        Scene dialogScene = new Scene(content, 300, 250);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void exportToCSV() {
        // 实现CSV导出逻辑
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Transaction Name,Amount,Type\n");
        
        for (Transaction t : transactions) {
            csv.append(String.format("%s,%s,%s,%s\n",
                t.getDate(),
                t.getName(),
                t.getAmount(),
                t.getType()));
        }
        
        // 这里应该添加文件保存对话框和实际的文件写入操作
        System.out.println("Exporting to CSV...");
        System.out.println(csv.toString());
    }

    private void exportToExcel() {
        // 实现Excel导出逻辑
        System.out.println("Exporting to Excel...");
        // 这里应该添加Apache POI或其他Excel库的导出实现
    }

    private void exportToPDF() {
        // 实现PDF导出逻辑
        System.out.println("Exporting to PDF...");
        // 这里应该添加iText或其他PDF库的导出实现
    }

    private void showFilterDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Filter Transactions");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Date Range
        Label dateLabel = new Label("Date Range");
        dateLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        HBox dateBox = new HBox(10);
        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();
        startDate.setPromptText("Start Date");
        endDate.setPromptText("End Date");
        dateBox.getChildren().addAll(startDate, endDate);

        // Amount Range
        Label amountLabel = new Label("Amount Range");
        amountLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        HBox amountBox = new HBox(10);
        TextField minAmount = new TextField();
        TextField maxAmount = new TextField();
        minAmount.setPromptText("Min Amount");
        maxAmount.setPromptText("Max Amount");
        amountBox.getChildren().addAll(minAmount, maxAmount);

        // Type Selection
        Label typeLabel = new Label("Transaction Type");
        typeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton allRadio = new RadioButton("All");
        RadioButton onlineRadio = new RadioButton("Online");
        RadioButton offlineRadio = new RadioButton("Offline");
        allRadio.setToggleGroup(typeGroup);
        onlineRadio.setToggleGroup(typeGroup);
        offlineRadio.setToggleGroup(typeGroup);
        allRadio.setSelected(true);

        HBox typeBox = new HBox(20);
        typeBox.getChildren().addAll(allRadio, onlineRadio, offlineRadio);

        Button applyButton = new Button("Apply Filter");
        applyButton.setStyle(
                "-fx-background-color: #cab6f4;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 30;" +
                        "-fx-background-radius: 8;"
        );

        applyButton.setOnAction(e -> {
            // 实现过滤逻辑
            filterTransactions(
                startDate.getValue(),
                endDate.getValue(),
                minAmount.getText(),
                maxAmount.getText(),
                getSelectedType(typeGroup)
            );
            dialog.close();
        });

        content.getChildren().addAll(
                dateLabel, dateBox,
                amountLabel, amountBox,
                typeLabel, typeBox,
                applyButton
        );

        Scene dialogScene = new Scene(content, 400, 350);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private String getSelectedType(ToggleGroup group) {
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        return selected.getText();
    }

    private void filterTransactions(LocalDate startDate, LocalDate endDate,
                                  String minAmount, String maxAmount, String type) {
        ObservableList<Transaction> filteredList = FXCollections.observableArrayList();
        
        // 转换金额范围
        double min = minAmount.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minAmount);
        double max = maxAmount.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxAmount);

        for (Transaction t : transactions) {
            LocalDate transDate = LocalDate.parse(t.getDate());
            double amount = Double.parseDouble(t.getAmount());
            
            boolean dateInRange = (startDate == null || !transDate.isBefore(startDate)) &&
                                (endDate == null || !transDate.isAfter(endDate));
            boolean amountInRange = amount >= min && amount <= max;
            boolean typeMatches = type.equals("All") || t.getType().equals(type);

            if (dateInRange && amountInRange && typeMatches) {
                filteredList.add(t);
            }
        }

        // 更新表格和图表
        transactions = filteredList;
        updateStatsAndChart();
    }

    private void showAddTransactionDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Transaction");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 5;" +
                        "-fx-pref-width: 200;"
        );
        datePicker.setPromptText("Select Date");

        TextField nameField = new TextField();
        nameField.setPromptText("Transaction Name");
        nameField.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 5;" +
                        "-fx-pref-width: 200;"
        );

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 5;" +
                        "-fx-pref-width: 200;"
        );

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton onlineRadio = new RadioButton("Online");
        RadioButton offlineRadio = new RadioButton("Offline");
        onlineRadio.setToggleGroup(typeGroup);
        offlineRadio.setToggleGroup(typeGroup);
        onlineRadio.setSelected(true);
        onlineRadio.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");
        offlineRadio.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");

        HBox typeBox = new HBox(20, onlineRadio, offlineRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        Button submitButton = new Button("Submit");
        submitButton.setStyle(
                "-fx-background-color: #cab6f4;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );

        submitButton.setOnAction(e -> {
            String date = datePicker.getValue() != null ? datePicker.getValue().format(dateFormatter) : "";
            String name = nameField.getText();
            String amount = amountField.getText();
            String type = onlineRadio.isSelected() ? "Online" : "Offline";
            updateStatsAndChart();

            if (!date.isEmpty() && !name.isEmpty() && !amount.isEmpty()) {
                transactions.add(new Transaction(date, name, amount, type));
                updateTypeChart();
                dialog.close();
            }
        });

        VBox dialogContent = new VBox(15, datePicker, nameField, amountField, typeBox, submitButton);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Scene dialogScene = new Scene(dialogContent, 300, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void setupTypeChart() {
        typeChart = new PieChart();
        typeChart.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-title-fill: #4a148c;" +
                        "-fx-font-weight: bold;" +
                        "-fx-pie-label-visible: true;" +
                        "-fx-pie-label-line-length: 20;" +
                        "-fx-pie-label-line-color: #cab6f4;" +
                        "-fx-pie-label-line-style: solid;" +
                        "-fx-pie-label-line-width: 2;" +
                        "-fx-background-color: transparent;"
        );
        typeChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        typeChart.setLabelsVisible(true);
        typeChart.setPrefSize(400, 400);
        typeChart.setMinSize(400, 400);
        typeChart.setMaxSize(400, 400);

        // Add animation to the chart
        typeChart.setAnimated(true);

        updateTypeChart();
    }

    private void updateTypeChart() {
        long onlineCount = transactions.stream().filter(t -> t.getType().equals("Online")).count();
        long offlineCount = transactions.stream().filter(t -> t.getType().equals("Offline")).count();
        long totalCount = onlineCount + offlineCount;

        double onlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Online"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double offlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Offline"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double totalAmount = onlineAmount + offlineAmount;

        typeChart.getData().clear();
        if (onlineCount > 0) {
            PieChart.Data onlineData = new PieChart.Data("Online", onlineCount);
            typeChart.getData().add(onlineData);
            Node node = onlineData.getNode();

            // Smoother hover effect
            node.setOnMouseEntered(e -> {
                node.setScaleX(1.05);
                node.setScaleY(1.05);
                node.setStyle("-fx-pie-color: #cab6f4;");

                double percentage = (onlineCount * 100.0) / totalCount;
                Tooltip tooltip = new Tooltip(String.format(
                        "Online Transactions\n" +
                        "Count: %d (%.1f%%)\n" +
                        "Total Amount: ¥%.2f (%.1f%%)\n" +
                        "Average Amount: ¥%.2f",
                        onlineCount,
                        percentage,
                        onlineAmount,
                        (onlineAmount * 100.0) / totalAmount,
                        onlineAmount/onlineCount
                ));
                tooltip.setStyle(
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-background-color: white;" +
                        "-fx-text-fill: #4a4a4a;" +
                        "-fx-padding: 10px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.5, 0, 2);"
                );
                Tooltip.install(node, tooltip);
            });

            node.setOnMouseExited(e -> {
                node.setScaleX(1.0);
                node.setScaleY(1.0);
                node.setStyle("-fx-pie-color: #cab6f4;");
            });
        }

        if (offlineCount > 0) {
            PieChart.Data offlineData = new PieChart.Data("Offline", offlineCount);
            typeChart.getData().add(offlineData);
            Node node = offlineData.getNode();

            // Smoother hover effect
            node.setOnMouseEntered(e -> {
                node.setScaleX(1.05);
                node.setScaleY(1.05);
                node.setStyle("-fx-pie-color: #a0a0a0;");

                double percentage = (offlineCount * 100.0) / totalCount;
                Tooltip tooltip = new Tooltip(String.format(
                        "Offline Transactions\n" +
                        "Count: %d (%.1f%%)\n" +
                        "Total Amount: ¥%.2f (%.1f%%)\n" +
                        "Average Amount: ¥%.2f",
                        offlineCount,
                        percentage,
                        offlineAmount,
                        (offlineAmount * 100.0) / totalAmount,
                        offlineAmount/offlineCount
                ));
                tooltip.setStyle(
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-background-color: white;" +
                        "-fx-text-fill: #4a4a4a;" +
                        "-fx-padding: 10px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0.5, 0, 2);"
                );
                Tooltip.install(node, tooltip);
            });

            node.setOnMouseExited(e -> {
                node.setScaleX(1.0);
                node.setScaleY(1.0);
                node.setStyle("-fx-pie-color: #a0a0a0;");
            });
        }

        // Set consistent colors for pie chart segments
        typeChart.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                "-fx-pie-color: #cab6f4, #a0a0a0;" +  // 固定紫色和灰色
                "-fx-background-color: transparent;"
        );
    }

    private HBox createMainContent(VBox leftPanel, VBox rightPanel) {
        HBox content = new HBox(20);
        content.setPadding(new Insets(0));  // 移除所有内边距
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(leftPanel, rightPanel);

        // 设置面板在HBox中的对齐方式
        HBox.setHgrow(leftPanel, Priority.NEVER);
        HBox.setHgrow(rightPanel, Priority.NEVER);

        return content;
    }

    private VBox createMainLayout(HBox content) {
        Label pageTitle = new Label("Transaction Management");
        pageTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 32));
        pageTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("Manage your transaction records, support categorized viewing and visual display");
        subtitle.setFont(Font.font("Microsoft YaHei", 16));
        subtitle.setTextFill(Color.WHITE);

        VBox titleBox = new VBox(5, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: #e6d5ff;");
        titleBox.setPadding(new Insets(30, 0, 30, 0));

        // 添加内容区域的包装器，确保内容居中且没有额外边距
        VBox contentWrapper = new VBox(content);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(20, 0, 20, 0));  // 只保留上下边距
        contentWrapper.setStyle("-fx-background-color: #f8f0ff;");

        VBox mainLayout = new VBox(0);
        mainLayout.getChildren().addAll(titleBox, contentWrapper);
        mainLayout.setStyle("-fx-background-color: #f8f0ff;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        return mainLayout;
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