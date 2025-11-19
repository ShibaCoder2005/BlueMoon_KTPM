package com.bluemoon.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.services.KhoanThuService;

/**
 * Triển khai {@link KhoanThuService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class KhoanThuServiceImpl implements KhoanThuService {

    private final Map<Integer, KhoanThu> khoanThuStore = new ConcurrentHashMap<>();
    private int nextId = 1;

    public KhoanThuServiceImpl() {
        seedSampleData();
    }

    @Override
    public List<KhoanThu> getAllKhoanThu() {
        return khoanThuStore.values()
                .stream()
                .sorted(Comparator.comparingInt(KhoanThu::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addKhoanThu(KhoanThu khoanThu) {
        if (khoanThu == null) {
            return false;
        }
        if (khoanThu.getId() == 0) {
            khoanThu.setId(nextId++);
        }
        khoanThuStore.put(khoanThu.getId(), cloneEntity(khoanThu));
        return true;
    }

    @Override
    public boolean updateKhoanThu(KhoanThu khoanThu) {
        if (khoanThu == null || !khoanThuStore.containsKey(khoanThu.getId())) {
            return false;
        }
        khoanThuStore.put(khoanThu.getId(), cloneEntity(khoanThu));
        return true;
    }

    @Override
    public boolean deleteKhoanThu(int maKhoanThu) {
        return khoanThuStore.remove(maKhoanThu) != null;
    }

    private void seedSampleData() {
        List<KhoanThu> samples = new ArrayList<>();
        samples.add(new KhoanThu(1, "Phí quản lý", "Bắt buộc", new BigDecimal("50000"), "tháng", "hộ", true, "Phí quản lý hàng tháng"));
        samples.add(new KhoanThu(2, "Phí vệ sinh", "Bắt buộc", new BigDecimal("30000"), "tháng", "hộ", true, "Phí vệ sinh hàng tháng"));
        samples.add(new KhoanThu(3, "Đóng góp quỹ", "Đóng góp", new BigDecimal("100000"), "lần", "hộ", false, "Đóng góp tự nguyện"));
        nextId = 4;
        samples.forEach(item -> {
            // Set loaiKhoanThu based on batBuoc
            item.setLoaiKhoanThu(item.isBatBuoc() ? 0 : 1);
            khoanThuStore.put(item.getId(), item);
        });
    }

    private KhoanThu cloneEntity(KhoanThu source) {
        KhoanThu clone = new KhoanThu();
        clone.setId(source.getId());
        clone.setTenKhoan(source.getTenKhoan());
        clone.setLoai(source.getLoai());
        clone.setDonGia(source.getDonGia());
        clone.setDonViTinh(source.getDonViTinh());
        clone.setTinhTheo(source.getTinhTheo());
        clone.setBatBuoc(source.isBatBuoc());
        clone.setMoTa(source.getMoTa());
        clone.setLoaiKhoanThu(source.getLoaiKhoanThu());
        return clone;
    }
}

