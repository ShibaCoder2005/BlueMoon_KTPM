-- ============================================
-- FILE XÓA TOÀN BỘ DỮ LIỆU DATABASE
-- ============================================
-- File này xóa toàn bộ dữ liệu trong các bảng
-- Sử dụng TRUNCATE CASCADE để tự động xử lý Foreign Key Constraints
-- ============================================

-- PHƯƠNG ÁN 1: SỬ DỤNG TRUNCATE CASCADE (KHUYẾN NGHỊ)
-- TRUNCATE CASCADE sẽ tự động xóa dữ liệu từ các bảng con có foreign key
-- Nhanh hơn và an toàn hơn DELETE

BEGIN;

-- Xử lý circular dependency giữa HoGiaDinh và NhanKhau
-- Tạm thời set maChuHo = NULL để có thể TRUNCATE
UPDATE HoGiaDinh SET maChuHo = NULL WHERE maChuHo IS NOT NULL;

-- Xóa dữ liệu từ các bảng có foreign key trước
-- TRUNCATE CASCADE sẽ tự động xóa dữ liệu từ các bảng con
TRUNCATE TABLE LichSuNopTien CASCADE;
TRUNCATE TABLE ChiTietThu CASCADE;
TRUNCATE TABLE PhieuThu CASCADE;
TRUNCATE TABLE PhuongTien CASCADE;
TRUNCATE TABLE LichSuNhanKhau CASCADE;

-- Xóa NhanKhau và HoGiaDinh (đã xử lý circular dependency ở trên)
TRUNCATE TABLE NhanKhau CASCADE;
TRUNCATE TABLE HoGiaDinh CASCADE;

-- Xóa các bảng độc lập
TRUNCATE TABLE DotThu CASCADE;
TRUNCATE TABLE KhoanThu CASCADE;

-- Xóa Tài khoản (Lưu ý: Nếu muốn giữ lại tài khoản admin, comment dòng này)
TRUNCATE TABLE TaiKhoan CASCADE;

COMMIT;

-- ============================================
-- PHƯƠNG ÁN 2: XÓA TỪNG BẢNG ĐỘC LẬP (NẾU TRUNCATE KHÔNG HOẠT ĐỘNG)
-- ============================================
-- Nếu TRUNCATE CASCADE không hoạt động, có thể dùng cách này
-- Xóa từng bảng một, không dùng transaction để tránh lỗi abort
-- 
-- -- Xử lý circular dependency
-- UPDATE HoGiaDinh SET maChuHo = NULL WHERE maChuHo IS NOT NULL;
-- 
-- -- Xóa các bảng con
-- DELETE FROM LichSuNopTien;
-- DELETE FROM ChiTietThu;
-- DELETE FROM PhieuThu;
-- DELETE FROM PhuongTien;
-- DELETE FROM LichSuNhanKhau;
-- DELETE FROM NhanKhau;
-- DELETE FROM HoGiaDinh;
-- DELETE FROM DotThu;
-- DELETE FROM KhoanThu;
-- DELETE FROM TaiKhoan;
-- ============================================

-- ============================================
-- KIỂM TRA KẾT QUẢ
-- ============================================
-- Sau khi chạy script, có thể kiểm tra bằng các lệnh sau:

-- SELECT 'ChiTietThu' as table_name, COUNT(*) as row_count FROM ChiTietThu
-- UNION ALL
-- SELECT 'PhieuThu', COUNT(*) FROM PhieuThu
-- UNION ALL
-- SELECT 'PhuongTien', COUNT(*) FROM PhuongTien
-- UNION ALL
-- SELECT 'LichSuNhanKhau', COUNT(*) FROM LichSuNhanKhau
-- UNION ALL
-- SELECT 'NhanKhau', COUNT(*) FROM NhanKhau
-- UNION ALL
-- SELECT 'HoGiaDinh', COUNT(*) FROM HoGiaDinh
-- UNION ALL
-- SELECT 'DotThu', COUNT(*) FROM DotThu
-- UNION ALL
-- SELECT 'KhoanThu', COUNT(*) FROM KhoanThu
-- UNION ALL
-- SELECT 'TaiKhoan', COUNT(*) FROM TaiKhoan;

