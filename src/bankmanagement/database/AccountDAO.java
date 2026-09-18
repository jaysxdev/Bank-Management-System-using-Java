package bankmanagement.database;

import bankmanagement.model.BankAccount;
import bankmanagement.model.CurrentAccount;
import bankmanagement.model.SavingsAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/** Saves savings and current accounts to MySQL. */
public class AccountDAO {

    public boolean save(BankAccount account) {
        String sql = "INSERT INTO accounts "
            + "(account_number, customer_id, account_type, balance, pin, "
            + "status, interest_rate, overdraft_limit) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE customer_id = VALUES(customer_id), "
            + "account_type = VALUES(account_type), balance = VALUES(balance), "
            + "pin = VALUES(pin), status = VALUES(status), "
            + "interest_rate = VALUES(interest_rate), "
            + "overdraft_limit = VALUES(overdraft_limit)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getAccountNumber());
            statement.setInt(2, account.getCustomer().getCustomerId());
            statement.setString(3, getAccountType(account));
            statement.setDouble(4, account.getBalance());
            statement.setString(5, account.getPin());
            statement.setString(6, account.getStatus());
            setAccountSpecificValues(statement, account);
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            System.out.println("Could not save account: "
                               + exception.getMessage());
            return false;
        }
    }

    public boolean authenticate(String accountNumber, String pin) {
        String sql = "SELECT account_number FROM accounts "
            + "WHERE account_number = ? AND pin = ? AND status = 'ACTIVE'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);
            statement.setString(2, pin);
            return statement.executeQuery().next();
        } catch (SQLException exception) {
            System.out.println("Could not verify login: "
                               + exception.getMessage());
            return false;
        }
    }

    public Double getBalance(String accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);
            ResultSet result = statement.executeQuery();
            return result.next() ? result.getDouble("balance") : null;
        } catch (SQLException exception) {
            System.out.println("Could not read balance: "
                               + exception.getMessage());
            return null;
        }
    }

    public Double deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            return null;
        }

        String updateSql = "UPDATE accounts SET balance = balance + ? "
            + "WHERE account_number = ? AND status = 'ACTIVE'";
        String transactionSql = "INSERT INTO transactions "
            + "(account_number, transaction_type, amount, "
            + "balance_after_transaction, transaction_date, description) "
            + "VALUES (?, 'DEPOSIT', ?, ?, ?, 'Deposit through GUI')";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement update =
                    connection.prepareStatement(updateSql)) {
                update.setDouble(1, amount);
                update.setString(2, accountNumber);

                if (update.executeUpdate() == 0) {
                    connection.rollback();
                    return null;
                }
            }

            Double newBalance = getBalance(connection, accountNumber);

            try (PreparedStatement transaction =
                    connection.prepareStatement(transactionSql)) {
                transaction.setString(1, accountNumber);
                transaction.setDouble(2, amount);
                transaction.setDouble(3, newBalance);
                transaction.setTimestamp(4,
                    new Timestamp(System.currentTimeMillis()));
                transaction.executeUpdate();
            }

            connection.commit();
            return newBalance;
        } catch (SQLException exception) {
            System.out.println("Could not deposit money: "
                               + exception.getMessage());
            return null;
        }
    }

    public Double withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            return null;
        }

        String updateSql = "UPDATE accounts SET balance = balance - ? "
            + "WHERE account_number = ? AND status = 'ACTIVE' AND "
            + "((account_type = 'SAVINGS' AND balance >= ?) OR "
            + "(account_type = 'CURRENT' "
            + "AND balance + overdraft_limit >= ?))";
        String transactionSql = "INSERT INTO transactions "
            + "(account_number, transaction_type, amount, "
            + "balance_after_transaction, transaction_date, description) "
            + "VALUES (?, 'WITHDRAW', ?, ?, ?, 'Withdrawal through GUI')";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement update =
                    connection.prepareStatement(updateSql)) {
                update.setDouble(1, amount);
                update.setString(2, accountNumber);
                update.setDouble(3, amount);
                update.setDouble(4, amount);

                if (update.executeUpdate() == 0) {
                    connection.rollback();
                    return null;
                }
            }

            Double newBalance = getBalance(connection, accountNumber);

            try (PreparedStatement transaction =
                    connection.prepareStatement(transactionSql)) {
                transaction.setString(1, accountNumber);
                transaction.setDouble(2, amount);
                transaction.setDouble(3, newBalance);
                transaction.setTimestamp(4,
                    new Timestamp(System.currentTimeMillis()));
                transaction.executeUpdate();
            }

            connection.commit();
            return newBalance;
        } catch (SQLException exception) {
            System.out.println("Could not withdraw money: "
                               + exception.getMessage());
            return null;
        }
    }

    public boolean transfer(String senderAccount, String receiverAccount,
                            double amount) {
        if (amount <= 0 || senderAccount.equals(receiverAccount)) {
            return false;
        }

        String debitSql = "UPDATE accounts SET balance = balance - ? "
            + "WHERE account_number = ? AND status = 'ACTIVE' AND "
            + "((account_type = 'SAVINGS' AND balance >= ?) OR "
            + "(account_type = 'CURRENT' "
            + "AND balance + overdraft_limit >= ?))";
        String creditSql = "UPDATE accounts SET balance = balance + ? "
            + "WHERE account_number = ? AND status = 'ACTIVE'";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement debit = connection.prepareStatement(debitSql);
                 PreparedStatement credit = connection.prepareStatement(creditSql)) {

                debit.setDouble(1, amount);
                debit.setString(2, senderAccount);
                debit.setDouble(3, amount);
                debit.setDouble(4, amount);

                if (debit.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                credit.setDouble(1, amount);
                credit.setString(2, receiverAccount);

                if (credit.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }

            saveTransferTransaction(connection, senderAccount, "TRANSFER_OUT",
                amount, getBalance(connection, senderAccount),
                "Transfer to " + receiverAccount);
            saveTransferTransaction(connection, receiverAccount, "TRANSFER_IN",
                amount, getBalance(connection, receiverAccount),
                "Transfer from " + senderAccount);

            connection.commit();
            return true;
        } catch (SQLException exception) {
            System.out.println("Could not transfer money: "
                               + exception.getMessage());
            return false;
        }
    }

    private void saveTransferTransaction(Connection connection,
                                         String accountNumber, String type,
                                         double amount, double balance,
                                         String description) throws SQLException {
        String sql = "INSERT INTO transactions "
            + "(account_number, transaction_type, amount, "
            + "balance_after_transaction, transaction_date, description) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            statement.setString(2, type);
            statement.setDouble(3, amount);
            statement.setDouble(4, balance);
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            statement.setString(6, description);
            statement.executeUpdate();
        }
    }

    private Double getBalance(Connection connection, String accountNumber)
                              throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            ResultSet result = statement.executeQuery();
            return result.next() ? result.getDouble("balance") : null;
        }
    }

    private String getAccountType(BankAccount account) {
        return account instanceof SavingsAccount ? "SAVINGS" : "CURRENT";
    }

    private void setAccountSpecificValues(PreparedStatement statement,
                                          BankAccount account)
                                          throws SQLException {
        if (account instanceof SavingsAccount savingsAccount) {
            statement.setDouble(7, savingsAccount.getInterestRate());
            statement.setNull(8, Types.DECIMAL);
        } else if (account instanceof CurrentAccount currentAccount) {
            statement.setNull(7, Types.DECIMAL);
            statement.setDouble(8, currentAccount.getOverdraftLimit());
        }
    }
}
