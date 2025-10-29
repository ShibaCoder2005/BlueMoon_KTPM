package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.LichSuNopTien;

/**
 * Nghiệp vụ ghi nhận và truy vấn lịch sử nộp tiền.
 */
public class LichSuNopTienService {

    /** DAO truy cập LichSuNopTien. */
    private final Object lichSuNopTienDAO; // TODO: thay bằng LichSuNopTienDAO

    /** Khởi tạo với DAO. */
    public LichSuNopTienService(Object lichSuNopTienDAO) {
        this.lichSuNopTienDAO = lichSuNopTienDAO;
    }

    /**
     * Ghi nhận một lần nộp tiền.
     * @param paymentRecord bản ghi nộp tiền
     * @return true nếu thành công
     */
    public boolean addLichSuNopTien(LichSuNopTien paymentRecord) {
        // TODO: Implement logic - validate và insert
        return false;
    }

    /**
     * Lấy lịch sử nộp tiền theo mã phiếu thu.
     * @param maPhieu mã phiếu thu
     * @return danh sách lịch sử
     */
    public List<LichSuNopTien> getLichSuNopTienByPhieuThu(int maPhieu) {
        // TODO: Implement logic - truy vấn theo phiếu
        return null;
    }

    /**
     * Lấy lịch sử nộp tiền theo mã hộ.
     * @param maHo mã hộ gia đình
     * @return danh sách lịch sử
     */
    public List<LichSuNopTien> getLichSuNopTienByHoGiaDinh(int maHo) {
        // TODO: Implement logic - truy vấn theo hộ
        return null;
    }
}


