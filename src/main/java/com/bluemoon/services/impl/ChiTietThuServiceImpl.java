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

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.services.ChiTietThuService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link ChiTietThuService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class ChiTietThuServiceImpl implements ChiTietThuService {

    private static final Logger logger = Logger.getLogger(ChiTietThuServiceImpl.class.getName());

    // SQL Queries
    private static final String SELECT_BY_MAPHIEU = 
            "SELECT ct.id, ct.maPhieu, ct.maKhoan, ct.soLuong, ct.donGia, ct.thanhTien, ct.ghiChu, kt.tenKhoan " +
            "FROM ChiTietThu ct " +
            "LEFT JOIN KhoanThu kt ON ct.maKhoan = kt.id " +
            "WHERE ct.maPhieu = ? ORDER BY ct.id";

    // Lưu ý: thanhTien cần được tính = soLuong * donGia và set khi INSERT
    private static final String INSERT = 
            "INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_BY_MAPHIEU = 
            "DELETE FROM ChiTietThu WHERE maPhieu = ?";

    @Override
    public List<ChiTietThu> getChiTietByMaPhieu(int maPhieu) {
        List<ChiTietThu> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_MAPHIEU)) {

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
    public boolean save(ChiTietThu chiTiet) {
        // 1. Kiểm tra ID: Nếu > 0 thì là Cập nhật, ngược lại là Thêm mới
        if (chiTiet.getId() > 0) {
            return update(chiTiet);
        } 
        
        // 2. (Tuỳ chọn) Kiểm tra xem khoản thu này đã có trong phiếu chưa để tránh trùng lặp
        // Nếu đã có (trùng maPhieu và maKhoan) -> Chuyển sang Update
        int existingId = findIdByPhieuAndKhoan(chiTiet.getMaPhieu(), chiTiet.getMaKhoan());
        if (existingId > 0) {
            chiTiet.setId(existingId);
            return update(chiTiet);
        }

        // 3. Nếu chưa có -> Thêm mới
        return insert(chiTiet);
    }

    // --- CÁC HÀM PHỤ TRỢ (Private) ---

    private boolean insert(ChiTietThu chiTiet) {
        String sql = "INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = DatabaseConnector.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            // Tính thành tiền
            BigDecimal thanhTien = chiTiet.getSoLuong().multiply(chiTiet.getDonGia());

            ps.setInt(1, chiTiet.getMaPhieu());
            ps.setInt(2, chiTiet.getMaKhoan());
            ps.setBigDecimal(3, chiTiet.getSoLuong());
            ps.setBigDecimal(4, chiTiet.getDonGia());
            ps.setBigDecimal(5, thanhTien); // Lưu thành tiền đã tính
            ps.setString(6, chiTiet.getGhiChu());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Cập nhật lại ID vừa sinh ra cho object
                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        chiTiet.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean update(ChiTietThu chiTiet) {
        String sql = "UPDATE ChiTietThu SET soLuong = ?, donGia = ?, thanhTien = ?, ghiChu = ? WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseConnector.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Tính lại thành tiền mới
            BigDecimal thanhTien = chiTiet.getSoLuong().multiply(chiTiet.getDonGia());

            ps.setBigDecimal(1, chiTiet.getSoLuong());
            ps.setBigDecimal(2, chiTiet.getDonGia());
            ps.setBigDecimal(3, thanhTien);
            ps.setString(4, chiTiet.getGhiChu());
            ps.setInt(5, chiTiet.getId()); // Quan trọng: Điều kiện WHERE id = ...
            
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int findIdByPhieuAndKhoan(int maPhieu, int maKhoan) {
        String sql = "SELECT id FROM ChiTietThu WHERE maPhieu = ? AND maKhoan = ?";
        try (java.sql.Connection conn = DatabaseConnector.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhieu);
            ps.setInt(2, maKhoan);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean saveAll(List<ChiTietThu> listChiTiet) {
        if (listChiTiet == null || listChiTiet.isEmpty()) {
            logger.log(Level.WARNING, "Cannot save ChiTietThu list: list is null or empty");
            return true; // Empty list is considered success
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            int batchCount = 0;
            for (ChiTietThu chiTiet : listChiTiet) {
                if (chiTiet == null) {
                    continue;
                }

                // Validate required fields
                if (chiTiet.getMaPhieu() <= 0 || chiTiet.getMaKhoan() <= 0) {
                    logger.log(Level.WARNING, "Skipping invalid ChiTietThu: maPhieu=" + chiTiet.getMaPhieu() + 
                        ", maKhoan=" + chiTiet.getMaKhoan());
                    continue;
                }

                // Validate donGia and soLuong (thanhTien sẽ được tính = soLuong * donGia)
                if (chiTiet.getDonGia() == null || chiTiet.getSoLuong() == null) {
                    logger.log(Level.WARNING, "Skipping ChiTietThu with invalid donGia or soLuong");
                    continue;
                }

                // Tính thanhTien = soLuong * donGia
                BigDecimal thanhTien = chiTiet.getSoLuong().multiply(chiTiet.getDonGia());

                pstmt.setInt(1, chiTiet.getMaPhieu());
                pstmt.setInt(2, chiTiet.getMaKhoan());
                pstmt.setBigDecimal(3, chiTiet.getSoLuong());
                pstmt.setBigDecimal(4, chiTiet.getDonGia());
                pstmt.setBigDecimal(5, thanhTien);
                pstmt.setString(6, null); // ghiChu

                pstmt.addBatch();
                batchCount++;
            }

            if (batchCount == 0) {
                logger.log(Level.WARNING, "No valid ChiTietThu records to save");
                return false;
            }

            int[] results = pstmt.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result >= 0) { // PreparedStatement.SUCCESS_NO_INFO or positive update count
                    successCount++;
                }
            }

            boolean success = successCount == batchCount;
            if (success) {
                logger.log(Level.INFO, "Successfully saved " + successCount + " ChiTietThu records in batch");
            } else {
                logger.log(Level.WARNING, "Partially saved ChiTietThu: " + successCount + "/" + batchCount);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving ChiTietThu list in batch", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save all ChiTietThu using an existing connection (for transaction support).
     * 
     * @param listChiTiet List of ChiTietThu to save
     * @param conn Existing database connection
     * @return true if successful
     */
    public boolean saveAll(List<ChiTietThu> listChiTiet, Connection conn) {
        if (listChiTiet == null || listChiTiet.isEmpty()) {
            return true; // Empty list is considered success
        }

        try (PreparedStatement pstmt = conn.prepareStatement(INSERT)) {
            int batchCount = 0;
            for (ChiTietThu chiTiet : listChiTiet) {
                if (chiTiet == null) {
                    continue;
                }

                // Validate required fields
                if (chiTiet.getMaPhieu() <= 0 || chiTiet.getMaKhoan() <= 0) {
                    logger.log(Level.WARNING, "Skipping invalid ChiTietThu: maPhieu=" + chiTiet.getMaPhieu() + 
                        ", maKhoan=" + chiTiet.getMaKhoan());
                    continue;
                }

                // Validate donGia and soLuong (thanhTien sẽ được tính = soLuong * donGia)
                if (chiTiet.getDonGia() == null || chiTiet.getSoLuong() == null) {
                    logger.log(Level.WARNING, "Skipping ChiTietThu with invalid donGia or soLuong");
                    continue;
                }

                // Tính thanhTien = soLuong * donGia
                BigDecimal thanhTien = chiTiet.getSoLuong().multiply(chiTiet.getDonGia());

                pstmt.setInt(1, chiTiet.getMaPhieu());
                pstmt.setInt(2, chiTiet.getMaKhoan());
                pstmt.setBigDecimal(3, chiTiet.getSoLuong());
                pstmt.setBigDecimal(4, chiTiet.getDonGia());
                pstmt.setBigDecimal(5, thanhTien);
                pstmt.setString(6, null); // ghiChu

                pstmt.addBatch();
                batchCount++;
            }

            if (batchCount == 0) {
                logger.log(Level.WARNING, "No valid ChiTietThu records to save");
                return false;
            }

            int[] results = pstmt.executeBatch();
            int successCount = 0;
            for (int result : results) {
                if (result >= 0) {
                    successCount++;
                }
            }

            boolean success = successCount == batchCount;
            if (success) {
                logger.log(Level.FINE, "Successfully saved " + successCount + " ChiTietThu records in batch (transaction)");
            } else {
                logger.log(Level.WARNING, "Partially saved ChiTietThu in transaction: " + successCount + "/" + batchCount);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving ChiTietThu list in batch (transaction)", e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteByMaPhieu(int maPhieu) {
        if (maPhieu <= 0) {
            logger.log(Level.WARNING, "Cannot delete ChiTietThu: maPhieu is invalid");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_BY_MAPHIEU)) {

            pstmt.setInt(1, maPhieu);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected >= 0; // 0 rows is also success (nothing to delete)

            if (success) {
                logger.log(Level.INFO, "Successfully deleted " + rowsAffected + " ChiTietThu records for maPhieu: " + maPhieu);
            } else {
                logger.log(Level.WARNING, "Failed to delete ChiTietThu for maPhieu: " + maPhieu);
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting ChiTietThu for maPhieu: " + maPhieu, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete all ChiTietThu by maPhieu using an existing connection (for transaction support).
     * 
     * @param maPhieu Receipt ID
     * @param conn Existing database connection
     * @return true if successful
     */
    public boolean deleteByMaPhieu(int maPhieu, Connection conn) {
        if (maPhieu <= 0) {
            logger.log(Level.WARNING, "Cannot delete ChiTietThu: maPhieu is invalid");
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(DELETE_BY_MAPHIEU)) {
            pstmt.setInt(1, maPhieu);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected >= 0;

            if (success) {
                logger.log(Level.FINE, "Successfully deleted " + rowsAffected + " ChiTietThu records for maPhieu: " + maPhieu + " (transaction)");
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting ChiTietThu for maPhieu: " + maPhieu + " (transaction)", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng ChiTietThu từ ResultSet.
     * 
     * @param rs ResultSet đã được positioned tại row cần đọc
     * @return đối tượng ChiTietThu
     * @throws SQLException nếu có lỗi khi đọc dữ liệu
     */
    private ChiTietThu buildChiTietThuFromResultSet(ResultSet rs) throws SQLException {
        ChiTietThu chiTiet = new ChiTietThu();
        chiTiet.setId(rs.getInt("id"));
        chiTiet.setMaPhieu(rs.getInt("maPhieu"));
        chiTiet.setMaKhoan(rs.getInt("maKhoan"));
        chiTiet.setSoLuong(rs.getBigDecimal("soLuong"));
        chiTiet.setDonGia(rs.getBigDecimal("donGia"));
        chiTiet.setThanhTien(rs.getBigDecimal("thanhTien"));
        chiTiet.setGhiChu(rs.getString("ghiChu"));
        
        // Set tenKhoan if available from JOIN
        try {
            String tenKhoan = rs.getString("tenKhoan");
            chiTiet.setTenKhoan(tenKhoan);
        } catch (SQLException e) {
            // Column may not exist in some queries, ignore
            logger.log(Level.FINE, "tenKhoan column not found in ResultSet, ignoring");
        }
        
        return chiTiet;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM ChiTietThu WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseConnector.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
            
        } catch (java.sql.SQLException e) {
            logger.log(Level.SEVERE, "Error deleting detail id: " + id, e);
            e.printStackTrace();
            return false;
        }
    }
}

