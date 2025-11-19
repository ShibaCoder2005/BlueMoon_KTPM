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
     *
     * @param maKhoanThu mã cần xóa
     * @return true nếu thành công
     */
    boolean deleteKhoanThu(int maKhoanThu);
}


