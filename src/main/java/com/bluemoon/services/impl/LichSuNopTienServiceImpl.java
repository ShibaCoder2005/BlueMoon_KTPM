package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.bluemoon.models.LichSuNopTien;
import com.bluemoon.services.LichSuNopTienService;

/**
 * Triển khai {@link LichSuNopTienService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class LichSuNopTienServiceImpl implements LichSuNopTienService {

    private final Map<Integer, LichSuNopTien> lichSuStore = new ConcurrentHashMap<>();
    private int nextId = 1;

    public LichSuNopTienServiceImpl() {
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

