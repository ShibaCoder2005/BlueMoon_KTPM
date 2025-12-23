-- ====================================================
-- DATABASE SCRIPT CHO DỰ ÁN QUẢN LÝ CHUNG CƯ BLUEMOON
-- ====================================================

-- 1. BẢNG HỘ GIA ĐÌNH
CREATE TABLE HoGiaDinh (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo VARCHAR(50) UNIQUE, -- Mã hiển thị (VD: P101, HK01)
    soPhong INT,
    dienTich DECIMAL(8,2),
    maChuHo INT, -- Sẽ tạo FK sau khi bảng NhanKhau tồn tại
    trangThai VARCHAR(50) DEFAULT 'DangO', -- (DangO, Trong, DangSuaChua) - Đã bổ sung
    ngayTao DATE DEFAULT CURRENT_DATE, -- Đã bổ sung
    thoiGianBatDauO DATE, -- Đổi sang DATE để dễ truy vấn
    thoiGianKetThucO DATE, -- Đổi sang DATE
    ghiChu TEXT
);

-- 2. BẢNG NHÂN KHẨU
CREATE TABLE NhanKhau (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    hoTen VARCHAR(200), -- Tăng độ dài cho an toàn
    ngaySinh DATE,
    gioiTinh VARCHAR(10),
    soCCCD VARCHAR(20), -- Tăng lên 20 phòng trường hợp số hộ chiếu
    ngheNghiep VARCHAR(100),
    quanHeVoiChuHo VARCHAR(50),
    dienThoai VARCHAR(20), -- Đã bổ sung
    email VARCHAR(150),    -- Đã bổ sung
    tinhTrang VARCHAR(20) CHECK (tinhTrang IN ('CuTru', 'TamVang', 'TamTru', 'ChuyenDi')) DEFAULT 'CuTru',
    ghiChu TEXT,
    CONSTRAINT fk_nk_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id)
);

-- Thêm khóa ngoại cho maChuHo trong bảng HoGiaDinh (vì NhanKhau giờ đã tồn tại)
ALTER TABLE HoGiaDinh ADD CONSTRAINT fk_hgd_chuho FOREIGN KEY (maChuHo) REFERENCES NhanKhau(id);

-- 3. BẢNG TÀI KHOẢN
CREATE TABLE TaiKhoan (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenDangNhap VARCHAR(100) UNIQUE,
    matKhau VARCHAR(255),
    hoTen VARCHAR(200),
    vaiTro VARCHAR(50), -- (Admin, KeToan, CuDan)
    email VARCHAR(150),
    dienThoai VARCHAR(20),
    maNhanKhau INT, -- Đã bổ sung: Link với thông tin dân cư nếu là tài khoản Cư dân
    trangThai VARCHAR(50) DEFAULT 'Hoạt động', -- Đã bổ sung
    ghiChu TEXT,
    CONSTRAINT fk_tk_nk FOREIGN KEY (maNhanKhau) REFERENCES NhanKhau(id)
);

-- 4. BẢNG LỊCH SỬ BIẾN ĐỘNG NHÂN KHẨU
CREATE TABLE LichSuNhanKhau (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maNhanKhau INT,
    loaiBienDong VARCHAR(50), -- (ChuyenDen, ChuyenDi, TamVang...)
    ngayBatDau DATE,
    ngayKetThuc DATE,
    nguoiGhi INT,
    ghiChu TEXT,
    CONSTRAINT fk_lsnk_nk FOREIGN KEY (maNhanKhau) REFERENCES NhanKhau(id),
    CONSTRAINT fk_lsnk_tk FOREIGN KEY (nguoiGhi) REFERENCES TaiKhoan(id)
);

-- 5. BẢNG KHOẢN THU (DANH MỤC PHÍ)
CREATE TABLE KhoanThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenKhoan VARCHAR(200),
    loai VARCHAR(50), -- (PhiDichVu, PhiGuiXe, Dien, Nuoc, TuNguyen...)
    donGia DECIMAL(12,2),
    donViTinh VARCHAR(50), -- (m2, soNguoi, soHo, soKhoi...)
    tinhTheo VARCHAR(50), -- (Thang, Nam, Lan)
    batBuoc BOOLEAN DEFAULT FALSE,
    trangThai VARCHAR(20) DEFAULT 'DangApDung', -- Đã bổ sung: DangApDung / NgungApDung
    moTa TEXT
);

-- 6. BẢNG ĐỢT THU (KỲ THU)
CREATE TABLE DotThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenDot VARCHAR(150), -- (VD: Phí tháng 10/2023)
    ngayBatDau DATE,
    ngayKetThuc DATE,
    trangThai VARCHAR(50) DEFAULT 'Mo', -- (Mo, Dong)
    ghiChu TEXT
);

-- 7. BẢNG PHIẾU THU (HÓA ĐƠN TỔNG)
CREATE TABLE PhieuThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    maDot INT, -- Có thể NULL nếu thu lẻ tẻ
    maTaiKhoan INT, -- Người tạo phiếu
    ngayLap TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    hanNop DATE, -- Đã bổ sung
    tongTien DECIMAL(14,2) DEFAULT 0,
    trangThai VARCHAR(50) DEFAULT 'ChuaThu', -- (ChuaThu, DaThu, Huy)
    hinhThucThu VARCHAR(50), -- (TienMat, ChuyenKhoan)
    ghiChu TEXT,
    CONSTRAINT fk_pt_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id),
    CONSTRAINT fk_pt_dot FOREIGN KEY (maDot) REFERENCES DotThu(id),
    CONSTRAINT fk_pt_tk FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(id)
);

-- 8. BẢNG CHI TIẾT THU (DÒNG TRONG HÓA ĐƠN)
CREATE TABLE ChiTietThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maPhieu INT,
    maKhoan INT,
    soLuong DECIMAL(10,2) DEFAULT 1,
    donGia DECIMAL(12,2),
    thanhTien DECIMAL(14,2),
    ghiChu TEXT,
    CONSTRAINT fk_ctt_pt FOREIGN KEY (maPhieu) REFERENCES PhieuThu(id),
    CONSTRAINT fk_ctt_khoan FOREIGN KEY (maKhoan) REFERENCES KhoanThu(id)
);

-- 9. BẢNG LỊCH SỬ NỘP TIỀN (THANH TOÁN)
CREATE TABLE LichSuNopTien (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maPhieu INT,
    ngayNop TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    soTien DECIMAL(14,2),
    phuongThuc VARCHAR(50),
    nguoiThu INT,
    ghiChu TEXT,
    CONSTRAINT fk_lsnt_phieu FOREIGN KEY (maPhieu) REFERENCES PhieuThu(id),
    CONSTRAINT fk_lsnt_tk FOREIGN KEY (nguoiThu) REFERENCES TaiKhoan(id)
);

-- 10. BẢNG PHƯƠNG TIỆN
CREATE TABLE PhuongTien (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    loaiXe VARCHAR(20), -- (XeMay, Oto, XeDapDien)
    bienSo VARCHAR(50),
    tenChuXe VARCHAR(200),
    ngayDangKy DATE,
    maKhoanThu INT, -- Đã bổ sung: Link đến loại phí gửi xe tương ứng
    ghiChu TEXT,
    CONSTRAINT fk_ptien_ho FOREIGN KEY (maHo) REFERENCES HoGiaDinh(id),
    CONSTRAINT fk_ptien_khoan FOREIGN KEY (maKhoanThu) REFERENCES KhoanThu(id)
);

-- 11. BẢNG THỐNG KÊ (LƯU KẾT QUẢ REPORT)
CREATE TABLE ThongKe (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    thoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    kieuThongKe VARCHAR(100),
    noiDung TEXT,
    ketQua TEXT -- Có thể lưu JSON kết quả
);