package com.bluemoon;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.bluemoon.services.*;
import com.bluemoon.services.impl.*;
import com.bluemoon.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebServer: Headless REST API server using Javalin.
 * Automatically maps service methods to REST endpoints.
 */
public class WebServer {

    private static final int PORT = 7000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Service instances
    private final AuthService authService;
    private final TaiKhoanService taiKhoanService;
    private final KhoanThuService khoanThuService;
    private final DotThuService dotThuService;
    private final HoGiaDinhService hoGiaDinhService;
    private final NhanKhauService nhanKhauService;
    private final PhieuThuService phieuThuService;
    private final LichSuNopTienService lichSuNopTienService;
    private final ThongKeService thongKeService;

    public WebServer() {
        // Initialize all services
        this.authService = new AuthServiceImpl();
        this.taiKhoanService = new TaiKhoanServiceImpl();
        this.khoanThuService = new KhoanThuServiceImpl();
        this.dotThuService = new DotThuServiceImpl();
        this.hoGiaDinhService = new HoGiaDinhServiceImpl();
        this.nhanKhauService = new NhanKhauServiceImpl();
        this.phieuThuService = new PhieuThuServiceImpl();
        this.lichSuNopTienService = new LichSuNopTienServiceImpl();
        this.thongKeService = new ThongKeServiceImpl();
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        });

        // ========== AUTH ENDPOINTS ==========
        app.post("/api/login", this::handleLogin);
        app.post("/api/register", this::handleRegister);
        app.post("/api/change-password", this::handleChangePassword);
        app.get("/api/check-username/:username", this::handleCheckUsername);

        // ========== TAI KHOAN (Account) ENDPOINTS ==========
        app.get("/api/tai-khoan", ctx -> ctx.json(taiKhoanService.getAllTaiKhoan()));
        app.get("/api/tai-khoan/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TaiKhoan account = taiKhoanService.findById(id);
            if (account != null) {
                ctx.json(account);
            } else {
                ctx.status(404).json(createErrorResponse("Account not found"));
            }
        });
        app.get("/api/tai-khoan/username/:username", ctx -> {
            String username = ctx.pathParam("username");
            TaiKhoan account = taiKhoanService.findByUsername(username);
            if (account != null) {
                ctx.json(account);
            } else {
                ctx.status(404).json(createErrorResponse("Account not found"));
            }
        });
        app.post("/api/tai-khoan", ctx -> {
            TaiKhoan account = ctx.bodyAsClass(TaiKhoan.class);
            boolean success = taiKhoanService.addTaiKhoan(account);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Account created successfully", account));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create account"));
            }
        });
        app.put("/api/tai-khoan/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TaiKhoan account = ctx.bodyAsClass(TaiKhoan.class);
            account.setId(id);
            boolean success = taiKhoanService.updateTaiKhoan(account);
            if (success) {
                ctx.json(createSuccessResponse("Account updated successfully", account));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update account"));
            }
        });
        app.put("/api/tai-khoan/:id/status", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String status = body.get("trangThai");
            boolean success = taiKhoanService.updateStatus(id, status);
            if (success) {
                ctx.json(createSuccessResponse("Status updated successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update status"));
            }
        });

        // ========== KHOAN THU (Fee) ENDPOINTS ==========
        app.get("/api/khoan-thu", ctx -> ctx.json(khoanThuService.getAllKhoanThu()));
        app.post("/api/khoan-thu", ctx -> {
            KhoanThu khoanThu = ctx.bodyAsClass(KhoanThu.class);
            boolean success = khoanThuService.addKhoanThu(khoanThu);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Fee created successfully", khoanThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create fee"));
            }
        });
        app.put("/api/khoan-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            KhoanThu khoanThu = ctx.bodyAsClass(KhoanThu.class);
            khoanThu.setId(id);
            boolean success = khoanThuService.updateKhoanThu(khoanThu);
            if (success) {
                ctx.json(createSuccessResponse("Fee updated successfully", khoanThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update fee"));
            }
        });
        app.delete("/api/khoan-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean success = khoanThuService.deleteKhoanThu(id);
            if (success) {
                ctx.json(createSuccessResponse("Fee deleted successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to delete fee (may be in use)"));
            }
        });

        // ========== DOT THU (Collection Drive) ENDPOINTS ==========
        app.get("/api/dot-thu", ctx -> ctx.json(dotThuService.getAllDotThu()));
        app.get("/api/dot-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DotThu dotThu = dotThuService.getDotThuById(id);
            if (dotThu != null) {
                ctx.json(dotThu);
            } else {
                ctx.status(404).json(createErrorResponse("Collection drive not found"));
            }
        });
        app.get("/api/dot-thu/search/:keyword", ctx -> {
            String keyword = ctx.pathParam("keyword");
            ctx.json(dotThuService.searchDotThu(keyword));
        });
        app.post("/api/dot-thu", ctx -> {
            DotThu dotThu = ctx.bodyAsClass(DotThu.class);
            boolean success = dotThuService.addDotThu(dotThu);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Collection drive created successfully", dotThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create collection drive"));
            }
        });
        app.put("/api/dot-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            DotThu dotThu = ctx.bodyAsClass(DotThu.class);
            dotThu.setId(id);
            boolean success = dotThuService.updateDotThu(dotThu);
            if (success) {
                ctx.json(createSuccessResponse("Collection drive updated successfully", dotThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update collection drive"));
            }
        });
        app.delete("/api/dot-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean success = dotThuService.deleteDotThu(id);
            if (success) {
                ctx.json(createSuccessResponse("Collection drive deleted successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to delete collection drive (may be in use)"));
            }
        });

        // ========== HO GIA DINH (Household) ENDPOINTS ==========
        app.get("/api/ho-gia-dinh", ctx -> ctx.json(hoGiaDinhService.getAllHoGiaDinh()));
        app.get("/api/ho-gia-dinh/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            HoGiaDinh hoGiaDinh = hoGiaDinhService.findById(id);
            if (hoGiaDinh != null) {
                ctx.json(hoGiaDinh);
            } else {
                ctx.status(404).json(createErrorResponse("Household not found"));
            }
        });
        app.get("/api/ho-gia-dinh/search/:keyword", ctx -> {
            String keyword = ctx.pathParam("keyword");
            ctx.json(hoGiaDinhService.searchHoGiaDinh(keyword));
        });
        app.post("/api/ho-gia-dinh", ctx -> {
            HoGiaDinh hoGiaDinh = ctx.bodyAsClass(HoGiaDinh.class);
            boolean success = hoGiaDinhService.addHoGiaDinh(hoGiaDinh);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Household created successfully", hoGiaDinh));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create household"));
            }
        });
        app.put("/api/ho-gia-dinh/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            HoGiaDinh hoGiaDinh = ctx.bodyAsClass(HoGiaDinh.class);
            hoGiaDinh.setId(id);
            boolean success = hoGiaDinhService.updateHoGiaDinh(hoGiaDinh);
            if (success) {
                ctx.json(createSuccessResponse("Household updated successfully", hoGiaDinh));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update household"));
            }
        });
        app.delete("/api/ho-gia-dinh/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean success = hoGiaDinhService.deleteHoGiaDinh(id);
            if (success) {
                ctx.json(createSuccessResponse("Household deleted successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to delete household (may be in use)"));
            }
        });

        // ========== NHAN KHAU (Resident) ENDPOINTS ==========
        app.get("/api/nhan-khau", ctx -> ctx.json(nhanKhauService.getAll()));
        app.get("/api/nhan-khau/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            NhanKhau nhanKhau = nhanKhauService.findById(id);
            if (nhanKhau != null) {
                ctx.json(nhanKhau);
            } else {
                ctx.status(404).json(createErrorResponse("Resident not found"));
            }
        });
        app.get("/api/nhan-khau/ho-gia-dinh/:maHo", ctx -> {
            int maHo = Integer.parseInt(ctx.pathParam("maHo"));
            ctx.json(nhanKhauService.getNhanKhauByHoGiaDinh(maHo));
        });
        app.get("/api/nhan-khau/:id/lich-su", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(nhanKhauService.getLichSuNhanKhau(id));
        });
        app.post("/api/nhan-khau", ctx -> {
            NhanKhau nhanKhau = ctx.bodyAsClass(NhanKhau.class);
            boolean success = nhanKhauService.addNhanKhau(nhanKhau);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Resident created successfully", nhanKhau));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create resident"));
            }
        });
        app.put("/api/nhan-khau/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            NhanKhau nhanKhau = ctx.bodyAsClass(NhanKhau.class);
            nhanKhau.setId(id);
            boolean success = nhanKhauService.updateNhanKhau(nhanKhau);
            if (success) {
                ctx.json(createSuccessResponse("Resident updated successfully", nhanKhau));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update resident"));
            }
        });
        app.delete("/api/nhan-khau/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean success = nhanKhauService.deleteNhanKhau(id);
            if (success) {
                ctx.json(createSuccessResponse("Resident deleted successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to delete resident"));
            }
        });
        app.post("/api/nhan-khau/lich-su", ctx -> {
            LichSuNhanKhau history = ctx.bodyAsClass(LichSuNhanKhau.class);
            boolean success = nhanKhauService.addLichSuNhanKhau(history);
            if (success) {
                ctx.status(201).json(createSuccessResponse("History record created successfully", history));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create history record"));
            }
        });
        app.put("/api/nhan-khau/:id/status", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String newStatus = (String) body.get("newStatus");
            LichSuNhanKhau historyRecord = body.containsKey("historyRecord") && body.get("historyRecord") != null
                    ? objectMapper.convertValue(body.get("historyRecord"), LichSuNhanKhau.class)
                    : null;
            boolean success = nhanKhauService.updateStatusWithHistory(id, newStatus, historyRecord);
            if (success) {
                ctx.json(createSuccessResponse("Status updated successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update status"));
            }
        });

        // ========== PHIEU THU (Receipt) ENDPOINTS ==========
        app.get("/api/phieu-thu", ctx -> ctx.json(phieuThuService.getAllPhieuThu()));
        app.get("/api/phieu-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            PhieuThu phieuThu = phieuThuService.getPhieuThuWithDetails(id);
            if (phieuThu != null) {
                ctx.json(phieuThu);
            } else {
                ctx.status(404).json(createErrorResponse("Receipt not found"));
            }
        });
        app.get("/api/phieu-thu/:id/chi-tiet", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(phieuThuService.getChiTietThuByPhieu(id));
        });
        app.get("/api/phieu-thu/ho-gia-dinh/:maHo", ctx -> {
            int maHo = Integer.parseInt(ctx.pathParam("maHo"));
            ctx.json(phieuThuService.findPhieuThuByHoGiaDinh(maHo));
        });
        app.get("/api/phieu-thu/dot-thu/:maDotThu", ctx -> {
            int maDotThu = Integer.parseInt(ctx.pathParam("maDotThu"));
            ctx.json(phieuThuService.findPhieuThuByDotThu(maDotThu));
        });
        app.post("/api/phieu-thu", ctx -> {
            PhieuThu phieuThu = ctx.bodyAsClass(PhieuThu.class);
            int maPhieu = phieuThuService.createPhieuThu(phieuThu);
            if (maPhieu > 0) {
                phieuThu.setId(maPhieu);
                ctx.status(201).json(createSuccessResponse("Receipt created successfully", phieuThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create receipt"));
            }
        });
        app.post("/api/phieu-thu/with-details", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            PhieuThu phieuThu = objectMapper.convertValue(body.get("phieuThu"), PhieuThu.class);
            @SuppressWarnings("unchecked")
            List<ChiTietThu> chiTietList = objectMapper.convertValue(body.get("chiTietList"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ChiTietThu.class));
            int maPhieu = phieuThuService.createPhieuThuWithDetails(phieuThu, chiTietList);
            if (maPhieu > 0) {
                phieuThu.setId(maPhieu);
                ctx.status(201).json(createSuccessResponse("Receipt with details created successfully", phieuThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create receipt with details"));
            }
        });
        app.post("/api/phieu-thu/chi-tiet", ctx -> {
            ChiTietThu chiTiet = ctx.bodyAsClass(ChiTietThu.class);
            boolean success = phieuThuService.addChiTietThu(chiTiet);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Detail added successfully", chiTiet));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to add detail"));
            }
        });
        app.put("/api/phieu-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            PhieuThu phieuThu = objectMapper.convertValue(body.get("phieuThu"), PhieuThu.class);
            phieuThu.setId(id);
            @SuppressWarnings("unchecked")
            List<ChiTietThu> chiTietList = body.containsKey("chiTietList") && body.get("chiTietList") != null
                    ? objectMapper.convertValue(body.get("chiTietList"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, ChiTietThu.class))
                    : null;
            boolean success = phieuThuService.updatePhieuThu(phieuThu, chiTietList);
            if (success) {
                ctx.json(createSuccessResponse("Receipt updated successfully", phieuThu));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update receipt (may be paid)"));
            }
        });
        app.put("/api/phieu-thu/:id/status", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String status = body.get("newStatus");
            boolean success = phieuThuService.updatePhieuThuStatus(id, status);
            if (success) {
                ctx.json(createSuccessResponse("Status updated successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update status"));
            }
        });
        app.delete("/api/phieu-thu/:id", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean success = phieuThuService.deletePhieuThu(id);
            if (success) {
                ctx.json(createSuccessResponse("Receipt deleted successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to delete receipt (may be paid)"));
            }
        });
        app.post("/api/phieu-thu/generate/:maDot", ctx -> {
            int maDot = Integer.parseInt(ctx.pathParam("maDot"));
            int count = phieuThuService.generateReceiptsForDrive(maDot);
            ctx.json(createSuccessResponse("Generated " + count + " receipts", count));
        });
        app.get("/api/phieu-thu/ho-gia-dinh/:maHo/unpaid", ctx -> {
            int maHo = Integer.parseInt(ctx.pathParam("maHo"));
            boolean hasUnpaid = phieuThuService.hasUnpaidFees(maHo);
            ctx.json(createSuccessResponse("Check completed", hasUnpaid));
        });

        // ========== LICH SU NOP TIEN (Payment History) ENDPOINTS ==========
        app.get("/api/lich-su-nop-tien", ctx -> ctx.json(lichSuNopTienService.getAllLichSuNopTien()));
        app.get("/api/lich-su-nop-tien/phieu-thu/:maPhieu", ctx -> {
            int maPhieu = Integer.parseInt(ctx.pathParam("maPhieu"));
            ctx.json(lichSuNopTienService.getLichSuNopTienByPhieuThu(maPhieu));
        });
        app.get("/api/lich-su-nop-tien/ho-gia-dinh/:maHo", ctx -> {
            int maHo = Integer.parseInt(ctx.pathParam("maHo"));
            ctx.json(lichSuNopTienService.getLichSuNopTienByHoGiaDinh(maHo));
        });
        app.post("/api/lich-su-nop-tien", ctx -> {
            LichSuNopTien payment = ctx.bodyAsClass(LichSuNopTien.class);
            boolean success = lichSuNopTienService.addLichSuNopTien(payment);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Payment record created successfully", payment));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create payment record"));
            }
        });
        app.post("/api/lich-su-nop-tien/with-status-update", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            LichSuNopTien payment = objectMapper.convertValue(body.get("paymentRecord"), LichSuNopTien.class);
            String updateStatusTo = (String) body.get("updateStatusTo");
            boolean success = lichSuNopTienService.recordPaymentWithStatusUpdate(payment, updateStatusTo);
            if (success) {
                ctx.status(201).json(createSuccessResponse("Payment recorded and status updated successfully", payment));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to record payment"));
            }
        });

        // ========== THONG KE (Statistics) ENDPOINTS ==========
        app.get("/api/thong-ke/dashboard", ctx -> ctx.json(thongKeService.getDashboardStats()));
        app.get("/api/thong-ke/revenue", ctx -> {
            String fromDateStr = ctx.queryParam("fromDate");
            String toDateStr = ctx.queryParam("toDate");
            if (fromDateStr == null || toDateStr == null) {
                ctx.status(400).json(createErrorResponse("fromDate and toDate query parameters are required"));
                return;
            }
            LocalDate fromDate = LocalDate.parse(fromDateStr);
            LocalDate toDate = LocalDate.parse(toDateStr);
            ctx.json(thongKeService.getRevenueStats(fromDate, toDate));
        });
        app.get("/api/thong-ke/revenue/total", ctx -> {
            String fromDateStr = ctx.queryParam("fromDate");
            String toDateStr = ctx.queryParam("toDate");
            if (fromDateStr == null || toDateStr == null) {
                ctx.status(400).json(createErrorResponse("fromDate and toDate query parameters are required"));
                return;
            }
            LocalDate fromDate = LocalDate.parse(fromDateStr);
            LocalDate toDate = LocalDate.parse(toDateStr);
            ctx.json(createSuccessResponse("Total revenue", thongKeService.getTotalRevenue(fromDate, toDate)));
        });
        app.get("/api/thong-ke/revenue/details", ctx -> {
            String fromDateStr = ctx.queryParam("fromDate");
            String toDateStr = ctx.queryParam("toDate");
            if (fromDateStr == null || toDateStr == null) {
                ctx.status(400).json(createErrorResponse("fromDate and toDate query parameters are required"));
                return;
            }
            LocalDate fromDate = LocalDate.parse(fromDateStr);
            LocalDate toDate = LocalDate.parse(toDateStr);
            ctx.json(thongKeService.getRevenueDetails(fromDate, toDate));
        });
        app.get("/api/thong-ke/debt", ctx -> ctx.json(thongKeService.getDebtStats()));
        app.get("/api/thong-ke/debt/total", ctx -> 
            ctx.json(createSuccessResponse("Total debt", thongKeService.getTotalDebt())));
        app.get("/api/thong-ke/debt/details", ctx -> ctx.json(thongKeService.getDebtStats()));
        app.get("/api/thong-ke/report/:maDotThu", ctx -> {
            int maDotThu = Integer.parseInt(ctx.pathParam("maDotThu"));
            ctx.json(thongKeService.generateCollectionReport(maDotThu));
        });

        // Health check endpoint
        app.get("/api/health", ctx -> ctx.json(createSuccessResponse("Server is running", null)));

        app.start(PORT);
        System.out.println("WebServer started on port " + PORT);
        System.out.println("API available at http://localhost:" + PORT + "/api");
    }

    // ========== AUTH HANDLERS ==========
    private void handleLogin(Context ctx) {
        try {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String username = body.get("username");
            String password = body.get("password");

            if (username == null || password == null) {
                ctx.status(400).json(createErrorResponse("Username and password are required"));
                return;
            }

            TaiKhoan account = authService.login(username, password);
            if (account != null) {
                // Don't send password back
                account.setMatKhau(null);
                ctx.json(createSuccessResponse("Login successful", account));
            } else {
                ctx.status(401).json(createErrorResponse("Invalid credentials or account is locked"));
            }
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Login error: " + e.getMessage()));
        }
    }

    private void handleRegister(Context ctx) {
        try {
            TaiKhoan account = ctx.bodyAsClass(TaiKhoan.class);
            boolean success = authService.register(account);
            if (success) {
                account.setMatKhau(null); // Don't send password back
                ctx.status(201).json(createSuccessResponse("Account registered successfully", account));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to register account (username may already exist)"));
            }
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Registration error: " + e.getMessage()));
        }
    }

    private void handleChangePassword(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            int id = ((Number) body.get("id")).intValue();
            String oldPassword = (String) body.get("oldPassword");
            String newPassword = (String) body.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                ctx.status(400).json(createErrorResponse("oldPassword and newPassword are required"));
                return;
            }

            boolean success = authService.changePassword(id, oldPassword, newPassword);
            if (success) {
                ctx.json(createSuccessResponse("Password changed successfully", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to change password (invalid old password)"));
            }
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Change password error: " + e.getMessage()));
        }
    }

    private void handleCheckUsername(Context ctx) {
        try {
            String username = ctx.pathParam("username");
            boolean exists = authService.isUsernameExist(username);
            ctx.json(createSuccessResponse("Check completed", exists));
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Check username error: " + e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}

