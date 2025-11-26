package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.AuthService;
import com.bluemoon.utils.DatabaseConnector;
import com.bluemoon.utils.Helper;

/**
 * Triển khai {@link AuthService} với database integration.
 * Sử dụng PreparedStatement để tránh SQL injection và hash password trước khi lưu.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());

    // SQL Queries
    private static final String SELECT_BY_USERNAME = 
            "SELECT id, tenDangNhap, matKhau, hoTen, vaiTro, trangThai, dienThoai " +
            "FROM tai_khoan WHERE tenDangNhap = ?";

    private static final String INSERT_ACCOUNT = 
            "INSERT INTO tai_khoan (tenDangNhap, matKhau, hoTen, vaiTro, trangThai, dienThoai) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_PASSWORD = 
            "UPDATE tai_khoan SET matKhau = ? WHERE id = ?";

    private static final String CHECK_USERNAME_EXISTS = 
            "SELECT COUNT(*) FROM tai_khoan WHERE tenDangNhap = ?";

    @Override
    public TaiKhoan login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.isEmpty()) {
            return null;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(SELECT_BY_USERNAME);
            pstmt.setString(1, username.trim());

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Check if account is active
                String trangThai = rs.getString("trangThai");
                if (trangThai == null || !trangThai.equalsIgnoreCase("Hoạt động")) {
                    logger.log(Level.WARNING, "Login attempt for inactive/locked account: " + username);
                    return null;
                }

                // Get hashed password from database
                String hashedPassword = rs.getString("matKhau");

                // Verify password
                if (!Helper.verifyPassword(password, hashedPassword)) {
                    logger.log(Level.WARNING, "Invalid password for username: " + username);
                    return null;
                }

                // Build and return TaiKhoan object
                return buildTaiKhoanFromResultSet(rs);
            }

            logger.log(Level.INFO, "Username not found: " + username);
            return null;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during login for username: " + username, e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean register(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            return false;
        }

        String username = taiKhoan.getTenDangNhap();
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        // Check if username already exists
        if (isUsernameExist(username)) {
            logger.log(Level.WARNING, "Registration attempt with existing username: " + username);
            return false;
        }

        // Hash password before saving
        String rawPassword = taiKhoan.getMatKhau();
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        String hashedPassword = Helper.hashPassword(rawPassword);

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(INSERT_ACCOUNT);

            pstmt.setString(1, username.trim());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, taiKhoan.getHoTen());
            pstmt.setString(4, taiKhoan.getVaiTro());
            pstmt.setString(5, taiKhoan.getTrangThai() != null ? taiKhoan.getTrangThai() : "Hoạt động");
            pstmt.setString(6, taiKhoan.getDienThoai());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully registered new account: " + username);
            } else {
                logger.log(Level.WARNING, "Failed to register account: " + username);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during registration for username: " + username, e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public boolean changePassword(int id, String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null || 
            oldPassword.isEmpty() || newPassword.isEmpty()) {
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // First, verify old password
            pstmt = conn.prepareStatement("SELECT matKhau FROM tai_khoan WHERE id = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                logger.log(Level.WARNING, "Account not found for password change: id = " + id);
                return false;
            }

            String currentHashedPassword = rs.getString("matKhau");
            if (!Helper.verifyPassword(oldPassword, currentHashedPassword)) {
                logger.log(Level.WARNING, "Old password verification failed for id: " + id);
                return false;
            }

            // Close previous statement and result set
            pstmt.close();
            rs.close();

            // Hash new password and update
            String newHashedPassword = Helper.hashPassword(newPassword);
            pstmt = conn.prepareStatement(UPDATE_PASSWORD);
            pstmt.setString(1, newHashedPassword);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully changed password for id: " + id);
            } else {
                logger.log(Level.WARNING, "Failed to change password for id: " + id);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during password change for id: " + id, e);
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean isUsernameExist(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(CHECK_USERNAME_EXISTS);
            pstmt.setString(1, username.trim());

            rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }

            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking username existence: " + username, e);
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Xây dựng đối tượng TaiKhoan từ ResultSet.
     *
     * @param rs ResultSet đã được positioned tại row cần đọc
     * @return đối tượng TaiKhoan
     * @throws SQLException nếu có lỗi khi đọc dữ liệu
     */
    private TaiKhoan buildTaiKhoanFromResultSet(ResultSet rs) throws SQLException {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(rs.getInt("id"));
        taiKhoan.setTenDangNhap(rs.getString("tenDangNhap"));
        taiKhoan.setMatKhau(rs.getString("matKhau")); // Store hashed password
        taiKhoan.setHoTen(rs.getString("hoTen"));
        taiKhoan.setVaiTro(rs.getString("vaiTro"));
        taiKhoan.setTrangThai(rs.getString("trangThai"));
        taiKhoan.setDienThoai(rs.getString("dienThoai"));
        return taiKhoan;
    }

    /**
     * Đóng các tài nguyên database.
     *
     * @param conn Connection
     * @param pstmt PreparedStatement
     * @param rs    ResultSet
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing database resources", e);
        }
    }
}
