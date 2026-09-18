package bankmanagement.model;

import java.time.LocalDateTime;

public class Transaction {

    private String transactionId;
    private String accountNumber;
    private String transactionType;
    private double amount;
    private double balanceAfterTransaction;
    private LocalDateTime transactionDate;
    private String description;

    public Transaction(String transactionId,
                       String accountNumber,
                       String transactionType,
                       double amount,
                       double balanceAfterTransaction,
                       String description) {

        this(transactionId, accountNumber, transactionType, amount,
             balanceAfterTransaction, LocalDateTime.now(), description);
    }

    public Transaction(String transactionId,
                       String accountNumber,
                       String transactionType,
                       double amount,
                       double balanceAfterTransaction,
                       LocalDateTime transactionDate,
                       String description) {

        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void displayTransaction() {

        System.out.println("----------------------------------------");
        System.out.println("Transaction ID : " + transactionId);
        System.out.println("Account Number : " + accountNumber);
        System.out.println("Type           : " + transactionType);
        System.out.println("Amount         : ₹" + amount);
        System.out.println("Balance After  : ₹" + balanceAfterTransaction);
        System.out.println("Date           : " + transactionDate);
        System.out.println("Description    : " + description);
        System.out.println("----------------------------------------");
    }
}
