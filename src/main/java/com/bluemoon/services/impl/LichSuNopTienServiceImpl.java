package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bluemoon.models.LichSuNopTien;
import com.bluemoon.services.LichSuNopTienService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link LichSuNopTienService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class LichSuNopTienServiceImpl implements LichSuNopTienService {

    private static final Logger logger = Logger.getLogger(LichSuNopTienServiceImpl.class.getName());
    
    private final PhieuThuService phieuThuService;

    // SQL Queries
    private static final String SELECT_ALL = 
            "SELECT id, maPhieu, ngayNop, soTien, phuongThuc, nguoiThu, ghiChu FROM LichSuNopTien ORDER BY ngayNop DESC";

    private static final String SELECT_BY_PHIEU = 
            "SELECT id, maPhieu, ngayNop, soTien, phuongThuc, nguoiThu, ghiChu FROM LichSuNopTien WHERE maPhieu = ? ORDER BY ngayNop DESC";

    private static final String SELECT_BY_HO = 
            "SELECT ls.id, ls.maPhieu, ls.ngayNop, ls.soTien, ls.phuongThuc, ls.nguoiThu, ls.ghiChu " +
            "FROM LichSuNopTien ls " +
            "INNER JOIN PhieuThu pt ON ls.maPhieu = pt.id " +
            "WHERE pt.maHo = ? ORDER BY ls.ngayNop DESC";

    private static final String INSERT = 
            "INSERT INTO LichSuNopTien (maPhieu, ngayNop, soTien, phuongThuc, nguoiThu, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";

    public LichSuNopTienServiceImpl() {
        this.phieuThuService = new PhieuThuServiceImpl();
    }

    /**
     * Constructor với dependency injection (cho testing hoặc future DI framework).
     */
    public LichSuNopTienServiceImpl(PhieuThuService phieuThuService) {
        this.phieuThuService = phieuThuService;
    }

    @Override
    public List<LichSuNopTien> getAllLichSuNopTien() {
        List<LichSuNopTien> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildLichSuFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " payment history records");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all payment history", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addLichSuNopTien(LichSuNopTien paymentRecord) {
        if (paymentRecord == null) {
            logger.log(Level.WARNING, "Cannot add payment record: paymentRecord is null");
            return false;
        }

        // Validate required fields
        if (paymentRecord.getMaPhieu() <= 0) {
            logger.log(Level.WARNING, "Cannot add payment record: maPhieu is invalid");
            return false;
        }

        if (paymentRecord.getSoTien() == null || paymentRecord.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            logger.log(Level.WARNING, "Cannot add payment record: soTien must be positive");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            pstmt.setInt(1, paymentRecord.getMaPhieu());
            pstmt.setTimestamp(2, convertToSqlTimestamp(paymentRecord.getNgayNop()));
            pstmt.setBigDecimal(3, paymentRecord.getSoTien());
            pstmt.setString(4, paymentRecord.getPhuongThuc());
            pstmt.setInt(5, paymentRecord.getNguoiThu());
            pstmt.setString(6, null); // ghiChu field not in model, set to null

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added payment record for maPhieu: " + paymentRecord.getMaPhieu() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to add payment record for maPhieu: " + paymentRecord.getMaPhieu() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding payment record for maPhieu: " + paymentRecord.getMaPhieu(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<LichSuNopTien> getLichSuNopTienByPhieuThu(int maPhieu) {
        List<LichSuNopTien> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_PHIEU)) {

            pstmt.setInt(1, maPhieu);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildLichSuFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " payment history records for maPhieu: " + maPhieu);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving payment history for maPhieu: " + maPhieu, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<LichSuNopTien> getLichSuNopTienByHoGiaDinh(int maHo) {
        List<LichSuNopTien> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_HO)) {

            pstmt.setInt(1, maHo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildLichSuFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " payment history records for maHo: " + maHo);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving payment history for maHo: " + maHo, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean recordPaymentWithStatusUpdate(LichSuNopTien paymentRecord, String updateStatusTo) {
        if (paymentRecord == null || paymentRecord.getMaPhieu() <= 0) {
            logger.log(Level.WARNING, "Invalid payment record: paymentRecord is null or maPhieu is invalid");
            return false;
        }

        if (updateStatusTo == null || updateStatusTo.trim().isEmpty()) {
            logger.log(Level.WARNING, "Invalid status: updateStatusTo is null or empty");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Step 1: Record the payment
                boolean paymentSuccess = addLichSuNopTien(paymentRecord);
                if (!paymentSuccess) {
                    conn.rollback();
                    logger.log(Level.WARNING, "Failed to record payment for maPhieu: " + paymentRecord.getMaPhieu());
                    return false;
                }

                // Step 2: Update receipt status
                boolean statusSuccess = phieuThuService.updatePhieuThuStatus(paymentRecord.getMaPhieu(), updateStatusTo);
                if (!statusSuccess) {
                    conn.rollback();
                    logger.log(Level.WARNING, 
                            "Payment recorded but failed to update receipt status for maPhieu: " + paymentRecord.getMaPhieu());
                    return false;
                }

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, 
                        "Successfully recorded payment and updated status for maPhieu: " + paymentRecord.getMaPhieu());
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, 
                    "Error in recordPaymentWithStatusUpdate for maPhieu: " + paymentRecord.getMaPhieu(), e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng LichSuNopTien từ ResultSet.
     */
    private LichSuNopTien buildLichSuFromResultSet(ResultSet rs) throws SQLException {
        LichSuNopTien lichSu = new LichSuNopTien();
        lichSu.setId(rs.getInt("id"));
        lichSu.setMaPhieu(rs.getInt("maPhieu"));
        
        Timestamp ngayNop = rs.getTimestamp("ngayNop");
        if (ngayNop != null) {
            lichSu.setNgayNop(ngayNop.toLocalDateTime());
        }
        
        lichSu.setSoTien(rs.getBigDecimal("soTien"));
        lichSu.setPhuongThuc(rs.getString("phuongThuc"));
        lichSu.setNguoiThu(rs.getInt("nguoiThu"));
        // ghiChu field exists in DB but not in model, skip it
        
        return lichSu;
    }

    /**
     * Chuyển đổi LocalDateTime sang java.sql.Timestamp.
     */
    private Timestamp convertToSqlTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        return Timestamp.valueOf(localDateTime);
    }
}
