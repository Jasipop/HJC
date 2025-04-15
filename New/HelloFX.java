import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        // 创建一个简单的 JavaFX 界面
        Label label = new Label("Hello, JavaFX 21!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);

        stage.setTitle("JavaFX Test Window");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // 启动 JavaFX 应用
    }
}