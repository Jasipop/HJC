package view.components;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.BankTransaction;
import javafx.collections.ObservableList;

public class TransactionTable extends TableView<BankTransaction> {
    
    public TransactionTable(ObservableList<BankTransaction> transactions) {
        super(transactions);
        setupTable();
    }

    private void setupTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setStyle(
            "-fx-font-size: 15px; " +
            "-fx-table-cell-size: 40px; " +
            "-fx-selection-bar: #d1c4e9; " +
            "-fx-selection-bar-non-focused: #b39ddb;" +
            "-fx-text-fill: black;" +
            "-fx-font-family: 'Microsoft YaHei';"
        );

        TableColumn<BankTransaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<BankTransaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<BankTransaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<BankTransaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        getColumns().addAll(dateCol, descCol, amountCol, typeCol);

        // 设置列样式
        getColumns().forEach(col -> 
            col.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: black;")
        );
    }
} 