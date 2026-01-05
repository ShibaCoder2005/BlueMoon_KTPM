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

-- Sửa nhân khẩu: Trigger fn_UpdateNhanKhau (Bảng NhanKhau)
CREATE OR REPLACE FUNCTION fn_UpdateNhanKhau()
RETURNS TRIGGER AS $$
BEGIN
    -- Đóng bản ghi cũ của người này (nếu có) trước khi tạo bản ghi mới
    UPDATE NhanKhau 
    SET hieuLuc = FALSE, 
        ngayKetThuc = CURRENT_TIMESTAMP 
    WHERE soCCCD = NEW.soCCCD AND hieuLuc = TRUE;

    -- Thiết lập mặc định cho bản ghi mới để chắc chắn nó có hiệu lực
    NEW.hieuLuc := TRUE;
    NEW.ngayBatDau := CURRENT_TIMESTAMP;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_Handle
BEFORE INSERT ON NhanKhau
FOR EACH ROW
EXECUTE FUNCTION fn_UpdateNhanKhau();

-- Nhóm Trigger tự động tính tiền (Bảng ChiTietThu)
-- Hệ thống sẽ cập nhật khi có thay đổi.

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

CREATE OR REPLACE PROCEDURE pr_TaoHoaDonHangLoat(p_ma_dot INT, p_ma_nv INT)
LANGUAGE plpgsql
AS $$
DECLARE
    r RECORD;
    v_phieu_id INT;
    -- Biến lưu thông tin khoản thu
    v_id_ql INT; v_gia_ql DECIMAL;
    v_id_xe_may INT; v_gia_xe_may DECIMAL;
    v_id_oto INT; v_gia_oto DECIMAL;
    -- Biến đếm xe
    v_count_xe_may INT; v_count_oto INT;
BEGIN
    -- 1. LẤY ID VÀ ĐƠN GIÁ TỪ BẢNG KHOANTHU (Khớp theo tên bạn đã INSERT)
    SELECT id, donGia INTO v_id_ql, v_gia_ql FROM KhoanThu WHERE tenKhoan = 'Phí quản lý chung cư' LIMIT 1;
    SELECT id, donGia INTO v_id_xe_may, v_gia_xe_may FROM KhoanThu WHERE tenKhoan = 'Phí gửi xe máy' LIMIT 1;
    SELECT id, donGia INTO v_id_oto, v_gia_oto FROM KhoanThu WHERE tenKhoan = 'Phí gửi ô tô' LIMIT 1;

    -- 2. DUYỆT QUA TỪNG HỘ ĐANG Ở
    FOR r IN 
        SELECT h.id AS ma_ho_id, p.soPhong, p.dienTich 
        FROM HoGiaDinh h 
        JOIN Phong p ON h.soPhong = p.soPhong 
        WHERE h.thoiGianKetThucO IS NULL
    LOOP
        -- A. Tạo Phiếu Thu gốc
        INSERT INTO PhieuThu (maHo, maDot, maTaiKhoan, trangThai, tongTien)
        VALUES (r.ma_ho_id, p_ma_dot, p_ma_nv, 'ChuaThu', 0)
        RETURNING id INTO v_phieu_id;

        -- B. TÍNH TIỀN PHÒNG (Dịch vụ diện tích)
        -- Lấy diện tích từ bảng Phong nhân với đơn giá từ KhoanThu
        IF v_id_ql IS NOT NULL THEN
            INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien)
            VALUES (v_phieu_id, v_id_ql, r.dienTich, v_gia_ql, r.dienTich * v_gia_ql);
        END IF;

        -- C. TÍNH TIỀN GỬI XE MÁY
        SELECT COUNT(*) INTO v_count_xe_may FROM PhuongTien 
        WHERE maHo = r.ma_ho_id AND loaiXe = 'XeMay';
        
        IF v_count_xe_may > 0 AND v_id_xe_may IS NOT NULL THEN
            INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien)
            VALUES (v_phieu_id, v_id_xe_may, v_count_xe_may, v_gia_xe_may, v_count_xe_may * v_gia_xe_may);
        END IF;

        -- D. TÍNH TIỀN GỬI Ô TÔ
        SELECT COUNT(*) INTO v_count_oto FROM PhuongTien 
        WHERE maHo = r.ma_ho_id AND loaiXe = 'Oto';
        
        IF v_count_oto > 0 AND v_id_oto IS NOT NULL THEN
            INSERT INTO ChiTietThu (maPhieu, maKhoan, soLuong, donGia, thanhTien)
            VALUES (v_phieu_id, v_id_oto, v_count_oto, v_gia_oto, v_count_oto * v_gia_oto);
        END IF;

        -- E. CẬP NHẬT TỔNG TIỀN PHIẾU THU (chỉ tính các khoản thu bắt buộc, bỏ qua tự nguyện)
        UPDATE PhieuThu 
        SET tongTien = (
            SELECT COALESCE(SUM(ct.thanhTien), 0) 
            FROM ChiTietThu ct
            JOIN KhoanThu kt ON ct.maKhoan = kt.id
            WHERE ct.maPhieu = v_phieu_id AND kt.batBuoc = TRUE
        )
        WHERE id = v_phieu_id;

    END LOOP;
END;
$$;