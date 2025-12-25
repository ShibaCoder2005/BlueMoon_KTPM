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
    register: (data) => apiRequest('/register', 'POST', data),
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
    updateStatus: (id, status) => apiRequest(`/tai-khoan/${id}/status`, 'PUT', { trangThai: status })
};

const KhoanThuAPI = {
    getAll: async () => {
        const response = await apiRequest('/khoan-thu', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    create: (data) => apiRequest('/khoan-thu', 'POST', data),
    update: (id, data) => apiRequest(`/khoan-thu/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/khoan-thu/${id}`, 'DELETE')
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

const PhieuThuAPI = {
    getAll: async () => {
        const response = await apiRequest('/phieu-thu', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    create: (data) => apiRequest('/phieu-thu', 'POST', data),
    update: (id, data) => apiRequest(`/phieu-thu/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/phieu-thu/${id}`, 'DELETE'),
    hasUnpaidFees: (maHo) => apiRequest(`/phieu-thu/ho-gia-dinh/${maHo}/unpaid`, 'GET')
};

const LichSuNopTienAPI = {
    getAll: async () => {
        const response = await apiRequest('/lich-su-nop-tien', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    create: (data) => apiRequest('/lich-su-nop-tien', 'POST', data),
    getByPhieuThu: (maPhieu) => apiRequest(`/lich-su-nop-tien/phieu-thu/${maPhieu}`, 'GET'),
    getByHoGiaDinh: (maHo) => apiRequest(`/lich-su-nop-tien/ho-gia-dinh/${maHo}`, 'GET'),
    createWithStatusUpdate: (paymentRecord, updateStatusTo) => apiRequest('/lich-su-nop-tien/with-status-update', 'POST', { paymentRecord, updateStatusTo })
};

const LichSuNhanKhauAPI = {
    getAll: async () => {
        const response = await apiRequest('/nhan-khau/lich-su/all', 'GET');
        return Array.isArray(response) ? response : (response.data || []);
    },
    getByNhanKhau: (id) => apiRequest(`/nhan-khau/${id}/lich-su`, 'GET'),
    create: (data) => apiRequest('/nhan-khau/lich-su', 'POST', data)
};

const ThongKeAPI = {
    dashboard: () => apiRequest('/thong-ke/dashboard', 'GET'),
    getDashboard: () => apiRequest('/thong-ke/dashboard', 'GET'),
    revenue: (params) => {
        const queryParams = new URLSearchParams();
        if (params.fromDate) queryParams.append('fromDate', params.fromDate);
        if (params.toDate) queryParams.append('toDate', params.toDate);
        const queryString = queryParams.toString();
        return apiRequest(`/thong-ke/revenue${queryString ? '?' + queryString : ''}`, 'GET');
    },
    revenueTotal: (params) => {
        const queryParams = new URLSearchParams();
        if (params.fromDate) queryParams.append('fromDate', params.fromDate);
        if (params.toDate) queryParams.append('toDate', params.toDate);
        const queryString = queryParams.toString();
        return apiRequest(`/thong-ke/revenue/total${queryString ? '?' + queryString : ''}`, 'GET');
    },
    revenueDetails: (params) => {
        const queryParams = new URLSearchParams();
        if (params.fromDate) queryParams.append('fromDate', params.fromDate);
        if (params.toDate) queryParams.append('toDate', params.toDate);
        const queryString = queryParams.toString();
        return apiRequest(`/thong-ke/revenue/details${queryString ? '?' + queryString : ''}`, 'GET');
    },
    debt: () => apiRequest('/thong-ke/debt', 'GET'),
    debtTotal: () => apiRequest('/thong-ke/debt/total', 'GET'),
    debtDetails: () => apiRequest('/thong-ke/debt/details', 'GET'),
    report: (maDotThu) => apiRequest(`/thong-ke/report/${maDotThu}`, 'GET')
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
