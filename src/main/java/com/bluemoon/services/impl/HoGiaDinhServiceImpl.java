package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.services.HoGiaDinhService;

/**
 * Triển khai {@link HoGiaDinhService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class HoGiaDinhServiceImpl implements HoGiaDinhService {

    private final Map<Integer, HoGiaDinh> hoGiaDinhStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> maHoToIdMap = new ConcurrentHashMap<>(); // Map maHo (String) to id
    private int nextId = 1;

    public HoGiaDinhServiceImpl() {
        seedSampleData();
    }

    @Override
    public List<HoGiaDinh> getAll() {
        return hoGiaDinhStore.values()
                .stream()
                .sorted(Comparator.comparingInt(HoGiaDinh::getId))
                .collect(Collectors.toList());
    }

    @Override
    public HoGiaDinh findById(int id) {
        return hoGiaDinhStore.get(id);
    }

    @Override
    public List<HoGiaDinh> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return hoGiaDinhStore.values()
                .stream()
                .filter(item -> containsIgnoreCase(item.getMaHo(), lowerKeyword)
                        || containsIgnoreCase(item.getGhiChu(), lowerKeyword)
                        || containsIgnoreCase(item.getTenChuHo(), lowerKeyword))
                .sorted(Comparator.comparingInt(HoGiaDinh::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean add(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh == null || hoGiaDinh.getMaHo() == null || hoGiaDinh.getMaHo().trim().isEmpty()) {
            return false;
        }
        // Check for duplicate maHo
        if (maHoToIdMap.containsKey(hoGiaDinh.getMaHo())) {
            return false;
        }
        if (hoGiaDinh.getId() == 0) {
            hoGiaDinh.setId(nextId++);
        }
        hoGiaDinhStore.put(hoGiaDinh.getId(), cloneEntity(hoGiaDinh));
        maHoToIdMap.put(hoGiaDinh.getMaHo(), hoGiaDinh.getId());
        return true;
    }

    @Override
    public boolean update(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh == null || !hoGiaDinhStore.containsKey(hoGiaDinh.getId())) {
            return false;
        }
        HoGiaDinh existing = hoGiaDinhStore.get(hoGiaDinh.getId());
        // If maHo changed, check for duplicate
        if (!existing.getMaHo().equals(hoGiaDinh.getMaHo())) {
            if (maHoToIdMap.containsKey(hoGiaDinh.getMaHo())) {
                return false;
            }
            maHoToIdMap.remove(existing.getMaHo());
            maHoToIdMap.put(hoGiaDinh.getMaHo(), hoGiaDinh.getId());
        }
        hoGiaDinhStore.put(hoGiaDinh.getId(), cloneEntity(hoGiaDinh));
        return true;
    }

    @Override
    public boolean delete(int id) {
        HoGiaDinh removed = hoGiaDinhStore.remove(id);
        if (removed != null) {
            maHoToIdMap.remove(removed.getMaHo());
            return true;
        }
        return false;
    }

    private void seedSampleData() {
        List<HoGiaDinh> samples = new ArrayList<>();
        samples.add(new HoGiaDinh(1, "H001", 101, new BigDecimal("45.50"), 1, "Hộ gia đình 1", LocalDate.now()));
        samples.add(new HoGiaDinh(2, "H002", 102, new BigDecimal("60.00"), 3, "Hộ gia đình 2", LocalDate.now()));
        samples.add(new HoGiaDinh(3, "H003", 201, new BigDecimal("35.75"), 2, "Hộ gia đình 3", LocalDate.now()));
        nextId = 4;
        samples.forEach(item -> {
            hoGiaDinhStore.put(item.getId(), item);
            maHoToIdMap.put(item.getMaHo(), item.getId());
        });
    }

    private boolean containsIgnoreCase(String target, String keywordLower) {
        return target != null && target.toLowerCase().contains(keywordLower);
    }

    private HoGiaDinh cloneEntity(HoGiaDinh source) {
        return new HoGiaDinh(
                source.getId(),
                source.getMaHo(),
                source.getSoPhong(),
                source.getDienTich(),
                source.getMaChuHo(),
                source.getGhiChu(),
                source.getNgayTao()
        );
    }
}

