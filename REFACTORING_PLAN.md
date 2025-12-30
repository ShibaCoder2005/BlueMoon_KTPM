# Kế Hoạch Refactoring - BlueMoon KTPM Project

## Tổng Quan
Tài liệu này liệt kê tất cả các file cần refactoring để làm code gọn gàng, dễ đọc hơn mà không thay đổi logic hoặc chức năng.

---

## 1. DEAD CODE - Code Không Sử Dụng

### 1.1 Commented Code
- **`pom.xml`** (dòng 72-79): Xóa comment block cho MySQL JDBC dependency
- **`src/main/java/com/bluemoon/TestApp.java`**: Kiểm tra xem có đang được sử dụng không, nếu không thì xóa hoặc di chuyển vào test folder

### 1.2 Unused Imports
Cần kiểm tra và xóa các import không sử dụng trong:
- **`src/main/java/com/bluemoon/WebServer.java`**: 
  - `java.nio.charset.StandardCharsets` (có thể không dùng)
  - Các import wildcard (`import com.bluemoon.services.*;`) nên thay bằng explicit imports
- **Tất cả Service Implementation files**: Kiểm tra unused imports

### 1.3 Unused Variables/Methods
- **`src/main/resources/js/api.js`**: 
  - `showSuccess()` và `showError()` functions (dòng 97-103) - có thể đã được thay thế bởi `APIUtils.showSuccess/showError`
  - `ThongBaoAPI` (dòng 362-384) - API này có thể không còn được sử dụng sau khi xóa giao diện thông báo

---

## 2. SIMPLIFY LOGIC - Đơn Giản Hóa Logic

### 2.1 Long If-Else Chains → Switch-Case
- **`src/main/java/com/bluemoon/WebServer.java`** (dòng 2075-2120): `calculateFeeAmountForHousehold()` - chuyển if-else chain thành switch-case
- **`src/main/resources/js/rbac.js`** (dòng 49-74): `normalizeRole()` - có thể dùng switch-case hoặc Map lookup

### 2.2 Boolean Expressions
- **`src/main/resources/js/api.js`** (dòng 79): `if (result.success === false)` → `if (!result.success)`
- **`src/main/resources/js/rbac.js`**: Kiểm tra các boolean comparisons

### 2.3 Java 8+ Streams & Lambdas
- **`src/main/java/com/bluemoon/WebServer.java`** (dòng 1982-1987): 
  ```java
  // Current:
  List<KhoanThu> mandatoryFees = new java.util.ArrayList<>();
  for (KhoanThu fee : allFees) {
      if (fee.isBatBuoc()) {
          mandatoryFees.add(fee);
      }
  }
  // Should be:
  List<KhoanThu> mandatoryFees = allFees.stream()
      .filter(KhoanThu::isBatBuoc)
      .collect(Collectors.toList());
  ```

- **`src/main/java/com/bluemoon/WebServer.java`** (dòng 2002-2014): Vehicle counting loop có thể dùng Stream với `filter()` và `count()`

---

## 3. FRONTEND OPTIMIZATION

### 3.1 Remove Console Logs
Cần xóa hoặc comment các `console.log/error/warn` statements trong:

- **`src/main/resources/js/api.js`**: 
  - Dòng 20: `console.log('[apiRequest] ...')`
  - Dòng 40, 44, 49, 59, 60, 65, 81, 92: Các console.error/warn
  - Dòng 166-168: Debug logs trong `NhanKhauAPI.delete`
  - Dòng 375, 378: ThongBaoAPI logs
  - Dòng 475: APIUtils.getFormValue warning
  - Dòng 487, 512, 518: Logout logs
  - Dòng 543, 635: UserUtils và updateNavbarAvatar logs

- **`src/main/resources/js/rbac.js`**:
  - Dòng 40: `console.error('Error getting user role:', e)`
  - Dòng 115, 152, 185: `console.warn('No user role found...')`

- **HTML Files** (cần scan tất cả):
  - `index.html`: Dòng 1187-1189, 1206
  - `dot-thu.html`: Dòng 507-509, 523
  - `tai-khoan.html`, `can-ho.html`, `ho-so.html`, `phuong-tien.html`, `danh-sach-dan-cu.html`, `khoan-thu.html`, `phieu-thu.html`, `thong-ke.html`: Các console.log statements

### 3.2 Consolidate Duplicated Export Logic
- **`src/main/resources/js/api.js`**: 
  - `exportPdf()` (dòng 197-219)
  - `exportRevenue()` (dòng 296-325)
  - `exportDebt()` (dòng 333-359)
  
  → Tạo helper function `downloadBlob(url, filename)` để tái sử dụng

### 3.3 Consolidate Duplicated API Patterns
- **`src/main/resources/js/api.js`**: Tất cả các `getAll()` methods có pattern giống nhau:
  ```javascript
  getAll: async () => {
      const response = await apiRequest('/endpoint', 'GET');
      return Array.isArray(response) ? response : (response.data || []);
  }
  ```
  → Có thể tạo helper function `createGetAllAPI(endpoint)`

---

## 4. STANDARDIZE & MODULARIZE

### 4.1 Extract Duplicated Error Response Creation
- **`src/main/java/com/bluemoon/WebServer.java`**:
  - `createSuccessResponse()` (dòng 1910-1918)
  - `createErrorResponse()` (dòng 1920-1925)
  - `createStandardErrorResponse()` (dòng 1952-1961)
  
  → Có thể tạo một `ResponseBuilder` utility class để chuẩn hóa

### 4.2 Extract Duplicated Exception Handling
- **`src/main/java/com/bluemoon/WebServer.java`**:
  - `handleException()` (dòng 1927-1947) - đã có nhưng có thể cải thiện
  - Exception handler trong `app.exception()` (dòng 1782-1803) - có code trùng lặp với `handleException()`

### 4.3 Extract Duplicated Role Checking Logic
- **HTML Files**: Nhiều file có code role checking giống nhau:
  ```javascript
  const userInfo = sessionStorage.getItem('currentUser');
  const user = JSON.parse(userInfo);
  const role = user.vaiTro || user.role || '';
  const isAdmin = role === 'BanQuanLy' || role === 'Ban Quản lý' || ...
  ```
  → Đã có `rbac.js`, nhưng cần đảm bảo tất cả HTML files đều dùng nó thay vì duplicate code

### 4.4 Extract Duplicated Date/Currency Formatting
- **HTML Files**: Nhiều file có code format date và currency giống nhau
  → Đã có `APIUtils.formatDate()` và `APIUtils.formatCurrency()`, cần đảm bảo tất cả files đều dùng

---

## 5. NAMING CONVENTIONS

### 5.1 Inconsistent Naming
- **`src/main/resources/js/api.js`**: 
  - `showSuccess()` và `showError()` vs `APIUtils.showSuccess()` và `APIUtils.showError()` - nên thống nhất
  - `ThongBaoAPI` - có thể không còn cần thiết

### 5.2 Variable Naming
- Kiểm tra tất cả files để đảm bảo:
  - Java: camelCase cho variables, PascalCase cho classes
  - JavaScript: camelCase cho variables/functions, PascalCase cho constructors
  - Constants: UPPER_SNAKE_CASE

---

## 6. FILES CẦN REFACTORING (Ưu Tiên)

### High Priority (Ảnh hưởng nhiều đến code quality):
1. **`src/main/java/com/bluemoon/WebServer.java`** - File lớn nhất, nhiều duplicated code
2. **`src/main/resources/js/api.js`** - Nhiều console.log, duplicated export logic
3. **`src/main/resources/js/rbac.js`** - Console warnings

### Medium Priority:
4. **HTML Files** (tất cả) - Console.log statements, duplicated role checking
5. **Service Implementation files** - Unused imports, có thể dùng Streams

### Low Priority:
6. **`pom.xml`** - Xóa commented code
7. **`src/main/java/com/bluemoon/TestApp.java`** - Kiểm tra và di chuyển nếu cần

---

## 7. REFACTORING CHECKLIST

### Phase 1: Dead Code Removal
- [ ] Xóa commented MySQL dependency trong `pom.xml`
- [ ] Xóa unused imports trong tất cả Java files
- [ ] Xóa unused functions trong `api.js` (`showSuccess`, `showError`, `ThongBaoAPI` nếu không dùng)
- [ ] Kiểm tra và xóa `TestApp.java` nếu không cần

### Phase 2: Logic Simplification
- [ ] Chuyển if-else chains thành switch-case trong `WebServer.java`
- [ ] Simplify boolean expressions trong JS files
- [ ] Refactor loops thành Streams trong `WebServer.java`

### Phase 3: Frontend Cleanup
- [ ] Xóa tất cả console.log/error/warn statements
- [ ] Extract duplicated export blob download logic
- [ ] Extract duplicated getAll() API patterns

### Phase 4: Standardization
- [ ] Tạo `ResponseBuilder` utility class
- [ ] Consolidate exception handling
- [ ] Đảm bảo tất cả HTML files dùng `rbac.js` thay vì duplicate code
- [ ] Đảm bảo tất cả HTML files dùng `APIUtils` cho formatting

### Phase 5: Final Review
- [ ] Kiểm tra naming conventions
- [ ] Test toàn bộ application trên localhost:7070
- [ ] Verify không có breaking changes

---

## Lưu Ý Quan Trọng

⚠️ **KHÔNG ĐƯỢC THAY ĐỔI:**
- Database schema
- API endpoint paths (`/api/*`)
- API response structures (JSON format)
- Business logic
- Functionality

✅ **ĐƯỢC PHÉP:**
- Refactor code structure
- Remove dead code
- Simplify expressions
- Extract common utilities
- Improve readability
- Standardize naming

---

## Estimated Impact

- **Lines of Code Reduction**: ~200-300 lines (chủ yếu từ console.log và duplicated code)
- **Code Quality**: Cải thiện đáng kể về readability và maintainability
- **Performance**: Không thay đổi (chỉ refactoring, không optimize logic)
- **Risk Level**: Thấp (chỉ refactoring, không thay đổi logic)

