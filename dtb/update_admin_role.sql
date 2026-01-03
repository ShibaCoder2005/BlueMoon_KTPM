-- Cập nhật role của tài khoản admin từ "Admin" thành "admin"
UPDATE taikhoan 
SET vaiTro = 'admin' 
WHERE tenDangNhap = 'admin' AND vaiTro = 'Admin';

