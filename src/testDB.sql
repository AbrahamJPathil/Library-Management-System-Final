CREATE DATABASE IF NOT EXISTS library_db;

USE library_db;


SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS=1;

-- TABLE SCHEMAS

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

-- SAMPLE DATA INSERTION

INSERT INTO `users` (`email`, `name`, `password`, `role`, `join_date`) VALUES
('admin@library.com', 'Admin User', 'admin', 'Administrator', '2023-01-15'),
('student@library.com', 'Student User', 'student', 'Student', '2023-02-20'),
('john.doe@email.com', 'John Doe', 'pass123', 'Student', '2023-03-01'),
('jane.smith@email.com', 'Jane Smith', 'pass456', 'Student', '2023-04-11'),
('sara.connor@email.com', 'Sara Connor', 'pro-pass', 'Student', '2023-05-21'),
('bob.johnson@email.com', 'Bob Johnson', 'pass789', 'Student', '2023-06-30');


INSERT INTO `books` (`isbn`, `title`, `author`, `genre`, `is_available`) VALUES
('978-0321765723', 'The C++ Programming Language', 'Bjarne Stroustrup', 'Programming', TRUE),
('978-0132350884', 'Clean Code', 'Robert C. Martin', 'Software Development', FALSE),
('978-1982137274', 'Fairy Tale', 'Stephen King', 'Horror', TRUE),
('978-0743273565', 'The Great Gatsby', 'F. Scott Fitzgerald', 'Classic', TRUE),
('978-0553103540', 'A Game of Thrones', 'George R.R. Martin', 'Fantasy', FALSE), 
('978-1400033428', 'The Kite Runner', 'Khaled Hosseini', 'Fiction', TRUE),
('978-0061120084', 'To Kill a Mockingbird', 'Harper Lee', 'Classic', FALSE), 
('978-1612620244', 'Attack on Titan Vol. 1', 'Hajime Isayama', 'Manga', TRUE);


INSERT INTO `transactions` (`book_isbn`, `user_email`, `issue_date`, `due_date`, `is_returned`) VALUES

('978-0132350884', 'john.doe@email.com', '2025-09-15', '2025-09-29', FALSE),


('978-0743273565', 'jane.smith@email.com', '2025-08-01', '2025-08-15', TRUE),

('978-0553103540', 'sara.connor@email.com', '2025-09-20', '2025-10-04', FALSE),


('978-0061120084', 'bob.johnson@email.com', '2025-09-01', '2025-09-15', FALSE),

('978-1982137274', 'student@library.com', '2025-09-05', '2025-09-19', TRUE);


