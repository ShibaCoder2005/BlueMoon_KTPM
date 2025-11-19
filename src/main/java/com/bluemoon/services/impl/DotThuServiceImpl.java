package com.bluemoon.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.bluemoon.models.DotThu;
import com.bluemoon.services.DotThuService;

/**
 * Triển khai {@link DotThuService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class DotThuServiceImpl implements DotThuService {

    private final Map<Integer, DotThu> dotThuStore = new ConcurrentHashMap<>();
    private int nextId = 1;

    public DotThuServiceImpl() {
        seedSampleData();
    }

    @Override
    public List<DotThu> getAllDotThu() {
        return dotThuStore.values()
                .stream()
                .sorted(Comparator.comparingInt(DotThu::getId))
                .collect(Collectors.toList());
    }

    @Override
    public DotThu getDotThuById(int id) {
        return dotThuStore.get(id);
    }

    @Override
    public boolean addDotThu(DotThu dotThu) {
        if (dotThu == null) {
            return false;
        }
        if (dotThu.getId() == 0) {
            dotThu.setId(nextId++);
        }
        dotThuStore.put(dotThu.getId(), cloneEntity(dotThu));
        return true;
    }

    @Override
    public boolean updateDotThu(DotThu dotThu) {
        if (dotThu == null || !dotThuStore.containsKey(dotThu.getId())) {
            return false;
        }
        dotThuStore.put(dotThu.getId(), cloneEntity(dotThu));
        return true;
    }

    @Override
    public boolean deleteDotThu(int id) {
        return dotThuStore.remove(id) != null;
    }

    private void seedSampleData() {
        List<DotThu> samples = new ArrayList<>();
        samples.add(new DotThu(1, "Đợt thu tháng 1/2024", 
                LocalDate.of(2024, 1, 1), 
                LocalDate.of(2024, 1, 31), 
                "Đã kết thúc"));
        samples.add(new DotThu(2, "Đợt thu tháng 2/2024", 
                LocalDate.of(2024, 2, 1), 
                LocalDate.of(2024, 2, 29), 
                "Đã kết thúc"));
        samples.add(new DotThu(3, "Đợt thu tháng 3/2024", 
                LocalDate.of(2024, 3, 1), 
                LocalDate.of(2024, 3, 31), 
                "Đang thu"));
        nextId = 4;
        samples.forEach(item -> dotThuStore.put(item.getId(), item));
    }

    private DotThu cloneEntity(DotThu source) {
        return new DotThu(
                source.getId(),
                source.getTenDot(),
                source.getNgayBatDau(),
                source.getNgayKetThuc(),
                source.getTrangThai(),
                source.getMoTa()
        );
    }
}

