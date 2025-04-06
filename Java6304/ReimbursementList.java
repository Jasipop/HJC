import javafx.animation.ScaleTransition;
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
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class ReimbursementList extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Main layout - using StackPane as root to overlay the button
        StackPane root = new StackPane();

        // Original main layout
        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);
        mainLayout.setStyle("-fx-background-color: #FFD4EC54;");

        // Title
        Text title = new Text("Reimbursements Items");
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

        // Reimbursement items
        VBox itemsContainer = new VBox();
        itemsContainer.setSpacing(10);
        itemsContainer.setAlignment(Pos.CENTER);

        // Add items
        itemsContainer.getChildren().addAll(
                createItem("Transportation", "Subway", "6", "Mar 5, 2025"),
                createItem("Catering", "Starbucks", "120", "Mar 5, 2025"),
                createItem("Transportation", "Subway", "4", "Mar 9, 2025"),
                createItem("Transportation", "Taxi", "37", "Mar 15, 2025"),
                createItem("Transportation", "Taxi", "48", "Mar 16, 2025"),
                createItem("Transportation", "Taxi", "15", "Mar 10, 2025")
        );

        // Add all components to main layout
        mainLayout.getChildren().addAll(titleBox, searchBox, itemsContainer);


        // Create the circular "+" button
        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-color: " + Color.web("#855FAF").toString().replace("0x", "#") +
                "; -fx-text-fill: white; -fx-font-size: 27px; -fx-font-weight: bold;");
        addButton.setShape(new Circle(30)); // Makes the button circular
        addButton.setMinSize(60, 60);
        addButton.setMaxSize(60, 60);

        // Add scale transition animation to the button
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.3), addButton);
        addButton.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.2);  // Increase size by 20%
            scaleTransition.setToY(1.2);  // Increase size by 20%
            scaleTransition.play();
        });
        addButton.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);  // Reset size to original
            scaleTransition.setToY(1.0);  // Reset size to original
            scaleTransition.play();
        });

        // Position the button in the bottom right
        StackPane.setAlignment(addButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 20, 20, 0));

        // Add action handler (navigation to be implemented)
        addButton.setOnAction(e -> {
            System.out.println("Add button clicked - navigation to be implemented");
            // Here you would add the navigation logic to another page
        });

        // Add both layouts to the root
        root.getChildren().addAll(mainLayout, addButton);

        // Set up scene
        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setTitle("Reimbursements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createItem(String category, String detail, String amount, String date) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(15);
        itemBox.setPadding(new Insets(15));
        itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
        itemBox.setMaxWidth(800);

        // Amount label (bold)
        Text amountLabel = new Text("-" + amount);
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        amountLabel.setFill(Color.RED);

        // Category and detail HBox
        HBox categoryDetailBox = new HBox();
        categoryDetailBox.setSpacing(10);
        categoryDetailBox.setAlignment(Pos.CENTER_LEFT);

        // Category and date VBox
        VBox detailsBox = new VBox();
        detailsBox.setSpacing(5);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        // Category label (bold)
        Text categoryLabel = new Text(category);
        categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        categoryLabel.setFill(Color.web("#2c3e50"));

        // Detail label
        Text detailLabel = new Text(detail);
        detailLabel.setFont(Font.font("Arial", 16));
        detailLabel.setFill(Color.web("#2c3e50"));

        categoryDetailBox.getChildren().addAll(categoryLabel, detailLabel);

        // Date label
        Text dateLabel = new Text(date);
        dateLabel.setFont(Font.font("Arial", 14));
        dateLabel.setFill(Color.web("#7f8c8d"));

        detailsBox.getChildren().addAll(categoryDetailBox, dateLabel);

        // Create star-shaped toggle button
        ToggleButton starButton = createStarToggleButton();

        // Add all components to item box
        itemBox.getChildren().addAll(amountLabel, detailsBox, starButton);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        return itemBox;
    }

    private ToggleButton createStarToggleButton() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setStyle("-fx-background-color: transparent; -fx-padding: 5;");

        // Create star shape
        SVGPath star = new SVGPath();
        star.setContent("M12,17.27L18.18,21l-1.64-7.03L22,9.24l-7.19-0.61L12,2L9.19,8.63L2,9.24l5.46,4.73L5.82,21L12,17.27z");

        // Set styles
        star.setFill(Color.web("#bdc3c7")); // Default gray color
        star.setStroke(Color.web("#bdc3c7"));

        // Bind color to selected state
        toggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                star.setFill(Color.web("#FFD700")); // Gold color when selected
                star.setStroke(Color.web("#FFD700"));
            } else {
                star.setFill(Color.web("#bdc3c7")); // Gray color when not selected
                star.setStroke(Color.web("#bdc3c7"));
            }
        });

        toggleButton.setGraphic(star);
        return toggleButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
