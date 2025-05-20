package myapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Discover extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Welcome to Discover");
        VBox root = new VBox(label);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Discover");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
} 