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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.awt.*;
import java.net.URI;

public class FinancialAnalysis extends Application {

    private final int[] years = {2014,2015,2016,2017,2018,2019, 2020, 2021, 2022, 2023, 2024};
    private final int[] peakEvents = {154,130,113,78,120,105,53, 29, 42, 34, 69};
    private final double[] peakAmount = {756.24,457.89,345.21,145.68,256.89,568.66, 253.7, 581.82, 164.8, 36.4, 132};
    private final double[] peakSingleAmount = {309,270,233,178, 116.4,67.9, 20.06, 100,34.8, 15.7, 0};

    // 🎨 自定义颜色
    private final String lineColor = "#855FAF";
    private final String barColor = "#855FAF";
    private final String[] pieColors = {"#855FAF", "#CEA3ED", "#7D4B79", "#F05865", "#36344C"};
    private final String backgroundColor = "#FFD4EC54"; // 半透明粉色背景

    @Override
    public void start(Stage stage) {

        // 英文分析段落
        Label analysis = new Label(
                "The express delivery industry in China has witnessed fluctuating financial activity between 2019 and 2024. " +
                        "While the number of financing events peaked in 2019, the highest total financing amount occurred in 2021, " +
                        "suggesting a shift from frequent smaller investments to fewer but more strategic funding initiatives. " +
                        "This trend indicates market consolidation and a focus on technological innovation, automation, and last-mile delivery optimization. " +
                        "The decline in single financing amount in 2023 reflects tighter capital flows, possibly due to macroeconomic uncertainties. " +
                        "Overall, the financial data stream illustrates a maturing industry adapting to digital transformation and competitive pressures."
        );
        // 修改analysis Label的设置部分
        analysis.setWrapText(true);
        analysis.setFont(Font.font("Arial", 20));
        analysis.setTextFill(Color.web("#855FAF"));
        analysis.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10;" +  // 与饼图相同的内部边距
                        "-fx-border-insets: 10;"  // 外部边距
        );

        VBox root = new VBox(30);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + backgroundColor + ";");

        Text title = new Text("Financial Analysis Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));

        LineChart<Number, Number> lineChart = createLineChart();
        BarChart<String, Number> barChart = createBarChart();
        PieChart pieChart = createPieChart();

        Label pieTitle = new Label("Event rating");
        pieTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        pieTitle.setTextFill(Color.web("#855FAF"));

        Label textTitle = new Label("AI Financial Analysis");
        textTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        textTitle.setTextFill(Color.web("#855FAF"));

        // 创建DeepSeek按钮
        Button AIButton = new Button("Ask More");
        AIButton.setStyle("-fx-background-color: " + lineColor + "; -fx-text-fill: white; -fx-font-size: 16px;");
        AIButton.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://chat.deepseek.com/"));
            } catch (Exception ex) {
                System.out.println("Fail to open the web: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(title, lineChart, barChart, pieTitle, pieChart, textTitle, analysis, AIButton);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 1366, 768);
        stage.setScene(scene);
        stage.setTitle("Financial Analysis");
        stage.show();
    }

    private LineChart<Number, Number> createLineChart() {
        NumberAxis xAxis = new NumberAxis(2014, 2024, 1);
        xAxis.setLabel("Year");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Peak Financing Events");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Trend of Peak Financing Events");
        lineChart.setStyle("-fx-background-color: transparent; -fx-plot-background-color: " + backgroundColor + ";");
        lineChart.setLegendVisible(false);
        lineChart.setPrefWidth(1000);
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);

        // 设置坐标轴和网格线样式
        lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: " + backgroundColor + ";");
        lineChart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: #855FAF20;");
        lineChart.lookup(".chart-vertical-grid-lines").setStyle("-fx-stroke: #855FAF20;");
        lineChart.lookup(".chart-title").setStyle("-fx-text-fill: #855FAF; -fx-font-size: 20px;");
        lineChart.lookup(".axis-label").setStyle("-fx-text-fill: #855FAF;");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < years.length; i++) {
            XYChart.Data<Number, Number> data = new XYChart.Data<>(years[i], peakEvents[i]);
            series.getData().add(data);
            Tooltip tooltip = new Tooltip("Year: " + years[i] + "\nEvents: " + peakEvents[i]);
            Tooltip.install(data.getNode(), tooltip);
        }
        lineChart.getData().add(series);

        lineChart.applyCss();
        lineChart.layout();

        for (XYChart.Data<Number, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + lineColor + ", white;");
            }
        }

        Node line = lineChart.lookup(".chart-series-line");
        if (line != null) {
            line.setStyle("-fx-stroke: " + lineColor + "; -fx-stroke-width: 2px;");
        }
        return lineChart;
    }

    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");
        xAxis.setTickLabelFill(Color.web("#855FAF"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Financing Amount (billion yuan)");
        yAxis.setTickLabelFill(Color.web("#855FAF"));

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Peak Financing Amount");
        barChart.setStyle("-fx-background-color: transparent; -fx-plot-background-color: " + backgroundColor + ";");
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(20);
        barChart.setBarGap(5);
        barChart.setPrefWidth(1000);
        barChart.setAlternativeRowFillVisible(false);
        barChart.setAlternativeColumnFillVisible(false);

        barChart.lookup(".chart-plot-background").setStyle("-fx-background-color: " + backgroundColor + ";");
        barChart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: #855FAF20;");
        barChart.lookup(".chart-vertical-grid-lines").setStyle("-fx-stroke: #855FAF20;");
        barChart.lookup(".chart-title").setStyle("-fx-text-fill: #855FAF; -fx-font-size: 20px;");
        barChart.lookup(".axis-label").setStyle("-fx-text-fill: #855FAF;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < years.length; i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(years[i]), peakAmount[i]);
            series.getData().add(data);
            Tooltip tooltip = new Tooltip("Year: " + years[i] + "\nAmount: " + peakAmount[i] + "B");
            Tooltip.install(data.getNode(), tooltip);
        }
        barChart.getData().add(series);

        barChart.applyCss();
        barChart.layout();

        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + barColor + ";");
            }
        }
        return barChart;
    }

    private PieChart createPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Peak Single Financing Amount");
        pieChart.setStyle("-fx-background-color: " + backgroundColor + ";");
        pieChart.lookup(".chart-title").setStyle("-fx-text-fill: #855FAF; -fx-font-size: 20px;");

        for (int i = 0; i < years.length; i++) {
            double value = peakSingleAmount[i];
            if (value > 0) {
                PieChart.Data slice = new PieChart.Data(String.valueOf(years[i]), value);
                pieChart.getData().add(slice);
                Tooltip tooltip = new Tooltip("Year: " + years[i] + "\nSingle Amount: " + value + "B");
                Tooltip.install(slice.getNode(), tooltip);
            }
        }

        pieChart.applyCss();
        pieChart.layout();
        pieChart.setLegendVisible(false);

        for (PieChart.Data d : pieChart.getData()) {
            Tooltip tooltip = new Tooltip(d.getName() + ": " + (int) d.getPieValue() + "B");
            Tooltip.install(d.getNode(), tooltip);
            d.getNode().setStyle("-fx-pie-color: " + pieColors[pieChart.getData().indexOf(d) % pieColors.length] + ";");
        }

        pieChart.setLabelsVisible(true);
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);
        pieChart.setPrefWidth(600);
        return pieChart;
    }

    public static void main(String[] args) {
        launch(args);
    }
}