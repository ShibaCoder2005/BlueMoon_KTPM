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
 * Sử dụng try-with-resources để đảm bảo resources được đóng tự động.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());

    // SQL Queries - PostgreSQL table names (snake_case)
    private static final String SELECT_BY_USERNAME = 
            "SELECT id, tenDangNhap, matKhau, hoTen, vaiTro, trangThai, dienThoai " +
            "FROM TaiKhoan WHERE tenDangNhap = ?";

    private static final String INSERT_ACCOUNT = 
            "INSERT INTO TaiKhoan (tenDangNhap, matKhau, hoTen, vaiTro, trangThai, dienThoai) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_PASSWORD = 
            "UPDATE TaiKhoan SET matKhau = ? WHERE id = ?";

    private static final String CHECK_USERNAME_EXISTS = 
            "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";

    @Override
    public TaiKhoan login(String username, String password) {
        // Validate inputs
        if (username == null || password == null || username.trim().isEmpty() || password.isEmpty()) {
            logger.log(Level.WARNING, "Login attempt with empty username or password");
            return null;
        }

        // Use try-with-resources to ensure resources are closed automatically
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_USERNAME)) {

            pstmt.setString(1, username.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Step 1: Check if user exists (already checked by rs.next())
                    
                    // Step 2: Check if account is active (trangThai = 'Hoạt động')
                    String trangThai = rs.getString("trangThai");
                    if (trangThai == null || !trangThai.equalsIgnoreCase("Hoạt động")) {
                        logger.log(Level.WARNING, 
                                "Login attempt for inactive/locked account: " + username + 
                                " (Status: " + trangThai + ")");
                        return null;
                    }

                    // Step 3: Verify password using Helper.verifyPassword
                    String hashedPassword = rs.getString("matKhau");
                    // --- START DEBUG CODE ---
                    System.out.println(">>> DEBUG LOGIN INFO <<<");
                    System.out.println("Username input: " + username);
                    System.out.println("Password input: " + password);
                    System.out.println("Hash from DB  : " + hashedPassword);
                    boolean isMatch = Helper.verifyPassword(password, hashedPassword);
                    System.out.println("Verify Result : " + isMatch);
                    System.out.println("------------------------");
                    if (!isMatch) {
                        logger.log(Level.WARNING, "Invalid password for username: " + username);
                        return null;
                    }
                    // --- END DEBUG CODE ---
                    /**if (!Helper.verifyPassword(password, hashedPassword)) {
                        logger.log(Level.WARNING, "Invalid password for username: " + username);
                        return null;
                    }**/

                    // Step 4: Build and return TaiKhoan object
                    TaiKhoan taiKhoan = buildTaiKhoanFromResultSet(rs);
                    logger.log(Level.INFO, "User logged in successfully: " + username);
                    return taiKhoan;
                } else {
                    logger.log(Level.WARNING, "Username not found: " + username);
                    return null;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during login for username: " + username, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean register(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getTenDangNhap() == null || taiKhoan.getMatKhau() == null) {
            logger.log(Level.WARNING, "Registration failed: TaiKhoan object or essential fields are null");
            return false;
        }

        String username = taiKhoan.getTenDangNhap().trim();
        if (username.isEmpty()) {
            logger.log(Level.WARNING, "Registration failed: Username is empty");
            return false;
        }

        // Check if username already exists
        if (isUsernameExist(username)) {
            logger.log(Level.WARNING, "Registration failed: Username already exists: " + username);
            return false;
        }

        // Hash password before saving
        String rawPassword = taiKhoan.getMatKhau();
        if (rawPassword == null || rawPassword.isEmpty()) {
            logger.log(Level.WARNING, "Registration failed: Password is empty");
            return false;
        }
        String hashedPassword = Helper.hashPassword(rawPassword);

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ACCOUNT)) {

            pstmt.setString(1, username);
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
            logger.log(Level.SEVERE, "Database error during registration for username: " + username, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changePassword(int id, String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null || 
            oldPassword.isEmpty() || newPassword.isEmpty()) {
            logger.log(Level.WARNING, "Change password failed: Old or new password is empty");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // First, verify the old password
            String currentHashedPassword = null;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT matKhau FROM TaiKhoan WHERE id = ?")) {
                pstmt.setInt(1, id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentHashedPassword = rs.getString("matKhau");
                    } else {
                        logger.log(Level.WARNING, "Change password failed: Account not found with id: " + id);
                        return false;
                    }
                }
            }

            // Verify old password
            if (currentHashedPassword == null || 
                !Helper.verifyPassword(oldPassword, currentHashedPassword)) {
                logger.log(Level.WARNING, "Change password failed: Old password verification failed for id: " + id);
                return false;
            }

            // Hash new password and update
            String newHashedPassword = Helper.hashPassword(newPassword);
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_PASSWORD)) {
                pstmt.setString(1, newHashedPassword);
                pstmt.setInt(2, id);

                int rowsAffected = pstmt.executeUpdate();
                boolean success = rowsAffected > 0;

                if (success) {
                    logger.log(Level.INFO, "Successfully changed password for account id: " + id);
                } else {
                    logger.log(Level.WARNING, "Failed to change password for account id: " + id);
                }

                return success;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during password change for account id: " + id, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isUsernameExist(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_USERNAME_EXISTS)) {

            pstmt.setString(1, username.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error checking username existence: " + username, e);
            e.printStackTrace();
            return false;
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
}
