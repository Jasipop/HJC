/**
 * 
 */
/**
 * 
 */
module Merge {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires transitive javafx.graphics;
    requires javafx.web;
    requires java.desktop;
    opens Merge to javafx.fxml, spring.core;
    exports Merge;
}