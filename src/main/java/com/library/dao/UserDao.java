package com.library.dao;

import com.library.database.DatabaseHandler;
import com.library.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final Connection connection = DatabaseHandler.getInstance().getConnection();

    public Optional<User> authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new User(rs.getString("name"), rs.getString("email"), rs.getString("password"), rs.getString("role"), rs.getDate("join_date").toLocalDate()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users(email, name, password, role, join_date) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setDate(5, Date.valueOf(user.getJoinDate()));
            pstmt.executeUpdate();
        }
    }
    
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, role = ?, join_date = ? WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getRole());
            pstmt.setDate(3, Date.valueOf(user.getJoinDate()));
            pstmt.setString(4, user.getEmail());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * **NEW METHOD**: Checks if a user has any books currently checked out (not returned).
     * @param userEmail The email of the user to check.
     * @return true if the user has active transactions, false otherwise.
     */
    public boolean hasActiveTransactions(String userEmail) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_email = ? AND is_returned = false";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteUser(String email) throws SQLException {
        // Before deleting a user, we must handle their transactions to avoid foreign key errors.
        // Option 1: Delete their transactions (could lose history)
        // Option 2 (Better): Set user_email to NULL in transactions. This preserves history.
        String updateTransactionsSql = "UPDATE transactions SET user_email = NULL WHERE user_email = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(updateTransactionsSql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        }

        String deleteUserSql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteUserSql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userList.add(new User(rs.getString("name"), rs.getString("email"), rs.getString("password"), rs.getString("role"), rs.getDate("join_date").toLocalDate()));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return userList;
    }
}

