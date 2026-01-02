package com.bluemoon.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bluemoon.models.BaoCaoCongNo;
import com.bluemoon.models.BaoCaoThu;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.BaoCaoService;
import com.bluemoon.services.DotThuService;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.NhanKhauService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Implementation của BaoCaoService.
 * Xử lý báo cáo thu, công nợ và xuất Excel.
 */
public class BaoCaoServiceImpl implements BaoCaoService {
    
    private static final Logger logger = Logger.getLogger(BaoCaoServiceImpl.class.getName());
    
    private final PhieuThuService phieuThuService;
    private final HoGiaDinhService hoGiaDinhService;
    private final NhanKhauService nhanKhauService;
    private final DotThuService dotThuService;
    
    public BaoCaoServiceImpl() {
        this.phieuThuService = new PhieuThuServiceImpl();
        this.hoGiaDinhService = new HoGiaDinhServiceImpl();
        this.nhanKhauService = new NhanKhauServiceImpl();
        this.dotThuService = new DotThuServiceImpl();
    }
    
    @Override
    public List<BaoCaoThu> getRevenueReport(int month, int year) {
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        return getRevenueReport(fromDate, toDate);
    }
    
    @Override
    public List<BaoCaoThu> getRevenueReport(LocalDate fromDate, LocalDate toDate) {
        List<BaoCaoThu> result = new ArrayList<>();
        
        // SQL query để lấy dữ liệu từ PhieuThu kèm thông tin hộ gia đình và đợt thu
        String sql = "SELECT " +
                     "pt.id as maPhieu, pt.maHo, pt.maDot, pt.ngayLap, pt.tongTien, " +
                     "pt.trangThai, pt.hinhThucThu, " +
                     "hg.soPhong, hg.maChuHo, " +
                     "dt.tenDot " +
                     "FROM PhieuThu pt " +
                     "INNER JOIN HoGiaDinh hg ON pt.maHo = hg.id " +
                     "LEFT JOIN DotThu dt ON pt.maDot = dt.id " +
                     "WHERE DATE(pt.ngayLap) >= ? AND DATE(pt.ngayLap) <= ? " +
                     "ORDER BY pt.ngayLap DESC, pt.id";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int maHo = rs.getInt("maHo");
                    int soPhong = rs.getInt("soPhong");
                    String maChuHo = rs.getString("maChuHo"); // maChuHo bây giờ là soCCCD (VARCHAR)
                    
                    // Lấy tên chủ hộ - tìm theo soCCCD thay vì id
                    String chuHo = "N/A";
                    if (maChuHo != null && !maChuHo.trim().isEmpty()) {
                        try {
                            // Tìm NhanKhau theo soCCCD (cần thêm method findBySoCCCD hoặc dùng getAll rồi filter)
                            List<NhanKhau> allResidents = nhanKhauService.getAll();
                            for (NhanKhau nk : allResidents) {
                                if (maChuHo.equals(nk.getSoCCCD())) {
                                    chuHo = nk.getHoTen();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Error getting chu ho name for soCCCD: " + maChuHo, e);
                        }
                    }
                    
                    Timestamp ngayLap = rs.getTimestamp("ngayLap");
                    LocalDateTime ngayThu = ngayLap != null ? ngayLap.toLocalDateTime() : null;
                    BigDecimal soTien = rs.getBigDecimal("tongTien");
                    String tenDot = rs.getString("tenDot");
                    String trangThai = rs.getString("trangThai");
                    String hinhThucThu = rs.getString("hinhThucThu");
                    
                    BaoCaoThu dto = new BaoCaoThu(
                        maHo,
                        soPhong,
                        chuHo,
                        ngayThu,
                        soTien != null ? soTien : BigDecimal.ZERO,
                        tenDot != null ? tenDot : "N/A",
                        tenDot != null ? tenDot : "N/A",
                        trangThai != null ? trangThai : "N/A",
                        hinhThucThu != null ? hinhThucThu : "N/A"
                    );
                    
                    result.add(dto);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting revenue report", e);
            e.printStackTrace();
        }
        
        return result;
    }
    
    @Override
    public List<BaoCaoCongNo> getDebtReport() {
        return getDebtReport(0); // 0 means all
    }
    
    @Override
    public List<BaoCaoCongNo> getDebtReport(int maDot) {
        List<BaoCaoCongNo> result = new ArrayList<>();
        
        // SQL query để lấy các hộ gia đình có công nợ
        // Công nợ = các hộ có PhieuThu với trangThai != 'DaThu' hoặc chưa có PhieuThu cho đợt thu
        String sql;
        if (maDot > 0) {
            sql = "SELECT DISTINCT " +
                  "hg.id as maHo, hg.soPhong, hg.maChuHo, " +
                  "dt.id as maDot, dt.tenDot, " +
                  "COALESCE(pt.tongTien, 0) as tongTien, " +
                  "CASE WHEN pt.trangThai = 'DaThu' THEN pt.tongTien ELSE 0 END as daThu, " +
                  "CASE WHEN pt.trangThai != 'DaThu' OR pt.id IS NULL THEN COALESCE(pt.tongTien, 0) ELSE 0 END as conNo, " +
                  "CASE WHEN pt.id IS NULL THEN 'Chưa có phiếu thu' " +
                  "     WHEN pt.trangThai = 'DaThu' THEN 'Đã thanh toán' " +
                  "     ELSE 'Chưa thanh toán' END as trangThai " +
                  "FROM HoGiaDinh hg " +
                  "CROSS JOIN DotThu dt " +
                  "LEFT JOIN PhieuThu pt ON pt.maHo = hg.id AND pt.maDot = dt.id " +
                  "WHERE dt.id = ? " +
                  "  AND (pt.id IS NULL OR pt.trangThai != 'DaThu') " +
                  "ORDER BY hg.soPhong, dt.id";
        } else {
            sql = "SELECT DISTINCT " +
                  "hg.id as maHo, hg.soPhong, hg.maChuHo, " +
                  "dt.id as maDot, dt.tenDot, " +
                  "COALESCE(pt.tongTien, 0) as tongTien, " +
                  "CASE WHEN pt.trangThai = 'DaThu' THEN pt.tongTien ELSE 0 END as daThu, " +
                  "CASE WHEN pt.trangThai != 'DaThu' OR pt.id IS NULL THEN COALESCE(pt.tongTien, 0) ELSE 0 END as conNo, " +
                  "CASE WHEN pt.id IS NULL THEN 'Chưa có phiếu thu' " +
                  "     WHEN pt.trangThai = 'DaThu' THEN 'Đã thanh toán' " +
                  "     ELSE 'Chưa thanh toán' END as trangThai " +
                  "FROM HoGiaDinh hg " +
                  "CROSS JOIN DotThu dt " +
                  "LEFT JOIN PhieuThu pt ON pt.maHo = hg.id AND pt.maDot = dt.id " +
                  "WHERE (pt.id IS NULL OR pt.trangThai != 'DaThu') " +
                  "ORDER BY hg.soPhong, dt.id";
        }
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (maDot > 0) {
                stmt.setInt(1, maDot);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int maHo = rs.getInt("maHo");
                    int soPhong = rs.getInt("soPhong");
                    String maChuHo = rs.getString("maChuHo"); // maChuHo bây giờ là soCCCD (VARCHAR)
                    int maDotResult = rs.getInt("maDot");
                    String tenDot = rs.getString("tenDot");
                    BigDecimal tongTien = rs.getBigDecimal("tongTien");
                    BigDecimal daThu = rs.getBigDecimal("daThu");
                    BigDecimal conNo = rs.getBigDecimal("conNo");
                    String trangThai = rs.getString("trangThai");
                    
                    // Lấy tên chủ hộ - tìm theo soCCCD thay vì id
                    String chuHo = "N/A";
                    if (maChuHo != null && !maChuHo.trim().isEmpty()) {
                        try {
                            // Tìm NhanKhau theo soCCCD
                            List<NhanKhau> allResidents = nhanKhauService.getAll();
                            for (NhanKhau nk : allResidents) {
                                if (maChuHo.equals(nk.getSoCCCD())) {
                                    chuHo = nk.getHoTen();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Error getting chu ho name for soCCCD: " + maChuHo, e);
                        }
                    }
                    
                    BaoCaoCongNo dto = new BaoCaoCongNo(
                        maHo,
                        soPhong,
                        chuHo,
                        tenDot != null ? tenDot : "N/A",
                        maDotResult,
                        tongTien != null ? tongTien : BigDecimal.ZERO,
                        daThu != null ? daThu : BigDecimal.ZERO,
                        conNo != null ? conNo : BigDecimal.ZERO,
                        trangThai != null ? trangThai : "N/A",
                        ""
                    );
                    
                    result.add(dto);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting debt report", e);
            e.printStackTrace();
        }
        
        return result;
    }
    
    @Override
    public InputStream exportRevenueToExcel(List<BaoCaoThu> data, LocalDate fromDate, LocalDate toDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo Thu");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Mã Hộ", "Số Phòng", "Chủ Hộ", "Ngày Thu", 
                               "Số Tiền", "Đợt Thu", "Trạng Thái", "Hình Thức Thu"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            int stt = 1;
            BigDecimal total = BigDecimal.ZERO;
            
            for (BaoCaoThu dto : data) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(dto.getMaHo());
                row.createCell(2).setCellValue(dto.getSoPhong());
                row.createCell(3).setCellValue(dto.getChuHo());
                
                if (dto.getNgayThu() != null) {
                    row.createCell(4).setCellValue(
                        dto.getNgayThu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    );
                } else {
                    row.createCell(4).setCellValue("N/A");
                }
                
                Cell amountCell = row.createCell(5);
                amountCell.setCellValue(dto.getSoTien().doubleValue());
                amountCell.setCellStyle(numberStyle);
                total = total.add(dto.getSoTien());
                
                row.createCell(6).setCellValue(dto.getTenDot());
                row.createCell(7).setCellValue(dto.getTrangThai());
                row.createCell(8).setCellValue(dto.getHinhThucThu());
            }
            
            // Create total row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(4);
            totalLabelCell.setCellValue("TỔNG CỘNG:");
            totalLabelCell.setCellStyle(headerStyle);
            
            Cell totalCell = totalRow.createCell(5);
            totalCell.setCellValue(total.doubleValue());
            totalCell.setCellStyle(numberStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error exporting revenue to Excel", e);
            e.printStackTrace();
            throw new RuntimeException("Error exporting to Excel: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream exportDebtToExcel(List<BaoCaoCongNo> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo Công nợ");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Create data style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Mã Hộ", "Số Phòng", "Chủ Hộ", "Đợt Thu", 
                               "Tổng Tiền", "Đã Thu", "Còn Nợ", "Trạng Thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            int stt = 1;
            BigDecimal totalDebt = BigDecimal.ZERO;
            
            for (BaoCaoCongNo dto : data) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(dto.getMaHo());
                row.createCell(2).setCellValue(dto.getSoPhong());
                row.createCell(3).setCellValue(dto.getChuHo());
                row.createCell(4).setCellValue(dto.getTenDot());
                
                Cell tongTienCell = row.createCell(5);
                tongTienCell.setCellValue(dto.getTongTien().doubleValue());
                tongTienCell.setCellStyle(numberStyle);
                
                Cell daThuCell = row.createCell(6);
                daThuCell.setCellValue(dto.getDaThu().doubleValue());
                daThuCell.setCellStyle(numberStyle);
                
                Cell conNoCell = row.createCell(7);
                conNoCell.setCellValue(dto.getConNo().doubleValue());
                conNoCell.setCellStyle(numberStyle);
                totalDebt = totalDebt.add(dto.getConNo());
                
                row.createCell(8).setCellValue(dto.getTrangThai());
            }
            
            // Create total row
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(6);
            totalLabelCell.setCellValue("TỔNG CÔNG NỢ:");
            totalLabelCell.setCellStyle(headerStyle);
            
            Cell totalCell = totalRow.createCell(7);
            totalCell.setCellValue(totalDebt.doubleValue());
            totalCell.setCellStyle(numberStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error exporting debt to Excel", e);
            e.printStackTrace();
            throw new RuntimeException("Error exporting to Excel: " + e.getMessage(), e);
        }
    }
}

