//package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;

public class NutlletEnterprise extends Application {
    // 颜色定义
    private static final Color PRIMARY_COLOR = Color.rgb(202, 182, 244);
    private static final Color SUCCESS_COLOR = Color.rgb(99, 176, 6);
    private static final Color TITLE_COLOR = Color.rgb(102, 0, 153);  // 深紫色

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createBottomNav(primaryStage));
        root.setRight(createSidebar());  // 添加右侧栏

        // 包裹主布局的滚动容器
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // 设置场景大小为 1366x768
        Scene scene = new Scene(scrollPane, 1366, 768);

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

        header.getChildren().addAll(title, balance);
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

        rightPanel.getChildren().addAll(recentTransactions, transactionList);

        splitPane.getItems().addAll(leftPanel, rightPanel);
        return splitPane;
    }

    private HBox createBottomNav(Stage primaryStage) {
        HBox navBar = new HBox();
        navBar.setSpacing(0);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPrefHeight(80);
        navBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeBtn = createNavButtonWithEmoji("Home", "🏠"); // 🏠
        Button discoverBtn = createNavButtonWithEmoji("Discover", "🔍"); // 🔍
        Button settingsBtn = createNavButtonWithEmoji("Settings", "⚙"); // ⚙

        homeBtn.setOnAction(e -> {
            try {
                new Nutllet().start(new Stage()); // 导航到 Nutllet.java
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        discoverBtn.setOnAction(e -> {
            try {
                new Discover().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        settingsBtn.setOnAction(e -> {
            try {
                new Settings().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);
        return navBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // 获取屏幕宽度的 1/3
        double screenWidth = 1366; // 假设屏幕宽度为 1366px
        double columnWidth = screenWidth / 3.0;

        // Tax Forecasting Module
        Label title1 = new Label("Tax Forecasting");
        title1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title1.setTextFill(TITLE_COLOR);

        Label text1 = new Label("Based on a company's financial data and historical tax records to forecast its future potential tax liabilities.");
        text1.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text1.setTextFill(Color.BLACK);
        text1.setWrapText(true);

        VBox card1 = new VBox(10, title1, text1);
        card1.setPadding(new Insets(20));
        card1.setPrefWidth(columnWidth);
        card1.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card1.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        // Financial Recommendations Module
        Label title2 = new Label("Financial Recommendations for Enterprises");
        title2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title2.setTextFill(TITLE_COLOR);

        Label text2 = new Label("Based on the company's income and expenditure, the fee for application documents accounts for 40% (2000/5000) of the total expenditure. "
                + "It is recommended to optimize the application process and reduce unnecessary costs for application materials. "
                + "At the same time, tax expenditures account for 6.6% (330/5000) of the total expenditure. "
                + "It is suggested to plan taxes reasonably and make full use of tax preferential policies.");
        text2.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text2.setTextFill(Color.BLACK);
        text2.setWrapText(true);

        VBox card2 = new VBox(10, title2, text2);
        card2.setPadding(new Insets(20));
        card2.setPrefWidth(columnWidth);
        card2.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card2.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        // AI Industry Analysis Module
        Label title3 = new Label("Al Industry Analysis");
        title3.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title3.setTextFill(TITLE_COLOR);

        Label text3 = new Label("Al will analyze market-related situations and trends for you and provide reasonable business-related recommendations.");
        text3.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text3.setTextFill(Color.BLACK);
        text3.setWrapText(true);

        Button askNowButton = new Button("Ask Now!");
        stylePrimaryButton(askNowButton); // 使用与“View More Details”按钮相同的样式
        askNowButton.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://chat.deepseek.com"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox card3 = new VBox(10, title3, text3, askNowButton);
        card3.setPadding(new Insets(20));
        card3.setPrefWidth(columnWidth);
        card3.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card3.setStyle("-fx-box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);");

        // Adding all cards to the sidebar
        sidebar.getChildren().addAll(card1, card2, card3);
        return sidebar;
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + "; -fx-background-color: rgba(255, 255, 255, 0.1);"
                + "-fx-padding: 8px 16px; -fx-border-radius: 20px; -fx-border-color: " + toHexString(PRIMARY_COLOR) + ";"
                + "-fx-background-radius: 20px; -fx-cursor: pointer;");
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
        button.setPrefWidth(1366 / 3.0);
        button.setPrefHeight(80);
        button.setGraphic(btnContainer);
        button.setStyle(
                "-fx-background-color: white; -fx-border-color: transparent; -fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #f5f5f5; -fx-border-color: transparent;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white; -fx-border-color: transparent;"
        ));

        return button;
    }

    public static void main(String[] args) {
        launch(args);
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

            PieChart chart = new PieChart();
            chart.getData().addAll(
                    new PieChart.Data("Revenue", 70), // 更新为“Revenue”
                    new PieChart.Data("Expenditure", 30) // 更新为“Expenditure”
            );

            // "View More Details" 按钮导航到 Intelligent_Transaction_Classifier.java
            Button viewMoreDetailsButton = new Button("VIEW MORE DETAILS");
            stylePrimaryButton(viewMoreDetailsButton);
            viewMoreDetailsButton.setOnAction(e -> {
                try {
                    // 获取当前窗口
                    Stage currentStage = (Stage) viewMoreDetailsButton.getScene().getWindow();
                    // 创建并显示新窗口
                    new Intelligent_Transaction_Classifier().start(new Stage());
                    // 关闭当前窗口
                    currentStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

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

            PieChart chart = new PieChart();
            chart.getData().addAll(
                    new PieChart.Data("Personal", 70),
                    new PieChart.Data("Corporate", 30)
            );

            getChildren().addAll(title, chart);
        }
    }
}