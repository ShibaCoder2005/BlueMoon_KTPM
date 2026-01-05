# Hướng dẫn sử dụng generate_crc.py

## Mô tả
Script Python tự động phân tích mã nguồn Java và tạo ra thiết kế CRC (Class-Responsibility-Collaboration) cho hệ thống.

## Yêu cầu
- Python 3.6 trở lên
- Không cần cài đặt thư viện bên ngoài (chỉ sử dụng thư viện chuẩn)

## Cách sử dụng

### 1. Chạy script
```bash
python generate_crc.py
```

Hoặc:
```bash
python3 generate_crc.py
```

### 2. Kết quả
Script sẽ tạo ra 2 file:

1. **CRC_Design_Auto.md**: File markdown chứa thiết kế CRC chi tiết
2. **CRC_Table.html**: File HTML với bảng CRC dạng thẻ (cards) có thể mở bằng trình duyệt

## Cấu trúc output

### File Markdown (CRC_Design_Auto.md)
- Nhóm các lớp theo package
- Mỗi lớp có:
  - Tên lớp và loại (class/interface/enum)
  - Trách nhiệm (Responsibilities)
  - Cộng tác (Collaborators)
  - Thuộc tính (Attributes)
  - Methods
- Phần quan hệ giữa các lớp

### File HTML (CRC_Table.html)
- Hiển thị dạng thẻ CRC (CRC Cards)
- Mỗi thẻ hiển thị:
  - Tên lớp
  - Trách nhiệm
  - Cộng tác
  - Thuộc tính
- Có thể mở trực tiếp bằng trình duyệt để xem

## Tính năng

### Tự động phát hiện:
- ✅ Package của mỗi lớp
- ✅ Loại lớp (class, interface, enum)
- ✅ Thuộc tính private
- ✅ Methods public
- ✅ Imports và dependencies
- ✅ Collaborators từ việc sử dụng các lớp khác

### Phân tích:
- ✅ Nhóm lớp theo package
- ✅ Xác định quan hệ Implementation
- ✅ Xác định quan hệ Dependency
- ✅ Tự động tạo responsibilities nếu không có comment

## Lưu ý

1. Script sẽ bỏ qua các file trong thư mục test
2. Script phân tích dựa trên regex, có thể không hoàn hảo 100%
3. Nếu có comment "Trách nhiệm" hoặc "Responsibility" trong code, script sẽ sử dụng
4. Nếu không có comment, script sẽ tự động tạo responsibilities dựa trên tên và loại lớp

## Cải tiến có thể thêm

- [ ] Phân tích sâu hơn về quan hệ Association, Aggregation, Composition
- [ ] Tạo sơ đồ UML tự động
- [ ] Export sang các format khác (PDF, JSON, XML)
- [ ] Phân tích phức tạp hơn về dependencies
- [ ] Tạo sequence diagrams

## Ví dụ output

Sau khi chạy script, bạn sẽ có:

```
CRC_Design_Auto.md  - Thiết kế CRC chi tiết
CRC_Table.html      - Bảng CRC dạng HTML
```

Mở file HTML bằng trình duyệt để xem các thẻ CRC một cách trực quan.

