package bankmanagement.database;

import bankmanagement.model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Saves each banking transaction to MySQL. */
public class TransactionDAO {

    public boolean save(Transaction transaction) {
        String sql = "INSERT INTO transactions "
            + "(account_number, transaction_type, amount, "
            + "balance_after_transaction, transaction_date, description) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, transaction.getAccountNumber());
            statement.setString(2, transaction.getTransactionType());
            statement.setDouble(3, transaction.getAmount());
            statement.setDouble(4, transaction.getBalanceAfterTransaction());
            statement.setTimestamp(5,
                Timestamp.valueOf(transaction.getTransactionDate()));
            statement.setString(6, transaction.getDescription());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            System.out.println("Could not save transaction: "
                               + exception.getMessage());
            return false;
        }
    }

    public List<Transaction> getTransactions(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? "
            + "ORDER BY transaction_date";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                transactions.add(new Transaction(
                    "DB" + result.getInt("transaction_id"),
                    result.getString("account_number"),
                    result.getString("transaction_type"),
                    result.getDouble("amount"),
                    result.getDouble("balance_after_transaction"),
                    result.getTimestamp("transaction_date").toLocalDateTime(),
                    result.getString("description")
                ));
            }
        } catch (SQLException exception) {
            System.out.println("Could not read transactions: "
                               + exception.getMessage());
        }

        return transactions;
    }
}
