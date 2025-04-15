import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Discover extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setBackground(new Background(new BackgroundFill(
                Color.web("#FFD4EC", 0.3)
                , new CornerRadii(0), Insets.EMPTY)));

        Text title = new Text("Discover");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(750);
        searchField.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 4; -fx-padding: 10 15; -fx-min-height: 40px; -fx-font-size: 16px;");
        searchField.setAlignment(Pos.CENTER);

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #7f8c8d;");

        HBox searchBox = new HBox(10, searchIcon, searchField);
        searchBox.setPrefWidth(800);
        searchBox.setAlignment(Pos.CENTER);

        VBox itemsContainer = new VBox();
        itemsContainer.setSpacing(15);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefHeight(600); // 控制高度范围

        String[] pastelColors = {
                "#FFB6C1E6", // #FFB6C1 with alpha 0.9
                "#FFDAB9E6", // #FFDAB9 with alpha 0.9
                "#FFFACDE6", // #FFFACD with alpha 0.9
                "#E0FFFFE6", // #E0FFFF with alpha 0.9
                "#D8BFD8E6", // #D8BFD8 with alpha 0.9
                "#C6E2FFE6", // #C6E2FF with alpha 0.9
                "#E6E6FAE6"  // #E6E6FA with alpha 0.9
        };

        String[] titles = {
                "Enterprise Edition",
                "MailBox",
                "Reminders",
                "Reimbursement Items",
                "Financial Analysis",
                "Transaction Management",
                "Bank Data Management",
                "Expenditure Classification System",
                "Seasonal Spikes",
                "International Currency Exchange",
                "AI Intelligent Classification",
        };

        String[] descriptions = {
                "Open the Enterprise Edition",
                "Get app updates and admin notifications",
                "Set budget alerts and reminders",
                "Track reimbursable/non-reimbursable funds",
                "Analyze trends and predict cash flow",
                "Manage all financial transactions",
                "Import bank CSV files for reconciliation",
                "Auto-categorize expenses with custom options",
                "Special date budget alerts",
                "Real-time multi-currency tracking",
                "AI transaction categorization with manual edits",
        };

        for (int i = 0; i < titles.length; i++) {
            Button btn = createSettingButton(primaryStage, titles[i], descriptions[i], pastelColors[i % pastelColors.length]);
            addHoverAnimation(btn);
            itemsContainer.getChildren().add(btn);
        }

        mainLayout.getChildren().addAll(titleBox, searchBox, scrollPane);

        root.getChildren().addAll(mainLayout);

        // Animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1), mainLayout);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), mainLayout);
        slide.setFromY(50);
        slide.setToY(0);

        fade.play();
        slide.play();

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Settings");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void addHoverAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });
    }

    private String getEmojiForTitle(String title) {
        return switch (title) {
            case "Enterprise Edition" ->"👔";
            case "MailBox" -> "📬";
            case "Reminders" -> "⏰";
            case "Reimbursement Items" -> "💸";
            case "Financial Analysis" -> "📊";
            case "Transaction Management" -> "💳";
            case "Bank Data Management" -> "🏦";
            case "Expenditure Classification System" -> "👓️";
            case "Seasonal Spikes" -> "📈";
            case "International Currency Exchange" -> "💱";
            case "AI Intelligent Classification" -> "🤖";
            default -> "⚙️";
        };
    }

    private Button createSettingButton(Stage primaryStage, String title, String description, String bgColor) {
        Button button = new Button();
        button.setMaxWidth(800);
        button.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 15 20; -fx-font-size: 16px; -fx-text-alignment: left;");

        Label emojiLabel = new Label(getEmojiForTitle(title));
        emojiLabel.setFont(Font.font("Arial", 24));
        emojiLabel.setPadding(new Insets(0, 5, 0, 0));

        VBox textContent = new VBox();
        textContent.setSpacing(5);
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#2c3e50"));

        Text descriptionText = new Text(description);
        descriptionText.setFont(Font.font("Arial", 14));
        descriptionText.setFill(Color.web("#7f8c8d"));

        textContent.getChildren().addAll(titleText, descriptionText);

        HBox graphicBox = new HBox(10, emojiLabel, textContent);
        graphicBox.setAlignment(Pos.CENTER_LEFT);

        button.setGraphic(graphicBox);
        button.setOnAction(e -> openNewPage(primaryStage, title));

        return button;
    }

    private void openNewPage(Stage primaryStage, String pageTitle) {
        try {
            switch (pageTitle) {
//                case "Enterprise Edition" -> new EnterPrise().start(new Stage());
                case "MailBox" -> new Mailbox().start(new Stage());
//                case "Reminders" -> new Reminders().start(new Stage());
                case "Reimbursement Items" -> new ReimbursementList().start(new Stage());
                case "Financial Analysis" -> new FinancialAnalysis().start(new Stage());
//                case "Transaction Management" -> new TransactionManager().start(new Stage());
//                case "Bank Data Management" -> new BankDataManager().start(new Stage());
//                case "Expenditure Classification System" -> new ExpenditureClassifier().start(new Stage());
//                case "Seasonal Spikes" -> new SeasonalAnalysis().start(new Stage());
//                case "International Currency Exchange" -> new CurrencyExchangeViewer().start(new Stage());
//                case "AI Intelligent Classification" -> new AIClassifier().start(new Stage());
                default -> showDefaultWelcomePage(primaryStage, pageTitle); // ← 用默认页兜底
            }
//            primaryStage.close(); // 是否关闭页面。全做完了才关。
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDefaultWelcomePage(Stage primaryStage, String pageTitle) {
        VBox newPageLayout = new VBox();
        newPageLayout.setAlignment(Pos.CENTER);
        newPageLayout.setSpacing(20);
        newPageLayout.setStyle("-fx-background-color: #FFFBE6;");
        Text label = new Text("Welcome to: " + pageTitle);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        label.setFill(Color.web("#855FAF"));

        Button backBtn = new Button("\u2190 Back");
        backBtn.setStyle("-fx-background-color: #855FAF; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 6;");
        backBtn.setOnAction(e -> start(primaryStage));

        newPageLayout.getChildren().addAll(label, backBtn);
        Scene newScene = new Scene(newPageLayout, 1366, 768);
        primaryStage.setScene(newScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}