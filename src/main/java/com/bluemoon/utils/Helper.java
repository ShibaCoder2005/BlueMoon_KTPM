package com.bluemoon.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class chứa các helper methods.
 */
public class Helper {

    /**
     * Hash mật khẩu sử dụng SHA-256.
     * Trong production, nên sử dụng BCrypt hoặc Argon2.
     *
     * @param password mật khẩu cần hash
     * @return mật khẩu đã hash (hex string)
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback: return password as-is if hashing fails
            // In production, this should throw an exception or use a different algorithm
            return password;
        }
    }
}

