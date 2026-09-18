package bankmanagement.ui;

import bankmanagement.database.AccountDAO;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** The first screen of the Bank Management System. */
public class LoginFrame extends JFrame {

    private JTextField accountNumberField;
    private JPasswordField pinField;
    private AccountDAO accountDAO;

    public LoginFrame() {
        accountDAO = new AccountDAO();
        setTitle("Bank Management System - Login");
        setSize(420, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        add(createContent());
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Bank Management System", SwingConstants.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.WEST;

        accountNumberField = new JTextField(16);
        pinField = new JPasswordField(16);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> login());
        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(event ->
            new CreateAccountFrame().setVisible(true));

        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(new JLabel("Account Number:"), constraints);
        constraints.gridx = 1;
        form.add(accountNumberField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        form.add(new JLabel("PIN:"), constraints);
        constraints.gridx = 1;
        form.add(pinField, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        form.add(loginButton, constraints);

        constraints.gridy = 3;
        form.add(createAccountButton, constraints);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private void login() {
        String accountNumber = accountNumberField.getText().trim();
        String pin = new String(pinField.getPassword());

        if (accountNumber.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Enter both account number and PIN.");
            return;
        }

        if (accountDAO.authenticate(accountNumber, pin)) {
            new DashboardFrame(accountNumber).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid account number, PIN, or inactive account.");
            pinField.setText("");
        }
    }
}
