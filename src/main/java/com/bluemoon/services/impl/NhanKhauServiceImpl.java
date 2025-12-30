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

import com.bluemoon.models.LichSuNhanKhau;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.NhanKhauService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link NhanKhauService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class NhanKhauServiceImpl implements NhanKhauService {

    private static final Logger logger = Logger.getLogger(NhanKhauServiceImpl.class.getName());
    
    // SQL Queries for NhanKhau
    private static final String SELECT_ALL = 
            "SELECT id, maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang FROM NhanKhau ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang FROM NhanKhau WHERE id = ?";

    private static final String SELECT_BY_MAHO = 
            "SELECT id, maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang FROM NhanKhau WHERE maHo = ? ORDER BY id";

    private static final String INSERT = 
            "INSERT INTO NhanKhau (maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE NhanKhau SET maHo = ?, hoTen = ?, ngaySinh = ?, gioiTinh = ?, soCCCD = ?, ngheNghiep = ?, quanHeVoiChuHo = ?, tinhTrang = ? WHERE id = ?";

    private static final String UPDATE_STATUS = 
            "UPDATE NhanKhau SET tinhTrang = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM NhanKhau WHERE id = ?";

    private static final String CHECK_CCCD_EXISTS = 
            "SELECT COUNT(*) FROM NhanKhau WHERE LOWER(TRIM(soCCCD)) = LOWER(TRIM(?)) AND id != ?";

    // SQL Queries for LichSuNhanKhau
    private static final String SELECT_LICHSU_BY_NHAN_KHAU = 
            "SELECT id, maNhanKhau, loaiBienDong, ngayBatDau, ngayKetThuc, nguoiGhi FROM LichSuNhanKhau WHERE maNhanKhau = ? ORDER BY ngayBatDau DESC";

    private static final String SELECT_ALL_LICHSU = 
            "SELECT id, maNhanKhau, loaiBienDong, ngayBatDau, ngayKetThuc, nguoiGhi FROM LichSuNhanKhau ORDER BY ngayBatDau DESC";

    private static final String INSERT_LICHSU = 
            "INSERT INTO LichSuNhanKhau (maNhanKhau, loaiBienDong, ngayBatDau, ngayKetThuc, nguoiGhi) VALUES (?, ?, ?, ?, ?)";

    @Override
    public List<NhanKhau> getAll() {
        List<NhanKhau> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildNhanKhauFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " residents");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all residents", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public NhanKhau findById(int id) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildNhanKhauFromResultSet(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving resident with id: " + id, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<NhanKhau> getNhanKhauByHoGiaDinh(int maHo) {
        List<NhanKhau> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_MAHO)) {

            pstmt.setInt(1, maHo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildNhanKhauFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " residents for household: " + maHo);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving residents for household: " + maHo, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addNhanKhau(NhanKhau nhanKhau) {
        if (nhanKhau == null) {
            logger.log(Level.WARNING, "Cannot add resident: nhanKhau is null");
            return false;
        }

        // Validate required fields
        if (nhanKhau.getHoTen() == null || nhanKhau.getHoTen().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot add resident: hoTen is empty");
            return false;
        }

        // Check CCCD uniqueness if provided
        if (nhanKhau.getSoCCCD() != null && !nhanKhau.getSoCCCD().trim().isEmpty()) {
            if (isCCCDExists(nhanKhau.getSoCCCD(), 0)) {
                logger.log(Level.WARNING, "Cannot add resident: CCCD already exists: " + nhanKhau.getSoCCCD());
                return false;
            }
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Step 1: Insert new resident
                int newResidentId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    pstmt.setInt(1, nhanKhau.getMaHo());
                    pstmt.setString(2, nhanKhau.getHoTen().trim());
                    pstmt.setDate(3, convertToSqlDate(nhanKhau.getNgaySinh()));
                    pstmt.setString(4, nhanKhau.getGioiTinh());
                    pstmt.setString(5, nhanKhau.getSoCCCD());
                    pstmt.setString(6, nhanKhau.getNgheNghiep());
                    pstmt.setString(7, nhanKhau.getQuanHeVoiChuHo());
                    pstmt.setString(8, nhanKhau.getTinhTrang() != null ? nhanKhau.getTinhTrang() : "CuTru");

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected <= 0) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to add resident: " + nhanKhau.getHoTen() + " (rows affected: " + rowsAffected + ")");
                        return false;
                    }

                    // Get generated ID
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            newResidentId = generatedKeys.getInt(1);
                        } else {
                            // Fallback: query by unique fields to get ID
                            try (PreparedStatement findStmt = conn.prepareStatement(
                                    "SELECT id FROM NhanKhau WHERE maHo = ? AND hoTen = ? AND soCCCD = ? ORDER BY id DESC LIMIT 1")) {
                                findStmt.setInt(1, nhanKhau.getMaHo());
                                findStmt.setString(2, nhanKhau.getHoTen().trim());
                                findStmt.setString(3, nhanKhau.getSoCCCD() != null ? nhanKhau.getSoCCCD() : "");
                                try (ResultSet rs = findStmt.executeQuery()) {
                                    if (rs.next()) {
                                        newResidentId = rs.getInt("id");
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 2: Automatically create history record for new resident (inline to use same connection)
                if (newResidentId > 0) {
                    try (PreparedStatement historyStmt = conn.prepareStatement(INSERT_LICHSU)) {
                        historyStmt.setInt(1, newResidentId);
                        historyStmt.setString(2, "Đăng ký cư trú");
                        historyStmt.setDate(3, convertToSqlDate(LocalDate.now()));
                        historyStmt.setDate(4, null); // ngayKetThuc
                        historyStmt.setInt(5, 1); // Default admin ID, có thể lấy từ session sau

                        int historyRowsAffected = historyStmt.executeUpdate();
                        if (historyRowsAffected <= 0) {
                            conn.rollback();
                            logger.log(Level.WARNING, "Resident added but failed to create history record for ID: " + newResidentId + ", transaction rolled back");
                            return false;
                        }
                    }
                    
                    // Step 3: If this resident is "Chủ hộ", update maChuHo in HoGiaDinh
                    String quanHe = nhanKhau.getQuanHeVoiChuHo();
                    if (quanHe != null && quanHe.trim().equalsIgnoreCase("Chủ hộ")) {
                        try (PreparedStatement updateHoStmt = conn.prepareStatement(
                                "UPDATE HoGiaDinh SET maChuHo = ? WHERE id = ?")) {
                            updateHoStmt.setInt(1, newResidentId);
                            updateHoStmt.setInt(2, nhanKhau.getMaHo());
                            
                            int updateRows = updateHoStmt.executeUpdate();
                            if (updateRows > 0) {
                                logger.log(Level.INFO, "Updated maChuHo in HoGiaDinh (id: " + nhanKhau.getMaHo() + 
                                    ") to resident ID: " + newResidentId);
                            } else {
                                logger.log(Level.WARNING, "Failed to update maChuHo in HoGiaDinh (id: " + 
                                    nhanKhau.getMaHo() + ") - household may not exist");
                            }
                        }
                    }
                } else {
                    conn.rollback();
                    logger.log(Level.WARNING, "Failed to get generated ID for resident, transaction rolled back");
                    return false;
                }

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully added resident: " + nhanKhau.getHoTen() + " (ID: " + newResidentId + ") with history record");
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding resident: " + nhanKhau.getHoTen(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateNhanKhau(NhanKhau nhanKhau) {
        if (nhanKhau == null || nhanKhau.getId() == 0) {
            logger.log(Level.WARNING, "Cannot update resident: nhanKhau is null or id is 0");
            return false;
        }

        // Validate required fields
        if (nhanKhau.getHoTen() == null || nhanKhau.getHoTen().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot update resident: hoTen is empty");
            return false;
        }

        // Check CCCD uniqueness (exclude current record)
        if (nhanKhau.getSoCCCD() != null && !nhanKhau.getSoCCCD().trim().isEmpty()) {
            if (isCCCDExists(nhanKhau.getSoCCCD(), nhanKhau.getId())) {
                logger.log(Level.WARNING, "Cannot update resident: CCCD already exists: " + nhanKhau.getSoCCCD());
                return false;
            }
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);
            
            try {
                // Get old relationship to check if maChuHo needs to be updated
                String oldQuanHe = null;
                int oldMaHo = 0;
                try (PreparedStatement getOldStmt = conn.prepareStatement(
                        "SELECT quanHeVoiChuHo, maHo FROM NhanKhau WHERE id = ?")) {
                    getOldStmt.setInt(1, nhanKhau.getId());
                    try (ResultSet rs = getOldStmt.executeQuery()) {
                        if (rs.next()) {
                            oldQuanHe = rs.getString("quanHeVoiChuHo");
                            oldMaHo = rs.getInt("maHo");
                        }
                    }
                }
                
                // Update resident
                try (PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
                    pstmt.setInt(1, nhanKhau.getMaHo());
                    pstmt.setString(2, nhanKhau.getHoTen().trim());
                    pstmt.setDate(3, convertToSqlDate(nhanKhau.getNgaySinh()));
                    pstmt.setString(4, nhanKhau.getGioiTinh());
                    pstmt.setString(5, nhanKhau.getSoCCCD());
                    pstmt.setString(6, nhanKhau.getNgheNghiep());
                    pstmt.setString(7, nhanKhau.getQuanHeVoiChuHo());
                    pstmt.setString(8, nhanKhau.getTinhTrang());
                    pstmt.setInt(9, nhanKhau.getId());

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected <= 0) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to update resident with id: " + nhanKhau.getId());
                        return false;
                    }
                }
                
                // Update maChuHo in HoGiaDinh if relationship changed
                String newQuanHe = nhanKhau.getQuanHeVoiChuHo();
                boolean wasChuHo = oldQuanHe != null && oldQuanHe.trim().equalsIgnoreCase("Chủ hộ");
                boolean isChuHo = newQuanHe != null && newQuanHe.trim().equalsIgnoreCase("Chủ hộ");
                boolean maHoChanged = oldMaHo > 0 && oldMaHo != nhanKhau.getMaHo();
                
                // If was chu ho in old household, clear maChuHo in old household
                if (wasChuHo && oldMaHo > 0 && (maHoChanged || !isChuHo)) {
                    try (PreparedStatement clearStmt = conn.prepareStatement(
                            "UPDATE HoGiaDinh SET maChuHo = NULL WHERE id = ? AND maChuHo = ?")) {
                        clearStmt.setInt(1, oldMaHo);
                        clearStmt.setInt(2, nhanKhau.getId());
                        clearStmt.executeUpdate();
                        logger.log(Level.INFO, "Cleared maChuHo in HoGiaDinh (id: " + oldMaHo + 
                            ") - resident no longer is Chủ hộ or moved to different household");
                    }
                }
                
                // If is now chu ho, update maChuHo in new household
                if (isChuHo && nhanKhau.getMaHo() > 0) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE HoGiaDinh SET maChuHo = ? WHERE id = ?")) {
                        updateStmt.setInt(1, nhanKhau.getId());
                        updateStmt.setInt(2, nhanKhau.getMaHo());
                        
                        int updateRows = updateStmt.executeUpdate();
                        if (updateRows > 0) {
                            logger.log(Level.INFO, "Updated maChuHo in HoGiaDinh (id: " + nhanKhau.getMaHo() + 
                                ") to resident ID: " + nhanKhau.getId());
                        } else {
                            logger.log(Level.WARNING, "Failed to update maChuHo in HoGiaDinh (id: " + 
                                nhanKhau.getMaHo() + ") - household may not exist");
                        }
                    }
                }
                
                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully updated resident with id: " + nhanKhau.getId());
        return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating resident with id: " + nhanKhau.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addLichSuNhanKhau(LichSuNhanKhau history) {
        if (history == null) {
            logger.log(Level.WARNING, "Cannot add history: history is null");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_LICHSU)) {

            pstmt.setInt(1, history.getMaNhanKhau());
            pstmt.setString(2, history.getLoaiBienDong());
            pstmt.setDate(3, convertToSqlDate(history.getNgayBatDau()));
            pstmt.setDate(4, convertToSqlDate(history.getNgayKetThuc()));
            pstmt.setInt(5, history.getNguoiGhi());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added history for resident: " + history.getMaNhanKhau() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to add history for resident: " + history.getMaNhanKhau() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding history for resident: " + history.getMaNhanKhau(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<LichSuNhanKhau> getLichSuNhanKhau(int maNhanKhau) {
        List<LichSuNhanKhau> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_LICHSU_BY_NHAN_KHAU)) {

            pstmt.setInt(1, maNhanKhau);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildLichSuFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " history records for resident: " + maNhanKhau);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving history for resident: " + maNhanKhau, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<LichSuNhanKhau> getAllLichSuNhanKhau() {
        List<LichSuNhanKhau> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_LICHSU);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildLichSuFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " total history records for all residents");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all resident history", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateStatusWithHistory(int nhanKhauId, String newStatus, LichSuNhanKhau historyRecord) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            logger.log(Level.WARNING, "Invalid status: newStatus is null or empty");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Get old status
            NhanKhau existing = findById(nhanKhauId);
            if (existing == null) {
            logger.log(Level.WARNING, "Resident not found with id: " + nhanKhauId);
            return false;
        }
            String oldStatus = existing.getTinhTrang();

            // Update status
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_STATUS)) {
                pstmt.setString(1, newStatus.trim());
                pstmt.setInt(2, nhanKhauId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    logger.log(Level.WARNING, "Failed to update status for resident: " + nhanKhauId);
                    return false;
                }
            }

            // Record history if provided
            if (historyRecord != null) {
                historyRecord.setMaNhanKhau(nhanKhauId);
                if (historyRecord.getNgayBatDau() == null) {
                    historyRecord.setNgayBatDau(LocalDate.now());
                }
                if (historyRecord.getLoaiBienDong() == null || historyRecord.getLoaiBienDong().trim().isEmpty()) {
                    historyRecord.setLoaiBienDong("Thay đổi từ " + oldStatus + " sang " + newStatus);
                }
                
                boolean historySuccess = addLichSuNhanKhau(historyRecord);
                if (!historySuccess) {
                    logger.log(Level.WARNING, "Status updated but failed to record history for resident id: " + nhanKhauId);
                }
            }

            logger.log(Level.INFO, "Successfully updated status from '" + oldStatus + "' to '" + newStatus + "' for resident id: " + nhanKhauId);
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in updateStatusWithHistory for resident id: " + nhanKhauId, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteNhanKhau(int id) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);
            
            try {
                // Step 1: Clear maChuHo in HoGiaDinh if this resident is a household head
                try (PreparedStatement clearChuHoStmt = conn.prepareStatement(
                        "UPDATE HoGiaDinh SET maChuHo = NULL WHERE maChuHo = ?")) {
                    clearChuHoStmt.setInt(1, id);
                    int clearedRows = clearChuHoStmt.executeUpdate();
                    if (clearedRows > 0) {
                        logger.log(Level.INFO, "Cleared maChuHo in " + clearedRows + 
                            " household(s) before deleting resident id: " + id);
                    }
                }

                // Step 2: Delete all history records for this resident
                try (PreparedStatement deleteHistoryStmt = conn.prepareStatement(
                        "DELETE FROM LichSuNhanKhau WHERE maNhanKhau = ?")) {
                    deleteHistoryStmt.setInt(1, id);
                    int historyRowsDeleted = deleteHistoryStmt.executeUpdate();
                    logger.log(Level.INFO, "Deleted " + historyRowsDeleted + " history records for resident id: " + id);
                }

                // Step 3: Delete the resident
                try (PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
                    pstmt.setInt(1, id);
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected <= 0) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to delete resident with id: " + id + 
                            " (rows affected: " + rowsAffected + ") - resident may not exist");
                        return false;
                    }
                    
                    // Commit transaction
                    conn.commit();
                    logger.log(Level.INFO, "Successfully deleted resident with id: " + id + 
                        " (rows affected: " + rowsAffected + ")");
                    return true;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting resident with id: " + id, e);
            e.printStackTrace();
            
            // Check if it's a foreign key constraint violation
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("foreign key") || errorMessage.contains("violates foreign key constraint"))) {
                logger.log(Level.WARNING, "Cannot delete resident id: " + id + " due to foreign key constraint - resident may be referenced by other records");
            }
            
            return false;
        }
    }

    @Override
    public boolean isCCCDExists(String soCCCD, int excludeId) {
        if (soCCCD == null || soCCCD.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_CCCD_EXISTS)) {

            pstmt.setString(1, soCCCD.trim());
            pstmt.setInt(2, excludeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking CCCD existence: " + soCCCD, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng NhanKhau từ ResultSet.
     */
    private NhanKhau buildNhanKhauFromResultSet(ResultSet rs) throws SQLException {
        NhanKhau nhanKhau = new NhanKhau();
        nhanKhau.setId(rs.getInt("id"));
        nhanKhau.setMaHo(rs.getInt("maHo"));
        nhanKhau.setHoTen(rs.getString("hoTen"));
        
        Date ngaySinh = rs.getDate("ngaySinh");
        if (ngaySinh != null) {
            nhanKhau.setNgaySinh(ngaySinh.toLocalDate());
        }
        
        nhanKhau.setGioiTinh(rs.getString("gioiTinh"));
        nhanKhau.setSoCCCD(rs.getString("soCCCD"));
        nhanKhau.setNgheNghiep(rs.getString("ngheNghiep"));
        nhanKhau.setQuanHeVoiChuHo(rs.getString("quanHeVoiChuHo"));
        nhanKhau.setTinhTrang(rs.getString("tinhTrang"));
        
        return nhanKhau;
    }

    /**
     * Xây dựng đối tượng LichSuNhanKhau từ ResultSet.
     */
    private LichSuNhanKhau buildLichSuFromResultSet(ResultSet rs) throws SQLException {
        LichSuNhanKhau lichSu = new LichSuNhanKhau();
        lichSu.setId(rs.getInt("id"));
        lichSu.setMaNhanKhau(rs.getInt("maNhanKhau"));
        lichSu.setLoaiBienDong(rs.getString("loaiBienDong"));
        
        Date ngayBatDau = rs.getDate("ngayBatDau");
        if (ngayBatDau != null) {
            lichSu.setNgayBatDau(ngayBatDau.toLocalDate());
        }
        
        Date ngayKetThuc = rs.getDate("ngayKetThuc");
        if (ngayKetThuc != null) {
            lichSu.setNgayKetThuc(ngayKetThuc.toLocalDate());
        }
        
        lichSu.setNguoiGhi(rs.getInt("nguoiGhi"));
        
        return lichSu;
    }

    /**
     * Chuyển đổi LocalDate sang java.sql.Date.
     */
    private Date convertToSqlDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.valueOf(localDate);
    }
}
