package com.bluemoon.services.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.AuthService;

/**
 * Cài đặt đơn giản của {@link AuthService} sử dụng dữ liệu trong bộ nhớ.
 * Trong môi trường production, lớp này sẽ tương tác với DAO/Repository.
 */
public class AuthServiceImpl implements AuthService {

    private final Map<String, TaiKhoan> taiKhoanStore = new ConcurrentHashMap<>();

    public AuthServiceImpl() {
        seedDefaultAccounts();
    }

    @Override
    public TaiKhoan authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        TaiKhoan taiKhoan = taiKhoanStore.get(username.trim().toLowerCase());
        if (taiKhoan == null) {
            return null;
        }
        return Objects.equals(taiKhoan.getMatKhau(), password) ? taiKhoan : null;
    }

    private void seedDefaultAccounts() {
        TaiKhoan admin = new TaiKhoan(1, "admin", "admin123", "Quản trị hệ thống", "BanQuanLy", "0900000000", "Hoạt động");
        taiKhoanStore.put(admin.getTenDangNhap().trim().toLowerCase(), admin);
    }
}

