package com.bluemoon.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.math.BigDecimal;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.models.DotThu;
import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.KhoanThu;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.models.PhieuThu;
import com.bluemoon.models.PhuongTien;
import com.bluemoon.services.ChiTietThuService;
import com.bluemoon.services.DotThuService;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.KhoanThuService;
import com.bluemoon.services.NhanKhauService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.PhuongTienService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Triển khai {@link PhieuThuService} với database integration.
 * Sử dụng PostgreSQL database để lưu trữ dữ liệu.
 */
public class PhieuThuServiceImpl implements PhieuThuService {

    private static final Logger logger = Logger.getLogger(PhieuThuServiceImpl.class.getName());
    
    // Services để kiểm tra trạng thái đợt thu và tính phí
    private final DotThuService dotThuService;
    private final KhoanThuService khoanThuService;
    private final HoGiaDinhService hoGiaDinhService;
    private final NhanKhauService nhanKhauService;
    private final PhuongTienService phuongTienService;
    private final ChiTietThuService chiTietThuService;
    
    public PhieuThuServiceImpl() {
        this.dotThuService = new com.bluemoon.services.impl.DotThuServiceImpl();
        this.khoanThuService = new com.bluemoon.services.impl.KhoanThuServiceImpl();
        this.hoGiaDinhService = new com.bluemoon.services.impl.HoGiaDinhServiceImpl();
        this.nhanKhauService = new com.bluemoon.services.impl.NhanKhauServiceImpl();
        this.phuongTienService = new com.bluemoon.services.impl.PhuongTienServiceImpl();
        this.chiTietThuService = new com.bluemoon.services.impl.ChiTietThuServiceImpl();
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

    // SQL Queries for ChiTietThu - DEPRECATED: Use ChiTietThuService instead
    // Keeping for backward compatibility in isFeeUsed method

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
        // Delegate to ChiTietThuService
        return chiTietThuService.save(chiTiet);
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

                // Step 2: Add all ChiTietThu using ChiTietThuService (with transaction support)
                BigDecimal calculatedTotal = BigDecimal.ZERO;
                if (chiTietList != null && !chiTietList.isEmpty()) {
                    // Set maPhieu for all chiTiet
                    for (ChiTietThu chiTiet : chiTietList) {
                        chiTiet.setMaPhieu(maPhieu);
                        
                        // Calculate thanhTien if not set (donGia * soLuong)
                        if (chiTiet.getThanhTien() == null || chiTiet.getThanhTien().compareTo(BigDecimal.ZERO) == 0) {
                            if (chiTiet.getDonGia() != null && chiTiet.getSoLuong() != null) {
                                BigDecimal thanhTien = chiTiet.getDonGia().multiply(chiTiet.getSoLuong());
                                chiTiet.setThanhTien(thanhTien);
                            }
                        }
                        
                        // Sum up total
                        if (chiTiet.getThanhTien() != null) {
                            calculatedTotal = calculatedTotal.add(chiTiet.getThanhTien());
                        }
                    }
                    
                    // Use ChiTietThuService with transaction support
                    ChiTietThuServiceImpl chiTietServiceImpl = (ChiTietThuServiceImpl) chiTietThuService;
                    if (!chiTietServiceImpl.saveAll(chiTietList, conn)) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to save ChiTietThu list, transaction rolled back");
                        return -1;
                    }
                }

                // Step 3: Update PhieuThu.tongTien with calculated total from ChiTietThu
                if (calculatedTotal.compareTo(BigDecimal.ZERO) > 0) {
                    try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE PhieuThu SET tongTien = ? WHERE id = ?")) {
                        updateStmt.setBigDecimal(1, calculatedTotal);
                        updateStmt.setInt(2, maPhieu);
                        int updateRows = updateStmt.executeUpdate();
                        if (updateRows > 0) {
                            logger.log(Level.INFO, "Updated PhieuThu.tongTien to " + calculatedTotal + " for id: " + maPhieu);
                        } else {
                            logger.log(Level.WARNING, "Failed to update PhieuThu.tongTien for id: " + maPhieu);
                        }
                    }
                } else {
                    logger.log(Level.WARNING, "Calculated total is 0 or negative for PhieuThu id: " + maPhieu);
                }

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully created PhieuThu with details, id: " + maPhieu + ", total: " + calculatedTotal);
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
        // Delegate to ChiTietThuService
        return chiTietThuService.getChiTietByMaPhieu(maPhieu);
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
        // Validate: Kiểm tra đợt thu có đang mở không
        if (!isDotThuOpen(maDot)) {
            logger.log(Level.WARNING, "Cannot generate receipts: DotThu with id " + maDot + " is closed");
            return 0;
        }

        // Get current user ID for maTaiKhoan (default to 1 if not available)
        int maTaiKhoan = 1;
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            int successCount = 0;

            try {
                // Step 1: Fetch all required data efficiently (batch queries to avoid N+1)
                List<HoGiaDinh> allHouseholds = hoGiaDinhService.getAllHoGiaDinh();
                List<KhoanThu> allFees = khoanThuService.getAllKhoanThu();
                
                // Filter only mandatory fees (batBuoc = true)
                List<KhoanThu> mandatoryFees = allFees.stream()
                    .filter(KhoanThu::isBatBuoc)
                    .collect(java.util.stream.Collectors.toList());

                if (mandatoryFees.isEmpty()) {
                    logger.log(Level.WARNING, "No mandatory fees found for drive generation");
                    conn.rollback();
                    return 0;
                }

                logger.log(Level.INFO, "Generating receipts for " + allHouseholds.size() + 
                    " households with " + mandatoryFees.size() + " mandatory fees");

                // Step 2: Pre-fetch all household data to avoid N+1 queries
                // Map: householdId -> household data
                java.util.Map<Integer, HoGiaDinh> householdMap = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> memberCountMap = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> motorbikeCountMap = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> carCountMap = new java.util.HashMap<>();

                for (HoGiaDinh household : allHouseholds) {
                    int householdId = household.getId();
                    householdMap.put(householdId, household);
                    
                    // Count members
                    List<NhanKhau> members = nhanKhauService.getNhanKhauByHoGiaDinh(householdId);
                    memberCountMap.put(householdId, members.size());
                    
                    // Count vehicles
                    List<PhuongTien> vehicles = phuongTienService.getPhuongTienByHoGiaDinh(householdId);
                    int motorbikeCount = 0;
                    int carCount = 0;
                    for (PhuongTien vehicle : vehicles) {
                        String loaiXe = vehicle.getLoaiXe();
                        if (loaiXe != null) {
                            String loaiXeLower = loaiXe.toLowerCase().trim();
                            if (loaiXeLower.contains("xe máy") || loaiXeLower.contains("xemay") || 
                                loaiXeLower.contains("moto") || loaiXeLower.contains("xe may")) {
                                motorbikeCount++;
                            } else if (loaiXeLower.contains("ô tô") || loaiXeLower.contains("oto") || 
                                      loaiXeLower.contains("car") || loaiXeLower.contains("o to")) {
                                carCount++;
                            }
                        }
                    }
                    motorbikeCountMap.put(householdId, motorbikeCount);
                    carCountMap.put(householdId, carCount);
                }

                // Step 3: Generate receipts for each household
                LocalDateTime now = LocalDateTime.now();
                
                for (HoGiaDinh household : allHouseholds) {
                    int householdId = household.getId();
                    
                    // Calculate total amount for this household
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    List<ChiTietThu> chiTietList = new ArrayList<>();

                    // Calculate each mandatory fee based on tinhTheo
                    for (KhoanThu fee : mandatoryFees) {
                        BigDecimal feeAmount = calculateFeeAmount(
                            fee, 
                            household, 
                            memberCountMap.get(householdId),
                            motorbikeCountMap.get(householdId),
                            carCountMap.get(householdId)
                        );

                        if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                            totalAmount = totalAmount.add(feeAmount);
                            
                            // Create ChiTietThu
                            ChiTietThu chiTiet = new ChiTietThu();
                            chiTiet.setMaKhoan(fee.getId());
                            chiTiet.setSoLuong(BigDecimal.ONE); // Default quantity
                            chiTiet.setDonGia(fee.getDonGia());
                            chiTiet.setThanhTien(feeAmount);
                            chiTietList.add(chiTiet);
                        }
                    }

                    // Only create receipt if total amount > 0
                    if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        // Create PhieuThu
                        PhieuThu phieuThu = new PhieuThu();
                        phieuThu.setMaHo(householdId);
                        phieuThu.setMaDot(maDot);
                        phieuThu.setMaTaiKhoan(maTaiKhoan);
                        phieuThu.setNgayLap(now);
                        phieuThu.setTongTien(totalAmount);
                        phieuThu.setTrangThai("ChuaThu");
                        phieuThu.setHinhThucThu(null);

                        // Create receipt with details using transaction
                        int maPhieu = createPhieuThuWithDetailsInTransaction(phieuThu, chiTietList, conn);
                        if (maPhieu > 0) {
                            successCount++;
                        } else {
                            logger.log(Level.WARNING, "Failed to create receipt for household: " + householdId);
                        }
                    }
                }

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully generated " + successCount + " receipts for drive: " + maDot);
                return successCount;

            } catch (Exception e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Error generating receipts for drive: " + maDot, e);
                e.printStackTrace();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in generateReceiptsForDrive for maDot: " + maDot, e);
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Calculate fee amount based on tinhTheo (calculation method).
     * 
     * @param fee The fee definition (KhoanThu)
     * @param household The household
     * @param memberCount Number of members in the household
     * @param motorbikeCount Number of motorbikes
     * @param carCount Number of cars
     * @return Calculated fee amount, or null if calculation method is not supported
     */
    private BigDecimal calculateFeeAmount(KhoanThu fee, HoGiaDinh household, 
                                         Integer memberCount, Integer motorbikeCount, Integer carCount) {
        if (fee == null || fee.getDonGia() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal basePrice = fee.getDonGia();
        String tinhTheo = fee.getTinhTheo();
        
        if (tinhTheo == null || tinhTheo.trim().isEmpty()) {
            // Default: Fixed fee per household
            return basePrice;
        }

        String tinhTheoLower = tinhTheo.toLowerCase().trim();
        BigDecimal multiplier = BigDecimal.ONE;

        // Switch-case logic for different calculation methods
        switch (tinhTheoLower) {
            case "hokhau":
            case "hộ khẩu":
            case "codinh":
            case "cố định":
            case "fixed":
                // Fixed Fee per Household: Amount = Fee_Price * 1
                multiplier = BigDecimal.ONE;
                break;

            case "nhankhau":
            case "nhân khẩu":
            case "person":
            case "perperson":
                // Fee per Person: Amount = Fee_Price * Count(NhanKhau)
                if (memberCount == null || memberCount <= 0) {
                    logger.log(Level.WARNING, "Cannot calculate fee per person: memberCount is 0 or null for household: " + household.getId());
                    return BigDecimal.ZERO;
                }
                multiplier = BigDecimal.valueOf(memberCount);
                break;

            case "dientich":
            case "diện tích":
            case "area":
            case "perarea":
                // Fee per Area: Amount = Fee_Price * HoGiaDinh.dienTich
                if (household.getDienTich() == null || household.getDienTich().compareTo(BigDecimal.ZERO) <= 0) {
                    logger.log(Level.WARNING, "Cannot calculate fee per area: dienTich is 0 or null for household: " + household.getId());
                    return BigDecimal.ZERO;
                }
                multiplier = household.getDienTich();
                break;

            case "xemay":
            case "xe máy":
            case "xe may":
            case "motorbike":
            case "moto":
                // Fee per Motorbike: Amount = Fee_Price * Count(Motorbikes)
                if (motorbikeCount == null || motorbikeCount <= 0) {
                    // No motorbikes, return 0 (or could return base price if business rule requires)
                    return BigDecimal.ZERO;
                }
                multiplier = BigDecimal.valueOf(motorbikeCount);
                break;

            case "oto":
            case "ô tô":
            case "o to":
            case "car":
            case "automobile":
                // Fee per Car: Amount = Fee_Price * Count(Cars)
                if (carCount == null || carCount <= 0) {
                    // No cars, return 0 (or could return base price if business rule requires)
                    return BigDecimal.ZERO;
                }
                multiplier = BigDecimal.valueOf(carCount);
                break;

            default:
                // Unknown calculation method, default to fixed fee
                logger.log(Level.WARNING, "Unknown tinhTheo value: '" + tinhTheo + "' for fee: " + fee.getTenKhoan() + 
                    ", defaulting to fixed fee per household");
                multiplier = BigDecimal.ONE;
                break;
        }

        // Calculate: Amount = basePrice * multiplier
        BigDecimal amount = basePrice.multiply(multiplier);
        logger.log(Level.FINE, "Calculated fee for " + fee.getTenKhoan() + " (tinhTheo: " + tinhTheo + 
            "): " + basePrice + " * " + multiplier + " = " + amount);
        
        return amount;
    }

    @Override
    public BigDecimal calculateTotalAmountForHousehold(int maHo, int maDot) {
        try {
            // Validate: Kiểm tra đợt thu có đang mở không
            if (!isDotThuOpen(maDot)) {
                logger.log(Level.WARNING, "Cannot calculate total: DotThu with id " + maDot + " is closed");
                return null;
            }

            // Get household
            HoGiaDinh household = hoGiaDinhService.findById(maHo);
            if (household == null) {
                logger.log(Level.WARNING, "Household not found: " + maHo);
                return null;
            }

            // Get all mandatory fees
            List<KhoanThu> allFees = khoanThuService.getAllKhoanThu();
            List<KhoanThu> mandatoryFees = allFees.stream()
                .filter(KhoanThu::isBatBuoc)
                .collect(java.util.stream.Collectors.toList());

            if (mandatoryFees.isEmpty()) {
                logger.log(Level.INFO, "No mandatory fees found for calculation");
                return BigDecimal.ZERO;
            }

            // Get household data
            List<NhanKhau> members = nhanKhauService.getNhanKhauByHoGiaDinh(maHo);
            int memberCount = members.size();

            // Count vehicles
            List<PhuongTien> vehicles = phuongTienService.getPhuongTienByHoGiaDinh(maHo);
            int motorbikeCount = 0;
            int carCount = 0;
            for (PhuongTien vehicle : vehicles) {
                String loaiXe = vehicle.getLoaiXe();
                if (loaiXe != null) {
                    String loaiXeLower = loaiXe.toLowerCase().trim();
                    if (loaiXeLower.contains("xe máy") || loaiXeLower.contains("xemay") || 
                        loaiXeLower.contains("moto") || loaiXeLower.contains("xe may")) {
                        motorbikeCount++;
                    } else if (loaiXeLower.contains("ô tô") || loaiXeLower.contains("oto") || 
                              loaiXeLower.contains("car") || loaiXeLower.contains("o to")) {
                        carCount++;
                    }
                }
            }

            // Calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (KhoanThu fee : mandatoryFees) {
                BigDecimal feeAmount = calculateFeeAmount(
                    fee, 
                    household, 
                    memberCount,
                    motorbikeCount,
                    carCount
                );

                if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    totalAmount = totalAmount.add(feeAmount);
                }
            }

            logger.log(Level.INFO, "Calculated total amount for household " + maHo + 
                " in drive " + maDot + ": " + totalAmount);
            return totalAmount;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating total amount for household: " + maHo + 
                " in drive: " + maDot, e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create PhieuThu with details using an existing connection (for transaction support).
     * 
     * @param phieuThu The receipt to create
     * @param chiTietList List of receipt details
     * @param conn Existing database connection
     * @return Generated receipt ID, or -1 if failed
     */
    private int createPhieuThuWithDetailsInTransaction(PhieuThu phieuThu, List<ChiTietThu> chiTietList, Connection conn) {
        try {
            // Step 1: Create PhieuThu
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
                    logger.log(Level.WARNING, "Failed to create PhieuThu in transaction");
                    return -1;
                }
            }

            // Step 2: Add all ChiTietThu using ChiTietThuService (with transaction support)
            BigDecimal calculatedTotal = BigDecimal.ZERO;
            if (chiTietList != null && !chiTietList.isEmpty()) {
                // Set maPhieu for all chiTiet
                for (ChiTietThu chiTiet : chiTietList) {
                    chiTiet.setMaPhieu(maPhieu);
                    
                    // Calculate thanhTien if not set (donGia * soLuong)
                    if (chiTiet.getThanhTien() == null || chiTiet.getThanhTien().compareTo(BigDecimal.ZERO) == 0) {
                        if (chiTiet.getDonGia() != null && chiTiet.getSoLuong() != null) {
                            BigDecimal thanhTien = chiTiet.getDonGia().multiply(chiTiet.getSoLuong());
                            chiTiet.setThanhTien(thanhTien);
                        }
                    }
                    
                    // Sum up total
                    if (chiTiet.getThanhTien() != null) {
                        calculatedTotal = calculatedTotal.add(chiTiet.getThanhTien());
                    }
                }
                
                // Use ChiTietThuService with transaction support
                ChiTietThuServiceImpl chiTietServiceImpl = (ChiTietThuServiceImpl) chiTietThuService;
                if (!chiTietServiceImpl.saveAll(chiTietList, conn)) {
                    logger.log(Level.WARNING, "Failed to save ChiTietThu list in transaction");
                    return -1;
                }
            }

            // Step 3: Update PhieuThu.tongTien with calculated total from ChiTietThu
            if (calculatedTotal.compareTo(BigDecimal.ZERO) > 0) {
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE PhieuThu SET tongTien = ? WHERE id = ?")) {
                    updateStmt.setBigDecimal(1, calculatedTotal);
                    updateStmt.setInt(2, maPhieu);
                    int updateRows = updateStmt.executeUpdate();
                    if (updateRows > 0) {
                        logger.log(Level.FINE, "Updated PhieuThu.tongTien to " + calculatedTotal + " for id: " + maPhieu);
                    } else {
                        logger.log(Level.WARNING, "Failed to update PhieuThu.tongTien for id: " + maPhieu);
                    }
                }
            } else {
                logger.log(Level.WARNING, "Calculated total is 0 or negative for PhieuThu id: " + maPhieu);
            }

            logger.log(Level.FINE, "Successfully created PhieuThu with details in transaction, id: " + maPhieu + ", total: " + calculatedTotal);
            return maPhieu;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating PhieuThu with details in transaction", e);
            e.printStackTrace();
            return -1;
        }
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
                    // Delete old details using ChiTietThuService (with transaction support)
                    ChiTietThuServiceImpl chiTietServiceImpl = (ChiTietThuServiceImpl) chiTietThuService;
                    if (!chiTietServiceImpl.deleteByMaPhieu(phieuThu.getId(), conn)) {
                        conn.rollback();
                        logger.log(Level.WARNING, "Failed to delete old ChiTietThu, transaction rolled back");
                        return false;
                    }

                    // Add new details using ChiTietThuService (with transaction support)
                    if (!chiTietList.isEmpty()) {
                        // Set maPhieu for all chiTiet
                        for (ChiTietThu chiTiet : chiTietList) {
                            chiTiet.setMaPhieu(phieuThu.getId());
                        }
                        
                        if (!chiTietServiceImpl.saveAll(chiTietList, conn)) {
                            conn.rollback();
                            logger.log(Level.WARNING, "Failed to save new ChiTietThu, transaction rolled back");
                            return false;
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
                // Step 1: Delete ChiTietThu first using ChiTietThuService (with transaction support)
                ChiTietThuServiceImpl chiTietServiceImpl = (ChiTietThuServiceImpl) chiTietThuService;
                if (!chiTietServiceImpl.deleteByMaPhieu(maPhieu, conn)) {
                    conn.rollback();
                    logger.log(Level.WARNING, "Failed to delete ChiTietThu, transaction rolled back");
                    return false;
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

    // buildChiTietThuFromResultSet method removed - now handled by ChiTietThuService

    /**
     * Chuyển đổi LocalDateTime sang java.sql.Timestamp.
     */
    private Timestamp convertToSqlTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        return Timestamp.valueOf(localDateTime);
    }

    @Override
    public int createBatch(int maDotThu) {
        // Validate: Kiểm tra đợt thu có đang mở không
        if (!isDotThuOpen(maDotThu)) {
            DotThu dotThu = dotThuService.getDotThuById(maDotThu);
            String errorMsg = "Không thể tạo phiếu thu hàng loạt: Đợt thu đã đóng";
            if (dotThu != null) {
                errorMsg = "Không thể tạo phiếu thu hàng loạt: Đợt thu \"" + dotThu.getTenDot() + "\" đã đóng (trạng thái: " + dotThu.getTrangThai() + ")";
            }
            logger.log(Level.WARNING, "Cannot create batch: DotThu with id " + maDotThu + " is closed");
            throw new IllegalArgumentException(errorMsg);
        }

        // Get current user ID for maTaiKhoan (default to 1 if not available)
        int maTaiKhoan = 1;
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            int successCount = 0;

            try {
                // Step 1: Fetch all required data efficiently
                List<HoGiaDinh> allHouseholds = hoGiaDinhService.getAllHoGiaDinh();
                List<KhoanThu> allFees = khoanThuService.getAllKhoanThu();
                
                // Filter only mandatory fees (batBuoc = true)
                List<KhoanThu> mandatoryFees = allFees.stream()
                    .filter(KhoanThu::isBatBuoc)
                    .collect(java.util.stream.Collectors.toList());

                if (mandatoryFees.isEmpty()) {
                    logger.log(Level.WARNING, "No mandatory fees found for batch creation");
                    conn.rollback();
                    return 0;
                }

                logger.log(Level.INFO, "Creating batch receipts for " + allHouseholds.size() + 
                    " households with " + mandatoryFees.size() + " mandatory fees");

                // Step 2: Pre-fetch all household data to avoid N+1 queries
                java.util.Map<Integer, HoGiaDinh> householdMap = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> memberCountMap = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> motorbikeCountMap = new java.util.HashMap<>();
                java.util.Map<Integer, Integer> carCountMap = new java.util.HashMap<>();

                for (HoGiaDinh household : allHouseholds) {
                    int householdId = household.getId();
                    householdMap.put(householdId, household);
                    
                    // Count members
                    List<NhanKhau> members = nhanKhauService.getNhanKhauByHoGiaDinh(householdId);
                    memberCountMap.put(householdId, members.size());
                    
                    // Count vehicles
                    List<PhuongTien> vehicles = phuongTienService.getPhuongTienByHoGiaDinh(householdId);
                    int motorbikeCount = 0;
                    int carCount = 0;
                    for (PhuongTien vehicle : vehicles) {
                        String loaiXe = vehicle.getLoaiXe();
                        if (loaiXe != null) {
                            String loaiXeLower = loaiXe.toLowerCase().trim();
                            if (loaiXeLower.contains("xe máy") || loaiXeLower.contains("xemay") || 
                                loaiXeLower.contains("moto") || loaiXeLower.contains("xe may")) {
                                motorbikeCount++;
                            } else if (loaiXeLower.contains("ô tô") || loaiXeLower.contains("oto") || 
                                      loaiXeLower.contains("car") || loaiXeLower.contains("o to")) {
                                carCount++;
                            }
                        }
                    }
                    motorbikeCountMap.put(householdId, motorbikeCount);
                    carCountMap.put(householdId, carCount);
                }

                // Step 3: Prepare data structures
                LocalDateTime now = LocalDateTime.now();
                java.util.List<java.util.Map<String, Object>> phieuDataList = new java.util.ArrayList<>();
                
                // Step 4: Calculate and prepare PhieuThu data for each household
                for (HoGiaDinh household : allHouseholds) {
                    int householdId = household.getId();
                    
                    // Calculate total amount for this household
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    List<ChiTietThu> chiTietList = new ArrayList<>();

                    // Calculate each mandatory fee based on tinhTheo
                    for (KhoanThu fee : mandatoryFees) {
                        BigDecimal feeAmount = calculateFeeAmount(
                            fee, 
                            household, 
                            memberCountMap.get(householdId),
                            motorbikeCountMap.get(householdId),
                            carCountMap.get(householdId)
                        );

                        if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                            totalAmount = totalAmount.add(feeAmount);
                            
                            // Create ChiTietThu
                            ChiTietThu chiTiet = new ChiTietThu();
                            chiTiet.setMaKhoan(fee.getId());
                            chiTiet.setSoLuong(BigDecimal.ONE);
                            chiTiet.setDonGia(fee.getDonGia());
                            chiTiet.setThanhTien(feeAmount);
                            chiTietList.add(chiTiet);
                        }
                    }

                    // Only create receipt if total amount > 0
                    if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        java.util.Map<String, Object> phieuData = new java.util.HashMap<>();
                        phieuData.put("maHo", householdId);
                        phieuData.put("tongTien", totalAmount);
                        phieuData.put("chiTietList", chiTietList);
                        phieuDataList.add(phieuData);
                    }
                }

                // Step 5: Insert PhieuThu one by one to get generated IDs, then batch insert ChiTietThu
                String insertPhieuThu = "INSERT INTO PhieuThu (maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                String insertChiTiet = "INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien) VALUES (?, ?, ?, ?, ?)";
                
                PreparedStatement chiTietStmt = conn.prepareStatement(insertChiTiet);
                
                for (java.util.Map<String, Object> phieuData : phieuDataList) {
                    // Insert PhieuThu and get generated ID
                    int maPhieu = -1;
                    try (PreparedStatement phieuStmt = conn.prepareStatement(insertPhieuThu, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        phieuStmt.setInt(1, (Integer) phieuData.get("maHo"));
                        phieuStmt.setInt(2, maDotThu);
                        phieuStmt.setInt(3, maTaiKhoan);
                        phieuStmt.setTimestamp(4, Timestamp.valueOf(now));
                        phieuStmt.setBigDecimal(5, (BigDecimal) phieuData.get("tongTien"));
                        phieuStmt.setString(6, "ChuaThu");
                        phieuStmt.setString(7, null);
                        phieuStmt.setString(8, null);
                        
                        int rowsAffected = phieuStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            try (ResultSet generatedKeys = phieuStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    maPhieu = generatedKeys.getInt(1);
                                }
                            }
                        }
                    }
                    
                    if (maPhieu > 0) {
                        successCount++;
                        
                        // Add ChiTietThu to batch
                        @SuppressWarnings("unchecked")
                        List<ChiTietThu> chiTietList = (List<ChiTietThu>) phieuData.get("chiTietList");
                        for (ChiTietThu chiTiet : chiTietList) {
                            chiTietStmt.setInt(1, maPhieu);
                            chiTietStmt.setInt(2, chiTiet.getMaKhoan());
                            chiTietStmt.setBigDecimal(3, chiTiet.getSoLuong());
                            chiTietStmt.setBigDecimal(4, chiTiet.getDonGia());
                            chiTietStmt.setBigDecimal(5, chiTiet.getThanhTien());
                            chiTietStmt.addBatch();
                        }
                    }
                }

                // Step 6: Execute ChiTietThu batch insert
                chiTietStmt.executeBatch();
                chiTietStmt.close();

                // Commit transaction
                conn.commit();
                logger.log(Level.INFO, "Successfully created " + successCount + " batch receipts for drive: " + maDotThu);
                return successCount;

            } catch (Exception e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Error creating batch receipts for drive: " + maDotThu, e);
                e.printStackTrace();
                throw new RuntimeException("Failed to create batch receipts: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in createBatch for maDotThu: " + maDotThu, e);
            e.printStackTrace();
            throw new RuntimeException("Database error while creating batch receipts: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getInvoiceDetail(int id) {
        // SQL query với JOIN để lấy đầy đủ thông tin
        String sql = "SELECT " +
                     "pt.id as maPhieu, pt.maHo, pt.maDot, pt.maTaiKhoan, pt.ngayLap, " +
                     "pt.tongTien, pt.trangThai, pt.hinhThucThu, " +
                     "hg.soPhong, hg.maChuHo, " +
                     "nk.hoTen as tenChuHo, " +
                     "dt.tenDot, " +
                     "tk.hoTen as tenNguoiLap " +
                     "FROM PhieuThu pt " +
                     "INNER JOIN HoGiaDinh hg ON pt.maHo = hg.id " +
                     "LEFT JOIN NhanKhau nk ON hg.maChuHo = nk.id " +
                     "LEFT JOIN DotThu dt ON pt.maDot = dt.id " +
                     "LEFT JOIN TaiKhoan tk ON pt.maTaiKhoan = tk.id " +
                     "WHERE pt.id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new NoSuchElementException("Phiếu thu với ID " + id + " không tồn tại");
                }
                
                // Build invoice detail map
                Map<String, Object> invoiceDetail = new HashMap<>();
                
                // General info
                invoiceDetail.put("id", rs.getInt("maPhieu"));
                invoiceDetail.put("maHo", rs.getInt("maHo"));
                invoiceDetail.put("soPhong", rs.getInt("soPhong"));
                invoiceDetail.put("maDot", rs.getInt("maDot"));
                invoiceDetail.put("tenDot", rs.getString("tenDot"));
                invoiceDetail.put("tenChuHo", rs.getString("tenChuHo"));
                invoiceDetail.put("tongTien", rs.getBigDecimal("tongTien"));
                invoiceDetail.put("trangThai", rs.getString("trangThai"));
                invoiceDetail.put("hinhThucThu", rs.getString("hinhThucThu"));
                invoiceDetail.put("ngayLap", rs.getTimestamp("ngayLap"));
                invoiceDetail.put("tenNguoiLap", rs.getString("tenNguoiLap"));
                
                // Get ChiTietThu with KhoanThu info
                String chiTietSql = "SELECT " +
                                   "ct.id, ct.maPhieu, ct.maKhoan, ct.soLuong, ct.donGia, ct.thanhTien, " +
                                   "kt.tenKhoan, kt.donViTinh " +
                                   "FROM ChiTietThu ct " +
                                   "INNER JOIN KhoanThu kt ON ct.maKhoan = kt.id " +
                                   "WHERE ct.maPhieu = ? " +
                                   "ORDER BY ct.id";
                
                List<Map<String, Object>> chiTietList = new ArrayList<>();
                try (PreparedStatement chiTietStmt = conn.prepareStatement(chiTietSql)) {
                    chiTietStmt.setInt(1, id);
                    try (ResultSet chiTietRs = chiTietStmt.executeQuery()) {
                        while (chiTietRs.next()) {
                            Map<String, Object> chiTiet = new HashMap<>();
                            chiTiet.put("id", chiTietRs.getInt("id"));
                            chiTiet.put("maKhoan", chiTietRs.getInt("maKhoan"));
                            chiTiet.put("tenKhoan", chiTietRs.getString("tenKhoan"));
                            chiTiet.put("soLuong", chiTietRs.getBigDecimal("soLuong"));
                            chiTiet.put("donGia", chiTietRs.getBigDecimal("donGia"));
                            chiTiet.put("thanhTien", chiTietRs.getBigDecimal("thanhTien"));
                            chiTiet.put("donViTinh", chiTietRs.getString("donViTinh"));
                            chiTietList.add(chiTiet);
                        }
                    }
                }
                
                invoiceDetail.put("chiTietList", chiTietList);
                
                return invoiceDetail;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting invoice detail for id: " + id, e);
            e.printStackTrace();
            throw new RuntimeException("Database error while retrieving invoice detail: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream exportInvoiceToPdf(int id) {
        try {
            // Get invoice detail
            Map<String, Object> invoiceDetail = getInvoiceDetail(id);
            
            // Create PDF document
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Add header
            Font headerFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph header = new Paragraph("PHIẾU THU", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            document.add(header);
            
            // Add apartment name
            Font companyFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Paragraph company = new Paragraph("CHUNG CƯ BLUEMOON", companyFont);
            company.setAlignment(Element.ALIGN_CENTER);
            company.setSpacingAfter(30);
            document.add(company);
            
            // Add invoice info
            Font normalFont = new Font(Font.HELVETICA, 10);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            
            Timestamp ngayLap = (Timestamp) invoiceDetail.get("ngayLap");
            String ngayLapStr = ngayLap != null ? 
                ngayLap.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
            
            Paragraph invoiceInfo = new Paragraph();
            invoiceInfo.add(new com.lowagie.text.Chunk("Mã phiếu: ", boldFont));
            invoiceInfo.add(new com.lowagie.text.Chunk(String.valueOf(invoiceDetail.get("id")), normalFont));
            invoiceInfo.add(new com.lowagie.text.Chunk("        Ngày lập: ", boldFont));
            invoiceInfo.add(new com.lowagie.text.Chunk(ngayLapStr, normalFont));
            invoiceInfo.setSpacingAfter(10);
            document.add(invoiceInfo);
            
            // Add customer info
            Paragraph customerInfo = new Paragraph();
            customerInfo.add(new com.lowagie.text.Chunk("Hộ gia đình: ", boldFont));
            customerInfo.add(new com.lowagie.text.Chunk("Phòng " + invoiceDetail.get("soPhong"), normalFont));
            customerInfo.add(new com.lowagie.text.Chunk("        Chủ hộ: ", boldFont));
            customerInfo.add(new com.lowagie.text.Chunk((String) invoiceDetail.getOrDefault("tenChuHo", "N/A"), normalFont));
            customerInfo.setSpacingAfter(10);
            document.add(customerInfo);
            
            Paragraph dotInfo = new Paragraph();
            dotInfo.add(new com.lowagie.text.Chunk("Đợt thu: ", boldFont));
            dotInfo.add(new com.lowagie.text.Chunk((String) invoiceDetail.getOrDefault("tenDot", "N/A"), normalFont));
            dotInfo.setSpacingAfter(20);
            document.add(dotInfo);
            
            // Add items table
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chiTietList = (List<Map<String, Object>>) invoiceDetail.get("chiTietList");
            
            if (chiTietList != null && !chiTietList.isEmpty()) {
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1, 3, 1, 1.5f, 1.5f});
                
                // Table header
                table.addCell(new PdfPCell(new Phrase("STT", boldFont)));
                table.addCell(new PdfPCell(new Phrase("Tên khoản thu", boldFont)));
                table.addCell(new PdfPCell(new Phrase("SL", boldFont)));
                table.addCell(new PdfPCell(new Phrase("Đơn giá", boldFont)));
                table.addCell(new PdfPCell(new Phrase("Thành tiền", boldFont)));
                
                // Table rows
                int stt = 1;
                DecimalFormat df = new DecimalFormat("#,##0");
                for (Map<String, Object> chiTiet : chiTietList) {
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(stt++), normalFont)));
                    table.addCell(new PdfPCell(new Phrase((String) chiTiet.getOrDefault("tenKhoan", "N/A"), normalFont)));
                    
                    BigDecimal soLuong = (BigDecimal) chiTiet.get("soLuong");
                    table.addCell(new PdfPCell(new Phrase(
                        soLuong != null ? soLuong.toString() : "0", normalFont)));
                    
                    BigDecimal donGia = (BigDecimal) chiTiet.get("donGia");
                    table.addCell(new PdfPCell(new Phrase(
                        donGia != null ? df.format(donGia) + " đ" : "0 đ", normalFont)));
                    
                    BigDecimal thanhTien = (BigDecimal) chiTiet.get("thanhTien");
                    table.addCell(new PdfPCell(new Phrase(
                        thanhTien != null ? df.format(thanhTien) + " đ" : "0 đ", normalFont)));
                }
                
                document.add(table);
            }
            
            // Add total
            BigDecimal tongTien = (BigDecimal) invoiceDetail.get("tongTien");
            Paragraph total = new Paragraph();
            total.setSpacingBefore(20);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.add(new com.lowagie.text.Chunk("TỔNG CỘNG: ", boldFont));
            total.add(new com.lowagie.text.Chunk(
                tongTien != null ? new DecimalFormat("#,##0").format(tongTien) + " đ" : "0 đ", 
                new Font(Font.HELVETICA, 12, Font.BOLD)));
            document.add(total);
            
            // Add signature area
            document.add(new Paragraph(" "));
            Paragraph signature = new Paragraph();
            signature.setSpacingBefore(40);
            signature.add(new com.lowagie.text.Chunk("Người lập: ", boldFont));
            signature.add(new com.lowagie.text.Chunk((String) invoiceDetail.getOrDefault("tenNguoiLap", "N/A"), normalFont));
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);
            
            document.close();
            
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error exporting invoice to PDF for id: " + id, e);
            e.printStackTrace();
            throw new RuntimeException("Error exporting invoice to PDF: " + e.getMessage(), e);
        }
    }
}
