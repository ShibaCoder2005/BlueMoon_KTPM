SET client_encoding TO 'UTF8';

-- 1. Lập các bảng
-- Bảng Phong
CREATE TABLE Phong (
    soPhong INT PRIMARY KEY,
    dienTich DECIMAL(8,2),
    giaTien DECIMAL(14,2) DEFAULT 0,
    trangThai VARCHAR(20) DEFAULT 'Trong' CHECK (trangThai IN ('Trong', 'DangO', 'BaoTri')),
    ghiChu TEXT
);

-- Bảng HoGiaDinh
CREATE TABLE HoGiaDinh (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    soPhong INT,
    maChuHo VARCHAR(20) NULL, -- Cho phép NULL để nhập sau
    trangThai VARCHAR(10) DEFAULT 'DangO',
    thoiGianBatDauO TIMESTAMP,
    thoiGianKetThucO TIMESTAMP,
    ghiChu TEXT
);

-- Bảng NhanKhau
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
    vaiTro VARCHAR(50) CHECK (vaiTro IN ('Admin', 'KeToan', 'BanQuanLy')),
    email VARCHAR(100),
    dienThoai VARCHAR(20),
    trangThai VARCHAR(20) DEFAULT 'Hoạt động',
    ghiChu TEXT
);

-- Bảng KhoanThu
CREATE TABLE KhoanThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenKhoan VARCHAR(200) NOT NULL,
    loai VARCHAR(50), -- Ví dụ: 'DichVu', 'Dien', 'Nuoc'
    donGia DECIMAL(12,2) DEFAULT 0,
    donViTinh VARCHAR(50), -- m2, khối, tháng
    tinhTheo VARCHAR(50),  -- 'DienTich', 'SoNguoi', 'ChiSo'
    batBuoc BOOLEAN DEFAULT FALSE,
    moTa TEXT
);

-- Bảng DotThu
CREATE TABLE DotThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenDot VARCHAR(150) NOT NULL,
    ngayBatDau DATE,
    ngayKetThuc DATE,
    trangThai VARCHAR(50) DEFAULT 'Mo', -- 'Mo', 'Dong'
    ghiChu TEXT
);

-- Bảng PhieuThu
CREATE TABLE PhieuThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT NOT NULL, 
    maDot INT NOT NULL REFERENCES DotThu(id),
    maTaiKhoan INT,      -- Người lập phiếu
    ngayLap TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tongTien DECIMAL(14,2) DEFAULT 0,      -- Tổng phải nộp
    soTienDaThu DECIMAL(14,2) DEFAULT 0,   -- Thực tế đã nộp
    trangThai VARCHAR(50) DEFAULT 'ChuaThu',
    hinhThucThu VARCHAR(20),
    ngayHoanThanh TIMESTAMP,               -- Tự động điền khi nộp đủ
    ghiChu TEXT,
);

-- Bảng ChiTietThu
CREATE TABLE ChiTietThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maPhieu INT REFERENCES PhieuThu(id) ON DELETE CASCADE,
    maKhoan INT REFERENCES KhoanThu(id),
    soLuong DECIMAL(10,2) DEFAULT 1,
    donGia DECIMAL(12,2),
    thanhTien DECIMAL(14,2), -- Trigger tự tính
    ghiChu TEXT
);

-- Bảng LichSuNopTien
CREATE TABLE LichSuNopTien (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maPhieu INT REFERENCES PhieuThu(id),
    ngayNop TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    soTienNop DECIMAL(14,2) NOT NULL,
    hinhThucNop VARCHAR(50), -- 'Tiền mặt', 'Chuyển khoản'
    maTaiKhoan INT,          -- Nhân viên thu tiền
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

-- Tự động cập nhật CCCD chủ hộ khi nhập thành viên có ID hộ và vai trò chủ hộ
-- 1. Tạo hàm xử lý cho Trigger
CREATE OR REPLACE FUNCTION trg_update_chu_ho()
RETURNS TRIGGER AS $$
BEGIN
    -- Kiểm tra nếu người vừa nhập có vai trò là 'Chủ hộ'
    IF NEW.quanHeVoiChuHo = 'Chủ hộ' THEN
        -- Cập nhật số CCCD của người này vào bảng HoGiaDinh tương ứng
        UPDATE HoGiaDinh 
        SET maChuHo = NEW.soCCCD 
        WHERE id = NEW.maHo;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Tạo Trigger liên kết với bảng NhanKhau
CREATE TRIGGER trigger_update_chu_ho_after_insert
AFTER INSERT OR UPDATE ON NhanKhau
FOR EACH ROW
EXECUTE FUNCTION trg_update_chu_ho();

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
WHERE nk.hieuLuc = TRUE               -- Chỉ lấy bản ghi mới nhất
AND nk.tinhTrang IN ('CuTru', 'TamTru') -- Chỉ lấy những người đang thực tế ở đó
AND hgd.thoiGianKetThucO IS NULL;    -- Hộ gia đình vẫn đang ở (chưa chuyển đi)

-- Update trạng thái phòng thì có thêm hộ gia đình vào ở
CREATE OR REPLACE FUNCTION fn_UpdateStatusPhong()
RETURNS TRIGGER AS $$
BEGIN
    -- Nếu có hộ mới chuyển vào
    IF (TG_OP = 'INSERT') THEN
        UPDATE Phong SET trangThai = 'DangO' WHERE soPhong = NEW.soPhong;
    -- Nếu hộ gia đình kết thúc thời gian ở (chuyển đi)
    ELSIF (TG_OP = 'UPDATE' AND NEW.thoiGianKetThucO IS NOT NULL) THEN
        UPDATE Phong SET trangThai = 'Trong' WHERE soPhong = NEW.soPhong;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_SyncPhongStatus
AFTER INSERT OR UPDATE ON HoGiaDinh
FOR EACH ROW EXECUTE FUNCTION fn_UpdateStatusPhong();

-- Sử dụng TRIGGER để tự động quản lý
-- Khi bạn cập nhật thông tin một nhân khẩu, dùng Trigger để tự động đóng bản ghi cũ.
CREATE OR REPLACE FUNCTION fn_UpdateNhanKhau()
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

CREATE TRIGGER trg_Handle
BEFORE INSERT ON NhanKhau
FOR EACH ROW
WHEN (NEW.hieuLuc = TRUE)
EXECUTE FUNCTION fn_UpdateNhanKhau();

-- Trigger chạy sau khi INSERT vào bảng HoGiaDinh
CREATE TRIGGER trg_AfterInsertHoGiaDinh
AFTER INSERT ON HoGiaDinh
FOR EACH ROW
EXECUTE FUNCTION fn_SyncPhongStatus();

-- Sử dụng TRIGGER để thu phí
-- 1. Trigger cho bảng ChiTietThu
-- A. Tự động tính thanhTien cho mỗi dòng (Số lượng x Đơn giá)
CREATE OR REPLACE FUNCTION fn_ctt_tinh_thanh_tien() 
RETURNS TRIGGER AS $$
BEGIN
    NEW.thanhTien := COALESCE(NEW.soLuong, 0) * COALESCE(NEW.donGia, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_01_ctt_tinh_thanh_tien
BEFORE INSERT OR UPDATE ON ChiTietThu
FOR EACH ROW EXECUTE FUNCTION fn_ctt_tinh_thanh_tien();

-- B. Tự động cập nhật tongTien vào bảng PhieuThu
-- Khi danh sách phí thay đổi, tổng hóa đơn phải thay đổi theo ngay lập tức.
CREATE OR REPLACE FUNCTION fn_pt_cap_nhat_tong_tien() 
RETURNS TRIGGER AS $$
DECLARE
    target_ma_phieu INT;
BEGIN
    target_ma_phieu := COALESCE(NEW.maPhieu, OLD.maPhieu);

    UPDATE PhieuThu 
    SET tongTien = (SELECT COALESCE(SUM(thanhTien), 0) FROM ChiTietThu WHERE maPhieu = target_ma_phieu)
    WHERE id = target_ma_phieu;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_02_pt_cap_nhat_tong_tien
AFTER INSERT OR UPDATE OR DELETE ON ChiTietThu
FOR EACH ROW EXECUTE FUNCTION fn_pt_cap_nhat_tong_tien();

-- 2. Trigger cho bảng LichSuNopTien
-- C. Tự động cập nhật soTienDaThu và trangThai
-- Trigger này sẽ so sánh số tiền đã nộp với tổng tiền phải nộp để đưa ra trạng thái chính xác (Chưa thu, Chưa đủ, Đã thu).
CREATE OR REPLACE FUNCTION fn_pt_cap_nhat_tien_da_thu() 
RETURNS TRIGGER AS $$
DECLARE
    v_tong_phai_nop DECIMAL(14,2);
    v_tong_da_nop DECIMAL(14,2);
    v_ma_phieu INT;
BEGIN
    v_ma_phieu := COALESCE(NEW.maPhieu, OLD.maPhieu);

    -- 1. Tính lại tổng số tiền cư dân đã nộp cho phiếu này từ bảng lịch sử
    SELECT COALESCE(SUM(soTienNop), 0) INTO v_tong_da_nop 
    FROM LichSuNopTien 
    WHERE maPhieu = v_ma_phieu;

    -- 2. Lấy số tiền cần phải nộp để so sánh
    SELECT tongTien INTO v_tong_phai_nop FROM PhieuThu WHERE id = v_ma_phieu;

    -- 3. Cập nhật bảng PhieuThu
    IF v_tong_da_nop >= v_tong_phai_nop AND v_tong_phai_nop > 0 THEN
        UPDATE PhieuThu SET 
            soTienDaThu = v_tong_da_nop, 
            trangThai = 'DaThu', 
            ngayHoanThanh = CURRENT_TIMESTAMP 
        WHERE id = v_ma_phieu;
    ELSIF v_tong_da_nop > 0 THEN
        UPDATE PhieuThu SET 
            soTienDaThu = v_tong_da_nop, 
            trangThai = 'ChuaDu',
            ngayHoanThanh = NULL 
        WHERE id = v_ma_phieu;
    ELSE
        UPDATE PhieuThu SET 
            soTienDaThu = v_tong_da_nop, 
            trangThai = 'ChuaThu',
            ngayHoanThanh = NULL
        WHERE id = v_ma_phieu;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_03_pt_cap_nhat_tien_da_thu
AFTER INSERT OR UPDATE OR DELETE ON LichSuNopTien
FOR EACH ROW EXECUTE FUNCTION fn_pt_cap_nhat_tien_da_thu();