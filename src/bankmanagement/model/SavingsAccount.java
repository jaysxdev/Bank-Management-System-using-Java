package bankmanagement.model;

public class SavingsAccount extends BankAccount {

    private double interestRate;

    public SavingsAccount(String accountNumber,
                          Customer customer,
                          double balance,
                          String pin,
                          double interestRate) {

        super(accountNumber, customer, balance, pin);
        this.interestRate = interestRate;
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
            System.out.println("Withdrawal amount must be greater than 0.");
            return false;
        }

        if (amount > getBalance()) {
            System.out.println("Insufficient balance.");
            return false;
        }

        reduceBalance(amount);
        addTransaction("WITHDRAW", amount, description);
        System.out.println("₹" + amount + " withdrawn successfully.");
        return true;
    }

    @Override
    public double calculateInterest() {
        return getBalance() * interestRate / 100;
    }

    public double getInterestRate() {
        return interestRate;
    }
}
