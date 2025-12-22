package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.TaiKhoanService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link TaiKhoanService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class TaiKhoanServiceImpl implements TaiKhoanService {

    private static final Logger logger = Logger.getLogger(TaiKhoanServiceImpl.class.getName());

    // SQL Queries
    private static final String SELECT_ALL = 
            "SELECT id, tenDangNhap, matKhau, hoTen, vaiTro, dienThoai, trangThai FROM TaiKhoan ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, tenDangNhap, matKhau, hoTen, vaiTro, dienThoai, trangThai FROM TaiKhoan WHERE id = ?";

    private static final String SELECT_BY_USERNAME = 
            "SELECT id, tenDangNhap, matKhau, hoTen, vaiTro, dienThoai, trangThai FROM TaiKhoan WHERE LOWER(TRIM(tenDangNhap)) = LOWER(TRIM(?))";

    private static final String INSERT = 
            "INSERT INTO TaiKhoan (tenDangNhap, matKhau, hoTen, vaiTro, dienThoai, trangThai) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE TaiKhoan SET tenDangNhap = ?, matKhau = ?, hoTen = ?, vaiTro = ?, dienThoai = ?, trangThai = ? WHERE id = ?";

    private static final String UPDATE_STATUS = 
            "UPDATE TaiKhoan SET trangThai = ? WHERE id = ?";

    private static final String UPDATE_PASSWORD = 
            "UPDATE TaiKhoan SET matKhau = ? WHERE id = ?";

    private static final String CHECK_USERNAME_EXISTS = 
            "SELECT COUNT(*) FROM TaiKhoan WHERE LOWER(TRIM(tenDangNhap)) = LOWER(TRIM(?)) AND id != ?";

    @Override
    public List<TaiKhoan> getAllTaiKhoan() {
        List<TaiKhoan> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildTaiKhoanFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " accounts");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all accounts", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public TaiKhoan findById(int id) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildTaiKhoanFromResultSet(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving account with id: " + id, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TaiKhoan findByUsername(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_USERNAME)) {

            pstmt.setString(1, tenDangNhap.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildTaiKhoanFromResultSet(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving account with username: " + tenDangNhap, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isUsernameExists(String tenDangNhap, int excludeId) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_USERNAME_EXISTS)) {

            pstmt.setString(1, tenDangNhap.trim());
            pstmt.setInt(2, excludeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking username existence: " + tenDangNhap, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            logger.log(Level.WARNING, "Cannot add account: taiKhoan is null");
            return false;
        }

        // Validate required fields
        if (taiKhoan.getTenDangNhap() == null || taiKhoan.getTenDangNhap().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot add account: tenDangNhap is empty");
            return false;
        }

        // Check username uniqueness
        if (isUsernameExists(taiKhoan.getTenDangNhap(), 0)) {
            logger.log(Level.WARNING, "Cannot add account: username already exists: " + taiKhoan.getTenDangNhap());
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            pstmt.setString(1, taiKhoan.getTenDangNhap().trim());
            pstmt.setString(2, taiKhoan.getMatKhau());
            pstmt.setString(3, taiKhoan.getHoTen());
            pstmt.setString(4, taiKhoan.getVaiTro());
            pstmt.setString(5, taiKhoan.getDienThoai());
            pstmt.setString(6, taiKhoan.getTrangThai() != null ? taiKhoan.getTrangThai() : "Hoạt động");

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added account: " + taiKhoan.getTenDangNhap() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to add account: " + taiKhoan.getTenDangNhap() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding account: " + taiKhoan.getTenDangNhap(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getId() == 0) {
            logger.log(Level.WARNING, "Cannot update account: taiKhoan is null or id is 0");
            return false;
        }

        // Validate required fields
        if (taiKhoan.getTenDangNhap() == null || taiKhoan.getTenDangNhap().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot update account: tenDangNhap is empty");
            return false;
        }

        // Check username uniqueness (exclude current account)
        if (isUsernameExists(taiKhoan.getTenDangNhap(), taiKhoan.getId())) {
            logger.log(Level.WARNING, "Cannot update account: username already exists: " + taiKhoan.getTenDangNhap());
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {

            pstmt.setString(1, taiKhoan.getTenDangNhap().trim());
            pstmt.setString(2, taiKhoan.getMatKhau());
            pstmt.setString(3, taiKhoan.getHoTen());
            pstmt.setString(4, taiKhoan.getVaiTro());
            pstmt.setString(5, taiKhoan.getDienThoai());
            pstmt.setString(6, taiKhoan.getTrangThai());
            pstmt.setInt(7, taiKhoan.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated account with id: " + taiKhoan.getId() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to update account with id: " + taiKhoan.getId() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating account with id: " + taiKhoan.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStatus(int id, String trangThai) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS)) {

            pstmt.setString(1, trangThai);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated status for account id: " + id + " to " + trangThai + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to update status for account id: " + id + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating status for account id: " + id, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePassword(int id, String hashedPassword) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PASSWORD)) {

            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated password for account id: " + id + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to update password for account id: " + id + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating password for account id: " + id, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng TaiKhoan từ ResultSet.
     */
    private TaiKhoan buildTaiKhoanFromResultSet(ResultSet rs) throws SQLException {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(rs.getInt("id"));
        taiKhoan.setTenDangNhap(rs.getString("tenDangNhap"));
        taiKhoan.setMatKhau(rs.getString("matKhau"));
        taiKhoan.setHoTen(rs.getString("hoTen"));
        taiKhoan.setVaiTro(rs.getString("vaiTro"));
        taiKhoan.setDienThoai(rs.getString("dienThoai"));
        taiKhoan.setTrangThai(rs.getString("trangThai"));
        return taiKhoan;
    }
}
