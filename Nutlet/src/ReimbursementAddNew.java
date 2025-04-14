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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReimbursementAddNew extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Add New Reimbursement Item");

        VBox root = new VBox(15);
        root.setPadding(new Insets(25, 30, 25, 30));
        root.setStyle("-fx-background-color: #FFD4EC54;");

        Text title = new Text("Add New Reimbursement Item");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web("#855FAF"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);

        Text requiredNote = new Text("* Required fields");
        requiredNote.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        requiredNote.setFill(Color.web("#e74c3c"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(15, 0, 15, 0));

        Label titleLabel = new Label("Reimbursement Title *");
        titleLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        TextField titleField = new TextField();
        titleField.setPromptText("Name the reimbursement incident.");
        titleField.setText("Transportation");
        titleField.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        formGrid.addRow(0, titleLabel, titleField);

        Label reimbursableLabel = new Label("Is it reimbursable? *");
        reimbursableLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        ToggleGroup reimbursableGroup = new ToggleGroup();
        RadioButton yesRadio = new RadioButton("Yes, needs a reminder.");
        yesRadio.setToggleGroup(reimbursableGroup);
        yesRadio.setStyle("-fx-text-fill: #34495e; -fx-font-size: 16px;");
        yesRadio.setSelected(true);
        RadioButton noRadio = new RadioButton("No, just record.");
        noRadio.setToggleGroup(reimbursableGroup);
        noRadio.setStyle("-fx-text-fill: #34495e; -fx-font-size: 16px;");
        VBox radioBox = new VBox(8, yesRadio, noRadio);
        formGrid.addRow(1, reimbursableLabel, radioBox);

        Label dateLabel = new Label("Estimated Date of reimbursement Arrival");
        dateLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px;");
        DatePicker datePicker = new DatePicker();
        datePicker.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        datePicker.setValue(LocalDate.of(2025, 3, 30));
        datePicker.setPromptText("Select date");
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }
            @Override public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        });
        formGrid.addRow(2, dateLabel, datePicker);

        Text amountSectionTitle = new Text("Reimbursement Amount *");
        amountSectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        amountSectionTitle.setFill(Color.web("#2c3e50"));

        Text amountDescription = new Text("Fill in the amount corresponding to the reimbursement reminder payment.");
        amountDescription.setFont(Font.font("Arial", 16));
        amountDescription.setFill(Color.web("#7f8c8d"));

        TextField amountField = new TextField();
        amountField.setPromptText("0");
        amountField.setPrefWidth(300);
        amountField.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; -fx-font-size: 16px; -fx-pref-height: 40px;");

        Text notesTitle = new Text("Notes");
        notesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        notesTitle.setFill(Color.web("#2c3e50"));

        TextField notes = new TextField();
        notes.setPromptText("Click to add notes.");
        notes.setPrefWidth(300);
        notes.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; -fx-font-size: 16px; -fx-pref-height: 40px;");

        Label responsiblePersonLabel = new Label("Reimbursement Responsible Person");
        responsiblePersonLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 16px;");

        Text responsiblePersonDescription = new Text("Select the reimbursement contact person.");
        responsiblePersonDescription.setFont(Font.font("Arial", 16));
        responsiblePersonDescription.setFill(Color.web("#7f8c8d"));

        ComboBox<String> personComboBox = new ComboBox<>();
        personComboBox.setPromptText("Financial Office");
        personComboBox.getItems().addAll("Financial Office", "The Accountant", "Direct Superior");
        personComboBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-font-size: 16px; -fx-pref-height: 40px;");

        HBox financialOfficeButtons = new HBox(15);
        Button clearButton = new Button("Clear all");
        clearButton.setStyle("-fx-background-color: #855faf; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 120px; -fx-pref-height: 40px;");
        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #71b6c5; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 120px; -fx-pref-height: 40px;");
        financialOfficeButtons.getChildren().addAll(clearButton, confirmButton);
        financialOfficeButtons.setAlignment(Pos.BOTTOM_RIGHT);

        // Button hover effects
        applyHoverScale(clearButton);
        applyHoverScale(confirmButton);

        clearButton.setOnAction(e -> {
            titleField.clear();
            reimbursableGroup.selectToggle(null);
            datePicker.setValue(LocalDate.of(2025, 3, 30));
            amountField.setText("0");
            personComboBox.getSelectionModel().clearSelection();
        });

        confirmButton.setOnAction(e -> {
            System.out.println("Form submitted with date: " + datePicker.getValue().format(formatter));
        });

        root.getChildren().addAll(
                titleBox,
                requiredNote,
                formGrid,
                amountSectionTitle,
                amountDescription,
                amountField,
                notesTitle,
                notes,
                responsiblePersonLabel,
                responsiblePersonDescription,
                personComboBox,
                financialOfficeButtons
        );

        Scene scene = new Scene(root, 1366, 768);
        primaryStage.setScene(scene);

        // Entry animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        TranslateTransition slideInForm = new TranslateTransition(Duration.millis(800), formGrid);
        slideInForm.setFromY(50);
        slideInForm.setToY(0);
        slideInForm.play();

        FadeTransition fadeInTitle = new FadeTransition(Duration.millis(800), titleBox);
        fadeInTitle.setFromValue(0);
        fadeInTitle.setToValue(1);
        fadeInTitle.play();

        primaryStage.show();
    }

    private void applyHoverScale(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);
            scaleUp.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
