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
import javafx.util.StringConverter;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
//import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class International extends Application {

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();

        // 主布局
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(25, 30, 25, 30));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

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
        localCurrencyCombo.getItems().addAll("CNY", "USD", "EUR", "GBP");
        localCurrencyCombo.setValue("CNY");
        localCurrencyCombo.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        localCurrencyCombo.setPrefWidth(500);

        // 外币
        Label foreignCurrencyLabel = new Label("Foreign currency *");
        foreignCurrencyLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        ComboBox<String> foreignCurrencyCombo = new ComboBox<>();
        foreignCurrencyCombo.getItems().addAll("CNY", "USD", "EUR", "GBP", "AUD", "JPY");
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
                title,
                formGrid,
                buttonBox
        );

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

        root.setCenter(mainLayout);
        root.setBottom(navBar);

        // 设置场景和舞台
        Scene scene = new Scene(root, 1366,768);
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
            LocalDate date = timePicker.getValue();

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

                // 5. 显示结果
                showAlert("Result",
                        String.format("%.2f %s = %.2f %s (Rate: 1 %s = %.4f %s)",
                                foreignAmount, foreignCurrency,
                                localAmount, localCurrency,
                                foreignCurrency, exchangeRate, localCurrency)
                );

            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount format!");
            } catch (Exception ex) {
                showAlert("Error", "Failed to fetch exchange rate: " + ex.getMessage());
            }
        });
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


    // --- 汇率查询方法 ---
    private double getExchangeRate(String fromCurrency, String toCurrency, LocalDate date) throws Exception {
        // 示例：使用固定汇率（实际项目应调用API）
        if (fromCurrency.equals("USD") && toCurrency.equals("CNY")) {
            return 7.2; // 模拟汇率
        } else if (fromCurrency.equals("EUR") && toCurrency.equals("CNY")) {
            return 7.8;
        }
        throw new Exception("Unsupported currency pair");
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