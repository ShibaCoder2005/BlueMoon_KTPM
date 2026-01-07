SET client_encoding TO 'UTF8';

-- ====================================================
-- DỮ LIỆU KIỂM THỬ (SEED DATA) CHO BLUEMOON
-- ====================================================

-- 1. BẢNG TÀI KHOẢN (20 users)
-- Mật khẩu mặc định là '123456' (đã hash hoặc plain text tùy hệ thống, ở đây để plain text cho dễ test)
INSERT INTO TaiKhoan (tenDangNhap, matKhau, hoTen, vaiTro, email, dienThoai, trangThai) VALUES
('admin', '123456', 'Quản Trị Viên', 'Admin', 'admin@bluemoon.vn', '0901000001', 'Hoạt động'),
('ketoan1', '123456', 'Nguyễn Thị Thu', 'KeToan', 'thu.nt@bluemoon.vn', '0901000002', 'Hoạt động'),
('ketoan2', '123456', 'Trần Văn Chi', 'KeToan', 'chi.tv@bluemoon.vn', '0901000003', 'Hoạt động'),
('totruong', '123456', 'Lê Văn Tổ', 'BanQuanLy', 'to.lv@bluemoon.vn', '0901000004', 'Hoạt động');

-- 2. BẢNG PHÒNG
INSERT INTO Phong (soPhong, dienTich, giaTien) VALUES
('501', 50, 1200000000),  
('502', 50, 1000000000), 
('503', 50, 1000000000), 
('504', 50, 1000000000), 
('505', 50, 1000000000), 
('506', 50, 1000000000), 
('507', 50, 1000000000), 
('508', 50, 1000000000), 
('509', 50, 1200000000),
('601', 50, 1200000000),  
('602', 50, 1000000000), 
('603', 50, 1000000000), 
('604', 50, 1000000000), 
('605', 50, 1000000000), 
('606', 50, 1000000000), 
('607', 50, 1000000000), 
('608', 50, 1000000000), 
('609', 50, 1200000000);

-- 5. BẢNG KHOẢN THU (5 khoản)
-- Lưu ý: tinhTheo phải khớp với các giá trị trong frontend: "Diện tích", "Nhân khẩu", "Hộ khẩu", "Xe máy", "Ô tô"
INSERT INTO KhoanThu (tenKhoan, loai, donGia, donViTinh, tinhTheo, batBuoc, moTa) VALUES
('Phí quản lý chung cư', 'PhiDichVu', 7000, 'VNĐ/m²', 'Diện tích', TRUE, 'Phí vận hành, vệ sinh, an ninh'),
('Phí gửi xe máy', 'PhiGuiXe', 100000, 'VNĐ/xe máy', 'Xe máy', TRUE, 'Vé tháng xe máy'),
('Phí gửi ô tô', 'PhiGuiXe', 1200000, 'VNĐ/ô tô', 'Ô tô', TRUE, 'Vé tháng ô tô'),
('Quỹ vì người nghèo', 'TuNguyen', 0, 'VNĐ/hộ', 'Hộ khẩu', FALSE, 'Ủng hộ tự nguyện'),
('Tiền nước sinh hoạt', 'DienNuoc', 0, 'VNĐ/người', 'Nhân khẩu', TRUE, 'Theo chỉ số đồng hồ');

-- -- ================================================================================================= --
-- -- 3. BẢNG HỘ GIA ĐÌNH (8 hộ)
-- -- Lưu ý: maChuHo để NULL trước, update sau khi có NhanKhau
-- INSERT INTO HoGiaDinh (soPhong, trangThai, thoiGianBatDauO) VALUES
-- (501, 'DangO', '2025-01-01'),
-- (502, 'DangO', '2025-01-05'),
-- (503, 'DangO', '2025-01-10'),
-- (504, 'DangO', '2025-01-15'),
-- (505, 'DangO', '2025-01-20'),
-- (506, 'DangO', '2025-02-01'),
-- (507, 'DangO', '2025-02-05'),
-- (508, 'DangO', '2025-02-10');

-- -- 4. BẢNG NHÂN KHẨU (16 người)
-- INSERT INTO NhanKhau (maHo, hoTen, ngaySinh, gioitinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang, ngayBatDau, nguoiGhi) VALUES
-- -- Hộ 501
-- (1, 'Phạm Văn Nhất', '1980-01-01', 'Nam', '001080000001', 'Tự do', 'Chủ hộ', 'CuTru', '2025-01-01', 1),
-- (1, 'Nguyễn Thị A', '1982-02-02', 'Nữ', '001082000002', 'Tự do', 'Vợ', 'CuTru', '2025-01-01', 1),
-- (1, 'Phạm Văn Con', '2010-03-03', 'Nam', '001001001002', 'Tự do', 'Con', 'CuTru', '2025-01-01', 1),
-- -- Hộ 502
-- (2, 'Trần Thị Nhị', '1985-04-04', 'Nữ', '001085000003', 'Tự do', 'Chủ hộ', 'CuTru', '2025-01-05', 1),
-- (2, 'Lê Văn B', '1983-05-05', 'Nam', '001083000004', 'Tự do', 'Chồng', 'CuTru', '2025-01-05', 1),
-- -- Hộ 503
-- (3, 'Lê Văn Tam', '1975-06-06', 'Nam', '001075000005', 'Tự do', 'Chủ hộ', 'CuTru', '2025-01-10', 1),
-- -- Hộ 504
-- (4, 'Hoàng Thị Tứ', '1990-07-07', 'Nữ', '001090000006', 'Tự do', 'Chủ hộ', 'TamTru', '2025-01-15', 4),
-- (4, 'Nguyễn Văn C', '1992-08-08', 'Nam', '001092000007', 'Tự do', 'Chồng', 'TamTru', '2025-01-15', 4),
-- -- Hộ 505
-- (5, 'Vũ Văn Ngũ', '1960-09-09', 'Nam', '001060000008', 'Tự do', 'Chủ hộ', 'CuTru', '2025-01-20', 4),
-- (5, 'Trần Thị D', '1962-10-10', 'Nữ', '001062000009', 'Tự do', 'Vợ', 'CuTru', '2025-01-20', 4),
-- (5, 'Vũ Thị E', '1990-11-11', 'Nữ', '001090000010', 'Tự do', 'Con', 'ChuyenDi', '2025-01-20', 4),
-- -- Hộ 506
-- (6, 'Đặng Thị Lục', '1988-12-12', 'Nữ', '001088000011', 'Tự do', 'Chủ hộ', 'CuTru', '2025-02-01', 1),
-- -- Hộ 507
-- (7, 'Bùi Văn Thất', '1978-01-13', 'Nam', '001078000012', 'Tự do', 'Chủ hộ', 'CuTru', '2025-02-05', 4),
-- (7, 'Phạm Thị F', '1980-02-14', 'Nữ', '001080000013', 'Tự do', 'Vợ', 'CuTru', '2025-02-05', 4),
-- (7, 'Bùi Văn G', '2005-03-15', 'Nam', '001205000014', 'Tự do', 'Con', 'CuTru', '2025-02-05', 4),
-- -- Hộ 508
-- (8, 'Đỗ Thị Bát', '1995-04-16', 'Nữ', '001095000015', 'Tự do', 'Chủ hộ', 'CuTru', '2025-02-10', 1);

-- -- Cập nhật maChuHo cho bảng HoGiaDinh (trigger)

-- -- 6. BẢNG ĐỢT THU (3 đợt)
-- INSERT INTO DotThu (tenDot, ngayBatDau, ngayKetThuc, trangThai) VALUES
-- ('Thu phí tháng 01/2025', '2025-01-01', '2025-01-31', 'Dong'),
-- ('Thu phí tháng 02/2025', '2025-02-01', '2025-02-28', 'Dong'),
-- ('Thu phí tháng 03/2025', '2025-03-01', '2025-03-31', 'Mo');

-- -- 9. BẢNG PHƯƠNG TIỆN (7 xe)
-- INSERT INTO PhuongTien (maHo, loaiXe, bienSo, tenChuXe, ngayDangKy) VALUES
-- (1, 'XeMay', '29-H1 123.45', 'Phạm Văn Nhất', '2025-01-10'),
-- (1, 'XeMay', '29-H1 543.21', 'Nguyễn Thị A', '2025-01-10'),
-- (2, 'Oto', '30A-999.99', 'Trần Thị Nhị', '2025-01-15'),
-- (3, 'XeMay', '29-B1 111.11', 'Lê Văn Tam', '2025-01-20'),
-- (5, 'Oto', '30F-555.66', 'Vũ Văn Ngũ', '2025-02-01'),
-- (7, 'XeMay', '29-C1 222.33', 'Bùi Văn Thất', '2025-02-10'),
-- (7, 'XeMay', '29-C1 444.55', 'Phạm Thị F', '2025-02-10');

-- -- 7, 8. BẢNG PHIẾU THU & CHI TIẾT THU
-- CALL pr_TaoHoaDonHangLoat(3, 2);