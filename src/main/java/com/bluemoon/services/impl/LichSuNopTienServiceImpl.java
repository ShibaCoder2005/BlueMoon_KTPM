package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bluemoon.models.LichSuNopTien;
import com.bluemoon.services.LichSuNopTienService;
import com.bluemoon.services.PhieuThuService;

/**
 * Triển khai {@link LichSuNopTienService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class LichSuNopTienServiceImpl implements LichSuNopTienService {

    private static final Logger logger = Logger.getLogger(LichSuNopTienServiceImpl.class.getName());
    
    private final Map<Integer, LichSuNopTien> lichSuStore = new ConcurrentHashMap<>();
    private final PhieuThuService phieuThuService;
    private int nextId = 1;

    public LichSuNopTienServiceImpl() {
        this.phieuThuService = new PhieuThuServiceImpl();
        seedSampleData();
    }

    /**
     * Constructor với dependency injection (cho testing hoặc future DI framework).
     */
    public LichSuNopTienServiceImpl(PhieuThuService phieuThuService) {
        this.phieuThuService = phieuThuService;
        seedSampleData();
    }

    @Override
    public List<LichSuNopTien> getAllLichSuNopTien() {
        return lichSuStore.values()
                .stream()
                .sorted(Comparator.comparing(LichSuNopTien::getNgayNop).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean addLichSuNopTien(LichSuNopTien paymentRecord) {
        if (paymentRecord == null) {
            return false;
        }
        if (paymentRecord.getId() == 0) {
            paymentRecord.setId(nextId++);
        }
        lichSuStore.put(paymentRecord.getId(), cloneEntity(paymentRecord));
        return true;
    }

    @Override
    public List<LichSuNopTien> getLichSuNopTienByPhieuThu(int maPhieu) {
        return lichSuStore.values()
                .stream()
                .filter(ls -> ls.getMaPhieu() == maPhieu)
                .sorted(Comparator.comparing(LichSuNopTien::getNgayNop).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuNopTien> getLichSuNopTienByHoGiaDinh(int maHo) {
        // TODO: Implement when relationship is available
        // For now, return empty list
        return new ArrayList<>();
    }

    @Override
    public boolean recordPaymentWithStatusUpdate(LichSuNopTien paymentRecord, String updateStatusTo) {
        if (paymentRecord == null || paymentRecord.getMaPhieu() <= 0) {
            logger.log(Level.WARNING, "Invalid payment record: paymentRecord is null or maPhieu is invalid");
            return false;
        }

        if (updateStatusTo == null || updateStatusTo.trim().isEmpty()) {
            logger.log(Level.WARNING, "Invalid status: updateStatusTo is null or empty");
            return false;
        }

        try {
            // Step 1: Record the payment
            boolean paymentSuccess = addLichSuNopTien(paymentRecord);
            if (!paymentSuccess) {
                logger.log(Level.WARNING, "Failed to record payment for maPhieu: " + paymentRecord.getMaPhieu());
                return false;
            }

            // Step 2: Update receipt status (atomic operation)
            boolean statusSuccess = phieuThuService.updatePhieuThuStatus(paymentRecord.getMaPhieu(), updateStatusTo);
            if (!statusSuccess) {
                logger.log(Level.WARNING, 
                        "Payment recorded but failed to update receipt status for maPhieu: " + paymentRecord.getMaPhieu());
                // Note: In a real database transaction, we would rollback here
                // For in-memory implementation, we log the warning but payment is already recorded
                return false;
            }

            logger.log(Level.INFO, 
                    "Successfully recorded payment and updated status for maPhieu: " + paymentRecord.getMaPhieu());
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, 
                    "Error in recordPaymentWithStatusUpdate for maPhieu: " + paymentRecord.getMaPhieu(), e);
            return false;
        }
    }

    private void seedSampleData() {
        List<LichSuNopTien> samples = new ArrayList<>();
        samples.add(new LichSuNopTien(1, 1, LocalDateTime.now().minusDays(5),
                new BigDecimal("500000"), "Tiền mặt", 1));
        samples.add(new LichSuNopTien(2, 1, LocalDateTime.now().minusDays(3),
                new BigDecimal("300000"), "Chuyển khoản", 1));
        samples.add(new LichSuNopTien(3, 2, LocalDateTime.now().minusDays(2),
                new BigDecimal("800000"), "Tiền mặt", 1));
        nextId = 4;
        samples.forEach(item -> lichSuStore.put(item.getId(), item));
    }

    private LichSuNopTien cloneEntity(LichSuNopTien source) {
        return new LichSuNopTien(
                source.getId(),
                source.getMaPhieu(),
                source.getNgayNop(),
                source.getSoTien(),
                source.getPhuongThuc(),
                source.getNguoiThu()
        );
    }
}

