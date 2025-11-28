package com.bluemoon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.AuthService;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.impl.AuthServiceImpl;
import com.bluemoon.services.impl.HoGiaDinhServiceImpl;
import com.bluemoon.utils.DatabaseConnector;

/**
 * Test application để kiểm tra Service layer logic mà không cần JavaFX UI.
 * Sử dụng System.out.println để hiển thị kết quả test.
 */
public class TestApp {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("BlueMoon Manager - Service Layer Test");
        System.out.println("=========================================\n");

        // Test 1: Database Connection
        testDatabaseConnection();

        // Test 2: Login
        testLogin();

        // Test 3: Household Operations
        testHouseholdOperations();

        System.out.println("\n=========================================");
        System.out.println("Test completed!");
        System.out.println("=========================================");
    }

    /**
     * Test 1: Kiểm tra kết nối database.
     */
    private static void testDatabaseConnection() {
        System.out.println("--- Test 1: Database Connection ---");
        try {
            Connection conn = DatabaseConnector.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection: SUCCESS");
                System.out.println("  Connection URL: " + conn.getMetaData().getURL());
                System.out.println("  Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("  Database Version: " + conn.getMetaData().getDatabaseProductVersion());
                conn.close();
            } else {
                System.out.println("✗ Database connection: FAILED (Connection is null or closed)");
            }
        } catch (SQLException e) {
            System.out.println("✗ Database connection: FAILED");
            System.out.println("  Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("✗ Database connection: FAILED (Unexpected error)");
            System.out.println("  Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test 2: Kiểm tra chức năng đăng nhập.
     */
    private static void testLogin() {
        System.out.println("--- Test 2: Login ---");
        try {
            AuthService authService = new AuthServiceImpl();

            // Test với username "admin" và password "123456"
            System.out.println("Attempting login with username: 'admin', password: '123456'");
            TaiKhoan taiKhoan = authService.login("admin", "123456");

            if (taiKhoan != null) {
                System.out.println("✓ Login Success: " + taiKhoan.getHoTen());
                System.out.println("  User ID: " + taiKhoan.getId());
                System.out.println("  Username: " + taiKhoan.getTenDangNhap());
                System.out.println("  Role: " + taiKhoan.getVaiTro());
                System.out.println("  Status: " + taiKhoan.getTrangThai());
            } else {
                System.out.println("✗ Login Failed: Invalid credentials or account is locked");
            }

            // Test với credentials không hợp lệ
            System.out.println("\nAttempting login with invalid credentials...");
            TaiKhoan invalidLogin = authService.login("invalid_user", "wrong_password");
            if (invalidLogin == null) {
                System.out.println("✓ Invalid login correctly rejected");
            } else {
                System.out.println("✗ Invalid login incorrectly accepted");
            }

        } catch (Exception e) {
            System.out.println("✗ Login test: FAILED (Exception occurred)");
            System.out.println("  Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * Test 3: Kiểm tra các operations của Household Service.
     */
    private static void testHouseholdOperations() {
        System.out.println("--- Test 3: Household Operations ---");
        try {
            HoGiaDinhService hoGiaDinhService = new HoGiaDinhServiceImpl();

            // Test getAllHoGiaDinh()
            System.out.println("Testing getAllHoGiaDinh()...");
            var allHouseholds = hoGiaDinhService.getAllHoGiaDinh();
            System.out.println("✓ Retrieved " + allHouseholds.size() + " household(s) from database");

            if (!allHouseholds.isEmpty()) {
                System.out.println("  First household:");
                HoGiaDinh first = allHouseholds.get(0);
                System.out.println("    - ID: " + first.getId());
                System.out.println("    - Ma Ho: " + first.getMaHo());
                System.out.println("    - So Phong: " + first.getSoPhong());
                System.out.println("    - Dien Tich: " + first.getDienTich());
            }

            // Test addHoGiaDinh()
            System.out.println("\nTesting addHoGiaDinh()...");
            HoGiaDinh dummyHousehold = new HoGiaDinh();
            dummyHousehold.setMaHo("TEST_HO_" + System.currentTimeMillis()); // Unique maHo
            dummyHousehold.setSoPhong(999);
            dummyHousehold.setDienTich(new BigDecimal("50.00"));
            dummyHousehold.setMaChuHo(1); // Assuming there's at least one NhanKhau with id=1
            dummyHousehold.setGhiChu("Test household - can be deleted");
            dummyHousehold.setNgayTao(LocalDate.now());

            boolean addResult = hoGiaDinhService.addHoGiaDinh(dummyHousehold);
            if (addResult) {
                System.out.println("✓ Add household: SUCCESS");
                System.out.println("  Added household with maHo: " + dummyHousehold.getMaHo());
            } else {
                System.out.println("✗ Add household: FAILED");
                System.out.println("  Possible reasons: maHo already exists, validation failed, or database error");
            }

            // Test checkMaHoExists()
            System.out.println("\nTesting checkMaHoExists()...");
            if (addResult) {
                boolean exists = hoGiaDinhService.checkMaHoExists(dummyHousehold.getMaHo());
                if (exists) {
                    System.out.println("✓ checkMaHoExists: SUCCESS (maHo exists as expected)");
                } else {
                    System.out.println("✗ checkMaHoExists: FAILED (maHo should exist but check returned false)");
                }
            }

            // Test findById() if we have households
            if (!allHouseholds.isEmpty()) {
                System.out.println("\nTesting findById()...");
                int testId = allHouseholds.get(0).getId();
                HoGiaDinh found = hoGiaDinhService.findById(testId);
                if (found != null) {
                    System.out.println("✓ findById: SUCCESS");
                    System.out.println("  Found household with ID " + testId + ": " + found.getMaHo());
                } else {
                    System.out.println("✗ findById: FAILED (Household not found)");
                }
            }

        } catch (Exception e) {
            System.out.println("✗ Household operations test: FAILED (Exception occurred)");
            System.out.println("  Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}

