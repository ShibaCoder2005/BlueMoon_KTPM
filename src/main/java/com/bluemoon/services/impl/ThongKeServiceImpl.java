package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluemoon.services.ThongKeService;
import com.bluemoon.utils.DatabaseConnector;

public class ThongKeServiceImpl implements ThongKeService {

    // 1. Dashboard tổng quan
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Tổng số hộ đang ở
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM HoGiaDinh WHERE trangThai = 'DangO'")) {
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.put("totalHouseholds", rs.getInt(1)); }
            }
            // Tổng nhân khẩu (Cư trú + Tạm trú)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM NhanKhau WHERE tinhTrang IN ('CuTru', 'TamTru')")) {
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.put("totalResidents", rs.getInt(1)); }
            }
            // Tổng phiếu thu
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM PhieuThu")) {
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.put("totalReceipts", rs.getInt(1)); }
            }
            // Tổng doanh thu (đã thu)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(tongTien), 0) FROM PhieuThu WHERE trangThai = 'DaThu'")) {
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.put("totalRevenue", rs.getBigDecimal(1)); }
            }
            // Tổng công nợ (chưa thu)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(tongTien), 0) FROM PhieuThu WHERE trangThai != 'DaThu'")) {
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) stats.put("totalDebt", rs.getBigDecimal(1)); }
            }
            // Doanh thu tháng này
            String sqlMonth = "SELECT COALESCE(SUM(tongTien), 0) FROM PhieuThu WHERE trangThai = 'DaThu' " +
                              "AND EXTRACT(MONTH FROM ngayLap) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                              "AND EXTRACT(YEAR FROM ngayLap) = EXTRACT(YEAR FROM CURRENT_DATE)";
            try (PreparedStatement ps = conn.prepareStatement(sqlMonth); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("monthlyRevenue", rs.getBigDecimal(1));
            }

        } catch (Exception e) { e.printStackTrace(); }
        return stats;
    }

    // 2. Biểu đồ doanh thu theo đợt
    @Override
    public Map<String, Number> getRevenueStats(LocalDate fromDate, LocalDate toDate) {
        Map<String, Number> result = new HashMap<>();
        String sql = "SELECT d.tenDot, COALESCE(SUM(p.tongTien), 0) as total " +
                     "FROM PhieuThu p JOIN DotThu d ON p.maDot = d.id " +
                     "WHERE p.trangThai = 'DaThu' AND p.ngayLap BETWEEN ? AND ? " +
                     "GROUP BY d.tenDot";
        
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fromDate));
            ps.setDate(2, java.sql.Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getString("tenDot"), rs.getBigDecimal("total"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // 3. Thống kê công nợ (Đã nộp vs Chưa nộp)
    @Override
    public Map<String, Number> getDebtStats() {
        Map<String, Number> stats = new HashMap<>();
        String sql = "SELECT " +
                     "SUM(CASE WHEN trangThai = 'DaThu' THEN tongTien ELSE 0 END) as daNop, " +
                     "SUM(CASE WHEN trangThai != 'DaThu' THEN tongTien ELSE 0 END) as chuaNop " +
                     "FROM PhieuThu";
        try (Connection conn = DatabaseConnector.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("Đã nộp", rs.getBigDecimal("daNop"));
                stats.put("Chưa nộp", rs.getBigDecimal("chuaNop"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return stats;
    }

    // 4. Tổng doanh thu trong khoảng
    @Override
    public BigDecimal getTotalRevenue(LocalDate fromDate, LocalDate toDate) {
        String sql = "SELECT COALESCE(SUM(tongTien), 0) FROM PhieuThu WHERE trangThai = 'DaThu' AND ngayLap BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fromDate));
            ps.setDate(2, java.sql.Date.valueOf(toDate));
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getBigDecimal(1); }
        } catch (Exception e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    // 5. Tổng nợ hiện tại
    @Override
    public BigDecimal getTotalDebt() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(tongTien), 0) FROM PhieuThu WHERE trangThai != 'DaThu'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (Exception e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    // 6. Báo cáo chi tiết theo đợt
    @Override
    public Map<String, Object> generateCollectionReport(int maDotThu) {
        Map<String, Object> report = new HashMap<>();
        String sql = "SELECT COUNT(*) as totalReceipts, " +
                     "COUNT(CASE WHEN trangThai = 'DaThu' THEN 1 END) as paidReceipts, " +
                     "SUM(tongTien) as totalAmount " +
                     "FROM PhieuThu WHERE maDot = ?";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDotThu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    report.put("totalReceipts", rs.getInt("totalReceipts"));
                    int paid = rs.getInt("paidReceipts");
                    report.put("paidReceipts", paid);
                    report.put("unpaidReceipts", rs.getInt("totalReceipts") - paid);
                    report.put("totalAmount", rs.getBigDecimal("totalAmount"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return report;
    }

    // 7. Chi tiết doanh thu (List)
    @Override
    public List<Map<String, Object>> getRevenueDetails(LocalDate fromDate, LocalDate toDate) {
        return getDetailsCommon(fromDate, toDate, true);
    }

    // 8. Chi tiết nợ (List)
    @Override
    public List<Map<String, Object>> getDebtDetails() {
        return getDetailsCommon(null, null, false);
    }

    // Hàm phụ trợ lấy list
    private List<Map<String, Object>> getDetailsCommon(LocalDate fromDate, LocalDate toDate, boolean isRevenue) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.ngayLap, p.tongTien, p.trangThai, h.maChuHo, d.tenDot " +
            "FROM PhieuThu p " +
            "LEFT JOIN HoGiaDinh h ON p.maHo = h.id " +
            "LEFT JOIN DotThu d ON p.maDot = d.id " +
            "WHERE 1=1 "
        );

        if (isRevenue) {
            // Sửa nhẹ: Dùng DATE(ngayLap) để đảm bảo so sánh chính xác nếu DB có giờ phút
            sql.append("AND p.trangThai = 'DaThu' AND DATE(p.ngayLap) BETWEEN ? AND ? ");
        } else {
            sql.append("AND p.trangThai != 'DaThu' ");
        }
        sql.append("ORDER BY p.ngayLap DESC");

        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (isRevenue) {
                ps.setDate(1, java.sql.Date.valueOf(fromDate));
                ps.setDate(2, java.sql.Date.valueOf(toDate));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maPhieu", rs.getInt("id"));
                    map.put("ngayLap", rs.getTimestamp("ngayLap"));
                    map.put("tongTien", rs.getBigDecimal("tongTien"));
                    map.put("trangThai", rs.getString("trangThai"));
                    map.put("tenHo", rs.getString("maChuHo")); 
                    map.put("tenDot", rs.getString("tenDot"));
                    list.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 9. Thống kê nhân khẩu học (Đã thêm mới để khớp với Interface)
    @Override
    public Map<String, Object> getResidentDemographics() {
        Map<String, Object> demo = new HashMap<>();
        try (Connection conn = DatabaseConnector.getConnection()) {
            
            // a. Giới tính
            Map<String, Integer> byGender = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT gioiTinh, COUNT(*) FROM NhanKhau GROUP BY gioiTinh");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) byGender.put(rs.getString(1), rs.getInt(2));
            }
            demo.put("byGender", byGender);

            // b. Tình trạng
            Map<String, Integer> byStatus = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT tinhTrang, COUNT(*) FROM NhanKhau GROUP BY tinhTrang");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) byStatus.put(rs.getString(1), rs.getInt(2));
            }
            demo.put("byStatus", byStatus);

            // c. Độ tuổi (Tính toán trong Java cho đơn giản)
            Map<String, Integer> byAge = new HashMap<>();
            int child = 0, adult = 0, senior = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT ngaySinh FROM NhanKhau WHERE ngaySinh IS NOT NULL");
                 ResultSet rs = ps.executeQuery()) {
                LocalDate now = LocalDate.now();
                while (rs.next()) {
                    java.sql.Date dobDate = rs.getDate(1);
                    if (dobDate != null) {
                        LocalDate dob = dobDate.toLocalDate();
                        int age = now.getYear() - dob.getYear();
                        if (age < 18) child++;
                        else if (age < 60) adult++;
                        else senior++;
                    }
                }
            }
            byAge.put("Trẻ em (0-17)", child);
            byAge.put("Người lớn (18-59)", adult);
            byAge.put("Người cao tuổi (60+)", senior);
            demo.put("byAgeGroup", byAge);

        } catch (Exception e) { e.printStackTrace(); }
        return demo;
    }
}