package bankmanagement;

import bankmanagement.model.BankAccount;

/** Contains the small set of banking operations used by this mini-project. */
public class AccountService {

    public double checkBalance(BankAccount account) {
        return account.getBalance();
    }

    public boolean transfer(BankAccount sender, BankAccount receiver,
                            double amount) {
        if (sender == null || receiver == null) {
            System.out.println("Both accounts are required for a transfer.");
            return false;
        }

        if (sender == receiver) {
            System.out.println("Cannot transfer to the same account.");
            return false;
        }

        if (!sender.isActive() || !receiver.isActive()) {
            System.out.println("Both accounts must be active for a transfer.");
            return false;
        }

        if (amount <= 0) {
            System.out.println("Transfer amount must be greater than 0.");
            return false;
        }

        if (!sender.withdraw(amount,
                "Transfer to " + receiver.getAccountNumber())) {
            return false;
        }

        receiver.deposit(amount, "Transfer from " + sender.getAccountNumber());
        System.out.println("Transfer completed successfully.");
        return true;
    }
}
