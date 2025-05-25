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
    requires com.fasterxml.jackson.databind;
    opens Merge to javafx.fxml, spring.core;
    exports Merge;
}