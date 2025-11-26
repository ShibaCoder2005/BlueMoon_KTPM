package com.bluemoon.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class để quản lý kết nối database.
 * Sử dụng PostgreSQL database.
 */
public class DatabaseConnector {

    private static final Logger logger = Logger.getLogger(DatabaseConnector.class.getName());

    // Database configuration - should be moved to config file in production
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/bluemoon";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    /**
     * Lấy kết nối database PostgreSQL.
     *
     * @return Connection object
     * @throws SQLException nếu không thể kết nối
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "PostgreSQL JDBC Driver not found.", e);
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }
    }
}
