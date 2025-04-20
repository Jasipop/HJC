//package myapp;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Bank_Data_Management extends Application {
    private Stage primaryStage;
    private ScrollPane accountScroll;
    private VBox accountCard;
    public static class BankTransaction {
        private final String date, description, amount, balance, type;
        public BankTransaction(String date, String desc, String amount, String balance, String type) {
            this.date = date; this.description = desc; this.amount = amount;
            this.balance = balance; this.type = type;
        }
        public String getDate() { return date; }
        public String getDescription() { return description; }
        public String getAmount() { return amount; }
        public String getBalance() { return balance; }
        public String getType() { return type; }
    }

    public static class BankAccount {
        private final String accountNumber, bankName;
        public BankAccount(String accNum, String bankName) {
            this.accountNumber = accNum; this.bankName = bankName;
        }
        public String getAccountNumber() { return accountNumber; }
        public String getBankName() { return bankName; }
    }

    private final ObservableList<BankTransaction> transactions = FXCollections.observableArrayList(
            // 原始10条数据
            new BankTransaction("2025-04-01", "Grocery Store", "-120.00", "5,880.00", "Debit"),
            new BankTransaction("2025-04-02", "Salary", "+10,000.00", "15,880.00", "Credit"),
            new BankTransaction("2025-04-03", "Restaurant", "-230.00", "15,650.00", "Debit"),
            new BankTransaction("2025-04-04", "Online Transfer", "-3,000.00", "12,650.00", "Debit"),
            new BankTransaction("2025-04-05", "Gym Membership", "-300.00", "12,350.00", "Debit"),
            new BankTransaction("2025-04-06", "Interest Income", "+80.00", "12,430.00", "Credit"),
            new BankTransaction("2025-04-07", "Mobile Bill", "-150.00", "12,280.00", "Debit"),
            new BankTransaction("2025-04-08", "Fuel", "-400.00", "11,880.00", "Debit"),
            new BankTransaction("2025-04-09", "Netflix", "-60.00", "11,820.00", "Debit"),
            new BankTransaction("2025-04-10", "Freelance Work", "+2,500.00", "14,320.00", "Credit"),

            // 新增15条数据
            new BankTransaction("2025-04-11", "Book Store", "-89.00", "14,231.00", "Debit"),
            new BankTransaction("2025-04-12", "Coffee Shop", "-35.00", "14,196.00", "Debit"),
            new BankTransaction("2025-04-13", "Tax Refund", "+1,200.00", "15,396.00", "Credit"),
            new BankTransaction("2025-04-14", "Car Maintenance", "-850.00", "14,546.00", "Debit"),
            new BankTransaction("2025-04-15", "Investment Income", "+450.00", "14,996.00", "Credit"),
            new BankTransaction("2025-04-16", "Clothing Store", "-299.00", "14,697.00", "Debit"),
            new BankTransaction("2025-04-17", "Electricity Bill", "-180.00", "14,517.00", "Debit"),
            new BankTransaction("2025-04-18", "Part-time Job", "+800.00", "15,317.00", "Credit"),
            new BankTransaction("2025-04-19", "Dentist", "-350.00", "14,967.00", "Debit"),
            new BankTransaction("2025-04-20", "Software License", "-199.00", "14,768.00", "Debit"),
            new BankTransaction("2025-04-21", "Stock Dividend", "+120.00", "14,888.00", "Credit"),
            new BankTransaction("2025-04-22", "Train Ticket", "-150.00", "14,738.00", "Debit"),
            new BankTransaction("2025-04-23", "Bonus", "+2,000.00", "16,738.00", "Credit"),
            new BankTransaction("2025-04-24", "Home Decor", "-680.00", "16,058.00", "Debit"),
            new BankTransaction("2025-04-25", "Insurance", "-450.00", "15,608.00", "Debit")
    );

    private final ObservableList<BankAccount> accounts = FXCollections.observableArrayList(
            new BankAccount("6222 1234 5678 9012", "ICBC"),
            new BankAccount("6217 8888 0000 9999", "Bank of China"),
            new BankAccount("6234 5678 9012 3456", "China Merchants Bank"),
            new BankAccount("6225 4321 9876 5432", "CCB"),
            new BankAccount("6210 1122 3344 5566", "ABC"),
            new BankAccount("6233 6655 4477 8899", "SPDB"),
            new BankAccount("6228 8765 4321 0987", "CMBC"),
            new BankAccount("6216 2233 4455 6677", "Ping An Bank")
    );

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        this.primaryStage = stage;
        // 主标题样式
        Label pageTitle = new Label("Bank Data Management");
        pageTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 32));
        pageTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("Manage your bank accounts and transactions");
        subtitle.setFont(Font.font("Microsoft YaHei", 16));
        subtitle.setTextFill(Color.WHITE);

        VBox titleBox = new VBox(5, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: #e6d5ff;");
        titleBox.setPadding(new Insets(25, 0, 25, 0));

        // ================== 调整边距 ==================
        TableView<BankTransaction> table = createTransactionTable();
        VBox tableCard = createStyledCard(table, "Transaction Records");
        VBox accountCard = createAccountCard();

        HBox mainContent = new HBox();
        mainContent.setSpacing(20);
        mainContent.setPadding(new Insets(20, 40, 20, 40));
        mainContent.setAlignment(Pos.CENTER);

        // 使用Region制造间距
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        mainContent.getChildren().addAll(leftSpacer, tableCard, accountCard, rightSpacer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 20 0;");

        VBox contentWrapper = new VBox(scrollPane);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(0));
        contentWrapper.setStyle("-fx-background-color: white;");

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

        navBar.getChildren().addAll(homeBtn, discoverBtn, settingsBtn);

        VBox mainLayout = new VBox(0, titleBox, contentWrapper);

        mainLayout.setStyle("-fx-background-color: white; -fx-spacing: 0;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPaneplus = new ScrollPane(mainLayout);
        scrollPaneplus.setFitToWidth(true);
        scrollPaneplus.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPaneplus.setPrefHeight(600);

        root.setCenter(mainLayout);
        root.setBottom(navBar);

        Scene scene = new Scene(root, 1366, 768);
        stage.setTitle("Bank Data Viewer");
        stage.setScene(scene);
        stage.show();
    }

    private TableView<BankTransaction> createTransactionTable() {
        TableView<BankTransaction> table = new TableView<>(transactions);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 14px; -fx-table-cell-size: 40px;");

        // 创建表格列
        table.getColumns().addAll(
                createStyledColumn("Date", BankTransaction::getDate),
                createStyledColumn("Description", BankTransaction::getDescription),
                createStyledColumn("Amount", BankTransaction::getAmount),
                createStyledColumn("Balance", BankTransaction::getBalance),
                createStyledColumn("Type", BankTransaction::getType)
        );
        return table;
    }

    private TableColumn<BankTransaction, String> createStyledColumn(String title,
                                                                    java.util.function.Function<BankTransaction, String> prop) {
        TableColumn<BankTransaction, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(prop.apply(cell.getValue())));
        col.setStyle("-fx-alignment: CENTER;");
        return col;
    }

    private VBox createAccountList() {
        VBox accountList = new VBox(10);
        accountList.setPadding(new Insets(5));

        for (BankAccount account : accounts) {
            HBox accountEntry = new HBox();
            accountEntry.setPadding(new Insets(10));
            accountEntry.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-radius: 8;"
            );

            VBox infoVBox = new VBox(5);
            Label bankLabel = createInfoLabel("🏦 " + account.getBankName());
            Label accountLabel = createInfoLabel("💳 " + account.getAccountNumber());
            infoVBox.getChildren().addAll(bankLabel, accountLabel);

            Button deleteBtn = new Button("×");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-size: 16px;");
            deleteBtn.setOnAction(e -> {
                accounts.remove(account);
                refreshAccountList();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            accountEntry.getChildren().addAll(infoVBox, spacer, deleteBtn);
            accountList.getChildren().add(accountEntry);
        }
        return accountList;
    }

    private Label createCardTitle(String title) {
        Label cardTitle = new Label(title);
        cardTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        cardTitle.setTextFill(Color.web("#6c757d"));
        return cardTitle;
    }

    private VBox createStyledCard(TableView<BankTransaction> table, String title) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);" +
                        "-fx-pref-width: 700;" +
                        "-fx-pref-height: 620;"
        );

        // 修改表格滚动设置，只保留一个滚动条
        table.setFixedCellSize(40);  // 设置固定行高
        table.setPrefHeight(480);    // 设置表格固定高度

        // 移除额外的ScrollPane包装
        table.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        // 说明文字样式调整
        Label infoLabel = new Label("The system will automatically integrate and display transaction details.");
        infoLabel.setFont(Font.font("Microsoft YaHei", 12));
        infoLabel.setTextFill(Color.web("#666"));
        infoLabel.setWrapText(true);
        infoLabel.setPadding(new Insets(15, 10, 10, 10));
        infoLabel.setMaxWidth(680);

        card.getChildren().addAll(createCardTitle(title), table, infoLabel);

        return card;
    }

    private Button createImportButton() {
        Button importBtn = new Button("📁 Import Bank CSV");
        importBtn.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 15;"
        );
        importBtn.setOnAction(e -> handleCSVImport(primaryStage));
        return importBtn;
    }

    private VBox createAccountCard() {
        accountCard = new VBox(10); // 保存卡片引用
        accountCard.setPadding(new Insets(15));
        accountCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.98);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);" +
                        "-fx-pref-width: 400;"
        );

        accountScroll = new ScrollPane(createAccountList()); // 保存滚动面板引用
        accountScroll.setFitToWidth(true);
        accountScroll.setPrefHeight(550);
        accountScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        Button importBtn = createImportButton();
        Button addAccountBtn = createAddAccountButton();

        HBox buttonBox = new HBox(10, importBtn, addAccountBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        accountCard.getChildren().addAll(
                createCardTitle("Bank Accounts"),
                accountScroll,
                buttonBox
        );

        return accountCard;
    }


    private Button createAddAccountButton() {
        Button addBtn = new Button("➕ Add Account");
        addBtn.setStyle(
                "-fx-background-color: #28a745;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 15;"
        );
        addBtn.setOnAction(e -> showAddAccountDialog());
        return addBtn;
    }

    private void validateInputs(TextField accountNumberField, TextField bankNameField, Node addButton) {
        boolean disable = accountNumberField.getText().trim().isEmpty() ||
                bankNameField.getText().trim().isEmpty();
        addButton.setDisable(disable);
    }

    private void showAddAccountDialog() {
        Dialog<BankAccount> dialog = new Dialog<>();
        dialog.setTitle("Add New Bank Account");
        dialog.setHeaderText("Please enter the account details:");

        // 设置按钮
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 创建输入字段
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField accountNumberField = new TextField();
        accountNumberField.setPromptText("Account Number");
        TextField bankNameField = new TextField();
        bankNameField.setPromptText("Bank Name");

        // 修正网格布局
        grid.add(new Label("Account Number:"), 0, 0);
        grid.add(accountNumberField, 1, 0);
        grid.add(new Label("Bank Name:"), 0, 1);
        grid.add(bankNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 启用/禁用添加按钮
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // 添加输入验证
        accountNumberField.textProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(accountNumberField, bankNameField, addButton));
        bankNameField.textProperty().addListener((obs, oldVal, newVal) ->
                validateInputs(accountNumberField, bankNameField, addButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new BankAccount(
                        accountNumberField.getText().trim(),
                        bankNameField.getText().trim()
                );
            }
            return null;
        });

        Optional<BankAccount> result = dialog.showAndWait();
        result.ifPresent(account -> {
            accounts.add(account);
            refreshAccountList();  // 这里会触发界面更新
            accountScroll.setVvalue(1.0);  // 滚动到底部显示新添加项
        });
    }

    private void refreshAccountList() {
        accountScroll.setContent(createAccountList());
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", 14));
        label.setTextFill(Color.web("#444"));
        label.setWrapText(true);
        return label;
    }

    private void handleCSVImport(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                transactions.clear();
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 5 && !line.contains("Date")) {
                        transactions.add(new BankTransaction(parts[0], parts[1], parts[2], parts[3], parts[4]));
                    }
                }
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to read file.").showAndWait();
            }
        }
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

    public static void main(String[] args) {
        launch(args);
    }
}