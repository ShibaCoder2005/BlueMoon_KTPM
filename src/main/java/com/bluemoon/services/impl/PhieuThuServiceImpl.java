package com.bluemoon.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.models.PhieuThu;
import com.bluemoon.services.PhieuThuService;

/**
 * Triển khai {@link PhieuThuService} bằng bộ nhớ tạm thời.
 * Có thể thay thế bằng DAO khi kết nối cơ sở dữ liệu.
 */
public class PhieuThuServiceImpl implements PhieuThuService {

    private static final Logger logger = Logger.getLogger(PhieuThuServiceImpl.class.getName());
    
    private final List<PhieuThu> phieuThuStore = new ArrayList<>();
    private final Map<Integer, List<ChiTietThu>> chiTietStore = new ConcurrentHashMap<>();
    private int nextId = 1;
    private int nextChiTietId = 1;

    @Override
    public int createPhieuThu(PhieuThu phieuThu) {
        if (phieuThu == null) {
            return -1;
        }
        phieuThu.setId(nextId++);
        phieuThuStore.add(clonePhieuThu(phieuThu));
        return phieuThu.getId();
    }

    @Override
    public boolean addChiTietThu(ChiTietThu chiTiet) {
        if (chiTiet == null) {
            return false;
        }
        if (chiTiet.getId() == 0) {
            chiTiet.setId(nextChiTietId++);
        }
        chiTietStore.computeIfAbsent(chiTiet.getMaPhieu(), k -> new ArrayList<>())
                .add(cloneChiTietThu(chiTiet));
        return true;
    }

    @Override
    public int createPhieuThuWithDetails(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
        if (phieuThu == null) {
            return -1;
        }
        // Create PhieuThu first
        int maPhieu = createPhieuThu(phieuThu);
        if (maPhieu == -1) {
            return -1;
        }
        // Add all ChiTietThu
        if (chiTietList != null && !chiTietList.isEmpty()) {
            for (ChiTietThu chiTiet : chiTietList) {
                chiTiet.setMaPhieu(maPhieu);
                addChiTietThu(chiTiet);
            }
        }
        return maPhieu;
    }

    @Override
    public List<ChiTietThu> getChiTietThuByPhieu(int maPhieu) {
        List<ChiTietThu> chiTietList = chiTietStore.get(maPhieu);
        if (chiTietList == null) {
            return new ArrayList<>();
        }
        return chiTietList.stream()
                .map(this::cloneChiTietThu)
                .collect(Collectors.toList());
    }

    @Override
    public PhieuThu getPhieuThuWithDetails(int maPhieu) {
        return phieuThuStore.stream()
                .filter(p -> p.getId() == maPhieu)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<PhieuThu> findPhieuThuByHoGiaDinh(int maHo) {
        return phieuThuStore.stream()
                .filter(p -> p.getMaHo() == maHo)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<PhieuThu> findPhieuThuByDotThu(int maDotThu) {
        return phieuThuStore.stream()
                .filter(p -> p.getMaDot() == maDotThu)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<PhieuThu> getAllPhieuThu() {
        return phieuThuStore.stream()
                .map(this::clonePhieuThu)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean updatePhieuThuStatus(int maPhieu, String newStatus) {
        PhieuThu phieuThu = getPhieuThuWithDetails(maPhieu);
        if (phieuThu != null) {
            phieuThu.setTrangThai(newStatus);
            return true;
        }
        return false;
    }

    @Override
    public int generateReceiptsForDrive(int maDot) {
        // Giả lập: Tạo phiếu thu cho tất cả hộ gia đình
        // Trong thực tế, cần lấy danh sách hộ gia đình từ HoGiaDinhService
        // và tạo PhieuThu cho mỗi hộ
        
        // TODO: Integrate with HoGiaDinhService to get all households
        // For now, return a mock count
        int count = 0;
        // This is a placeholder - actual implementation would:
        // 1. Get all households from HoGiaDinhService
        // 2. For each household, create a PhieuThu with maDot
        // 3. Calculate total amount based on KhoanThu and household members
        // 4. Save each PhieuThu
        
        return count;
    }

    @Override
    public boolean hasUnpaidFees(int maHo) {
        // Kiểm tra xem có phiếu thu nào chưa thanh toán cho hộ này không
        // Một phiếu thu được coi là chưa thanh toán nếu:
        // - Trạng thái là "Chưa thanh toán" hoặc "Chưa đủ"
        // - Hoặc tổng tiền > số tiền đã nộp
        
        List<PhieuThu> phieuThuList = findPhieuThuByHoGiaDinh(maHo);
        for (PhieuThu phieuThu : phieuThuList) {
            // Giả sử trạng thái "Chưa thanh toán" hoặc "Chưa đủ" nghĩa là còn nợ
            String trangThai = phieuThu.getTrangThai();
            if (trangThai != null && 
                (trangThai.equalsIgnoreCase("Chưa thanh toán") || 
                 trangThai.equalsIgnoreCase("Chưa đủ") ||
                 trangThai.equalsIgnoreCase("Nợ"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFeeUsed(int maKhoanThu) {
        // Kiểm tra xem khoản thu có đang được sử dụng trong bất kỳ ChiTietThu nào không
        return chiTietStore.values()
                .stream()
                .flatMap(List::stream)
                .anyMatch(ct -> ct.getMaKhoan() == maKhoanThu);
    }

    @Override
    public boolean canModifyPhieuThu(int maPhieu) {
        PhieuThu phieuThu = getPhieuThuWithDetails(maPhieu);
        if (phieuThu == null) {
            return false;
        }
        
        String trangThai = phieuThu.getTrangThai();
        if (trangThai == null) {
            return true; // No status means can modify
        }
        
        // Cannot modify if already paid
        String trangThaiLower = trangThai.toLowerCase();
        return !trangThaiLower.contains("đã thu") && 
               !trangThaiLower.contains("đã thanh toán") &&
               !trangThaiLower.contains("hoàn thành");
    }

    @Override
    public boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
        if (phieuThu == null || phieuThu.getId() == 0) {
            logger.log(Level.WARNING, "Invalid PhieuThu: null or id is 0");
            return false;
        }

        // Check if receipt can be modified
        if (!canModifyPhieuThu(phieuThu.getId())) {
            logger.log(Level.WARNING, 
                    "Cannot update PhieuThu with id: " + phieuThu.getId() + " - already paid");
            return false;
        }

        try {
            // Find and update PhieuThu
            PhieuThu existing = getPhieuThuWithDetails(phieuThu.getId());
            if (existing == null) {
                logger.log(Level.WARNING, "PhieuThu not found with id: " + phieuThu.getId());
                return false;
            }

            // Update PhieuThu fields
            existing.setMaHo(phieuThu.getMaHo());
            existing.setMaDot(phieuThu.getMaDot());
            existing.setMaTaiKhoan(phieuThu.getMaTaiKhoan());
            existing.setNgayLap(phieuThu.getNgayLap());
            existing.setTongTien(phieuThu.getTongTien());
            existing.setTrangThai(phieuThu.getTrangThai());
            existing.setHinhThucThu(phieuThu.getHinhThucThu());

            // Update ChiTietThu if provided
            if (chiTietList != null) {
                // Remove old details
                chiTietStore.remove(phieuThu.getId());
                
                // Add new details
                if (!chiTietList.isEmpty()) {
                    List<ChiTietThu> newChiTietList = new ArrayList<>();
                    for (ChiTietThu chiTiet : chiTietList) {
                        chiTiet.setMaPhieu(phieuThu.getId());
                        if (chiTiet.getId() == 0) {
                            chiTiet.setId(nextChiTietId++);
                        }
                        newChiTietList.add(cloneChiTietThu(chiTiet));
                    }
                    chiTietStore.put(phieuThu.getId(), newChiTietList);
                }
            }

            logger.log(Level.INFO, "Successfully updated PhieuThu with id: " + phieuThu.getId());
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating PhieuThu with id: " + phieuThu.getId(), e);
            return false;
        }
    }

    @Override
    public boolean deletePhieuThu(int maPhieu) {
        // Check if receipt can be modified (deleted)
        if (!canModifyPhieuThu(maPhieu)) {
            logger.log(Level.WARNING, 
                    "Cannot delete PhieuThu with id: " + maPhieu + " - already paid");
            return false;
        }

        try {
            // Remove ChiTietThu first
            chiTietStore.remove(maPhieu);
            
            // Remove PhieuThu
            boolean removed = phieuThuStore.removeIf(p -> p.getId() == maPhieu);
            
            if (removed) {
                logger.log(Level.INFO, "Successfully deleted PhieuThu with id: " + maPhieu);
            } else {
                logger.log(Level.WARNING, "PhieuThu not found with id: " + maPhieu);
            }
            
            return removed;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting PhieuThu with id: " + maPhieu, e);
            return false;
        }
    }

    private PhieuThu clonePhieuThu(PhieuThu source) {
        PhieuThu clone = new PhieuThu();
        clone.setId(source.getId());
        clone.setMaHo(source.getMaHo());
        clone.setMaDot(source.getMaDot());
        clone.setMaTaiKhoan(source.getMaTaiKhoan());
        clone.setNgayLap(source.getNgayLap());
        clone.setTongTien(source.getTongTien());
        clone.setTrangThai(source.getTrangThai());
        clone.setHinhThucThu(source.getHinhThucThu());
        return clone;
    }

    private ChiTietThu cloneChiTietThu(ChiTietThu source) {
        ChiTietThu clone = new ChiTietThu();
        clone.setId(source.getId());
        clone.setMaPhieu(source.getMaPhieu());
        clone.setMaKhoan(source.getMaKhoan());
        clone.setSoLuong(source.getSoLuong());
        clone.setDonGia(source.getDonGia());
        clone.setThanhTien(source.getThanhTien());
        clone.setTenKhoan(source.getTenKhoan());
        return clone;
    }
}

