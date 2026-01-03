-- ============================================
-- FILE XÓA TOÀN BỘ CẤU TRÚC BẢNG (DROP TABLES)
-- ============================================
-- File này xóa toàn bộ cấu trúc bảng trong database
-- Sau khi chạy file này, có thể chạy lại tao_bang.sql để tạo bảng mới
-- ============================================

-- Xóa các bảng theo thứ tự: bảng con trước, bảng cha sau
-- DROP CASCADE sẽ tự động xóa các foreign key constraints

BEGIN;

-- 1. Xóa các bảng con trước (có Foreign Key)
-- Xóa Lịch sử nộp tiền (phụ thuộc vào PhieuThu và TaiKhoan)
DROP TABLE IF EXISTS LichSuNopTien CASCADE;

-- Xóa Chi tiết thu (phụ thuộc vào PhieuThu và KhoanThu)
DROP TABLE IF EXISTS ChiTietThu CASCADE;

-- Xóa Phiếu thu (phụ thuộc vào HoGiaDinh, DotThu, TaiKhoan)
DROP TABLE IF EXISTS PhieuThu CASCADE;

-- Xóa Phương tiện (phụ thuộc vào HoGiaDinh)
DROP TABLE IF EXISTS PhuongTien CASCADE;

-- Xóa Lịch sử nhân khẩu (phụ thuộc vào NhanKhau và TaiKhoan)
DROP TABLE IF EXISTS LichSuNhanKhau CASCADE;

-- 2. Xóa các bảng chính
-- Xóa Nhân khẩu (phụ thuộc vào HoGiaDinh, và HoGiaDinh phụ thuộc vào NhanKhau - circular)
-- Cần xóa foreign key constraint trước
ALTER TABLE IF EXISTS HoGiaDinh DROP CONSTRAINT IF EXISTS fk_hgd_chuho;
ALTER TABLE IF EXISTS NhanKhau DROP CONSTRAINT IF EXISTS fk_nk_ho;

-- Xóa Nhân khẩu
DROP TABLE IF EXISTS NhanKhau CASCADE;

-- Xóa Hộ gia đình
DROP TABLE IF EXISTS HoGiaDinh CASCADE;

-- 3. Xóa các bảng độc lập
-- Xóa Đợt thu
DROP TABLE IF EXISTS DotThu CASCADE;

-- Xóa Khoản thu
DROP TABLE IF EXISTS KhoanThu CASCADE;

-- Xóa Tài khoản
DROP TABLE IF EXISTS TaiKhoan CASCADE;

COMMIT;

-- ============================================
-- KIỂM TRA KẾT QUẢ
-- ============================================
-- Sau khi chạy script, có thể kiểm tra bằng lệnh sau:
-- SELECT table_name 
-- FROM information_schema.tables 
-- WHERE table_schema = 'public' 
-- AND table_type = 'BASE TABLE'
-- ORDER BY table_name;

-- ============================================
-- LƯU Ý
-- ============================================
-- Sau khi chạy file này, tất cả bảng sẽ bị xóa hoàn toàn
-- Để tạo lại bảng, chạy file: tao_bang.sql

