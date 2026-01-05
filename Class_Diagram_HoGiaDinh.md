# BIỂU ĐỒ LỚP - MODULE HỘ GIA ĐÌNH
*(File này được tạo tự động bởi generate_mermaid.py)*

---

## Mô tả

Biểu đồ này thể hiện các lớp và mối quan hệ trong module quản lý Hộ gia đình, bao gồm:
- **Models**: HoGiaDinh, Phong, NhanKhau, PhieuThu, ChiTietThu, PhuongTien, LichSuNhanKhau
- **Services**: HoGiaDinhService, NhanKhauService, PhieuThuService và các implementation

---

```mermaid
classDiagram

    %% Models
    class ChiTietThu {
        -id : int
        -maPhieu : int
        -maKhoan : int
        -soLuong : BigDecimal
        -donGia : BigDecimal
        -thanhTien : BigDecimal
        -tenKhoan : String
    }
    class HoGiaDinh {
        -id : int
        -soPhong : int
        -maChuHo : String
        -ghiChu : String
        -thoiGianBatDauO : LocalDateTime
        -thoiGianKetThucO : LocalDateTime
        -tenChuHo : String
    }
    class LichSuNhanKhau {
        -id : int
        -maNhanKhau : int
        -loaiBienDong : String
        -ngayBatDau : LocalDate
        -ngayKetThuc : LocalDate
        -nguoiGhi : int
    }
    class NhanKhau {
        -id : int
        -maHo : int
        -hoTen : String
        -ngaySinh : LocalDate
        -gioiTinh : String
        -soCCCD : String
        -ngheNghiep : String
        -quanHeVoiChuHo : String
    }
    class PhieuThu {
        -id : int
        -maHo : int
        -maDot : int
        -maTaiKhoan : int
        -ngayLap : LocalDateTime
        -tongTien : BigDecimal
        -trangThai : String
        -hinhThucThu : String
    }
    class Phong {
        -soPhong : int
        -dienTich : BigDecimal
        -giaTien : BigDecimal
        -ghiChu : String
    }
    class PhuongTien {
        -id : int
        -maHo : int
        -loaiXe : String
        -bienSo : String
        -tenChuXe : String
        -ngayDangKy : LocalDate
    }

    %% Service Interfaces
    class HoGiaDinhService <<interface>> {
    }
    class NhanKhauService <<interface>> {
    }
    class PhieuThuService <<interface>> {
    }

    %% Service Implementations
    class HoGiaDinhServiceImpl {
        -logger : Logger
        -SELECT_ALL : String
        -SELECT_BY_ID : String
        -INSERT : String
        -UPDATE : String
        -DELETE : String
        -CHECK_SOPHONG_EXISTS : String
        -CHECK_SOPHONG_EXISTS_EXCLUDE_ID : String
    }
    class NhanKhauServiceImpl {
        -logger : Logger
        -SELECT_ALL : String
        -SELECT_BY_ID : String
        -SELECT_BY_MAHO : String
        -INSERT : String
        -UPDATE : String
        -UPDATE_STATUS : String
        -DELETE : String
    }
    class PhieuThuServiceImpl {
        -logger : Logger
        -dotThuService : DotThuService
        -khoanThuService : KhoanThuService
        -hoGiaDinhService : HoGiaDinhService
        -nhanKhauService : NhanKhauService
        -phuongTienService : PhuongTienService
        -chiTietThuService : ChiTietThuService
        -SELECT_ALL : String
    }

    %% Relationships
    HoGiaDinhService <|.. HoGiaDinhServiceImpl : implements
    PhieuThuService <|.. PhieuThuServiceImpl : implements
    NhanKhauService <|.. NhanKhauServiceImpl : implements
    Phong ||--o{ HoGiaDinh : "soPhong"
    NhanKhau ||--o| HoGiaDinh : "maChuHo (soCCCD)"
    HoGiaDinh ||--o{ NhanKhau : "maHo"
    HoGiaDinh ||--o{ PhieuThu : "maHo"
    PhieuThu ||--o{ ChiTietThu : "maPhieu"
    HoGiaDinh ||--o{ PhuongTien : "maHo"
    NhanKhau ||--o{ LichSuNhanKhau : "maNhanKhau"
```
