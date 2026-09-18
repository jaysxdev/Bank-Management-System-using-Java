package bankmanagement.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BankAccount {

    private String accountNumber;
    private Customer customer;
    private double balance;
    private String pin;
    private String status;
    private List<Transaction> transactionHistory;

    public BankAccount(String accountNumber,
                       Customer customer,
                       double balance,
                       String pin) {

        this.accountNumber = accountNumber;
        this.customer = customer;
        this.balance = balance;
        this.pin = pin;
        this.status = "ACTIVE";
        this.transactionHistory = new ArrayList<>();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getBalance() {
        return balance;
    }

    public String getPin() {
        return pin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean deposit(double amount) {
        return deposit(amount, "Cash deposit");
    }

    public boolean deposit(double amount, String description) {

        if (!isActive()) {
            System.out.println("Account is not active.");
            return false;
        }

        if (amount <= 0) {
            System.out.println("Deposit amount must be greater than 0.");
            return false;
        }

        balance += amount;
        addTransaction("DEPOSIT", amount, description);
        System.out.println("₹" + amount + " deposited successfully.");
        return true;
    }

    protected void reduceBalance(double amount) {
        balance -= amount;
    }

    protected void addTransaction(String type, double amount,
                                  String description) {
        String transactionId = "TXN" + (transactionHistory.size() + 1);
        transactionHistory.add(new Transaction(transactionId, accountNumber,
                                               type, amount, balance,
                                               description));
    }

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    public void displayTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        for (Transaction transaction : transactionHistory) {
            transaction.displayTransaction();
        }
    }

    public abstract boolean withdraw(double amount);

    public abstract boolean withdraw(double amount, String description);

    public abstract double calculateInterest();

    public void displayAccountDetails() {

        System.out.println("Account Number : " + accountNumber);
        System.out.println("Customer Name  : " + customer.getName());
        System.out.println("Account Status : " + status);
        System.out.println("Balance        : ₹" + balance);
    }
}
