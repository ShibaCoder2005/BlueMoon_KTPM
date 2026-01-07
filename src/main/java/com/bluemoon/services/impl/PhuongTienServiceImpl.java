package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.PhuongTien;
import com.bluemoon.services.PhuongTienService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link PhuongTienService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class PhuongTienServiceImpl implements PhuongTienService {

    private static final Logger logger = Logger.getLogger(PhuongTienServiceImpl.class.getName());

    // SQL Queries
    private static final String SELECT_ALL = 
            "SELECT id, maHo, loaiXe, bienSo, tenChuXe, ngayDangKy FROM PhuongTien ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, maHo, loaiXe, bienSo, tenChuXe, ngayDangKy FROM PhuongTien WHERE id = ?";

    private static final String SELECT_BY_MAHO = 
            "SELECT id, maHo, loaiXe, bienSo, tenChuXe, ngayDangKy FROM PhuongTien WHERE maHo = ? ORDER BY id";

    private static final String SELECT_BY_BIENSO = 
            "SELECT id, maHo, loaiXe, bienSo, tenChuXe, ngayDangKy FROM PhuongTien WHERE UPPER(TRIM(bienSo)) = UPPER(TRIM(?))";

    private static final String INSERT = 
            "INSERT INTO PhuongTien (maHo, loaiXe, bienSo, tenChuXe, ngayDangKy) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE PhuongTien SET maHo = ?, loaiXe = ?, bienSo = ?, tenChuXe = ?, ngayDangKy = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM PhuongTien WHERE id = ?";

    private static final String SEARCH = 
            "SELECT id, maHo, loaiXe, bienSo, tenChuXe, ngayDangKy FROM PhuongTien " +
            "WHERE UPPER(TRIM(bienSo)) LIKE UPPER(TRIM(?)) OR " +
            "UPPER(TRIM(tenChuXe)) LIKE UPPER(TRIM(?)) OR " +
            "UPPER(TRIM(loaiXe)) LIKE UPPER(TRIM(?)) " +
            "ORDER BY id";

    private static final String CHECK_BIENSO_EXISTS = 
            "SELECT COUNT(*) FROM PhuongTien WHERE bienSo IS NOT NULL AND TRIM(bienSo) != '' AND UPPER(TRIM(bienSo)) = UPPER(TRIM(?)) AND id != ?";

    @Override
    public List<PhuongTien> getAllPhuongTien() {
        List<PhuongTien> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildPhuongTienFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " vehicles");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all vehicles", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public PhuongTien getPhuongTienById(int id) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildPhuongTienFromResultSet(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving vehicle with id: " + id, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<PhuongTien> getPhuongTienByHoGiaDinh(int maHo) {
        List<PhuongTien> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_MAHO)) {

            pstmt.setInt(1, maHo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildPhuongTienFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " vehicles for maHo: " + maHo);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving vehicles for maHo: " + maHo, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public PhuongTien getPhuongTienByBienSo(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_BIENSO)) {

            pstmt.setString(1, bienSo.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildPhuongTienFromResultSet(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving vehicle with bienSo: " + bienSo, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addPhuongTien(PhuongTien phuongTien) {
        if (phuongTien == null) {
            logger.log(Level.WARNING, "Cannot add vehicle: phuongTien is null");
            return false;
        }

        // Validate required fields
        if (phuongTien.getBienSo() == null || phuongTien.getBienSo().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot add vehicle: bienSo is empty");
            return false;
        }

        if (phuongTien.getMaHo() <= 0) {
            logger.log(Level.WARNING, "Cannot add vehicle: maHo is invalid");
            return false;
        }

        // Check if bienSo already exists (case-insensitive, trimmed)
        String bienSoToCheck = phuongTien.getBienSo().trim();
        if (isBienSoExists(bienSoToCheck, 0)) {
            logger.log(Level.WARNING, "Cannot add vehicle: bienSo already exists (case-insensitive): " + bienSoToCheck);
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            pstmt.setInt(1, phuongTien.getMaHo());
            pstmt.setString(2, phuongTien.getLoaiXe());
            pstmt.setString(3, phuongTien.getBienSo().trim());
            pstmt.setString(4, phuongTien.getTenChuXe());
            pstmt.setDate(5, phuongTien.getNgayDangKy() != null ? Date.valueOf(phuongTien.getNgayDangKy()) : null);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added vehicle: " + phuongTien.getBienSo() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to add vehicle: " + phuongTien.getBienSo() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            // Check for duplicate key violation (unique constraint on bienSo)
            String errorMessage = e.getMessage();
            if (errorMessage != null && (
                errorMessage.contains("duplicate key") || 
                errorMessage.contains("unique constraint") ||
                errorMessage.contains("UNIQUE constraint") ||
                errorMessage.contains("violates unique constraint")
            )) {
                logger.log(Level.WARNING, "Cannot add vehicle: bienSo already exists in database: " + phuongTien.getBienSo());
                return false;
            }
            logger.log(Level.SEVERE, "Error adding vehicle: " + phuongTien.getBienSo(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePhuongTien(PhuongTien phuongTien) {
        if (phuongTien == null || phuongTien.getId() == 0) {
            logger.log(Level.WARNING, "Cannot update vehicle: phuongTien is null or id is 0");
            return false;
        }

        // Validate required fields
        if (phuongTien.getBienSo() == null || phuongTien.getBienSo().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot update vehicle: bienSo is empty");
            return false;
        }

        if (phuongTien.getMaHo() <= 0) {
            logger.log(Level.WARNING, "Cannot update vehicle: maHo is invalid");
            return false;
        }

        // Check if bienSo already exists (excluding current record, case-insensitive)
        String bienSoToCheck = phuongTien.getBienSo().trim();
        if (isBienSoExists(bienSoToCheck, phuongTien.getId())) {
            logger.log(Level.WARNING, "Cannot update vehicle: bienSo already exists (case-insensitive): " + bienSoToCheck);
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {

            pstmt.setInt(1, phuongTien.getMaHo());
            pstmt.setString(2, phuongTien.getLoaiXe());
            pstmt.setString(3, phuongTien.getBienSo().trim());
            pstmt.setString(4, phuongTien.getTenChuXe());
            pstmt.setDate(5, phuongTien.getNgayDangKy() != null ? Date.valueOf(phuongTien.getNgayDangKy()) : null);
            pstmt.setInt(6, phuongTien.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully updated vehicle with id: " + phuongTien.getId() + " (rows affected: " + rowsAffected + ")");
            } else {
                logger.log(Level.WARNING, "Failed to update vehicle with id: " + phuongTien.getId() + " (rows affected: " + rowsAffected + ")");
            }

            return success;

        } catch (SQLException e) {
            // Check for duplicate key violation (unique constraint on bienSo)
            String errorMessage = e.getMessage();
            if (errorMessage != null && (
                errorMessage.contains("duplicate key") || 
                errorMessage.contains("unique constraint") ||
                errorMessage.contains("UNIQUE constraint") ||
                errorMessage.contains("violates unique constraint")
            )) {
                logger.log(Level.WARNING, "Cannot update vehicle: bienSo already exists in database: " + phuongTien.getBienSo());
                return false;
            }
            logger.log(Level.SEVERE, "Error updating vehicle with id: " + phuongTien.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deletePhuongTien(int id) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully deleted vehicle with id: " + id);
            } else {
                logger.log(Level.WARNING, "Failed to delete vehicle with id: " + id + " - vehicle may not exist");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting vehicle with id: " + id, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<PhuongTien> searchPhuongTien(String keyword) {
        List<PhuongTien> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPhuongTien();
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH)) {

            String searchPattern = "%" + keyword.trim() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildPhuongTienFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Found " + result.size() + " vehicles matching keyword: " + keyword);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching vehicles with keyword: " + keyword, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Helper method to check if bienSo already exists.
     */
    private boolean isBienSoExists(String bienSo, int excludeId) {
        if (bienSo == null || bienSo.trim().isEmpty()) {
            logger.log(Level.FINE, "isBienSoExists: bienSo is null or empty, returning false");
            return false;
        }

        // Trim the input - SQL will normalize both sides with UPPER(TRIM())
        String trimmedBienSo = bienSo.trim();
        String normalizedBienSo = trimmedBienSo.toUpperCase();
        logger.log(Level.INFO, "isBienSoExists: Checking bienSo='" + bienSo + "' (trimmed: '" + trimmedBienSo + "', normalized: '" + normalizedBienSo + "'), excludeId=" + excludeId);

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_BIENSO_EXISTS)) {

            // Set trimmed value - SQL will apply UPPER(TRIM()) to both sides for comparison
            pstmt.setString(1, trimmedBienSo);
            pstmt.setInt(2, excludeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    boolean exists = count > 0;
                    if (exists) {
                        logger.log(Level.WARNING, "isBienSoExists: bienSo='" + bienSo + "' (normalized: '" + normalizedBienSo + "') already exists. Count: " + count + ", excludeId: " + excludeId);
                        // Debug: Query and log matching records
                        try (PreparedStatement debugStmt = conn.prepareStatement(
                                "SELECT id, bienSo, UPPER(TRIM(bienSo)) as normalized FROM PhuongTien " +
                                "WHERE bienSo IS NOT NULL AND TRIM(bienSo) != '' AND UPPER(TRIM(bienSo)) = UPPER(TRIM(?)) AND id != ?")) {
                            debugStmt.setString(1, trimmedBienSo);
                            debugStmt.setInt(2, excludeId);
                            try (ResultSet debugRs = debugStmt.executeQuery()) {
                                List<String> matches = new ArrayList<>();
                                while (debugRs.next()) {
                                    int id = debugRs.getInt("id");
                                    String originalBienSo = debugRs.getString("bienSo");
                                    String normalized = debugRs.getString("normalized");
                                    matches.add("id=" + id + ", bienSo='" + originalBienSo + "', normalized='" + normalized + "'");
                                }
                                if (!matches.isEmpty()) {
                                    logger.log(Level.WARNING, "Matching bienSo records: " + String.join("; ", matches));
                                }
                            }
                        } catch (SQLException debugE) {
                            logger.log(Level.WARNING, "Error logging matching bienSo records", debugE);
                        }
                    } else {
                        logger.log(Level.INFO, "isBienSoExists: bienSo='" + bienSo + "' (normalized: '" + normalizedBienSo + "') not found. Count: " + count);
                    }
                    return exists;
                }
            }

            logger.log(Level.FINE, "isBienSoExists: No result from query, returning false");
            return false;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if bienSo exists: " + bienSo + " (normalized: " + normalizedBienSo + "), excludeId: " + excludeId, e);
            e.printStackTrace();
            // Return false on error to allow insertion (fail-safe)
            return false;
        }
    }

    /**
     * Helper method to build PhuongTien object from ResultSet.
     */
    private PhuongTien buildPhuongTienFromResultSet(ResultSet rs) throws SQLException {
        PhuongTien pt = new PhuongTien();
        pt.setId(rs.getInt("id"));
        pt.setMaHo(rs.getInt("maHo"));
        pt.setLoaiXe(rs.getString("loaiXe"));
        pt.setBienSo(rs.getString("bienSo"));
        pt.setTenChuXe(rs.getString("tenChuXe"));
        
        Date ngayDangKy = rs.getDate("ngayDangKy");
        if (ngayDangKy != null) {
            pt.setNgayDangKy(ngayDangKy.toLocalDate());
        }
        
        return pt;
    }
}

