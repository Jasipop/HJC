module com.example.international {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.international to javafx.fxml;
    exports com.example.international;
}