CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- Temporarily disable the safety check that is causing the error
SET FOREIGN_KEY_CHECKS=0;

-- Drop tables. Adding 'issue_log' just in case an old version is lingering.
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS issue_log; 
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- Re-enable the safety check for future data integrity
SET FOREIGN_KEY_CHECKS=1;

-- Now, create the correct schema from scratch
CREATE TABLE IF NOT EXISTS books (
    isbn VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(255),
    is_available BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS users (
    email VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    join_date DATE
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_isbn VARCHAR(255),
    user_email VARCHAR(255),
    issue_date DATE,
    due_date DATE,
    is_returned BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (book_isbn) REFERENCES books(isbn) ON DELETE SET NULL,
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE SET NULL
);

-- Create a default admin user (password: admin)
INSERT INTO users (email, name, password, role, join_date)
VALUES ('admin@library.com', 'Admin User', 'admin', 'Administrator', CURDATE())
ON DUPLICATE KEY UPDATE name = 'Admin User';

-- Create a default student user (password: student)
INSERT INTO users (email, name, password, role, join_date)
VALUES ('student@library.com', 'Student User', 'student', 'Student', CURDATE())
ON DUPLICATE KEY UPDATE name = 'Student User';
