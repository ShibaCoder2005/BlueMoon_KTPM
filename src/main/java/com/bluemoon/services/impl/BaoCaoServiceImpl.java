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
import com.bluemoon.services.BaoCaoService;
import com.bluemoon.utils.DatabaseConnector;

public class BaoCaoServiceImpl implements BaoCaoService {

    private static final Logger logger = Logger.getLogger(BaoCaoServiceImpl.class.getName());

    @Override
    public List<BaoCaoThu> getRevenueReport(int month, int year) {
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        return getRevenueReport(fromDate, toDate);
    }

    @Override
    public List<BaoCaoThu> getRevenueReport(LocalDate fromDate, LocalDate toDate) {
        List<BaoCaoThu> result = new ArrayList<>();

        // SQL: JOIN các bảng để lấy đầy đủ thông tin hiển thị
        String sql = "SELECT " +
                     "pt.id as maPhieu, pt.maHo, pt.maDot, pt.ngayLap, pt.tongTien, " +
                     "pt.trangThai, pt.hinhThucThu, " +
                     "hg.soPhong, " +
                     "dt.tenDot, " +
                     "COALESCE(nk.hoTen, 'Chưa xác định') as tenChuHo, " +
                     "tk.hoTen as nguoiThu " +
                     "FROM PhieuThu pt " +
                     "JOIN HoGiaDinh hg ON pt.maHo = hg.id " +
                     "LEFT JOIN DotThu dt ON pt.maDot = dt.id " +
                     "LEFT JOIN TaiKhoan tk ON pt.maTaiKhoan = tk.id " +
                     "LEFT JOIN NhanKhau nk ON hg.id = nk.maHo AND nk.quanHeVoiChuHo = 'ChuHo' " +
                     "WHERE pt.trangThai = 'DaThu' " +
                     "AND DATE(pt.ngayLap) >= ? AND DATE(pt.ngayLap) <= ? " +
                     "ORDER BY pt.ngayLap DESC, pt.id";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(fromDate));
            stmt.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Lấy dữ liệu từ ResultSet
                    int maHo = rs.getInt("maHo");
                    String soPhong = String.valueOf(rs.getInt("soPhong"));
                    String chuHo = rs.getString("tenChuHo");
                    
                    Timestamp ngayLap = rs.getTimestamp("ngayLap");
                    LocalDateTime ngayThu = ngayLap != null ? ngayLap.toLocalDateTime() : null;
                    BigDecimal soTien = rs.getBigDecimal("tongTien");
                    
                    String tenDot = rs.getString("tenDot");
                    String trangThai = rs.getString("trangThai");
                    String hinhThucThu = rs.getString("hinhThucThu");
                    String nguoiThu = rs.getString("nguoiThu");

                    // Tạo đối tượng DTO và gán giá trị
                    BaoCaoThu dto = new BaoCaoThu();
                    dto.setMaPhieu(rs.getInt("maPhieu"));
                    dto.setMaHo(maHo);
                    dto.setSoPhong(soPhong);
                    dto.setChuHo(chuHo);
                    // Chuyển đổi LocalDateTime sang LocalDate nếu cần thiết
                    dto.setNgayThu(ngayThu != null ? ngayThu.toLocalDate() : null);
                    dto.setSoTien(soTien != null ? soTien : BigDecimal.ZERO);
                    dto.setTenDot(tenDot != null ? tenDot : "-");
                    dto.setTrangThai(trangThai);
                    dto.setHinhThuc(hinhThucThu != null ? hinhThucThu : "-");
                    dto.setNguoiThu(nguoiThu);

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
        return getDebtReport(0);
    }

    @Override
    public List<BaoCaoCongNo> getDebtReport(int maDot) {
        List<BaoCaoCongNo> result = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("hg.id as maHo, hg.soPhong, ");
        sql.append("dt.id as maDot, dt.tenDot, ");
        sql.append("COALESCE(nk.hoTen, 'Chưa xác định') as tenChuHo, ");
        sql.append("COALESCE(pt.tongTien, 0) as tongTien, ");
        sql.append("CASE WHEN pt.trangThai = 'DaThu' THEN pt.tongTien ELSE 0 END as daThu, ");
        sql.append("CASE WHEN pt.trangThai != 'DaThu' OR pt.id IS NULL THEN COALESCE(pt.tongTien, 0) ELSE 0 END as conNo, ");
        sql.append("CASE WHEN pt.id IS NULL THEN 'Chưa tạo phiếu' WHEN pt.trangThai = 'DaThu' THEN 'Đã nộp' ELSE 'Chưa nộp' END as trangThai ");
        sql.append("FROM HoGiaDinh hg ");
        sql.append("CROSS JOIN DotThu dt ");
        sql.append("LEFT JOIN PhieuThu pt ON pt.maHo = hg.id AND pt.maDot = dt.id ");
        sql.append("LEFT JOIN NhanKhau nk ON hg.id = nk.maHo AND nk.quanHeVoiChuHo = 'ChuHo' ");
        sql.append("WHERE hg.trangThai = 'DangO' ");
        
        if (maDot > 0) {
            sql.append("AND dt.id = ? ");
        }
        
        sql.append("AND (pt.id IS NULL OR pt.trangThai != 'DaThu') ");
        sql.append("ORDER BY dt.id DESC, hg.soPhong ASC");

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (maDot > 0) {
                stmt.setInt(1, maDot);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BaoCaoCongNo dto = new BaoCaoCongNo();
                    dto.setMaHo(rs.getInt("maHo"));
                    dto.setSoPhong(String.valueOf(rs.getInt("soPhong")));
                    dto.setChuHo(rs.getString("tenChuHo"));
                    dto.setMaDot(rs.getInt("maDot"));
                    dto.setTenDot(rs.getString("tenDot"));
                    dto.setTongTien(rs.getBigDecimal("tongTien"));
                    dto.setDaThu(rs.getBigDecimal("daThu"));
                    dto.setConNo(rs.getBigDecimal("conNo"));
                    dto.setTrangThai(rs.getString("trangThai"));

                    result.add(dto);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting debt report", e);
            e.printStackTrace();
        }

        return result;
    }

    // --- XUẤT EXCEL ---

    @Override
    public InputStream exportRevenueToExcel(List<BaoCaoThu> data, LocalDate fromDate, LocalDate toDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo Thu");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Number Style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Mã Hộ", "Số Phòng", "Chủ Hộ", "Ngày Thu", "Số Tiền", "Đợt Thu", "Trạng Thái", "Hình Thức"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int rowNum = 1;
            int stt = 1;
            BigDecimal total = BigDecimal.ZERO;

            for (BaoCaoThu dto : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(dto.getMaHo());
                row.createCell(2).setCellValue(dto.getSoPhong());
                row.createCell(3).setCellValue(dto.getChuHo());
                
                String ngayStr = (dto.getNgayThu() != null) ? dto.getNgayThu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                row.createCell(4).setCellValue(ngayStr);

                Cell tienCell = row.createCell(5);
                tienCell.setCellValue(dto.getSoTien().doubleValue());
                tienCell.setCellStyle(numberStyle);
                total = total.add(dto.getSoTien());

                row.createCell(6).setCellValue(dto.getTenDot());
                row.createCell(7).setCellValue(dto.getTrangThai());
                row.createCell(8).setCellValue(dto.getHinhThuc());
            }

            // Total Row
            Row totalRow = sheet.createRow(rowNum);
            Cell lblCell = totalRow.createCell(4);
            lblCell.setCellValue("TỔNG CỘNG:");
            lblCell.setCellStyle(headerStyle);
            
            Cell valCell = totalRow.createCell(5);
            valCell.setCellValue(total.doubleValue());
            valCell.setCellStyle(numberStyle);

            // Auto size
            for(int i=0; i<headers.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error export revenue excel", e);
            return null;
        }
    }

    @Override
    public InputStream exportDebtToExcel(List<BaoCaoCongNo> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Báo cáo Công nợ");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Number Style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Mã Hộ", "Số Phòng", "Chủ Hộ", "Đợt Thu", "Tổng Tiền", "Đã Thu", "Còn Nợ", "Trạng Thái"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
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

                Cell cTong = row.createCell(5);
                cTong.setCellValue(dto.getTongTien().doubleValue());
                cTong.setCellStyle(numberStyle);

                Cell cDaThu = row.createCell(6);
                cDaThu.setCellValue(dto.getDaThu().doubleValue());
                cDaThu.setCellStyle(numberStyle);

                Cell cConNo = row.createCell(7);
                cConNo.setCellValue(dto.getConNo().doubleValue());
                cConNo.setCellStyle(numberStyle);
                totalDebt = totalDebt.add(dto.getConNo());

                row.createCell(8).setCellValue(dto.getTrangThai());
            }

            // Total Row
            Row totalRow = sheet.createRow(rowNum);
            Cell lblCell = totalRow.createCell(6);
            lblCell.setCellValue("TỔNG NỢ:");
            lblCell.setCellStyle(headerStyle);

            Cell valCell = totalRow.createCell(7);
            valCell.setCellValue(totalDebt.doubleValue());
            valCell.setCellStyle(numberStyle);

            // Auto size
            for(int i=0; i<headers.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error export debt excel", e);
            return null;
        }
    }
}