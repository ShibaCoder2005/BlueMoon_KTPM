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
    thoiGianBatDauO DATE,
    thoiGianKetThucO DATE DEFAULT NULL,
    ghiChu TEXT
);

-- Bảng NhanKhau
CREATE TABLE NhanKhau (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    hoTen VARCHAR(100),
    ngaySinh DATE,
    gioiTinh VARCHAR(10),
    soCCCD VARCHAR(20) NOT NULL,
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
    ngayLap TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tongTien DECIMAL(14,2) DEFAULT 0,
    trangThai VARCHAR(50) DEFAULT 'ChuaThu' CHECK (trangThai IN ('ChuaThu', 'DaThu')),
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
    thanhTien DECIMAL(14,2) DEFAULT 0,
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

-- Tạo ràng buộc thông minh (Chỉ cấm trùng CCCD với người ĐANG Ở)
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_cccd_active 
ON NhanKhau (soCCCD) 
WHERE hieuLuc = TRUE;

-- Thiết lập luật mới cho Chủ hộ (1 nhà chỉ 1 chủ hộ đang ở)
CREATE UNIQUE INDEX idx_unique_chuho_active 
ON NhanKhau (maHo) 
WHERE quanHeVoiChuHo = 'Chủ hộ' AND hieuLuc = TRUE;

-- ==================================================================================================
-- 3. Tạo view, trigger truy vấn

-- Sửa trạng thái phòng: Trigger fn_UpdateStatusPhong (Bảng HoGiaDinh)
CREATE OR REPLACE FUNCTION fn_UpdateStatusPhong()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Nếu có hộ mới chuyển vào
    IF (TG_OP = 'INSERT') THEN
        UPDATE Phong SET trangThai = 'DangO' WHERE soPhong = NEW.soPhong;
        
    -- 2. Nếu cập nhật thông tin hộ gia đình
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Trường hợp: Hộ gia đình kết thúc thời gian ở (chuyển đi)
        IF (NEW.thoiGianKetThucO IS NOT NULL AND OLD.thoiGianKetThucO IS NULL) THEN
            UPDATE Phong SET trangThai = 'Trong' WHERE soPhong = NEW.soPhong;
            
        -- Trường hợp đặc biệt: Cập nhật đổi số phòng trên cùng 1 ID hộ
        ELSIF (NEW.soPhong <> OLD.soPhong) THEN
            UPDATE Phong SET trangThai = 'Trong' WHERE soPhong = OLD.soPhong;
            UPDATE Phong SET trangThai = 'DangO' WHERE soPhong = NEW.soPhong;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_SyncPhongStatus
AFTER INSERT OR UPDATE ON HoGiaDinh
FOR EACH ROW EXECUTE FUNCTION fn_UpdateStatusPhong();

-- Tự động cập nhật CCCD chủ hộ khi nhập thành viên có ID hộ và vai trò chủ hộ
-- 1. Tạo hàm xử lý cho Trigger
CREATE OR REPLACE FUNCTION trg_update_chu_ho()
RETURNS TRIGGER AS $$
BEGIN
    -- SỬA ĐIỀU KIỆN: Dùng unaccent hoặc so sánh tương đối
    IF LOWER(NEW.quanHeVoiChuHo) LIKE '%chủ hộ%' THEN 
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

-- Danh sách cư dân
-- View này dùng cho bảng "Lịch sử nhân khẩu"
-- VIEW LỊCH SỬ (Bổ sung cột vaiTroTrongHo)
CREATE OR REPLACE VIEW v_LichSuNhanKhauTongHop AS
SELECT 
    nk.id,
    nk.maHo,
    nk.hoTen,
    nk.ngaySinh,
    nk.gioiTinh,
    nk.soCCCD,
    nk.ngheNghiep,
    nk.quanHeVoiChuHo,
    nk.quanHeVoiChuHo AS vaiTroTrongHo, 
    nk.tinhTrang,
    nk.ngayBatDau,
    nk.ngayKetThuc,
    nk.hieuLuc,
    nk.ghiChu,
    nk.nguoiGhi AS nguoiGhiId,
    tk.hoTen AS tenNguoiGhi,
    hgd.soPhong,
    CASE 
        WHEN nk.tinhTrang = 'CuTru' THEN 'Thường trú'
        WHEN nk.tinhTrang = 'TamTru' THEN 'Tạm trú'
        WHEN nk.tinhTrang = 'TamVang' THEN 'Tạm vắng'
        WHEN nk.tinhTrang = 'ChuyenDi' THEN 'Đã chuyển đi'
        WHEN nk.tinhTrang = 'DaMat' THEN 'Đã mất'
        ELSE 'Khác'
    END AS loaiCuTru
FROM NhanKhau nk
LEFT JOIN HoGiaDinh hgd ON nk.maHo = hgd.id
LEFT JOIN TaiKhoan tk ON nk.nguoiGhi = tk.id;

-- VIEW DANH SÁCH HIỆN TẠI (Lọc từ View trên)
CREATE OR REPLACE VIEW v_DanhSachCuDanHienTai AS
SELECT * FROM v_LichSuNhanKhauTongHop
WHERE hieuLuc = TRUE 
AND tinhTrang NOT IN ('ChuyenDi', 'DaMat');

-- Nhóm Trigger tự động tính tiền (Bảng ChiTietThu)
-- Trigger tự động cập nhật tongTien trong PhieuThu khi ChiTietThu thay đổi
-- Chỉ tính các khoản thu bắt buộc (batBuoc = TRUE), bỏ qua khoản thu tự nguyện
CREATE OR REPLACE FUNCTION fn_UpdateTongTienPhieuThu()
RETURNS TRIGGER AS $$
BEGIN
    -- Cập nhật tongTien cho phiếu thu tương ứng
    -- Chỉ tính tổng từ các khoản thu bắt buộc (batBuoc = TRUE)
    UPDATE PhieuThu 
    SET tongTien = (
        SELECT COALESCE(SUM(ct.thanhTien), 0) 
        FROM ChiTietThu ct
        JOIN KhoanThu kt ON ct.maKhoan = kt.id
        WHERE ct.maPhieu = COALESCE(NEW.maPhieu, OLD.maPhieu) 
          AND kt.batBuoc = TRUE
    )
    WHERE id = COALESCE(NEW.maPhieu, OLD.maPhieu);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger cho INSERT, UPDATE, DELETE trên ChiTietThu
CREATE TRIGGER trg_UpdateTongTienAfterChiTietInsert
AFTER INSERT ON ChiTietThu
FOR EACH ROW
EXECUTE FUNCTION fn_UpdateTongTienPhieuThu();

CREATE TRIGGER trg_UpdateTongTienAfterChiTietUpdate
AFTER UPDATE ON ChiTietThu
FOR EACH ROW
EXECUTE FUNCTION fn_UpdateTongTienPhieuThu();

CREATE TRIGGER trg_UpdateTongTienAfterChiTietDelete
AFTER DELETE ON ChiTietThu
FOR EACH ROW
EXECUTE FUNCTION fn_UpdateTongTienPhieuThu();

-- Tạo hoá đơn hàng loạt
CREATE OR REPLACE PROCEDURE pr_TaoHoaDonHangLoat(
    p_id_dot_thu INT,
    p_nguoi_tao_id INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_ho_rec RECORD;
    v_khoan_rec RECORD;
    v_id_phieu INT;
    v_tong_tien DECIMAL(15, 2);
    v_so_luong DECIMAL(10, 2);
    v_thanh_tien DECIMAL(15, 2);
    v_so_xe_may INT;
    v_so_o_to INT;
    v_dien_tich DECIMAL(10, 2);
    -- Đã xóa biến v_so_nhan_khau
BEGIN
    -- Duyệt qua tất cả các hộ gia đình ĐANG Ở
    FOR v_ho_rec IN 
        SELECT h.id, h.soPhong, p.dienTich 
        FROM HoGiaDinh h
        JOIN Phong p ON h.soPhong = p.soPhong
        WHERE h.trangThai = 'DangO'
    LOOP
        -- A. TÍNH TOÁN CHỈ SỐ
        
        -- (Đã xóa phần đếm Nhân khẩu ở đây)
        
        -- 1. Đếm Xe Máy (Logic tìm gần đúng 'XeMay', 'xe máy'...)
        SELECT COUNT(*) INTO v_so_xe_may 
        FROM PhuongTien 
        WHERE maHo = v_ho_rec.id 
        AND (
            loaiXe ILIKE 'XeMay' OR      
            loaiXe ILIKE '%xemay%' OR    
            loaiXe ILIKE '%xe máy%' OR   
            loaiXe ILIKE '%xe may%'      
        );

        -- 2. Đếm Ô tô (Logic tìm gần đúng 'Oto', 'ô tô'...)
        SELECT COUNT(*) INTO v_so_o_to 
        FROM PhuongTien 
        WHERE maHo = v_ho_rec.id 
        AND (
            loaiXe ILIKE 'Oto' OR        
            loaiXe ILIKE '%oto%' OR      
            loaiXe ILIKE '%ô tô%' OR     
            loaiXe ILIKE '%car%'
        );

        -- 3. Lấy diện tích
        v_dien_tich := COALESCE(v_ho_rec.dienTich, 0);

        -- B. TẠO PHIẾU THU (Header)
        INSERT INTO PhieuThu (maHo, maDot, ngayLap, tongTien, trangThai, maTaiKhoan)
        VALUES (v_ho_rec.id, p_id_dot_thu, CURRENT_DATE, 0, 'ChuaThu', p_nguoi_tao_id)
        RETURNING id INTO v_id_phieu;

        v_tong_tien := 0;

        -- C. TẠO CHI TIẾT THU (Detail)
        FOR v_khoan_rec IN SELECT * FROM KhoanThu WHERE batBuoc = TRUE LOOP
            
            v_so_luong := 0;

            -- Logic mapping:
            IF (v_khoan_rec.tinhTheo ILIKE '%diện tích%') THEN
                v_so_luong := v_dien_tich;
            
            -- (Đã xóa nhánh ELSIF tính theo nhân khẩu)
            
            ELSIF (v_khoan_rec.tinhTheo ILIKE '%xe máy%' OR v_khoan_rec.tinhTheo ILIKE '%xemay%') THEN
                v_so_luong := v_so_xe_may;
            ELSIF (v_khoan_rec.tinhTheo ILIKE '%ô tô%' OR v_khoan_rec.tinhTheo ILIKE '%oto%') THEN
                v_so_luong := v_so_o_to;
            ELSE
                -- Mặc định là 1 (Tính theo Hộ)
                -- Các khoản phí "Theo người" giờ sẽ chạy vào đây và tính là 1 suất/hộ
                v_so_luong := 1; 
            END IF;

            -- Chỉ insert nếu có số lượng > 0
            IF v_so_luong > 0 THEN
                v_thanh_tien := v_so_luong * v_khoan_rec.donGia;
                v_tong_tien := v_tong_tien + v_thanh_tien;

                INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien)
                VALUES (v_id_phieu, v_khoan_rec.id, v_so_luong, v_khoan_rec.donGia, v_thanh_tien);
            END IF;

        END LOOP;

        -- D. CẬP NHẬT TỔNG TIỀN
        UPDATE PhieuThu SET tongTien = v_tong_tien WHERE id = v_id_phieu;

    END LOOP;
END;
$$;