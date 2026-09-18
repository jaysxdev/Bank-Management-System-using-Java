package bankmanagement.ui;

import bankmanagement.database.AccountDAO;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Simple post-login screen; banking actions are added in the next step. */
public class DashboardFrame extends JFrame {

    private String accountNumber;
    private AccountDAO accountDAO;

    public DashboardFrame(String accountNumber) {
        this.accountNumber = accountNumber;
        this.accountDAO = new AccountDAO();
        setTitle("Bank Management System - Dashboard");
        setSize(420, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        panel.add(new JLabel("Welcome, " + accountNumber,
            SwingConstants.CENTER), BorderLayout.NORTH);

        JButton balanceButton = new JButton("Balance Enquiry");
        balanceButton.addActionListener(event -> showBalance());
        JButton depositButton = new JButton("Deposit Money");
        depositButton.addActionListener(event -> depositMoney());
        JButton withdrawButton = new JButton("Withdraw Money");
        withdrawButton.addActionListener(event -> withdrawMoney());
        JButton transferButton = new JButton("Transfer Money");
        transferButton.addActionListener(event -> transferMoney());
        JButton historyButton = new JButton("Transaction History");
        historyButton.addActionListener(event ->
            new TransactionHistoryFrame(accountNumber).setVisible(true));
        JPanel actions = new JPanel(new GridLayout(5, 1, 10, 10));
        actions.add(balanceButton);
        actions.add(depositButton);
        actions.add(withdrawButton);
        actions.add(transferButton);
        actions.add(historyButton);
        panel.add(actions, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(event -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        panel.add(logoutButton, BorderLayout.SOUTH);
        add(panel);
    }

    private void showBalance() {
        Double balance = accountDAO.getBalance(accountNumber);

        if (balance == null) {
            JOptionPane.showMessageDialog(this,
                "Could not find the account balance.");
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Current balance: ₹" + balance);
    }

    private void depositMoney() {
        String input = JOptionPane.showInputDialog(this,
            "Enter deposit amount:");

        if (input == null) {
            return;
        }

        try {
            double amount = Double.parseDouble(input.trim());
            Double newBalance = accountDAO.deposit(accountNumber, amount);

            if (newBalance == null) {
                JOptionPane.showMessageDialog(this,
                    "Enter a valid positive amount.");
                return;
            }

            JOptionPane.showMessageDialog(this,
                "₹" + amount + " deposited successfully.\n"
                + "New balance: ₹" + newBalance);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this,
                "Enter a valid number.");
        }
    }

    private void withdrawMoney() {
        String input = JOptionPane.showInputDialog(this,
            "Enter withdrawal amount:");

        if (input == null) {
            return;
        }

        try {
            double amount = Double.parseDouble(input.trim());
            Double newBalance = accountDAO.withdraw(accountNumber, amount);

            if (newBalance == null) {
                JOptionPane.showMessageDialog(this,
                    "Withdrawal failed. Check the amount and available balance.");
                return;
            }

            JOptionPane.showMessageDialog(this,
                "₹" + amount + " withdrawn successfully.\n"
                + "New balance: ₹" + newBalance);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this,
                "Enter a valid number.");
        }
    }

    private void transferMoney() {
        String receiverAccount = JOptionPane.showInputDialog(this,
            "Enter receiver account number:");

        if (receiverAccount == null || receiverAccount.trim().isEmpty()) {
            return;
        }

        String input = JOptionPane.showInputDialog(this,
            "Enter transfer amount:");

        if (input == null) {
            return;
        }

        try {
            double amount = Double.parseDouble(input.trim());
            boolean successful = accountDAO.transfer(accountNumber,
                receiverAccount.trim(), amount);

            JOptionPane.showMessageDialog(this, successful
                ? "₹" + amount + " transferred successfully."
                : "Transfer failed. Check account details and balance.");
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this,
                "Enter a valid number.");
        }
    }
}
