/**
 * Frontend RBAC Utility - UI-level access control
 * Không thay đổi backend logic
 */

/**
 * Lấy role của user hiện tại
 * Ưu tiên: localStorage.getItem('role') > currentUser.role > sessionStorage
 * Admin role: 'ADMIN' hoặc 'Admin' (không phải 'BanQuanLy' hay 'Ban quản lý')
 */
function getCurrentUserRole() {
    try {
        // Try localStorage first
        let role = localStorage.getItem('role');
        if (role) {
            const normalized = normalizeRole(role);
            if (normalized) return normalized;
        }
        
        // Try global currentUser variable
        if (typeof currentUser !== 'undefined' && currentUser && currentUser.role) {
            const normalized = normalizeRole(currentUser.role);
            if (normalized) return normalized;
        }
        
        // Try sessionStorage
        const userStr = sessionStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            if (user.role) {
                const normalized = normalizeRole(user.role);
                if (normalized) return normalized;
            }
            if (user.vaiTro) {
                const normalized = normalizeRole(user.vaiTro);
                if (normalized) return normalized;
            }
        }
    } catch (e) {
        console.error('Error getting user role:', e);
    }
    return null;
}

/**
 * Chuẩn hóa role string thành format chuẩn
 * Admin: 'ADMIN' (không phải 'BanQuanLy' hay 'Ban quản lý')
 */
function normalizeRole(role) {
    if (!role) return null;
    
    const roleStr = role.toString().trim();
    const roleLower = roleStr.toLowerCase();
    
    // Admin - chỉ map các giá trị rõ ràng là admin (hỗ trợ cả 'admin' và 'Admin')
    if (roleLower === 'admin' || roleStr === 'Admin' || roleStr === 'admin') {
        return 'ADMIN';
    }
    
    // Management - Ban quản lý
    if (roleLower.includes('quản lý') || roleLower.includes('quan ly') || 
        roleLower === 'banquanly' || roleLower === 'management') {
        return 'MANAGEMENT';
    }
    
    // Accountant - Kế toán
    if (roleLower.includes('kế toán') || roleLower.includes('ke toan') || 
        roleLower === 'ketoan' || roleLower === 'accountant') {
        return 'ACCOUNTANT';
    }
    
    // Nếu không match, trả về uppercase
    return roleStr.toUpperCase();
}

/**
 * Kiểm tra xem user có role cụ thể không
 */
function hasRole(role) {
    const currentRole = getCurrentUserRole();
    if (!currentRole) return false;
    return currentRole === role.toUpperCase();
}

/**
 * Kiểm tra xem user có phải Admin không
 */
function isAdmin() {
    return hasRole('ADMIN');
}

/**
 * Kiểm tra xem user có phải Management không
 */
function isManagement() {
    return hasRole('MANAGEMENT');
}

/**
 * Kiểm tra xem user có phải Accountant không
 */
function isAccountant() {
    return hasRole('ACCOUNTANT');
}

/**
 * Áp dụng RBAC cho sidebar menu
 * Ẩn/hiện menu items dựa trên role
 * ADMIN: Hiển thị tất cả menu items
 */
function applySidebarRBAC() {
    const role = getCurrentUserRole();
    
    if (!role) {
        console.warn('No user role found, showing all menu items');
        return;
    }
    
    // ADMIN: Hiển thị tất cả menu items (không ẩn gì cả)
    if (role === 'ADMIN') {
        // Đảm bảo tất cả menu items đều hiển thị
        $('li.nav-item').show();
        return;
    }
    
    // Menu items cần ẩn cho MANAGEMENT
    if (role === 'MANAGEMENT') {
        // Ẩn: Khoản thu, Đợt thu, Phiếu thu, Báo cáo
        $('a[href="khoan-thu.html"]').closest('li.nav-item').hide();
        $('a[href="dot-thu.html"]').closest('li.nav-item').hide();
        $('a[href="phieu-thu.html"]').closest('li.nav-item').hide();
        $('a[href="bao-cao.html"]').closest('li.nav-item').hide();
    }
    
    // Menu items cần ẩn cho ACCOUNTANT
    if (role === 'ACCOUNTANT') {
        // Ẩn: Hộ gia đình, Cư dân, Phương tiện
        $('a[href="can-ho.html"]').closest('li.nav-item').hide();
        $('a[href="danh-sach-dan-cu.html"]').closest('li.nav-item').hide();
        $('a[href="phuong-tien.html"]').closest('li.nav-item').hide();
    }
}

/**
 * Áp dụng RBAC cho Dashboard buttons
 * ADMIN: Hiển thị tất cả buttons
 */
function applyDashboardRBAC() {
    const role = getCurrentUserRole();
    
    if (!role) {
        console.warn('No user role found');
        return;
    }
    
    // ADMIN: Hiển thị tất cả buttons
    if (role === 'ADMIN') {
        $('#btnQuickCreateFee').show();
        $('#btnQuickExportResidents').show();
        return;
    }
    
    // MANAGEMENT: Ẩn button "Tạo khoản thu mới"
    if (role === 'MANAGEMENT') {
        $('#btnQuickCreateFee').hide();
        $('#btnQuickExportResidents').show(); // Vẫn hiển thị button xuất danh sách
    }
    
    // ACCOUNTANT: Ẩn button "Xuất danh sách cư dân"
    if (role === 'ACCOUNTANT') {
        $('#btnQuickCreateFee').show(); // Vẫn hiển thị button tạo khoản thu
        $('#btnQuickExportResidents').hide();
    }
}

/**
 * Áp dụng RBAC cho Statistics page
 * Ẩn/hiện charts dựa trên role
 * ADMIN: Hiển thị tất cả charts
 */
function applyStatisticsRBAC() {
    const role = getCurrentUserRole();
    
    if (!role) {
        console.warn('No user role found');
        return;
    }
    
    // ADMIN: Hiển thị tất cả charts
    if (role === 'ADMIN') {
        $('.card').show();
        return;
    }
    
    // MANAGEMENT: Chỉ hiển thị charts liên quan đến cư dân
    if (role === 'MANAGEMENT') {
        // Ẩn các charts tài chính
        // Tìm các elements chứa "revenue", "debt", "thu", "nộp" trong title hoặc id
        $('.card-header, .card-body').each(function() {
            const text = $(this).text().toLowerCase();
            if (text.includes('doanh thu') || text.includes('công nợ') || 
                text.includes('thu tháng') || text.includes('nộp tiền') ||
                text.includes('revenue') || text.includes('debt')) {
                $(this).closest('.card').hide();
            }
        });
        
        // Ẩn các charts có id chứa "revenue" hoặc "debt"
        $('[id*="revenue"], [id*="debt"], [id*="Revenue"], [id*="Debt"]').closest('.card').hide();
    }
    
    // ACCOUNTANT: Chỉ hiển thị charts liên quan đến tài chính
    if (role === 'ACCOUNTANT') {
        // Ẩn các charts cư dân
        $('.card-header, .card-body').each(function() {
            const text = $(this).text().toLowerCase();
            if (text.includes('dân số') || text.includes('nhân khẩu') || 
                text.includes('giới tính') || text.includes('độ tuổi') ||
                text.includes('demographics') || text.includes('resident')) {
                $(this).closest('.card').hide();
            }
        });
        
        // Ẩn các charts có id chứa "demographics" hoặc "resident"
        $('[id*="demographics"], [id*="resident"], [id*="Demographics"], [id*="Resident"]').closest('.card').hide();
    }
}

// Export functions globally
if (typeof window !== 'undefined') {
    window.getCurrentUserRole = getCurrentUserRole;
    window.hasRole = hasRole;
    window.isAdmin = isAdmin;
    window.isManagement = isManagement;
    window.isAccountant = isAccountant;
    window.applySidebarRBAC = applySidebarRBAC;
    window.applyDashboardRBAC = applyDashboardRBAC;
    window.applyStatisticsRBAC = applyStatisticsRBAC;
}
