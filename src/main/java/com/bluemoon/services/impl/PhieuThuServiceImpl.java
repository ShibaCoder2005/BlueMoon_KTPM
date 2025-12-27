package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.models.DotThu;
import com.bluemoon.models.PhieuThu;
import com.bluemoon.services.DotThuService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link PhieuThuService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class PhieuThuServiceImpl implements PhieuThuService {

    private static final Logger logger = Logger.getLogger(PhieuThuServiceImpl.class.getName());
    
    // Service để kiểm tra trạng thái đợt thu
    private final DotThuService dotThuService;
    
    public PhieuThuServiceImpl() {
        this.dotThuService = new com.bluemoon.services.impl.DotThuServiceImpl();
    }
    
    /**
     * Kiểm tra xem đợt thu có đang mở không (cho phép tạo phiếu thu).
     * @param maDot mã đợt thu
     * @return true nếu đợt thu đang mở, false nếu đã đóng hoặc không tồn tại
     */
    private boolean isDotThuOpen(int maDot) {
        DotThu dotThu = dotThuService.getDotThuById(maDot);
        if (dotThu == null) {
            logger.log(Level.WARNING, "DotThu with id " + maDot + " not found");
            return false;
        }
        
        String trangThai = dotThu.getTrangThai();
        if (trangThai == null) {
            // Nếu không có trạng thái, coi như đang mở
            return true;
        }
        
        // Kiểm tra các trạng thái đóng (case-insensitive)
        String trangThaiLower = trangThai.toLowerCase().trim();
        boolean isClosed = trangThaiLower.equals("đóng") || trangThaiLower.equals("dong") || 
                          trangThaiLower.equals("closed") || trangThaiLower.equals("đã đóng") ||
                          trangThaiLower.equals("da dong");
        
        if (isClosed) {
            logger.log(Level.INFO, "DotThu with id " + maDot + " is closed (trangThai: " + trangThai + ")");
            return false;
        }
        
        return true;
    }

    // SQL Queries for PhieuThu
    private static final String SELECT_ALL = 
            "SELECT id, maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu, ghiChu FROM PhieuThu ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu, ghiChu FROM PhieuThu WHERE id = ?";

    private static final String SELECT_BY_MAHO = 
            "SELECT id, maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu, ghiChu FROM PhieuThu WHERE maHo = ? ORDER BY id";

    private static final String SELECT_BY_DOT = 
            "SELECT id, maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu, ghiChu FROM PhieuThu WHERE maDot = ? ORDER BY id";

    private static final String INSERT = 
            "INSERT INTO PhieuThu (maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE PhieuThu SET maHo = ?, maDot = ?, maTaiKhoan = ?, ngayLap = ?, tongTien = ?, trangThai = ?, hinhThucThu = ?, ghiChu = ? WHERE id = ?";

    private static final String UPDATE_STATUS = 
            "UPDATE PhieuThu SET trangThai = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM PhieuThu WHERE id = ?";

    // SQL Queries for ChiTietThu
    private static final String SELECT_CHITIET_BY_PHIEU = 
            "SELECT ct.id, ct.maPhieu, ct.maKhoan, ct.soLuong, ct.donGia, ct.thanhTien, ct.ghiChu, kt.tenKhoan " +
            "FROM ChiTietThu ct " +
            "LEFT JOIN KhoanThu kt ON ct.maKhoan = kt.id " +
            "WHERE ct.maPhieu = ? ORDER BY ct.id";

    private static final String INSERT_CHITIET = 
            "INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_CHITIET_BY_PHIEU = 
            "DELETE FROM ChiTietThu WHERE maPhieu = ?";

    private static final String CHECK_FEE_USED = 
            "SELECT COUNT(*) FROM ChiTietThu WHERE maKhoan = ?";

    @Override
    public int createPhieuThu(PhieuThu phieuThu) {
        if (phieuThu == null) {
            logger.log(Level.WARNING, "Cannot create PhieuThu: phieuThu is null");
            return -1;
        }

        // Validate: Kiểm tra đợt thu có đang mở không
        if (!isDotThuOpen(phieuThu.getMaDot())) {
            DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
            String errorMsg = "Không thể tạo phiếu thu: Đợt thu đã đóng";
            if (dotThu != null) {
                errorMsg = "Không thể tạo phiếu thu: Đợt thu \"" + dotThu.getTenDot() + "\" đã đóng (trạng thái: " + dotThu.getTrangThai() + ")";
            }
            logger.log(Level.WARNING, "Cannot create PhieuThu: DotThu with id " + phieuThu.getMaDot() + " is closed");
            throw new IllegalArgumentException(errorMsg);
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, phieuThu.getMaHo());
            pstmt.setInt(2, phieuThu.getMaDot());
            pstmt.setInt(3, phieuThu.getMaTaiKhoan());
            pstmt.setTimestamp(4, convertToSqlTimestamp(phieuThu.getNgayLap()));
            pstmt.setBigDecimal(5, phieuThu.getTongTien());
            pstmt.setString(6, phieuThu.getTrangThai() != null ? phieuThu.getTrangThai() : "ChuaThu");
            pstmt.setString(7, phieuThu.getHinhThucThu());
            pstmt.setString(8, null); // ghiChu field not in model

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        logger.log(Level.INFO, "Successfully created PhieuThu with id: " + newId);
                        return newId;
                    }
                }
            }

            logger.log(Level.WARNING, "Failed to create PhieuThu (rows affected: " + rowsAffected + ")");
            return -1;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating PhieuThu", e);
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean addChiTietThu(ChiTietThu chiTiet) {
        if (chiTiet == null) {
            logger.log(Level.WARNING, "Cannot add ChiTietThu: chiTiet is null");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_CHITIET)) {

            pstmt.setInt(1, chiTiet.getMaPhieu());
            pstmt.setInt(2, chiTiet.getMaKhoan());
            pstmt.setBigDecimal(3, chiTiet.getSoLuong());
            pstmt.setBigDecimal(4, chiTiet.getDonGia());
            pstmt.setBigDecimal(5, chiTiet.getThanhTien());
            pstmt.setString(6, null); // ghiChu field not in model

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added ChiTietThu for maPhieu: " + chiTiet.getMaPhieu());
            } else {
                logger.log(Level.WARNING, "Failed to add ChiTietThu for maPhieu: " + chiTiet.getMaPhieu());
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding ChiTietThu for maPhieu: " + chiTiet.getMaPhieu(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int createPhieuThuWithDetails(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
        if (phieuThu == null) {
            logger.log(Level.WARNING, "Cannot create PhieuThu with details: phieuThu is null");
            return -1;
        }

        // Validate: Kiểm tra đợt thu có đang mở không
        if (!isDotThuOpen(phieuThu.getMaDot())) {
            DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
            String errorMsg = "Không thể tạo phiếu thu: Đợt thu đã đóng";
            if (dotThu != null) {
                errorMsg = "Không thể tạo phiếu thu: Đợt thu \"" + dotThu.getTenDot() + "\" đã đóng (trạng thái: " + dotThu.getTrangThai() + ")";
            }
            logger.log(Level.WARNING, "Cannot create PhieuThu with details: DotThu with id " + phieuThu.getMaDot() + " is closed");
            throw new IllegalArgumentException(errorMsg);
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Step 1: Create PhieuThu (inline to use same connection)
                int maPhieu = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, phieuThu.getMaHo());
                    pstmt.setInt(2, phieuThu.getMaDot());
                    pstmt.setInt(3, phieuThu.getMaTaiKhoan());
                    pstmt.setTimestamp(4, convertToSqlTimestamp(phieuThu.getNgayLap()));
                    pstmt.setBigDecimal(5, phieuThu.getTongTien());
                    pstmt.setString(6, phieuThu.getTrangThai() != null ? phieuThu.getTrangThai() : "ChuaThu");
                    pstmt.setString(7, phieuThu.getHinhThucThu());
                    pstmt.setString(8, null); // ghiChu

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                maPhieu = generatedKeys.getInt(1);
                            }
                        }
                    }

                    if (maPhieu == -1) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to create PhieuThu, transaction rolled back");
                        return -1;
                    }
                }

                // Step 2: Add all ChiTietThu (inline to use same connection)
                if (chiTietList != null && !chiTietList.isEmpty()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(INSERT_CHITIET)) {
                        for (ChiTietThu chiTiet : chiTietList) {
                            chiTiet.setMaPhieu(maPhieu);
                            pstmt.setInt(1, chiTiet.getMaPhieu());
                            pstmt.setInt(2, chiTiet.getMaKhoan());
                            pstmt.setBigDecimal(3, chiTiet.getSoLuong());
                            pstmt.setBigDecimal(4, chiTiet.getDonGia());
                            pstmt.setBigDecimal(5, chiTiet.getThanhTien());
                            pstmt.setString(6, null); // ghiChu
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully created PhieuThu with details, id: " + maPhieu);
                return maPhieu;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in createPhieuThuWithDetails", e);
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public List<ChiTietThu> getChiTietThuByPhieu(int maPhieu) {
        List<ChiTietThu> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_CHITIET_BY_PHIEU)) {

            pstmt.setInt(1, maPhieu);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildChiTietThuFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " ChiTietThu records for maPhieu: " + maPhieu);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving ChiTietThu for maPhieu: " + maPhieu, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public PhieuThu getPhieuThuWithDetails(int maPhieu) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, maPhieu);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildPhieuThuFromResultSet(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving PhieuThu with id: " + maPhieu, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<PhieuThu> findPhieuThuByHoGiaDinh(int maHo) {
        List<PhieuThu> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_MAHO)) {

            pstmt.setInt(1, maHo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildPhieuThuFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " PhieuThu records for maHo: " + maHo);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving PhieuThu for maHo: " + maHo, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<PhieuThu> findPhieuThuByDotThu(int maDotThu) {
        List<PhieuThu> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_DOT)) {

            pstmt.setInt(1, maDotThu);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildPhieuThuFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " PhieuThu records for maDot: " + maDotThu);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving PhieuThu for maDot: " + maDotThu, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<PhieuThu> getAllPhieuThu() {
        List<PhieuThu> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildPhieuThuFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " PhieuThu records");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all PhieuThu", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updatePhieuThuStatus(int maPhieu, String newStatus) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            return updatePhieuThuStatus(maPhieu, newStatus, conn);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating connection for updatePhieuThuStatus", e);
            return false;
        }
    }

    /**
     * Internal method to update PhieuThu status with provided connection (for transaction support).
     */
    private boolean updatePhieuThuStatus(int maPhieu, String newStatus, Connection conn) {
        try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, maPhieu);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated PhieuThu status for id: " + maPhieu + " to " + newStatus);
            } else {
                logger.log(Level.WARNING, "Failed to update PhieuThu status for id: " + maPhieu);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating PhieuThu status for id: " + maPhieu, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int generateReceiptsForDrive(int maDot) {
        // TODO: Implement batch receipt generation
        logger.log(Level.INFO, "generateReceiptsForDrive called for maDot: " + maDot + " (not yet implemented)");
        return 0;
    }

    @Override
    public boolean hasUnpaidFees(int maHo) {
        List<PhieuThu> phieuThuList = findPhieuThuByHoGiaDinh(maHo);
        for (PhieuThu phieuThu : phieuThuList) {
            String trangThai = phieuThu.getTrangThai();
            if (trangThai != null && 
                (trangThai.equalsIgnoreCase("ChuaThu") || 
                 trangThai.equalsIgnoreCase("Chưa thanh toán") || 
                 trangThai.equalsIgnoreCase("Chưa đủ") ||
                 trangThai.equalsIgnoreCase("Nợ"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFeeUsed(int maKhoanThu) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_FEE_USED)) {

            pstmt.setInt(1, maKhoanThu);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if fee is used: " + maKhoanThu, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean canModifyPhieuThu(int maPhieu) {
        PhieuThu phieuThu = getPhieuThuWithDetails(maPhieu);
        if (phieuThu == null) {
            return false;
        }
        
        String trangThai = phieuThu.getTrangThai();
        if (trangThai == null) {
            return true; // No status means can modify
        }
        
        // Use normalized status comparison instead of fragile string matching
        String normalizedStatus = trangThai.trim().toLowerCase();
        
        // Define paid status values (case-insensitive)
        final String[] PAID_STATUSES = {
            "dathu", "đã thu", "đãthu",
            "dathanhtoan", "đã thanh toán", "đãthanhtoan",
            "hoanthanh", "hoàn thành", "hoànthành",
            "paid", "completed"
        };
        
        // Check if status matches any paid status
        for (String paidStatus : PAID_STATUSES) {
            if (normalizedStatus.equals(paidStatus) || normalizedStatus.contains(paidStatus)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
        if (phieuThu == null || phieuThu.getId() == 0) {
            logger.log(Level.WARNING, "Invalid PhieuThu: null or id is 0");
            return false;
        }

        // Check if receipt can be modified
        if (!canModifyPhieuThu(phieuThu.getId())) {
            logger.log(Level.WARNING, 
                    "Cannot update PhieuThu with id: " + phieuThu.getId() + " - already paid");
            return false;
        }

        // Validate: Nếu maDot thay đổi, kiểm tra đợt thu mới có đang mở không
        PhieuThu existingPhieuThu = getPhieuThuWithDetails(phieuThu.getId());
        if (existingPhieuThu != null && existingPhieuThu.getMaDot() != phieuThu.getMaDot()) {
            // Đợt thu đã thay đổi, kiểm tra đợt thu mới
            if (!isDotThuOpen(phieuThu.getMaDot())) {
                DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
                String errorMsg = "Không thể cập nhật phiếu thu: Đợt thu mới đã đóng";
                if (dotThu != null) {
                    errorMsg = "Không thể cập nhật phiếu thu: Đợt thu mới \"" + dotThu.getTenDot() + "\" đã đóng (trạng thái: " + dotThu.getTrangThai() + ")";
                }
                logger.log(Level.WARNING, "Cannot update PhieuThu: new DotThu with id " + phieuThu.getMaDot() + " is closed");
                throw new IllegalArgumentException(errorMsg);
            }
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Step 1: Update PhieuThu
                try (PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
                    pstmt.setInt(1, phieuThu.getMaHo());
                    pstmt.setInt(2, phieuThu.getMaDot());
                    pstmt.setInt(3, phieuThu.getMaTaiKhoan());
                    pstmt.setTimestamp(4, convertToSqlTimestamp(phieuThu.getNgayLap()));
                    pstmt.setBigDecimal(5, phieuThu.getTongTien());
                    pstmt.setString(6, phieuThu.getTrangThai());
                    pstmt.setString(7, phieuThu.getHinhThucThu());
                    pstmt.setString(8, null); // ghiChu
                    pstmt.setInt(9, phieuThu.getId());

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected <= 0) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to update PhieuThu with id: " + phieuThu.getId());
                        return false;
                    }
                }

                // Step 2: Update ChiTietThu if provided
                if (chiTietList != null) {
                    // Delete old details
                    try (PreparedStatement deleteStmt = conn.prepareStatement(DELETE_CHITIET_BY_PHIEU)) {
                        deleteStmt.setInt(1, phieuThu.getId());
                        deleteStmt.executeUpdate();
                    }

                    // Add new details (inline to use same connection)
                    if (!chiTietList.isEmpty()) {
                        try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_CHITIET)) {
                            for (ChiTietThu chiTiet : chiTietList) {
                                chiTiet.setMaPhieu(phieuThu.getId());
                                insertStmt.setInt(1, chiTiet.getMaPhieu());
                                insertStmt.setInt(2, chiTiet.getMaKhoan());
                                insertStmt.setBigDecimal(3, chiTiet.getSoLuong());
                                insertStmt.setBigDecimal(4, chiTiet.getDonGia());
                                insertStmt.setBigDecimal(5, chiTiet.getThanhTien());
                                insertStmt.setString(6, null); // ghiChu
                                insertStmt.addBatch();
                            }
                            insertStmt.executeBatch();
                        }
                    }
                }

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully updated PhieuThu with id: " + phieuThu.getId());
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating PhieuThu with id: " + phieuThu.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deletePhieuThu(int maPhieu) {
        // Check if receipt can be modified (deleted)
        if (!canModifyPhieuThu(maPhieu)) {
            logger.log(Level.WARNING, 
                    "Cannot delete PhieuThu with id: " + maPhieu + " - already paid");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Step 1: Delete ChiTietThu first
                try (PreparedStatement deleteChiTietStmt = conn.prepareStatement(DELETE_CHITIET_BY_PHIEU)) {
                    deleteChiTietStmt.setInt(1, maPhieu);
                    deleteChiTietStmt.executeUpdate();
                }

                // Step 2: Delete PhieuThu
                try (PreparedStatement deletePhieuStmt = conn.prepareStatement(DELETE)) {
                    deletePhieuStmt.setInt(1, maPhieu);
                    int rowsAffected = deletePhieuStmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        conn.commit();
                        logger.log(Level.INFO, "Successfully deleted PhieuThu with id: " + maPhieu);
                        return true;
                    } else {
                        conn.rollback();
                        logger.log(Level.WARNING, "PhieuThu not found with id: " + maPhieu);
                        return false;
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting PhieuThu with id: " + maPhieu, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng PhieuThu từ ResultSet.
     */
    private PhieuThu buildPhieuThuFromResultSet(ResultSet rs) throws SQLException {
        PhieuThu phieuThu = new PhieuThu();
        phieuThu.setId(rs.getInt("id"));
        phieuThu.setMaHo(rs.getInt("maHo"));
        phieuThu.setMaDot(rs.getInt("maDot"));
        phieuThu.setMaTaiKhoan(rs.getInt("maTaiKhoan"));
        
        Timestamp ngayLap = rs.getTimestamp("ngayLap");
        if (ngayLap != null) {
            phieuThu.setNgayLap(ngayLap.toLocalDateTime());
        }
        
        phieuThu.setTongTien(rs.getBigDecimal("tongTien"));
        phieuThu.setTrangThai(rs.getString("trangThai"));
        phieuThu.setHinhThucThu(rs.getString("hinhThucThu"));
        
        return phieuThu;
    }

    /**
     * Xây dựng đối tượng ChiTietThu từ ResultSet.
     */
    private ChiTietThu buildChiTietThuFromResultSet(ResultSet rs) throws SQLException {
        ChiTietThu chiTiet = new ChiTietThu();
        chiTiet.setId(rs.getInt("id"));
        chiTiet.setMaPhieu(rs.getInt("maPhieu"));
        chiTiet.setMaKhoan(rs.getInt("maKhoan"));
        chiTiet.setSoLuong(rs.getBigDecimal("soLuong"));
        chiTiet.setDonGia(rs.getBigDecimal("donGia"));
        chiTiet.setThanhTien(rs.getBigDecimal("thanhTien"));
        
        // Set tenKhoan if available from JOIN
        try {
            String tenKhoan = rs.getString("tenKhoan");
            chiTiet.setTenKhoan(tenKhoan);
        } catch (SQLException e) {
            // Column may not exist in some queries, ignore
        }
        
        return chiTiet;
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
