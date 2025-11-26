package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.LichSuNopTien;

/**
 * Định nghĩa các nghiệp vụ ghi nhận và truy vấn lịch sử nộp tiền.
 */
public interface LichSuNopTienService {

    /**
     * Lấy toàn bộ lịch sử nộp tiền.
     *
     * @return danh sách lịch sử nộp tiền
     */
    List<LichSuNopTien> getAllLichSuNopTien();

    /**
     * Ghi nhận một lần nộp tiền.
     *
     * @param paymentRecord bản ghi nộp tiền
     * @return true nếu thành công
     */
    boolean addLichSuNopTien(LichSuNopTien paymentRecord);

    /**
     * Lấy lịch sử nộp tiền theo mã phiếu thu.
     *
     * @param maPhieu mã phiếu thu
     * @return danh sách lịch sử
     */
    List<LichSuNopTien> getLichSuNopTienByPhieuThu(int maPhieu);

    /**
     * Lấy lịch sử nộp tiền theo mã hộ.
     *
     * @param maHo mã hộ gia đình
     * @return danh sách lịch sử
     */
    List<LichSuNopTien> getLichSuNopTienByHoGiaDinh(int maHo);

    /**
     * Ghi nhận thanh toán và tự động cập nhật trạng thái phiếu thu trong một transaction.
     * Method này đảm bảo tính nhất quán dữ liệu: ghi nhận thanh toán và cập nhật trạng thái phiếu thu
     * được thực hiện cùng lúc (atomic operation).
     * 
     * @param paymentRecord bản ghi nộp tiền (phải có maPhieu hợp lệ)
     * @param updateStatusTo trạng thái mới cho phiếu thu (e.g., "Đã thu", "Đã thanh toán")
     * @return true nếu thành công, false nếu thất bại
     */
    boolean recordPaymentWithStatusUpdate(LichSuNopTien paymentRecord, String updateStatusTo);
}


