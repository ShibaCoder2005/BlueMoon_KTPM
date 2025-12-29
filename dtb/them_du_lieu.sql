-- TAIKHOAN
INSERT INTO taikhoan (tenDangNhap, matKhau, hoTen, vaiTro, email, dienThoai)
VALUES
('admin', '123456', 'Nguyễn Quản Trị', 'admin', 'admin@example.com', '0900000001'),
('user1', '123456', 'Nguyễn Văn A', 'Nhân viên', 'a@example.com', '0900000002'),
('user2', '123456', 'Trần Thị B', 'Nhân viên', 'b@example.com', '0900000003'),
('user3', '123456', 'Lê Văn C', 'Kế toán', 'c@example.com', '0900000004');

-- HOGIADINH
INSERT INTO hogiadinh (soPhong, dienTich, maChuHo, ghiChu)
VALUES
(3, 60.5, NULL, 'Khu dân cư A1'),
(5, 120.0, NULL, 'Khu dân cư A2'),
(4, 80.0, NULL, 'Khu dân cư B1'),
(2, 45.0, NULL, 'Khu dân cư B2'),
(6, 150.5, NULL, 'Khu dân cư C1');

-- NHANKHAU
INSERT INTO nhankhau (maHo, hoTen, ngaySinh, gioiTinh, soCCCD, ngheNghiep, quanHeVoiChuHo, tinhTrang)
VALUES
(1, 'Nguyễn Văn A', '1980-01-01', 'Nam', '012345678001', 'Kỹ sư', 'Chủ hộ', 'CuTru'),
(1, 'Trần Thị B', '1982-02-02', 'Nữ', '012345678002', 'Giáo viên', 'Vợ', 'CuTru'),
(1, 'Nguyễn Văn C', '2005-05-05', 'Nam', '012345678003', 'Học sinh', 'Con', 'CuTru'),
(2, 'Lê Văn D', '1975-03-03', 'Nam', '012345678004', 'Công nhân', 'Chủ hộ', 'CuTru'),
(2, 'Lê Thị E', '1978-06-06', 'Nữ', '012345678005', 'Buôn bán', 'Vợ', 'CuTru'),
(3, 'Phạm Văn F', '1990-04-04', 'Nam', '012345678006', 'Tự do', 'Chủ hộ', 'CuTru'),
(4, 'Hoàng Thị G', '1985-07-07', 'Nữ', '012345678007', 'Công chức', 'Chủ hộ', 'CuTru'),
(5, 'Đặng Văn H', '1970-08-08', 'Nam', '012345678008', 'Doanh nhân', 'Chủ hộ', 'CuTru');

UPDATE hogiadinh SET maChuHo = 1 WHERE id = 1;
UPDATE hogiadinh SET maChuHo = 4 WHERE id = 2;
UPDATE hogiadinh SET maChuHo = 6 WHERE id = 3;
UPDATE hogiadinh SET maChuHo = 7 WHERE id = 4;
UPDATE hogiadinh SET maChuHo = 8 WHERE id = 5;

-- KHOANTHU
INSERT INTO khoanthu (tenKhoan, loai, donGia, donViTinh, tinhTheo, batBuoc, moTa)
VALUES
('Phí vệ sinh', 'Dân cư', 20000, 'Tháng', 'Hộ', true, 'Thu hàng tháng'),
('Quỹ đền ơn đáp nghĩa', 'Tự nguyện', 50000, 'Năm', 'Hộ', false, 'Đóng góp xã hội'),
('Phí gửi xe máy', 'Dịch vụ', 10000, 'Tháng', 'Xe', true, 'Thu theo số xe'),
('Phí bảo trì', 'Dân cư', 150000, 'Năm', 'Hộ', true, 'Bảo trì cơ sở hạ tầng');

-- DOTTHU
INSERT INTO dotthu (tenDot, ngayBatDau, ngayKetThuc, trangThai)
VALUES
('Đợt thu tháng 1/2025', '2025-01-01', '2025-01-31', 'Đã đóng'),
('Đợt thu tháng 2/2025', '2025-02-01', '2025-02-28', 'Đã đóng'),
('Đợt thu tháng 3/2025', '2025-03-01', '2025-03-31', 'Mo'),
('Đợt thu năm 2025', '2025-01-01', '2025-12-31', 'Mo');

-- PHIEUTHU
INSERT INTO phieuthu (maHo, maDot, maTaiKhoan, ngayLap, tongTien, trangThai, hinhThucThu)
VALUES
(1, 1, 1, '2025-01-05 08:00:00', 50000, 'DaThu', 'Tiền mặt'),
(2, 1, 2, '2025-01-06 09:00:00', 60000, 'DaThu', 'Chuyển khoản'),
(3, 2, 3, '2025-02-10 08:30:00', 45000, 'DaThu', 'Tiền mặt'),
(4, 3, 2, '2025-03-15 10:00:00', 0, 'ChuaThu', 'Tiền mặt');

-- LICHSUNHANKHAU
INSERT INTO lichsunhankhau (maNhanKhau, loaiBienDong, ngayBatDau, ngayKetThuc, ghiChu, nguoiGhi)
VALUES
(1, 'Đăng ký thường trú', '2020-01-01', NULL, 'Chủ hộ đăng ký mới', 1),
(2, 'Thêm nhân khẩu', '2020-01-05', NULL, 'Vợ chủ hộ', 1),
(3, 'Sinh con', '2005-05-05', NULL, 'Thêm thành viên', 1),
(4, 'Chuyển đến', '2021-01-01', NULL, 'Từ nơi khác', 2);

-- LICHSUNOPTIEN
INSERT INTO lichsunoptien (maPhieu, ngayNop, soTien, phuongThuc, nguoiThu)
VALUES
(1, '2025-01-05 09:00:00', 50000, 'Tiền mặt', 1),
(2, '2025-01-06 10:00:00', 60000, 'Chuyển khoản', 2),
(3, '2025-02-10 11:00:00', 45000, 'Tiền mặt', 3);

-- PHUONGTIEN
INSERT INTO phuongtien (maHo, loaiXe, bienSo, tenChuXe, ngayDangKy)
VALUES
(1, 'Xe máy', '29A1-12345', 'Nguyễn Văn A', '2022-03-03'),
(1, 'Xe đạp', 'N/A', 'Trần Thị B', '2023-05-01'),
(2, 'Xe máy', '30B1-67890', 'Lê Văn D', '2021-07-15'),
(3, 'Ô tô', '31C-99999', 'Phạm Văn F', '2020-11-20');



