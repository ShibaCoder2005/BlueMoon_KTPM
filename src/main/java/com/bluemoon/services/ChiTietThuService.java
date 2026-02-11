package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.ChiTietThu;

/**
 * Định nghĩa các nghiệp vụ quản lý Chi tiết Thu (Invoice Details).
 */
public interface ChiTietThuService {

    /**
     * Lấy danh sách chi tiết thu của một phiếu thu.
     * Kết quả bao gồm tên khoản thu (tenKhoan) từ bảng KhoanThu thông qua JOIN.
     *
     * @param maPhieu mã phiếu thu
     * @return danh sách chi tiết thu
     */
    List<ChiTietThu> getChiTietByMaPhieu(int maPhieu);

    /**
     * Lưu một chi tiết thu.
     *
     * @param chiTiet đối tượng chi tiết thu cần lưu
     * @return true nếu thành công
     */
    boolean save(ChiTietThu chiTiet);

    /**
     * Lưu nhiều chi tiết thu cùng lúc (batch insert).
     * Sử dụng batch processing để tối ưu hiệu suất.
     *
     * @param listChiTiet danh sách chi tiết thu cần lưu
     * @return true nếu tất cả đều thành công
     */
    boolean saveAll(List<ChiTietThu> listChiTiet);

    /**
     * Xóa tất cả chi tiết thu của một phiếu thu.
     * Thường được dùng để xóa chi tiết cũ trước khi cập nhật phiếu thu.
     *
     * @param maPhieu mã phiếu thu
     * @return true nếu thành công
     */
    boolean deleteByMaPhieu(int maPhieu);

    /**
     * Xóa một chi tiết thu theo ID.
     * @param id ID của chi tiết thu cần xóa
     * @return true nếu xóa thành công
     */
    boolean delete(int id);
}

