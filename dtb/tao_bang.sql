CREATE TABLE HoGiaDinh (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    soPhong INT,
    dienTich DECIMAL(8,2),
    maChuHo INT,
    ghiChu TEXT,
    thoiGianBatDauO VARCHAR(255),
    thoiGianKetThucO VARCHAR(255)
);

-- Bảng NhanKhau
CREATE TABLE NhanKhau (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    hoTen VARCHAR(200),
    ngaySinh DATE,
    gioiTinh VARCHAR(10),
    soCCCD VARCHAR(20),
    ngheNghiep VARCHAR(100),
    quanHeVoiChuHo VARCHAR(50),
    tinhTrang VARCHAR(20) CHECK (tinhTrang IN ('CuTru', 'TamVang', 'TamTru', 'ChuyenDi')) DEFAULT 'CuTru',
    ghiChu TEXT
);

-- Bảng LichSuNhanKhau
CREATE TABLE LichSuNhanKhau (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maNhanKhau INT,
    loaiBienDong VARCHAR(50),
    ngayBatDau DATE,
    ngayKetThuc DATE,
    ghiChu TEXT,
    nguoiGhi INT
);

-- Bảng TaiKhoan
CREATE TABLE TaiKhoan (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenDangNhap VARCHAR(100) UNIQUE,
    matKhau VARCHAR(255),
    hoTen VARCHAR(200),
    vaiTro VARCHAR(50),
    email VARCHAR(150),
    dienThoai VARCHAR(20),
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
    maHo INT,
    maDot INT,
    maTaiKhoan INT,
    ngayLap TIMESTAMP,
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
    thanhTien DECIMAL(14,2),
    ghiChu TEXT
);

-- Bảng LichSuNopTien
CREATE TABLE LichSuNopTien (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maPhieu INT,
    ngayNop TIMESTAMP,
    soTien DECIMAL(14,2),
    phuongThuc VARCHAR(50),
    nguoiThu INT,
    ghiChu TEXT
);

-- Bảng PhuongTien
CREATE TABLE PhuongTien (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    loaiXe VARCHAR(20),
    bienSo VARCHAR(50),
    tenChuXe VARCHAR(200),
    ngayDangKy DATE,
    ghiChu TEXT
);

-- Bảng ThongKe
CREATE TABLE ThongKe (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    thoiGian TIMESTAMP,
    kieuThongKe VARCHAR(100),
    noiDung TEXT,
    ketQua TEXT
);

-- THÊM CÁC KHÓA NGOẠI (FOREIGN KEY)

ALTER TABLE HoGiaDinh ADD CONSTRAINT fk_hgd_chuho FOREIGN KEY (maChuHo) REFERENCES NhanKhau(id);

ALTER TABLE NhanKhau ADD CONSTRAINT fk_nk_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id);

ALTER TABLE LichSuNhanKhau ADD CONSTRAINT fk_lsnhankhau_nk FOREIGN KEY (maNhanKhau) REFERENCES NhanKhau(id);
ALTER TABLE LichSuNhanKhau ADD CONSTRAINT fk_lsnhankhau_tk FOREIGN KEY (nguoiGhi) REFERENCES TaiKhoan(id);

ALTER TABLE PhieuThu ADD CONSTRAINT fk_pt_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id);
ALTER TABLE PhieuThu ADD CONSTRAINT fk_pt_dot FOREIGN KEY (maDot) REFERENCES DotThu(id);
ALTER TABLE PhieuThu ADD CONSTRAINT fk_pt_tk FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(id);

ALTER TABLE ChiTietThu ADD CONSTRAINT fk_ctt_pt FOREIGN KEY (maPhieu) REFERENCES PhieuThu(id);
ALTER TABLE ChiTietThu ADD CONSTRAINT fk_ctt_khoan FOREIGN KEY (maKhoan) REFERENCES KhoanThu(id);

ALTER TABLE LichSuNopTien ADD CONSTRAINT fk_lsnt_phieu FOREIGN KEY (maPhieu) REFERENCES PhieuThu(id);
ALTER TABLE LichSuNopTien ADD CONSTRAINT fk_lsnt_tk FOREIGN KEY (nguoiThu) REFERENCES TaiKhoan(id);

ALTER TABLE PhuongTien ADD CONSTRAINT fk_ptien_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id);

-- 1. Bổ sung cột 'trangThai' cho bảng TaiKhoan
-- Mục đích: Để quản lý trạng thái đăng nhập (Hoạt động/Bị khóa)
ALTER TABLE TaiKhoan 
ADD COLUMN trangThai VARCHAR(50) DEFAULT 'Hoạt động';

-- 2. Bổ sung cột 'maHo' cho bảng HoGiaDinh
-- Mục đích: Để quản lý mã hộ khẩu (ví dụ: "HK001", "P101") thay vì chỉ dùng ID số
ALTER TABLE HoGiaDinh 
ADD COLUMN maHo VARCHAR(50) UNIQUE;

-- (Tùy chọn) Cập nhật dữ liệu mẫu cho cột maHo vừa tạo để tránh bị null
-- Ví dụ: Gán maHo = "HK" + id
UPDATE HoGiaDinh SET maHo = 'HK' || id WHERE maHo IS NULL;

ALTER TABLE HoGiaDinh ADD COLUMN ngayTao DATE DEFAULT CURRENT_DATE;

SELECT * FROM TaiKhoan;
