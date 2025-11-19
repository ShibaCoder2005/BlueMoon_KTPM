package com.bluemoon.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.NhanKhauService;

/**
 * Triển khai {@link NhanKhauService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class NhanKhauServiceImpl implements NhanKhauService {

    private final Map<Integer, NhanKhau> nhanKhauStore = new ConcurrentHashMap<>();
    private int nextId = 1;

    public NhanKhauServiceImpl() {
        seedSampleData();
    }

    @Override
    public List<NhanKhau> getAll() {
        return nhanKhauStore.values()
                .stream()
                .sorted(Comparator.comparingInt(NhanKhau::getId))
                .collect(Collectors.toList());
    }

    @Override
    public NhanKhau findById(int id) {
        return nhanKhauStore.get(id);
    }

    @Override
    public List<NhanKhau> getNhanKhauByHoGiaDinh(int maHo) {
        return nhanKhauStore.values()
                .stream()
                .filter(nk -> nk.getMaHo() == maHo)
                .sorted(Comparator.comparingInt(NhanKhau::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addNhanKhau(NhanKhau nhanKhau) {
        if (nhanKhau == null) {
            return false;
        }
        // Kiểm tra CCCD unique
        if (nhanKhau.getSoCCCD() != null && !nhanKhau.getSoCCCD().trim().isEmpty()) {
            if (isCCCDExists(nhanKhau.getSoCCCD(), 0)) {
                return false;
            }
        }
        if (nhanKhau.getId() == 0) {
            nhanKhau.setId(nextId++);
        }
        nhanKhauStore.put(nhanKhau.getId(), cloneEntity(nhanKhau));
        return true;
    }

    @Override
    public boolean updateNhanKhau(NhanKhau nhanKhau) {
        if (nhanKhau == null || !nhanKhauStore.containsKey(nhanKhau.getId())) {
            return false;
        }
        // Kiểm tra CCCD unique (loại trừ chính nó)
        if (nhanKhau.getSoCCCD() != null && !nhanKhau.getSoCCCD().trim().isEmpty()) {
            if (isCCCDExists(nhanKhau.getSoCCCD(), nhanKhau.getId())) {
                return false;
            }
        }
        nhanKhauStore.put(nhanKhau.getId(), cloneEntity(nhanKhau));
        return true;
    }

    @Override
    public boolean addLichSuNhanKhau(com.bluemoon.models.LichSuNhanKhau history) {
        // TODO: Implement when needed
        return false;
    }

    @Override
    public List<com.bluemoon.models.LichSuNhanKhau> getLichSuNhanKhau(int maNhanKhau) {
        // TODO: Implement when needed
        return new ArrayList<>();
    }

    @Override
    public boolean deleteNhanKhau(int id) {
        return nhanKhauStore.remove(id) != null;
    }

    @Override
    public boolean isCCCDExists(String soCCCD, int excludeId) {
        if (soCCCD == null || soCCCD.trim().isEmpty()) {
            return false;
        }
        String cccdTrimmed = soCCCD.trim();
        return nhanKhauStore.values()
                .stream()
                .filter(nk -> nk.getId() != excludeId)
                .anyMatch(nk -> cccdTrimmed.equalsIgnoreCase(nk.getSoCCCD()));
    }

    private void seedSampleData() {
        List<NhanKhau> samples = new ArrayList<>();
        samples.add(new NhanKhau(1, 1, "Nguyễn Văn A", LocalDate.of(1980, 1, 15),
                "Nam", "001234567890", "Kỹ sư", "Chủ hộ", "Thường trú"));
        samples.add(new NhanKhau(2, 1, "Nguyễn Thị B", LocalDate.of(1985, 5, 20),
                "Nữ", "001234567891", "Giáo viên", "Vợ", "Thường trú"));
        samples.add(new NhanKhau(3, 2, "Trần Văn C", LocalDate.of(1975, 3, 10),
                "Nam", "001234567892", "Bác sĩ", "Chủ hộ", "Thường trú"));
        nextId = 4;
        samples.forEach(item -> nhanKhauStore.put(item.getId(), item));
    }

    private NhanKhau cloneEntity(NhanKhau source) {
        return new NhanKhau(
                source.getId(),
                source.getMaHo(),
                source.getHoTen(),
                source.getNgaySinh(),
                source.getGioiTinh(),
                source.getSoCCCD(),
                source.getNgheNghiep(),
                source.getQuanHeVoiChuHo(),
                source.getTinhTrang()
        );
    }
}

