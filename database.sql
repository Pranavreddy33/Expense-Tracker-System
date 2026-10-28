CREATE DATABASE IF NOT EXISTS expense_tracker;

USE expense_tracker;

CREATE TABLE IF NOT EXISTS Expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    expense_date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL
);

INSERT INTO Expenses
(expense_date, category, description, amount)
VALUES
('2026-10-27', 'Food', 'Lunch', 150.00);
