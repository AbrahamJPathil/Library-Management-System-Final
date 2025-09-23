package com.library.dao;

import com.library.database.DatabaseHandler;
import com.library.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    private final Connection connection = DatabaseHandler.getInstance().getConnection();

    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books(isbn, title, author, genre, is_available) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getGenre());
            pstmt.setBoolean(5, book.isAvailable());
            pstmt.executeUpdate();
        }
    }
    
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, is_available = ? WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getGenre());
            pstmt.setBoolean(4, book.isAvailable());
            pstmt.setString(5, book.getIsbn());
            pstmt.executeUpdate();
        }
    }

    public void deleteBook(String isbn) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
        }
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookList.add(new Book(rs.getString("title"), rs.getString("author"), rs.getString("genre"), rs.getString("isbn"), rs.getBoolean("is_available")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return bookList;
    }
}
