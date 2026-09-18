package bankmanagement.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Creates the JDBC connection used by the Bank Management System. */
public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/bank_management";
    private static final String USER = "root";
    private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    private DBConnection() {
        // Utility class: no object is needed.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
