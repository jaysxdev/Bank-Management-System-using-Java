package bankmanagement.web;

import bankmanagement.database.AccountDAO;
import bankmanagement.database.DBConnection;
import bankmanagement.database.TransactionDAO;
import bankmanagement.model.Transaction;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Small local web server that connects the website to the MySQL database. */
public class BankWebServer {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public static void start() throws IOException {
        BankWebServer application = new BankWebServer();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", application::serveWebsite);
        server.createContext("/api/login", application::login);
        server.createContext("/api/account", application::account);
        server.createContext("/api/deposit", application::deposit);
        server.createContext("/api/withdraw", application::withdraw);
        server.createContext("/api/transfer", application::transfer);
        server.createContext("/api/transactions", application::transactions);
        server.createContext("/api/create", application::createAccount);
        server.setExecutor(null);
        server.start();
        System.out.println("Website is running at http://localhost:8080");
    }

    private void serveWebsite(HttpExchange exchange) throws IOException {
        if (!"/".equals(exchange.getRequestURI().getPath())) {
            send(exchange, 404, "text/plain", "Page not found");
            return;
        }

        Path website = Path.of("website", "index.html");
        if (!Files.exists(website)) {
            send(exchange, 404, "text/plain", "website/index.html was not found.");
            return;
        }
        send(exchange, 200, "text/html; charset=utf-8", Files.readString(website));
    }

    private void login(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String accountNumber = value(body, "account");
        String pin = value(body, "pin");
        AccountInfo account = getAccount(accountNumber, pin);
        sendJson(exchange, account == null ? "{\"ok\":false}" : account.toJson(true));
    }

    private void account(HttpExchange exchange) throws IOException {
        String accountNumber = queryValue(exchange, "account");
        AccountInfo account = getAccount(accountNumber, null);
        sendJson(exchange, account == null ? "{\"ok\":false}" : account.toJson(true));
    }

    private void deposit(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Double balance = accountDAO.deposit(value(body, "account"),
            number(value(body, "amount")));
        sendJson(exchange, balance == null ? "{\"ok\":false}" : success(balance));
    }

    private void withdraw(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Double balance = accountDAO.withdraw(value(body, "account"),
            number(value(body, "amount")));
        sendJson(exchange, balance == null ? "{\"ok\":false}" : success(balance));
    }

    private void transfer(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        boolean successful = accountDAO.transfer(value(body, "sender"),
            value(body, "receiver"), number(value(body, "amount")));
        sendJson(exchange, "{\"ok\":" + successful + "}");
    }

    private void transactions(HttpExchange exchange) throws IOException {
        List<Transaction> records = transactionDAO.getTransactions(
            queryValue(exchange, "account"));
        StringBuilder json = new StringBuilder("{\"ok\":true,\"transactions\":[");
        for (int index = 0; index < records.size(); index++) {
            Transaction transaction = records.get(index);
            if (index > 0) {
                json.append(',');
            }
            json.append("{\"type\":\"").append(escape(transaction.getTransactionType()))
                .append("\",\"amount\":").append(transaction.getAmount())
                .append(",\"balanceAfter\":").append(transaction.getBalanceAfterTransaction())
                .append(",\"date\":\"").append(transaction.getTransactionDate())
                .append("\",\"note\":\"").append(escape(transaction.getDescription()))
                .append("\"}");
        }
        sendJson(exchange, json.append("]}").toString());
    }

    private void createAccount(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        boolean created = create(value(body, "name"), value(body, "account"),
            value(body, "pin"), value(body, "type"), number(value(body, "balance")));
        sendJson(exchange, "{\"ok\":" + created + "}");
    }

    private AccountInfo getAccount(String accountNumber, String pin) {
        String sql = "SELECT c.name, a.account_number, a.account_type, a.balance "
            + "FROM accounts a JOIN customers c ON a.customer_id = c.customer_id "
            + "WHERE a.account_number = ? AND a.status = 'ACTIVE'"
            + (pin == null ? "" : " AND a.pin = ?");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            if (pin != null) {
                statement.setString(2, pin);
            }
            ResultSet result = statement.executeQuery();
            return result.next() ? new AccountInfo(result.getString("name"),
                result.getString("account_number"), result.getString("account_type"),
                result.getDouble("balance")) : null;
        } catch (SQLException exception) {
            System.out.println("Could not read account: " + exception.getMessage());
            return null;
        }
    }

    private boolean create(String name, String accountNumber, String pin,
                           String type, double balance) {
        if (name.isBlank() || accountNumber.isBlank() || pin.length() < 4
                || balance < 0 || getAccount(accountNumber, null) != null) {
            return false;
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            int customerId;
            try (PreparedStatement nextId = connection.prepareStatement(
                    "SELECT COALESCE(MAX(customer_id), 0) + 1 FROM customers")) {
                ResultSet result = nextId.executeQuery();
                result.next();
                customerId = result.getInt(1);
            }
            try (PreparedStatement customer = connection.prepareStatement(
                    "INSERT INTO customers (customer_id, name, id_number) VALUES (?, ?, ?)");
                 PreparedStatement account = connection.prepareStatement(
                    "INSERT INTO accounts (account_number, customer_id, account_type, balance, pin, status, interest_rate, overdraft_limit) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)")) {
                customer.setInt(1, customerId);
                customer.setString(2, name);
                customer.setString(3, "WEB" + accountNumber);
                customer.executeUpdate();

                account.setString(1, accountNumber);
                account.setInt(2, customerId);
                account.setString(3, type.toUpperCase());
                account.setDouble(4, balance);
                account.setString(5, pin);
                if ("SAVINGS".equalsIgnoreCase(type)) {
                    account.setDouble(6, 4.0);
                    account.setNull(7, java.sql.Types.DECIMAL);
                } else {
                    account.setNull(6, java.sql.Types.DECIMAL);
                    account.setDouble(7, 10000.0);
                }
                account.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException exception) {
            System.out.println("Could not create account: " + exception.getMessage());
            return false;
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String queryValue(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            return "";
        }
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private String value(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("\\\"" + key + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"")
            .matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private double number(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private String success(double balance) {
        return "{\"ok\":true,\"balance\":" + balance + "}";
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        send(exchange, 200, "application/json; charset=utf-8", json);
    }

    private void send(HttpExchange exchange, int status, String type,
                      String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private record AccountInfo(String name, String accountNumber,
                               String type, double balance) {
        private String toJson(boolean ok) {
            return "{\"ok\":" + ok + ",\"name\":\"" + name
                + "\",\"account\":\"" + accountNumber
                + "\",\"type\":\"" + type + "\",\"balance\":"
                + balance + "}";
        }
    }
}
