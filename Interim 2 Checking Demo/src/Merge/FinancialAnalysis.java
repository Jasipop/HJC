import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.*;
import java.net.URI;

public class FinancialAnalysis extends Application {

    private final int[] years = {2014,2015,2016,2017,2018,2019, 2020, 2021, 2022, 2023, 2024};
    private final int[] eventCounts = {154,130,113,78,120,105,53, 29, 42, 34, 69};
    private final double[] totalAmounts = {756.24,457.89,345.21,145.68,256.89,568.66, 253.7, 581.82, 164.8, 36.4, 132};
    private final double[] singleAmounts = {309,270,233,178, 116.4,67.9, 20.06, 100,34.8, 15.7, 0};

    private final String lineColor = "#855FAF";
    private final String barColor = "#855FAF";
    private final String[] pieColors = {"#855FAF", "#CEA3ED", "#7D4B79", "#F05865", "#36344C"};
    private final String backgroundColor = "#FFD4EC54";

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        VBox mainContainer = new VBox();
        mainContainer.setPadding(new Insets(20));
        mainContainer.setSpacing(20);
        mainContainer.setStyle("-fx-background-color: " + backgroundColor + ";");

        Text title = new Text("Financial Analysis Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        title.setOpacity(0);
        title.setScaleX(0.5);
        title.setScaleY(0.5);

        ParallelTransition titleAnimation = new ParallelTransition(
                new FadeTransition(Duration.seconds(1), title),
                new ScaleTransition(Duration.seconds(1), title)
        );
        ((FadeTransition)titleAnimation.getChildren().get(0)).setToValue(1);
        ((ScaleTransition)titleAnimation.getChildren().get(1)).setToX(1);
        ((ScaleTransition)titleAnimation.getChildren().get(1)).setToY(1);

        LineChart<Number, Number> lineChart = buildLineChart();
        BarChart<String, Number> barChart = buildBarChart();
        PieChart pieChart = buildPieChart();

        Label pieTitle = new Label("Event rating");
        pieTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pieTitle.setTextFill(Color.web("#855FAF"));

        Label textTitle = new Label("AI Financial Analysis");
        textTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        textTitle.setTextFill(Color.web("#855FAF"));

        Label analysis = new Label(
                "The express delivery industry in China has witnessed fluctuating financial activity between 2019 and 2024. " +
                        "While the number of financing events peaked in 2019, the highest total financing amount occurred in 2021, " +
                        "suggesting a shift from frequent smaller investments to fewer but more strategic funding initiatives. " +
                        "This trend indicates market consolidation and a focus on technological innovation, automation, and last-mile delivery optimization. " +
                        "The decline in single financing amount in 2023 reflects tighter capital flows, possibly due to macroeconomic uncertainties. " +
                        "Overall, the financial data stream illustrates a maturing industry adapting to digital transformation and competitive pressures."
        );
        analysis.setWrapText(true);
        analysis.setFont(Font.font("Arial", 20));
        analysis.setTextFill(Color.web("#855FAF"));
        analysis.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 5; -fx-padding: 10; -fx-border-insets: 10;");

        Button aiButton = new Button("Ask More");
        aiButton.setStyle("-fx-background-color: " + lineColor + "; -fx-text-fill: white; -fx-font-size: 16px;");
        aiButton.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://chat.deepseek.com/"));
            } catch (Exception ex) {
                System.out.println("Fail to open the web: " + ex.getMessage());
            }
        });

        // 为AI按钮添加悬停动画
        ScaleTransition scaleBtn = new ScaleTransition(Duration.millis(200), aiButton);
        aiButton.setOnMouseEntered(e -> {
            scaleBtn.setToX(1.05);
            scaleBtn.setToY(1.05);
            scaleBtn.play();
        });
        aiButton.setOnMouseExited(e -> {
            scaleBtn.setToX(1.0);
            scaleBtn.setToY(1.0);
            scaleBtn.play();
        });

        VBox contentContainer = new VBox(30);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setStyle("-fx-background-color: " + backgroundColor + ";");
        contentContainer.setOpacity(0);

        contentContainer.getChildren().addAll(title, lineChart, barChart, pieTitle, pieChart, textTitle, analysis, aiButton);

        // 页面淡入动画
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), contentContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // 图表逐个出现的动画
        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Node node : contentContainer.getChildren()) {
            if (node instanceof Chart || node instanceof Label || node instanceof Text) {
                node.setOpacity(0);
                node.setTranslateY(20);

                ParallelTransition pt = new ParallelTransition(
                        new FadeTransition(Duration.seconds(0.5), node),
                        new TranslateTransition(Duration.seconds(0.5), node)
                );
                ((FadeTransition)pt.getChildren().get(0)).setFromValue(0);
                ((FadeTransition)pt.getChildren().get(0)).setToValue(1);
                ((TranslateTransition)pt.getChildren().get(1)).setFromY(20);
                ((TranslateTransition)pt.getChildren().get(1)).setToY(0);

                sequentialTransition.getChildren().add(pt);
            }
        }

        // 组合动画
        ParallelTransition allAnimations = new ParallelTransition(fadeIn, sequentialTransition, titleAnimation);
        allAnimations.play();

        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVvalue(0);

        mainContainer.getChildren().add(scrollPane);
        root.setCenter(mainContainer);

        // Bottom Navigation Bar
        HBox bottomNavigationBar = new HBox();
        bottomNavigationBar.setSpacing(0);
        bottomNavigationBar.setAlignment(Pos.CENTER);
        bottomNavigationBar.setPrefHeight(80);
        bottomNavigationBar.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        Button homeButton = createNavButtonWithEmoji("Home", "🏠");
        Button discoverButton = createNavButtonWithEmoji("Discover", "🔍");
        Button settingsButton = createNavButtonWithEmoji("Settings", "⚙");

        homeButton.setOnAction(e -> {
            try { new Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverButton.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsButton.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        bottomNavigationBar.getChildren().addAll(homeButton, discoverButton, settingsButton);
        root.setBottom(bottomNavigationBar);

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Financial Analysis");
        primaryStage.show();
    }

    private LineChart<Number, Number> buildLineChart() {
        NumberAxis xAxis = new NumberAxis(2014, 2024, 1);
        xAxis.setLabel("Year");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Peak Financing Events");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Trend of Peak Financing Events");
        chart.setStyle("-fx-background-color: transparent; -fx-plot-background-color: " + backgroundColor + ";");
        chart.setLegendVisible(false);
        chart.setPrefWidth(1000);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        // 添加图表悬停动画
        ScaleTransition scaleChart = new ScaleTransition(Duration.millis(200), chart);
        chart.setOnMouseEntered(e -> {
            scaleChart.setToX(1.01);
            scaleChart.setToY(1.01);
            scaleChart.play();
        });
        chart.setOnMouseExited(e -> {
            scaleChart.setToX(1.0);
            scaleChart.setToY(1.0);
            scaleChart.play();
        });

        XYChart.Series<Number, Number> dataSeries = new XYChart.Series<>();
        for (int i = 0; i < years.length; i++) {
            XYChart.Data<Number, Number> data = new XYChart.Data<>(years[i], eventCounts[i]);
            dataSeries.getData().add(data);
            Tooltip tooltip = new Tooltip("Year: " + years[i] + "\nEvents: " + eventCounts[i]);
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.getData().add(dataSeries);

        chart.applyCss();
        chart.layout();

        for (XYChart.Data<Number, Number> data : dataSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + lineColor + ", white;");
            }
        }

        Node line = chart.lookup(".chart-series-line");
        if (line != null) {
            line.setStyle("-fx-stroke: " + lineColor + "; -fx-stroke-width: 2px;");
        }

        return chart;
    }

    private BarChart<String, Number> buildBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Financing Amount (billion yuan)");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Peak Financing Amount");
        chart.setStyle("-fx-background-color: transparent; -fx-plot-background-color: " + backgroundColor + ";");
        chart.setLegendVisible(false);
        chart.setCategoryGap(20);
        chart.setBarGap(5);
        chart.setPrefWidth(1000);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        // 添加图表悬停动画
        ScaleTransition scaleChart = new ScaleTransition(Duration.millis(200), chart);
        chart.setOnMouseEntered(e -> {
            scaleChart.setToX(1.01);
            scaleChart.setToY(1.01);
            scaleChart.play();
        });
        chart.setOnMouseExited(e -> {
            scaleChart.setToX(1.0);
            scaleChart.setToY(1.0);
            scaleChart.play();
        });

        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        for (int i = 0; i < years.length; i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(years[i]), totalAmounts[i]);
            dataSeries.getData().add(data);
            Tooltip tooltip = new Tooltip("Year: " + years[i] + "\nAmount: " + totalAmounts[i] + "B");
            Tooltip.install(data.getNode(), tooltip);
        }
        chart.getData().add(dataSeries);

        chart.applyCss();
        chart.layout();

        for (XYChart.Data<String, Number> data : dataSeries.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + barColor + ";");
            }
        }

        return chart;
    }

    private PieChart buildPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Peak Single Financing Amount");
        chart.setStyle("-fx-background-color: " + backgroundColor + ";");
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setClockwise(true);
        chart.setStartAngle(90);
        chart.setPrefWidth(600);

        // 添加图表悬停动画
        ScaleTransition scaleChart = new ScaleTransition(Duration.millis(200), chart);
        chart.setOnMouseEntered(e -> {
            scaleChart.setToX(1.01);
            scaleChart.setToY(1.01);
            scaleChart.play();
        });
        chart.setOnMouseExited(e -> {
            scaleChart.setToX(1.0);
            scaleChart.setToY(1.0);
            scaleChart.play();
        });

        for (int i = 0; i < years.length; i++) {
            double value = singleAmounts[i];
            if (value > 0) {
                PieChart.Data slice = new PieChart.Data(String.valueOf(years[i]), value);
                chart.getData().add(slice);
                Tooltip tooltip = new Tooltip("Year: " + years[i] + "\nSingle Amount: " + value + "B");
                Tooltip.install(slice.getNode(), tooltip);
            }
        }

        chart.applyCss();
        chart.layout();

        for (PieChart.Data data : chart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName() + ": " + (int) data.getPieValue() + "B");
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().setStyle("-fx-pie-color: " + pieColors[chart.getData().indexOf(data) % pieColors.length] + ";");
        }

        return chart;
    }

    private Button createNavButtonWithEmoji(String labelText, String emojiSymbol) {
        VBox buttonContent = new VBox();
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setSpacing(2);

        Label emojiLabel = new Label(emojiSymbol);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label textLabel = new Label(labelText);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        buttonContent.getChildren().addAll(emojiLabel, textLabel);

        Button navigationButton = new Button();
        navigationButton.setPrefWidth(456);
        navigationButton.setPrefHeight(80);
        navigationButton.setGraphic(buttonContent);
        navigationButton.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        // 添加按钮悬停动画
        ScaleTransition scaleNavBtn = new ScaleTransition(Duration.millis(150), navigationButton);
        navigationButton.setOnMouseEntered(e -> {
            scaleNavBtn.setToX(1.03);
            scaleNavBtn.setToY(1.03);
            scaleNavBtn.play();

            emojiLabel.setStyle("-fx-font-size: 18px;"); // 放大emoji
            textLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #855FAF;"); // 改变颜色
        });

        navigationButton.setOnMouseExited(e -> {
            scaleNavBtn.setToX(1.0);
            scaleNavBtn.setToY(1.0);
            scaleNavBtn.play();

            emojiLabel.setStyle("-fx-font-size: 16px;");
            textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        });

        return navigationButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}