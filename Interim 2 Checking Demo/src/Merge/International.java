package Merge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;

public class International extends Application {

    private static final Map<LocalDate, Map<String, Double>> dateRatesMap = new HashMap<>();

    static {
        loadExchangeRates();
    }

    private static class CurrencyPairInfo {
        String normalizedPair;
        double divisor;

        CurrencyPairInfo(String pair, double divisor) {
            this.normalizedPair = pair;
            this.divisor = divisor;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // 主布局
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(25, 30, 25, 30));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

        // 返回按钮
        Button backButton = new Button("← Back");
        backButton.setStyle(
                "-fx-background-color: #855FAF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        backButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Data");
            alert.setHeaderText("Exit without saving?");
            alert.setContentText("Leaving now will discard the current international transaction form. Are you sure you want to go back?");

            ButtonType yes = new ButtonType("Yes");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yes, cancel);

            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    try {
                        new InternationalList().start(new Stage());
                        primaryStage.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        HBox backBox = new HBox(backButton);
        backBox.setAlignment(Pos.TOP_LEFT);

        // 标题
        Text title = new Text("Add New International Transactions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));

        // 表单布局
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(15, 0, 15, 0));

        // 必填字段提示
        Text requiredHint = new Text("* Required fields");
        requiredHint.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        requiredHint.setFill(Color.web("#e74c3c"));

        // 本地货币
        Label localCurrencyLabel = new Label("Local currency *");
        localCurrencyLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        ComboBox<String> localCurrencyCombo = new ComboBox<>();
        localCurrencyCombo.getItems().addAll("CNY", "USD", "EUR", "GBP", "JPY", "HKD", "AUD", "NZD","SGD","CHF","CAD","MOP","MYR","RUB","ZAR","KRW","AED","SAR","HUF","PLN","DKK","SEK","NOK","TRY","MXN","THB");
        localCurrencyCombo.setValue("CNY");
        localCurrencyCombo.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        localCurrencyCombo.setPrefWidth(500);

        // 外币
        Label foreignCurrencyLabel = new Label("Foreign currency *");
        foreignCurrencyLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        ComboBox<String> foreignCurrencyCombo = new ComboBox<>();
        foreignCurrencyCombo.getItems().addAll("CNY", "USD", "EUR", "GBP", "JPY", "HKD", "AUD", "NZD","SGD","CHF","CAD","MOP","MYR","RUB","ZAR","KRW","AED","SAR","HUF","PLN","DKK","SEK","NOK","TRY","MXN","THB");
        foreignCurrencyCombo.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        foreignCurrencyCombo.setPromptText("Click here to input the kind of foreign currency");
        foreignCurrencyCombo.setPrefWidth(500);

        // 外币金额
        Label amountLabel = new Label("Amount in foreign currency *");
        amountLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        TextField amountField = new TextField();
        amountField.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        amountField.setPromptText("Click here to input the amount in foreign currency");
        amountField.setPrefWidth(500);

        // 交易时间
        Label timeLabel = new Label("Trading time *");
        timeLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        DatePicker timePicker = new DatePicker();
        timePicker.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        timePicker.setPromptText("Click here to select the trading time");

        timePicker.setPrefWidth(500);

        // 添加表单元素到网格
        formGrid.add(requiredHint, 0, 0, 2, 1);
        formGrid.add(localCurrencyLabel, 0, 1);
        formGrid.add(localCurrencyCombo, 1, 1);
        formGrid.add(foreignCurrencyLabel, 0, 2);
        formGrid.add(foreignCurrencyCombo, 1, 2);
        formGrid.add(amountLabel, 0, 3);
        formGrid.add(amountField, 1, 3);
        formGrid.add(timeLabel, 0, 4);
        formGrid.add(timePicker, 1, 4);



        // 按钮区域
        HBox buttonBox = new HBox(15);
        Button clearButton = new Button("Clear all");
        clearButton.setStyle("-fx-background-color: #855faf; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 120px; -fx-pref-height: 40px;");
        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 120px; -fx-pref-height: 40px;");
        buttonBox.getChildren().addAll(new Node[]{clearButton, confirmButton});
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);


        // 添加所有组件到主布局
        mainLayout.getChildren().addAll(
                backBox,
                title,
                formGrid,
                buttonBox
        );

        // 设置场景和舞台
        Scene scene = new Scene(mainLayout, 1366,768);
        primaryStage.setTitle("International Transaction Recorder");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 按钮事件处理
        clearButton.setOnAction(e -> {
            foreignCurrencyCombo.setValue(null);
            amountField.clear();
            timePicker.setValue(null);
        });

        confirmButton.setOnAction(e -> {
            // 1. 获取用户输入
            String localCurrency = localCurrencyCombo.getValue();
            String foreignCurrency = foreignCurrencyCombo.getValue();
            String amountText = amountField.getText();
            String date = timePicker.getValue().format(formatter);

            // 2. 验证输入
            if (localCurrency == null || foreignCurrency == null ||
                    amountText.isEmpty() || date == null) {
                showAlert("Error", "Please fill all required fields!");
                return;
            }

            try {
                double foreignAmount = Double.parseDouble(amountText);

                // 3. 获取汇率（示例API，实际需替换为真实API）
                double exchangeRate = getExchangeRate(foreignCurrency, localCurrency, date);

                // 4. 计算本币金额
                double localAmount = foreignAmount * exchangeRate;

                try (PrintWriter writer = new PrintWriter(new FileWriter("international.csv", true))) {
                    writer.printf("\n%s,%.2f,%.2f,%s,",foreignCurrency, foreignAmount, localAmount, date);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount format!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to fetch exchange rate: " + ex.getMessage());
            }

            try {
                new InternationalList().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // --- 汇率查询方法 ---
    private double getExchangeRate(String fromCurrency, String toCurrency, String dateStr) throws Exception {
        // 参数标准化
        fromCurrency = fromCurrency.toUpperCase().trim();
        toCurrency = toCurrency.toUpperCase().trim();
        if (fromCurrency.equals(toCurrency)) return 1.0;

        // 日期解析
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (DateTimeParseException e) {
            throw new Exception("日期格式错误，请使用yyyy/MM/dd格式");
        }

        // 获取当日汇率
        Map<String, Double> rates = dateRatesMap.get(date);
        if (rates == null) {
            throw new Exception("无" + dateStr + "的汇率数据");
        }

        // 直接匹配
        String directKey = fromCurrency + "/" + toCurrency;
        if (rates.containsKey(directKey)) {
            return rates.get(directKey);
        }

        // 反向匹配
        String reverseKey = toCurrency + "/" + fromCurrency;
        if (rates.containsKey(reverseKey)) {
            return 1.0 / rates.get(reverseKey);
        }

        // CNY中转逻辑
        boolean fromIsCNY = fromCurrency.equals("CNY");
        boolean toIsCNY = toCurrency.equals("CNY");

        // Case 1: 从CNY到其他货币
        if (fromIsCNY) {
            String cnyToTarget = "CNY/" + toCurrency;
            if (rates.containsKey(cnyToTarget)) {
                return rates.get(cnyToTarget);
            }
            String targetToCNY = toCurrency + "/CNY";
            if (rates.containsKey(targetToCNY)) {
                return 1.0 / rates.get(targetToCNY);
            }
        }

        // Case 2: 从其他货币到CNY
        if (toIsCNY) {
            String sourceToCNY = fromCurrency + "/CNY";
            if (rates.containsKey(sourceToCNY)) {
                return rates.get(sourceToCNY);
            }
            String cnyToSource = "CNY/" + fromCurrency;
            if (rates.containsKey(cnyToSource)) {
                return 1.0 / rates.get(cnyToSource);
            }
        }

        // Case 3: 通过CNY中转的交叉汇率
        String fromToCNY = fromCurrency + "/CNY";
        String cnyToTarget = "CNY/" + toCurrency;
        if (rates.containsKey(fromToCNY) && rates.containsKey(cnyToTarget)) {
            return rates.get(fromToCNY) * rates.get(cnyToTarget);
        }

        String fromToCNY2 = fromCurrency + "/CNY";
        String targetToCNY = toCurrency + "/CNY";
        if (rates.containsKey(fromToCNY2) && rates.containsKey(targetToCNY)) {
            return rates.get(fromToCNY2) / rates.get(targetToCNY);
        }

        throw new Exception("无法转换: " + fromCurrency + "→" + toCurrency);
    }


    // --- 加载汇率数据 ---
    private static void loadExchangeRates() {
        String filename = "人民币汇率中间价历史数据.csv";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {

            String line;
            List<String> headers = new ArrayList<>();
            Map<String, CurrencyPairInfo> columnMap = new HashMap<>();
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll("\uFEFF", ""); // 彻底清除BOM
                if (line.isEmpty()) continue;

                if (isFirstLine) {
                    // 智能列头处理
                    String[] rawHeaders = line.split(",");
                    for (String header : rawHeaders) {
                        String processedHeader = header.trim();
                        if (processedHeader.equals("日期")) {
                            headers.add(processedHeader);
                            continue;
                        }

                        // 特殊列处理（如100JPY/CNY）
                        if (processedHeader.startsWith("100")) {
                            String normalized = processedHeader.substring(3);
                            columnMap.put(processedHeader,
                                    new CurrencyPairInfo(normalized.toUpperCase(), 100.0));
                        } else {
                            columnMap.put(processedHeader,
                                    new CurrencyPairInfo(processedHeader.toUpperCase(), 1.0));
                        }
                        headers.add(processedHeader);
                    }
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",", -1); // 保留空值
                if (values.length < 2) continue;

                // 日期解析
                LocalDate date;
                try {
                    date = LocalDate.parse(values[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    System.err.println("日期解析失败: " + values[0]);
                    continue;
                }

                // 汇率解析
                Map<String, Double> rateMap = new HashMap<>();
                for (int i = 1; i < headers.size(); i++) {
                    if (i >= values.length) break;
                    String rawValue = values[i].trim();
                    if (rawValue.isEmpty()) continue;

                    String columnName = headers.get(i);
                    if (columnName.equals("日期")) continue;

                    try {
                        double value = Double.parseDouble(rawValue);
                        CurrencyPairInfo info = columnMap.get(columnName);
                        if (info != null) {
                            double adjustedValue = value / info.divisor;
                            rateMap.put(info.normalizedPair, adjustedValue);
                        }
                    } catch (NumberFormatException e) {
                        System.err.printf("数值解析错误 [列:%s 值:%s]%n", columnName, rawValue);
                    }
                }
                dateRatesMap.put(date, rateMap);
            }
        } catch (IOException e) {
            System.err.println("致命错误：汇率文件加载失败");
            throw new RuntimeException("无法加载汇率文件: " + filename, e);
        }
    }

    private static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        String[] patterns = {
                "yyyy-M-d",
                "yyyy/MM/dd",
                "yyyy年M月d日",
                "yyyyMMdd"
        };

        for (String pattern : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {}
        }
        throw new DateTimeParseException("无法解析日期格式", dateStr, 0);
    }
    // --- 显示弹窗 ---
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public static void main(String[] args) {
    launch(args);
    }
}