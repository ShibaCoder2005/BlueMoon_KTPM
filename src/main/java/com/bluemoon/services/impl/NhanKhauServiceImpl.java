package com.bluemoon.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bluemoon.models.LichSuNhanKhau;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.NhanKhauService;

/**
 * Triển khai {@link NhanKhauService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class NhanKhauServiceImpl implements NhanKhauService {

    private static final Logger logger = Logger.getLogger(NhanKhauServiceImpl.class.getName());
    
    private final Map<Integer, NhanKhau> nhanKhauStore = new ConcurrentHashMap<>();
    private final Map<Integer, LichSuNhanKhau> lichSuStore = new ConcurrentHashMap<>();
    private int nextId = 1;
    private int nextLichSuId = 1;

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
    public boolean addLichSuNhanKhau(LichSuNhanKhau history) {
        if (history == null) {
            return false;
        }
        if (history.getId() == 0) {
            history.setId(nextLichSuId++);
        }
        lichSuStore.put(history.getId(), cloneLichSu(history));
        return true;
    }

    @Override
    public List<LichSuNhanKhau> getLichSuNhanKhau(int maNhanKhau) {
        return lichSuStore.values()
                .stream()
                .filter(ls -> ls.getMaNhanKhau() == maNhanKhau)
                .sorted(Comparator.comparing(LichSuNhanKhau::getNgayBatDau).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateStatusWithHistory(int nhanKhauId, String newStatus, LichSuNhanKhau historyRecord) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            logger.log(Level.WARNING, "Invalid status: newStatus is null or empty");
            return false;
        }

        // Step 1: Get the resident
        NhanKhau nhanKhau = nhanKhauStore.get(nhanKhauId);
        if (nhanKhau == null) {
            logger.log(Level.WARNING, "Resident not found with id: " + nhanKhauId);
            return false;
        }

        try {
            // Step 2: Update the status
            String oldStatus = nhanKhau.getTinhTrang();
            nhanKhau.setTinhTrang(newStatus.trim());
            nhanKhauStore.put(nhanKhauId, cloneEntity(nhanKhau));

            // Step 3: Record history if provided
            if (historyRecord != null) {
                historyRecord.setMaNhanKhau(nhanKhauId);
                // Set default values if not provided
                if (historyRecord.getNgayBatDau() == null) {
                    historyRecord.setNgayBatDau(LocalDate.now());
                }
                if (historyRecord.getLoaiBienDong() == null || historyRecord.getLoaiBienDong().trim().isEmpty()) {
                    // Auto-generate loaiBienDong from status change
                    historyRecord.setLoaiBienDong("Thay đổi từ " + oldStatus + " sang " + newStatus);
                }
                
                boolean historySuccess = addLichSuNhanKhau(historyRecord);
                if (!historySuccess) {
                    logger.log(Level.WARNING, 
                            "Status updated but failed to record history for resident id: " + nhanKhauId);
                    // Status is already updated, so we return true but log the warning
                }
            }

            logger.log(Level.INFO, 
                    "Successfully updated status from '" + oldStatus + "' to '" + newStatus + 
                    "' for resident id: " + nhanKhauId);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, 
                    "Error in updateStatusWithHistory for resident id: " + nhanKhauId, e);
            return false;
        }
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

    private LichSuNhanKhau cloneLichSu(LichSuNhanKhau source) {
        return new LichSuNhanKhau(
                source.getId(),
                source.getMaNhanKhau(),
                source.getLoaiBienDong(),
                source.getNgayBatDau(),
                source.getNgayKetThuc(),
                source.getNguoiGhi()
        );
    }
}

