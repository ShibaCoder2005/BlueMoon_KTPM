package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bluemoon.models.PhieuThu;
import com.bluemoon.services.DotThuService;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.ThongKeService;
import com.bluemoon.services.impl.DotThuServiceImpl;
import com.bluemoon.services.impl.HoGiaDinhServiceImpl;
import com.bluemoon.services.impl.PhieuThuServiceImpl;

/**
 * Triển khai {@link ThongKeService} bằng cách tổng hợp dữ liệu từ các service khác.
 */
public class ThongKeServiceImpl implements ThongKeService {

    private final PhieuThuService phieuThuService = new PhieuThuServiceImpl();
    private final DotThuService dotThuService = new DotThuServiceImpl();
    private final HoGiaDinhService hoGiaDinhService = new HoGiaDinhServiceImpl();

    @Override
    public Map<String, Number> getRevenueStats(LocalDate fromDate, LocalDate toDate) {
        Map<String, Number> stats = new HashMap<>();
        List<PhieuThu> allPhieuThu = phieuThuService.getAllPhieuThu();

        // Filter by date range
        List<PhieuThu> filteredPhieuThu = allPhieuThu.stream()
                .filter(p -> {
                    if (p.getNgayLap() == null) {
                        return false;
                    }
                    LocalDate ngayLap = p.getNgayLap().toLocalDate();
                    return !ngayLap.isBefore(fromDate) && !ngayLap.isAfter(toDate);
                })
                .collect(Collectors.toList());

        // Group by drive (maDot)
        Map<Integer, BigDecimal> revenueByDrive = filteredPhieuThu.stream()
                .filter(p -> p.getTongTien() != null)
                .collect(Collectors.groupingBy(
                        PhieuThu::getMaDot,
                        Collectors.reducing(BigDecimal.ZERO, PhieuThu::getTongTien, BigDecimal::add)
                ));

        // Convert to Map<String, Number> with drive names
        for (Map.Entry<Integer, BigDecimal> entry : revenueByDrive.entrySet()) {
            com.bluemoon.models.DotThu dotThu = dotThuService.getDotThuById(entry.getKey());
            String driveName = dotThu != null ? dotThu.getTenDot() : "Đợt thu #" + entry.getKey();
            stats.put(driveName, entry.getValue());
        }

        return stats;
    }

    @Override
    public Map<String, Number> getDebtStats() {
        Map<String, Number> stats = new HashMap<>();
        List<PhieuThu> allPhieuThu = phieuThuService.getAllPhieuThu();

        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal unpaidAmount = BigDecimal.ZERO;

        for (PhieuThu phieuThu : allPhieuThu) {
            if (phieuThu.getTongTien() == null) {
                continue;
            }
            String trangThai = phieuThu.getTrangThai();
            if (trangThai != null && (trangThai.equalsIgnoreCase("Đã thu") || trangThai.equalsIgnoreCase("Đã thanh toán"))) {
                paidAmount = paidAmount.add(phieuThu.getTongTien());
            } else {
                unpaidAmount = unpaidAmount.add(phieuThu.getTongTien());
            }
        }

        stats.put("Đã thanh toán", paidAmount);
        stats.put("Chưa thanh toán", unpaidAmount);

        return stats;
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate fromDate, LocalDate toDate) {
        Map<String, Number> revenueStats = getRevenueStats(fromDate, toDate);
        return revenueStats.values().stream()
                .map(n -> n instanceof BigDecimal ? (BigDecimal) n : BigDecimal.valueOf(n.doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalDebt() {
        Map<String, Number> debtStats = getDebtStats();
        Number unpaid = debtStats.get("Chưa thanh toán");
        return unpaid != null ? BigDecimal.valueOf(unpaid.doubleValue()) : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        List<PhieuThu> allPhieuThu = phieuThuService.getAllPhieuThu();

        BigDecimal totalRevenue = allPhieuThu.stream()
                .filter(p -> p.getTongTien() != null)
                .map(PhieuThu::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalHouseholds = hoGiaDinhService.getAll().size();
        long totalReceipts = allPhieuThu.size();

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalHouseholds", totalHouseholds);
        stats.put("totalReceipts", totalReceipts);
        stats.put("totalDebt", getTotalDebt());

        return stats;
    }

    @Override
    public Map<String, Object> generateCollectionReport(int maDotThu) {
        Map<String, Object> report = new HashMap<>();
        List<PhieuThu> phieuThuList = phieuThuService.findPhieuThuByDotThu(maDotThu);

        BigDecimal totalAmount = phieuThuList.stream()
                .filter(p -> p.getTongTien() != null)
                .map(PhieuThu::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = phieuThuList.stream()
                .filter(p -> p.getTrangThai() != null && 
                        (p.getTrangThai().equalsIgnoreCase("Đã thu") || 
                         p.getTrangThai().equalsIgnoreCase("Đã thanh toán")))
                .count();

        report.put("totalAmount", totalAmount);
        report.put("totalReceipts", phieuThuList.size());
        report.put("paidReceipts", paidCount);
        report.put("unpaidReceipts", phieuThuList.size() - paidCount);

        return report;
    }

    @Override
    public List<Map<String, Object>> getRevenueDetails(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> details = new ArrayList<>();
        List<PhieuThu> allPhieuThu = phieuThuService.getAllPhieuThu();

        List<PhieuThu> filteredPhieuThu = allPhieuThu.stream()
                .filter(p -> {
                    if (p.getNgayLap() == null) {
                        return false;
                    }
                    LocalDate ngayLap = p.getNgayLap().toLocalDate();
                    return !ngayLap.isBefore(fromDate) && !ngayLap.isAfter(toDate);
                })
                .collect(Collectors.toList());

        for (PhieuThu phieuThu : filteredPhieuThu) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("maPhieu", phieuThu.getId());
            detail.put("ngayLap", phieuThu.getNgayLap());
            detail.put("tongTien", phieuThu.getTongTien());
            detail.put("trangThai", phieuThu.getTrangThai());

            // Lookup household name
            com.bluemoon.models.HoGiaDinh hoGiaDinh = hoGiaDinhService.findById(phieuThu.getMaHo());
            detail.put("tenHo", hoGiaDinh != null ? hoGiaDinh.getTenChuHo() : "N/A");

            // Lookup drive name
            com.bluemoon.models.DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
            detail.put("tenDot", dotThu != null ? dotThu.getTenDot() : "N/A");

            details.add(detail);
        }

        return details;
    }

    @Override
    public List<Map<String, Object>> getDebtDetails() {
        List<Map<String, Object>> details = new ArrayList<>();
        List<PhieuThu> allPhieuThu = phieuThuService.getAllPhieuThu();

        List<PhieuThu> unpaidPhieuThu = allPhieuThu.stream()
                .filter(p -> {
                    String trangThai = p.getTrangThai();
                    return trangThai == null || 
                           (!trangThai.equalsIgnoreCase("Đã thu") && 
                            !trangThai.equalsIgnoreCase("Đã thanh toán"));
                })
                .collect(Collectors.toList());

        for (PhieuThu phieuThu : unpaidPhieuThu) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("maPhieu", phieuThu.getId());
            detail.put("ngayLap", phieuThu.getNgayLap());
            detail.put("tongTien", phieuThu.getTongTien());
            detail.put("trangThai", phieuThu.getTrangThai());

            // Lookup household name
            com.bluemoon.models.HoGiaDinh hoGiaDinh = hoGiaDinhService.findById(phieuThu.getMaHo());
            detail.put("tenHo", hoGiaDinh != null ? hoGiaDinh.getTenChuHo() : "N/A");

            // Lookup drive name
            com.bluemoon.models.DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
            detail.put("tenDot", dotThu != null ? dotThu.getTenDot() : "N/A");

            details.add(detail);
        }

        return details;
    }
}

