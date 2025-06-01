package model;

public class BankAccount {
    private final String accountNumber;
    private final String bankName;

    public BankAccount(String accountNumber, String bankName) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getBankName() { return bankName; }
} 