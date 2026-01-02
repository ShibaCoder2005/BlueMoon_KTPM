-- 1. Lập các bảng
-- Bảng Phong
CREATE TABLE Phong (
    soPhong INT PRIMARY KEY,
    dienTich DECIMAL(8,2),
    giaTien DECIMAL(14,2) DEFAULT 0,
    ghiChu TEXT
);

-- Bảng HoGiaDinh
CREATE TABLE HoGiaDinh (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    soPhong INT,
    maChuHo VARCHAR(20) NULL, -- Cho phép NULL để nhập sau
    ghiChu TEXT,
    thoiGianBatDauO TIMESTAMP,
    thoiGianKetThucO TIMESTAMP
);

-- Bảng NhanKhau (SCD Type 2)
CREATE TABLE NhanKhau (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    hoTen VARCHAR(100),
    ngaySinh DATE,
    gioiTinh VARCHAR(10),
    soCCCD VARCHAR(20) UNIQUE,
    ngheNghiep VARCHAR(20),
    quanHeVoiChuHo VARCHAR(20),
    tinhTrang VARCHAR(10) CHECK (tinhTrang IN ('CuTru', 'TamVang', 'TamTru', 'ChuyenDi')) DEFAULT 'CuTru',
    ngayBatDau TIMESTAMP,
    ngayKetThuc TIMESTAMP DEFAULT NULL,
    hieuLuc BOOLEAN DEFAULT TRUE,
    nguoiGhi INT,
    ghiChu TEXT
);

-- Bảng TaiKhoan
CREATE TABLE TaiKhoan (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenDangNhap VARCHAR(100) UNIQUE,
    matKhau VARCHAR(255),
    hoTen VARCHAR(200),
    vaiTro VARCHAR(50) CHECK (vaiTro IN ('Admin', 'KeToan', 'NhanVien', 'NguoiDung')),
    email VARCHAR(100),
    dienThoai VARCHAR(20),
    trangThai VARCHAR(20) DEFAULT 'Hoạt động',
    ghiChu TEXT
);

-- Bảng KhoanThu
CREATE TABLE KhoanThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenKhoan VARCHAR(200),
    loai VARCHAR(50),
    donGia DECIMAL(12,2),
    donViTinh VARCHAR(50),
    tinhTheo VARCHAR(50),
    batBuoc BOOLEAN DEFAULT FALSE,
    moTa TEXT
);

-- Bảng DotThu
CREATE TABLE DotThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenDot VARCHAR(150),
    ngayBatDau DATE,
    ngayKetThuc DATE,
    trangThai VARCHAR(50) DEFAULT 'Mo',
    ghiChu TEXT
);

-- Bảng PhieuThu
CREATE TABLE PhieuThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT, -- Tham chiếu đến ID của HoGiaDinh
    maDot INT,
    maTaiKhoan INT,
    ngayLap TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tongTien DECIMAL(14,2) DEFAULT 0,
    trangThai VARCHAR(50) DEFAULT 'ChuaThu',
    hinhThucThu VARCHAR(50),
    ghiChu TEXT
);

-- Bảng ChiTietThu
CREATE TABLE ChiTietThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maPhieu INT,
    maKhoan INT,
    soLuong DECIMAL(10,2) DEFAULT 1,
    donGia DECIMAL(12,2),
    thanhTien DECIMAL(14,2) GENERATED ALWAYS AS (soLuong * donGia) STORED,
    ghiChu TEXT
);

-- Bảng PhuongTien
CREATE TABLE PhuongTien (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    loaiXe VARCHAR(20),
    bienSo VARCHAR(50),
    tenChuXe VARCHAR(100),
    ngayDangKy DATE,
    ghiChu TEXT
);

-- THÊM CÁC KHÓA NGOẠI (FOREIGN KEY)
ALTER TABLE HoGiaDinh ADD CONSTRAINT fk_hgd_phong FOREIGN KEY (soPhong) REFERENCES Phong(soPhong);
ALTER TABLE HoGiaDinh ADD CONSTRAINT fk_hgd_chuho FOREIGN KEY (maChuHo) REFERENCES NhanKhau(soCCCD);

ALTER TABLE NhanKhau ADD CONSTRAINT fk_nk_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id);
ALTER TABLE NhanKhau ADD CONSTRAINT fk_nk_tk FOREIGN KEY (nguoiGhi) REFERENCES TaiKhoan(id);

ALTER TABLE PhieuThu ADD CONSTRAINT fk_pt_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id);
ALTER TABLE PhieuThu ADD CONSTRAINT fk_pt_dot FOREIGN KEY (maDot) REFERENCES DotThu(id);
ALTER TABLE PhieuThu ADD CONSTRAINT fk_pt_tk FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(id);

ALTER TABLE ChiTietThu ADD CONSTRAINT fk_ctt_pt FOREIGN KEY (maPhieu) REFERENCES PhieuThu(id);
ALTER TABLE ChiTietThu ADD CONSTRAINT fk_ctt_khoan FOREIGN KEY (maKhoan) REFERENCES KhoanThu(id);

ALTER TABLE PhuongTien ADD CONSTRAINT fk_ptien_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id);

-- Thêm unique constraint cho biển số trong bảng PhuongTien
-- Đảm bảo mỗi biển số chỉ được đăng ký một lần
-- Kiểm tra và xóa constraint cũ nếu tồn tại (nếu có)
ALTER TABLE PhuongTien DROP CONSTRAINT IF EXISTS uk_phuongtien_bienso;

-- Thêm unique constraint cho biển số
-- Sử dụng UPPER và TRIM để đảm bảo không có biển số trùng (ví dụ: "30A-12345" và "30a-12345")
CREATE UNIQUE INDEX uk_phuongtien_bienso ON PhuongTien (UPPER(TRIM(bienSo)));

-- ===================================================================================================================
-- 2. Thêm các index (nếu cần)
-- Index cho bảng NhanKhau
-- Lọc cư dân theo hộ gia đình hoặc tìm những người "đang ở" (hieuLuc = TRUE).

-- Tìm kiếm nhanh thành viên của một hộ
CREATE INDEX idx_nhankhau_maho ON NhanKhau(maHo);

-- Lấy cư dân hiện tại (hieuLuc = true)
CREATE INDEX idx_nhankhau_hieuluc_true ON NhanKhau(maHo) WHERE hieuLuc = TRUE;

-- Tìm kiếm nhanh theo CCCD (nếu cần)
CREATE INDEX idx_nhankhau_cccd ON NhanKhau(soCCCD);

-- Index cho bảng HoGiaDinh và Phong
-- Các bảng này dùng để tra cứu căn hộ và trạng thái ở.

-- Truy vấn nhanh hộ gia đình theo số phòng
CREATE INDEX idx_hgd_sophong ON HoGiaDinh(soPhong);

-- Tìm các hộ đang còn ở (thoiGianKetThucO IS NULL)
CREATE INDEX idx_hgd_dang_o ON HoGiaDinh(soPhong) WHERE thoiGianKetThucO IS NULL;

-- Index cho bảng Tài chính (PhieuThu, ChiTietThu)
-- Khi số lượng phiếu thu lên đến hàng nghìn bản ghi mỗi năm.

-- Tìm nhanh các phiếu thu của một hộ (Dùng cho chức năng lịch sử nộp tiền)
CREATE INDEX idx_phieuthu_maho ON PhieuThu(maHo);

-- Tìm các phiếu thu chưa thanh toán (Tối ưu cho báo cáo nợ phí)
CREATE INDEX idx_phieuthu_chua_thu ON PhieuThu(trangThai) WHERE trangThai = 'ChuaThu';

-- Liên kết nhanh giữa chi tiết và phiếu thu
CREATE INDEX idx_chitietthu_maphieu ON ChiTietThu(maPhieu);

-- Index cho bảng PhuongTien

-- Tìm xe theo hộ gia đình
CREATE INDEX idx_phuongtien_maho ON PhuongTien(maHo);

-- ==================================================================================================
-- 3. Tạo view, trigger truy vấn

-- Sử dụng VIEW để xem danh sách thành viên hiện tại
-- View này sẽ giúp bạn ẩn đi các bản ghi lịch sử cũ, chỉ hiện ra những người đang thực sự sinh sống tại chung cư kèm theo thông tin phòng của họ.
CREATE OR REPLACE VIEW v_DanhSachCuDanHienTai AS
SELECT 
    nk.id AS nhan_khau_id,
    nk.hoTen,
    nk.soCCCD,
    nk.quanHeVoiChuHo,
    nk.tinhTrang,
    nk.ngayBatDau,
    hgd.soPhong,
    p.dienTich,
-- Kiểm tra xem người này có phải chủ hộ không
CASE WHEN hgd.maChuHo = nk.soCCCD THEN 'Chủ hộ' ELSE 'Thành viên' END AS vaiTroTrongHo
FROM NhanKhau nk
JOIN HoGiaDinh hgd ON nk.maHo = hgd.id
JOIN Phong p ON hgd.soPhong = p.soPhong
WHERE nk.hieuLuc = TRUE               -- Chỉ lấy bản ghi mới nhất (SCD2)
AND nk.tinhTrang IN ('CuTru', 'TamTru') -- Chỉ lấy những người đang thực tế ở đó
AND hgd.thoiGianKetThucO IS NULL;    -- Hộ gia đình vẫn đang ở (chưa chuyển đi)


-- Sử dụng TRIGGER để tự động quản lý SCD Type 2
-- Khi bạn cập nhật thông tin một nhân khẩu (ví dụ: đổi từ Tạm Trú sang Cư Trú), dùng Trigger để tự động đóng bản ghi cũ.
CREATE OR REPLACE FUNCTION fn_UpdateNhanKhauSCD2()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Đóng bản ghi cũ trước khi chèn bản ghi mới
    UPDATE NhanKhau 
    SET hieuLuc = FALSE, 
        ngayKetThuc = CURRENT_TIMESTAMP 
    WHERE soCCCD = NEW.soCCCD AND hieuLuc = TRUE;

    -- 2. Bản ghi mới sẽ mặc định có hieuLuc = TRUE (do DEFAULT trong bảng)
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_HandleSCD2
BEFORE INSERT ON NhanKhau
FOR EACH ROW
WHEN (NEW.hieuLuc = TRUE)
EXECUTE FUNCTION fn_UpdateNhanKhauSCD2();

-- Sử dụng TRIGGER để tính tổng tiền Phiếu Thu
-- Mỗi khi bạn thêm một món đồ vào ChiTietThu, Trigger này sẽ tự động cập nhật tongTien ở bảng PhieuThu.
CREATE OR REPLACE FUNCTION fn_UpdateTongTienPhieuThu()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE PhieuThu 
    SET tongTien = (SELECT SUM(thanhTien) FROM ChiTietThu WHERE maPhieu = NEW.maPhieu)
    WHERE id = NEW.maPhieu;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_AfterInsertChiTiet
AFTER INSERT OR UPDATE ON ChiTietThu
FOR EACH ROW
EXECUTE FUNCTION fn_UpdateTongTienPhieuThu();