//package myapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.time.LocalDate;

public class UI_1 extends Application {

    @Override
    public void start(Stage primaryStage) {

        BorderPane mainLayout = new BorderPane();

        // 创建主布局
        GridPane root = new GridPane();
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(20));

        // 标题
        Label titleLabel = new Label("Localized Budgeting");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.PURPLE);
        titleLabel.setAlignment(Pos.CENTER);
        root.add(titleLabel, 0, 0, 2, 1);

        // 左上角部分框
        VBox leftTopBox = new VBox(10);
        leftTopBox.setPadding(new Insets(10));
        leftTopBox.setStyle("-fx-background-color: #FFF0F5; -fx-border-color: #FFC0CB; -fx-border-width: 2px;");

        // 节日选择部分
        Label festivalLabel = new Label("Festival Selection *");
        festivalLabel.setTextFill(Color.PURPLE);

        ComboBox<String> festivalComboBox = new ComboBox<>();
        festivalComboBox.getItems().addAll("Spring Festival", "Dragon Boat Festival",
                "Mid-Autumn Festival", "Christmas",
                "Harvest Day", "Others");
        festivalComboBox.setPrefWidth(200);
        festivalComboBox.setEditable(true); // 设置为可编辑

        // 当选择"Others"时，允许用户输入
        festivalComboBox.setOnAction(event -> {
            if (festivalComboBox.getValue().equals("Others")) {
                festivalComboBox.setEditable(true);
                festivalComboBox.requestFocus();
            } else {
                festivalComboBox.setEditable(false);
            }
        });

        Label festivalNote = new Label("Choose the festival and set your preferred budget");
        festivalNote.setFont(Font.font("Arial", 12));
        festivalNote.setTextFill(Color.GRAY);

        leftTopBox.getChildren().addAll(festivalLabel, festivalComboBox, festivalNote);

        // 节日日期区间部分
        Label dateRangeLabel = new Label("Festival Date Range *");
        dateRangeLabel.setTextFill(Color.PURPLE);

        HBox dateRangeBox = new HBox(10);

        Label startDateLabel = new Label("Start Date:");
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPrefWidth(150);

        Label endDateLabel = new Label("End Date:");
        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPrefWidth(150);

        dateRangeBox.getChildren().addAll(startDateLabel, startDatePicker, endDateLabel, endDatePicker);

        Label dateRangeNote = new Label("Choose the time range you will receive the budget");
        dateRangeNote.setFont(Font.font("Arial", 12));
        dateRangeNote.setTextFill(Color.GRAY);

        leftTopBox.getChildren().addAll(dateRangeLabel, dateRangeBox, dateRangeNote);

        // 收入和支出部分
        HBox amountBox = new HBox(10);

        // 收入部分
        Label incomeLabel = new Label("Income");
        incomeLabel.setTextFill(Color.PURPLE);

        TextField incomeField = new TextField("0");
        incomeField.setPrefWidth(150);

        // 支出部分
        Label expensesLabel = new Label("Expenses");
        expensesLabel.setTextFill(Color.PURPLE);

        TextField expensesField = new TextField("0");
        expensesField.setPrefWidth(150);

        amountBox.getChildren().addAll(incomeLabel, incomeField, expensesLabel, expensesField);

        Label amountNote = new Label("Enter the amount value");
        amountNote.setFont(Font.font("Arial", 12));
        amountNote.setTextFill(Color.GRAY);

        leftTopBox.getChildren().addAll(amountBox, amountNote);

        // 备注部分
        Label notesLabel = new Label("Notes");
        notesLabel.setTextFill(Color.PURPLE);

        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(5);
        notesArea.setPrefColumnCount(30);

        leftTopBox.getChildren().addAll(notesLabel, notesArea);

        // 富文本编辑器工具栏
        HBox toolbar = new HBox(5);
        toolbar.setAlignment(Pos.CENTER);

        // 添加一些工具按钮
        Button boldBtn = new Button("B");
        Button italicBtn = new Button("I");
        Button underlineBtn = new Button("U");
        Button alignLeftBtn = new Button("Left");
        Button alignCenterBtn = new Button("Center");
        Button alignRightBtn = new Button("Right");
        Button listBtn = new Button("List");

        toolbar.getChildren().addAll(boldBtn, italicBtn, underlineBtn,
                alignLeftBtn, alignCenterBtn, alignRightBtn, listBtn);

        leftTopBox.getChildren().add(toolbar);

        // 右上角数据展示部分
        VBox dataDisplayBox = new VBox(10);
        dataDisplayBox.setPadding(new Insets(10));

        // 添加数据展示
        Label dataTitle = new Label("Budget Data");
        dataTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        dataTitle.setTextFill(Color.PURPLE);

        TableView<BudgetData> tableView = new TableView<>();
        TableColumn<BudgetData, String> festivalColumn = new TableColumn<>("Festival");
        festivalColumn.setCellValueFactory(cellData -> cellData.getValue().festivalProperty());

        TableColumn<BudgetData, Number> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());

        TableColumn<BudgetData, Number> ratioColumn = new TableColumn<>("Ratio");
        ratioColumn.setCellValueFactory(cellData -> cellData.getValue().ratioProperty());

        tableView.getColumns().addAll(festivalColumn, amountColumn, ratioColumn);

        // 添加数据
        tableView.getItems().addAll(
                new BudgetData("Spring Festival", 3000, 1900),
                new BudgetData("Dragon Boat Festival", 500, 800),
                new BudgetData("Mid-Autumn Festival", 700, 750),
                new BudgetData("Christmas", 1000, 700),
                new BudgetData("Harvest Day", 500, 800)
        );

        dataDisplayBox.getChildren().addAll(dataTitle, tableView);

        // 图表部分
        HBox chartsBox = new HBox(10);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.setPadding(new Insets(20));

        // 柱状图 - 所有节日的金额
        CategoryAxis xAxis1 = new CategoryAxis();
        NumberAxis yAxis1 = new NumberAxis();
        xAxis1.setLabel("Festivals");
        yAxis1.setLabel("Amount");

        BarChart<String, Number> barChart1 = new BarChart<>(xAxis1, yAxis1);
        barChart1.setTitle("The amount of all the festivals");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Budget");
        series1.getData().add(new XYChart.Data("Spring Festival", 3000));
        series1.getData().add(new XYChart.Data("Dragon Boat Festival", 500));
        series1.getData().add(new XYChart.Data("Mid-Autumn Festival", 700));
        series1.getData().add(new XYChart.Data("Christmas", 1000));
        series1.getData().add(new XYChart.Data("Harvest Day", 500));

        barChart1.getData().add(series1);

        // 横向柱状图 - 每个节日的比例
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        xAxis2.setLabel("Amount");
        yAxis2.setLabel("Festivals");

        BarChart<String, Number> barChart2 = new BarChart<>(xAxis2, yAxis2);
        barChart2.setTitle("The ratio of every festival");
        barChart2.setCategoryGap(10);

        XYChart.Series series2_1 = new XYChart.Series();
        series2_1.setName("Budget");
        series2_1.getData().add(new XYChart.Data("Spring Festival", 3000));
        series2_1.getData().add(new XYChart.Data("Dragon Boat Festival", 500));
        series2_1.getData().add(new XYChart.Data("Mid-Autumn Festival", 700));
        series2_1.getData().add(new XYChart.Data("Christmas", 1000));
        series2_1.getData().add(new XYChart.Data("Harvest Day", 500));

        XYChart.Series series2_2 = new XYChart.Series();
        series2_2.setName("Ratio");
        series2_2.getData().add(new XYChart.Data("Spring Festival", 1900));
        series2_2.getData().add(new XYChart.Data("Dragon Boat Festival", 800));
        series2_2.getData().add(new XYChart.Data("Mid-Autumn Festival", 750));
        series2_2.getData().add(new XYChart.Data("Christmas", 700));
        series2_2.getData().add(new XYChart.Data("Harvest Day", 800));

        barChart2.getData().addAll(series2_1, series2_2);

        chartsBox.getChildren().addAll(barChart1, barChart2);

        // 按钮部分
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));

        Button clearAllBtn = new Button("Clear all");
        clearAllBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button confirmBtn = new Button("Confirm");
        confirmBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");

        buttonBox.getChildren().addAll(clearAllBtn, confirmBtn);

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

        // 组合所有部分
        root.add(leftTopBox, 0, 1);
        root.add(dataDisplayBox, 1, 1);
        root.add(chartsBox, 0, 2, 2, 1);

        // 设置组件高度
        leftTopBox.setPrefHeight(400);
        dataDisplayBox.setPrefHeight(400);
        chartsBox.setPrefHeight(350);

        // ScrollPane 包裹 GridPane
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:transparent;");
        mainLayout.setCenter(scrollPane);

        // 设置底部导航栏
        mainLayout.setBottom(navBar);

        // 设置背景颜色
        root.setStyle("-fx-background-color: #FFF0F5;");

        // 设置场景
        Scene scene = new Scene(mainLayout, 1366, 768);

        // 设置舞台
        primaryStage.setTitle("Localized Budgeting");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
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

    // 数据模型类
    public static class BudgetData {
        private final javafx.beans.property.SimpleStringProperty festival;
        private final javafx.beans.property.SimpleIntegerProperty amount;
        private final javafx.beans.property.SimpleIntegerProperty ratio;

        public BudgetData(String festival, int amount, int ratio) {
            this.festival = new javafx.beans.property.SimpleStringProperty(festival);
            this.amount = new javafx.beans.property.SimpleIntegerProperty(amount);
            this.ratio = new javafx.beans.property.SimpleIntegerProperty(ratio);
        }

        public javafx.beans.property.StringProperty festivalProperty() {
            return festival;
        }

        public javafx.beans.property.IntegerProperty amountProperty() {
            return amount;
        }

        public javafx.beans.property.IntegerProperty ratioProperty() {
            return ratio;
        }
    }
}