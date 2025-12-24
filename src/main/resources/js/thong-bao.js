// ThongBao API - Sử dụng từ api.js
// LƯU Ý: Backend chưa có endpoint /api/thong-bao
// ThongBaoAPI đã được định nghĩa trong api.js

// Load danh sách thông báo
async function loadThongBao() {
    const tbody = $('#notiHistoryTable');
    
    try {
        tbody.html('<tr><td colspan="5" class="text-center"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td></tr>');

        const notifications = await ThongBaoAPI.getAll();

        if (notifications.length === 0) {
            tbody.html('<tr><td colspan="5" class="text-center text-muted">Chưa có thông báo nào</td></tr>');
            return;
        }

        renderThongBao(notifications);
    } catch (error) {
        console.error('[loadThongBao] Lỗi:', error);
        tbody.html(`<tr><td colspan="5" class="text-center text-danger">Lỗi: ${error.message}</td></tr>`);
    }
}

// Render danh sách thông báo vào bảng
function renderThongBao(notifications) {
    const tbody = $('#notiHistoryTable');
    tbody.empty();

    notifications.forEach(noti => {
        const timeString = formatDateTime(noti.ngayTao || noti.createdAt || noti.thoiGian || noti.ngayGui);
        const targetText = getTargetText(noti.doiTuong || noti.target || noti.doiTuongNhan);
        const priorityClass = (noti.mucDoUuTien === 'Khẩn cấp' || noti.priority === 'urgent' || noti.mucDoUuTien === 'urgent') ? 'text-danger' : '';
        const titleText = noti.tieuDe || noti.title || 'Không có tiêu đề';
        const titleHtml = priorityClass ? 
            `<div class="font-weight-bold ${priorityClass}">${titleText.toUpperCase()}</div>` :
            `<div class="font-weight-bold">${titleText}</div>`;
        
        const contentText = (noti.noiDung || noti.content || '').substring(0, 50);
        const notiId = noti.id || noti.maThongBao || '';
        
        const row = `
            <tr>
                <td>
                    ${titleHtml}
                    <div class="small text-gray-500 text-truncate" style="max-width: 150px;">${contentText}${contentText.length >= 50 ? '...' : ''}</div>
                </td>
                <td class="small">${timeString}</td>
                <td><span class="badge badge-info">${targetText}</span></td>
                <td class="text-center"><span class="badge badge-success">Đã gửi</span></td>
                <td>
                    <button class="btn btn-sm btn-circle btn-light text-danger btn-delete-noti" data-id="${notiId}" title="Xóa">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });

    // Gắn event cho nút xóa (event delegation)
    $(document).off('click', '.btn-delete-noti').on('click', '.btn-delete-noti', function() {
        const id = $(this).data('id');
        if (id) {
            deleteThongBao(id);
        }
    });
}

// Gửi thông báo mới
async function guiThongBao() {
    const form = $('#notificationForm')[0];
    if (!form) {
        console.error('[guiThongBao] Không tìm thấy form #notificationForm');
        return;
    }

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const btnSubmit = $('#notificationForm button[type="submit"]');
    if (btnSubmit.length === 0) {
        console.error('[guiThongBao] Không tìm thấy button submit');
        return;
    }

    // Lấy giá trị TRƯỚC khi disable button để tránh timing issues
    const titleEl = $('#title');
    const contentEl = $('#content');
    const targetEl = $('#target');
    const priorityUrgentEl = $('#priorityUrgent');

    // Debug: Kiểm tra element có tồn tại không
    console.log('[guiThongBao] Debug elements:', {
        titleExists: titleEl.length > 0,
        contentExists: contentEl.length > 0,
        targetExists: targetEl.length > 0,
        priorityUrgentExists: priorityUrgentEl.length > 0
    });

    // Lấy giá trị RAW trước khi trim để debug
    // Sử dụng cả jQuery và native DOM để đảm bảo lấy được value
    let tieuDeRaw = null;
    let noiDungRaw = null;
    
    if (titleEl.length > 0) {
        tieuDeRaw = titleEl.val();
        // Fallback: dùng native DOM nếu jQuery không lấy được
        if (tieuDeRaw === null || tieuDeRaw === undefined) {
            const nativeEl = document.getElementById('title');
            if (nativeEl) {
                tieuDeRaw = nativeEl.value;
            }
        }
    }
    
    if (contentEl.length > 0) {
        noiDungRaw = contentEl.val();
        // Fallback: dùng native DOM nếu jQuery không lấy được
        if (noiDungRaw === null || noiDungRaw === undefined) {
            const nativeEl = document.getElementById('content');
            if (nativeEl) {
                noiDungRaw = nativeEl.value;
            }
        }
    }
    
    console.log('[guiThongBao] Raw values (before trim):', {
        tieuDeRaw: tieuDeRaw,
        noiDungRaw: noiDungRaw,
        tieuDeRawType: typeof tieuDeRaw,
        noiDungRawType: typeof noiDungRaw,
        tieuDeRawLength: tieuDeRaw ? String(tieuDeRaw).length : 0,
        noiDungRawLength: noiDungRaw ? String(noiDungRaw).length : 0
    });

    // Lấy giá trị an toàn và trim - đảm bảo convert sang String
    const tieuDe = tieuDeRaw !== null && tieuDeRaw !== undefined ? String(tieuDeRaw).trim() : '';
    const noiDung = noiDungRaw !== null && noiDungRaw !== undefined ? String(noiDungRaw).trim() : '';
    const doiTuong = targetEl.length > 0 ? (targetEl.val() || 'all') : 'all';
    const mucDoUuTien = priorityUrgentEl.length > 0 && priorityUrgentEl.is(':checked') ? 'Khẩn cấp' : 'Bình thường';

    // Debug: Kiểm tra giá trị sau khi trim
    console.log('[guiThongBao] Debug values (after trim):', {
        tieuDe: tieuDe,
        noiDung: noiDung,
        doiTuong: doiTuong,
        mucDoUuTien: mucDoUuTien,
        tieuDeLength: tieuDe.length,
        noiDungLength: noiDung.length
    });

    // Validate dữ liệu TRƯỚC khi disable button
    if (!tieuDe || tieuDe.length === 0) {
        APIUtils.showError('Vui lòng nhập tiêu đề thông báo');
        return;
    }
    if (!noiDung || noiDung.length === 0) {
        APIUtils.showError('Vui lòng nhập nội dung thông báo');
        return;
    }

    const notificationData = {
        tieuDe: tieuDe,
        noiDung: noiDung,
        doiTuong: doiTuong,
        mucDoUuTien: mucDoUuTien
    };

    // Log payload cuối cùng
    console.log('[guiThongBao] Payload gửi lên backend:', JSON.stringify(notificationData, null, 2));

    const originalText = btnSubmit.html();
    btnSubmit.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-2"></i> Đang gửi...');

    try {
        await ThongBaoAPI.create(notificationData);
        
        APIUtils.showSuccess('Đã gửi thông báo thành công!');
        form.reset();
        
        // Reload danh sách sau khi gửi thành công
        await loadThongBao();
    } catch (error) {
        console.error('[guiThongBao] Lỗi:', error);
        
        if (error.message.includes('404') || error.message.includes('Not Found') || error.message.includes('API_ENDPOINT_NOT_FOUND')) {
            APIUtils.showError('API thông báo chưa được triển khai. Vui lòng liên hệ quản trị viên để triển khai backend endpoint /api/thong-bao');
        } else {
            APIUtils.showError('Không thể gửi thông báo: ' + error.message);
        }
    } finally {
        btnSubmit.prop('disabled', false).html(originalText);
    }
}

// Xóa thông báo
async function deleteThongBao(id) {
    if (!id) {
        console.error('[deleteThongBao] ID không hợp lệ');
        return;
    }

    if (!confirm('Bạn có chắc chắn muốn xóa thông báo này?')) {
        return;
    }

    try {
        await ThongBaoAPI.delete(id);
        APIUtils.showSuccess('Đã xóa thông báo thành công');
        await loadThongBao();
    } catch (error) {
        console.error('[deleteThongBao] Lỗi:', error);
        APIUtils.showError('Không thể xóa thông báo: ' + error.message);
    }
}

// Format ngày giờ
function formatDateTime(dateValue) {
    if (!dateValue) return 'N/A';
    
    try {
        const date = new Date(dateValue);
        if (isNaN(date.getTime())) return dateValue;
        
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        
        return `${hours}:${minutes} ${day}/${month}/${year}`;
    } catch (e) {
        return dateValue;
    }
}

// Lấy text hiển thị cho đối tượng
function getTargetText(target) {
    const map = {
        'all': 'Toàn bộ cư dân',
        'blockA': 'Block A',
        'blockB': 'Block B',
        'Toàn bộ cư dân': 'Toàn bộ cư dân',
        'Block A': 'Block A',
        'Block B': 'Block B'
    };
    return map[target] || target || 'Toàn bộ cư dân';
}

// Khởi tạo khi trang load
$(document).ready(function() {
    console.log('[thong-bao.js] Đã load script');
    
    // Kiểm tra api.js đã load chưa
    if (typeof apiRequest === 'undefined') {
        console.error('[thong-bao.js] api.js chưa được load!');
        $('#notiHistoryTable').html('<tr><td colspan="5" class="text-center text-danger">Lỗi: api.js chưa được load</td></tr>');
        return;
    }

    // Kiểm tra APIUtils
    if (typeof APIUtils === 'undefined') {
        console.warn('[thong-bao.js] APIUtils chưa được định nghĩa, sử dụng fallback');
        window.APIUtils = {
            showError: (msg) => alert('Lỗi: ' + msg),
            showSuccess: (msg) => alert('Thành công: ' + msg)
        };
    }

    // Load danh sách thông báo khi trang load
    loadThongBao();

    // Xử lý form gửi thông báo
    const form = $('#notificationForm');
    if (form.length === 0) {
        console.error('[thong-bao.js] Không tìm thấy form #notificationForm');
    } else {
        form.on('submit', function(e) {
            e.preventDefault();
            guiThongBao();
        });
        console.log('[thong-bao.js] Đã gắn event cho form submit');
    }
});
