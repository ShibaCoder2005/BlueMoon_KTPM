package com.bluemoon.services;

import com.bluemoon.models.TaiKhoan;

/**
 * Định nghĩa các nghiệp vụ xác thực người dùng.
 */
public interface AuthService {

    /**
     * Xác thực thông tin đăng nhập.
     *
     * @param username tên đăng nhập
     * @param password mật khẩu
     * @return {@link TaiKhoan} nếu thông tin hợp lệ, ngược lại {@code null}
     */
    TaiKhoan authenticate(String username, String password);
}