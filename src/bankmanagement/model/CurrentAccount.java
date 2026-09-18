package bankmanagement.model;

public class CurrentAccount extends BankAccount {

    private double overdraftLimit;

    public CurrentAccount(String accountNumber,
                          Customer customer,
                          double balance,
                          String pin,
                          double overdraftLimit) {

        super(accountNumber, customer, balance, pin);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public boolean withdraw(double amount) {
        return withdraw(amount, "Cash withdrawal");
    }

    @Override
    public boolean withdraw(double amount, String description) {

        if (!isActive()) {
            System.out.println("Account is not active.");
            return false;
        }

        if (amount <= 0) {
            System.out.println(
                "Withdrawal amount must be greater than 0."
            );
            return false;
        }

        if (amount > getBalance() + overdraftLimit) {
            System.out.println(
                "Withdrawal exceeds overdraft limit."
            );
            return false;
        }

        reduceBalance(amount);
        addTransaction("WITHDRAW", amount, description);

        System.out.println(
            "₹" + amount + " withdrawn successfully."
        );
        return true;
    }

    @Override
    public double calculateInterest() {
        return 0;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }
}
