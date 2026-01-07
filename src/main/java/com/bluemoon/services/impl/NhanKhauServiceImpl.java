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
    // Database mới có thêm: ngayBatDau, ngayKetThuc, hieuLuc, nguoiGhi, ghiChu
    private static final String SELECT_ALL = 
            "SELECT * FROM v_DanhSachCuDanHienTai ORDER BY soPhong, vaiTroTrongHo";

    private static final String SELECT_ALL_HISTORY = 
        "SELECT * FROM v_LichSuNhanKhauTongHop ORDER BY ngayBatDau DESC, id DESC";

    private static final String SELECT_BY_ID = 
            "SELECT id, maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang, " +
            "ngayBatDau, ngayKetThuc, hieuLuc, nguoiGhi, ghiChu FROM NhanKhau WHERE id = ? ORDER BY hieuLuc DESC, ngayBatDau DESC LIMIT 1";

    private static final String SELECT_BY_MAHO = 
            "SELECT id, maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang, " +
            "ngayBatDau, ngayKetThuc, hieuLuc, nguoiGhi, ghiChu FROM NhanKhau WHERE maHo = ? AND hieuLuc = TRUE ORDER BY id";

    private static final String INSERT = 
            "INSERT INTO NhanKhau (hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, " +
            "quanHeVoiChuHo, tinhTrang, maHo, ngayBatDau, ngayKetThuc, hieuLuc, ghiChu, nguoiGhi) " + 
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CLOSE_RECORD = 
            "UPDATE NhanKhau SET hieuLuc = FALSE, ngayKetThuc = ? WHERE id = ?";
    
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
    public boolean addNhanKhau(NhanKhau nk) {
        if (nk == null) return false;
        
        if (nk.getSoCCCD() == null || nk.getSoCCCD().trim().isEmpty()) {
            logger.log(Level.WARNING, "Thất bại: Số CCCD không được để trống");
            return false; 
        }

        // 1. Chuẩn bị ngày bắt đầu (SCD Type 2)
        LocalDate ngayBatDau = nk.getNgayBatDau() != null ? nk.getNgayBatDau() : LocalDate.now();
        Date sqlNgayBatDau = Date.valueOf(ngayBatDau);

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newResidentId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    // Map chính xác 12 tham số theo hằng số INSERT của bạn
                    pstmt.setString(1, nk.getHoTen().trim());
                    pstmt.setDate(2, convertToSqlDate(nk.getNgaySinh()));
                    pstmt.setString(3, nk.getGioiTinh());
                    pstmt.setString(4, nk.getSoCCCD());
                    pstmt.setString(5, nk.getNgheNghiep());
                    pstmt.setString(6, nk.getQuanHeVoiChuHo());
                    pstmt.setString(7, nk.getTinhTrang() != null ? nk.getTinhTrang() : "CuTru");
                    pstmt.setInt(8, nk.getMaHo());
                    pstmt.setDate(9, sqlNgayBatDau);      // ngayBatDau
                    pstmt.setNull(10, java.sql.Types.DATE); // ngayKetThuc (luôn là NULL cho người mới)
                    pstmt.setBoolean(11, true);             // hieuLuc
                    pstmt.setString(12, nk.getGhiChu());    // ghiChu
                    if (nk.getNguoiGhi() != null) {
                        pstmt.setInt(13, nk.getNguoiGhi());
                    } else {
                        pstmt.setNull(13, java.sql.Types.INTEGER);
                    }

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        try (ResultSet rs = pstmt.getGeneratedKeys()) {
                            if (rs.next()) newResidentId = rs.getInt(1);
                        }
                    }
                }

                // Logic cập nhật Chủ hộ nếu cần
                if (newResidentId > 0 && "Chủ hộ".equalsIgnoreCase(nk.getQuanHeVoiChuHo())) {
                    try (PreparedStatement updateHo = conn.prepareStatement("UPDATE HoGiaDinh SET maChuHo = ? WHERE id = ?")) {
                        updateHo.setString(1, nk.getSoCCCD());
                        updateHo.setInt(2, nk.getMaHo());
                        updateHo.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi thêm cư dân", e);
            return false;
        }
    }

    @Override
    public boolean updateNhanKhau(NhanKhau nk) {
        if (nk == null || nk.getId() == 0) return false;

        if (nk.getSoCCCD() == null || nk.getSoCCCD().trim().isEmpty()) {
            logger.log(Level.WARNING, "Thất bại: Số CCCD không được để trống");
            return false; 
        }

        // 1. Chuẩn bị ngày mốc (SCD Type 2)
        // Đã import java.sql.Date ở trên nên chỉ cần viết Date
        Date sqlMocThoiGian = (nk.getNgayBatDau() != null) 
            ? Date.valueOf(nk.getNgayBatDau()) 
            : new Date(System.currentTimeMillis());

        // Sử dụng try-with-resources để tự động đóng Connection
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false); 

            try {
                // BƯỚC 1: Đóng bản ghi cũ
                try (PreparedStatement closeStmt = conn.prepareStatement(CLOSE_RECORD)) {
                    closeStmt.setDate(1, sqlMocThoiGian);
                    closeStmt.setInt(2, nk.getId());
                    closeStmt.executeUpdate();
                }

                // BƯỚC 2: Tạo bản ghi mới
                try (PreparedStatement insertStmt = conn.prepareStatement(INSERT)) {
                    insertStmt.setString(1, nk.getHoTen());
                    insertStmt.setDate(2, convertToSqlDate(nk.getNgaySinh())); // Viết tắt Date
                    insertStmt.setString(3, nk.getGioiTinh());
                    insertStmt.setString(4, nk.getSoCCCD());
                    insertStmt.setString(5, nk.getNgheNghiep());
                    insertStmt.setString(6, nk.getQuanHeVoiChuHo());
                    insertStmt.setString(7, nk.getTinhTrang());
                    insertStmt.setInt(8, nk.getMaHo());
                    insertStmt.setDate(9, sqlMocThoiGian);
                    insertStmt.setNull(10, java.sql.Types.DATE); 
                    insertStmt.setBoolean(11, true);             
                    insertStmt.setString(12, nk.getGhiChu());    
                    if (nk.getNguoiGhi() != null) {
                        insertStmt.setInt(13, nk.getNguoiGhi());
                    } else {
                        insertStmt.setNull(13, java.sql.Types.INTEGER);
                    }
                    insertStmt.executeUpdate();
                }

                conn.commit(); 
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Nếu rollback lỗi, nó sẽ ném ngoại lệ ra ngoài để log
                throw e;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi cập nhật cư dân: " + nk.getId(), e);
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
        try (Connection conn = DatabaseConnector.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
        
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Lỗi khi xóa nhân khẩu id: " + id, e);
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
     * Xây dựng đối tượng NhanKhau từ ResultSet (SCD Type 2).
     */
    private NhanKhau buildNhanKhauFromResultSet(ResultSet rs) throws SQLException {
        NhanKhau nk = new NhanKhau();
        // Tên cột bây giờ khớp 100% với bảng NhanKhau trong DB
        nk.setId(rs.getInt("id"));
        nk.setMaHo(rs.getInt("maHo")); 
        nk.setHoTen(rs.getString("hoTen"));
        nk.setGioiTinh(rs.getString("gioiTinh"));
        nk.setSoCCCD(rs.getString("soCCCD"));
        nk.setNgheNghiep(rs.getString("ngheNghiep"));
        nk.setQuanHeVoiChuHo(rs.getString("quanHeVoiChuHo"));
        nk.setTinhTrang(rs.getString("tinhTrang"));
        nk.setGhiChu(rs.getString("ghiChu"));

        // Xử lý ngày tháng an toàn
        Date dbNgaySinh = rs.getDate("ngaySinh");
        if (dbNgaySinh != null) nk.setNgaySinh(dbNgaySinh.toLocalDate());

        Date dbNgayBatDau = rs.getDate("ngayBatDau");
        if (dbNgayBatDau != null) nk.setNgayBatDau(dbNgayBatDau.toLocalDate());

        Date dbNgayKetThuc = rs.getDate("ngayKetThuc");
        if (dbNgayKetThuc != null) nk.setNgayKetThuc(dbNgayKetThuc.toLocalDate());

        nk.setHieuLuc(rs.getBoolean("hieuLuc"));

        // Các trường bổ trợ từ VIEW
        try {
            nk.setSoPhong(rs.getString("soPhong"));
            nk.setVaiTroTrongHo(rs.getString("vaiTroTrongHo"));
            nk.setLoaiCuTru(rs.getString("loaiCuTru"));
        } catch (SQLException e) {
            // Bỏ qua nếu query không từ View (ví dụ SELECT_BY_ID)
        }
        return nk;
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

    @Override
    public List<NhanKhau> getAllHistory() {
        List<NhanKhau> list = new ArrayList<>();
        // Sử dụng try-with-resources để tự động đóng connection
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_HISTORY);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(buildNhanKhauFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Lỗi khi lấy lịch sử cư trú tổng hợp", e);
        }
        return list;
    }
}
