const API_BASE = '/api';

/**
 * Chuẩn hóa API request handler
 * - Luôn trả về JSON
 * - Xử lý lỗi rõ ràng
 * - Log payload để debug
 */
async function apiRequest(endpoint, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (data !== null && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
        // Log payload để debug
        console.log(`[apiRequest] ${method} ${endpoint}:`, data);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        
        // Đọc response text trước để kiểm tra content-type
        const contentType = response.headers.get('content-type') || '';
        const isJson = contentType.includes('application/json');
        
        let result;
        const text = await response.text();
        
        // Kiểm tra HTTP status TRƯỚC khi parse
        if (!response.ok) {
            // Response không ok - xử lý lỗi
            if (isJson && text) {
                try {
                    result = JSON.parse(text);
                    const errorMsg = result.message || result.details || result.errorCode || `HTTP ${response.status}`;
                    console.error(`[apiRequest] HTTP ${response.status} from ${endpoint}:`, errorMsg);
                    throw new Error(errorMsg);
                } catch (parseError) {
                    // Không parse được JSON từ error response
                    console.error(`[apiRequest] Error response not JSON from ${endpoint}:`, text.substring(0, 200));
                    throw new Error(`Server error (${response.status}): ${text.substring(0, 100)}`);
                }
            } else {
                // Response không phải JSON
                console.error(`[apiRequest] HTTP ${response.status} non-JSON error from ${endpoint}:`, text.substring(0, 200));
                throw new Error(`Server error (${response.status}): ${text.substring(0, 100) || 'Unknown error'}`);
            }
        }
        
        // Response OK - parse JSON
        if (isJson && text) {
            try {
                result = JSON.parse(text);
            } catch (parseError) {
                console.error(`[apiRequest] JSON parse error for ${endpoint}:`, parseError);
                console.error(`[apiRequest] Response text:`, text.substring(0, 200));
                throw new Error(`Invalid JSON response from server: ${text.substring(0, 100)}`);
            }
        } else if (text) {
            // Response OK nhưng không phải JSON - không nên xảy ra với API
            console.warn(`[apiRequest] Non-JSON response from ${endpoint}:`, text.substring(0, 200));
            result = {
                success: true,
                data: text
            };
        } else {
            // Empty response - có thể là DELETE thành công
            result = {
                success: true,
                data: null
            };
        }

        // Kiểm tra success flag trong response (nếu có)
        if (result.success === false) {
            const errorMsg = result.message || result.details || 'Request failed';
            console.error(`[apiRequest] Request failed for ${endpoint}:`, errorMsg);
            throw new Error(errorMsg);
        }

        return result;
    } catch (error) {
        // Re-throw nếu đã là Error object
        if (error instanceof Error) {
            throw error;
        }
        // Wrap các lỗi khác
        console.error(`[apiRequest] Unexpected error for ${endpoint}:`, error);
        throw new Error(error.message || String(error));
    }
}

function showSuccess(message) {
    alert('Thành công: ' + message);
}

function showError(message) {
    alert('Lỗi: ' + message);
}

const AuthAPI = {
    login: (data) => apiRequest('/login', 'POST', data),
    checkUsername: (username) => apiRequest(`/check-username/${encodeURIComponent(username)}`, 'GET'),
    changePassword: (data) => apiRequest('/change-password', 'POST', data)
};

const TaiKhoanAPI = {
    getAll: async () => {
        const response = await apiRequest('/tai-khoan', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    create: (data) => apiRequest('/tai-khoan', 'POST', data),
    update: (id, data) => apiRequest(`/tai-khoan/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/tai-khoan/${id}`, 'DELETE'),
    updateStatus: (id, trangThai, currentUserId) => {
        const data = { trangThai };
        if (currentUserId) {
            data.currentUserId = currentUserId;
        }
        return apiRequest(`/tai-khoan/${id}/status`, 'PUT', data);
    }
};

const KhoanThuAPI = {
    getAll: async () => {
        const response = await apiRequest('/khoan-thu', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    create: async (data) => {
        const response = await apiRequest('/khoan-thu', 'POST', data);
        return response.data || response;
    },
    update: async (id, data) => {
        const response = await apiRequest(`/khoan-thu/${id}`, 'PUT', data);
        return response.data || response;
    },
    delete: async (id) => {
        const response = await apiRequest(`/khoan-thu/${id}`, 'DELETE');
        return response.data || response;
    }
};

const HoGiaDinhAPI = {
    getAll: async () => {
        const response = await apiRequest('/ho-gia-dinh', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    create: (data) => apiRequest('/ho-gia-dinh', 'POST', data),
    update: (id, data) => apiRequest(`/ho-gia-dinh/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/ho-gia-dinh/${id}`, 'DELETE')
};

const NhanKhauAPI = {
    getAll: async () => {
        const response = await apiRequest('/nhan-khau', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },

    getAllHistory: async () => {
        const response = await apiRequest('/nhan-khau/lich-su/all', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },

    getById: (id) => apiRequest(`/nhan-khau/${id}`, 'GET'),
    create: (data) => apiRequest('/nhan-khau', 'POST', data),
    update: (id, data) => apiRequest(`/nhan-khau/${id}`, 'PUT', data),
    delete: async (id) => {
        console.log('[NhanKhauAPI.delete] Calling DELETE /api/nhan-khau/' + id);
        const response = await apiRequest(`/nhan-khau/${id}`, 'DELETE');
        console.log('[NhanKhauAPI.delete] Response:', response);
        return response;
    },
    updateStatus: (id, newStatus, historyRecord) => apiRequest(`/nhan-khau/${id}/status`, 'PUT', { newStatus, historyRecord })
};

const DotThuAPI = {
    getAll: async () => {
        const response = await apiRequest('/dot-thu', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    getById: (id) => apiRequest(`/dot-thu/${id}`, 'GET'),
    create: (data) => apiRequest('/dot-thu', 'POST', data),
    update: (id, data) => apiRequest(`/dot-thu/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/dot-thu/${id}`, 'DELETE'),
    search: (keyword) => apiRequest(`/dot-thu/search/${encodeURIComponent(keyword)}`, 'GET')
};

const PhieuThuAPI = {
    getAll: async () => {
        const response = await apiRequest('/phieu-thu', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    getById: (id) => apiRequest(`/phieu-thu/${id}`, 'GET'),
    getChiTiet: (id) => apiRequest(`/phieu-thu/${id}/chi-tiet`, 'GET'),
    getDetail: async (id) => {
        const response = await apiRequest(`/phieu-thu/${id}/detail`, 'GET');
        return response.data || response;
    },
    exportPdf: async (id) => {
        const response = await fetch(`/api/phieu-thu/${id}/export`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            }
        });
        
        if (!response.ok) {
            const text = await response.text();
            throw new Error(`Export failed: ${text}`);
        }
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `PhieuThu_${id}_${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    },
    create: (data) => apiRequest('/phieu-thu', 'POST', data),
    createWithDetails: (phieuThu, chiTietList) => apiRequest('/phieu-thu/with-details', 'POST', { phieuThu, chiTietList }),
    update: (id, data) => apiRequest(`/phieu-thu/${id}`, 'PUT', data),
    updateStatus: (id, newStatus) => apiRequest(`/phieu-thu/${id}/status`, 'PUT', { newStatus }),
    delete: (id) => apiRequest(`/phieu-thu/${id}`, 'DELETE'),
    getByHoGiaDinh: (maHo) => apiRequest(`/phieu-thu/ho-gia-dinh/${maHo}`, 'GET'),
    getByDotThu: (maDotThu) => apiRequest(`/phieu-thu/dot-thu/${maDotThu}`, 'GET'),
    hasUnpaidFees: (maHo) => apiRequest(`/phieu-thu/ho-gia-dinh/${maHo}/unpaid`, 'GET'),
    generateForDrive: (maDot) => apiRequest(`/phieu-thu/generate/${maDot}`, 'POST')
};

const PhongAPI = {
    getAll: () => apiRequest('/phong', 'GET'),
    getById: (id) => apiRequest(`/phong/${id}`, 'GET'),
    create: (data) => apiRequest('/phong', 'POST', data),
    update: (id, data) => apiRequest(`/phong/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/phong/${id}`, 'DELETE')
};

const PhuongTienAPI = {
    getAll: async () => {
        const response = await apiRequest('/phuong-tien', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    getById: (id) => apiRequest(`/phuong-tien/${id}`, 'GET'),
    getByHoGiaDinh: (maHo) => apiRequest(`/phuong-tien/ho-gia-dinh/${maHo}`, 'GET'),
    create: (data) => apiRequest('/phuong-tien', 'POST', data),
    update: (id, data) => apiRequest(`/phuong-tien/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/phuong-tien/${id}`, 'DELETE'),
    search: (keyword) => apiRequest(`/phuong-tien/search/${encodeURIComponent(keyword)}`, 'GET')
};


const LichSuNhanKhauAPI = {
    getAll: async () => {
        const response = await apiRequest('/nhan-khau/lich-su/all', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    getByNhanKhau: (id) => apiRequest(`/nhan-khau/${id}/lich-su`, 'GET'),
    create: (data) => apiRequest('/nhan-khau/lich-su', 'POST', data)
};

// --- THỐNG KÊ API ---
const ThongKeAPI = {
    // 1. Dashboard tổng quan
    getDashboardStats: () => apiRequest('/thong-ke/dashboard', 'GET'),
    
    // 2. Biểu đồ doanh thu
    getRevenueStats: (fromDate, toDate) => {
        let url = '/thong-ke/doanh-thu';
        if (fromDate && toDate) url += `?from=${fromDate}&to=${toDate}`;
        return apiRequest(url, 'GET');
    },
    
    // 3. Biểu đồ nhân khẩu
    getDemographics: () => apiRequest('/thong-ke/nhan-khau', 'GET'),

    // 4. Biểu đồ công nợ (MỚI THÊM)
    getDebtStats: () => apiRequest('/thong-ke/cong-no', 'GET'),

    // 5. Chi tiết công nợ (MỚI THÊM)
    getDebtDetails: () => apiRequest('/thong-ke/cong-no/chi-tiet', 'GET')
};

// --- BÁO CÁO API ---
const BaoCaoAPI = {
    // Báo cáo thu (theo tháng/năm hoặc khoảng ngày)
    getRevenueReport: (params) => {
        const query = new URLSearchParams(params).toString();
        return apiRequest(`/bao-cao/thu?${query}`, 'GET');
    },

    // Báo cáo công nợ (theo đợt)
    getDebtReport: (maDot) => {
        const url = maDot ? `/bao-cao/cong-no?maDot=${maDot}` : '/bao-cao/cong-no';
        return apiRequest(url, 'GET');
    },

    // Xuất Excel Báo cáo thu
    exportRevenue: async (params) => {
        const query = new URLSearchParams(params).toString();
        // Với file binary, ta dùng window.open hoặc fetch blob
        window.open(`/api/bao-cao/thu/export?${query}`, '_blank');
    },

    // Xuất Excel Công nợ
    exportDebt: async (maDot) => {
        const query = maDot ? `?maDot=${maDot}` : '';
        window.open(`/api/bao-cao/cong-no/export${query}`, '_blank');
    }
};

const ThongBaoAPI = {
    getAll: async () => {
        try {
            const response = await apiRequest('/thong-bao', 'GET');
            if (Array.isArray(response)) {
                return response;
            }
            if (response.data && Array.isArray(response.data)) {
                return response.data;
            }
            return [];
        } catch (error) {
            if (error.message.includes('404') || error.message.includes('Not Found') || error.message.includes('API_ENDPOINT_NOT_FOUND')) {
                console.warn('[ThongBaoAPI] API /api/thong-bao chưa được triển khai. Backend cần thêm endpoint này.');
                return [];
            }
            console.error('[ThongBaoAPI.getAll] Lỗi:', error);
            throw error;
        }
    },
    create: (data) => apiRequest('/thong-bao', 'POST', data),
    delete: (id) => apiRequest(`/thong-bao/${id}`, 'DELETE')
};

const ChiTietThuAPI = {
    // Lấy danh sách chi tiết của 1 phiếu
    getByPhieuThu: (maPhieu) => apiRequest(`/phieu-thu/${maPhieu}/chi-tiet`, 'GET'),
    
    // Lưu (Thêm mới hoặc Cập nhật)
    save: (data) => apiRequest('/chi-tiet-thu', 'POST', data),
    
    // Xóa chi tiết
    delete: (id) => apiRequest(`/chi-tiet-thu/${id}`, 'DELETE')
};

const APIUtils = {
    showError: (message) => {
        alert('Lỗi: ' + message);
    },
    showSuccess: (message) => {
        alert('Thành công: ' + message);
    },
    formatDate: (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    },
    normalizeDate: (dateValue) => {
        if (dateValue === null || dateValue === undefined || dateValue === '') {
            return '';
        }
        if (typeof dateValue === 'string') {
            const trimmed = dateValue.trim();
            if (!trimmed) return '';
            if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) {
                return trimmed;
            }
            const isoMatch = trimmed.match(/^(\d{4}-\d{2}-\d{2})/);
            if (isoMatch) {
                return isoMatch[1];
            }
            try {
                const parsed = new Date(trimmed);
                if (!isNaN(parsed.getTime())) {
                    const year = parsed.getFullYear();
                    const month = String(parsed.getMonth() + 1).padStart(2, '0');
                    const day = String(parsed.getDate()).padStart(2, '0');
                    return `${year}-${month}-${day}`;
                }
            } catch (e) {
            }
        }
        if (dateValue instanceof Date) {
            if (isNaN(dateValue.getTime())) return '';
            const year = dateValue.getFullYear();
            const month = String(dateValue.getMonth() + 1).padStart(2, '0');
            const day = String(dateValue.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
        if (typeof dateValue === 'number') {
            try {
                const parsed = new Date(dateValue);
                if (!isNaN(parsed.getTime())) {
                    const year = parsed.getFullYear();
                    const month = String(parsed.getMonth() + 1).padStart(2, '0');
                    const day = String(parsed.getDate()).padStart(2, '0');
                    return `${year}-${month}-${day}`;
                }
            } catch (e) {
            }
        }
        if (typeof dateValue === 'object') {
            const year = dateValue.year || dateValue.yearValue;
            const month = dateValue.month !== undefined ? dateValue.month : (dateValue.monthValue !== undefined ? dateValue.monthValue : null);
            const day = dateValue.day || dateValue.dayOfMonth || dateValue.dayOfMonthValue;
            if (year && month !== null && day) {
                const monthValue = (month < 1 || month > 12) ? month + 1 : month;
                const monthStr = String(monthValue).padStart(2, '0');
                const dayStr = String(day).padStart(2, '0');
                return `${year}-${monthStr}-${dayStr}`;
            }
        }
        return '';
    },
    validateId: (id, entityName) => {
        if (id === null || id === undefined || id === '' || (typeof id === 'number' && id <= 0)) {
            showError(`ID ${entityName || ''} không hợp lệ`);
            return false;
        }
        return true;
    },
    formatCurrency: (amount) => {
        if (!amount) return '0';
        return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
    },
    /**
     * Lấy giá trị từ form element an toàn
     * @param {string} selector - jQuery selector
     * @param {string} defaultValue - Giá trị mặc định nếu không tìm thấy
     * @returns {string}
     */
    getFormValue: (selector, defaultValue = '') => {
        const el = $(selector);
        if (el.length === 0) {
            console.warn(`[APIUtils.getFormValue] Element not found: ${selector}`);
            return defaultValue;
        }
        const value = el.val();
        return value !== null && value !== undefined ? String(value).trim() : defaultValue;
    }
};

/**
 * Hàm logout - Xóa tất cả dữ liệu authentication và redirect về login
 */
function logout() {
    console.log('[logout] Starting logout process...');
    
    try {
        // Clear sessionStorage
        sessionStorage.removeItem('currentUser');
        sessionStorage.removeItem('isLoggedIn');
        sessionStorage.removeItem('userInfo');
        sessionStorage.removeItem('accessToken');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('role');
        sessionStorage.clear();
        
        // Clear localStorage (nếu có)
        localStorage.removeItem('currentUser');
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('userInfo');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        
        // Clear cookies (nếu có)
        document.cookie.split(";").forEach(function(c) {
            document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });
        
        console.log('[logout] All authentication data cleared');
        
        // Redirect to login page
        window.location.href = 'login.html';
        
    } catch (error) {
        console.error('[logout] Error during logout:', error);
        // Vẫn redirect về login dù có lỗi
        window.location.href = 'login.html';
    }
}

// Export logout function globally
if (typeof window !== 'undefined') {
    window.logout = logout;
}

/**
 * Utility functions for user management
 */
const UserUtils = {
    /**
     * Get current user from sessionStorage
     * @returns {Object|null} User object or null if not logged in
     */
    getCurrentUser: () => {
        try {
            const userStr = sessionStorage.getItem('currentUser');
            if (!userStr) return null;
            return JSON.parse(userStr);
        } catch (error) {
            console.error('[UserUtils] Error parsing currentUser:', error);
            return null;
        }
    },
    
    /**
     * Get user display name (hoTen or tenDangNhap)
     * @returns {string} Display name or 'Người dùng' if not available
     */
    getUserDisplayName: () => {
        const user = UserUtils.getCurrentUser();
        if (!user) return 'Người dùng';
        
        // Try hoTen first, then tenDangNhap, then fallback
        if (user.hoTen && user.hoTen.trim()) {
            return user.hoTen.trim();
        }
        if (user.tenDangNhap && user.tenDangNhap.trim()) {
            return user.tenDangNhap.trim();
        }
        return 'Người dùng';
    },
    
    /**
     * Check if user is logged in
     * @returns {boolean}
     */
    isLoggedIn: () => {
        const isLoggedIn = sessionStorage.getItem('isLoggedIn');
        return isLoggedIn === 'true' && UserUtils.getCurrentUser() !== null;
    },
    
    /**
     * Update user display name in navbar
     * Call this on page load to update the user name display
     */
    updateUserDisplay: () => {
        const displayName = UserUtils.getUserDisplayName();
        const userDisplayElements = $('.text-gray-600.small, [id*="userDisplay"], [class*="user-name"]');
        
        // Update all elements that might display user name
        if (userDisplayElements.length > 0) {
            userDisplayElements.each(function() {
                const $el = $(this);
                // Only update if it looks like a hardcoded admin name
                const text = $el.text().trim();
                if (text === 'Admin Quản Lý' || text === 'Ban Quản Lý' || text === 'Trưởng Ban Quản Lý' || 
                    text === 'Nguyễn Văn A' || text === 'Douglas McGee' || text.startsWith('Admin')) {
                    $el.text(displayName);
                }
            });
        }
        
        // Also update specific selector if exists
        const $userDropdown = $('#userDropdown');
        if ($userDropdown.length > 0) {
            const $nameSpan = $userDropdown.find('.text-gray-600.small');
            if ($nameSpan.length > 0) {
                $nameSpan.text(displayName);
            }
        }
    }
};

// Export UserUtils globally
if (typeof window !== 'undefined') {
    window.UserUtils = UserUtils;
}

/**
 * Utility function to update navbar avatar across all pages
 * Call this after changing profile image
 */
function updateNavbarAvatar() {
    try {
        const userStr = sessionStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            if (user && user.id) {
                const imageKey = `profileImage_${user.id}`;
                let imageData = sessionStorage.getItem(imageKey);
                if (!imageData) {
                    imageData = localStorage.getItem(imageKey);
                }
                if (imageData) {
                    // Update all navbar avatars on the page (except profile image)
                    $('.img-profile.rounded-circle').not('#profileImage').attr('src', imageData);
                    $('#navbarAvatar').attr('src', imageData);
                }
            }
        }
    } catch (error) {
        console.error('[updateNavbarAvatar] Error:', error);
    }
}

// Export globally
if (typeof window !== 'undefined') {
    window.updateNavbarAvatar = updateNavbarAvatar;
}

/**
 * 1. Hàm bảo vệ trang - Phiên bản chặn triệt để
 */
function checkPageAccess(allowedRoles) {
    try {
        const userStr = sessionStorage.getItem('currentUser');
        if (!userStr) {
            window.location.href = 'login.html';
            return;
        }

        const user = JSON.parse(userStr);
        const role = (user.vaiTro || user.role || '').toLowerCase();
        const normalizedAllowed = allowedRoles.map(r => r.toLowerCase());

        // A. BẢO VỆ TRANG
        // Nếu không phải Admin và không có quyền
        if (role !== 'admin' && role !== 'quantrivien' && !normalizedAllowed.includes(role)) {
            
            // --- CHẶN GIAO DIỆN NGAY LẬP TỨC ---
            // Ẩn toàn bộ nội dung trang web để không bị lộ
            document.body.style.display = 'none';
            // -----------------------------------

            alert('Bạn không có quyền truy cập trang này!');
            window.location.href = 'index.html'; 
            return;
        }

        // B. XỬ LÝ MENU (Nếu được phép vào)
        // Vì checkPageAccess giờ chạy trước $(document).ready, ta cần chờ DOM
        if (window.jQuery) {
            $(document).ready(function() { applySidebarRBAC_Internal(role); });
        } else {
            document.addEventListener('DOMContentLoaded', function() { applySidebarRBAC_Internal(role); });
        }

    } catch (e) {
        console.error("Lỗi checkPageAccess:", e);
        window.location.href = 'login.html';
    }
}

/**
 * 2. Hàm xử lý hiển thị Menu (Được gọi tự động bên trên)
 * Hàm này cũng có thể được gọi độc lập từ applySidebarRBAC() nếu trang web yêu cầu
 */
function applySidebarRBAC_Internal(role) {
    console.log("[RBAC] Setup menu cho:", role);

    // Nhóm 1: BAN QUẢN LÝ (Ẩn Thu phí, Báo cáo, Tài khoản)
    if (role === 'banquanly' || role === 'quanlycudan') {
        const hideList = ['khoan-thu.html', 'dot-thu.html', 'phieu-thu.html', 'bao-cao.html', 'bao-cao-thu.html', 'bao-cao-cong-no.html'];
        hideList.forEach(page => $(`a[href*="${page}"]`).closest('li').hide());
        $('#menuTaiKhoan').hide();
    } 
    // Nhóm 2: KẾ TOÁN (Ẩn Dân cư, Tài khoản)
    else if (role === 'ketoan' || role === 'quanlythuphi') {
        const hideList = ['phong.html', 'can-ho.html', 'danh-sach-dan-cu.html', 'phuong-tien.html'];
        hideList.forEach(page => $(`a[href*="${page}"]`).closest('li').hide());
        $('#menuTaiKhoan').hide();
    }
    // Nhóm 3: ADMIN (Hiện Tài khoản)
    else if (role === 'admin' || role === 'quantrivien') {
        $('#menuTaiKhoan').show();
    }
}

/**
 * 3. Hàm Wrapper để tương thích với code cũ ở HTML
 * Các trang HTML đang gọi hàm này, nên ta giữ lại tên hàm này để không bị lỗi.
 */
function applySidebarRBAC() {
    const userStr = sessionStorage.getItem('currentUser');
    if (userStr) {
        const role = (JSON.parse(userStr).vaiTro || '').toLowerCase();
        applySidebarRBAC_Internal(role);
    }
}

// Hàm giữ chỗ cho Dashboard (tránh lỗi ở index.html)
function applyDashboardRBAC() {}