package bankmanagement.ui;

import bankmanagement.database.AccountDAO;
import bankmanagement.database.CustomerDAO;
import bankmanagement.model.CurrentAccount;
import bankmanagement.model.Customer;
import bankmanagement.model.SavingsAccount;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Creates a basic customer and account for this mini-project. */
public class CreateAccountFrame extends JFrame {

    private JTextField customerIdField = new JTextField(16);
    private JTextField nameField = new JTextField(16);
    private JTextField accountNumberField = new JTextField(16);
    private JComboBox<String> accountTypeBox =
        new JComboBox<>(new String[] {"Savings", "Current"});
    private JTextField openingBalanceField = new JTextField(16);
    private JPasswordField pinField = new JPasswordField(16);

    public CreateAccountFrame() {
        setTitle("Bank Management System - Create Account");
        setSize(440, 390);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        add(createForm());
    }

    private javax.swing.JPanel createForm() {
        javax.swing.JPanel panel = new javax.swing.JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 7, 7, 7);
        constraints.anchor = GridBagConstraints.WEST;

        addRow(panel, constraints, 0, "Customer ID:", customerIdField);
        addRow(panel, constraints, 1, "Customer Name:", nameField);
        addRow(panel, constraints, 2, "Account Number:", accountNumberField);
        addRow(panel, constraints, 3, "Account Type:", accountTypeBox);
        addRow(panel, constraints, 4, "Opening Balance:", openingBalanceField);
        addRow(panel, constraints, 5, "PIN:", pinField);

        JButton createButton = new JButton("Create Account");
        createButton.addActionListener(event -> createAccount());
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(createButton, constraints);
        return panel;
    }

    private void addRow(javax.swing.JPanel panel,
                        GridBagConstraints constraints, int row,
                        String label, java.awt.Component field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        panel.add(field, constraints);
    }

    private void createAccount() {
        try {
            int customerId = Integer.parseInt(customerIdField.getText().trim());
            String name = nameField.getText().trim();
            String accountNumber = accountNumberField.getText().trim();
            double openingBalance =
                Double.parseDouble(openingBalanceField.getText().trim());
            String pin = new String(pinField.getPassword());

            if (name.isEmpty() || accountNumber.isEmpty() || pin.isEmpty()
                    || openingBalance < 0) {
                showError("Complete all fields with a valid opening balance.");
                return;
            }

            Customer customer = new Customer(customerId, name, "",
                "Not specified", "", "", "", "ID" + customerId);
            CustomerDAO customerDAO = new CustomerDAO();
            AccountDAO accountDAO = new AccountDAO();

            if (!customerDAO.save(customer)) {
                showError("Could not save customer details.");
                return;
            }

            boolean isSavings = accountTypeBox.getSelectedIndex() == 0;
            boolean accountSaved = isSavings
                ? accountDAO.save(new SavingsAccount(accountNumber, customer,
                    openingBalance, pin, 4.0))
                : accountDAO.save(new CurrentAccount(accountNumber, customer,
                    openingBalance, pin, 10000.0));

            if (accountSaved) {
                JOptionPane.showMessageDialog(this,
                    "Account created successfully. You can now log in.");
                dispose();
            } else {
                showError("Could not save account details.");
            }
        } catch (NumberFormatException exception) {
            showError("Customer ID and opening balance must be numbers.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
