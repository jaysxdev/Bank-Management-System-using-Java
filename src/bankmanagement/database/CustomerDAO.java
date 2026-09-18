package bankmanagement.database;

import bankmanagement.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Saves customer details to MySQL. */
public class CustomerDAO {

    public boolean save(Customer customer) {
        String sql = "INSERT INTO customers "
            + "(customer_id, name, date_of_birth, gender, address, phone, "
            + "email, id_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE name = VALUES(name), "
            + "date_of_birth = VALUES(date_of_birth), gender = VALUES(gender), "
            + "address = VALUES(address), phone = VALUES(phone), "
            + "email = VALUES(email), id_number = VALUES(id_number)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customer.getCustomerId());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getDateOfBirth());
            statement.setString(4, customer.getGender());
            statement.setString(5, customer.getAddress());
            statement.setString(6, customer.getPhone());
            statement.setString(7, customer.getEmail());
            statement.setString(8, customer.getIdNumber());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            System.out.println("Could not save customer: "
                               + exception.getMessage());
            return false;
        }
    }
}
