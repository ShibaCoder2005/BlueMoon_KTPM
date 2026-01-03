-- Thêm unique constraint cho biển số trong bảng PhuongTien
-- Đảm bảo mỗi biển số chỉ được đăng ký một lần

-- Kiểm tra và xóa constraint cũ nếu tồn tại (nếu có)
ALTER TABLE PhuongTien DROP CONSTRAINT IF EXISTS uk_phuongtien_bienso;

-- Thêm unique constraint cho biển số (case-insensitive)
-- Sử dụng UPPER và TRIM để đảm bảo không có biển số trùng (ví dụ: "30A-12345" và "30a-12345")
CREATE UNIQUE INDEX uk_phuongtien_bienso ON PhuongTien (UPPER(TRIM(bienSo)));

-- Hoặc nếu muốn constraint đơn giản hơn (không case-insensitive):
-- ALTER TABLE PhuongTien ADD CONSTRAINT uk_phuongtien_bienso UNIQUE (bienSo);

