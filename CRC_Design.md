# THIẾT KẾ CHI TIẾT CÁC LỚP THEO SƠ ĐỒ CRC
## Hệ thống Quản lý Chung cư BlueMoon

---

## 1. PACKAGE: models

### Class: HoGiaDinh
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin về hộ gia đình trong chung cư
- Quản lý thông tin: số phòng, chủ hộ, thời gian ở
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- Phong (tham chiếu qua soPhong)
- NhanKhau (tham chiếu qua maChuHo = soCCCD)
- HoGiaDinhService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private int soPhong;
- private String maChuHo;
- private String ghiChu;
- private LocalDateTime thoiGianBatDauO;
- private LocalDateTime thoiGianKetThucO;
- private String tenChuHo; (helper field)

---

### Class: NhanKhau
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin cá nhân của nhân khẩu
- Quản lý lịch sử thay đổi (SCD Type 2) với ngayBatDau, ngayKetThuc, hieuLuc
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- HoGiaDinh (tham chiếu qua maHo)
- TaiKhoan (tham chiếu qua nguoiGhi)
- NhanKhauService (được sử dụng bởi)
- LichSuNhanKhau (liên kết với)

**Thuộc tính (Attributes):**
- private int id;
- private int maHo;
- private String hoTen;
- private LocalDate ngaySinh;
- private String gioiTinh;
- private String soCCCD;
- private String ngheNghiep;
- private String quanHeVoiChuHo;
- private String tinhTrang;
- private LocalDateTime ngayBatDau;
- private LocalDateTime ngayKetThuc;
- private boolean hieuLuc;
- private Integer nguoiGhi;
- private String ghiChu;

---

### Class: PhieuThu
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin phiếu thu tiền của hộ gia đình
- Quản lý trạng thái thanh toán và tổng tiền
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- HoGiaDinh (tham chiếu qua maHo)
- DotThu (tham chiếu qua maDot)
- TaiKhoan (tham chiếu qua maTaiKhoan)
- ChiTietThu (quan hệ 1-nhiều)
- PhieuThuService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private int maHo;
- private int maDot;
- private int maTaiKhoan;
- private LocalDateTime ngayLap;
- private BigDecimal tongTien;
- private String trangThai;
- private String hinhThucThu;

---

### Class: ChiTietThu
**Trách nhiệm (Responsibilities):**
- Lưu trữ chi tiết các khoản thu trong một phiếu thu
- Quản lý số lượng, đơn giá (thanhTien là GENERATED column)
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- PhieuThu (tham chiếu qua maPhieu)
- KhoanThu (tham chiếu qua maKhoan)
- ChiTietThuService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private int maPhieu;
- private int maKhoan;
- private BigDecimal soLuong;
- private BigDecimal donGia;
- private BigDecimal thanhTien; (GENERATED)
- private String ghiChu;
- private String tenKhoan; (helper field)

---

### Class: KhoanThu
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin các khoản thu (phí dịch vụ)
- Quản lý đơn giá, đơn vị tính, cách tính (tinhTheo)
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- ChiTietThu (tham chiếu qua maKhoan)
- KhoanThuService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private String tenKhoan;
- private String loai;
- private BigDecimal donGia;
- private String donViTinh;
- private String tinhTheo;
- private boolean batBuoc;
- private String moTa;

---

### Class: DotThu
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin đợt thu tiền
- Quản lý thời gian và trạng thái đợt thu
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- PhieuThu (tham chiếu qua maDot)
- DotThuService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private String tenDot;
- private LocalDate ngayBatDau;
- private LocalDate ngayKetThuc;
- private String trangThai;

---

### Class: TaiKhoan
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin tài khoản người dùng
- Quản lý xác thực và phân quyền
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- AuthService (được sử dụng bởi)
- TaiKhoanService (được sử dụng bởi)
- NhanKhau (tham chiếu qua nguoiGhi)
- PhieuThu (tham chiếu qua maTaiKhoan)

**Thuộc tính (Attributes):**
- private int id;
- private String tenDangNhap;
- private String matKhau;
- private String hoTen;
- private String vaiTro;
- private String email;
- private String dienThoai;
- private String trangThai;
- private String ghiChu;

---

### Class: Phong
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin về các phòng trong chung cư
- Quản lý diện tích và giá tiền phòng
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- HoGiaDinh (tham chiếu qua soPhong)

**Thuộc tính (Attributes):**
- private int soPhong;
- private BigDecimal dienTich;
- private BigDecimal giaTien;
- private String ghiChu;

---

### Class: PhuongTien
**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin phương tiện của cư dân
- Quản lý biển số, loại xe, chủ xe
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- HoGiaDinh (tham chiếu qua maHo)
- PhuongTienService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private int maHo;
- private String loaiXe;
- private String bienSo;
- private String tenChuXe;
- private LocalDate ngayDangKy;
- private String ghiChu;

---

### Class: LichSuNhanKhau
**Trách nhiệm (Responsibilities):**
- Lưu trữ lịch sử biến động của nhân khẩu
- Quản lý loại biến động và thời gian
- Cung cấp getter/setter cho các thuộc tính

**Cộng tác (Collaborators):**
- NhanKhau (tham chiếu qua maNhanKhau)
- NhanKhauService (được sử dụng bởi)

**Thuộc tính (Attributes):**
- private int id;
- private int maNhanKhau;
- private String loaiBienDong;
- private LocalDate ngayBatDau;
- private LocalDate ngayKetThuc;
- private int nguoiGhi;

---

## 2. PACKAGE: services

### Class: HoGiaDinhService (Interface)
**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ quản lý hộ gia đình
- Khai báo các method CRUD và tìm kiếm

**Cộng tác (Collaborators):**
- HoGiaDinh (tham số và giá trị trả về)
- HoGiaDinhServiceImpl (implementation)

**Methods:**
- List<HoGiaDinh> getAllHoGiaDinh();
- HoGiaDinh findById(int id);
- boolean addHoGiaDinh(HoGiaDinh hoGiaDinh);
- boolean updateHoGiaDinh(HoGiaDinh hoGiaDinh);
- boolean deleteHoGiaDinh(int id);
- List<HoGiaDinh> searchHoGiaDinh(String keyword);
- boolean checkMaHoExists(String maHo);

---

### Class: HoGiaDinhServiceImpl
**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý hộ gia đình
- Thực hiện CRUD operations với database
- Validate dữ liệu trước khi lưu

**Cộng tác (Collaborators):**
- HoGiaDinhService (implements)
- HoGiaDinh (xử lý)
- DatabaseConnector (kết nối database)
- NhanKhauService (để resolve tên chủ hộ)

**Dependencies:**
- DatabaseConnector.getConnection()

---

### Class: NhanKhauService (Interface)
**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ quản lý nhân khẩu
- Khai báo các method CRUD, lịch sử, và quản lý SCD Type 2

**Cộng tác (Collaborators):**
- NhanKhau (tham số và giá trị trả về)
- LichSuNhanKhau (tham số và giá trị trả về)
- NhanKhauServiceImpl (implementation)

**Methods:**
- List<NhanKhau> getAll();
- NhanKhau findById(int id);
- List<NhanKhau> getNhanKhauByHoGiaDinh(int maHo);
- boolean addNhanKhau(NhanKhau nhanKhau);
- boolean updateNhanKhau(NhanKhau nhanKhau);
- boolean deleteNhanKhau(int id);
- boolean addLichSuNhanKhau(LichSuNhanKhau history);
- List<LichSuNhanKhau> getLichSuNhanKhau(int maNhanKhau);
- List<LichSuNhanKhau> getAllLichSuNhanKhau();
- boolean isCCCDExists(String soCCCD, int excludeId);
- boolean updateStatusWithHistory(int nhanKhauId, String newStatus, LichSuNhanKhau historyRecord);

---

### Class: NhanKhauServiceImpl
**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý nhân khẩu
- Xử lý SCD Type 2 (Slowly Changing Dimension)
- Quản lý lịch sử biến động nhân khẩu
- Tự động cập nhật maChuHo trong HoGiaDinh khi cần

**Cộng tác (Collaborators):**
- NhanKhauService (implements)
- NhanKhau (xử lý)
- LichSuNhanKhau (xử lý)
- DatabaseConnector (kết nối database)
- HoGiaDinhService (cập nhật maChuHo)

**Dependencies:**
- DatabaseConnector.getConnection()

---

### Class: PhieuThuService (Interface)
**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ quản lý phiếu thu
- Khai báo các method tạo phiếu, thêm chi tiết, tính toán

**Cộng tác (Collaborators):**
- PhieuThu (tham số và giá trị trả về)
- ChiTietThu (tham số và giá trị trả về)
- PhieuThuServiceImpl (implementation)

**Methods:**
- int createPhieuThu(PhieuThu phieuThu);
- int createPhieuThuWithDetails(PhieuThu phieuThu, List<ChiTietThu> chiTietList);
- boolean addChiTietThu(ChiTietThu chiTiet);
- PhieuThu getPhieuThuWithDetails(int maPhieu);
- List<ChiTietThu> getChiTietThuByPhieu(int maPhieu);
- List<PhieuThu> findPhieuThuByHoGiaDinh(int maHo);
- List<PhieuThu> findPhieuThuByDotThu(int maDotThu);
- List<PhieuThu> getAllPhieuThu();
- boolean updatePhieuThu(PhieuThu phieuThu);
- boolean deletePhieuThu(int maPhieu);
- BigDecimal calculateTotalAmountForHousehold(int maHo, int maDot);
- boolean hasUnpaidFees(int maHo);

---

### Class: PhieuThuServiceImpl
**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý phiếu thu
- Tự động tính toán tổng tiền từ chi tiết (trigger xử lý)
- Quản lý transaction khi tạo phiếu với chi tiết
- Tạo chi tiết tự động từ khoản thu bắt buộc

**Cộng tác (Collaborators):**
- PhieuThuService (implements)
- PhieuThu (xử lý)
- ChiTietThu (xử lý)
- ChiTietThuService (sử dụng)
- KhoanThuService (lấy khoản thu bắt buộc)
- HoGiaDinhService (lấy thông tin hộ)
- NhanKhauService (đếm nhân khẩu)
- PhuongTienService (đếm phương tiện)
- DatabaseConnector (kết nối database)

**Dependencies:**
- DatabaseConnector.getConnection()
- ChiTietThuService
- KhoanThuService
- HoGiaDinhService
- NhanKhauService
- PhuongTienService

---

### Class: ThongKeService (Interface)
**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ thống kê và báo cáo
- Khai báo các method thống kê doanh thu, công nợ, nhân khẩu

**Cộng tác (Collaborators):**
- ThongKeServiceImpl (implementation)
- Các Service khác (để tổng hợp dữ liệu)

**Methods:**
- Map<String, Number> getRevenueStats(LocalDate fromDate, LocalDate toDate);
- Map<String, Number> getDebtStats();
- BigDecimal getTotalRevenue(LocalDate fromDate, LocalDate toDate);
- BigDecimal getTotalDebt();
- Map<String, Object> getDashboardStats();
- Map<String, Object> generateCollectionReport(int maDotThu);
- List<Map<String, Object>> getRevenueDetails(LocalDate fromDate, LocalDate toDate);
- List<Map<String, Object>> getDebtDetails();
- Map<String, Object> getResidentDemographics();

---

### Class: ThongKeServiceImpl
**Trách nhiệm (Responsibilities):**
- Triển khai logic thống kê và báo cáo
- Tổng hợp dữ liệu từ nhiều service khác
- Tính toán các chỉ số thống kê

**Cộng tác (Collaborators):**
- ThongKeService (implements)
- PhieuThuService (lấy dữ liệu phiếu thu)
- DotThuService (lấy thông tin đợt thu)
- HoGiaDinhService (lấy thông tin hộ)
- NhanKhauService (lấy thông tin nhân khẩu)

**Dependencies:**
- PhieuThuService
- DotThuService
- HoGiaDinhService
- NhanKhauService

---

## 3. PACKAGE: utils

### Class: WebServer
**Trách nhiệm (Responsibilities):**
- Khởi tạo và quản lý REST API server (Javalin)
- Xử lý HTTP requests và routing
- Chuyển đổi giữa JSON và Java objects
- Xử lý exception và trả về response chuẩn

**Cộng tác (Collaborators):**
- Tất cả các Service interfaces (sử dụng)
- Models (tham số và giá trị trả về)
- ObjectMapper (JSON serialization)

**Dependencies:**
- AuthService
- TaiKhoanService
- KhoanThuService
- DotThuService
- HoGiaDinhService
- NhanKhauService
- PhieuThuService
- ThongKeService
- PhuongTienService

---

### Class: DatabaseConnector
**Trách nhiệm (Responsibilities):**
- Quản lý kết nối đến PostgreSQL database
- Cung cấp method getConnection() cho các service
- Xử lý lỗi kết nối database

**Cộng tác (Collaborators):**
- Tất cả các Service implementations (sử dụng)

**Methods:**
- static Connection getConnection() throws SQLException

**Attributes:**
- private static final String DB_URL
- private static final String DB_USER
- private static final String DB_PASSWORD

---

### Class: Helper
**Trách nhiệm (Responsibilities):**
- Cung cấp các utility methods hỗ trợ
- Xử lý mật khẩu (hash/verify) - hiện tại chỉ cho testing

**Cộng tác (Collaborators):**
- AuthServiceImpl (sử dụng)
- TaiKhoanServiceImpl (sử dụng)

**Methods:**
- static String hashPassword(String password)
- static boolean verifyPassword(String rawPassword, String storedPassword)

---

### Class: UserRole (Enum)
**Trách nhiệm (Responsibilities):**
- Định nghĩa các vai trò người dùng trong hệ thống
- Cung cấp method chuyển đổi string thành enum
- Kiểm tra loại vai trò

**Cộng tác (Collaborators):**
- AccessManager (sử dụng)

**Values:**
- ADMIN ("Admin", "Quản trị viên")
- MANAGEMENT ("BanQuanLy", "Ban quản lý")
- ACCOUNTANT ("KeToan", "Kế toán")

**Methods:**
- static UserRole fromString(String roleStr)
- boolean isAdmin()
- boolean isManagement()
- boolean isAccountant()

---

### Class: AccessManager
**Trách nhiệm (Responsibilities):**
- Quản lý quyền truy cập dựa trên vai trò người dùng
- Lấy role từ request context
- Kiểm tra quyền truy cập endpoint và module

**Cộng tác (Collaborators):**
- UserRole (sử dụng)
- Context (Javalin) (lấy thông tin request)

**Methods:**
- static UserRole getUserRole(Context ctx)
- static boolean hasAccess(Context ctx, UserRole... allowedRoles)
- static boolean requireAccess(Context ctx, UserRole... allowedRoles)
- static boolean canAccessModule(String path, UserRole userRole)

---

## 4. QUAN HỆ GIỮA CÁC LỚP

### Quan hệ Implementation:
- `*ServiceImpl` implements `*Service` interface (11 cặp)

### Quan hệ Dependency:
- `WebServer` → tất cả Service interfaces
- `*ServiceImpl` → `DatabaseConnector`
- `*ServiceImpl` → Models
- `ThongKeServiceImpl` → các Service khác
- `PhieuThuServiceImpl` → nhiều Service khác

### Quan hệ Association:
- `HoGiaDinh` ↔ `Phong` (qua soPhong)
- `HoGiaDinh` ↔ `NhanKhau` (qua maChuHo = soCCCD)
- `NhanKhau` ↔ `HoGiaDinh` (qua maHo)
- `PhieuThu` ↔ `HoGiaDinh` (qua maHo)
- `PhieuThu` ↔ `DotThu` (qua maDot)
- `PhieuThu` ↔ `ChiTietThu` (1-nhiều)
- `ChiTietThu` ↔ `KhoanThu` (qua maKhoan)
- `NhanKhau` ↔ `LichSuNhanKhau` (1-nhiều)

### Quan hệ Composition:
- `PhieuThu` chứa nhiều `ChiTietThu`

---

## 5. DESIGN PATTERNS ĐƯỢC SỬ DỤNG

1. **Service Layer Pattern**: Tách biệt business logic khỏi presentation và data access
2. **DAO Pattern**: Ẩn trong Service implementations
3. **Interface Segregation**: Mỗi domain có service interface riêng
4. **Dependency Injection (Simple)**: WebServer tự khởi tạo services
5. **Template Method**: Các service implementations có cấu trúc tương tự

---

## 6. ĐẶC ĐIỂM THIẾT KẾ

- **Tách biệt rõ ràng**: Interface và Implementation tách riêng
- **Single Responsibility**: Mỗi service quản lý một domain cụ thể
- **Loose Coupling**: WebServer chỉ phụ thuộc vào interfaces
- **Reusability**: Utils classes được tái sử dụng
- **SCD Type 2**: NhanKhau hỗ trợ lưu lịch sử thay đổi
- **Database Triggers**: Sử dụng trigger để tự động tính toán (thanhTien, tongTien)

