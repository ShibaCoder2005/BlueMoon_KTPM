package com.bluemoon;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.bluemoon.services.*;
import com.bluemoon.services.impl.*;
import com.bluemoon.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebServer: Headless REST API server using Javalin.
 * Automatically maps service methods to REST endpoints.
 */
public class WebServer {

    private static final int PORT = 7070;
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
            // Configure static files to be served from classpath
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/";
                staticFiles.hostedPath = "/";
            });
        });

        // ========== GLOBAL BEFORE HANDLER FOR /api/* ==========
        app.before("/api/*", ctx -> {
            // Set default content-type to JSON for all API endpoints
            ctx.contentType("application/json; charset=utf-8");
        });

        // ========== NOT FOUND HANDLER ==========
        app.error(404, ctx -> {
            String path = ctx.path();
            if (path.startsWith("/api")) {
                // API endpoints return JSON 404
                Map<String, Object> errorResponse = createStandardErrorResponse(
                    "Not Found",
                    "API_ENDPOINT_NOT_FOUND",
                    "The requested API endpoint does not exist: " + path,
                    path
                );
                ctx.status(404);
                ctx.contentType("application/json; charset=utf-8");
                ctx.json(errorResponse);
            } else {
                // Non-API paths serve index.html or helpful page
                try (InputStream is = getClass().getResourceAsStream("/index.html")) {
                    if (is != null) {
                        String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        ctx.html(html);
                    } else {
                        ctx.html("<h1>BlueMoon KTPM</h1><p>Page not found. <a href='/'>Go to home</a> | <a href='/api'>API</a></p>");
                    }
                } catch (Exception e) {
                    ctx.html("<h1>BlueMoon KTPM</h1><p>Page not found. <a href='/'>Go to home</a> | <a href='/api'>API</a></p>");
                }
            }
        });

        // ========== ROOT ENDPOINT ==========
        app.get("/", ctx -> {
            try (InputStream is = getClass().getResourceAsStream("/index.html")) {
                if (is != null) {
                    String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    ctx.html(html);
                } else {
                    ctx.html("<h1>BlueMoon KTPM</h1><p>Server is running. <a href='/api'>API</a> available at <a href='/api'>/api</a></p>");
                }
            } catch (Exception e) {
                ctx.html("<h1>BlueMoon KTPM</h1><p>Server is running. <a href='/api'>API</a> available at <a href='/api'>/api</a></p>");
            }
        });

        // ========== API ROOT ENDPOINT ==========
        app.get("/api", ctx -> {
            try {
                Map<String, Object> apiInfo = new HashMap<>();
                apiInfo.put("status", "running");
                apiInfo.put("message", "BlueMoon KTPM API");
                apiInfo.put("version", "1.0.0");
                apiInfo.put("endpoints", Map.of(
                    "health", "/api/health",
                    "auth", "/api/login, /api/register",
                    "accounts", "/api/tai-khoan",
                    "fees", "/api/khoan-thu",
                    "collection_drives", "/api/dot-thu",
                    "households", "/api/ho-gia-dinh",
                    "residents", "/api/nhan-khau",
                    "receipts", "/api/phieu-thu",
                    "payment_history", "/api/lich-su-nop-tien",
                    "statistics", "/api/thong-ke"
                ));
                ctx.json(apiInfo);
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== AUTH ENDPOINTS ==========
        app.post("/api/login", this::handleLogin);
        app.post("/api/register", this::handleRegister);
        app.post("/api/change-password", this::handleChangePassword);
        app.get("/api/check-username/{username}", this::handleCheckUsername);

        // ========== TAI KHOAN (Account) ENDPOINTS ==========
        app.get("/api/tai-khoan", ctx -> {
            try {
                ctx.json(taiKhoanService.getAllTaiKhoan());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/tai-khoan/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                TaiKhoan account = taiKhoanService.findById(id);
                if (account != null) {
                    ctx.json(account);
                } else {
                    ctx.status(404).json(createErrorResponse("Account not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/tai-khoan/username/{username}", ctx -> {
            try {
                String username = ctx.pathParam("username");
                TaiKhoan account = taiKhoanService.findByUsername(username);
                if (account != null) {
                    ctx.json(account);
                } else {
                    ctx.status(404).json(createErrorResponse("Account not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/tai-khoan", ctx -> {
            try {
                TaiKhoan account = ctx.bodyAsClass(TaiKhoan.class);
                boolean success = taiKhoanService.addTaiKhoan(account);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Account created successfully", account));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create account"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/tai-khoan/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                TaiKhoan account = ctx.bodyAsClass(TaiKhoan.class);
                account.setId(id);
                boolean success = taiKhoanService.updateTaiKhoan(account);
                if (success) {
                    ctx.json(createSuccessResponse("Account updated successfully", account));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update account"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/tai-khoan/{id}/status", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String status = body.get("trangThai");
                boolean success = taiKhoanService.updateStatus(id, status);
                if (success) {
                    ctx.json(createSuccessResponse("Status updated successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update status"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== KHOAN THU (Fee) ENDPOINTS ==========
        app.get("/api/khoan-thu", ctx -> {
            try {
                ctx.json(khoanThuService.getAllKhoanThu());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/khoan-thu", ctx -> {
            try {
                // Parse body as Map to handle field mapping
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                
                // Validate required fields
                String tenKhoanThu = (String) body.get("tenKhoanThu");
                if (tenKhoanThu == null || tenKhoanThu.trim().isEmpty()) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "MISSING_REQUIRED_FIELD",
                        "tenKhoanThu is required",
                        ctx.path()
                    ));
                    return;
                }
                
                // Create KhoanThu object
                KhoanThu khoanThu = new KhoanThu();
                khoanThu.setTenKhoanThu(tenKhoanThu.trim());
                
                // Map loaiPhi to model fields
                String loaiPhi = (String) body.get("loaiPhi");
                if (loaiPhi != null) {
                    if ("BatBuoc".equals(loaiPhi)) {
                        khoanThu.setLoai("Bắt buộc");
                        khoanThu.setBatBuoc(true);
                        khoanThu.setLoaiKhoanThu(0);
                    } else if ("TuNguyen".equals(loaiPhi)) {
                        khoanThu.setLoai("Tự nguyện");
                        khoanThu.setBatBuoc(false);
                        khoanThu.setLoaiKhoanThu(1);
                    } else {
                        khoanThu.setLoai(loaiPhi);
                        khoanThu.setBatBuoc(false);
                        khoanThu.setLoaiKhoanThu(1);
                    }
                } else {
                    // Default to optional
                    khoanThu.setLoai("Tự nguyện");
                    khoanThu.setBatBuoc(false);
                    khoanThu.setLoaiKhoanThu(1);
                }
                
                // Handle donGia (can be Number or String)
                Object donGiaObj = body.get("donGia");
                if (donGiaObj != null) {
                    try {
                        BigDecimal donGia;
                        if (donGiaObj instanceof Number) {
                            donGia = BigDecimal.valueOf(((Number) donGiaObj).doubleValue());
                        } else if (donGiaObj instanceof String) {
                            donGia = new BigDecimal((String) donGiaObj);
                        } else {
                            donGia = BigDecimal.ZERO;
                        }
                        khoanThu.setDonGia(donGia);
                        khoanThu.setDonViTinh("VNĐ/m²");
                        khoanThu.setTinhTheo("Diện tích");
                    } catch (NumberFormatException e) {
                        ctx.status(400).json(createStandardErrorResponse(
                            "Validation failed",
                            "INVALID_NUMBER_FORMAT",
                            "donGia must be a valid number, got: " + donGiaObj,
                            ctx.path()
                        ));
                        return;
                    }
                } else {
                    khoanThu.setDonGia(BigDecimal.ZERO);
                }
                
                // Handle moTa
                String moTa = (String) body.get("moTa");
                if (moTa != null) {
                    khoanThu.setMoTa(moTa.trim());
                }
                
                // hanNop is ignored (not in model)
                
                // Save
                boolean success = khoanThuService.addKhoanThu(khoanThu);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Fee created successfully", khoanThu));
                } else {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Failed to create fee",
                        "CREATE_FAILED",
                        "Service layer returned false",
                        ctx.path()
                    ));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/khoan-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                
                // Parse body as Map to handle field mapping
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                
                // Validate required fields
                String tenKhoanThu = (String) body.get("tenKhoanThu");
                if (tenKhoanThu == null || tenKhoanThu.trim().isEmpty()) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "MISSING_REQUIRED_FIELD",
                        "tenKhoanThu is required",
                        ctx.path()
                    ));
                    return;
                }
                
                // Get existing KhoanThu or create new
                KhoanThu existing = khoanThuService.getAllKhoanThu().stream()
                    .filter(k -> k.getId() == id)
                    .findFirst()
                    .orElse(new KhoanThu());
                
                KhoanThu khoanThu = new KhoanThu();
                khoanThu.setId(id);
                khoanThu.setTenKhoanThu(tenKhoanThu.trim());
                
                // Map loaiPhi to model fields
                String loaiPhi = (String) body.get("loaiPhi");
                if (loaiPhi != null) {
                    if ("BatBuoc".equals(loaiPhi)) {
                        khoanThu.setLoai("Bắt buộc");
                        khoanThu.setBatBuoc(true);
                        khoanThu.setLoaiKhoanThu(0);
                    } else if ("TuNguyen".equals(loaiPhi)) {
                        khoanThu.setLoai("Tự nguyện");
                        khoanThu.setBatBuoc(false);
                        khoanThu.setLoaiKhoanThu(1);
                    } else {
                        khoanThu.setLoai(loaiPhi);
                        khoanThu.setBatBuoc(false);
                        khoanThu.setLoaiKhoanThu(1);
                    }
                } else {
                    // Preserve existing values if not provided
                    khoanThu.setLoai(existing.getLoai() != null ? existing.getLoai() : "Tự nguyện");
                    khoanThu.setBatBuoc(existing.isBatBuoc());
                    khoanThu.setLoaiKhoanThu(existing.getLoaiKhoanThu());
                }
                
                // Handle donGia (can be Number or String)
                Object donGiaObj = body.get("donGia");
                if (donGiaObj != null) {
                    try {
                        BigDecimal donGia;
                        if (donGiaObj instanceof Number) {
                            donGia = BigDecimal.valueOf(((Number) donGiaObj).doubleValue());
                        } else if (donGiaObj instanceof String) {
                            donGia = new BigDecimal((String) donGiaObj);
                        } else {
                            donGia = existing.getDonGia() != null ? existing.getDonGia() : BigDecimal.ZERO;
                        }
                        khoanThu.setDonGia(donGia);
                        khoanThu.setDonViTinh("VNĐ/m²");
                        khoanThu.setTinhTheo("Diện tích");
                    } catch (NumberFormatException e) {
                        ctx.status(400).json(createStandardErrorResponse(
                            "Validation failed",
                            "INVALID_NUMBER_FORMAT",
                            "donGia must be a valid number, got: " + donGiaObj,
                            ctx.path()
                        ));
                        return;
                    }
                } else {
                    // Preserve existing donGia
                    khoanThu.setDonGia(existing.getDonGia() != null ? existing.getDonGia() : BigDecimal.ZERO);
                    khoanThu.setDonViTinh(existing.getDonViTinh() != null ? existing.getDonViTinh() : "VNĐ/m²");
                    khoanThu.setTinhTheo(existing.getTinhTheo() != null ? existing.getTinhTheo() : "Diện tích");
                }
                
                // Handle moTa
                String moTa = (String) body.get("moTa");
                if (moTa != null) {
                    khoanThu.setMoTa(moTa.trim());
                } else {
                    khoanThu.setMoTa(existing.getMoTa());
                }
                
                // hanNop is ignored (not in model)
                
                // Update
                boolean success = khoanThuService.updateKhoanThu(khoanThu);
                if (success) {
                    ctx.json(createSuccessResponse("Fee updated successfully", khoanThu));
                } else {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Failed to update fee",
                        "UPDATE_FAILED",
                        "Service layer returned false or fee not found",
                        ctx.path()
                    ));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.delete("/api/khoan-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = khoanThuService.deleteKhoanThu(id);
                if (success) {
                    ctx.json(createSuccessResponse("Fee deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete fee (may be in use)"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== DOT THU (Collection Drive) ENDPOINTS ==========
        app.get("/api/dot-thu", ctx -> {
            try {
                ctx.json(dotThuService.getAllDotThu());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/dot-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                DotThu dotThu = dotThuService.getDotThuById(id);
                if (dotThu != null) {
                    ctx.json(dotThu);
                } else {
                    ctx.status(404).json(createErrorResponse("Collection drive not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/dot-thu/search/{keyword}", ctx -> {
            try {
                String keyword = ctx.pathParam("keyword");
                ctx.json(dotThuService.searchDotThu(keyword));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/dot-thu", ctx -> {
            try {
                DotThu dotThu = ctx.bodyAsClass(DotThu.class);
                boolean success = dotThuService.addDotThu(dotThu);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Collection drive created successfully", dotThu));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create collection drive"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/dot-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                DotThu dotThu = ctx.bodyAsClass(DotThu.class);
                dotThu.setId(id);
                boolean success = dotThuService.updateDotThu(dotThu);
                if (success) {
                    ctx.json(createSuccessResponse("Collection drive updated successfully", dotThu));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update collection drive"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.delete("/api/dot-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = dotThuService.deleteDotThu(id);
                if (success) {
                    ctx.json(createSuccessResponse("Collection drive deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete collection drive (may be in use)"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== HO GIA DINH (Household) ENDPOINTS ==========
        app.get("/api/ho-gia-dinh", ctx -> {
            try {
                ctx.json(hoGiaDinhService.getAllHoGiaDinh());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/ho-gia-dinh/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                HoGiaDinh hoGiaDinh = hoGiaDinhService.findById(id);
                if (hoGiaDinh != null) {
                    ctx.json(hoGiaDinh);
                } else {
                    ctx.status(404).json(createErrorResponse("Household not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/ho-gia-dinh/search/{keyword}", ctx -> {
            try {
                String keyword = ctx.pathParam("keyword");
                ctx.json(hoGiaDinhService.searchHoGiaDinh(keyword));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/ho-gia-dinh", ctx -> {
            try {
                HoGiaDinh hoGiaDinh = ctx.bodyAsClass(HoGiaDinh.class);
                boolean success = hoGiaDinhService.addHoGiaDinh(hoGiaDinh);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Household created successfully", hoGiaDinh));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create household"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/ho-gia-dinh/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                HoGiaDinh hoGiaDinh = ctx.bodyAsClass(HoGiaDinh.class);
                hoGiaDinh.setId(id);
                boolean success = hoGiaDinhService.updateHoGiaDinh(hoGiaDinh);
                if (success) {
                    ctx.json(createSuccessResponse("Household updated successfully", hoGiaDinh));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update household"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.delete("/api/ho-gia-dinh/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = hoGiaDinhService.deleteHoGiaDinh(id);
                if (success) {
                    ctx.json(createSuccessResponse("Household deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete household (may be in use)"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== NHAN KHAU (Resident) ENDPOINTS ==========
        app.get("/api/nhan-khau", ctx -> {
            try {
                ctx.json(nhanKhauService.getAll());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/nhan-khau/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                NhanKhau nhanKhau = nhanKhauService.findById(id);
                if (nhanKhau != null) {
                    ctx.json(nhanKhau);
                } else {
                    ctx.status(404).json(createErrorResponse("Resident not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/nhan-khau/ho-gia-dinh/{maHo}", ctx -> {
            try {
                Integer maHo = parseIntSafe(ctx, "maHo");
                if (maHo == null) return;
                ctx.json(nhanKhauService.getNhanKhauByHoGiaDinh(maHo));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/nhan-khau/{id}/lich-su", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                ctx.json(nhanKhauService.getLichSuNhanKhau(id));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/nhan-khau/lich-su/all", ctx -> {
            try {
                ctx.json(nhanKhauService.getAllLichSuNhanKhau());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/nhan-khau", ctx -> {
            try {
                NhanKhau nhanKhau = ctx.bodyAsClass(NhanKhau.class);
                boolean success = nhanKhauService.addNhanKhau(nhanKhau);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Resident created successfully", nhanKhau));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create resident"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/nhan-khau/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                NhanKhau nhanKhau = ctx.bodyAsClass(NhanKhau.class);
                nhanKhau.setId(id);
                boolean success = nhanKhauService.updateNhanKhau(nhanKhau);
                if (success) {
                    ctx.json(createSuccessResponse("Resident updated successfully", nhanKhau));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update resident"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.delete("/api/nhan-khau/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = nhanKhauService.deleteNhanKhau(id);
                if (success) {
                    ctx.json(createSuccessResponse("Resident deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete resident"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/nhan-khau/lich-su", ctx -> {
            try {
                LichSuNhanKhau history = ctx.bodyAsClass(LichSuNhanKhau.class);
                boolean success = nhanKhauService.addLichSuNhanKhau(history);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("History record created successfully", history));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create history record"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/nhan-khau/{id}/status", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
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
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== PHIEU THU (Receipt) ENDPOINTS ==========
        app.get("/api/phieu-thu", ctx -> {
            try {
                ctx.json(phieuThuService.getAllPhieuThu());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phieu-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                PhieuThu phieuThu = phieuThuService.getPhieuThuWithDetails(id);
                if (phieuThu != null) {
                    ctx.json(phieuThu);
                } else {
                    ctx.status(404).json(createErrorResponse("Receipt not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phieu-thu/{id}/chi-tiet", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                ctx.json(phieuThuService.getChiTietThuByPhieu(id));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phieu-thu/ho-gia-dinh/{maHo}", ctx -> {
            try {
                Integer maHo = parseIntSafe(ctx, "maHo");
                if (maHo == null) return;
                ctx.json(phieuThuService.findPhieuThuByHoGiaDinh(maHo));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phieu-thu/dot-thu/{maDotThu}", ctx -> {
            try {
                Integer maDotThu = parseIntSafe(ctx, "maDotThu");
                if (maDotThu == null) return;
                ctx.json(phieuThuService.findPhieuThuByDotThu(maDotThu));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/phieu-thu", ctx -> {
            try {
                PhieuThu phieuThu = ctx.bodyAsClass(PhieuThu.class);
                int maPhieu = phieuThuService.createPhieuThu(phieuThu);
                if (maPhieu > 0) {
                    phieuThu.setId(maPhieu);
                    ctx.status(201).json(createSuccessResponse("Receipt created successfully", phieuThu));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create receipt"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/phieu-thu/with-details", ctx -> {
            try {
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
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/phieu-thu/chi-tiet", ctx -> {
            try {
                ChiTietThu chiTiet = ctx.bodyAsClass(ChiTietThu.class);
                boolean success = phieuThuService.addChiTietThu(chiTiet);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Detail added successfully", chiTiet));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to add detail"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/phieu-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
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
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/phieu-thu/{id}/status", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String status = body.get("newStatus");
                boolean success = phieuThuService.updatePhieuThuStatus(id, status);
                if (success) {
                    ctx.json(createSuccessResponse("Status updated successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update status"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.delete("/api/phieu-thu/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = phieuThuService.deletePhieuThu(id);
                if (success) {
                    ctx.json(createSuccessResponse("Receipt deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete receipt (may be paid)"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/phieu-thu/generate/{maDot}", ctx -> {
            try {
                Integer maDot = parseIntSafe(ctx, "maDot");
                if (maDot == null) return;
                int count = phieuThuService.generateReceiptsForDrive(maDot);
                ctx.json(createSuccessResponse("Generated " + count + " receipts", count));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phieu-thu/ho-gia-dinh/{maHo}/unpaid", ctx -> {
            try {
                Integer maHo = parseIntSafe(ctx, "maHo");
                if (maHo == null) return;
                boolean hasUnpaid = phieuThuService.hasUnpaidFees(maHo);
                ctx.json(createSuccessResponse("Check completed", hasUnpaid));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== LICH SU NOP TIEN (Payment History) ENDPOINTS ==========
        app.get("/api/lich-su-nop-tien", ctx -> {
            try {
                ctx.json(lichSuNopTienService.getAllLichSuNopTien());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/lich-su-nop-tien/phieu-thu/{maPhieu}", ctx -> {
            try {
                Integer maPhieu = parseIntSafe(ctx, "maPhieu");
                if (maPhieu == null) return;
                ctx.json(lichSuNopTienService.getLichSuNopTienByPhieuThu(maPhieu));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/lich-su-nop-tien/ho-gia-dinh/{maHo}", ctx -> {
            try {
                Integer maHo = parseIntSafe(ctx, "maHo");
                if (maHo == null) return;
                ctx.json(lichSuNopTienService.getLichSuNopTienByHoGiaDinh(maHo));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/lich-su-nop-tien", ctx -> {
            try {
                LichSuNopTien payment = ctx.bodyAsClass(LichSuNopTien.class);
                boolean success = lichSuNopTienService.addLichSuNopTien(payment);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Payment record created successfully", payment));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create payment record"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/lich-su-nop-tien/with-status-update", ctx -> {
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                LichSuNopTien payment = objectMapper.convertValue(body.get("paymentRecord"), LichSuNopTien.class);
                String updateStatusTo = (String) body.get("updateStatusTo");
                boolean success = lichSuNopTienService.recordPaymentWithStatusUpdate(payment, updateStatusTo);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Payment recorded and status updated successfully", payment));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to record payment"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== THONG KE (Statistics) ENDPOINTS ==========
        app.get("/api/thong-ke/dashboard", ctx -> {
            try {
                ctx.json(thongKeService.getDashboardStats());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/revenue", ctx -> {
            try {
                String fromDateStr = ctx.queryParam("fromDate");
                String toDateStr = ctx.queryParam("toDate");
                if (fromDateStr == null || toDateStr == null) {
                    ctx.status(400).json(createErrorResponse("fromDate and toDate query parameters are required"));
                    return;
                }
                LocalDate fromDate = LocalDate.parse(fromDateStr);
                LocalDate toDate = LocalDate.parse(toDateStr);
                ctx.json(thongKeService.getRevenueStats(fromDate, toDate));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/revenue/total", ctx -> {
            try {
                String fromDateStr = ctx.queryParam("fromDate");
                String toDateStr = ctx.queryParam("toDate");
                if (fromDateStr == null || toDateStr == null) {
                    ctx.status(400).json(createErrorResponse("fromDate and toDate query parameters are required"));
                    return;
                }
                LocalDate fromDate = LocalDate.parse(fromDateStr);
                LocalDate toDate = LocalDate.parse(toDateStr);
                ctx.json(createSuccessResponse("Total revenue", thongKeService.getTotalRevenue(fromDate, toDate)));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/revenue/details", ctx -> {
            try {
                String fromDateStr = ctx.queryParam("fromDate");
                String toDateStr = ctx.queryParam("toDate");
                if (fromDateStr == null || toDateStr == null) {
                    ctx.status(400).json(createErrorResponse("fromDate and toDate query parameters are required"));
                    return;
                }
                LocalDate fromDate = LocalDate.parse(fromDateStr);
                LocalDate toDate = LocalDate.parse(toDateStr);
                ctx.json(thongKeService.getRevenueDetails(fromDate, toDate));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/debt", ctx -> {
            try {
                ctx.json(thongKeService.getDebtStats());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/debt/total", ctx -> {
            try {
                ctx.json(createSuccessResponse("Total debt", thongKeService.getTotalDebt()));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/debt/details", ctx -> {
            try {
                ctx.json(thongKeService.getDebtStats());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/thong-ke/report/{maDotThu}", ctx -> {
            try {
                Integer maDotThu = parseIntSafe(ctx, "maDotThu");
                if (maDotThu == null) return;
                ctx.json(thongKeService.generateCollectionReport(maDotThu));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // Health check endpoint
        app.get("/api/health", ctx -> {
            try {
                Map<String, Object> health = new HashMap<>();
                health.put("status", "healthy");
                health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                health.put("version", "1.0.0");
                ctx.json(createSuccessResponse("Server is running", health));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // Debug endpoint to list registered routes (optional, for development)
        app.get("/api/debug/routes", ctx -> {
            try {
                Map<String, Object> routes = new HashMap<>();
                routes.put("note", "This endpoint lists all registered API routes");
                routes.put("baseUrl", "/api");
                routes.put("endpoints", Map.of(
                    "health", "GET /api/health",
                    "auth", "POST /api/login, POST /api/register, POST /api/change-password, GET /api/check-username/{username}",
                    "accounts", "GET /api/tai-khoan, GET /api/tai-khoan/{id}, POST /api/tai-khoan, PUT /api/tai-khoan/{id}, PUT /api/tai-khoan/{id}/status",
                    "fees", "GET /api/khoan-thu, POST /api/khoan-thu, PUT /api/khoan-thu/{id}, DELETE /api/khoan-thu/{id}",
                    "collection_drives", "GET /api/dot-thu, GET /api/dot-thu/{id}, POST /api/dot-thu, PUT /api/dot-thu/{id}, DELETE /api/dot-thu/{id}",
                    "households", "GET /api/ho-gia-dinh, GET /api/ho-gia-dinh/{id}, POST /api/ho-gia-dinh, PUT /api/ho-gia-dinh/{id}, DELETE /api/ho-gia-dinh/{id}",
                    "residents", "GET /api/nhan-khau, GET /api/nhan-khau/{id}, POST /api/nhan-khau, PUT /api/nhan-khau/{id}, DELETE /api/nhan-khau/{id}, PUT /api/nhan-khau/{id}/status",
                    "receipts", "GET /api/phieu-thu, GET /api/phieu-thu/{id}, POST /api/phieu-thu, PUT /api/phieu-thu/{id}, DELETE /api/phieu-thu/{id}",
                    "payment_history", "GET /api/lich-su-nop-tien, POST /api/lich-su-nop-tien",
                    "statistics", "GET /api/thong-ke/dashboard, GET /api/thong-ke/revenue, GET /api/thong-ke/debt"
                ));
                ctx.json(createSuccessResponse("Routes listed", routes));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== GLOBAL EXCEPTION HANDLERS ==========
        // Handler for NumberFormatException (Invalid ID format)
        app.exception(NumberFormatException.class, (e, ctx) -> {
            e.printStackTrace();
            Map<String, Object> errorResponse = createStandardErrorResponse(
                "Invalid ID format",
                "INVALID_ID_FORMAT",
                e.getMessage() != null ? e.getMessage() : "ID must be a valid number",
                ctx.path()
            );
            ctx.status(400);
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(errorResponse);
        });

        // Handler for IllegalArgumentException (Invalid request data)
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            e.printStackTrace();
            Map<String, Object> errorResponse = createStandardErrorResponse(
                "Invalid request",
                "INVALID_REQUEST",
                e.getMessage() != null ? e.getMessage() : "Invalid request data",
                ctx.path()
            );
            ctx.status(400);
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(errorResponse);
        });

        // Handler for NullPointerException (Missing data)
        app.exception(NullPointerException.class, (e, ctx) -> {
            e.printStackTrace();
            Map<String, Object> errorResponse = createStandardErrorResponse(
                "Missing data",
                "MISSING_DATA",
                e.getMessage() != null ? e.getMessage() : "Required data is missing",
                ctx.path()
            );
            ctx.status(500);
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(errorResponse);
        });

        // Handler for all other exceptions (catch-all)
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            Map<String, Object> errorResponse = createStandardErrorResponse(
                "Server Error",
                "INTERNAL_SERVER_ERROR",
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(),
                ctx.path()
            );
            ctx.status(500);
            ctx.contentType("application/json; charset=utf-8");
            try {
                ctx.json(errorResponse);
            } catch (Exception jsonException) {
                // Fallback if JSON serialization fails - still return JSON string
                String fallbackJson = String.format(
                    "{\"success\":false,\"message\":\"Server Error\",\"errorCode\":\"JSON_SERIALIZATION_ERROR\",\"details\":\"Internal server error\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                    ctx.path(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
                ctx.result(fallbackJson);
            }
        });

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
            handleException(ctx, e);
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
            handleException(ctx, e);
        }
    }

    private void handleChangePassword(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Object idObj = body.get("id");
            if (idObj == null) {
                ctx.status(400).json(createErrorResponse("ID is required"));
                return;
            }
            int id;
            try {
                id = ((Number) idObj).intValue();
            } catch (ClassCastException | NullPointerException e) {
                ctx.status(400).json(createErrorResponse("Invalid ID format"));
                return;
            }
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
            handleException(ctx, e);
        }
    }

    private void handleCheckUsername(Context ctx) {
        try {
            String username = ctx.pathParam("username");
            if (username == null || username.trim().isEmpty()) {
                ctx.status(400).json(createErrorResponse("Username parameter is required"));
                return;
            }
            boolean exists = authService.isUsernameExist(username);
            ctx.json(createSuccessResponse("Check completed", exists));
        } catch (Exception e) {
            handleException(ctx, e);
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

    private void handleException(Context ctx, Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = createStandardErrorResponse(
            "Server Error",
            "INTERNAL_SERVER_ERROR",
            e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(),
            ctx.path()
        );
        ctx.status(500);
        ctx.contentType("application/json; charset=utf-8");
        try {
            ctx.json(errorResponse);
        } catch (Exception jsonException) {
            String fallbackJson = String.format(
                "{\"success\":false,\"message\":\"Server Error\",\"errorCode\":\"JSON_SERIALIZATION_ERROR\",\"details\":\"Internal server error\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                ctx.path(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            ctx.result(fallbackJson);
        }
    }

    /**
     * Create standardized error response with all required fields
     */
    private Map<String, Object> createStandardErrorResponse(String message, String errorCode, String details, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("details", details);
        response.put("path", path);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    /**
     * Safely parse integer from path parameter
     * @param ctx Javalin context
     * @param paramName Parameter name (e.g., "id", "maHo", "maDot")
     * @return Parsed integer, or null if invalid/empty
     */
    private Integer parseIntSafe(Context ctx, String paramName) {
        String paramValue = ctx.pathParam(paramName);
        if (paramValue == null || paramValue.trim().isEmpty()) {
            Map<String, Object> errorResponse = createStandardErrorResponse(
                "Invalid ID format",
                "MISSING_PARAMETER",
                paramName + " is required but was not provided",
                ctx.path()
            );
            ctx.status(400);
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(errorResponse);
            return null;
        }
        try {
            int parsed = Integer.parseInt(paramValue.trim());
            if (parsed <= 0) {
                Map<String, Object> errorResponse = createStandardErrorResponse(
                    "Invalid ID format",
                    "INVALID_ID_VALUE",
                    paramName + " must be a positive number, got: " + paramValue,
                    ctx.path()
                );
                ctx.status(400);
                ctx.contentType("application/json; charset=utf-8");
                ctx.json(errorResponse);
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = createStandardErrorResponse(
                "Invalid ID format",
                "INVALID_ID_FORMAT",
                paramName + " must be a valid number, got: " + paramValue,
                ctx.path()
            );
            ctx.status(400);
            ctx.contentType("application/json; charset=utf-8");
            ctx.json(errorResponse);
            return null;
        }
    }
}

