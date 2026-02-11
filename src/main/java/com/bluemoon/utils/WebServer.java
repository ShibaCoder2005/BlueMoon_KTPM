package com.bluemoon.utils;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.bluemoon.services.*;
import com.bluemoon.services.impl.*;
import com.bluemoon.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * WebServer: Headless REST API server using Javalin.
 * Automatically maps service methods to REST endpoints.
 */
public class WebServer {

    private static final int PORT = 7070;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Logger logger = Logger.getLogger(WebServer.class.getName());

    // Service instances
    private final AuthService authService;
    private final TaiKhoanService taiKhoanService;
    private final KhoanThuService khoanThuService;
    private final DotThuService dotThuService;
    private final HoGiaDinhService hoGiaDinhService;
    private final NhanKhauService nhanKhauService;
    private final PhieuThuService phieuThuService;
    private final ThongKeService thongKeService;
    private final BaoCaoService baoCaoService;
    private final PhuongTienService phuongTienService;
    private final ChiTietThuService chiTietThuService;
    private final PhongService phongService;

    public WebServer() {
        // Initialize all services
        this.authService = new AuthServiceImpl();
        this.taiKhoanService = new TaiKhoanServiceImpl();
        this.khoanThuService = new KhoanThuServiceImpl();
        this.dotThuService = new DotThuServiceImpl();
        this.hoGiaDinhService = new HoGiaDinhServiceImpl();
        this.nhanKhauService = new NhanKhauServiceImpl();
        this.phieuThuService = new PhieuThuServiceImpl();
        this.thongKeService = new ThongKeServiceImpl();
        this.baoCaoService = new BaoCaoServiceImpl();
        this.phuongTienService = new PhuongTienServiceImpl();
        this.chiTietThuService = new ChiTietThuServiceImpl();
        this.phongService = new PhongServiceImpl();
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/";
                staticFiles.hostedPath = "/";
            });
        });

        // Global Config
        app.before("/api/*", ctx -> ctx.contentType("application/json; charset=utf-8"));
        setupGlobalErrorHandlers(app);
        setupRootEndpoints(app);

        // Setup API Routes by Category
        setupAuthRoutes(app);
        setupAccountRoutes(app);
        setupFeeRoutes(app);
        setupCollectionDriveRoutes(app);
        setupRoomRoutes(app);
        setupHouseholdRoutes(app);
        setupResidentRoutes(app);
        setupVehicleRoutes(app);
        setupReceiptRoutes(app);
        setupStatisticsRoutes(app);
        setupReportRoutes(app);
        
        // Start Server
        app.start(PORT);
        logger.info("WebServer started on port " + PORT);
        logger.info("API available at http://localhost:" + PORT + "/api");
    }

    // ===================================================================================
    // GROUP 1: SYSTEM & CONFIG ROUTES
    // ===================================================================================

    private void setupGlobalErrorHandlers(Javalin app) {
        // 404 Handler
        app.error(404, ctx -> {
            String path = ctx.path();
            if (path.startsWith("/api")) {
                ctx.status(404).json(createStandardErrorResponse("Not Found", "API_ENDPOINT_NOT_FOUND", "The requested API endpoint does not exist: " + path, path));
            } else {
                serveHtmlFile(ctx, "/index.html");
            }
        });

        // Exception Handlers
        app.exception(NumberFormatException.class, (e, ctx) -> {
            ctx.status(400).json(createStandardErrorResponse("Invalid ID format", "INVALID_ID_FORMAT", e.getMessage(), ctx.path()));
        });
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(createStandardErrorResponse("Invalid request", "INVALID_REQUEST", e.getMessage(), ctx.path()));
        });
        app.exception(Exception.class, (e, ctx) -> handleException(ctx, e));
    }

    private void setupRootEndpoints(Javalin app) {
        app.get("/", ctx -> serveHtmlFile(ctx, "/index.html"));
        
        app.get("/api", ctx -> {
            Map<String, Object> apiInfo = new HashMap<>();
            apiInfo.put("status", "running");
            apiInfo.put("version", "1.0.0");
            apiInfo.put("endpoints", Map.of(
                "auth", "/api/login",
                "accounts", "/api/tai-khoan",
                "statistics", "/api/thong-ke/dashboard"
            ));
            ctx.json(createSuccessResponse("BlueMoon KTPM API", apiInfo));
        });

        app.get("/api/health", ctx -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ctx.json(createSuccessResponse("Server is running", health));
        });
    }

    // ===================================================================================
    // GROUP 2: AUTH & ACCOUNT ROUTES
    // ===================================================================================

    private void setupAuthRoutes(Javalin app) {
        app.post("/api/login", this::handleLogin);
        app.post("/api/change-password", this::handleChangePassword);
        app.get("/api/check-username/{username}", this::handleCheckUsername);
    }

    private void setupAccountRoutes(Javalin app) {
        app.get("/api/tai-khoan", ctx -> ctx.json(taiKhoanService.getAllTaiKhoan()));
        
        app.get("/api/tai-khoan/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null) {
                TaiKhoan acc = taiKhoanService.findById(id);
                if (acc != null) ctx.json(acc); else ctx.status(404).json(createErrorResponse("Account not found"));
            }
        });

        app.post("/api/tai-khoan", ctx -> {
            TaiKhoan account = ctx.bodyAsClass(TaiKhoan.class);
            if (taiKhoanService.addTaiKhoan(account)) {
                ctx.status(201).json(createSuccessResponse("Account created", account));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create account"));
            }
        });

        app.put("/api/tai-khoan/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            @SuppressWarnings("unchecked")
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            
            TaiKhoan acc = new TaiKhoan();
            acc.setId(id);
            acc.setTenDangNhap((String) body.get("tenDangNhap"));
            acc.setHoTen((String) body.get("hoTen"));
            acc.setVaiTro((String) body.get("vaiTro"));
            acc.setDienThoai((String) body.get("dienThoai"));
            if (body.get("matKhau") != null && !body.get("matKhau").toString().isEmpty()) {
                acc.setMatKhau(body.get("matKhau").toString());
            }
            
            if (taiKhoanService.updateTaiKhoan(acc)) {
                ctx.json(createSuccessResponse("Account updated", acc));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update account"));
            }
        });

        app.delete("/api/tai-khoan/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null) {
                if (taiKhoanService.deleteTaiKhoan(id)) ctx.json(createSuccessResponse("Deleted", null));
                else ctx.status(400).json(createErrorResponse("Failed to delete"));
            }
        });

        app.put("/api/tai-khoan/{id}/status", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            @SuppressWarnings("unchecked")
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String status = (String) body.get("trangThai");
            if (taiKhoanService.updateStatus(id, status)) {
                ctx.json(createSuccessResponse("Status updated", null));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to update status"));
            }
        });
    }

    // ===================================================================================
    // GROUP 3: MASTER DATA (Fees, Rooms, Collection Drives)
    // ===================================================================================

    private void setupFeeRoutes(Javalin app) {
        app.get("/api/khoan-thu", ctx -> ctx.json(khoanThuService.getAllKhoanThu()));
        
        app.post("/api/khoan-thu", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            KhoanThu kt = new KhoanThu();
            kt.setTenKhoanThu((String) body.get("tenKhoanThu"));
            kt.setMoTa((String) body.get("moTa"));
            
            Object donGiaObj = body.get("donGia");
            kt.setDonGia(donGiaObj instanceof Number ? BigDecimal.valueOf(((Number) donGiaObj).doubleValue()) : BigDecimal.ZERO);

            String loaiPhi = (String) body.get("loaiPhi");
            if ("BatBuoc".equals(loaiPhi)) {
                kt.setLoai("Bắt buộc"); kt.setBatBuoc(true); kt.setLoaiKhoanThu(0);
            } else {
                kt.setLoai("Tự nguyện"); kt.setBatBuoc(false); kt.setLoaiKhoanThu(1);
            }
            
            String tinhTheo = (String) body.get("tinhTheo");
            if (tinhTheo != null) {
                switch (tinhTheo.toLowerCase()) {
                    case "dientich": kt.setTinhTheo("Diện tích"); kt.setDonViTinh("VNĐ/m²"); break;
                    case "nhankhau": kt.setTinhTheo("Nhân khẩu"); kt.setDonViTinh("VNĐ/người"); break;
                    case "hokhau": kt.setTinhTheo("Hộ khẩu"); kt.setDonViTinh("VNĐ/hộ"); break;
                    case "xemay": kt.setTinhTheo("Xe máy"); kt.setDonViTinh("VNĐ/xe máy"); break;
                    case "oto": kt.setTinhTheo("Ô tô"); kt.setDonViTinh("VNĐ/ô tô"); break;
                    default: kt.setTinhTheo(tinhTheo); kt.setDonViTinh("VNĐ");
                }
            } else {
                kt.setTinhTheo("Diện tích"); kt.setDonViTinh("VNĐ/m²");
            }

            if (khoanThuService.addKhoanThu(kt)) {
                ctx.status(201).json(createSuccessResponse("Fee created", kt));
            } else {
                ctx.status(400).json(createErrorResponse("Failed to create fee"));
            }
        });

        app.put("/api/khoan-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            KhoanThu kt = ctx.bodyAsClass(KhoanThu.class);
            kt.setId(id);
            if (khoanThuService.updateKhoanThu(kt)) ctx.json(createSuccessResponse("Updated", kt));
            else ctx.status(400).json(createErrorResponse("Update failed"));
        });

        app.delete("/api/khoan-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null && khoanThuService.deleteKhoanThu(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });
    }

    private void setupCollectionDriveRoutes(Javalin app) {
        app.get("/api/dot-thu", ctx -> ctx.json(dotThuService.getAllDotThu()));
        app.get("/api/dot-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null) ctx.json(dotThuService.getDotThuById(id));
        });
        
        app.post("/api/dot-thu", ctx -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = objectMapper.readValue(ctx.body(), Map.class);
            int userId = body.get("nguoiTaoId") != null ? Integer.parseInt(body.get("nguoiTaoId").toString()) : 1;
            body.remove("nguoiTaoId");
            DotThu dotThu = objectMapper.convertValue(body, DotThu.class);
            
            int newId = dotThuService.createDotThuWithReceipts(dotThu, userId);
            if (newId != -1) {
                dotThu.setId(newId);
                ctx.status(201).json(createSuccessResponse("Drive created", dotThu));
            } else {
                ctx.status(400).json(createErrorResponse("Create failed"));
            }
        });

        // [BỔ SUNG] API Cập nhật Đợt thu
        app.put("/api/dot-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            DotThu dotThu = ctx.bodyAsClass(DotThu.class);
            dotThu.setId(id);
            if (dotThuService.updateDotThu(dotThu)) ctx.json(createSuccessResponse("Drive updated", dotThu));
            else ctx.status(400).json(createErrorResponse("Update failed"));
        });

        app.delete("/api/dot-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null && dotThuService.deleteDotThu(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });
    }

    private void setupRoomRoutes(Javalin app) {
        // 1. Lấy danh sách
        app.get("/api/phong", ctx -> ctx.json(phongService.getAllPhong()));
        
        // 2. Lấy chi tiết [BỔ SUNG QUAN TRỌNG]
        app.get("/api/phong/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null) {
                Phong p = phongService.getPhongById(id);
                if (p != null) ctx.json(p); 
                else ctx.status(404).json(createErrorResponse("Room not found"));
            }
        });
        
        // 3. Thêm mới
        app.post("/api/phong", ctx -> {
            Phong p = ctx.bodyAsClass(Phong.class);
            if(phongService.addPhong(p)) ctx.status(201).json(createSuccessResponse("Room created", p));
            else ctx.status(400).json(createErrorResponse("Create failed"));
        });

        // 4. Cập nhật
        app.put("/api/phong/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id == null) return;
            Phong p = ctx.bodyAsClass(Phong.class);
            p.setSoPhong(id);
            if(phongService.updatePhong(p)) ctx.json(createSuccessResponse("Room updated", null));
            else ctx.status(400).json(createErrorResponse("Update failed"));
        });

        // 5. Xóa
        app.delete("/api/phong/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null && phongService.deletePhong(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });
    }

    // ===================================================================================
    // GROUP 4: RESIDENTS & HOUSEHOLDS
    // ===================================================================================

    private void setupHouseholdRoutes(Javalin app) {
        app.get("/api/ho-gia-dinh", ctx -> ctx.json(hoGiaDinhService.getAllHoGiaDinh()));
        
        app.get("/api/ho-gia-dinh/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null) ctx.json(hoGiaDinhService.findById(id));
        });
        
        app.get("/api/ho-gia-dinh/search/{keyword}", ctx -> {
             String keyword = ctx.pathParam("keyword");
             ctx.json(hoGiaDinhService.searchHoGiaDinh(keyword));
        });

        app.post("/api/ho-gia-dinh", ctx -> {
            HoGiaDinh h = ctx.bodyAsClass(HoGiaDinh.class);
            if(hoGiaDinhService.addHoGiaDinh(h)) ctx.status(201).json(createSuccessResponse("Household created", h));
            else ctx.status(400).json(createErrorResponse("Create failed"));
        });

        // [BỔ SUNG QUAN TRỌNG] Cập nhật Hộ gia đình
        app.put("/api/ho-gia-dinh/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            HoGiaDinh h = ctx.bodyAsClass(HoGiaDinh.class);
            h.setId(id);
            if(hoGiaDinhService.updateHoGiaDinh(h)) ctx.json(createSuccessResponse("Household updated", h));
            else ctx.status(400).json(createErrorResponse("Update failed"));
        });

        app.delete("/api/ho-gia-dinh/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null && hoGiaDinhService.deleteHoGiaDinh(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });
    }

    private void setupResidentRoutes(Javalin app) {
        app.get("/api/nhan-khau", ctx -> ctx.json(nhanKhauService.getAll()));
        
        // [BỔ SUNG QUAN TRỌNG] Lấy chi tiết nhân khẩu
        app.get("/api/nhan-khau/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null) {
                NhanKhau nk = nhanKhauService.findById(id);
                if (nk != null) ctx.json(nk); else ctx.status(404).json(createErrorResponse("Resident not found"));
            }
        });

        app.get("/api/nhan-khau/ho-gia-dinh/{maHo}", ctx -> {
            Integer maHo = parseIntSafe(ctx, "maHo");
            if(maHo != null) ctx.json(nhanKhauService.getNhanKhauByHoGiaDinh(maHo));
        });

        app.post("/api/nhan-khau", ctx -> {
            NhanKhau nk = ctx.bodyAsClass(NhanKhau.class);
            if(nhanKhauService.addNhanKhau(nk)) ctx.status(201).json(createSuccessResponse("Resident created", nk));
            else ctx.status(400).json(createErrorResponse("Create failed"));
        });

        // [BỔ SUNG QUAN TRỌNG] Cập nhật thông tin nhân khẩu
        app.put("/api/nhan-khau/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            NhanKhau nk = ctx.bodyAsClass(NhanKhau.class);
            nk.setId(id);
            if(nhanKhauService.updateNhanKhau(nk)) ctx.json(createSuccessResponse("Resident updated", nk));
            else ctx.status(400).json(createErrorResponse("Update failed"));
        });

        app.delete("/api/nhan-khau/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null && nhanKhauService.deleteNhanKhau(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });
        
        // --- CÁC API LỊCH SỬ & TRẠNG THÁI ---
        app.get("/api/nhan-khau/lich-su/all", ctx -> ctx.json(nhanKhauService.getAllHistory()));
        
        app.get("/api/nhan-khau/{id}/lich-su", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null) ctx.json(nhanKhauService.getLichSuNhanKhau(id));
        });
        
        app.post("/api/nhan-khau/lich-su", ctx -> {
            LichSuNhanKhau history = ctx.bodyAsClass(LichSuNhanKhau.class);
            if(nhanKhauService.addLichSuNhanKhau(history)) 
                ctx.status(201).json(createSuccessResponse("History created", history));
            else 
                ctx.status(400).json(createErrorResponse("Failed to create history"));
        });
        
        app.put("/api/nhan-khau/{id}/status", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            @SuppressWarnings("unchecked")
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String newStatus = (String) body.get("newStatus");
            Object historyObj = body.get("historyRecord");
            LichSuNhanKhau historyRecord = null;
            if (historyObj != null) {
                historyRecord = objectMapper.convertValue(historyObj, LichSuNhanKhau.class);
            }
            if (nhanKhauService.updateStatusWithHistory(id, newStatus, historyRecord))
                ctx.json(createSuccessResponse("Status updated", null));
            else 
                ctx.status(400).json(createErrorResponse("Failed to update"));
        });

        app.get("/api/phuong-tien/ho-gia-dinh/{maHo}", ctx -> {
            Integer maHo = parseIntSafe(ctx, "maHo");
            if(maHo != null) ctx.json(phuongTienService.getPhuongTienByHoGiaDinh(maHo));
        });
    }

    private void setupVehicleRoutes(Javalin app) {
        app.get("/api/phuong-tien", ctx -> ctx.json(phuongTienService.getAllPhuongTien()));
        
        app.get("/api/phuong-tien/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null) {
                PhuongTien pt = phuongTienService.getPhuongTienById(id);
                if (pt != null) ctx.json(pt); else ctx.status(404).json(createErrorResponse("Vehicle not found"));
            }
        });

        app.get("/api/phuong-tien/search/{keyword}", ctx -> {
             String keyword = ctx.pathParam("keyword");
             ctx.json(phuongTienService.searchPhuongTien(keyword));
        });

        app.post("/api/phuong-tien", ctx -> {
            PhuongTien pt = ctx.bodyAsClass(PhuongTien.class);
            if (phuongTienService.addPhuongTien(pt)) ctx.status(201).json(createSuccessResponse("Vehicle created", pt));
            else ctx.status(400).json(createErrorResponse("Failed to create vehicle"));
        });

        app.put("/api/phuong-tien/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            PhuongTien pt = ctx.bodyAsClass(PhuongTien.class);
            pt.setId(id);
            if (phuongTienService.updatePhuongTien(pt)) ctx.json(createSuccessResponse("Vehicle updated", pt));
            else ctx.status(400).json(createErrorResponse("Failed to update vehicle"));
        });

        app.delete("/api/phuong-tien/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id != null && phuongTienService.deletePhuongTien(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Failed to delete vehicle"));
        });
    }

    // ===================================================================================
    // GROUP 5: RECEIPTS & DETAILS
    // ===================================================================================

    private void setupReceiptRoutes(Javalin app) {
        app.get("/api/phieu-thu", ctx -> ctx.result(objectMapper.writeValueAsString(phieuThuService.getAllPhieuThu())));
        
        app.get("/api/phieu-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null) ctx.result(objectMapper.writeValueAsString(phieuThuService.getPhieuThuWithDetails(id)));
        });

        app.post("/api/phieu-thu/generate/{maDot}", ctx -> {
            Integer maDot = parseIntSafe(ctx, "maDot");
            if(maDot != null) {
                int count = phieuThuService.generateReceiptsForDrive(maDot);
                ctx.json(createSuccessResponse("Generated " + count + " receipts", count));
            }
        });

        // [BỔ SUNG QUAN TRỌNG] Cập nhật phiếu thu (Sửa tiền điện/nước)
        app.put("/api/phieu-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                
                // Parse thông tin phiếu thu
                PhieuThu phieuThu;
                if (body.containsKey("phieuThu")) {
                    phieuThu = objectMapper.convertValue(body.get("phieuThu"), PhieuThu.class);
                } else {
                    phieuThu = objectMapper.convertValue(body, PhieuThu.class);
                }
                phieuThu.setId(id);

                // Parse danh sách chi tiết
                List<ChiTietThu> chiTietList = null;
                if (body.containsKey("chiTietList") && body.get("chiTietList") != null) {
                    chiTietList = objectMapper.convertValue(body.get("chiTietList"), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ChiTietThu.class));
                }

                boolean success = phieuThuService.updatePhieuThu(phieuThu, chiTietList);
                
                if (success) {
                    ctx.json(createSuccessResponse("Updated successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Update failed"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(400).json(createErrorResponse("Invalid data: " + e.getMessage()));
            }
        });

        app.delete("/api/phieu-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null && phieuThuService.deletePhieuThu(id)) ctx.json(createSuccessResponse("Deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });

        // Detail Routes
        app.get("/api/phieu-thu/{id}/chi-tiet", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null) ctx.json(chiTietThuService.getChiTietByMaPhieu(id));
        });

        app.post("/api/chi-tiet-thu", ctx -> {
            ChiTietThu ct = ctx.bodyAsClass(ChiTietThu.class);
            if(chiTietThuService.save(ct)) ctx.status(201).json(createSuccessResponse("Detail saved", ct));
            else ctx.status(400).json(createErrorResponse("Save failed"));
        });

        app.delete("/api/chi-tiet-thu/{id}", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if(id != null && chiTietThuService.delete(id)) ctx.json(createSuccessResponse("Detail deleted", null));
            else ctx.status(400).json(createErrorResponse("Delete failed"));
        });
        
        // Calculate Total
        app.get("/api/phieu-thu/calculate-total", ctx -> {
            Integer maHo = parseIntSafe(ctx, "maHo");
            Integer maDot = parseIntSafe(ctx, "maDot");
            if(maHo != null && maDot != null) {
                BigDecimal total = phieuThuService.calculateTotalAmountForHousehold(maHo, maDot);
                ctx.json(createSuccessResponse("Calculated", Map.of("tongTien", total)));
            }
        });
        
        // Update Receipt Status
        app.put("/api/phieu-thu/{id}/status", ctx -> {
            Integer id = parseIntSafe(ctx, "id");
            if (id == null) return;
            @SuppressWarnings("unchecked")
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            if(phieuThuService.updatePhieuThuStatus(id, body.get("newStatus"))) 
                ctx.json(createSuccessResponse("Status updated", null));
            else ctx.status(400).json(createErrorResponse("Update failed"));
        });
    }

    // ===================================================================================
    // GROUP 6: STATISTICS & REPORTS
    // ===================================================================================

    private void setupStatisticsRoutes(Javalin app) {
        // 1. Dashboard
        app.get("/api/thong-ke/dashboard", ctx -> ctx.json(thongKeService.getDashboardStats()));

        // 2. Doanh thu
        app.get("/api/thong-ke/doanh-thu", ctx -> {
            LocalDate fDate = parseDateParam(ctx.queryParam("from"), LocalDate.now().minusMonths(6));
            LocalDate tDate = parseDateParam(ctx.queryParam("to"), LocalDate.now());
            ctx.json(thongKeService.getRevenueStats(fDate, tDate));
        });

        // 3. Nhân khẩu
        app.get("/api/thong-ke/nhan-khau", ctx -> ctx.json(thongKeService.getResidentDemographics()));

        // 4. Công nợ (Biểu đồ)
        app.get("/api/thong-ke/cong-no", ctx -> ctx.json(thongKeService.getDebtStats()));

        // 5. Chi tiết nợ (Bảng)
        app.get("/api/thong-ke/cong-no/chi-tiet", ctx -> ctx.json(thongKeService.getDebtDetails()));
    }

    private void setupReportRoutes(Javalin app) {
        // 6. Xem báo cáo thu
        app.get("/api/bao-cao/thu", ctx -> {
            LocalDate fDate = parseDateParam(ctx.queryParam("from"), LocalDate.now().withDayOfMonth(1));
            LocalDate tDate = parseDateParam(ctx.queryParam("to"), LocalDate.now());
            ctx.json(baoCaoService.getRevenueReport(fDate, tDate));
        });

        // 7. Xuất Excel Báo cáo thu
        app.get("/api/bao-cao/thu/export", ctx -> {
            LocalDate fDate = parseDateParam(ctx.queryParam("from"), LocalDate.now().withDayOfMonth(1));
            LocalDate tDate = parseDateParam(ctx.queryParam("to"), LocalDate.now());
            
            List<BaoCaoThu> data = baoCaoService.getRevenueReport(fDate, tDate);
            InputStream is = baoCaoService.exportRevenueToExcel(data, fDate, tDate);
            
            if(is != null) {
                ctx.header("Content-Disposition", "attachment; filename=BaoCaoThu.xlsx");
                ctx.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ctx.result(is);
            } else {
                ctx.status(500).result("Error exporting file");
            }
        });

        // 8. Xem báo cáo công nợ
        app.get("/api/bao-cao/cong-no", ctx -> {
            String maDotStr = ctx.queryParam("maDot");
            int maDot = (maDotStr != null && !maDotStr.isEmpty()) ? Integer.parseInt(maDotStr) : 0;
            ctx.json(baoCaoService.getDebtReport(maDot));
        });

        // 9. Xuất Excel Công nợ
        app.get("/api/bao-cao/cong-no/export", ctx -> {
            String maDotStr = ctx.queryParam("maDot");
            int maDot = (maDotStr != null && !maDotStr.isEmpty()) ? Integer.parseInt(maDotStr) : 0;
            
            List<BaoCaoCongNo> data = baoCaoService.getDebtReport(maDot);
            InputStream is = baoCaoService.exportDebtToExcel(data);
            
            if(is != null) {
                ctx.header("Content-Disposition", "attachment; filename=BaoCaoCongNo.xlsx");
                ctx.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ctx.result(is);
            } else {
                ctx.status(500).result("Error exporting file");
            }
        });
    }

    // ===================================================================================
    // HANDLERS & HELPERS
    // ===================================================================================

    @SuppressWarnings("unchecked")
    private void handleLogin(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String u = (String) body.get("username");
            String p = (String) body.get("password");
            
            if (u == null || p == null) {
                ctx.status(400).json(createErrorResponse("Missing credentials"));
                return;
            }

            TaiKhoan acc = authService.login(u, p);
            if (acc != null) {
                acc.setMatKhau(null);
                ctx.json(createSuccessResponse("Login successful", acc));
            } else {
                ctx.status(401).json(createErrorResponse("Invalid credentials"));
            }
        } catch (Exception e) {
            handleException(ctx, e);
        }
    }

    private void handleChangePassword(Context ctx) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            int id = Integer.parseInt(body.get("id").toString());
            String oldPass = (String) body.get("oldPassword");
            String newPass = (String) body.get("newPassword");
            
            if(authService.changePassword(id, oldPass, newPass)) 
                ctx.json(createSuccessResponse("Password changed", null));
            else 
                ctx.status(400).json(createErrorResponse("Failed to change password"));
        } catch (Exception e) {
            handleException(ctx, e);
        }
    }

    private void handleCheckUsername(Context ctx) {
        String username = ctx.pathParam("username");
        ctx.json(createSuccessResponse("Check completed", authService.isUsernameExist(username)));
    }

    // --- Common Utilities ---

    private void serveHtmlFile(Context ctx, String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) ctx.html(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            else ctx.html("<h1>404 Not Found</h1>");
        } catch (Exception e) {
            ctx.html("<h1>500 Server Error</h1>");
        }
    }

    private Integer parseIntSafe(Context ctx, String paramName) {
        String val = ctx.pathParam(paramName);
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            ctx.status(400).json(createErrorResponse("Invalid " + paramName));
            return null;
        }
    }

    private LocalDate parseDateParam(String dateStr, LocalDate defaultDate) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try { return LocalDate.parse(dateStr); } catch (Exception ignored) {}
        }
        return defaultDate;
    }

    private void handleException(Context ctx, Exception e) {
        logger.severe("Exception: " + e.getMessage());
        e.printStackTrace();
        ctx.status(500).json(createStandardErrorResponse("Server Error", "INTERNAL_ERROR", e.getMessage(), ctx.path()));
    }

    private Map<String, Object> createSuccessResponse(String msg, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", msg);
        if(data != null) map.put("data", data);
        return map;
    }

    private Map<String, Object> createErrorResponse(String msg) {
        return createStandardErrorResponse(msg, "ERROR", msg, "");
    }

    private Map<String, Object> createStandardErrorResponse(String msg, String code, String details, String path) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", msg);
        map.put("errorCode", code);
        map.put("details", details);
        map.put("path", path);
        map.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return map;
    }
}