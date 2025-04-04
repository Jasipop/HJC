package Nutllet;

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
import java.util.Arrays;

public class Nutllet extends Application {
    // 颜色定义
    private static final Color PRIMARY_PURPLE = Color.rgb(128, 100, 228);
    private static final Color LIGHT_PURPLE_BG = Color.rgb(245, 241, 255);
    private static final Color TEXT_SECONDARY = Color.rgb(102, 102, 102);

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createMainContent());
        root.setBottom(createAdvicePanel());

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        primaryStage.setTitle("NUTLLET - Financial Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        // 左侧品牌区
        HBox leftPanel = new HBox(10);
        Label title = new Label("NUTLLET");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        
        Label edition = new Label("Edition");
        edition.setFont(Font.font("Segoe UI", 12));
        edition.setTextFill(Color.rgb(255, 255, 255, 0.6));
        
        leftPanel.getChildren().addAll(title, edition);
        leftPanel.setAlignment(Pos.CENTER_LEFT);

        // 右侧控制按钮
        HBox rightPanel = new HBox(15);
        String[] buttons = {"QSearch", "Enterprise", "Logout"};
        Arrays.stream(buttons).forEach(btnText -> {
            Button btn = new Button(btnText);
            btn.setStyle(btnText.equals("Enterprise") ? 
                "-fx-text-fill: #8064E4; -fx-background-color: white;" :
                "-fx-text-fill: white; -fx-background-color: #8064E4;");
            rightPanel.getChildren().add(btn);
        });
        rightPanel.setAlignment(Pos.CENTER_RIGHT);

        // 头部容器
        HBox header = new HBox();
        header.setBackground(new Background(new BackgroundFill(
            PRIMARY_PURPLE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setPadding(new Insets(12, 20, 12, 20));
        header.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        return header;
    }

    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.54);
        
        // 左侧面板
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setBackground(new Background(new BackgroundFill(
            Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        Label balance = new Label("Current Balance\n¥ 8888.88");
        balance.setStyle("-fx-text-fill: #333333; -fx-font-size: 24px;");
        
        leftPanel.getChildren().addAll(
            balance, 
            createPieChart(),
            createProgressSection(),
            createButtonPanel()
        );
        
        // 右侧面板
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20, 20, 20, 0));
        rightPanel.setBackground(new Background(new BackgroundFill(
            Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        HBox searchBox = new HBox();
        TextField searchField = new TextField("Search transactions...");
        searchBox.getChildren().addAll(
            new Label("Recent Consumption"), 
            new Region(), 
            searchField
        );
        HBox.setHgrow(searchBox.getChildren().get(1), Priority.ALWAYS);
        
        ListView<String> transactionList = new ListView<>();
        transactionList.setItems(getTransactionItems());
        transactionList.setCellFactory(lv -> new TransactionCell());
        
        rightPanel.getChildren().addAll(searchBox, transactionList);
        
        splitPane.getItems().addAll(leftPanel, rightPanel);
        return splitPane;
    }

    private PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.getData().addAll(
            new PieChart.Data("Catering", 45),
            new PieChart.Data("Living", 25),
            new PieChart.Data("Entertainment", 15),
            new PieChart.Data("Transportation", 10),
            new PieChart.Data("Others", 5)
        );
        chart.setLegendVisible(false);
        return chart;
    }

    private VBox createProgressSection() {
        VBox progressBox = new VBox(10);
        String[] items = {
            "Loan Repayment Reminder:0.2",
            "Month Budget:0.65", 
            "Present Planning:0.3"
        };
        
        Arrays.stream(items).forEach(item -> {
            String[] parts = item.split(":");
            ProgressBar pb = new ProgressBar(Double.parseDouble(parts[1]));
            pb.setStyle("-fx-accent: #8064E4; -fx-background-color: #F5F1FF;");
            
            Label label = new Label(parts[0]);
            label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            
            VBox vbox = new VBox(5, label, pb);
            progressBox.getChildren().add(vbox);
        });
        return progressBox;
    }

    private HBox createButtonPanel() {
        Button modifyBtn = new Button("Modify Your Reminder");
        modifyBtn.setStyle("-fx-text-fill: white; -fx-background-color: #8064E4;");
        
        Button detailsBtn = new Button("More Details");
        detailsBtn.setStyle("-fx-text-fill: #8064E4; -fx-background-color: white;");
        
        return new HBox(10, modifyBtn, detailsBtn);
    }

    private ScrollPane createAdvicePanel() {
        TextArea adviceArea = new TextArea();
        adviceArea.setText("Based on my consumption, give me some advice.\n\n"
            + "Here are some suggestions based on your consumption...");
        adviceArea.setEditable(false);
        adviceArea.setWrapText(true);
        adviceArea.setStyle("-fx-border-color: #E6E6E6; -fx-border-width: 1 0 0 0;");
        
        ScrollPane scrollPane = new ScrollPane(adviceArea);
        scrollPane.setPadding(new Insets(15, 20, 15, 20));
        scrollPane.setBackground(new Background(new BackgroundFill(
            Color.rgb(248, 248, 248), CornerRadii.EMPTY, Insets.EMPTY)));
        return scrollPane;
    }

    private javafx.collections.ObservableList<String> getTransactionItems() {
        return javafx.collections.FXCollections.observableArrayList(
            "8:08 PM • Catering - ¥451 - Feb 20 2015",
            "5:02 PM • Living - ¥200 - Feb 20 2015",
            "4:53 PM • Catering - ¥10 - Feb 20 2015"
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
                Label timeCat = new Label(parts[0]);
                timeCat.setStyle("-fx-text-fill: " + 
                    (getIndex() % 2 == 0 ? "#8064E4" : "#333333") + ";");
                
                // 创建金额标签
                Label amount = new Label(parts[1]);
                
                // 创建日期标签
                Label date = new Label(parts[2]);
                date.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");

                // 创建水平布局容器
                Region spacer = new Region();
                HBox hbox = new HBox(timeCat, spacer, amount);
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // 创建垂直布局容器
                VBox vbox = new VBox(5, hbox, date);
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