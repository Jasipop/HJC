package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class NutlletEnterprise extends Application {
    // 颜色定义
    private static final Color PRIMARY_COLOR = Color.rgb(202, 182, 244);
    private static final Color BACKGROUND_COLOR = Color.rgb(255, 212, 236, 0.33);
    private static final Color SUCCESS_COLOR = Color.rgb(99, 176, 6);
    private static final Color TEXT_SECONDARY = Color.rgb(108, 117, 125);
    private static final Color TITLE_COLOR = Color.rgb(102, 0, 153);  // 深紫色

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createBottomNav());
        root.setRight(createSidebar());  // 添加右侧栏

        Scene scene = new Scene(root, 1600, 800);  // 增加宽度
        // Remove the line that loads the stylesheet
        // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setTitle("Intelligent Financial Assistant");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Intelligent Financial Assistant");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label balance = new Label("Current Balance: ¥4,765.00");
        balance.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        balance.setTextFill(Color.WHITE);
        balance.setPadding(new Insets(0, 0, 0, 20));

        Button currentMonthButton = new Button("Current Month / All");
        Button syncingButton = new Button("Syncing...");
        styleControlButton(currentMonthButton);
        styleControlButton(syncingButton);

        HBox rightPanel = new HBox(10, currentMonthButton, syncingButton);
        rightPanel.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(title, balance, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        return header;
    }

    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        RevenueExpenditureCard revenueExpenditureCard = new RevenueExpenditureCard();
        PersonalCorporateExpenditureCard personalCorporateExpenditureCard = new PersonalCorporateExpenditureCard();

        leftPanel.getChildren().addAll(revenueExpenditureCard, personalCorporateExpenditureCard);

        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(20, 20, 20, 0));
        rightPanel.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label recentTransactions = new Label("Recent Income or Expenditure");
        recentTransactions.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        recentTransactions.setTextFill(PRIMARY_COLOR);

        ListView<String> transactionList = new ListView<>();
        transactionList.setItems(getTransactionItems());
        transactionList.setCellFactory(lv -> new TransactionCell());

        rightPanel.getChildren().addAll(recentTransactions, transactionList);

        splitPane.getItems().addAll(leftPanel, rightPanel);
        return splitPane;
    }

    private class RevenueExpenditureCard extends VBox {
        public RevenueExpenditureCard() {
            setSpacing(10);
            setPadding(new Insets(20));
            setBackground(new Background(new BackgroundFill(
                    Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
            setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

            Label title = new Label("Enterprise Revenue & Expenditure");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            title.setTextFill(PRIMARY_COLOR);

            PieChart chart = createPieChart();

            Button viewMoreDetailsButton = new Button("VIEW MORE DETAILS");
            stylePrimaryButton(viewMoreDetailsButton);

            getChildren().addAll(title, chart, viewMoreDetailsButton);
        }
    }

    private class PersonalCorporateExpenditureCard extends VBox {
        public PersonalCorporateExpenditureCard() {
            setSpacing(10);
            setPadding(new Insets(20));
            setBackground(new Background(new BackgroundFill(
                    Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
            setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

            Label title = new Label("Personal VS Corporate Expenditures");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            title.setTextFill(PRIMARY_COLOR);

            PieChart chart = createPieChart();

            Button modifyExpenditureButton = new Button("Modify Your Expenditure Classification");
            stylePrimaryButton(modifyExpenditureButton);

            getChildren().addAll(title, chart, modifyExpenditureButton);
        }
    }

    private PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.getData().addAll(
                new PieChart.Data("Catering", 35),
                new PieChart.Data("Transportation", 200),
                new PieChart.Data("Living", 0),
                new PieChart.Data("Entertainment", 0),
                new PieChart.Data("Others", 0)
        );
        chart.setLegendVisible(false);
        return chart;
    }

    private HBox createBottomNav() {
        HBox bottomNav = new HBox(10);
        bottomNav.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        bottomNav.setPadding(new Insets(10));
        bottomNav.setAlignment(Pos.CENTER);
        bottomNav.setStyle("-fx-box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);");

        Button homeButton = new Button("HOME");
        Button discoverButton = new Button("DISCOVER");
        Button settingsButton = new Button("SETTINGS");

        bottomNav.getChildren().addAll(homeButton, discoverButton, settingsButton);
        return bottomNav;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title1 = new Label("Tax Forecasting");
        title1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title1.setTextFill(TITLE_COLOR);

        Label text1 = new Label("Based on a company's financial data and historical tax records\n to forecast its future potential tax liabilities.");
        text1.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text1.setTextFill(Color.BLACK);
        text1.setWrapText(true);

        VBox card1 = new VBox(10, title1, text1);
        card1.setPadding(new Insets(20));
        card1.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card1.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        Label title2 = new Label("Financial Recommendations for Enterprises");
        title2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title2.setTextFill(TITLE_COLOR);

        Label text2 = new Label("Based on the company's income and expenditure,\n the fee for application documents accounts for 40% (2000/5000)\n of the total expenditure. It is recommended to optimize the application\n process and reduce unnecessary costs for application materials.\n At the same time, tax expenditures account for 6.6% (330/5000) of\n the total expenditure. It is suggested to plan taxes reasonably and make\n full use of tax preferential policies.");
        text2.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text2.setTextFill(Color.BLACK);
        text2.setWrapText(true);

        VBox card2 = new VBox(10, title2, text2);
        card2.setPadding(new Insets(20));
        card2.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card2.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        Label title3 = new Label("AI Industry Analysis");
        title3.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title3.setTextFill(TITLE_COLOR);

        Label text3 = new Label("AI will analyze market-related situations and trends for you and\n provide reasonable business-related recommendations.");
        text3.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text3.setTextFill(Color.BLACK);
        text3.setWrapText(true);

        VBox card3 = new VBox(10, title3, text3);
        card3.setPadding(new Insets(20));
        card3.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card3.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        sidebar.getChildren().addAll(card1, card2, card3);
        return sidebar;
    }

    private void styleControlButton(Button button) {
        button.setStyle("-fx-text-fill: white; -fx-background-color: rgba(255, 255, 255, 0.1);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: rgba(255, 255, 255, 0.3);"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: white; -fx-background-color: rgba(255, 255, 255, 0.2);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: rgba(255, 255, 255, 0.3);"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: white; -fx-background-color: rgba(255, 255, 255, 0.1);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: rgba(255, 255, 255, 0.3);"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;"));
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + "; -fx-background-color: rgba(255, 255, 255, 0.1);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + "; -fx-background-color: rgba(255, 255, 255, 0.2);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + "; -fx-background-color: rgba(255, 255, 255, 0.1);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;"));
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private javafx.collections.ObservableList<String> getTransactionItems() {
        return javafx.collections.FXCollections.observableArrayList(
                "2023-07-05 - Fee for Application Documents - -¥2000",
                "2023-07-04 - Profit - +¥5000",
                "2023-07-04 - Tax - -¥330"
        );
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

                // 创建时间分类标签
                Label date = new Label(parts[0]);
                date.setStyle("-fx-text-fill: #666;");

                // 创建描述标签
                Label description = new Label(parts[1]);
                description.setStyle("-fx-text-fill: " +
                        (getIndex() % 2 == 0 ? "#007bff" : PRIMARY_COLOR.toString().replace("0x", "#")) + ";");

                // 创建金额标签
                Label amount = new Label(parts[2]);
                amount.setStyle("-fx-text-fill: " +
                        (amount.getText().startsWith("+") ? SUCCESS_COLOR.toString().replace("0x", "#") : "#dc3545") +
                        "; -fx-font-weight: 600;");

                // 创建水平布局容器
                Region spacer = new Region();
                HBox hbox = new HBox(date, spacer, description, amount);
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // 创建垂直布局容器
                VBox vbox = new VBox(5, hbox);
                vbox.setBackground(new Background(new BackgroundFill(
                        getIndex() % 2 == 0 ? Color.rgb(250, 250, 250) : Color.WHITE,
                        CornerRadii.EMPTY, Insets.EMPTY)));
                vbox.setPadding(new Insets(5, 10, 5, 10));

                setGraphic(vbox);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}