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
    
    public void deleteUser(String email) throws SQLException {
        String sql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
