package myapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.Pair;

import java.io.File;
import java.util.*;

public class Free_Design_Classification extends Application {

    private ComboBox<String> groupComboBox;
    private TreeView<String> expenseTreeView;
    private TreeView<String> classificationTreeView;
    private double totalExpenditure = 0.0;
    private Label totalExpenditureLabel;
    private Map<String, TreeItem<String>> groupData;
    private Map<String, TreeItem<String>> classificationSystems;
    private String currentGroup;
    private List<String> addedNames = new ArrayList<>();
    private List<String> addedAmounts = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        initializeDataStructures();
        VBox mainLayout = createMainLayout(createMainContent());

        Scene scene = new Scene(mainLayout, 1366, 768);
        primaryStage.setTitle("Free Definition");
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeSampleData();
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
            try { new Nutllet.Nutllet().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        discoverBtn.setOnAction(e -> {
            try { new Discover().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        settingsBtn.setOnAction(e -> {
            try { new Settings().start(new Stage()); primaryStage.close(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        navBar.getChildren().addAll(settingsBtn, discoverBtn, homeBtn); // 从右到左
        mainLayout.getChildren().add(navBar);
    }

    private void initializeDataStructures() {
        groupData = new HashMap<>();
        classificationSystems = new HashMap<>();
    }

    private void initializeSampleData() {
        // Initialize groups
        String[] groups = {"Daily Expenses", "Experiment Group 1", "Project Expenses"};
        for (String group : groups) {
            TreeItem<String> rootItem = new TreeItem<>("Expense Classification");
            rootItem.setExpanded(true);
            groupData.put(group, rootItem);

            TreeItem<String> classRoot = new TreeItem<>("Classification System");
            classRoot.setExpanded(true);
            classificationSystems.put(group, classRoot);
        }

        // Add sample data for "Daily Expenses"
        addSampleDataForDailyExpenses();

        // Add sample data for "Project Expenses"
        addSampleDataForProjectExpenses();

        // Add sample data for "Experiment Group 1"
        addSampleDataForExperiment();

        // Set initial group
        currentGroup = "Daily Expenses";
        groupComboBox.getItems().addAll(groups);
        groupComboBox.setValue(currentGroup);

        // Set initial trees
        updateTrees();
    }

    private void addSampleDataForDailyExpenses() {
        TreeItem<String> root = groupData.get("Daily Expenses");
        TreeItem<String> classRoot = classificationSystems.get("Daily Expenses");

        String[] newCategories = {
                "Food & Beverage", "Shopping", "Transportation",
                "Entertainment", "Education", "Fitness", "Utilities & Transfer"
        };

        for (String category : newCategories) {
            TreeItem<String> categoryNode = new TreeItem<>(category);
            root.getChildren().add(new TreeItem<>(category));      // 左侧支出分类结构
            classRoot.getChildren().add(new TreeItem<>(category)); // 右侧分类系统结构
        }
    }


    private void clearExistingSubcategories(TreeItem<String> root) {
        if (root != null) {
            root.getChildren().clear(); // 清空所有子节点
        }
    }

    private void addSampleDataForProjectExpenses() {
        TreeItem<String> root = groupData.get("Project Expenses");
        TreeItem<String> classRoot = classificationSystems.get("Project Expenses");

        // Hardware
        TreeItem<String> hardware = new TreeItem<>("Hardware");
        hardware.getChildren().addAll(
                new TreeItem<>("Server - ¥15000"),
                new TreeItem<>("Laptop - ¥8000"),
                new TreeItem<>("Monitor - ¥2000"),
                new TreeItem<>("Network - ¥1500")
        );

        // Software
        TreeItem<String> software = new TreeItem<>("Software");
        software.getChildren().addAll(
                new TreeItem<>("Dev Tools - ¥5000"),
                new TreeItem<>("Server OS - ¥3000"),
                new TreeItem<>("Design - ¥2400")
        );

        // Human Resources
        TreeItem<String> hr = new TreeItem<>("Human Resources");
        hr.getChildren().addAll(
                new TreeItem<>("Outsourcing - ¥20000"),
                new TreeItem<>("Design - ¥8000"),
                new TreeItem<>("Consulting - ¥5000")
        );

        // Operations
        TreeItem<String> operation = new TreeItem<>("Operations");
        operation.getChildren().addAll(
                new TreeItem<>("Cloud - ¥3000"),
                new TreeItem<>("Domain - ¥500"),
                new TreeItem<>("Office - ¥10000")
        );

        root.getChildren().addAll(hardware, software, hr, operation);

        // Add categories to classification system
        classRoot.getChildren().addAll(
                new TreeItem<>("Hardware"),
                new TreeItem<>("Software"),
                new TreeItem<>("Human Resources"),
                new TreeItem<>("Operations")
        );
    }

    private void addSampleDataForExperiment() {
        TreeItem<String> root = groupData.get("Experiment Group 1");
        TreeItem<String> classRoot = classificationSystems.get("Experiment Group 1");

        // Hardware
        TreeItem<String> hardware = new TreeItem<>("Hardware");
        hardware.getChildren().addAll(
                new TreeItem<>("Hard Drive - ¥200"),
                new TreeItem<>("Monitor - ¥1200")
        );

        // Software
        TreeItem<String> software = new TreeItem<>("Software");
        software.getChildren().addAll(
                new TreeItem<>("OS - ¥800"),
                new TreeItem<>("Dev Tools - ¥500")
        );

        root.getChildren().addAll(hardware, software);

        // Add categories to classification system
        classRoot.getChildren().addAll(
                new TreeItem<>("Hardware"),
                new TreeItem<>("Software")
        );
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPrefSize(600, 480);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Transaction Group");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#6c757d"));

        groupComboBox = new ComboBox<>();
        groupComboBox.setPromptText("Select Expense Group");
        groupComboBox.setStyle(
                "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-pref-width: 560;"
        );

        groupComboBox.setOnAction(e -> {
            currentGroup = groupComboBox.getValue();
            updateTrees();
        });

        expenseTreeView = new TreeView<>();
        expenseTreeView.setEditable(true);
        expenseTreeView.setShowRoot(false);
        expenseTreeView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-tree-cell-indent: 20;" +
                        "-fx-padding: 5;"
        );

        expenseTreeView.setCellFactory(tv -> new TreeCell<String>() {
            private HBox cellContent;
            private Label itemLabel;
            private Label actionButton;

            {
                itemLabel = new Label();
                itemLabel.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");

                actionButton = new Label();
                actionButton.setStyle(
                        "-fx-text-fill: #9c36b5;" +
                                "-fx-font-size: 18px;" +
                                "-fx-min-width: 24px;" +
                                "-fx-min-height: 24px;" +
                                "-fx-alignment: center;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );

                cellContent = new HBox(5);
                cellContent.setAlignment(Pos.CENTER_LEFT);

                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && getTreeItem() != null && getTreeItem().getValue().contains("¥")) {
                        showEditDialog(getTreeItem());
                    }
                });

                actionButton.setOnMouseClicked(e -> {
                    TreeItem<String> treeItem = getTreeItem();
                    if (treeItem != null) {
                        if (treeItem.getValue().contains("¥")) {
                            if (treeItem.getParent() != null) {
                                treeItem.getParent().getChildren().remove(treeItem);
                                updateTotalExpenditure();
                            }
                        } else {
                            showAddTransactionDialog(treeItem); // 弹出条目选择窗口
                        }
                    }
                });

            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                if (item.contains("¥")) {
                    String[] parts = item.split(" - ¥");
                    if (parts.length == 2) {
                        itemLabel.setText(String.format("%s    ¥%s", parts[0], parts[1]));
                        itemLabel.setStyle(
                                "-fx-font-family: 'Microsoft YaHei';" +
                                        "-fx-font-size: 14px;" +
                                        "-fx-text-fill: #495057;" +
                                        "-fx-font-weight: normal;"
                        );
                        actionButton.setText("-");
                    }
                } else {
                    itemLabel.setText(item);
                    itemLabel.setStyle(
                            "-fx-font-family: 'Microsoft YaHei';" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-text-fill: #2b8a3e;" +
                                    "-fx-font-weight: bold;"
                    );
                    actionButton.setText("+");
                }

                cellContent.getChildren().setAll(itemLabel, actionButton);
                setGraphic(cellContent);
            }
        });

        expenseTreeView.setPrefHeight(350);

        totalExpenditureLabel = new Label("Total Expenditure: ¥0");
        totalExpenditureLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        totalExpenditureLabel.setStyle("-fx-text-fill: #4a4a4a;");

        panel.getChildren().addAll(title, groupComboBox, expenseTreeView, totalExpenditureLabel);

        return panel;
    }

    private void showAddTransactionDialog(TreeItem<String> parentItem) {
        if (parentItem == null) return;

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Select Transaction from Deals");
        dialog.setHeaderText("Choose a transaction to add");

        ButtonType confirmButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        ListView<String[]> listView = new ListView<>();
        List<String[]> deals = loadDealsFromCSV("src/deals.csv");
        listView.getItems().addAll(deals);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.join(" | ", item));
                }
            }
        });

        listView.setPrefWidth(600);
        listView.setPrefHeight(300);

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selected -> {
            if (selected != null) {
                // 原始带引号的字符串，例如："早饭"
                String rawName = selected.length >= 4 ? selected[3] : "Unnamed";
                String rawAmount = selected.length >= 6 ? selected[5] : "0";

                // 去除前后引号
                String cleanName = rawName.replaceAll("^\"|\"$", "").trim();

                // 去除￥符号和引号，保留数字及小数点
                String cleanAmount = rawAmount.replaceAll("[^0-9.]", "").trim();

                // 添加到树结构显示
                TreeItem<String> newItem = new TreeItem<>(cleanName + " - ¥" + cleanAmount);
                parentItem.getChildren().add(newItem);
                parentItem.setExpanded(true);
                updateTotalExpenditure();

                // 保存到列表中
                addedNames.add(cleanName);
                addedAmounts.add(cleanAmount);

                System.out.println("Added Name: " + cleanName);
                System.out.println("Added Amount: " + cleanAmount);
            }
        });
    }


    private List<String[]> loadDealsFromCSV(String filename) {
        List<String[]> deals = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String fourth = parts[3].replaceAll("\"", "").trim();
                    String third = parts[2].replaceAll("\"", "").trim();

                    // 如果第四列是 "/" 或者是全数字（纯数字字符串）
                    if (fourth.equals("/") || fourth.matches("\\d+")) {
                        parts[3] = parts[2];  // 用第三列内容替换第四列
                    }
                    deals.add(parts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deals;
    }




    private void showEditDialog(TreeItem<String> item) {
        if (item == null || !item.getValue().contains("¥")) return;

        String[] parts = item.getValue().split(" - ¥");
        if (parts.length != 2) return;

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.setHeaderText("Please enter new transaction details");

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(parts[0]);
        nameField.setPromptText("Transaction Name");
        TextField amountField = new TextField(parts[1]);
        amountField.setPromptText("Amount");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new Pair<>(nameField.getText(), amountField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String newName = result.getKey().trim();
            String newAmountStr = result.getValue().trim();

            if (newName.isEmpty() || newAmountStr.isEmpty()) {
                // 名称或金额为空，不更新
                return;
            }

            try {
                double newAmount = Double.parseDouble(newAmountStr);
                item.setValue(newName + " - ¥" + String.format("%.2f", newAmount));
                updateTotalExpenditure();
            } catch (NumberFormatException e) {
                // 金额输入非法，忽略修改或提示
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid number for the amount.");
                alert.showAndWait();
            }
        });

    }

    private void updateTotalExpenditure() {
        totalExpenditure = calculateTotalExpenditure(expenseTreeView.getRoot());
        totalExpenditureLabel.setText(String.format("Total Expenditure: ¥%.2f", totalExpenditure));
    }

    private double calculateTotalExpenditure(TreeItem<String> item) {
        if (item == null) return 0.0;

        double sum = 0.0;
        String itemValue = item.getValue();
        if (itemValue != null && itemValue.contains("¥")) {
            try {
                // 用正则匹配 ¥后面的数字（允许小数）
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("¥([0-9]+(\\.[0-9]+)?)").matcher(itemValue);
                while (matcher.find()) {
                    String numberStr = matcher.group(1);
                    sum += Double.parseDouble(numberStr);
                }
            } catch (NumberFormatException e) {
                // 跳过格式错误
            }
        }

        for (TreeItem<String> child : item.getChildren()) {
            sum += calculateTotalExpenditure(child);
        }
        return sum;
    }


    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefSize(600, 480);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Classification System Management");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#6c757d"));

        classificationTreeView = new TreeView<>();
        classificationTreeView.setEditable(true);
        classificationTreeView.setCellFactory(TextFieldTreeCell.forTreeView());
        classificationTreeView.setPrefHeight(350);
        classificationTreeView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-tree-cell-indent: 10;" +
                        "-fx-tree-disclosure-node-size: 0;" +
                        "-fx-padding: 5;"
        );

        Button addCategoryButton = new Button("Add Category");
        Button addSubcategoryButton = new Button("Add Subcategory");
        Button deleteCategoryButton = new Button("Delete Category");
        Button saveButton = new Button("Save Changes");

        String buttonStyle =
                "-fx-background-color: #9c36b5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: 'Microsoft YaHei';" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 100px;";

        addCategoryButton.setStyle(buttonStyle);
        addSubcategoryButton.setStyle(buttonStyle);
        deleteCategoryButton.setStyle(buttonStyle);
        saveButton.setStyle(buttonStyle);

        addCategoryButton.setOnAction(e -> addCategory());
        addSubcategoryButton.setOnAction(e -> addSubcategory());
        deleteCategoryButton.setOnAction(e -> deleteCategory());
        saveButton.setOnAction(e -> saveClassificationChanges());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(addCategoryButton, addSubcategoryButton, deleteCategoryButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        panel.getChildren().addAll(title, classificationTreeView, buttonBox);

        return panel;
    }

    private void updateTrees() {
        // Update expense tree
        expenseTreeView.setRoot(groupData.get(currentGroup));
        updateTotalExpenditure();

        // Update classification tree
        classificationTreeView.setRoot(classificationSystems.get(currentGroup));
    }

    private void saveClassificationChanges() {
        TreeItem<String> oldExpenseRoot = groupData.get(currentGroup);
        TreeItem<String> newClassificationRoot = classificationTreeView.getRoot();

        // Create new expense tree based on classification
        TreeItem<String> newExpenseRoot = new TreeItem<>("Expense Classification");
        newExpenseRoot.setExpanded(true);

        // Create map of old expenses
        Map<String, List<TreeItem<String>>> oldExpenses = new HashMap<>();
        collectExpensesRecursively(oldExpenseRoot, oldExpenses, "");

        // Rebuild expense tree
        rebuildExpenseTree(newClassificationRoot, newExpenseRoot, oldExpenses, "");

        // Update data structures
        groupData.put(currentGroup, newExpenseRoot);
        classificationSystems.put(currentGroup, copyTree(newClassificationRoot));

        // Update trees
        updateTrees();
    }

    private void collectExpensesRecursively(TreeItem<String> root, Map<String, List<TreeItem<String>>> expenses, String parentPath) {
        for (TreeItem<String> category : root.getChildren()) {
            String currentPath = parentPath.isEmpty() ? category.getValue() : parentPath + "/" + category.getValue();
            List<TreeItem<String>> categoryExpenses = new ArrayList<>();

            // Collect direct expenses
            for (TreeItem<String> expense : category.getChildren()) {
                if (expense.getValue().contains("¥")) {
                    categoryExpenses.add(expense);
                }
            }
            expenses.put(currentPath, categoryExpenses);

            // Recursively collect expenses from subcategories
            collectExpensesRecursively(category, expenses, currentPath);
        }
    }

    private void rebuildExpenseTree(TreeItem<String> sourceRoot, TreeItem<String> targetRoot, Map<String, List<TreeItem<String>>> oldExpenses, String parentPath) {
        for (TreeItem<String> category : sourceRoot.getChildren()) {
            TreeItem<String> newCategory = new TreeItem<>(category.getValue());
            newCategory.setExpanded(true);

            String currentPath = parentPath.isEmpty() ? category.getValue() : parentPath + "/" + category.getValue();

            // Add existing expenses if available
            List<TreeItem<String>> expenses = oldExpenses.get(currentPath);
            if (expenses != null) {
                for (TreeItem<String> expense : expenses) {
                    newCategory.getChildren().add(new TreeItem<>(expense.getValue()));
                }
            }

            targetRoot.getChildren().add(newCategory);

            // Recursively rebuild subcategories
            rebuildExpenseTree(category, newCategory, oldExpenses, currentPath);
        }
    }

    private TreeItem<String> copyTree(TreeItem<String> item) {
        TreeItem<String> copy = new TreeItem<>(item.getValue());
        copy.setExpanded(item.isExpanded());
        for (TreeItem<String> child : item.getChildren()) {
            copy.getChildren().add(copyTree(child));
        }
        return copy;
    }

    private void addCategory() {
        TreeItem<String> root = classificationTreeView.getRoot();
        TreeItem<String> newCategory = new TreeItem<>("New Category");
        root.getChildren().add(newCategory);
        root.setExpanded(true);
    }

    private void addSubcategory() {
        TreeItem<String> selectedItem = classificationTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TreeItem<String> newCategory = new TreeItem<>("New Subcategory");
            selectedItem.getChildren().add(newCategory);
            selectedItem.setExpanded(true);
        }
    }

    private void deleteCategory() {
        TreeItem<String> selectedItem = classificationTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getParent() != null) {
            selectedItem.getParent().getChildren().remove(selectedItem);
        }
    }

    private HBox createMainContent() {
        HBox content = new HBox(40);  // Increase spacing between panels to 40px
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20, 40, 20, 40));  // Add horizontal padding

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        content.getChildren().addAll(leftPanel, rightPanel);
        return content;
    }

    private VBox createMainLayout(HBox content) {
        Label pageTitle = new Label("Free Definition");
        pageTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 32));
        pageTitle.setTextFill(Color.WHITE);

        Label subtitle = new Label("Create an exclusive Classification System for you");
        subtitle.setFont(Font.font("Microsoft YaHei", 16));
        subtitle.setTextFill(Color.WHITE);

        VBox titleBox = new VBox(5, pageTitle, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: #e6d5ff;");
        titleBox.setPadding(new Insets(25, 0, 25, 0));

        VBox contentWrapper = new VBox(content);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(20, 0, 20, 0));
        contentWrapper.setStyle("-fx-background-color: white;");

        VBox mainLayout = new VBox(0);
        mainLayout.getChildren().addAll(titleBox, contentWrapper);
        mainLayout.setStyle("-fx-background-color: white;");
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