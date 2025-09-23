package com.library.dao;

import com.library.database.DatabaseHandler;
import com.library.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    private final Connection connection = DatabaseHandler.getInstance().getConnection();

    public void issueBook(String bookIsbn, String userEmail) throws SQLException {
        String checkBookSql = "SELECT is_available FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkBookSql)) {
            pstmt.setString(1, bookIsbn);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next() || !rs.getBoolean("is_available")) {
                throw new SQLException("Book is not available for issue or does not exist.");
            }
        }

        String issueSql = "INSERT INTO transactions(book_isbn, user_email, issue_date, due_date) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(issueSql)) {
            pstmt.setString(1, bookIsbn);
            pstmt.setString(2, userEmail);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.setDate(4, Date.valueOf(LocalDate.now().plusDays(14)));
            pstmt.executeUpdate();
        }

        String updateBookSql = "UPDATE books SET is_available = false WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateBookSql)) {
            pstmt.setString(1, bookIsbn);
            pstmt.executeUpdate();
        }
    }

    public void returnBook(int transactionId, String bookIsbn) throws SQLException {
        String returnSql = "UPDATE transactions SET is_returned = true WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(returnSql)) {
            pstmt.setInt(1, transactionId);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected == 0) throw new SQLException("Could not find transaction to return.");
        }

        String updateBookSql = "UPDATE books SET is_available = true WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateBookSql)) {
            pstmt.setString(1, bookIsbn);
            pstmt.executeUpdate();
        }
    }

    public List<Transaction> getAllTransactions() {
        return getTransactionsForQuery("SELECT t.id, t.book_isbn, b.title, u.name, t.issue_date, t.due_date, t.is_returned " +
                                       "FROM transactions t " +
                                       "JOIN books b ON t.book_isbn = b.isbn " +
                                       "JOIN users u ON t.user_email = u.email " +
                                       "ORDER BY t.issue_date DESC");
    }

    public List<Transaction> getTransactionsForUser(String userEmail, boolean onlyActive) {
        String sql = "SELECT t.id, t.book_isbn, b.title, u.name, t.issue_date, t.due_date, t.is_returned " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_isbn = b.isbn " +
                     "JOIN users u ON t.user_email = u.email " +
                     "WHERE t.user_email = ?";
        if(onlyActive) {
            sql += " AND t.is_returned = false";
        }
        sql += " ORDER BY t.issue_date DESC";
        
        List<Transaction> transactionList = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                 transactionList.add(new Transaction(
                    rs.getInt("id"),
                    rs.getString("book_isbn"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getDate("issue_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("is_returned")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }

    private List<Transaction> getTransactionsForQuery(String sql) {
        List<Transaction> transactionList = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactionList.add(new Transaction(
                    rs.getInt("id"),
                    rs.getString("book_isbn"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getDate("issue_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("is_returned")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionList;
    }
}
