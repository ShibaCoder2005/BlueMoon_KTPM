package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.TaiKhoan;

/**
 * Định nghĩa các nghiệp vụ quản lý tài khoản người dùng.
 */
public interface TaiKhoanService {

    /**
     * Lấy toàn bộ danh sách tài khoản.
     *
     * @return danh sách tài khoản
     */
    List<TaiKhoan> getAllTaiKhoan();

    /**
     * Tìm tài khoản theo ID.
     *
     * @param id ID tài khoản
     * @return tài khoản nếu tồn tại, null nếu không
     */
    TaiKhoan findById(int id);

    /**
     * Tìm tài khoản theo tên đăng nhập.
     *
     * @param tenDangNhap tên đăng nhập
     * @return tài khoản nếu tồn tại, null nếu không
     */
    TaiKhoan findByUsername(String tenDangNhap);

    /**
     * Kiểm tra xem tên đăng nhập đã tồn tại chưa.
     *
     * @param tenDangNhap tên đăng nhập
     * @param excludeId   ID tài khoản cần loại trừ (khi update), 0 nếu không loại trừ
     * @return true nếu đã tồn tại, false nếu không
     */
    boolean isUsernameExists(String tenDangNhap, int excludeId);

    /**
     * Thêm mới một tài khoản.
     *
     * @param taiKhoan đối tượng tài khoản
     * @return true nếu thành công
     */
    boolean addTaiKhoan(TaiKhoan taiKhoan);

    /**
     * Cập nhật thông tin tài khoản.
     *
     * @param taiKhoan đối tượng tài khoản
     * @return true nếu thành công
     */
    boolean updateTaiKhoan(TaiKhoan taiKhoan);

    /**
     * Cập nhật trạng thái tài khoản.
     *
     * @param id        ID tài khoản
     * @param trangThai trạng thái mới
     * @return true nếu thành công
     */
    boolean updateStatus(int id, String trangThai);

    /**
     * Cập nhật mật khẩu tài khoản.
     *
     * @param id           ID tài khoản
     * @param hashedPassword mật khẩu đã hash
     * @return true nếu thành công
     */
    boolean updatePassword(int id, String hashedPassword);
}

