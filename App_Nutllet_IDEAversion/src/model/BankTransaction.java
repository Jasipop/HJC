package model;

public class BankTransaction {
    private final String date;
    private final String description;
    private final String amount;
    private final String type;

    public BankTransaction(String date, String description, String amount, String type) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getAmount() { return amount; }
    public String getType() { return type; }
} 