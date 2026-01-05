# THIẾT KẾ CRC TỰ ĐỘNG
## Hệ thống Quản lý Chung cư BlueMoon
*(File này được tạo tự động bởi generate_crc.py)*

---

## PACKAGE: com.bluemoon.models

### Class: BaoCaoCongNo
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/BaoCaoCongNo.java

**Trách nhiệm (Responsibilities):**
- Quản lý BaoCaoCongNo

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int maHo
- int soPhong
- String chuHo
- String tenDot
- int maDot
- BigDecimal tongTien
- BigDecimal daThu
- BigDecimal conNo
- String trangThai
- String ghiChu

**Methods (một số):**
- int getMaHo()
- void setMaHo()
- int getSoPhong()
- void setSoPhong()
- String getChuHo()
- void setChuHo()
- String getTenDot()
- void setTenDot()
- int getMaDot()
- void setMaDot()
- ... và 10 methods khác


---

### Class: BaoCaoThu
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/BaoCaoThu.java

**Trách nhiệm (Responsibilities):**
- Quản lý BaoCaoThu

**Cộng tác (Collaborators):**
- ChiTietThu
- PhieuThu

**Thuộc tính (Attributes):**
- int maHo
- int soPhong
- String chuHo
- LocalDateTime ngayThu
- BigDecimal soTien
- String noiDung
- String tenDot
- String trangThai
- String hinhThucThu

**Methods (một số):**
- int getMaHo()
- void setMaHo()
- int getSoPhong()
- void setSoPhong()
- String getChuHo()
- void setChuHo()
- LocalDateTime getNgayThu()
- void setNgayThu()
- BigDecimal getSoTien()
- void setSoTien()
- ... và 8 methods khác


---

### Class: ChiTietThu
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/ChiTietThu.java

**Trách nhiệm (Responsibilities):**
- Quản lý ChiTietThu

**Cộng tác (Collaborators):**
- Helper

**Thuộc tính (Attributes):**
- int id
- int maPhieu
- int maKhoan
- BigDecimal soLuong
- BigDecimal donGia
- BigDecimal thanhTien
- String tenKhoan

**Methods (một số):**
- int getId()
- void setId()
- int getMaPhieu()
- void setMaPhieu()
- int getMaKhoan()
- void setMaKhoan()
- BigDecimal getSoLuong()
- void setSoLuong()
- BigDecimal getDonGia()
- void setDonGia()
- ... và 4 methods khác


---

### Class: DotThu
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/DotThu.java

**Trách nhiệm (Responsibilities):**
- Quản lý DotThu

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int id
- String tenDot
- LocalDate ngayBatDau
- LocalDate ngayKetThuc
- String trangThai
- String moTa

**Methods (một số):**
- int getId()
- void setId()
- String getTenDot()
- void setTenDot()
- LocalDate getNgayBatDau()
- void setNgayBatDau()
- LocalDate getNgayKetThuc()
- void setNgayKetThuc()
- String getTrangThai()
- void setTrangThai()
- ... và 4 methods khác


---

### Class: HoGiaDinh
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/HoGiaDinh.java

**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin về HoGiaDinh

**Cộng tác (Collaborators):**
- Helper
- NhanKhau
- NhanKhauService
- Phong

**Thuộc tính (Attributes):**
- int id
- int soPhong
- String maChuHo
- String ghiChu
- LocalDateTime thoiGianBatDauO
- LocalDateTime thoiGianKetThucO
- String tenChuHo

**Methods (một số):**
- int getId()
- void setId()
- int getSoPhong()
- void setSoPhong()
- String getMaChuHo()
- void setMaChuHo()
- String getGhiChu()
- void setGhiChu()
- LocalDateTime getThoiGianBatDauO()
- void setThoiGianBatDauO()
- ... và 4 methods khác


---

### Class: KhoanThu
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/KhoanThu.java

**Trách nhiệm (Responsibilities):**
- Quản lý KhoanThu

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int id
- String tenKhoan
- String loai
- BigDecimal donGia
- String donViTinh
- String tinhTheo
- boolean batBuoc
- String moTa
- int loaiKhoanThu

**Methods (một số):**
- int getId()
- void setId()
- String getTenKhoan()
- void setTenKhoan()
- String getLoai()
- void setLoai()
- BigDecimal getDonGia()
- void setDonGia()
- String getDonViTinh()
- void setDonViTinh()
- ... và 14 methods khác


---

### Class: LichSuNhanKhau
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/LichSuNhanKhau.java

**Trách nhiệm (Responsibilities):**
- Quản lý LichSuNhanKhau

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int id
- int maNhanKhau
- String loaiBienDong
- LocalDate ngayBatDau
- LocalDate ngayKetThuc
- int nguoiGhi

**Methods (một số):**
- int getId()
- void setId()
- int getMaNhanKhau()
- void setMaNhanKhau()
- String getLoaiBienDong()
- void setLoaiBienDong()
- LocalDate getNgayBatDau()
- void setNgayBatDau()
- LocalDate getNgayKetThuc()
- void setNgayKetThuc()
- ... và 2 methods khác


---

### Class: NhanKhau
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/NhanKhau.java

**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin về NhanKhau

**Cộng tác (Collaborators):**
- TaiKhoan

**Thuộc tính (Attributes):**
- int id
- int maHo
- String hoTen
- LocalDate ngaySinh
- String gioiTinh
- String soCCCD
- String ngheNghiep
- String quanHeVoiChuHo
- String tinhTrang
- LocalDateTime ngayBatDau
- ... và 4 thuộc tính khác

**Methods (một số):**
- int getId()
- void setId()
- int getMaHo()
- void setMaHo()
- String getHoTen()
- void setHoTen()
- LocalDate getNgaySinh()
- void setNgaySinh()
- String getGioiTinh()
- void setGioiTinh()
- ... và 18 methods khác


---

### Class: PhieuThu
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/PhieuThu.java

**Trách nhiệm (Responsibilities):**
- Lưu trữ thông tin về PhieuThu

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int id
- int maHo
- int maDot
- int maTaiKhoan
- LocalDateTime ngayLap
- BigDecimal tongTien
- String trangThai
- String hinhThucThu

**Methods (một số):**
- int getId()
- void setId()
- int getMaHo()
- void setMaHo()
- int getMaDot()
- void setMaDot()
- int getMaTaiKhoan()
- void setMaTaiKhoan()
- LocalDateTime getNgayLap()
- void setNgayLap()
- ... và 6 methods khác


---

### Class: Phong
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/Phong.java

**Trách nhiệm (Responsibilities):**
- Quản lý Phong

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int soPhong
- BigDecimal dienTich
- BigDecimal giaTien
- String ghiChu

**Methods (một số):**
- int getSoPhong()
- void setSoPhong()
- BigDecimal getDienTich()
- void setDienTich()
- BigDecimal getGiaTien()
- void setGiaTien()
- String getGhiChu()
- void setGhiChu()


---

### Class: PhuongTien
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/PhuongTien.java

**Trách nhiệm (Responsibilities):**
- Quản lý PhuongTien

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int id
- int maHo
- String loaiXe
- String bienSo
- String tenChuXe
- LocalDate ngayDangKy

**Methods (một số):**
- int getId()
- void setId()
- int getMaHo()
- void setMaHo()
- String getLoaiXe()
- void setLoaiXe()
- String getBienSo()
- void setBienSo()
- String getTenChuXe()
- void setTenChuXe()
- ... và 2 methods khác


---

### Class: TaiKhoan
**Loại:** class
**Package:** com.bluemoon.models
**File:** models/TaiKhoan.java

**Trách nhiệm (Responsibilities):**
- Quản lý TaiKhoan

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- int id
- String tenDangNhap
- String matKhau
- String hoTen
- String vaiTro
- String email
- String dienThoai
- String trangThai
- String ghiChu

**Methods (một số):**
- int getId()
- void setId()
- String getTenDangNhap()
- void setTenDangNhap()
- String getMatKhau()
- void setMatKhau()
- String getHoTen()
- void setHoTen()
- String getVaiTro()
- void setVaiTro()
- ... và 8 methods khác


---

## PACKAGE: com.bluemoon.services

### Class: AuthService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/AuthService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến AuthService

**Cộng tác (Collaborators):**
- TaiKhoan

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: BaoCaoService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/BaoCaoService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến BaoCaoService

**Cộng tác (Collaborators):**
- BaoCaoCongNo
- BaoCaoThu

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: ChiTietThuService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/ChiTietThuService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến ChiTietThuService

**Cộng tác (Collaborators):**
- ChiTietThu
- KhoanThu

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: DotThuService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/DotThuService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến DotThuService

**Cộng tác (Collaborators):**
- DotThu
- PhieuThu

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: HoGiaDinhService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/HoGiaDinhService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến HoGiaDinhService

**Cộng tác (Collaborators):**
- HoGiaDinh
- NhanKhau
- PhieuThu

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: KhoanThuService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/KhoanThuService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến KhoanThuService

**Cộng tác (Collaborators):**
- ChiTietThu
- KhoanThu
- PhieuThuService

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: NhanKhauService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/NhanKhauService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến NhanKhauService

**Cộng tác (Collaborators):**
- LichSuNhanKhau
- NhanKhau

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: PhieuThuService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/PhieuThuService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến PhieuThuService

**Cộng tác (Collaborators):**
- ChiTietThu
- KhoanThu
- PhieuThu

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: PhuongTienService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/PhuongTienService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến PhuongTienService

**Cộng tác (Collaborators):**
- PhuongTien

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: TaiKhoanService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/TaiKhoanService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến TaiKhoanService

**Cộng tác (Collaborators):**
- TaiKhoan

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

### Class: ThongKeService
**Loại:** interface
**Package:** com.bluemoon.services
**File:** services/ThongKeService.java

**Trách nhiệm (Responsibilities):**
- Định nghĩa contract cho các nghiệp vụ liên quan đến ThongKeService

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)


---

## PACKAGE: com.bluemoon.services.impl

### Class: AuthServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/AuthServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý AuthImpl

**Cộng tác (Collaborators):**
- AuthService
- DatabaseConnector
- Helper
- TaiKhoan

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_BY_USERNAME
- String INSERT_ACCOUNT
- String UPDATE_PASSWORD
- String CHECK_USERNAME_EXISTS

**Methods (một số):**
- TaiKhoan login()
- boolean register()
- boolean changePassword()
- boolean isUsernameExist()


---

### Class: BaoCaoServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/BaoCaoServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý BaoCaoImpl

**Cộng tác (Collaborators):**
- BaoCaoCongNo
- BaoCaoService
- BaoCaoThu
- DatabaseConnector
- DotThu
- DotThuService
- DotThuServiceImpl
- HoGiaDinh
- HoGiaDinhService
- HoGiaDinhServiceImpl
- NhanKhau
- NhanKhauService
- NhanKhauServiceImpl
- PhieuThu
- PhieuThuService
- PhieuThuServiceImpl

**Thuộc tính (Attributes):**
- Logger logger
- PhieuThuService phieuThuService
- HoGiaDinhService hoGiaDinhService
- NhanKhauService nhanKhauService
- DotThuService dotThuService

**Methods (một số):**
- List<BaoCaoThu> getRevenueReport()
- List<BaoCaoThu> getRevenueReport()
- List<BaoCaoCongNo> getDebtReport()
- List<BaoCaoCongNo> getDebtReport()
- InputStream exportRevenueToExcel()
- InputStream exportDebtToExcel()


---

### Class: ChiTietThuServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/ChiTietThuServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý ChiTietThuImpl

**Cộng tác (Collaborators):**
- ChiTietThu
- ChiTietThuService
- DatabaseConnector
- KhoanThu

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_BY_MAPHIEU
- String INSERT
- String DELETE_BY_MAPHIEU

**Methods (một số):**
- List<ChiTietThu> getChiTietByMaPhieu()
- boolean save()
- boolean saveAll()
- boolean saveAll()
- boolean deleteByMaPhieu()
- boolean deleteByMaPhieu()


---

### Class: DotThuServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/DotThuServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý DotThuImpl

**Cộng tác (Collaborators):**
- DatabaseConnector
- DotThu
- DotThuService
- PhieuThu

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_ALL
- String SELECT_BY_ID
- String INSERT
- String UPDATE
- String DELETE
- String CHECK_DEPENDENCIES
- String SEARCH

**Methods (một số):**
- List<DotThu> getAllDotThu()
- DotThu getDotThuById()
- boolean addDotThu()
- boolean updateDotThu()
- boolean deleteDotThu()
- List<DotThu> searchDotThu()


---

### Class: HoGiaDinhServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/HoGiaDinhServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý HoGiaDinhImpl

**Cộng tác (Collaborators):**
- DatabaseConnector
- HoGiaDinh
- HoGiaDinhService
- NhanKhau
- PhieuThu

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_ALL
- String SELECT_BY_ID
- String INSERT
- String UPDATE
- String DELETE
- String CHECK_SOPHONG_EXISTS
- String CHECK_SOPHONG_EXISTS_EXCLUDE_ID
- String CHECK_NHAN_KHAU_DEPENDENCIES
- String CHECK_PHIEU_THU_DEPENDENCIES
- ... và 1 thuộc tính khác

**Methods (một số):**
- List<HoGiaDinh> getAllHoGiaDinh()
- HoGiaDinh findById()
- boolean addHoGiaDinh()
- boolean updateHoGiaDinh()
- boolean deleteHoGiaDinh()
- List<HoGiaDinh> searchHoGiaDinh()
- boolean checkMaHoExists()


---

### Class: KhoanThuServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/KhoanThuServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý KhoanThuImpl

**Cộng tác (Collaborators):**
- ChiTietThu
- DatabaseConnector
- KhoanThu
- KhoanThuService

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_ALL
- String SELECT_BY_ID
- String INSERT
- String UPDATE
- String DELETE
- String CHECK_FEE_USED

**Methods (một số):**
- List<KhoanThu> getAllKhoanThu()
- boolean addKhoanThu()
- boolean updateKhoanThu()
- boolean deleteKhoanThu()


---

### Class: NhanKhauServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/NhanKhauServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý NhanKhauImpl

**Cộng tác (Collaborators):**
- DatabaseConnector
- HoGiaDinh
- LichSuNhanKhau
- NhanKhau
- NhanKhauService

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_ALL
- String SELECT_BY_ID
- String SELECT_BY_MAHO
- String INSERT
- String UPDATE
- String UPDATE_STATUS
- String DELETE
- String CHECK_CCCD_EXISTS
- String SELECT_LICHSU_BY_NHAN_KHAU
- ... và 2 thuộc tính khác

**Methods (một số):**
- List<NhanKhau> getAll()
- NhanKhau findById()
- List<NhanKhau> getNhanKhauByHoGiaDinh()
- boolean addNhanKhau()
- boolean updateNhanKhau()
- boolean addLichSuNhanKhau()
- List<LichSuNhanKhau> getLichSuNhanKhau()
- List<LichSuNhanKhau> getAllLichSuNhanKhau()
- boolean updateStatusWithHistory()
- boolean deleteNhanKhau()
- ... và 1 methods khác


---

### Class: PhieuThuServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/PhieuThuServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý PhieuThuImpl

**Cộng tác (Collaborators):**
- ChiTietThu
- ChiTietThuService
- ChiTietThuServiceImpl
- DatabaseConnector
- DotThu
- DotThuService
- DotThuServiceImpl
- HoGiaDinh
- HoGiaDinhService
- HoGiaDinhServiceImpl
- KhoanThu
- KhoanThuService
- KhoanThuServiceImpl
- NhanKhau
- NhanKhauService
- NhanKhauServiceImpl
- PhieuThu
- PhieuThuService
- Phong
- PhuongTien
- PhuongTienService
- PhuongTienServiceImpl
- TaiKhoan

**Thuộc tính (Attributes):**
- Logger logger
- DotThuService dotThuService
- KhoanThuService khoanThuService
- HoGiaDinhService hoGiaDinhService
- NhanKhauService nhanKhauService
- PhuongTienService phuongTienService
- ChiTietThuService chiTietThuService
- String SELECT_ALL
- String SELECT_BY_ID
- String SELECT_BY_MAHO
- ... và 6 thuộc tính khác

**Methods (một số):**
- int createPhieuThu()
- boolean addChiTietThu()
- int createPhieuThuWithDetails()
- List<ChiTietThu> getChiTietThuByPhieu()
- PhieuThu getPhieuThuWithDetails()
- List<PhieuThu> findPhieuThuByHoGiaDinh()
- List<PhieuThu> findPhieuThuByDotThu()
- List<PhieuThu> getAllPhieuThu()
- boolean updatePhieuThuStatus()
- int generateReceiptsForDrive()
- ... và 9 methods khác


---

### Class: PhuongTienServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/PhuongTienServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý PhuongTienImpl

**Cộng tác (Collaborators):**
- DatabaseConnector
- Helper
- PhuongTien
- PhuongTienService

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_ALL
- String SELECT_BY_ID
- String SELECT_BY_MAHO
- String SELECT_BY_BIENSO
- String INSERT
- String UPDATE
- String DELETE
- String SEARCH
- String CHECK_BIENSO_EXISTS

**Methods (một số):**
- List<PhuongTien> getAllPhuongTien()
- PhuongTien getPhuongTienById()
- List<PhuongTien> getPhuongTienByHoGiaDinh()
- PhuongTien getPhuongTienByBienSo()
- boolean addPhuongTien()
- boolean updatePhuongTien()
- boolean deletePhuongTien()
- List<PhuongTien> searchPhuongTien()


---

### Class: TaiKhoanServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/TaiKhoanServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý TaiKhoanImpl

**Cộng tác (Collaborators):**
- DatabaseConnector
- Helper
- TaiKhoan
- TaiKhoanService

**Thuộc tính (Attributes):**
- Logger logger
- String SELECT_ALL
- String SELECT_BY_ID
- String SELECT_BY_USERNAME
- String INSERT
- String UPDATE
- String UPDATE_PASSWORD
- String CHECK_USERNAME_EXISTS
- String DELETE
- String UPDATE_STATUS

**Methods (một số):**
- List<TaiKhoan> getAllTaiKhoan()
- TaiKhoan findById()
- TaiKhoan findByUsername()
- boolean isUsernameExists()
- boolean addTaiKhoan()
- boolean updateTaiKhoan()
- boolean updatePassword()
- boolean deleteTaiKhoan()
- boolean updateStatus()


---

### Class: ThongKeServiceImpl
**Loại:** class
**Package:** com.bluemoon.services.impl
**File:** services/impl/ThongKeServiceImpl.java

**Trách nhiệm (Responsibilities):**
- Triển khai logic nghiệp vụ quản lý ThongKeImpl

**Cộng tác (Collaborators):**
- DotThu
- DotThuService
- DotThuServiceImpl
- HoGiaDinh
- HoGiaDinhService
- HoGiaDinhServiceImpl
- NhanKhau
- NhanKhauService
- NhanKhauServiceImpl
- PhieuThu
- PhieuThuService
- PhieuThuServiceImpl
- ThongKeService

**Thuộc tính (Attributes):**
- PhieuThuService phieuThuService
- DotThuService dotThuService
- HoGiaDinhService hoGiaDinhService
- NhanKhauService nhanKhauService

**Methods (một số):**
- Map<String, Number> getRevenueStats()
- Map<String, Number> getDebtStats()
- BigDecimal getTotalRevenue()
- BigDecimal getTotalDebt()
- Map<String, Object> getDashboardStats()
- Map<String, Object> generateCollectionReport()
- List<Map<String, Object>> getRevenueDetails()
- List<Map<String, Object>> getDebtDetails()
- Map<String, Object> getResidentDemographics()


---

## PACKAGE: com.bluemoon.utils

### Class: AccessManager
**Loại:** class
**Package:** com.bluemoon.utils
**File:** utils/AccessManager.java

**Trách nhiệm (Responsibilities):**
- Quản lý AccessManager

**Cộng tác (Collaborators):**
- UserRole

**Thuộc tính (Attributes):**
- Logger logger

**Methods (một số):**
- UserRole getUserRole()
- boolean hasAccess()
- boolean requireAccess()
- boolean canAccessModule()


---

### Class: DatabaseConnector
**Loại:** class
**Package:** com.bluemoon.utils
**File:** utils/DatabaseConnector.java

**Trách nhiệm (Responsibilities):**
- Quản lý DatabaseConnector

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- Logger logger
- String DB_URL
- String DB_USER
- String DB_PASSWORD

**Methods (một số):**
- Connection getConnection()


---

### Class: Helper
**Loại:** class
**Package:** com.bluemoon.utils
**File:** utils/Helper.java

**Trách nhiệm (Responsibilities):**
- Quản lý Helper

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- (Không có thuộc tính private)

**Methods (một số):**
- String hashPassword()
- boolean verifyPassword()


---

### Class: UserRole
**Loại:** enum
**Package:** com.bluemoon.utils
**File:** utils/UserRole.java

**Trách nhiệm (Responsibilities):**
- Quản lý UserRole

**Cộng tác (Collaborators):**
- (Chưa xác định)

**Thuộc tính (Attributes):**
- String code
- String displayName

**Methods (một số):**
- String getCode()
- String getDisplayName()
- UserRole fromString()
- boolean isAdmin()
- boolean isManagement()
- boolean isAccountant()


---

### Class: WebServer
**Loại:** class
**Package:** com.bluemoon.utils
**File:** utils/WebServer.java

**Trách nhiệm (Responsibilities):**
- Quản lý WebServer

**Cộng tác (Collaborators):**
- AuthService
- AuthServiceImpl
- BaoCaoCongNo
- BaoCaoService
- BaoCaoServiceImpl
- BaoCaoThu
- ChiTietThu
- DatabaseConnector
- DotThu
- DotThuService
- DotThuServiceImpl
- HoGiaDinh
- HoGiaDinhService
- HoGiaDinhServiceImpl
- KhoanThu
- KhoanThuService
- KhoanThuServiceImpl
- LichSuNhanKhau
- NhanKhau
- NhanKhauService
- NhanKhauServiceImpl
- PhieuThu
- PhieuThuService
- PhieuThuServiceImpl
- Phong
- PhuongTien
- PhuongTienService
- PhuongTienServiceImpl
- TaiKhoan
- TaiKhoanService
- TaiKhoanServiceImpl
- ThongKeService
- ThongKeServiceImpl

**Thuộc tính (Attributes):**
- int PORT
- ObjectMapper objectMapper
- Logger logger
- AuthService authService
- TaiKhoanService taiKhoanService
- KhoanThuService khoanThuService
- DotThuService dotThuService
- HoGiaDinhService hoGiaDinhService
- NhanKhauService nhanKhauService
- PhieuThuService phieuThuService
- ... và 2 thuộc tính khác

**Methods (một số):**
- void main()
- void start()


---

## QUAN HỆ GIỮA CÁC LỚP

### Quan hệ Implementation:
- `DotThuServiceImpl` implements `DotThuService`
- `ChiTietThuServiceImpl` implements `ChiTietThuService`
- `ThongKeServiceImpl` implements `ThongKeService`
- `HoGiaDinhServiceImpl` implements `HoGiaDinhService`
- `KhoanThuServiceImpl` implements `KhoanThuService`
- `PhieuThuServiceImpl` implements `PhieuThuService`
- `BaoCaoServiceImpl` implements `BaoCaoService`
- `AuthServiceImpl` implements `AuthService`
- `PhuongTienServiceImpl` implements `PhuongTienService`
- `TaiKhoanServiceImpl` implements `TaiKhoanService`
- `NhanKhauServiceImpl` implements `NhanKhauService`

### Quan hệ Dependency:
- `AccessManager` → `UserRole`
- `WebServer` → `AuthService`
- `WebServer` → `AuthServiceImpl`
- `WebServer` → `BaoCaoCongNo`
- `WebServer` → `BaoCaoService`
- `WebServer` → `BaoCaoServiceImpl`
- `WebServer` → `BaoCaoThu`
- `WebServer` → `ChiTietThu`
- `WebServer` → `DatabaseConnector`
- `WebServer` → `DotThu`
- `WebServer` → `DotThuService`
- `WebServer` → `DotThuServiceImpl`
- `WebServer` → `HoGiaDinh`
- `WebServer` → `HoGiaDinhService`
- `WebServer` → `HoGiaDinhServiceImpl`
- `WebServer` → `KhoanThu`
- `WebServer` → `KhoanThuService`
- `WebServer` → `KhoanThuServiceImpl`
- `WebServer` → `LichSuNhanKhau`
- `WebServer` → `NhanKhau`
- `WebServer` → `NhanKhauService`
- `WebServer` → `NhanKhauServiceImpl`
- `WebServer` → `PhieuThu`
- `WebServer` → `PhieuThuService`
- `WebServer` → `PhieuThuServiceImpl`
- `WebServer` → `Phong`
- `WebServer` → `PhuongTien`
- `WebServer` → `PhuongTienService`
- `WebServer` → `PhuongTienServiceImpl`
- `WebServer` → `TaiKhoan`
- `WebServer` → `TaiKhoanService`
- `WebServer` → `TaiKhoanServiceImpl`
- `WebServer` → `ThongKeService`
- `WebServer` → `ThongKeServiceImpl`
- `HoGiaDinh` → `Helper`
- `HoGiaDinh` → `NhanKhau`
- `HoGiaDinh` → `NhanKhauService`
- `HoGiaDinh` → `Phong`
- `NhanKhau` → `TaiKhoan`
- `ChiTietThu` → `Helper`
- `BaoCaoThu` → `ChiTietThu`
- `BaoCaoThu` → `PhieuThu`
- `DotThuService` → `DotThu`
- `DotThuService` → `PhieuThu`
- `ChiTietThuService` → `ChiTietThu`
- `ChiTietThuService` → `KhoanThu`
- `HoGiaDinhService` → `HoGiaDinh`
- `HoGiaDinhService` → `NhanKhau`
- `HoGiaDinhService` → `PhieuThu`
- `KhoanThuService` → `ChiTietThu`
- `KhoanThuService` → `KhoanThu`
- `KhoanThuService` → `PhieuThuService`
- `PhieuThuService` → `ChiTietThu`
- `PhieuThuService` → `KhoanThu`
- `PhieuThuService` → `PhieuThu`
- `BaoCaoService` → `BaoCaoCongNo`
- `BaoCaoService` → `BaoCaoThu`
- `AuthService` → `TaiKhoan`
- `PhuongTienService` → `PhuongTien`
- `TaiKhoanService` → `TaiKhoan`
- `NhanKhauService` → `LichSuNhanKhau`
- `NhanKhauService` → `NhanKhau`
- `HoGiaDinhServiceImpl` → `DatabaseConnector`
- `HoGiaDinhServiceImpl` → `HoGiaDinh`
- `HoGiaDinhServiceImpl` → `HoGiaDinhService`
- `HoGiaDinhServiceImpl` → `NhanKhau`
- `HoGiaDinhServiceImpl` → `PhieuThu`
- `PhieuThuServiceImpl` → `ChiTietThu`
- `PhieuThuServiceImpl` → `ChiTietThuService`
- `PhieuThuServiceImpl` → `ChiTietThuServiceImpl`
- `PhieuThuServiceImpl` → `DatabaseConnector`
- `PhieuThuServiceImpl` → `DotThu`
- `PhieuThuServiceImpl` → `DotThuService`
- `PhieuThuServiceImpl` → `DotThuServiceImpl`
- `PhieuThuServiceImpl` → `HoGiaDinh`
- `PhieuThuServiceImpl` → `HoGiaDinhService`
- `PhieuThuServiceImpl` → `HoGiaDinhServiceImpl`
- `PhieuThuServiceImpl` → `KhoanThu`
- `PhieuThuServiceImpl` → `KhoanThuService`
- `PhieuThuServiceImpl` → `KhoanThuServiceImpl`
- `PhieuThuServiceImpl` → `NhanKhau`
- `PhieuThuServiceImpl` → `NhanKhauService`
- `PhieuThuServiceImpl` → `NhanKhauServiceImpl`
- `PhieuThuServiceImpl` → `PhieuThu`
- `PhieuThuServiceImpl` → `PhieuThuService`
- `PhieuThuServiceImpl` → `Phong`
- `PhieuThuServiceImpl` → `PhuongTien`
- `PhieuThuServiceImpl` → `PhuongTienService`
- `PhieuThuServiceImpl` → `PhuongTienServiceImpl`
- `PhieuThuServiceImpl` → `TaiKhoan`
- `PhuongTienServiceImpl` → `DatabaseConnector`
- `PhuongTienServiceImpl` → `Helper`
- `PhuongTienServiceImpl` → `PhuongTien`
- `PhuongTienServiceImpl` → `PhuongTienService`
- `NhanKhauServiceImpl` → `DatabaseConnector`
- `NhanKhauServiceImpl` → `HoGiaDinh`
- `NhanKhauServiceImpl` → `LichSuNhanKhau`
- `NhanKhauServiceImpl` → `NhanKhau`
- `NhanKhauServiceImpl` → `NhanKhauService`
- `BaoCaoServiceImpl` → `BaoCaoCongNo`
- `BaoCaoServiceImpl` → `BaoCaoService`
- `BaoCaoServiceImpl` → `BaoCaoThu`
- `BaoCaoServiceImpl` → `DatabaseConnector`
- `BaoCaoServiceImpl` → `DotThu`
- `BaoCaoServiceImpl` → `DotThuService`
- `BaoCaoServiceImpl` → `DotThuServiceImpl`
- `BaoCaoServiceImpl` → `HoGiaDinh`
- `BaoCaoServiceImpl` → `HoGiaDinhService`
- `BaoCaoServiceImpl` → `HoGiaDinhServiceImpl`
- `BaoCaoServiceImpl` → `NhanKhau`
- `BaoCaoServiceImpl` → `NhanKhauService`
- `BaoCaoServiceImpl` → `NhanKhauServiceImpl`
- `BaoCaoServiceImpl` → `PhieuThu`
- `BaoCaoServiceImpl` → `PhieuThuService`
- `BaoCaoServiceImpl` → `PhieuThuServiceImpl`
- `ChiTietThuServiceImpl` → `ChiTietThu`
- `ChiTietThuServiceImpl` → `ChiTietThuService`
- `ChiTietThuServiceImpl` → `DatabaseConnector`
- `ChiTietThuServiceImpl` → `KhoanThu`
- `DotThuServiceImpl` → `DatabaseConnector`
- `DotThuServiceImpl` → `DotThu`
- `DotThuServiceImpl` → `DotThuService`
- `DotThuServiceImpl` → `PhieuThu`
- `TaiKhoanServiceImpl` → `DatabaseConnector`
- `TaiKhoanServiceImpl` → `Helper`
- `TaiKhoanServiceImpl` → `TaiKhoan`
- `TaiKhoanServiceImpl` → `TaiKhoanService`
- `ThongKeServiceImpl` → `DotThu`
- `ThongKeServiceImpl` → `DotThuService`
- `ThongKeServiceImpl` → `DotThuServiceImpl`
- `ThongKeServiceImpl` → `HoGiaDinh`
- `ThongKeServiceImpl` → `HoGiaDinhService`
- `ThongKeServiceImpl` → `HoGiaDinhServiceImpl`
- `ThongKeServiceImpl` → `NhanKhau`
- `ThongKeServiceImpl` → `NhanKhauService`
- `ThongKeServiceImpl` → `NhanKhauServiceImpl`
- `ThongKeServiceImpl` → `PhieuThu`
- `ThongKeServiceImpl` → `PhieuThuService`
- `ThongKeServiceImpl` → `PhieuThuServiceImpl`
- `ThongKeServiceImpl` → `ThongKeService`
- `KhoanThuServiceImpl` → `ChiTietThu`
- `KhoanThuServiceImpl` → `DatabaseConnector`
- `KhoanThuServiceImpl` → `KhoanThu`
- `KhoanThuServiceImpl` → `KhoanThuService`
- `AuthServiceImpl` → `AuthService`
- `AuthServiceImpl` → `DatabaseConnector`
- `AuthServiceImpl` → `Helper`
- `AuthServiceImpl` → `TaiKhoan`

