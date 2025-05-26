package Merge;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
        String scrollBarCss =
                ".scroll-bar:vertical {" +
                        "    -fx-background-color: transparent;" +
                        "    -fx-pref-width: 8px;" +
                        "}" +
                        ".scroll-bar:vertical .track {" +
                        "    -fx-background-color: transparent;" +
                        "}" +
                        ".scroll-bar:vertical .thumb {" +
                        "    -fx-background-color: rgba(150,150,150,0.5);" +
                        "    -fx-background-radius: 4px;" +
                        "}" +
                        ".scroll-bar:vertical .thumb:hover {" +
                        "    -fx-background-color: rgba(150,150,150,0.7);" +
                        "}" +
                        ".scroll-bar .increment-button, .scroll-bar .decrement-button {" +
                        "    -fx-background-color: transparent;" +
                        "    -fx-padding: 0;" +
                        "}" +
                        ".scroll-bar .increment-arrow, .scroll-bar .decrement-arrow {" +
                        "    -fx-shape: \" \";" +
                        "}";

        scene.getRoot().setStyle(scrollBarCss);
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
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(homeBtn,discoverBtn,settingsBtn  );

        mainLayout.getChildren().add(navBar);
    }

    private void initializeData() {
        transactions = FXCollections.observableArrayList();
        String csvFile = "deals.csv";
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
                    System.err.println("Invalid amount format: " + amount);
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
                        "-fx-background-color: #fff;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-table-header-border-color: transparent;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        table.setPrefHeight(350);
        table.setFixedCellSize(40);
        table.setPrefWidth(560);  // 增加表格宽度

        // Add hover effect to table rows
        table.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
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
        dateCol.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold; -fx-text-fill: #616161;");

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
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 12 30;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 2);"
        );

        addButton.setOnMouseEntered(e ->
                addButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                                "-fx-text-fill: white;" +
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
                        "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                                "-fx-text-fill: white;" +
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

        Platform.runLater(() -> {
            // 仅更新统计面板，不操作图表
            chartContent.getChildren().set(1, newStatsBox);
        });
    }
    private VBox createRightPanel() {
        Label chartTitle = new Label("Transaction Analysis");
        chartTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        chartTitle.setTextFill(Color.web("#6c757d"));

        VBox statsBox = createStatsBox();
        statsBox.setMaxWidth(220);
        statsBox.setPrefWidth(220);

        StackPane chartWrapper = new StackPane(typeChart);
        chartWrapper.setPrefSize(420, 420);
        chartWrapper.setMinSize(420, 420);
        chartWrapper.setMaxSize(420, 420);
        chartWrapper.setAlignment(Pos.CENTER);

        chartContent = new HBox(20);
        chartContent.setAlignment(Pos.CENTER);
        chartContent.getChildren().addAll(chartWrapper, statsBox);

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
        chartBox.setMinWidth(700);
        chartBox.setMaxWidth(700);

        // 创建滚动面板并设置样式
        ScrollPane scrollPane = new ScrollPane(chartBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));

        VBox rightPanel = new VBox(0);
        rightPanel.getChildren().add(scrollPane);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        return rightPanel;
    }

    private VBox createStatsBox() {
        VBox statsBox = new VBox(10);  // 减小间距
        statsBox.setStyle(
                "-fx-padding: 15;" +  // 减小内边距
                        "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
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
                        "-fx-text-fill: white;"
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

    // 修改后的createStatsItemBox方法
    private VBox createStatsItemBox(String title, String value, String iconCode) {
        VBox itemBox = new VBox(8);
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // 使用FontAwesome图标
        Text icon = new Text(iconCode);
        icon.setFont(Font.font("FontAwesome", 20));
        icon.setFill(Color.web("#7b1fa2"));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-text-fill: #757575; -fx-font-size: 14;");

        header.getChildren().addAll(icon, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 18; -fx-text-fill: #212121; -fx-font-weight: bold;");

        // 添加动态效果
        itemBox.setOnMouseEntered(e -> {
            itemBox.setStyle("-fx-background-color: #f3e5f5; -fx-background-radius: 12; -fx-padding: 15;");
            itemBox.setCursor(Cursor.HAND);
            itemBox.setEffect(new DropShadow(10, Color.web("#d1c4e940")));
        });
        itemBox.setOnMouseExited(e -> {
            itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
            itemBox.setEffect(null);
        });

        itemBox.getChildren().addAll(header, valueLabel);
        return itemBox;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button exportButton = new Button("Export Data");
        exportButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
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
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
                        "-fx-text-fill: white;" +
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

    // 修改后的exportData方法（移除了Excel选项）
    private void exportData() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Export Data");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Results cannot be changed");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        Button csvButton = new Button("Export as CSV");

        // 统一按钮样式
        String buttonStyle = "-fx-background-color: linear-gradient(to right, #7b1fa2, #9c27b0);"
                + "-fx-text-fill: white;"
                + "-fx-font-family: 'Microsoft YaHei';"
                + "-fx-font-size: 14px;"
                + "-fx-padding: 12 30;"
                + "-fx-background-radius: 25;"
                + "-fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);";

        csvButton.setStyle(buttonStyle);

        csvButton.setOnAction(e -> {
            exportToCSV();
            dialog.close();
        });


        content.getChildren().addAll(titleLabel, csvButton);

        Scene dialogScene = new Scene(content, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    

    private TableView<Transaction> createPreviewTable() {
        TableView<Transaction> table = new TableView<>();

        // 列定义（复用原有列样式）
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));

        TableColumn<Transaction, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleStringProperty("¥" + cell.getValue().getAmount()));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));

        table.getColumns().addAll(dateCol, nameCol, amountCol, typeCol);
        table.setItems(transactions);
        table.setPrefHeight(300);

        return table;
    }

    // 计算总金额
    private String calculateTotalAmount() {
        double total = transactions.stream()
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();
        return String.format("%.2f", total);
    }
    private void exportToCSV() {
        // 生成CSV数据
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Transaction Name,Amount,Type\n");
        for (Transaction t : transactions) {
            csv.append(String.format("%s,%s,%s,%s\n",
                    t.getDate(),
                    t.getName(),
                    t.getAmount(),
                    t.getType()));
        }
        String csvData = csv.toString();

        // 控制台输出
        System.out.println("Exporting to CSV...");
        System.out.println(csvData);

        // 在UI线程显示弹窗
        Platform.runLater(() -> {
            showExportDataDialog(csvData);
            showSuccessAlert();
        });
    }
    // 显示导出数据弹窗
    private void showExportDataDialog(String csvData) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Exported CSV Data");

        // 图标和标题
        Node csvIcon = new Text("\uD83D\uDCC0"); // 📀
        csvIcon.setStyle("-fx-font-size: 24px;");

        // 文本区域
        TextArea textArea = new TextArea(csvData);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14px;");

        // 滚动面板
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefSize(800, 500);

        // 保存按钮
        Button saveButton = new Button("Save to Local");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setGraphic(new Text("\uD83D\uDCBE")); // 💾 软盘图标
        saveButton.setOnAction(e -> saveToLocal(csvData, dialog));

        // 提示信息
        HBox hintBox = new HBox(10);
        hintBox.setAlignment(Pos.CENTER_RIGHT);
        Text warningIcon = new Text("\u2754"); // ❔
        warningIcon.setStyle("-fx-font-family: 'Segoe UI Symbol'; -fx-font-size: 16px; -fx-fill: #666;");
        Label hintLabel = new Label("Any questions? Try again!");
        hintLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic; -fx-font-size: 12px;");

        // 组合底部控件
        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.getChildren().addAll(hintLabel, warningIcon, saveButton);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(
                new HBox(5, csvIcon, new Label("CSV Export Content:")),
                scrollPane,
                bottomBox
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    private void saveToLocal(String csvData, Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("transactions_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(parentStage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(csvData);
                showSaveSuccessAlert();  // 显示保存成功提示
            } catch (IOException ex) {
                showErrorAlert("Save Failed", "Error saving file: " + ex.getMessage());
            }
        }
    }
    // 显示成功提示
    private void showSuccessAlert() {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.NONE);
        alertStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label("✅ Successfully Exported!");
        label.setStyle("-fx-font-size: 18px;"
                + "-fx-text-fill: #4CAF50;"
                + "-fx-font-weight: bold;"
                + "-fx-background-color: rgba(255,255,255,0.9);"
                + "-fx-padding: 15px 25px;"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // 定位到屏幕中心
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        alertStage.setX((screenBounds.getWidth() - 300) / 2);
        alertStage.setY((screenBounds.getHeight() - 100) / 2);

        alertStage.setScene(scene);
        alertStage.setWidth(300);
        alertStage.setHeight(100);
        alertStage.show();

        // 自动关闭
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2),
                        e -> alertStage.close()
        ));
        timeline.play();
    }
    private void showSaveSuccessAlert() {
        Stage alertStage = new Stage();
        alertStage.initStyle(StageStyle.UTILITY);
        alertStage.initModality(Modality.NONE);

        Label label = new Label("✅ File saved successfully!");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        Scene scene = new Scene(label);
        alertStage.setScene(scene);
        alertStage.setWidth(300);
        alertStage.setHeight(100);
        alertStage.show();

        // 自动关闭
        new Timeline(new KeyFrame(Duration.seconds(2), e -> alertStage.close())).play();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
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
                "-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);" +
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
            String amount = amountField.getText().replaceAll("[^\\d.]", ""); // 新增：清理非法字符
            String type = onlineRadio.isSelected() ? "Online" : "Offline";

            if (!date.isEmpty() && !name.isEmpty() && !amount.isEmpty()) {
                try {
                    // 新增金额有效性验证
                    Double.parseDouble(amount);
                    transactions.add(new Transaction(date, name, amount, type));
                    updateStatsAndChart(); // 调整到正确位置
                    dialog.close();
                } catch (NumberFormatException ex) {
                    // 新增错误提示
                    System.err.println("Invalid amount format: " + amount);
                    amountField.setStyle("-fx-border-color: #ff0000;");
                }
            } else {
                // 新增输入验证提示
                System.err.println("Please fill in all fields");
            }
        });

        VBox dialogContent = new VBox(15, datePicker, nameField, amountField, typeBox, submitButton);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Scene dialogScene = new Scene(dialogContent, 300, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // 修改setupTypeChart方法
    private void setupTypeChart() {
        typeChart = new PieChart();
        typeChart.setLegendSide(Side.BOTTOM);
        typeChart.setLabelsVisible(true);
        typeChart.setClockwise(true);
        typeChart.setStartAngle(90);

        // 设置图表大小
        typeChart.setPrefSize(400, 400);
        typeChart.setMinSize(400, 400);
        typeChart.setMaxSize(400, 400);

        // 包装为 StackPane 居中显示
        StackPane chartWrapper = new StackPane(typeChart);
        chartWrapper.setPrefSize(420, 420);
        chartWrapper.setMinSize(420, 420);
        chartWrapper.setMaxSize(420, 420);
        chartWrapper.setAlignment(Pos.CENTER);

        chartContent = new HBox(20);
        chartContent.setAlignment(Pos.CENTER);
        chartContent.getChildren().addAll(chartWrapper, createStatsBox());

        // 初始填充图表数据
        updateTypeChart();
    }


    private void updateTypeChart() {
        double onlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Online"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double offlineAmount = transactions.stream()
                .filter(t -> t.getType().equals("Offline"))
                .mapToDouble(t -> Double.parseDouble(t.getAmount()))
                .sum();

        double total = onlineAmount + offlineAmount;

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList(
                new PieChart.Data("Online", onlineAmount),
                new PieChart.Data("Offline", offlineAmount)
        );

        Platform.runLater(() -> {
            typeChart.setData(chartData);

            for (PieChart.Data data : chartData) {
                String name = data.getName();
                double amount = data.getPieValue();
                double percent = (total == 0) ? 0 : (amount / total) * 100;

                String tooltipText = String.format(
                        "Category: %s\nAmount: %.2f\nProportion: %.1f%%",
                        name, amount, percent
                );

                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(data.getNode(), tooltip);
            }
        });
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
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 38));
        pageTitle.setTextFill(Color.WHITE);
        pageTitle.setEffect(new DropShadow(15, Color.web("#4c3092")));

        Label subtitle = new Label("Manage your transaction records, support categorized viewing and visual display");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        subtitle.setTextFill(Color.web("#e6d5ff"));


        HBox titleContent = new HBox(15);
        titleContent.setAlignment(Pos.CENTER);
        ImageView logo = new ImageView(new Image("expenses.png")); // 添加插图
        logo.setFitHeight(48);
        logo.setFitWidth(48);
        titleContent.getChildren().addAll(logo, new VBox(5, pageTitle, subtitle));

        VBox titleBox = new VBox(8, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: linear-gradient(to right, #6c5ce7, #8e7dff);");
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