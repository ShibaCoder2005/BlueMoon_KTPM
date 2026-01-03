-- ============================================
-- FILE INSERT DANH SÁCH PHÒNG
-- ============================================
-- File này tạo danh sách phòng cho chung cư
-- Định dạng số phòng: 101, 102, ..., 109 (tầng 1)
--                     201, 202, ..., 209 (tầng 2)
--                     ...
-- ============================================

-- Xóa dữ liệu cũ nếu có (để tránh lỗi duplicate)
DELETE FROM Phong;

-- Insert danh sách phòng
-- Giả sử: 5 tầng, mỗi tầng 9 phòng (101-109, 201-209, 301-309, 401-409, 501-509)
-- Diện tích mặc định: 50m², Giá tiền: 5000000 VNĐ/tháng

-- Tầng 1: Phòng 101-109
INSERT INTO Phong (soPhong, dienTich, giaTien, ghiChu) VALUES
(101, 50.00, 5000000, 'Phòng tầng 1'),
(102, 50.00, 5000000, 'Phòng tầng 1'),
(103, 50.00, 5000000, 'Phòng tầng 1'),
(104, 50.00, 5000000, 'Phòng tầng 1'),
(105, 50.00, 5000000, 'Phòng tầng 1'),
(106, 50.00, 5000000, 'Phòng tầng 1'),
(107, 50.00, 5000000, 'Phòng tầng 1'),
(108, 50.00, 5000000, 'Phòng tầng 1'),
(109, 50.00, 5000000, 'Phòng tầng 1');

-- Tầng 2: Phòng 201-209
INSERT INTO Phong (soPhong, dienTich, giaTien, ghiChu) VALUES
(201, 50.00, 5000000, 'Phòng tầng 2'),
(202, 50.00, 5000000, 'Phòng tầng 2'),
(203, 50.00, 5000000, 'Phòng tầng 2'),
(204, 50.00, 5000000, 'Phòng tầng 2'),
(205, 50.00, 5000000, 'Phòng tầng 2'),
(206, 50.00, 5000000, 'Phòng tầng 2'),
(207, 50.00, 5000000, 'Phòng tầng 2'),
(208, 50.00, 5000000, 'Phòng tầng 2'),
(209, 50.00, 5000000, 'Phòng tầng 2');

-- Tầng 3: Phòng 301-309
INSERT INTO Phong (soPhong, dienTich, giaTien, ghiChu) VALUES
(301, 50.00, 5000000, 'Phòng tầng 3'),
(302, 50.00, 5000000, 'Phòng tầng 3'),
(303, 50.00, 5000000, 'Phòng tầng 3'),
(304, 50.00, 5000000, 'Phòng tầng 3'),
(305, 50.00, 5000000, 'Phòng tầng 3'),
(306, 50.00, 5000000, 'Phòng tầng 3'),
(307, 50.00, 5000000, 'Phòng tầng 3'),
(308, 50.00, 5000000, 'Phòng tầng 3'),
(309, 50.00, 5000000, 'Phòng tầng 3');

-- Tầng 4: Phòng 401-409
INSERT INTO Phong (soPhong, dienTich, giaTien, ghiChu) VALUES
(401, 50.00, 5000000, 'Phòng tầng 4'),
(402, 50.00, 5000000, 'Phòng tầng 4'),
(403, 50.00, 5000000, 'Phòng tầng 4'),
(404, 50.00, 5000000, 'Phòng tầng 4'),
(405, 50.00, 5000000, 'Phòng tầng 4'),
(406, 50.00, 5000000, 'Phòng tầng 4'),
(407, 50.00, 5000000, 'Phòng tầng 4'),
(408, 50.00, 5000000, 'Phòng tầng 4'),
(409, 50.00, 5000000, 'Phòng tầng 4');

-- Tầng 5: Phòng 501-509
INSERT INTO Phong (soPhong, dienTich, giaTien, ghiChu) VALUES
(501, 50.00, 5000000, 'Phòng tầng 5'),
(502, 50.00, 5000000, 'Phòng tầng 5'),
(503, 50.00, 5000000, 'Phòng tầng 5'),
(504, 50.00, 5000000, 'Phòng tầng 5'),
(505, 50.00, 5000000, 'Phòng tầng 5'),
(506, 50.00, 5000000, 'Phòng tầng 5'),
(507, 50.00, 5000000, 'Phòng tầng 5'),
(508, 50.00, 5000000, 'Phòng tầng 5'),
(509, 50.00, 5000000, 'Phòng tầng 5');

-- Kiểm tra kết quả
SELECT COUNT(*) as total_phong FROM Phong;
SELECT soPhong, dienTich, giaTien, ghiChu 
FROM Phong 
ORDER BY soPhong;

-- ============================================
-- LƯU Ý
-- ============================================
-- 1. File này tạo 5 tầng, mỗi tầng 9 phòng (tổng 45 phòng)
-- 2. Diện tích mặc định: 50m²
-- 3. Giá tiền mặc định: 5,000,000 VNĐ/tháng
-- 4. Có thể chỉnh sửa số tầng, số phòng mỗi tầng, diện tích, giá tiền theo nhu cầu
-- 5. Để thêm nhiều tầng hơn, copy pattern và thay đổi số tầng (601-609, 701-709, ...)

