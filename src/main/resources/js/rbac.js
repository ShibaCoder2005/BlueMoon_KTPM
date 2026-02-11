/**
 * rbac.js - Phân quyền hiển thị Menu & Chặn truy cập
 * Dựa trên Database Schema: vaiTro IN ('Admin', 'KeToan', 'BanQuanLy')
 */

// Hàm tiện ích để lấy role hiện tại (đã chuẩn hóa về chữ thường)
function getCurrentUserRole() {
    try {
        const userStr = sessionStorage.getItem('currentUser');
        if (!userStr) return null;
        
        const user = JSON.parse(userStr);
        // Ưu tiên lấy trường 'vaiTro', nếu không có thì 'role'
        const rawRole = user.vaiTro || user.role || '';
        
        return rawRole.toLowerCase().trim();
    } catch (e) {
        console.error('[RBAC] Lỗi parse user:', e);
        return null;
    }
}

// 1. Hàm ẩn/hiện menu bên trái
function applySidebarRBAC() {
    const role = getCurrentUserRole();
    console.log('[RBAC] Vai trò hiện tại:', role);

    if (!role) {
        // Chưa đăng nhập -> Ẩn menu nhạy cảm
        $('#menuTaiKhoan').hide();
        return;
    }

    // --- CẤU HÌNH QUYỀN ---
    // Những vai trò được phép truy cập Quản lý Tài khoản
    // Database: 'Admin', 'BanQuanLy' (Bỏ 'KeToan' vì kế toán không được sửa user)
    const allowedRoles = ['admin', 'banquanly'];

    // Kiểm tra
    const isAllowed = allowedRoles.includes(role);

    // Xử lý giao diện
    const menuTaiKhoan = $('#menuTaiKhoan');
    if (isAllowed) {
        menuTaiKhoan.show();
    } else {
        menuTaiKhoan.hide();
    }
}

// 2. Hàm chặn khi click vào link (Bảo vệ 2 lớp)
function checkAdminBeforeNavigate(event) {
    const role = getCurrentUserRole();
    
    // Danh sách được phép (phải khớp với logic ở trên)
    const allowedRoles = ['admin', 'banquanly'];

    if (!role || !allowedRoles.includes(role)) {
        event.preventDefault(); // Chặn chuyển trang
        alert('Truy cập bị từ chối!\nChức năng này chỉ dành cho Admin hoặc Ban Quản Lý.');
        return false;
    }
    return true;
}