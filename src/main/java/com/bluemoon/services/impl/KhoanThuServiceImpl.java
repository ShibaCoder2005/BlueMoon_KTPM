package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.services.KhoanThuService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link KhoanThuService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class KhoanThuServiceImpl implements KhoanThuService {

    private static final Logger logger = Logger.getLogger(KhoanThuServiceImpl.class.getName());

    // SQL Queries
    private static final String SELECT_ALL = 
            "SELECT id, tenKhoan, loai, donGia, donViTinh, tinhTheo, batBuoc, moTa FROM KhoanThu ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, tenKhoan, loai, donGia, donViTinh, tinhTheo, batBuoc, moTa FROM KhoanThu WHERE id = ?";

    private static final String INSERT = 
            "INSERT INTO KhoanThu (tenKhoan, loai, donGia, donViTinh, tinhTheo, batBuoc, moTa) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE KhoanThu SET tenKhoan = ?, loai = ?, donGia = ?, donViTinh = ?, tinhTheo = ?, batBuoc = ?, moTa = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM KhoanThu WHERE id = ?";

    private static final String CHECK_FEE_USED = 
            "SELECT COUNT(*) FROM ChiTietThu WHERE maKhoan = ?";

    @Override
    public List<KhoanThu> getAllKhoanThu() {
        List<KhoanThu> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildKhoanThuFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " fee items");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all fee items", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addKhoanThu(KhoanThu khoanThu) {
        if (khoanThu == null) {
            logger.log(Level.WARNING, "Cannot add fee: khoanThu is null");
            return false;
        }

        // Validate required fields
        if (khoanThu.getTenKhoan() == null || khoanThu.getTenKhoan().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot add fee: tenKhoan is empty");
            return false;
        }

        // Validate donGia: must be non-negative
        if (khoanThu.getDonGia() != null && khoanThu.getDonGia().compareTo(java.math.BigDecimal.ZERO) < 0) {
            logger.log(Level.WARNING, "Cannot add fee: donGia cannot be negative");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            pstmt.setString(1, khoanThu.getTenKhoan().trim());
            pstmt.setString(2, khoanThu.getLoai());
            pstmt.setBigDecimal(3, khoanThu.getDonGia());
            pstmt.setString(4, khoanThu.getDonViTinh());
            pstmt.setString(5, khoanThu.getTinhTheo());
            pstmt.setBoolean(6, khoanThu.isBatBuoc());
            pstmt.setString(7, khoanThu.getMoTa());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added fee: " + khoanThu.getTenKhoan() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to add fee: " + khoanThu.getTenKhoan() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding fee: " + khoanThu.getTenKhoan(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateKhoanThu(KhoanThu khoanThu) {
        if (khoanThu == null || khoanThu.getId() == 0) {
            logger.log(Level.WARNING, "Cannot update fee: khoanThu is null or id is 0");
            return false;
        }

        // Validate required fields
        if (khoanThu.getTenKhoan() == null || khoanThu.getTenKhoan().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot update fee: tenKhoan is empty");
            return false;
        }

        // Validate donGia: must be non-negative
        if (khoanThu.getDonGia() != null && khoanThu.getDonGia().compareTo(java.math.BigDecimal.ZERO) < 0) {
            logger.log(Level.WARNING, "Cannot update fee: donGia cannot be negative");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {

            pstmt.setString(1, khoanThu.getTenKhoan().trim());
            pstmt.setString(2, khoanThu.getLoai());
            pstmt.setBigDecimal(3, khoanThu.getDonGia());
            pstmt.setString(4, khoanThu.getDonViTinh());
            pstmt.setString(5, khoanThu.getTinhTheo());
            pstmt.setBoolean(6, khoanThu.isBatBuoc());
            pstmt.setString(7, khoanThu.getMoTa());
            pstmt.setInt(8, khoanThu.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated fee with id: " + khoanThu.getId() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to update fee with id: " + khoanThu.getId() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating fee with id: " + khoanThu.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteKhoanThu(int maKhoanThu) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Check if fee is being used in any ChiTietThu
            try (PreparedStatement checkStmt = conn.prepareStatement(CHECK_FEE_USED)) {
                checkStmt.setInt(1, maKhoanThu);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count > 0) {
                            logger.log(Level.WARNING, 
                                    "Cannot delete fee with id: " + maKhoanThu + 
                                    ". Fee is being used in " + count + " receipt detail(s).");
                            return false;
                        }
                    }
                }
            }

            // Proceed with deletion if not used
            try (PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
                pstmt.setInt(1, maKhoanThu);

                int rowsAffected = pstmt.executeUpdate();
                boolean success = rowsAffected > 0;

                if (success) {
                    logger.log(Level.INFO, "Successfully deleted fee with id: " + maKhoanThu + " (rows affected: " + rowsAffected + ")");
                } else {
                    logger.log(Level.WARNING, "Failed to delete fee with id: " + maKhoanThu + " (rows affected: " + rowsAffected + ")");
                }

                return success;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting fee with id: " + maKhoanThu, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng KhoanThu từ ResultSet.
     */
    private KhoanThu buildKhoanThuFromResultSet(ResultSet rs) throws SQLException {
        KhoanThu khoanThu = new KhoanThu();
        khoanThu.setId(rs.getInt("id"));
        khoanThu.setTenKhoan(rs.getString("tenKhoan"));
        khoanThu.setLoai(rs.getString("loai"));
        khoanThu.setDonGia(rs.getBigDecimal("donGia"));
        khoanThu.setDonViTinh(rs.getString("donViTinh"));
        khoanThu.setTinhTheo(rs.getString("tinhTheo"));
        khoanThu.setBatBuoc(rs.getBoolean("batBuoc"));
        khoanThu.setMoTa(rs.getString("moTa"));
        
        // Set loaiKhoanThu based on batBuoc
        khoanThu.setLoaiKhoanThu(khoanThu.isBatBuoc() ? 0 : 1);
        
        return khoanThu;
    }
}
