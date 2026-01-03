-- ============================================
-- FILE XÓA TOÀN BỘ DỮ LIỆU DATABASE (PHƯƠNG ÁN ĐƠN GIẢN)
-- ============================================
-- Xóa từng bảng một, không dùng transaction
-- Chạy từng lệnh một nếu gặp lỗi
-- ============================================

-- Bước 1: Xử lý circular dependency giữa HoGiaDinh và NhanKhau
UPDATE HoGiaDinh SET maChuHo = NULL WHERE maChuHo IS NOT NULL;

-- Bước 2: Xóa các bảng con (có foreign key)
-- Chạy từng lệnh một, nếu bảng không tồn tại thì bỏ qua

-- Xóa Lịch sử nộp tiền (nếu bảng tồn tại)
DELETE FROM LichSuNopTien;

-- Xóa Chi tiết thu
DELETE FROM ChiTietThu;

-- Xóa Phiếu thu
DELETE FROM PhieuThu;

-- Xóa Phương tiện
DELETE FROM PhuongTien;

-- Xóa Lịch sử nhân khẩu
DELETE FROM LichSuNhanKhau;

-- Bước 3: Xóa các bảng chính
-- Xóa Nhân khẩu
DELETE FROM NhanKhau;

-- Xóa Hộ gia đình
DELETE FROM HoGiaDinh;

-- Bước 4: Xóa các bảng độc lập
-- Xóa Đợt thu
DELETE FROM DotThu;

-- Xóa Khoản thu
DELETE FROM KhoanThu;

-- Bước 5: Xóa Tài khoản
-- Lưu ý: Nếu muốn giữ lại tài khoản admin, comment dòng này
DELETE FROM TaiKhoan;

-- ============================================
-- HOẶC SỬ DỤNG TRUNCATE (NHANH HƠN)
-- ============================================
-- Nếu DELETE vẫn gặp lỗi, thử dùng TRUNCATE:

-- UPDATE HoGiaDinh SET maChuHo = NULL WHERE maChuHo IS NOT NULL;
-- TRUNCATE TABLE LichSuNopTien CASCADE;
-- TRUNCATE TABLE ChiTietThu CASCADE;
-- TRUNCATE TABLE PhieuThu CASCADE;
-- TRUNCATE TABLE PhuongTien CASCADE;
-- TRUNCATE TABLE LichSuNhanKhau CASCADE;
-- TRUNCATE TABLE NhanKhau CASCADE;
-- TRUNCATE TABLE HoGiaDinh CASCADE;
-- TRUNCATE TABLE DotThu CASCADE;
-- TRUNCATE TABLE KhoanThu CASCADE;
-- TRUNCATE TABLE TaiKhoan CASCADE;

