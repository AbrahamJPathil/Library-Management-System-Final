package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseHandler {
    private static DatabaseHandler handler = null;
    private static Connection conn = null;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Project2025";

    private DatabaseHandler() { createConnection(); }
    public static synchronized DatabaseHandler getInstance() {
        if (handler == null) { handler = new DatabaseHandler(); }
        return handler;
    }
    private void createConnection() {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("FATAL: Error connecting to the database. Check credentials and if MySQL is running.");
            e.printStackTrace();
            System.exit(1);
        }
    }
    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) { createConnection(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return conn;
    }
}
