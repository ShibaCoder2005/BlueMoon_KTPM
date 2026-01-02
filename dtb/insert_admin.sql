-- ============================================
-- FILE INSERT TÀI KHOẢN ADMIN
-- ============================================
-- File này tạo tài khoản admin mặc định cho hệ thống
-- ============================================

-- Xóa tài khoản admin cũ nếu tồn tại (để tránh lỗi duplicate)
DELETE FROM TaiKhoan WHERE tenDangNhap = 'admin';

-- Insert tài khoản admin
-- Lưu ý: Mật khẩu được lưu plain text (theo Helper.hashPassword hiện tại)
-- Nếu cần hash password, cần cập nhật Helper.hashPassword() trước
INSERT INTO TaiKhoan (tenDangNhap, matKhau, hoTen, vaiTro, email, dienThoai, trangThai, ghiChu)
VALUES (
    'admin',                           -- tenDangNhap
    '123456',                          -- matKhau (plain text - có thể hash sau)
    'Quản Trị Viên',                   -- hoTen
    'Admin',                           -- vaiTro (phải là 'Admin', 'KeToan', hoặc 'BanQuanLy')
    'admin@bluemoon.com',              -- email
    '0900000000',                      -- dienThoai
    'Hoạt động',                       -- trangThai
    'Tài khoản quản trị viên hệ thống'  -- ghiChu
);

-- Kiểm tra kết quả
SELECT id, tenDangNhap, hoTen, vaiTro, email, dienThoai, trangThai 
FROM TaiKhoan 
WHERE tenDangNhap = 'admin';

-- ============================================
-- LƯU Ý
-- ============================================
-- 1. Mật khẩu mặc định: admin123
-- 2. Nếu muốn đổi mật khẩu, có thể chạy:
--    UPDATE TaiKhoan SET matKhau = 'matKhauMoi' WHERE tenDangNhap = 'admin';
-- 3. Để hash password, cần cập nhật Helper.hashPassword() và chạy lại script này

