package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
    // Lưu ý: Database mới không có maHo, dienTich, ngayTao
    // maChuHo là VARCHAR(20) tham chiếu đến NhanKhau.soCCCD
    // JOIN với Phong để lấy diện tích
    private static final String SELECT_ALL = 
            "SELECT h.id, h.soPhong, h.maChuHo, h.trangThai, h.ghiChu, h.thoiGianBatDauO, h.thoiGianKetThucO, p.dienTich " +
            "FROM HoGiaDinh h LEFT JOIN Phong p ON h.soPhong = p.soPhong ORDER BY h.id";

    private static final String SELECT_BY_ID = 
            "SELECT h.id, h.soPhong, h.maChuHo, h.trangThai, h.ghiChu, h.thoiGianBatDauO, h.thoiGianKetThucO, p.dienTich " +
            "FROM HoGiaDinh h LEFT JOIN Phong p ON h.soPhong = p.soPhong WHERE h.id = ?";

    private static final String INSERT = 
            "INSERT INTO HoGiaDinh (soPhong, maChuHo, trangThai, ghiChu, thoiGianBatDauO, thoiGianKetThucO) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = 
            "UPDATE HoGiaDinh SET soPhong = ?, maChuHo = ?, trangThai = ?, ghiChu = ?, thoiGianBatDauO = ?, thoiGianKetThucO = ? WHERE id = ?";

    private static final String DELETE = 
            "DELETE FROM HoGiaDinh WHERE id = ?";

    private static final String CHECK_SOPHONG_EXISTS = 
        "SELECT COUNT(*) FROM HoGiaDinh WHERE soPhong = ? AND thoiGianKetThucO IS NULL";

    private static final String CHECK_SOPHONG_EXISTS_EXCLUDE_ID = 
        "SELECT COUNT(*) FROM HoGiaDinh WHERE soPhong = ? AND id != ? AND thoiGianKetThucO IS NULL";

    private static final String CHECK_NHAN_KHAU_DEPENDENCIES = 
            "SELECT COUNT(*) FROM NhanKhau WHERE maHo = ?";

    private static final String CHECK_PHIEU_THU_DEPENDENCIES = 
            "SELECT COUNT(*) FROM PhieuThu WHERE maHo = ?";

    private static final String SEARCH = 
            "SELECT h.id, h.soPhong, h.maChuHo, h.trangThai, h.ghiChu, h.thoiGianBatDauO, h.thoiGianKetThucO, p.dienTich " +
            "FROM HoGiaDinh h LEFT JOIN Phong p ON h.soPhong = p.soPhong " +
            "WHERE h.ghiChu LIKE ? ORDER BY h.id";

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

        // Check if soPhong already exists (must be unique)
        if (hoGiaDinh.getSoPhong() > 0 && checkSoPhongExists(hoGiaDinh.getSoPhong(), 0)) {
            String errorMsg = "Số phòng " + hoGiaDinh.getSoPhong() + " đã tồn tại. Mỗi số phòng chỉ có thể được sử dụng bởi một hộ gia đình.";
            logger.log(Level.WARNING, "Cannot add household: soPhong already exists: " + hoGiaDinh.getSoPhong());
            throw new IllegalArgumentException(errorMsg);
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {

            pstmt.setInt(1, hoGiaDinh.getSoPhong());
            // maChuHo là VARCHAR(20) - soCCCD của NhanKhau, có thể NULL
            if (hoGiaDinh.getMaChuHo() != null && !hoGiaDinh.getMaChuHo().trim().isEmpty()) {
                pstmt.setString(2, hoGiaDinh.getMaChuHo().trim());
            } else {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            }
            // trangThai có DEFAULT 'DangO' trong database
            // Logic tự động: Nếu có ngày kết thúc -> Trạng thái là "LichSu"
            if (hoGiaDinh.getThoiGianKetThucO() != null) {
                pstmt.setString(3, "LichSu"); // Hoặc "DaChuyenDi" tùy bạn thích
            } else {
            // Nếu không có ngày kết thúc -> Giữ nguyên trạng thái gửi lên (hoặc mặc định DangO)
                pstmt.setString(3, hoGiaDinh.getTrangThai() != null ? hoGiaDinh.getTrangThai() : "DangO");
            }
            pstmt.setString(4, hoGiaDinh.getGhiChu());
            // thoiGianBatDauO và thoiGianKetThucO là TIMESTAMP
            if (hoGiaDinh.getThoiGianBatDauO() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(hoGiaDinh.getThoiGianBatDauO()));
            } else {
                pstmt.setNull(5, java.sql.Types.TIMESTAMP);
            }
            if (hoGiaDinh.getThoiGianKetThucO() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(hoGiaDinh.getThoiGianKetThucO()));
            } else {
                pstmt.setNull(6, java.sql.Types.TIMESTAMP);
            }

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.log(Level.INFO, "Successfully added household with soPhong: " + hoGiaDinh.getSoPhong());
            } else {
                logger.log(Level.WARNING, "Failed to add household with soPhong: " + hoGiaDinh.getSoPhong());
            }

            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding household with soPhong: " + hoGiaDinh.getSoPhong(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateHoGiaDinh(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh == null || hoGiaDinh.getId() == 0) {
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Check soPhong uniqueness (exclude current record)
            if (hoGiaDinh.getSoPhong() > 0 && checkSoPhongExists(hoGiaDinh.getSoPhong(), hoGiaDinh.getId())) {
                String errorMsg = "Số phòng " + hoGiaDinh.getSoPhong() + " đã tồn tại. Mỗi số phòng chỉ có thể được sử dụng bởi một hộ gia đình.";
                logger.log(Level.WARNING, 
                        "Cannot update household: soPhong already exists: " + hoGiaDinh.getSoPhong());
                throw new IllegalArgumentException(errorMsg);
            }

            // Proceed with update
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
                pstmt.setInt(1, hoGiaDinh.getSoPhong());
                // maChuHo là VARCHAR(20) - soCCCD của NhanKhau, có thể NULL
                if (hoGiaDinh.getMaChuHo() != null && !hoGiaDinh.getMaChuHo().trim().isEmpty()) {
                    pstmt.setString(2, hoGiaDinh.getMaChuHo().trim());
                } else {
                    pstmt.setNull(2, java.sql.Types.VARCHAR);
                }
                // trangThai có DEFAULT 'DangO' trong database
                pstmt.setString(3, hoGiaDinh.getTrangThai() != null ? hoGiaDinh.getTrangThai() : "DangO");
                pstmt.setString(4, hoGiaDinh.getGhiChu());
                // thoiGianBatDauO và thoiGianKetThucO là TIMESTAMP
                if (hoGiaDinh.getThoiGianBatDauO() != null) {
                    pstmt.setTimestamp(5, Timestamp.valueOf(hoGiaDinh.getThoiGianBatDauO()));
                } else {
                    pstmt.setNull(5, java.sql.Types.TIMESTAMP);
                }
                if (hoGiaDinh.getThoiGianKetThucO() != null) {
                    pstmt.setTimestamp(6, Timestamp.valueOf(hoGiaDinh.getThoiGianKetThucO()));
                } else {
                    pstmt.setNull(6, java.sql.Types.TIMESTAMP);
                }
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

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw business logic errors
        } catch (SQLException e) {
            // Check for unique constraint violation (if database has unique constraint on soPhong)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // PostgreSQL unique violation
                logger.log(Level.WARNING, "Duplicate soPhong detected by database constraint", e);
                throw new IllegalArgumentException(
                    "Số phòng đã tồn tại. Mỗi số phòng chỉ có thể được sử dụng bởi một hộ gia đình."
                );
            }
            logger.log(Level.SEVERE, "Error updating household with id: " + hoGiaDinh.getId(), e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteHoGiaDinh(int id) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Constraint Check: Check for dependencies in NhanKhau table
            int nhanKhauCount = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(CHECK_NHAN_KHAU_DEPENDENCIES)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        nhanKhauCount = rs.getInt(1);
                    }
                }
            }

            // Constraint Check: Check for dependencies in PhieuThu table
            int phieuThuCount = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(CHECK_PHIEU_THU_DEPENDENCIES)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        phieuThuCount = rs.getInt(1);
                    }
                }
            }

            // If either has dependencies, block deletion
            if (nhanKhauCount > 0 || phieuThuCount > 0) {
                logger.log(Level.WARNING, 
                        "Cannot delete household with id: " + id + 
                        ". Related records: NhanKhau=" + nhanKhauCount + 
                        ", PhieuThu=" + phieuThuCount);
                return false;
            }

            // Proceed with deletion
            try (PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
                pstmt.setInt(1, id);

                int rowsAffected = pstmt.executeUpdate();
                boolean success = rowsAffected > 0;

                if (success) {
                    logger.log(Level.INFO, "Successfully deleted household with id: " + id);
                } else {
                    logger.log(Level.WARNING, "Failed to delete household with id: " + id);
                }

                return success;
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
        // Database mới không có cột maHo, method này không còn cần thiết
        // Giữ lại để tương thích với interface, nhưng luôn trả về false
        return false;
    }

    /**
     * Kiểm tra xem số phòng đã tồn tại chưa.
     * @param soPhong Số phòng cần kiểm tra
     * @param excludeId ID hộ gia đình cần loại trừ (cho trường hợp update), 0 nếu không loại trừ
     * @return true nếu số phòng đã tồn tại, false nếu chưa
     */
    private boolean checkSoPhongExists(int soPhong, int excludeId) {
        if (soPhong <= 0) {
            return false; // Số phòng <= 0 không cần kiểm tra
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            String query = excludeId > 0 ? CHECK_SOPHONG_EXISTS_EXCLUDE_ID : CHECK_SOPHONG_EXISTS;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, soPhong);
                if (excludeId > 0) {
                    pstmt.setInt(2, excludeId);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        return count > 0;
                    }
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking soPhong existence: " + soPhong, e);
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
        hoGiaDinh.setSoPhong(rs.getInt("soPhong"));
        hoGiaDinh.setMaChuHo(rs.getString("maChuHo")); // VARCHAR(20) - soCCCD
        hoGiaDinh.setTrangThai(rs.getString("trangThai")); // trangThai có DEFAULT 'DangO'
        hoGiaDinh.setGhiChu(rs.getString("ghiChu"));
        
        // Lấy diện tích từ bảng Phong (JOIN)
        java.math.BigDecimal dienTich = rs.getBigDecimal("dienTich");
        hoGiaDinh.setDienTich(dienTich);

        // Convert java.sql.Timestamp to LocalDateTime
        Timestamp thoiGianBatDauO = rs.getTimestamp("thoiGianBatDauO");
        if (thoiGianBatDauO != null) {
            hoGiaDinh.setThoiGianBatDauO(thoiGianBatDauO.toLocalDateTime());
        }

        Timestamp thoiGianKetThucO = rs.getTimestamp("thoiGianKetThucO");
        if (thoiGianKetThucO != null) {
            hoGiaDinh.setThoiGianKetThucO(thoiGianKetThucO.toLocalDateTime());
        }

        return hoGiaDinh;
    }
}
