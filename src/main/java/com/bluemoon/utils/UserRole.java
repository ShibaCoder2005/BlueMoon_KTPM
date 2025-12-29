package com.bluemoon.utils;

/**
 * Enum định nghĩa các vai trò người dùng trong hệ thống.
 */
public enum UserRole {
    ADMIN("Admin", "Quản trị viên"),
    MANAGEMENT("BanQuanLy", "Ban quản lý"),
    ACCOUNTANT("KeToan", "Kế toán");

    private final String code;
    private final String displayName;

    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển đổi string thành UserRole enum.
     * Hỗ trợ nhiều format: "Admin", "BanQuanLy", "KeToan", "Ban Quản lý", "Kế toán", etc.
     */
    public static UserRole fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return null;
        }

        String normalized = roleStr.trim();
        
        // Check exact matches first
        for (UserRole role : UserRole.values()) {
            if (role.code.equalsIgnoreCase(normalized) || 
                role.displayName.equalsIgnoreCase(normalized)) {
                return role;
            }
        }

        // Check partial matches
        String lower = normalized.toLowerCase();
        if (lower.contains("admin") || lower.equals("admin")) {
            return ADMIN;
        } else if (lower.contains("quản lý") || lower.contains("quan ly") || 
                   lower.contains("management") || lower.equals("banquanly")) {
            return MANAGEMENT;
        } else if (lower.contains("kế toán") || lower.contains("ke toan") || 
                   lower.contains("accountant") || lower.equals("ketoan")) {
            return ACCOUNTANT;
        }

        return null;
    }

    /**
     * Kiểm tra xem role có phải là Admin không.
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Kiểm tra xem role có phải là Management không.
     */
    public boolean isManagement() {
        return this == MANAGEMENT;
    }

    /**
     * Kiểm tra xem role có phải là Accountant không.
     */
    public boolean isAccountant() {
        return this == ACCOUNTANT;
    }
}

