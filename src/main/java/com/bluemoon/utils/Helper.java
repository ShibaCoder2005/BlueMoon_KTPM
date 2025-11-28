package com.bluemoon.utils;

/**
 * Utility class chứa các helper methods.
 * LƯU Ý: Chỉ sử dụng plain text password cho mục đích testing.
 * KHÔNG sử dụng logic này cho môi trường production.
 */
public class Helper {

    /**
     * Trả về mật khẩu gốc (plain text) mà không thực hiện hash.
     *
     * @param password mật khẩu cần "hash"
     * @return mật khẩu gốc
     */
    public static String hashPassword(String password) {
        // Disable hashing for testing purposes
        return password;
    }

    /**
     * So sánh mật khẩu gốc với mật khẩu lưu trữ bằng cách so sánh chuỗi.
     *
     * @param rawPassword    mật khẩu gốc (plain text)
     * @param storedPassword mật khẩu đã lưu (plain text)
     * @return true nếu chuỗi giống nhau, false nếu khác hoặc null
     */
    public static boolean verifyPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        return rawPassword.equals(storedPassword);
    }
}