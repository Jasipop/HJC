package service;

import model.BankAccount;
import model.BankTransaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BankDataService {
    private final ObservableList<BankTransaction> transactions;
    private final ObservableList<BankAccount> accounts;

    public BankDataService() {
        this.transactions = FXCollections.observableArrayList();
        this.accounts = FXCollections.observableArrayList(
            new BankAccount("6222 1234 5678 9012", "ICBC"),
            new BankAccount("6217 8888 0000 9999", "Bank of China"),
            new BankAccount("6234 5678 9012 3456", "China Merchants Bank"),
            new BankAccount("6225 4321 9876 5432", "CCB"),
            new BankAccount("6210 1122 3344 5566", "ABC"),
            new BankAccount("6233 6655 4477 8899", "SPDB"),
            new BankAccount("6228 8765 4321 0987", "CMBC"),
            new BankAccount("6216 2233 4455 6677", "Ping An Bank")
        );
    }

    public ObservableList<BankTransaction> getTransactions() {
        return transactions;
    }

    public ObservableList<BankAccount> getAccounts() {
        return accounts;
    }

    public void addAccount(BankAccount account) {
        accounts.add(account);
    }

    public void removeAccount(BankAccount account) {
        accounts.remove(account);
    }

    public void loadTransactionsFromCSV(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("CSV文件不存在: " + path);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            transactions.clear();
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split("\",\"");
                if (parts.length >= 8) {
                    parts[0] = parts[0].replaceFirst("^\"", "");
                    parts[parts.length - 1] = parts[parts.length - 1].replaceFirst("\"$", "");

                    String date = parts[0].trim();
                    String col2 = parts[2].trim();
                    String col3 = parts[3].trim();
                    String rawAmount = parts[5].trim();
                    String col7 = parts[7].trim();

                    String description = (col3.equals("/") || col3.matches("^\\d+$")) ? col2 : col3;
                    String amount = rawAmount.replace("¥", "").trim();
                    String type = col7;

                    transactions.add(new BankTransaction(date, description, amount, type));
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("读取CSV文件失败: " + path, ex);
        }
    }
} 