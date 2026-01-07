package com.bluemoon.services;

import com.bluemoon.models.TaiKhoan;

/**
 * Định nghĩa các nghiệp vụ xác thực người dùng.
 */
public interface AuthService {

    /**
     * Xác thực thông tin đăng nhập (Login).
     *
     * @param username tên đăng nhập
     * @param password mật khẩu (plain text)
     * @return {@link TaiKhoan} nếu thông tin hợp lệ, ngược lại {@code null}
     */
    TaiKhoan login(String username, String password);

    /**
     * Tạo tài khoản mới (Register/Create Account).
     *
     * @param taiKhoan đối tượng tài khoản (password sẽ được hash tự động)
     * @return true nếu thành công, false nếu thất bại
     */
    boolean register(TaiKhoan taiKhoan);

    /**
     * Đổi mật khẩu.
     *
     * @param id          ID tài khoản
     * @param oldPassword mật khẩu cũ (plain text)
     * @param newPassword mật khẩu mới (plain text)
     * @return true nếu thành công, false nếu thất bại
     */
    boolean changePassword(int id, String oldPassword, String newPassword);

    /**
     * Kiểm tra xem tên đăng nhập đã tồn tại chưa.
     *
     * @param username tên đăng nhập
     * @return true nếu đã tồn tại, false nếu không
     */
    boolean isUsernameExist(String username);
}