package com.bluemoon.utils;

import io.javalin.http.Context;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AccessManager: Quản lý quyền truy cập dựa trên vai trò người dùng.
 */
public class AccessManager {
    private static final Logger logger = Logger.getLogger(AccessManager.class.getName());

    /**
     * Lấy role từ request context (từ header hoặc session).
     * Frontend gửi role trong header "X-User-Role".
     */
    public static UserRole getUserRole(Context ctx) {
        // Try to get from header first (for API calls from frontend)
        String roleHeader = ctx.header("X-User-Role");
        if (roleHeader != null && !roleHeader.isEmpty()) {
            UserRole role = UserRole.fromString(roleHeader);
            if (role != null) {
                logger.fine("User role from header: " + role.getDisplayName() + " for path: " + ctx.path());
                return role;
            }
        }

        // Try to get from Authorization header (if using JWT in future)
        String authHeader = ctx.header("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // TODO: Decode JWT and extract role
            // For now, skip JWT parsing
        }

        // Try to get from request body (for backward compatibility)
        try {
            String bodyStr = ctx.body();
            if (bodyStr != null && !bodyStr.isEmpty()) {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                Object roleObj = body.get("role") != null ? body.get("role") : body.get("vaiTro");
                if (roleObj != null) {
                    UserRole role = UserRole.fromString(roleObj.toString());
                    if (role != null) {
                        return role;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore - body might not be JSON or might not have role
        }

        // Try query param (for backward compatibility)
        String roleParam = ctx.queryParam("role");
        if (roleParam != null && !roleParam.isEmpty()) {
            UserRole role = UserRole.fromString(roleParam);
            if (role != null) {
                return role;
            }
        }

        logger.warning("Could not determine user role for path: " + ctx.path());
        return null;
    }

    /**
     * Kiểm tra xem user có quyền truy cập endpoint không.
     * 
     * @param ctx Javalin context
     * @param allowedRoles Danh sách các role được phép
     * @return true nếu có quyền, false nếu không
     */
    public static boolean hasAccess(Context ctx, UserRole... allowedRoles) {
        UserRole userRole = getUserRole(ctx);
        
        // Nếu không có role, có thể là user chưa login hoặc đang truy cập public endpoint
        // Cho phép truy cập nếu endpoint không yêu cầu authentication
        if (userRole == null) {
            // Log warning nhưng không block - để frontend xử lý redirect
            logger.fine("User role is null for path: " + ctx.path() + " - may be unauthenticated request");
            // Trả về false để yêu cầu authentication, nhưng không throw error
            return false;
        }

        // Admin có quyền truy cập tất cả
        if (userRole.isAdmin()) {
            return true;
        }

        // Kiểm tra xem user role có trong danh sách allowed roles không
        List<UserRole> allowedList = Arrays.asList(allowedRoles);
        return allowedList.contains(userRole);
    }

    /**
     * Kiểm tra và trả về 403 nếu không có quyền.
     * 
     * @param ctx Javalin context
     * @param allowedRoles Danh sách các role được phép
     * @return true nếu có quyền, false nếu đã trả về 403
     */
    public static boolean requireAccess(Context ctx, UserRole... allowedRoles) {
        if (!hasAccess(ctx, allowedRoles)) {
            UserRole userRole = getUserRole(ctx);
            String roleStr = userRole != null ? userRole.getDisplayName() : "Chưa đăng nhập";
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Access Denied");
            errorResponse.put("errorCode", userRole == null ? "UNAUTHORIZED" : "FORBIDDEN");
            if (userRole == null) {
                errorResponse.put("details", "Bạn cần đăng nhập để truy cập tài nguyên này.");
            } else {
                errorResponse.put("details", 
                    String.format("Người dùng với vai trò '%s' không có quyền truy cập tài nguyên này.", roleStr));
            }
            errorResponse.put("path", ctx.path());
            
            ctx.status(userRole == null ? 401 : 403);
            ctx.json(errorResponse);
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra quyền truy cập module dựa trên path.
     * 
     * @param path API path
     * @param userRole Role của user
     * @return true nếu có quyền truy cập
     */
    public static boolean canAccessModule(String path, UserRole userRole) {
        if (userRole == null) {
            return false;
        }

        // Admin có quyền truy cập tất cả
        if (userRole.isAdmin()) {
            return true;
        }

        // Management: Chỉ truy cập được modules liên quan đến cư dân
        if (userRole.isManagement()) {
            return path.contains("/ho-gia-dinh") || 
                   path.contains("/nhan-khau") || 
                   path.contains("/phuong-tien") ||
                   path.contains("/thong-ke") ||
                   path.contains("/dashboard") ||
                   path.equals("/") || path.equals("/index.html");
        }

        // Accountant: Chỉ truy cập được modules liên quan đến tài chính
        if (userRole.isAccountant()) {
            return path.contains("/khoan-thu") || 
                   path.contains("/dot-thu") || 
                   path.contains("/phieu-thu") ||
                   path.contains("/thong-ke") ||
                   path.contains("/bao-cao") ||
                   path.contains("/reports") ||
                   path.contains("/dashboard") ||
                   path.equals("/") || path.equals("/index.html");
        }

        return false;
    }
}

