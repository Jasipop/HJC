/**
 * 
 */
/**
 * 
 */
module Nutllet {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    
    opens Nutllet to javafx.graphics;
    
    exports Nutllet;
}