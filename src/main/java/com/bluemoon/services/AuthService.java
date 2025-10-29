package com.bluemoon.services;

import com.bluemoon.models.TaiKhoan;

/**
 * Cung cấp logic xác thực người dùng dựa trên thông tin tài khoản.
 */
public class AuthService {

    /** DAO truy cập dữ liệu tài khoản. */
    private final Object taiKhoanDAO; // TODO: thay bằng TaiKhoanDAO

    /**
     * Khởi tạo với dependency DAO (DI qua constructor).
     * @param taiKhoanDAO đối tượng DAO làm việc với bảng TaiKhoan
     */
    public AuthService(Object taiKhoanDAO) {
        this.taiKhoanDAO = taiKhoanDAO;
    }

    /**
     * Xác thực thông tin đăng nhập.
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @return đối tượng TaiKhoan nếu hợp lệ; null nếu thất bại
     */
    public TaiKhoan authenticate(String username, String password) {
        // TODO: Implement logic - tra cứu TaiKhoan theo username, kiểm tra password
        return null;
    }
}


