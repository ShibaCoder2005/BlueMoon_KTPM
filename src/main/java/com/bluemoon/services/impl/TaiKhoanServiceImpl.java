package com.bluemoon.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.TaiKhoanService;

/**
 * Triển khai {@link TaiKhoanService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class TaiKhoanServiceImpl implements TaiKhoanService {

    private final Map<Integer, TaiKhoan> taiKhoanStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> usernameToIdMap = new ConcurrentHashMap<>();
    private int nextId = 1;

    public TaiKhoanServiceImpl() {
        seedSampleData();
    }

    @Override
    public List<TaiKhoan> getAllTaiKhoan() {
        return taiKhoanStore.values()
                .stream()
                .sorted(Comparator.comparingInt(TaiKhoan::getId))
                .collect(Collectors.toList());
    }

    @Override
    public TaiKhoan findById(int id) {
        return taiKhoanStore.get(id);
    }

    @Override
    public TaiKhoan findByUsername(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return null;
        }
        Integer id = usernameToIdMap.get(tenDangNhap.trim().toLowerCase());
        if (id == null) {
            return null;
        }
        return taiKhoanStore.get(id);
    }

    @Override
    public boolean isUsernameExists(String tenDangNhap, int excludeId) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return false;
        }
        String usernameLower = tenDangNhap.trim().toLowerCase();
        Integer existingId = usernameToIdMap.get(usernameLower);
        return existingId != null && existingId != excludeId;
    }

    @Override
    public boolean addTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            return false;
        }
        // Check username uniqueness
        if (isUsernameExists(taiKhoan.getTenDangNhap(), 0)) {
            return false;
        }
        if (taiKhoan.getId() == 0) {
            taiKhoan.setId(nextId++);
        }
        taiKhoanStore.put(taiKhoan.getId(), cloneEntity(taiKhoan));
        usernameToIdMap.put(taiKhoan.getTenDangNhap().trim().toLowerCase(), taiKhoan.getId());
        return true;
    }

    @Override
    public boolean updateTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null || !taiKhoanStore.containsKey(taiKhoan.getId())) {
            return false;
        }
        // Check username uniqueness (exclude current account)
        if (isUsernameExists(taiKhoan.getTenDangNhap(), taiKhoan.getId())) {
            return false;
        }
        TaiKhoan oldAccount = taiKhoanStore.get(taiKhoan.getId());
        // Remove old username mapping if username changed
        if (!oldAccount.getTenDangNhap().equalsIgnoreCase(taiKhoan.getTenDangNhap())) {
            usernameToIdMap.remove(oldAccount.getTenDangNhap().trim().toLowerCase());
            usernameToIdMap.put(taiKhoan.getTenDangNhap().trim().toLowerCase(), taiKhoan.getId());
        }
        taiKhoanStore.put(taiKhoan.getId(), cloneEntity(taiKhoan));
        return true;
    }

    @Override
    public boolean updateStatus(int id, String trangThai) {
        TaiKhoan taiKhoan = taiKhoanStore.get(id);
        if (taiKhoan == null) {
            return false;
        }
        taiKhoan.setTrangThai(trangThai);
        return true;
    }

    @Override
    public boolean updatePassword(int id, String hashedPassword) {
        TaiKhoan taiKhoan = taiKhoanStore.get(id);
        if (taiKhoan == null) {
            return false;
        }
        taiKhoan.setMatKhau(hashedPassword);
        return true;
    }

    private void seedSampleData() {
        List<TaiKhoan> samples = new ArrayList<>();
        samples.add(new TaiKhoan(1, "admin", "admin123", "Quản trị hệ thống", "BanQuanLy", "0900000000", "Hoạt động"));
        samples.add(new TaiKhoan(2, "ketoan", "ketoan123", "Nguyễn Văn A", "KeToan", "0900000001", "Hoạt động"));
        samples.add(new TaiKhoan(3, "totruong", "totruong123", "Trần Thị B", "ToTruong", "0900000002", "Hoạt động"));
        nextId = 4;
        samples.forEach(item -> {
            taiKhoanStore.put(item.getId(), item);
            usernameToIdMap.put(item.getTenDangNhap().trim().toLowerCase(), item.getId());
        });
    }

    private TaiKhoan cloneEntity(TaiKhoan source) {
        TaiKhoan clone = new TaiKhoan();
        clone.setId(source.getId());
        clone.setTenDangNhap(source.getTenDangNhap());
        clone.setMatKhau(source.getMatKhau());
        clone.setHoTen(source.getHoTen());
        clone.setVaiTro(source.getVaiTro());
        clone.setDienThoai(source.getDienThoai());
        clone.setTrangThai(source.getTrangThai());
        return clone;
    }
}

