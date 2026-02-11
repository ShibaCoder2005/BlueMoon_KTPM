# 🏢 BlueMoon - Hệ Thống Quản Lý Chung Cư

> **BlueMoon** là giải pháp phần mềm toàn diện hỗ trợ Ban quản lý chung cư trong việc vận hành, quản lý cư dân và kiểm soát tài chính một cách hiệu quả, minh bạch.

## 📖 Giới Thiệu Dự Án

Dự án được xây dựng nhằm giải quyết bài toán quản lý cho chung cư **BlueMoon**, tập trung vào hai nghiệp vụ cốt lõi: **Quản lý dân cư** và **Quản lý thu phí**.

### Thông tin dự án (Context)
| Thông tin | Chi tiết |
|-----------|----------|
| **Địa chỉ** | Ngã tư Văn Phú |
| **Quy mô** | 30 tầng |
| **Diện tích sàn** | 450m² |
| **Thời gian** | Khởi công 2021 \| Hoàn thành 2023 |

## Tính Năng Chính

Hệ thống được chia thành các phân hệ chính sau:

### 1. Quản Lý Dân Cư
* **Quản lý Phòng/Căn hộ:** Theo dõi trạng thái, thông tin chi tiết từng căn hộ.
* **Quản lý Hộ gia đình:** Quản lý thông tin chủ hộ và các thành viên.
* **Quản lý Nhân khẩu:** Theo dõi biến động dân cư (tạm trú, tạm vắng).
* **Quản lý Phương tiện:** Kiểm soát xe máy, ô tô của cư dân.

### 2. Quản Lý Thu Phí
* **Danh mục Khoản thu:** Thiết lập các loại phí (dịch vụ, vệ sinh, gửi xe, đóng góp...).
* **Đợt thu phí:** Tạo và quản lý các đợt thu theo tháng/quý.
* **Phiếu thu:** Tự động tạo phiếu thu, in ấn và theo dõi trạng thái thanh toán.

### 3. Báo Cáo & Thống Kê
* Thống kê tình hình dân cư.
* Báo cáo doanh thu, công nợ chi tiết.

### 4. Bảo Mật
* Hệ thống đăng nhập/đăng xuất an toàn.
* Phân quyền người dùng (Admin, Ban quản lý...).

---

## Công Nghệ Sử Dụng

* **Ngôn ngữ:** Java (JDK 23)
* **Build Tool:** Maven
* **Database:** PostgreSQL (v17.7)
* **Framework/Library:** Javalin (Web Server), JDBC/Hibernate (tùy thực tế dự án).
* **Frontend:** HTML, CSS, JavaScript (Giao diện web).

## Hướng Dẫn Cài Đặt

### Yêu Cầu Hệ Thống (Prerequisites)
Để chạy được dự án, máy tính của bạn cần đáp ứng:
* **OS:** Windows 10 trở lên.
* **Java:** JDK 20 trở lên.
* **Maven:** Apache Maven 3.9 trở lên. 
* **Database:** PostgreSQL 17
* **Phần cứng:** RAM tối thiểu 8GB.

### 📥 Các Bước Cài Đặt

#### Bước 1: Clone dự án
1. Mở CMD hoặc Terminal và chạy lệnh:
```bash
git clone https://github.com/ShibaCoder2005/BlueMoon_KTPM.git
```

#### Bước 2: Cài Đặt JDK
* Tải JDK 20 từ trang chính thức của Oracle: https://www.oracle.com/java/technologies/javase-jdk20-downloads.html
* Cài đặt môi trường (Edit the system environment variables)
* Kiểm tra
Gõ lệnh:
```bash
java -version
```
Xuất hiện thông báo: java version.... là thành công

#### Bước 3: Cài Đặt Maven
* Tải Maven từ trang chính thức: http://apache-maven-3.9.12-bin.zip
* Cài đặt môi trường (Edit the system environment variables)
* Kiểm tra
Gõ lệnh:
```bash
mvn -version
```
Xuất hiện thông báo: Apache Maven.... là thành công

#### Bước 4: Cài Đặt PostgreSQL
* Tải PostgreSQL từ trang chính thức: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads 
Tải bản 17.7, Windows x86-64.
* Cài đặt: liên tục bấm Next đến khi hộp lệnh Stack Builder mở ra. Chọn bản vừa cài đặt và nhấn Next.
* Kiểm tra phần mềm đã chạy chưa. Tìm kiếm và mở Service, postgresql có Status: Running là được. Nếu chưa, nhấn Start the service ở bên trái.

#### Bước 5: Cài Đặt Database BlueMoon
* Mở cmd
Trong cửa sổ CMD, gõ lệnh sau để đi vào thư mục cài đặt:
```bash
cd "C:\Program Files\PostgreSQL\17\bin"
```
* Đăng nhập vào PostgreSQL
Chạy dòng lệnh sau:
```bash
psql -U postgres -d bluemoon -f "C:\Users\ADMIN\OneDrive\Documents\Github\BlueMoon_KTPM\dtb\them_bang.sql"
```
Terminate sẽ hỏi về mật khẩu đăng nhập, lúc này điền ‘admin’ là được.

#### Bước 6:  Chạy WebServer và chạy phần mềm dự án
* Mở cmd, chạy lệnh sau:
```bash
cd "C:\Users\ADMIN\OneDrive\Documents\Github\BlueMoon_KTPM"
```
* Chạy lệnh mvn
```bash
mvn compile
```
Thông báo SUCCESS thì gõ tiếp:
```bash
mvn exec:java -Dexec.mainClass=com.bluemoon.WebServer
```
* Chạy phân mềm
Giữ Ctrl và nhấn vào đường dẫn http://localhost:7070/

### Hướng Dẫn Sử Dụng
* Thông tin đăng nhập mặc định
** Tài khoản: admin
** Mật khẩu: 123456

* Chi tiết các chức năng
** Tạo tải khoản: Vào mục Tài khoản, tạo thêm tài khoản cho chhir chức năng Quản lý dân cư, Quản lý thu phí
** Sử dụng phần mềm

### Đóng Góp và Báo Cáo Lỗi
Chúng tôi hoan nghênh mọi đóng góp để dự án hoàn thiện hơn!
Nếu gặp vấn đề trong quá trình sử dụng, vui lòng tạo Issue với đầy đủ thông tin: mô tả lỗi, các bước tái hiện và hình ảnh minh họa (nếu có).

### Tác Giả

### Giấy Phép
