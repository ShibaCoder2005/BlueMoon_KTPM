package com.bluemoon.services.impl;

import java.math.BigDecimal;
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

import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link HoGiaDinhService} với database integration.
 * Sử dụng PreparedStatement để tránh SQL injection và kiểm tra ràng buộc trước khi xóa.
 * Sử dụng try-with-resources để đảm bảo resources được đóng tự động.
 */
public class HoGiaDinhServiceImpl implements HoGiaDinhService {

    private static final Logger logger = Logger.getLogger(HoGiaDinhServiceImpl.class.getName());

    // SQL Queries - PostgreSQL table names
    private static final String SELECT_ALL = 
            "SELECT id, maHo, soPhong, dienTich, maChuHo, ghiChu, ngayTao FROM HoGiaDinh ORDER BY id";

    private static final String SELECT_BY_ID = 
            "SELECT id, maHo, soPhong, dienTich, maChuHo, ghiChu, ngayTao FROM HoGiaDinh WHERE id = ?";

    private static final String INSERT = 
            "INSERT INTO HoGiaDinh (maHo, soPhong, dienTich, maChuHo, ghiChu, ngayTao) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE HoGiaDinh SET maHo = ?, soPhong = ?, dienTich = ?, maChuHo = ?, ghiChu = ?, ngayTao = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM HoGiaDinh WHERE id = ?";

    private static final String CHECK_MAHO_EXISTS = 
            "SELECT COUNT(*) FROM HoGiaDinh WHERE maHo = ?";

    private static final String CHECK_MAHO_EXISTS_EXCLUDE_ID = 
            "SELECT COUNT(*) FROM HoGiaDinh WHERE maHo = ? AND id != ?";

    private static final String CHECK_NHAN_KHAU_DEPENDENCIES = 
            "SELECT COUNT(*) FROM NhanKhau WHERE maHo = ?";

    private static final String CHECK_PHIEU_THU_DEPENDENCIES = 
            "SELECT COUNT(*) FROM PhieuThu WHERE maHo = ?";

    private static final String CHECK_PHUONG_TIEN_DEPENDENCIES = 
            "SELECT COUNT(*) FROM PhuongTien WHERE maHo = ?";

    private static final String DELETE_NHAN_KHAU_BY_MAHO = 
            "DELETE FROM NhanKhau WHERE maHo = ?";

    private static final String DELETE_PHIEU_THU_BY_MAHO = 
            "DELETE FROM PhieuThu WHERE maHo = ?";

    private static final String DELETE_PHUONG_TIEN_BY_MAHO = 
            "DELETE FROM PhuongTien WHERE maHo = ?";

    private static final String SEARCH = 
            "SELECT id, maHo, soPhong, dienTich, maChuHo, ghiChu, ngayTao FROM HoGiaDinh " +
            "WHERE maHo LIKE ? OR ghiChu LIKE ? ORDER BY id";

    @Override
    public List<HoGiaDinh> getAllHoGiaDinh() {
        List<HoGiaDinh> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result.add(buildHoGiaDinhFromResultSet(rs));
            }

            logger.log(Level.INFO, "Retrieved " + result.size() + " households");
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all households", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public HoGiaDinh findById(int id) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildHoGiaDinhFromResultSet(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving household with id: " + id, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addHoGiaDinh(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh == null) {
            return false;
        }

        // Validate maHo not empty
        if (hoGiaDinh.getMaHo() == null || hoGiaDinh.getMaHo().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot add household: maHo is empty");
            return false;
        }

        // Validate dienTich >= 0 (allow 0 or null, but if provided must be >= 0)
        if (hoGiaDinh.getDienTich() != null && hoGiaDinh.getDienTich().compareTo(BigDecimal.ZERO) < 0) {
            logger.log(Level.WARNING, "Cannot add household: dienTich cannot be negative");
            return false;
        }

        // Check if maHo already exists
        if (checkMaHoExists(hoGiaDinh.getMaHo())) {
            logger.log(Level.WARNING, "Cannot add household: maHo already exists: " + hoGiaDinh.getMaHo());
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            pstmt.setString(1, hoGiaDinh.getMaHo().trim());
            pstmt.setInt(2, hoGiaDinh.getSoPhong());
            // Allow null dienTich
            if (hoGiaDinh.getDienTich() != null) {
                pstmt.setBigDecimal(3, hoGiaDinh.getDienTich());
            } else {
                pstmt.setNull(3, java.sql.Types.DECIMAL);
            }
            // Allow null or 0 maChuHo (chủ hộ sẽ được gán sau trong giao diện Cư dân)
            if (hoGiaDinh.getMaChuHo() > 0) {
                pstmt.setInt(4, hoGiaDinh.getMaChuHo());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setString(5, hoGiaDinh.getGhiChu());
            pstmt.setDate(6, convertToSqlDate(hoGiaDinh.getNgayTao() != null ? hoGiaDinh.getNgayTao() : LocalDate.now()));

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added household: " + hoGiaDinh.getMaHo());
            } else {
                logger.log(Level.WARNING, "Failed to add household: " + hoGiaDinh.getMaHo());
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding household: " + hoGiaDinh.getMaHo(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateHoGiaDinh(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh == null || hoGiaDinh.getId() == 0) {
            return false;
        }

        // Validate maHo not empty
        if (hoGiaDinh.getMaHo() == null || hoGiaDinh.getMaHo().trim().isEmpty()) {
            logger.log(Level.WARNING, "Cannot update household: maHo is empty");
            return false;
        }

        // Validate dienTich >= 0 (allow 0 or null, but if provided must be >= 0)
        if (hoGiaDinh.getDienTich() != null && hoGiaDinh.getDienTich().compareTo(BigDecimal.ZERO) < 0) {
            logger.log(Level.WARNING, "Cannot update household: dienTich cannot be negative");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Check maHo uniqueness (exclude current record)
            boolean maHoExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(CHECK_MAHO_EXISTS_EXCLUDE_ID)) {
                pstmt.setString(1, hoGiaDinh.getMaHo().trim());
                pstmt.setInt(2, hoGiaDinh.getId());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        maHoExists = true;
                    }
                }
            }

            if (maHoExists) {
                logger.log(Level.WARNING, 
                        "Cannot update household: maHo already exists: " + hoGiaDinh.getMaHo());
                return false;
            }

            // Proceed with update
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
                pstmt.setString(1, hoGiaDinh.getMaHo().trim());
                pstmt.setInt(2, hoGiaDinh.getSoPhong());
                // Allow null dienTich
                if (hoGiaDinh.getDienTich() != null) {
                    pstmt.setBigDecimal(3, hoGiaDinh.getDienTich());
                } else {
                    pstmt.setNull(3, java.sql.Types.DECIMAL);
                }
                // Allow null or 0 maChuHo
                if (hoGiaDinh.getMaChuHo() > 0) {
                    pstmt.setInt(4, hoGiaDinh.getMaChuHo());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }
                pstmt.setString(5, hoGiaDinh.getGhiChu());
                pstmt.setDate(6, convertToSqlDate(hoGiaDinh.getNgayTao()));
                pstmt.setInt(7, hoGiaDinh.getId());

                int rowsAffected = pstmt.executeUpdate();
                boolean success = rowsAffected > 0;

                if (success) {
                    logger.log(Level.INFO, "Successfully updated household with id: " + hoGiaDinh.getId());
                } else {
                    logger.log(Level.WARNING, "Failed to update household with id: " + hoGiaDinh.getId());
                }

                return success;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating household with id: " + hoGiaDinh.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteHoGiaDinh(int id) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Check for dependencies
                int nhanKhauCount = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(CHECK_NHAN_KHAU_DEPENDENCIES)) {
                    pstmt.setInt(1, id);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            nhanKhauCount = rs.getInt(1);
                        }
                    }
                }

                int phieuThuCount = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(CHECK_PHIEU_THU_DEPENDENCIES)) {
                    pstmt.setInt(1, id);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            phieuThuCount = rs.getInt(1);
                        }
                    }
                }

                int phuongTienCount = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(CHECK_PHUONG_TIEN_DEPENDENCIES)) {
                    pstmt.setInt(1, id);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            phuongTienCount = rs.getInt(1);
                        }
                    }
                }

                // Delete related records first (CASCADE delete)
                // 1. Delete LichSuNhanKhau for residents in this household
                if (nhanKhauCount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "DELETE FROM LichSuNhanKhau WHERE maNhanKhau IN (SELECT id FROM NhanKhau WHERE maHo = ?)")) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        logger.log(Level.INFO, "Deleted " + deleted + " LichSuNhanKhau records for household id: " + id);
                    }
                }

                // 2. Delete NhanKhau records
                if (nhanKhauCount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(DELETE_NHAN_KHAU_BY_MAHO)) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        logger.log(Level.INFO, "Deleted " + deleted + " NhanKhau records for household id: " + id);
                    }
                }

                // 3. Delete ChiTietThu for PhieuThu in this household
                if (phieuThuCount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "DELETE FROM ChiTietThu WHERE maPhieu IN (SELECT id FROM PhieuThu WHERE maHo = ?)")) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        logger.log(Level.INFO, "Deleted " + deleted + " ChiTietThu records for household id: " + id);
                    }
                }

                // 4. Delete LichSuNopTien for PhieuThu in this household
                if (phieuThuCount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "DELETE FROM LichSuNopTien WHERE maPhieu IN (SELECT id FROM PhieuThu WHERE maHo = ?)")) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        logger.log(Level.INFO, "Deleted " + deleted + " LichSuNopTien records for household id: " + id);
                    }
                }

                // 5. Delete PhieuThu records
                if (phieuThuCount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(DELETE_PHIEU_THU_BY_MAHO)) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        logger.log(Level.INFO, "Deleted " + deleted + " PhieuThu records for household id: " + id);
                    }
                }

                // 6. Delete PhuongTien records
                if (phuongTienCount > 0) {
                    try (PreparedStatement pstmt = conn.prepareStatement(DELETE_PHUONG_TIEN_BY_MAHO)) {
                        pstmt.setInt(1, id);
                        int deleted = pstmt.executeUpdate();
                        logger.log(Level.INFO, "Deleted " + deleted + " PhuongTien records for household id: " + id);
                    }
                }

                // 7. Finally, delete the household
                try (PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
                    pstmt.setInt(1, id);
                    int rowsAffected = pstmt.executeUpdate();
                    boolean success = rowsAffected > 0;

                    if (success) {
                        conn.commit();
                        logger.log(Level.INFO, "Successfully deleted household with id: " + id + 
                                " (and " + nhanKhauCount + " residents, " + phieuThuCount + " receipts, " + phuongTienCount + " vehicles)");
                    } else {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to delete household with id: " + id + " - household may not exist");
                    }

                    return success;
                }

            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Error deleting household with id: " + id + " - transaction rolled back", e);
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting household with id: " + id, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<HoGiaDinh> searchHoGiaDinh(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllHoGiaDinh();
        }

        List<HoGiaDinh> result = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH)) {

            String searchPattern = "%" + keyword.trim() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(buildHoGiaDinhFromResultSet(rs));
                }
            }

            logger.log(Level.INFO, "Found " + result.size() + " households matching: " + keyword);
            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching households with keyword: " + keyword, e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean checkMaHoExists(String maHo) {
        if (maHo == null || maHo.trim().isEmpty()) {
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_MAHO_EXISTS)) {

            pstmt.setString(1, maHo.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking maHo existence: " + maHo, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xây dựng đối tượng HoGiaDinh từ ResultSet.
     *
     * @param rs ResultSet đã được positioned tại row cần đọc
     * @return đối tượng HoGiaDinh
     * @throws SQLException nếu có lỗi khi đọc dữ liệu
     */
    private HoGiaDinh buildHoGiaDinhFromResultSet(ResultSet rs) throws SQLException {
        HoGiaDinh hoGiaDinh = new HoGiaDinh();
        hoGiaDinh.setId(rs.getInt("id"));
        hoGiaDinh.setMaHo(rs.getString("maHo"));
        hoGiaDinh.setSoPhong(rs.getInt("soPhong"));
        hoGiaDinh.setDienTich(rs.getBigDecimal("dienTich"));
        hoGiaDinh.setMaChuHo(rs.getInt("maChuHo"));
        hoGiaDinh.setGhiChu(rs.getString("ghiChu"));

        // Convert java.sql.Date to LocalDate
        Date ngayTao = rs.getDate("ngayTao");
        if (ngayTao != null) {
            hoGiaDinh.setNgayTao(ngayTao.toLocalDate());
        }

        return hoGiaDinh;
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
}
