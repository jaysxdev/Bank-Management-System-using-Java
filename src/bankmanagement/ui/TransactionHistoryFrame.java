package bankmanagement.ui;

import bankmanagement.database.TransactionDAO;
import bankmanagement.model.Transaction;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Displays the logged-in account's transactions saved in MySQL. */
public class TransactionHistoryFrame extends JFrame {

    public TransactionHistoryFrame(String accountNumber) {
        setTitle("Transaction History - " + accountNumber);
        setSize(650, 400);
        setLocationRelativeTo(null);

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        historyArea.setText(createHistory(accountNumber));

        add(new JScrollPane(historyArea), BorderLayout.CENTER);
    }

    private String createHistory(String accountNumber) {
        List<Transaction> transactions =
            new TransactionDAO().getTransactions(accountNumber);

        if (transactions.isEmpty()) {
            return "No transactions found.";
        }

        StringBuilder history = new StringBuilder();
        for (Transaction transaction : transactions) {
            history.append("Date: ")
                .append(transaction.getTransactionDate())
                .append("\nType: ")
                .append(transaction.getTransactionType())
                .append("\nAmount: ₹")
                .append(transaction.getAmount())
                .append("\nBalance After: ₹")
                .append(transaction.getBalanceAfterTransaction())
                .append("\nDescription: ")
                .append(transaction.getDescription())
                .append("\n----------------------------------------\n");
        }
        return history.toString();
    }
}
