CREATE DATABASE IF NOT EXISTS bank_management;
USE bank_management;

CREATE TABLE customers (
    customer_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    date_of_birth VARCHAR(20),
    gender VARCHAR(20),
    address VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    id_number VARCHAR(50) UNIQUE
);

CREATE TABLE accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id INT NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(12, 2) NOT NULL,
    pin VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    interest_rate DECIMAL(5, 2),
    overdraft_limit DECIMAL(12, 2),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    balance_after_transaction DECIMAL(12, 2) NOT NULL,
    transaction_date DATETIME NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);
