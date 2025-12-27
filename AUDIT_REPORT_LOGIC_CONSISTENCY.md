# Logic Consistency Audit Report
## PhieuThu, ChiTietThu, KhoanThu, DotThu Relationship Analysis

**Date:** 2025-12-27  
**Auditor:** Senior Backend Architect & Data Auditor  
**Scope:** Service/DAO layer logic consistency for invoice management system

---

## Executive Summary

This audit examines the logical integrity of the relationship between 4 core entities:
- `DotThu` (Collection Period/Batch)
- `KhoanThu` (Fee Definition)
- `PhieuThu` (Invoice/Receipt)
- `ChiTietThu` (Invoice Detail/Line Item)

**Overall Risk Level:** 🟡 **MEDIUM-HIGH** - Several critical logic gaps identified.

---

## 1. Summation Integrity (Tính toàn vẹn tổng tiền)

### Requirement
`PhieuThu.tongTien` MUST ALWAYS equal `SUM(ChiTietThu.thanhTien)` for the same `maPhieu`.

### Current Implementation Analysis

#### ✅ **SAFE:** `createPhieuThuWithDetails()`
```java:214:298:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
// Line 531-545: Calculates totalAmount from chiTietList
BigDecimal totalAmount = BigDecimal.ZERO;
for (KhoanThu fee : mandatoryFees) {
    BigDecimal feeAmount = calculateFeeAmount(...);
    if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
        totalAmount = totalAmount.add(feeAmount);
        ChiTietThu chiTiet = new ChiTietThu();
        chiTiet.setThanhTien(feeAmount);
        chiTietList.add(chiTiet);
    }
}
phieuThu.setTongTien(totalAmount); // ✅ Correctly set
```

**Status:** ✅ **SAFE** - Total is calculated from details before insertion.

---

#### ❌ **CRITICAL RISK:** `addChiTietThu()`
```java:179:211:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
public boolean addChiTietThu(ChiTietThu chiTiet) {
    // ... inserts ChiTietThu ...
    // ❌ NO UPDATE to PhieuThu.tongTien
    return success;
}
```

**Problem:** When a new `ChiTietThu` is added to an existing `PhieuThu`, the `PhieuThu.tongTien` is NOT recalculated.

**Risk:** Data inconsistency - `PhieuThu.tongTien` becomes stale.

**Impact:** HIGH - Financial data integrity compromised.

---

#### ❌ **CRITICAL RISK:** `updatePhieuThu()`
```java:917:1013:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
public boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
    // ... updates PhieuThu with provided tongTien ...
    pstmt.setBigDecimal(5, phieuThu.getTongTien()); // ❌ Uses provided value
    
    // ... updates ChiTietThu list ...
    // ❌ NO VALIDATION that tongTien == SUM(chiTietList.thanhTien)
}
```

**Problem:** 
1. Accepts `tongTien` from caller without validation
2. Does NOT recalculate `tongTien` from `chiTietList` sum
3. No integrity check after update

**Risk:** Data inconsistency if caller provides incorrect `tongTien`.

**Impact:** HIGH - Financial data integrity compromised.

---

### Code Fix for Summation Integrity

```java
/**
 * Recalculate and update PhieuThu.tongTien from ChiTietThu sum.
 * @param maPhieu Receipt ID
 * @param conn Database connection (for transaction support)
 * @return true if successful, false otherwise
 */
private boolean recalculatePhieuThuTotal(int maPhieu, Connection conn) {
    try {
        // Calculate sum from ChiTietThu
        String sumQuery = "SELECT COALESCE(SUM(thanhTien), 0) FROM ChiTietThu WHERE maPhieu = ?";
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        
        try (PreparedStatement sumStmt = conn.prepareStatement(sumQuery)) {
            sumStmt.setInt(1, maPhieu);
            try (ResultSet rs = sumStmt.executeQuery()) {
                if (rs.next()) {
                    calculatedTotal = rs.getBigDecimal(1);
                }
            }
        }
        
        // Update PhieuThu.tongTien
        String updateQuery = "UPDATE PhieuThu SET tongTien = ? WHERE id = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setBigDecimal(1, calculatedTotal);
            updateStmt.setInt(2, maPhieu);
            int rowsAffected = updateStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.log(Level.INFO, "Recalculated PhieuThu.tongTien for id: " + maPhieu + 
                    " to: " + calculatedTotal);
                return true;
            }
        }
        
        return false;
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error recalculating PhieuThu total for id: " + maPhieu, e);
        return false;
    }
}

// Update addChiTietThu():
@Override
public boolean addChiTietThu(ChiTietThu chiTiet) {
    if (chiTiet == null) {
        logger.log(Level.WARNING, "Cannot add ChiTietThu: chiTiet is null");
        return false;
    }

    try (Connection conn = DatabaseConnector.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // Insert ChiTietThu
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_CHITIET)) {
                pstmt.setInt(1, chiTiet.getMaPhieu());
                pstmt.setInt(2, chiTiet.getMaKhoan());
                pstmt.setBigDecimal(3, chiTiet.getSoLuong());
                pstmt.setBigDecimal(4, chiTiet.getDonGia());
                pstmt.setBigDecimal(5, chiTiet.getThanhTien());
                pstmt.setString(6, null);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            // ✅ FIX: Recalculate and update PhieuThu.tongTien
            if (!recalculatePhieuThuTotal(chiTiet.getMaPhieu(), conn)) {
                conn.rollback();
                return false;
            }
            
            conn.commit();
            logger.log(Level.INFO, "Successfully added ChiTietThu and recalculated total for maPhieu: " + chiTiet.getMaPhieu());
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error adding ChiTietThu for maPhieu: " + chiTiet.getMaPhieu(), e);
        e.printStackTrace();
        return false;
    }
}

// Update updatePhieuThu():
@Override
public boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
    // ... existing validation code ...
    
    try (Connection conn = DatabaseConnector.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // Step 1: Update ChiTietThu if provided
            if (chiTietList != null) {
                // Delete old details
                try (PreparedStatement deleteStmt = conn.prepareStatement(DELETE_CHITIET_BY_PHIEU)) {
                    deleteStmt.setInt(1, phieuThu.getId());
                    deleteStmt.executeUpdate();
                }
                
                // Add new details
                if (!chiTietList.isEmpty()) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_CHITIET)) {
                        for (ChiTietThu chiTiet : chiTietList) {
                            chiTiet.setMaPhieu(phieuThu.getId());
                            insertStmt.setInt(1, chiTiet.getMaPhieu());
                            insertStmt.setInt(2, chiTiet.getMaKhoan());
                            insertStmt.setBigDecimal(3, chiTiet.getSoLuong());
                            insertStmt.setBigDecimal(4, chiTiet.getDonGia());
                            insertStmt.setBigDecimal(5, chiTiet.getThanhTien());
                            insertStmt.setString(6, null);
                            insertStmt.addBatch();
                        }
                        insertStmt.executeBatch();
                    }
                }
            }
            
            // ✅ FIX: Recalculate tongTien from ChiTietThu sum
            BigDecimal calculatedTotal = BigDecimal.ZERO;
            if (chiTietList != null && !chiTietList.isEmpty()) {
                for (ChiTietThu chiTiet : chiTietList) {
                    if (chiTiet.getThanhTien() != null) {
                        calculatedTotal = calculatedTotal.add(chiTiet.getThanhTien());
                    }
                }
            } else {
                // If chiTietList is null, recalculate from database
                if (!recalculatePhieuThuTotal(phieuThu.getId(), conn)) {
                    conn.rollback();
                    return false;
                }
                // Get the recalculated value
                try (PreparedStatement getStmt = conn.prepareStatement("SELECT tongTien FROM PhieuThu WHERE id = ?")) {
                    getStmt.setInt(1, phieuThu.getId());
                    try (ResultSet rs = getStmt.executeQuery()) {
                        if (rs.next()) {
                            calculatedTotal = rs.getBigDecimal("tongTien");
                        }
                    }
                }
            }
            
            // Step 2: Update PhieuThu with recalculated total
            phieuThu.setTongTien(calculatedTotal); // ✅ Use calculated value, not provided value
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {
                pstmt.setInt(1, phieuThu.getMaHo());
                pstmt.setInt(2, phieuThu.getMaDot());
                pstmt.setInt(3, phieuThu.getMaTaiKhoan());
                pstmt.setTimestamp(4, convertToSqlTimestamp(phieuThu.getNgayLap()));
                pstmt.setBigDecimal(5, phieuThu.getTongTien()); // ✅ Now uses calculated value
                pstmt.setString(6, phieuThu.getTrangThai());
                pstmt.setString(7, phieuThu.getHinhThucThu());
                pstmt.setString(8, null);
                pstmt.setInt(9, phieuThu.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error updating PhieuThu with id: " + phieuThu.getId(), e);
        e.printStackTrace();
        return false;
    }
}
```

---

## 2. Price Snapshotting (Lưu vết giá)

### Requirement
When creating `ChiTietThu`, the code MUST **copy** the current `donGia` from `KhoanThu` to `ChiTietThu.donGia` at that moment. This ensures historical invoices remain unchanged even if `KhoanThu.donGia` is updated later.

### Current Implementation Analysis

#### ✅ **SAFE:** `generateReceiptsForDrive()`
```java:547:552:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
ChiTietThu chiTiet = new ChiTietThu();
chiTiet.setMaKhoan(fee.getId());
chiTiet.setSoLuong(BigDecimal.ONE);
chiTiet.setDonGia(fee.getDonGia()); // ✅ Copies price from KhoanThu
chiTiet.setThanhTien(feeAmount);
```

**Status:** ✅ **SAFE** - Price is snapshotted from `KhoanThu`.

---

#### ⚠️ **RISK:** `createPhieuThuWithDetails()`
```java:264:279:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
for (ChiTietThu chiTiet : chiTietList) {
    chiTiet.setMaPhieu(maPhieu);
    pstmt.setInt(1, chiTiet.getMaPhieu());
    pstmt.setInt(2, chiTiet.getMaKhoan());
    pstmt.setBigDecimal(3, chiTiet.getSoLuong());
    pstmt.setBigDecimal(4, chiTiet.getDonGia()); // ⚠️ Uses provided value
    pstmt.setBigDecimal(5, chiTiet.getThanhTien());
}
```

**Problem:** Accepts `donGia` from caller without verification. If frontend/API doesn't snapshot, historical integrity is lost.

**Risk:** MEDIUM - Depends on caller behavior.

**Impact:** If caller doesn't snapshot, old invoices can be affected by future price changes.

---

#### ❌ **CRITICAL RISK:** `addChiTietThu()`
```java:179:211:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
public boolean addChiTietThu(ChiTietThu chiTiet) {
    // ... inserts ChiTietThu with provided donGia ...
    pstmt.setBigDecimal(4, chiTiet.getDonGia()); // ❌ No snapshot from KhoanThu
}
```

**Problem:** Does NOT fetch and snapshot `donGia` from `KhoanThu`. Relies on caller to provide correct price.

**Risk:** HIGH - If caller provides wrong price, data integrity is compromised.

**Impact:** Historical invoices may have incorrect prices.

---

### Code Fix for Price Snapshotting

```java
/**
 * Snapshot donGia from KhoanThu and set it in ChiTietThu.
 * @param chiTiet ChiTietThu to update (must have maKhoan set)
 * @param conn Database connection (for transaction support)
 * @return true if successful, false if KhoanThu not found
 */
private boolean snapshotDonGiaFromKhoanThu(ChiTietThu chiTiet, Connection conn) {
    if (chiTiet == null || chiTiet.getMaKhoan() <= 0) {
        return false;
    }
    
    try {
        String query = "SELECT donGia FROM KhoanThu WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, chiTiet.getMaKhoan());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal donGia = rs.getBigDecimal("donGia");
                    chiTiet.setDonGia(donGia); // ✅ Snapshot current price
                    logger.log(Level.FINE, "Snapshotted donGia: " + donGia + 
                        " from KhoanThu id: " + chiTiet.getMaKhoan());
                    return true;
                } else {
                    logger.log(Level.WARNING, "KhoanThu not found for id: " + chiTiet.getMaKhoan());
                    return false;
                }
            }
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error snapshotting donGia from KhoanThu id: " + chiTiet.getMaKhoan(), e);
        return false;
    }
}

// Update addChiTietThu():
@Override
public boolean addChiTietThu(ChiTietThu chiTiet) {
    if (chiTiet == null) {
        logger.log(Level.WARNING, "Cannot add ChiTietThu: chiTiet is null");
        return false;
    }

    try (Connection conn = DatabaseConnector.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // ✅ FIX: Snapshot donGia from KhoanThu
            if (!snapshotDonGiaFromKhoanThu(chiTiet, conn)) {
                conn.rollback();
                logger.log(Level.WARNING, "Failed to snapshot donGia from KhoanThu");
                return false;
            }
            
            // Recalculate thanhTien if needed
            if (chiTiet.getThanhTien() == null && chiTiet.getSoLuong() != null) {
                BigDecimal thanhTien = chiTiet.getDonGia().multiply(chiTiet.getSoLuong());
                chiTiet.setThanhTien(thanhTien);
            }
            
            // Insert ChiTietThu
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_CHITIET)) {
                pstmt.setInt(1, chiTiet.getMaPhieu());
                pstmt.setInt(2, chiTiet.getMaKhoan());
                pstmt.setBigDecimal(3, chiTiet.getSoLuong());
                pstmt.setBigDecimal(4, chiTiet.getDonGia()); // ✅ Now uses snapshotted value
                pstmt.setBigDecimal(5, chiTiet.getThanhTien());
                pstmt.setString(6, null);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected <= 0) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Recalculate PhieuThu.tongTien
            if (!recalculatePhieuThuTotal(chiTiet.getMaPhieu(), conn)) {
                conn.rollback();
                return false;
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error adding ChiTietThu for maPhieu: " + chiTiet.getMaPhieu(), e);
        e.printStackTrace();
        return false;
    }
}

// Update createPhieuThuWithDetails():
@Override
public int createPhieuThuWithDetails(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
    // ... existing validation code ...
    
    try (Connection conn = DatabaseConnector.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // Step 1: Create PhieuThu
            int maPhieu = -1;
            // ... existing PhieuThu creation code ...
            
            // Step 2: Add all ChiTietThu with price snapshotting
            if (chiTietList != null && !chiTietList.isEmpty()) {
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_CHITIET)) {
                    for (ChiTietThu chiTiet : chiTietList) {
                        chiTiet.setMaPhieu(maPhieu);
                        
                        // ✅ FIX: Snapshot donGia from KhoanThu
                        if (!snapshotDonGiaFromKhoanThu(chiTiet, conn)) {
                            conn.rollback();
                            logger.log(Level.WARNING, "Failed to snapshot donGia for KhoanThu id: " + chiTiet.getMaKhoan());
                            return -1;
                        }
                        
                        // Recalculate thanhTien if needed
                        if (chiTiet.getThanhTien() == null && chiTiet.getSoLuong() != null) {
                            BigDecimal thanhTien = chiTiet.getDonGia().multiply(chiTiet.getSoLuong());
                            chiTiet.setThanhTien(thanhTien);
                        }
                        
                        pstmt.setInt(1, chiTiet.getMaPhieu());
                        pstmt.setInt(2, chiTiet.getMaKhoan());
                        pstmt.setBigDecimal(3, chiTiet.getSoLuong());
                        pstmt.setBigDecimal(4, chiTiet.getDonGia()); // ✅ Now uses snapshotted value
                        pstmt.setBigDecimal(5, chiTiet.getThanhTien());
                        pstmt.setString(6, null);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                
                // ✅ FIX: Recalculate tongTien from ChiTietThu sum
                BigDecimal calculatedTotal = BigDecimal.ZERO;
                for (ChiTietThu chiTiet : chiTietList) {
                    if (chiTiet.getThanhTien() != null) {
                        calculatedTotal = calculatedTotal.add(chiTiet.getThanhTien());
                    }
                }
                phieuThu.setTongTien(calculatedTotal);
                
                // Update PhieuThu.tongTien in database
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE PhieuThu SET tongTien = ? WHERE id = ?")) {
                    updateStmt.setBigDecimal(1, calculatedTotal);
                    updateStmt.setInt(2, maPhieu);
                    updateStmt.executeUpdate();
                }
            }
            
            conn.commit();
            return maPhieu;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error in createPhieuThuWithDetails", e);
        e.printStackTrace();
        return -1;
    }
}
```

---

## 3. Uniqueness Constraint (Ràng buộc duy nhất)

### Requirement
There MUST be a check to prevent creating **two** `PhieuThu` for the **same Household** (`maHo`) in the **same `DotThu`** (`maDot`).

### Current Implementation Analysis

#### ❌ **CRITICAL RISK:** Database Schema
```sql:71:81:dtb/tao_bang.sql
CREATE TABLE PhieuThu (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    maHo INT,
    maDot INT,
    -- ❌ NO UNIQUE CONSTRAINT on (maHo, maDot)
    ...
);
```

**Problem:** No database-level UNIQUE constraint on `(maHo, maDot)`.

**Risk:** HIGH - Database allows duplicate invoices.

---

#### ❌ **CRITICAL RISK:** `createPhieuThu()`
```java:128:176:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
public int createPhieuThu(PhieuThu phieuThu) {
    // ... validation code ...
    // ❌ NO CHECK for duplicate (maHo, maDot)
    
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(INSERT, ...)) {
        // ... inserts without duplicate check ...
    }
}
```

**Problem:** No application-level check for duplicate `(maHo, maDot)` before insertion.

**Risk:** HIGH - Double charging possible.

**Impact:** CRITICAL - Financial integrity compromised, residents can be charged twice.

---

### Code Fix for Uniqueness Constraint

#### Database Migration (SQL)
```sql
-- Add UNIQUE constraint to prevent duplicate invoices
ALTER TABLE PhieuThu 
ADD CONSTRAINT uk_phieuthu_maHo_maDot UNIQUE (maHo, maDot);
```

#### Application-Level Check
```java
/**
 * Check if a PhieuThu already exists for the same (maHo, maDot).
 * @param maHo Household ID
 * @param maDot Collection period ID
 * @param excludeId Optional PhieuThu ID to exclude from check (for updates)
 * @param conn Database connection
 * @return true if duplicate exists, false otherwise
 */
private boolean checkDuplicatePhieuThu(int maHo, int maDot, int excludeId, Connection conn) {
    try {
        String query = "SELECT COUNT(*) FROM PhieuThu WHERE maHo = ? AND maDot = ?";
        if (excludeId > 0) {
            query += " AND id != ?";
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, maHo);
            pstmt.setInt(2, maDot);
            if (excludeId > 0) {
                pstmt.setInt(3, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error checking duplicate PhieuThu", e);
        return true; // Fail-safe: assume duplicate exists on error
    }
}

// Update createPhieuThu():
@Override
public int createPhieuThu(PhieuThu phieuThu) {
    if (phieuThu == null) {
        logger.log(Level.WARNING, "Cannot create PhieuThu: phieuThu is null");
        return -1;
    }

    // Validate: Kiểm tra đợt thu có đang mở không
    if (!isDotThuOpen(phieuThu.getMaDot())) {
        // ... existing error handling ...
    }

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
        
        // ✅ FIX: Check for duplicate before insertion
        if (checkDuplicatePhieuThu(phieuThu.getMaHo(), phieuThu.getMaDot(), 0, conn)) {
            logger.log(Level.WARNING, "Duplicate PhieuThu: maHo=" + phieuThu.getMaHo() + 
                ", maDot=" + phieuThu.getMaDot());
            throw new IllegalArgumentException(
                "Đã tồn tại phiếu thu cho hộ gia đình này trong đợt thu này. " +
                "Mỗi hộ gia đình chỉ có thể có một phiếu thu trong mỗi đợt thu."
            );
        }
        
        // ... rest of insertion code ...
    } catch (IllegalArgumentException e) {
        throw e; // Re-throw business logic errors
    } catch (SQLException e) {
        // Check for unique constraint violation
        if (e.getSQLState() != null && e.getSQLState().startsWith("23")) { // PostgreSQL unique violation
            logger.log(Level.WARNING, "Duplicate PhieuThu detected by database constraint", e);
            throw new IllegalArgumentException(
                "Đã tồn tại phiếu thu cho hộ gia đình này trong đợt thu này."
            );
        }
        logger.log(Level.SEVERE, "Error creating PhieuThu", e);
        e.printStackTrace();
        return -1;
    }
}

// Update createPhieuThuWithDetails():
@Override
public int createPhieuThuWithDetails(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
    // ... existing validation code ...
    
    try (Connection conn = DatabaseConnector.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // ✅ FIX: Check for duplicate before insertion
            if (checkDuplicatePhieuThu(phieuThu.getMaHo(), phieuThu.getMaDot(), 0, conn)) {
                conn.rollback();
                logger.log(Level.WARNING, "Duplicate PhieuThu: maHo=" + phieuThu.getMaHo() + 
                    ", maDot=" + phieuThu.getMaDot());
                throw new IllegalArgumentException(
                    "Đã tồn tại phiếu thu cho hộ gia đình này trong đợt thu này."
                );
            }
            
            // ... rest of creation code ...
        } catch (IllegalArgumentException e) {
            conn.rollback();
            throw e;
        } catch (SQLException e) {
            conn.rollback();
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new IllegalArgumentException(
                    "Đã tồn tại phiếu thu cho hộ gia đình này trong đợt thu này."
                );
            }
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error in createPhieuThuWithDetails", e);
        e.printStackTrace();
        return -1;
    }
}

// Update updatePhieuThu():
@Override
public boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
    // ... existing validation code ...
    
    try (Connection conn = DatabaseConnector.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // ✅ FIX: Check for duplicate if maHo or maDot changed
            PhieuThu existingPhieuThu = getPhieuThuWithDetails(phieuThu.getId());
            if (existingPhieuThu != null) {
                boolean maHoChanged = existingPhieuThu.getMaHo() != phieuThu.getMaHo();
                boolean maDotChanged = existingPhieuThu.getMaDot() != phieuThu.getMaDot();
                
                if (maHoChanged || maDotChanged) {
                    if (checkDuplicatePhieuThu(phieuThu.getMaHo(), phieuThu.getMaDot(), phieuThu.getId(), conn)) {
                        conn.rollback();
                        throw new IllegalArgumentException(
                            "Đã tồn tại phiếu thu khác cho hộ gia đình này trong đợt thu này."
                        );
                    }
                }
            }
            
            // ... rest of update code ...
        } catch (IllegalArgumentException e) {
            conn.rollback();
            throw e;
        } catch (SQLException e) {
            conn.rollback();
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new IllegalArgumentException(
                    "Đã tồn tại phiếu thu cho hộ gia đình này trong đợt thu này."
                );
            }
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error updating PhieuThu with id: " + phieuThu.getId(), e);
        e.printStackTrace();
        return false;
    }
}
```

---

## 4. Mandatory Fee Logic

### Requirement
When generating a `PhieuThu` (via `generateReceiptsForDrive()`), the code MUST automatically include all `KhoanThu` where `batBuoc == true` (Mandatory).

### Current Implementation Analysis

#### ✅ **SAFE:** `generateReceiptsForDrive()`
```java:474:555:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
// Filter only mandatory fees (batBuoc = true)
List<KhoanThu> mandatoryFees = allFees.stream()
    .filter(KhoanThu::isBatBuoc)
    .collect(java.util.stream.Collectors.toList());

// Calculate each mandatory fee based on tinhTheo
for (KhoanThu fee : mandatoryFees) {
    BigDecimal feeAmount = calculateFeeAmount(...);
    if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
        totalAmount = totalAmount.add(feeAmount);
        ChiTietThu chiTiet = new ChiTietThu();
        chiTiet.setMaKhoan(fee.getId());
        chiTietList.add(chiTiet);
    }
}
```

**Status:** ✅ **SAFE** - All mandatory fees are automatically included.

---

#### ⚠️ **NOTE:** Manual Creation
When creating `PhieuThu` manually (not via `generateReceiptsForDrive()`), mandatory fees are NOT automatically enforced.

**Assessment:** This may be **intentional** - allowing manual creation with custom fees. However, it's worth documenting.

**Recommendation:** Consider adding a validation method that warns if mandatory fees are missing from manually created invoices.

---

## 5. Status Synchronization

### Requirement
If `DotThu` is closed (`trangThai = 'Dong'` or similar), the code MUST prevent:
- Adding new `PhieuThu`
- Editing existing `PhieuThu` (if changing to a closed `DotThu`)

### Current Implementation Analysis

#### ✅ **SAFE:** `createPhieuThu()`
```java:134:143:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
// Validate: Kiểm tra đợt thu có đang mở không
if (!isDotThuOpen(phieuThu.getMaDot())) {
    DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
    String errorMsg = "Không thể tạo phiếu thu: Đợt thu đã đóng";
    // ...
    throw new IllegalArgumentException(errorMsg);
}
```

**Status:** ✅ **SAFE** - Prevents creation when `DotThu` is closed.

---

#### ✅ **SAFE:** `createPhieuThuWithDetails()`
```java:220:229:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
// Validate: Kiểm tra đợt thu có đang mở không
if (!isDotThuOpen(phieuThu.getMaDot())) {
    // ... throws IllegalArgumentException ...
}
```

**Status:** ✅ **SAFE** - Prevents creation when `DotThu` is closed.

---

#### ⚠️ **PARTIAL RISK:** `updatePhieuThu()`
```java:930:943:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
// Validate: Nếu maDot thay đổi, kiểm tra đợt thu mới có đang mở không
PhieuThu existingPhieuThu = getPhieuThuWithDetails(phieuThu.getId());
if (existingPhieuThu != null && existingPhieuThu.getMaDot() != phieuThu.getMaDot()) {
    // Đợt thu đã thay đổi, kiểm tra đợt thu mới
    if (!isDotThuOpen(phieuThu.getMaDot())) {
        // ... throws error ...
    }
}
```

**Problem:** Only checks if `maDot` **changes**. Does NOT check if the **existing** `maDot` is closed.

**Risk:** MEDIUM - Can edit invoices for closed `DotThu` if not changing `maDot`.

**Impact:** May allow modifications to invoices in closed periods, which could be against business rules.

---

#### ❌ **CRITICAL RISK:** `addChiTietThu()`
```java:179:211:src/main/java/com/bluemoon/services/impl/PhieuThuServiceImpl.java
public boolean addChiTietThu(ChiTietThu chiTiet) {
    // ❌ NO CHECK for DotThu status
    // ... inserts ChiTietThu ...
}
```

**Problem:** Does NOT check if the `PhieuThu`'s `DotThu` is closed before adding details.

**Risk:** HIGH - Can add details to invoices in closed periods.

**Impact:** Financial integrity compromised - closed periods should be immutable.

---

### Code Fix for Status Synchronization

```java
// Update addChiTietThu():
@Override
public boolean addChiTietThu(ChiTietThu chiTiet) {
    if (chiTiet == null) {
        logger.log(Level.WARNING, "Cannot add ChiTietThu: chiTiet is null");
        return false;
    }

    try (Connection conn = DatabaseConnector.getConnection()) {
        // ✅ FIX: Check if PhieuThu's DotThu is closed
        PhieuThu phieuThu = getPhieuThuWithDetails(chiTiet.getMaPhieu());
        if (phieuThu == null) {
            logger.log(Level.WARNING, "PhieuThu not found for id: " + chiTiet.getMaPhieu());
            return false;
        }
        
        if (!isDotThuOpen(phieuThu.getMaDot())) {
            DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
            String errorMsg = "Không thể thêm chi tiết: Đợt thu đã đóng";
            if (dotThu != null) {
                errorMsg = "Không thể thêm chi tiết: Đợt thu \"" + dotThu.getTenDot() + "\" đã đóng";
            }
            logger.log(Level.WARNING, "Cannot add ChiTietThu: DotThu is closed");
            throw new IllegalArgumentException(errorMsg);
        }
        
        // ... rest of method with snapshotting and recalculation ...
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error adding ChiTietThu for maPhieu: " + chiTiet.getMaPhieu(), e);
        e.printStackTrace();
        return false;
    }
}

// Update updatePhieuThu():
@Override
public boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList) {
    // ... existing validation code ...
    
    // ✅ FIX: Check if existing DotThu is closed
    PhieuThu existingPhieuThu = getPhieuThuWithDetails(phieuThu.getId());
    if (existingPhieuThu != null) {
        // Check existing DotThu
        if (!isDotThuOpen(existingPhieuThu.getMaDot())) {
            DotThu existingDotThu = dotThuService.getDotThuById(existingPhieuThu.getMaDot());
            String errorMsg = "Không thể cập nhật phiếu thu: Đợt thu hiện tại đã đóng";
            if (existingDotThu != null) {
                errorMsg = "Không thể cập nhật phiếu thu: Đợt thu \"" + existingDotThu.getTenDot() + "\" đã đóng";
            }
            logger.log(Level.WARNING, "Cannot update PhieuThu: existing DotThu is closed");
            throw new IllegalArgumentException(errorMsg);
        }
        
        // Check new DotThu if changed
        if (existingPhieuThu.getMaDot() != phieuThu.getMaDot()) {
            if (!isDotThuOpen(phieuThu.getMaDot())) {
                DotThu newDotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
                String errorMsg = "Không thể cập nhật phiếu thu: Đợt thu mới đã đóng";
                if (newDotThu != null) {
                    errorMsg = "Không thể cập nhật phiếu thu: Đợt thu mới \"" + newDotThu.getTenDot() + "\" đã đóng";
                }
                logger.log(Level.WARNING, "Cannot update PhieuThu: new DotThu is closed");
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }
    
    // ... rest of update code ...
}
```

---

## Summary of Findings

| # | Check | Status | Risk Level | Priority |
|---|-------|--------|------------|----------|
| 1 | Summation Integrity | ❌ **FAIL** | 🔴 **HIGH** | **P0 - CRITICAL** |
| 2 | Price Snapshotting | ⚠️ **PARTIAL** | 🟡 **MEDIUM-HIGH** | **P1 - HIGH** |
| 3 | Uniqueness Constraint | ❌ **FAIL** | 🔴 **HIGH** | **P0 - CRITICAL** |
| 4 | Mandatory Fee Logic | ✅ **PASS** | 🟢 **LOW** | - |
| 5 | Status Synchronization | ⚠️ **PARTIAL** | 🟡 **MEDIUM** | **P1 - HIGH** |

---

## Recommended Action Plan

### **Priority 0 (Critical - Fix Immediately):**

1. **Add UNIQUE constraint** to database: `ALTER TABLE PhieuThu ADD CONSTRAINT uk_phieuthu_maHo_maDot UNIQUE (maHo, maDot);`

2. **Implement duplicate check** in `createPhieuThu()` and `createPhieuThuWithDetails()`.

3. **Implement summation recalculation** in `addChiTietThu()` and `updatePhieuThu()`.

### **Priority 1 (High - Fix Soon):**

4. **Implement price snapshotting** in `addChiTietThu()` and `createPhieuThuWithDetails()`.

5. **Enhance status synchronization** in `addChiTietThu()` and `updatePhieuThu()`.

### **Priority 2 (Medium - Consider):**

6. **Add validation warning** for manually created invoices missing mandatory fees.

7. **Add database trigger** to automatically maintain `PhieuThu.tongTien = SUM(ChiTietThu.thanhTien)` (defense in depth).

---

## Conclusion

The current implementation has **3 critical gaps** and **2 partial risks** that could lead to:
- Financial data inconsistency
- Double charging residents
- Historical invoice price corruption
- Unauthorized modifications to closed periods

**Recommendation:** Implement all Priority 0 and Priority 1 fixes before production deployment.

---

**End of Audit Report**

