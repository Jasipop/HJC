/**
 * 
 */
/**
 * 
 */
module Merge {
    // 基础模块声明
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires transitive javafx.graphics;
    requires java.desktop;
    // 对反射框架开放包（如Spring/Netty）
    opens Merge to javafx.fxml, spring.core;
    exports Merge;
}