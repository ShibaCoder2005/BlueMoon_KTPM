package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.KhoanThu;

/**
 * Định nghĩa các nghiệp vụ quản lý danh mục KhoanThu.
 */
public interface KhoanThuService {

    /**
     * Lấy toàn bộ danh sách KhoanThu.
     *
     * @return danh sách KhoanThu
     */
    List<KhoanThu> getAllKhoanThu();

    /**
     * Thêm mới một KhoanThu.
     *
     * @param khoanThu đối tượng cần thêm
     * @return true nếu thành công
     */
    boolean addKhoanThu(KhoanThu khoanThu);

    /**
     * Cập nhật một KhoanThu.
     *
     * @param khoanThu đối tượng cần cập nhật
     * @return true nếu thành công
     */
    boolean updateKhoanThu(KhoanThu khoanThu);

    /**
     * Xóa một KhoanThu theo mã.
     * Kiểm tra ràng buộc: không được xóa nếu khoản thu đã được sử dụng trong bất kỳ ChiTietThu nào.
     * Sử dụng PhieuThuService.isFeeUsed() để kiểm tra trước khi xóa.
     *
     * @param maKhoanThu mã cần xóa
     * @return true nếu thành công, false nếu có ràng buộc (đã được sử dụng) hoặc lỗi
     */
    boolean deleteKhoanThu(int maKhoanThu);
}


