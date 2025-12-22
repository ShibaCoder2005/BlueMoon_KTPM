package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.DotThu;
import com.bluemoon.services.DotThuService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link DotThuService} với database integration.
 * Sử dụng PreparedStatement để tránh SQL injection và kiểm tra ràng buộc trước khi xóa.
 */
public class DotThuServiceImpl implements DotThuService {

    private static final Logger logger = Logger.getLogger(DotThuServiceImpl.class.getName());

    // SQL Queries - PostgreSQL table names (case-sensitive, use exact name from CREATE TABLE)
    private static final String SELECT_ALL = 
            "SELECT id, tenDot, ngayBatDau, ngayKetThuc, trangThai, ghiChu FROM DotThu ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, tenDot, ngayBatDau, ngayKetThuc, trangThai, ghiChu FROM DotThu WHERE id = ?";

    private static final String INSERT = 
            "INSERT INTO DotThu (tenDot, ngayBatDau, ngayKetThuc, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE DotThu SET tenDot = ?, ngayBatDau = ?, ngayKetThuc = ?, trangThai = ?, ghiChu = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM DotThu WHERE id = ?";

    private static final String CHECK_DEPENDENCIES = 
            "SELECT COUNT(*) FROM PhieuThu WHERE maDot = ?";

    private static final String SEARCH = 
            "SELECT id, tenDot, ngayBatDau, ngayKetThuc, trangThai, ghiChu FROM DotThu " +
            "WHERE tenDot LIKE ? OR ghiChu LIKE ? ORDER BY id";

    @Override
    public List<DotThu> getAllDotThu() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<DotThu> result = new ArrayList<>();

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(SELECT_ALL);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                result.add(buildDotThuFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " collection drives");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all collection drives", e);
            return new ArrayList<>();
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public DotThu getDotThuById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(SELECT_BY_ID);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return buildDotThuFromResultSet(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving collection drive with id: " + id, e);
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public boolean addDotThu(DotThu dotThu) {
        if (dotThu == null) {
            return false;
        }

        // Validate date logic: ngayKetThuc must be >= ngayBatDau
        if (dotThu.getNgayBatDau() != null && dotThu.getNgayKetThuc() != null) {
            if (dotThu.getNgayKetThuc().isBefore(dotThu.getNgayBatDau())) {
                logger.log(Level.WARNING, "Invalid date range: end date is before start date");
                return false;
            }
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(INSERT);

            pstmt.setString(1, dotThu.getTenDot());
            pstmt.setDate(2, convertToSqlDate(dotThu.getNgayBatDau()));
            pstmt.setDate(3, convertToSqlDate(dotThu.getNgayKetThuc()));
            pstmt.setString(4, dotThu.getTrangThai());
            pstmt.setString(5, dotThu.getMoTa()); // Map moTa to ghiChu column

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added collection drive: " + dotThu.getTenDot());
            } else {
                logger.log(Level.WARNING, "Failed to add collection drive: " + dotThu.getTenDot());
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding collection drive: " + dotThu.getTenDot(), e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public boolean updateDotThu(DotThu dotThu) {
        if (dotThu == null || dotThu.getId() == 0) {
            return false;
        }

        // Validate date logic: ngayKetThuc must be >= ngayBatDau
        if (dotThu.getNgayBatDau() != null && dotThu.getNgayKetThuc() != null) {
            if (dotThu.getNgayKetThuc().isBefore(dotThu.getNgayBatDau())) {
                logger.log(Level.WARNING, "Invalid date range: end date is before start date for id: " + dotThu.getId());
                return false;
            }
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(UPDATE);

            pstmt.setString(1, dotThu.getTenDot());
            pstmt.setDate(2, convertToSqlDate(dotThu.getNgayBatDau()));
            pstmt.setDate(3, convertToSqlDate(dotThu.getNgayKetThuc()));
            pstmt.setString(4, dotThu.getTrangThai());
            pstmt.setString(5, dotThu.getMoTa()); // Map moTa to ghiChu column
            pstmt.setInt(6, dotThu.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated collection drive with id: " + dotThu.getId());
            } else {
                logger.log(Level.WARNING, "Failed to update collection drive with id: " + dotThu.getId());
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating collection drive with id: " + dotThu.getId(), e);
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    @Override
    public boolean deleteDotThu(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // Check for dependencies: PhieuThu records
            pstmt = conn.prepareStatement(CHECK_DEPENDENCIES);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    logger.log(Level.WARNING, 
                            "Cannot delete collection drive with id: " + id + 
                            ". There are " + count + " receipts associated with this drive.");
                    return false;
                }
            }

            // Close previous statement and result set
            pstmt.close();
            rs.close();

            // Proceed with deletion
            pstmt = conn.prepareStatement(DELETE);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully deleted collection drive with id: " + id);
            } else {
                logger.log(Level.WARNING, "Failed to delete collection drive with id: " + id);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting collection drive with id: " + id, e);
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    @Override
    public List<DotThu> searchDotThu(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDotThu();
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<DotThu> result = new ArrayList<>();

        try {
            conn = DatabaseConnector.getConnection();
            pstmt = conn.prepareStatement(SEARCH);

            String searchPattern = "%" + keyword.trim() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                result.add(buildDotThuFromResultSet(rs));
            }

            logger.log(Level.INFO, "Found " + result.size() + " collection drives matching: " + keyword);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching collection drives with keyword: " + keyword, e);
            return new ArrayList<>();
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Xây dựng đối tượng DotThu từ ResultSet.
     *
     * @param rs ResultSet đã được positioned tại row cần đọc
     * @return đối tượng DotThu
     * @throws SQLException nếu có lỗi khi đọc dữ liệu
     */
    private DotThu buildDotThuFromResultSet(ResultSet rs) throws SQLException {
        DotThu dotThu = new DotThu();
        dotThu.setId(rs.getInt("id"));
        dotThu.setTenDot(rs.getString("tenDot"));
        
        // Convert java.sql.Date to LocalDate
        Date ngayBatDau = rs.getDate("ngayBatDau");
        if (ngayBatDau != null) {
            dotThu.setNgayBatDau(ngayBatDau.toLocalDate());
        }
        
        Date ngayKetThuc = rs.getDate("ngayKetThuc");
        if (ngayKetThuc != null) {
            dotThu.setNgayKetThuc(ngayKetThuc.toLocalDate());
        }
        
        dotThu.setTrangThai(rs.getString("trangThai"));
        dotThu.setMoTa(rs.getString("ghiChu")); // Map ghiChu column to moTa field
        
        return dotThu;
    }

    /**
     * Chuyển đổi LocalDate sang java.sql.Date.
     *
     * @param localDate LocalDate cần chuyển đổi
     * @return java.sql.Date, null nếu localDate là null
     */
    private Date convertToSqlDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.valueOf(localDate);
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
