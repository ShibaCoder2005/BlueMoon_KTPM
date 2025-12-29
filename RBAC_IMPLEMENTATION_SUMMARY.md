# RBAC Implementation Summary

## Đã hoàn thành

### 1. Backend (Java/Javalin)

#### Files đã tạo:
- `src/main/java/com/bluemoon/utils/UserRole.java` - Enum định nghĩa 3 roles: ADMIN, MANAGEMENT, ACCOUNTANT
- `src/main/java/com/bluemoon/utils/AccessManager.java` - Utility class để kiểm tra quyền truy cập

#### Files đã cập nhật:
- `src/main/java/com/bluemoon/WebServer.java`:
  - Import `AccessManager` và `UserRole`
  - Thêm helper methods: `requireFinancialAccess()`, `requireResidentAccess()`, `requireAdminAccess()`
  - Bảo vệ các endpoints:
    - **Khoản thu** (khoan-thu): Chỉ Admin và Accountant
    - **Đợt thu** (dot-thu): Chỉ Admin và Accountant
    - **Phiếu thu** (phieu-thu): Chỉ Admin và Accountant (một số endpoints đã được bảo vệ)
    - **Phương tiện** (phuong-tien): Chỉ Admin và Management (GET endpoint đã được bảo vệ)

### 2. Frontend (JavaScript/HTML)

#### Files đã cập nhật:
- `src/main/resources/js/api.js`:
  - Cập nhật `apiRequest()` để gửi role trong header `X-User-Role`
  - Thêm các utility functions vào `APIUtils`:
    - `getCurrentUserRole()` - Lấy role từ sessionStorage
    - `hasRole(role)` - Kiểm tra role cụ thể
    - `isAdmin()`, `isManagement()`, `isAccountant()` - Kiểm tra từng role
    - `canAccessFinancial()`, `canAccessResident()` - Kiểm tra quyền module
    - `redirectIfNoAccess(requiredRole, redirectUrl)` - Redirect nếu không có quyền

- `src/main/resources/index.html`:
  - Thêm `data-role-access` attributes cho menu items
  - Thêm function `applyRBAC()` để ẩn/hiện buttons và menu items
  - Ẩn "Tạo khoản thu mới" cho Management
  - Ẩn "Xuất danh sách cư dân" cho Accountant

## Cần hoàn thiện

### 1. Backend - Bảo vệ các endpoints còn lại

Cần thêm `requireFinancialAccess()` hoặc `requireResidentAccess()` vào các endpoints sau:

#### PhieuThu endpoints (còn lại):
- `GET /api/phieu-thu/{id}/chi-tiet`
- `GET /api/phieu-thu/ho-gia-dinh/{maHo}`
- `GET /api/phieu-thu/dot-thu/{maDotThu}`
- `POST /api/phieu-thu`
- `POST /api/phieu-thu/with-details`
- `POST /api/phieu-thu/chi-tiet`
- `GET /api/phieu-thu/calculate-total`
- `PUT /api/phieu-thu/{id}`
- `PUT /api/phieu-thu/{id}/status`
- `DELETE /api/phieu-thu/{id}`
- `POST /api/phieu-thu/generate/{maDot}`
- `GET /api/phieu-thu/ho-gia-dinh/{maHo}/unpaid`
- `POST /api/phieu-thu/batch`
- `GET /api/phieu-thu/{id}/detail`
- `GET /api/phieu-thu/{id}/export`

#### PhuongTien endpoints (còn lại):
- `GET /api/phuong-tien/{id}`
- `GET /api/phuong-tien/ho-gia-dinh/{maHo}`
- `GET /api/phuong-tien/search/{keyword}`
- `POST /api/phuong-tien`
- `PUT /api/phuong-tien/{id}`
- `DELETE /api/phuong-tien/{id}`

#### HoGiaDinh endpoints:
- Tất cả endpoints cần `requireResidentAccess()`

#### NhanKhau endpoints:
- Tất cả endpoints cần `requireResidentAccess()`

### 2. Frontend - Áp dụng RBAC cho tất cả các trang

Cần cập nhật các trang HTML sau để:
- Thêm `data-role-access` attributes cho menu items
- Thêm `applyRBAC()` function
- Thêm redirect logic khi user không có quyền truy cập

#### Các trang cần cập nhật:
- `can-ho.html` - Thêm redirect nếu không phải Admin/Management
- `danh-sach-dan-cu.html` - Thêm redirect nếu không phải Admin/Management
- `phuong-tien.html` - Thêm redirect nếu không phải Admin/Management
- `khoan-thu.html` - Thêm redirect nếu không phải Admin/Accountant
- `dot-thu.html` - Thêm redirect nếu không phải Admin/Accountant
- `phieu-thu.html` - Thêm redirect nếu không phải Admin/Accountant
- `bao-cao.html` - Thêm redirect nếu không phải Admin/Accountant
- `thong-ke.html` - Có thể truy cập bởi tất cả, nhưng filter data dựa trên role

### 3. Thống kê (Statistics) - Filter theo role

- Management: Chỉ hiển thị thống kê liên quan đến cư dân
- Accountant: Chỉ hiển thị thống kê liên quan đến tài chính
- Admin: Hiển thị tất cả

## Cách sử dụng

### Backend:
```java
// Bảo vệ endpoint tài chính
app.get("/api/khoan-thu", ctx -> {
    if (!requireFinancialAccess(ctx)) return;
    // ... handler code
});

// Bảo vệ endpoint cư dân
app.get("/api/ho-gia-dinh", ctx -> {
    if (!requireResidentAccess(ctx)) return;
    // ... handler code
});
```

### Frontend:
```javascript
// Kiểm tra quyền trước khi truy cập trang
if (!APIUtils.canAccessFinancial()) {
    APIUtils.redirectIfNoAccess('financial', 'index.html');
}

// Ẩn/hiện elements
if (APIUtils.isManagement() && !APIUtils.isAdmin()) {
    $('#btnCreateFee').hide();
}
```

## Notes

- Role được gửi từ frontend trong header `X-User-Role`
- AccessManager lấy role từ header hoặc body/query params (backward compatibility)
- Frontend lưu role trong sessionStorage với key `currentUser.vaiTro` hoặc `currentUser.role`
- Admin có quyền truy cập tất cả modules

