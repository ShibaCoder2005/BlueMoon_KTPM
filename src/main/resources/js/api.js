const API_BASE = '/api';

async function apiRequest(endpoint, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (data !== null && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    const response = await fetch(`${API_BASE}${endpoint}`, options);
    const result = await response.json().catch(() => ({ message: `HTTP error! status: ${response.status}` }));

    if (!response.ok) {
        throw new Error(result.message || result.details || `HTTP error! status: ${response.status}`);
    }

    if (result.success === false) {
        throw new Error(result.message || result.details || 'Request failed');
    }

    return result;
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
    checkUsername: (username) => apiRequest(`/check-username/${encodeURIComponent(username)}`, 'GET')
};

const TaiKhoanAPI = {
    getAll: () => apiRequest('/tai-khoan', 'GET'),
    create: (data) => apiRequest('/tai-khoan', 'POST', data),
    update: (id, data) => apiRequest(`/tai-khoan/${id}`, 'PUT', data),
    updateStatus: (id, status) => apiRequest(`/tai-khoan/${id}/status`, 'PUT', { trangThai: status })
};

const KhoanThuAPI = {
    getAll: () => apiRequest('/khoan-thu', 'GET'),
    create: (data) => apiRequest('/khoan-thu', 'POST', data),
    update: (id, data) => apiRequest(`/khoan-thu/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/khoan-thu/${id}`, 'DELETE')
};

const HoGiaDinhAPI = {
    getAll: () => apiRequest('/ho-gia-dinh', 'GET'),
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
    delete: (id) => apiRequest(`/nhan-khau/${id}`, 'DELETE'),
    updateStatus: (id, newStatus, historyRecord) => apiRequest(`/nhan-khau/${id}/status`, 'PUT', { newStatus, historyRecord })
};

const PhieuThuAPI = {
    getAll: () => apiRequest('/phieu-thu', 'GET'),
    create: (data) => apiRequest('/phieu-thu', 'POST', data),
    update: (id, data) => apiRequest(`/phieu-thu/${id}`, 'PUT', data),
    delete: (id) => apiRequest(`/phieu-thu/${id}`, 'DELETE'),
    hasUnpaidFees: (maHo) => apiRequest(`/phieu-thu/ho-gia-dinh/${maHo}/unpaid`, 'GET')
};

const LichSuNopTienAPI = {
    getAll: () => apiRequest('/lich-su-nop-tien', 'GET'),
    create: (data) => apiRequest('/lich-su-nop-tien', 'POST', data)
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
    }
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
    }
};
