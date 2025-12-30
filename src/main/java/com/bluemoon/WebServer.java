package com.bluemoon;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.bluemoon.services.*;
import com.bluemoon.services.impl.*;
import com.bluemoon.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
    private final PhuongTienService phuongTienService;

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
        this.phuongTienService = new PhuongTienServiceImpl();
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
                    "auth", "/api/login",
                    "accounts", "/api/tai-khoan",
                    "fees", "/api/khoan-thu",
                    "collection_drives", "/api/dot-thu",
                    "households", "/api/ho-gia-dinh",
                    "residents", "/api/nhan-khau",
                    "receipts", "/api/phieu-thu",
                    "statistics", "/api/thong-ke"
                ));
                ctx.json(apiInfo);
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== AUTH ENDPOINTS ==========
        app.post("/api/login", this::handleLogin);
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
                
                // Parse body manually to handle optional password
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                
                // Validate required fields
                String tenDangNhap = body.get("tenDangNhap") != null ? body.get("tenDangNhap").toString().trim() : null;
                String hoTen = body.get("hoTen") != null ? body.get("hoTen").toString().trim() : null;
                String vaiTro = body.get("vaiTro") != null ? body.get("vaiTro").toString().trim() : null;
                
                if (tenDangNhap == null || tenDangNhap.isEmpty()) {
                    ctx.status(400).json(createErrorResponse("Tên đăng nhập là bắt buộc"));
                    return;
                }
                if (hoTen == null || hoTen.isEmpty()) {
                    ctx.status(400).json(createErrorResponse("Họ tên là bắt buộc"));
                    return;
                }
                if (vaiTro == null || vaiTro.isEmpty()) {
                    ctx.status(400).json(createErrorResponse("Vai trò là bắt buộc"));
                    return;
                }
                
                TaiKhoan account = new TaiKhoan();
                account.setId(id);
                account.setTenDangNhap(tenDangNhap);
                account.setHoTen(hoTen);
                account.setVaiTro(vaiTro);
                account.setDienThoai(body.get("dienThoai") != null ? body.get("dienThoai").toString().trim() : null);
                // Không set trangThai vì sẽ giữ nguyên từ database
                
                // Only set password if provided
                if (body.containsKey("matKhau") && body.get("matKhau") != null && !body.get("matKhau").toString().trim().isEmpty()) {
                    account.setMatKhau(body.get("matKhau").toString());
                }
                
                logger.info("Updating account: id=" + id + ", username=" + tenDangNhap + ", role=" + vaiTro);
                boolean success = taiKhoanService.updateTaiKhoan(account);
                if (success) {
                    ctx.json(createSuccessResponse("Account updated successfully", account));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update account. Username may already exist or account not found."));
                }
            } catch (Exception e) {
                logger.severe("Error updating account: " + e.getMessage());
                e.printStackTrace();
                handleException(ctx, e);
            }
        });
        app.delete("/api/tai-khoan/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = taiKhoanService.deleteTaiKhoan(id);
                if (success) {
                    ctx.json(createSuccessResponse("Account deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete account. Account may not exist."));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/tai-khoan/{id}/status", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                
                // Parse body to get trangThai and currentUserId
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                String trangThai = body.get("trangThai") != null ? body.get("trangThai").toString().trim() : null;
                
                if (trangThai == null || trangThai.isEmpty()) {
                    ctx.status(400).json(createErrorResponse("Trạng thái là bắt buộc"));
                    return;
                }
                
                // Validate trangThai value
                if (!trangThai.equals("Hoạt động") && !trangThai.equals("Đã đóng")) {
                    ctx.status(400).json(createErrorResponse("Trạng thái không hợp lệ. Chỉ chấp nhận 'Hoạt động' hoặc 'Đã đóng'"));
                    return;
                }
                
                // Check if user is trying to change their own status
                Object currentUserIdObj = body.get("currentUserId");
                if (currentUserIdObj != null) {
                    try {
                        int currentUserId = Integer.parseInt(currentUserIdObj.toString());
                        if (currentUserId == id) {
                            ctx.status(403).json(createErrorResponse("Không thể thay đổi trạng thái của chính tài khoản đang đăng nhập"));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        // currentUserId is not a valid number, ignore
                    }
                }
                
                logger.info("Updating account status: id=" + id + ", trangThai=" + trangThai);
                boolean success = taiKhoanService.updateStatus(id, trangThai);
                if (success) {
                    TaiKhoan updatedAccount = taiKhoanService.findById(id);
                    ctx.json(createSuccessResponse("Trạng thái tài khoản đã được cập nhật", updatedAccount));
                } else {
                    ctx.status(400).json(createErrorResponse("Không thể cập nhật trạng thái. Tài khoản có thể không tồn tại."));
                }
            } catch (Exception e) {
                logger.severe("Error updating account status: " + e.getMessage());
                e.printStackTrace();
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
                        
                        // Handle tinhTheo from frontend
                        String tinhTheo = (String) body.get("tinhTheo");
                        if (tinhTheo != null && !tinhTheo.trim().isEmpty()) {
                            // Map frontend values to backend format
                            String tinhTheoLower = tinhTheo.toLowerCase().trim();
                            switch (tinhTheoLower) {
                                case "dientich":
                                    khoanThu.setTinhTheo("Diện tích");
                                    khoanThu.setDonViTinh("VNĐ/m²");
                                    break;
                                case "nhankhau":
                                    khoanThu.setTinhTheo("Nhân khẩu");
                                    khoanThu.setDonViTinh("VNĐ/người");
                                    break;
                                case "hokhau":
                                    khoanThu.setTinhTheo("Hộ khẩu");
                                    khoanThu.setDonViTinh("VNĐ/hộ");
                                    break;
                                case "xemay":
                                    khoanThu.setTinhTheo("Xe máy");
                                    khoanThu.setDonViTinh("VNĐ/xe máy");
                                    break;
                                case "oto":
                                    khoanThu.setTinhTheo("Ô tô");
                                    khoanThu.setDonViTinh("VNĐ/ô tô");
                                    break;
                                default:
                                    khoanThu.setTinhTheo(tinhTheo);
                                    khoanThu.setDonViTinh("VNĐ");
                                    break;
                            }
                        } else {
                            // Default to Diện tích if not provided
                            khoanThu.setTinhTheo("Diện tích");
                            khoanThu.setDonViTinh("VNĐ/m²");
                        }
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
                    // Set default tinhTheo even if donGia is not provided
                    String tinhTheo = (String) body.get("tinhTheo");
                    if (tinhTheo != null && !tinhTheo.trim().isEmpty()) {
                        String tinhTheoLower = tinhTheo.toLowerCase().trim();
                        switch (tinhTheoLower) {
                            case "dientich":
                                khoanThu.setTinhTheo("Diện tích");
                                khoanThu.setDonViTinh("VNĐ/m²");
                                break;
                            case "nhankhau":
                                khoanThu.setTinhTheo("Nhân khẩu");
                                khoanThu.setDonViTinh("VNĐ/người");
                                break;
                            case "hokhau":
                                khoanThu.setTinhTheo("Hộ khẩu");
                                khoanThu.setDonViTinh("VNĐ/hộ");
                                break;
                            case "xemay":
                                khoanThu.setTinhTheo("Xe máy");
                                khoanThu.setDonViTinh("VNĐ/xe máy");
                                break;
                            case "oto":
                                khoanThu.setTinhTheo("Ô tô");
                                khoanThu.setDonViTinh("VNĐ/ô tô");
                                break;
                            default:
                                khoanThu.setTinhTheo(tinhTheo);
                                khoanThu.setDonViTinh("VNĐ");
                                break;
                        }
                    } else {
                        khoanThu.setTinhTheo("Diện tích");
                        khoanThu.setDonViTinh("VNĐ/m²");
                    }
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
                
                // Get existing KhoanThu
                KhoanThu existing = khoanThuService.getAllKhoanThu().stream()
                    .filter(k -> k.getId() == id)
                    .findFirst()
                    .orElse(null);
                
                if (existing == null) {
                    ctx.status(404).json(createStandardErrorResponse(
                        "Fee not found",
                        "NOT_FOUND",
                        "KhoanThu with id " + id + " not found",
                        ctx.path()
                    ));
                    return;
                }
                
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
                        
                        // Handle tinhTheo from frontend
                        String tinhTheo = (String) body.get("tinhTheo");
                        if (tinhTheo != null && !tinhTheo.trim().isEmpty()) {
                            // Map frontend values to backend format
                            String tinhTheoLower = tinhTheo.toLowerCase().trim();
                            switch (tinhTheoLower) {
                                case "dientich":
                                    khoanThu.setTinhTheo("Diện tích");
                                    khoanThu.setDonViTinh("VNĐ/m²");
                                    break;
                                case "nhankhau":
                                    khoanThu.setTinhTheo("Nhân khẩu");
                                    khoanThu.setDonViTinh("VNĐ/người");
                                    break;
                                case "hokhau":
                                    khoanThu.setTinhTheo("Hộ khẩu");
                                    khoanThu.setDonViTinh("VNĐ/hộ");
                                    break;
                                case "xemay":
                                    khoanThu.setTinhTheo("Xe máy");
                                    khoanThu.setDonViTinh("VNĐ/xe máy");
                                    break;
                                case "oto":
                                    khoanThu.setTinhTheo("Ô tô");
                                    khoanThu.setDonViTinh("VNĐ/ô tô");
                                    break;
                                default:
                                    khoanThu.setTinhTheo(tinhTheo);
                                    khoanThu.setDonViTinh("VNĐ");
                                    break;
                            }
                        } else {
                            // Preserve existing tinhTheo if not provided
                            khoanThu.setTinhTheo(existing.getTinhTheo() != null ? existing.getTinhTheo() : "Diện tích");
                            khoanThu.setDonViTinh(existing.getDonViTinh() != null ? existing.getDonViTinh() : "VNĐ/m²");
                        }
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
                    
                    // Handle tinhTheo from frontend even if donGia is not provided
                    String tinhTheo = (String) body.get("tinhTheo");
                    if (tinhTheo != null && !tinhTheo.trim().isEmpty()) {
                        String tinhTheoLower = tinhTheo.toLowerCase().trim();
                        switch (tinhTheoLower) {
                            case "dientich":
                                khoanThu.setTinhTheo("Diện tích");
                                khoanThu.setDonViTinh("VNĐ/m²");
                                break;
                            case "nhankhau":
                                khoanThu.setTinhTheo("Nhân khẩu");
                                khoanThu.setDonViTinh("VNĐ/người");
                                break;
                            case "hokhau":
                                khoanThu.setTinhTheo("Hộ khẩu");
                                khoanThu.setDonViTinh("VNĐ/hộ");
                                break;
                            case "xemay":
                                khoanThu.setTinhTheo("Xe máy");
                                khoanThu.setDonViTinh("VNĐ/xe máy");
                                break;
                            case "oto":
                                khoanThu.setTinhTheo("Ô tô");
                                khoanThu.setDonViTinh("VNĐ/ô tô");
                                break;
                            default:
                                khoanThu.setTinhTheo(tinhTheo);
                                khoanThu.setDonViTinh("VNĐ");
                                break;
                        }
                    } else {
                        // Preserve existing values
                        khoanThu.setDonViTinh(existing.getDonViTinh() != null ? existing.getDonViTinh() : "VNĐ/m²");
                        khoanThu.setTinhTheo(existing.getTinhTheo() != null ? existing.getTinhTheo() : "Diện tích");
                    }
                }
                
                // Handle moTa
                String moTa = (String) body.get("moTa");
                if (moTa != null) {
                    khoanThu.setMoTa(moTa.trim());
                } else {
                    khoanThu.setMoTa(existing.getMoTa() != null ? existing.getMoTa() : null);
                }
                
                // Ensure all required fields are set (use existing values if not provided)
                if (khoanThu.getLoai() == null) {
                    khoanThu.setLoai(existing.getLoai() != null ? existing.getLoai() : "Tự nguyện");
                }
                if (khoanThu.getDonGia() == null) {
                    khoanThu.setDonGia(existing.getDonGia() != null ? existing.getDonGia() : BigDecimal.ZERO);
                }
                if (khoanThu.getDonViTinh() == null) {
                    khoanThu.setDonViTinh(existing.getDonViTinh() != null ? existing.getDonViTinh() : "VNĐ/m²");
                }
                if (khoanThu.getTinhTheo() == null) {
                    khoanThu.setTinhTheo(existing.getTinhTheo() != null ? existing.getTinhTheo() : "Diện tích");
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

        // ========== PHUONG TIEN (Vehicle) ENDPOINTS ==========
        app.get("/api/phuong-tien", ctx -> {
            try {
                ctx.json(phuongTienService.getAllPhuongTien());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phuong-tien/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                PhuongTien phuongTien = phuongTienService.getPhuongTienById(id);
                if (phuongTien != null) {
                    ctx.json(phuongTien);
                } else {
                    ctx.status(404).json(createErrorResponse("Vehicle not found"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phuong-tien/ho-gia-dinh/{maHo}", ctx -> {
            try {
                Integer maHo = parseIntSafe(ctx, "maHo");
                if (maHo == null) return;
                ctx.json(phuongTienService.getPhuongTienByHoGiaDinh(maHo));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.get("/api/phuong-tien/search/{keyword}", ctx -> {
            try {
                String keyword = ctx.pathParam("keyword");
                ctx.json(phuongTienService.searchPhuongTien(keyword));
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.post("/api/phuong-tien", ctx -> {
            try {
                PhuongTien phuongTien = ctx.bodyAsClass(PhuongTien.class);
                boolean success = phuongTienService.addPhuongTien(phuongTien);
                if (success) {
                    ctx.status(201).json(createSuccessResponse("Vehicle created successfully", phuongTien));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to create vehicle (may be duplicate license plate)"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.put("/api/phuong-tien/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                PhuongTien phuongTien = ctx.bodyAsClass(PhuongTien.class);
                phuongTien.setId(id);
                boolean success = phuongTienService.updatePhuongTien(phuongTien);
                if (success) {
                    ctx.json(createSuccessResponse("Vehicle updated successfully", phuongTien));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update vehicle (may be duplicate license plate)"));
                }
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        app.delete("/api/phuong-tien/{id}", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                boolean success = phuongTienService.deletePhuongTien(id);
                if (success) {
                    ctx.json(createSuccessResponse("Vehicle deleted successfully", null));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to delete vehicle"));
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
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "VALIDATION_ERROR",
                        "Không thể tạo hộ gia đình. Vui lòng kiểm tra lại thông tin.",
                        ctx.path()
                    ));
                }
            } catch (IllegalArgumentException e) {
                // Handle business logic errors (duplicate room number, etc.)
                ctx.status(400).json(createStandardErrorResponse(
                    "Validation failed",
                    "VALIDATION_ERROR",
                    e.getMessage(),
                    ctx.path()
                ));
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
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "VALIDATION_ERROR",
                        "Không thể cập nhật hộ gia đình. Vui lòng kiểm tra lại thông tin.",
                        ctx.path()
                    ));
                }
            } catch (IllegalArgumentException e) {
                // Handle business logic errors (duplicate room number, etc.)
                ctx.status(400).json(createStandardErrorResponse(
                    "Validation failed",
                    "VALIDATION_ERROR",
                    e.getMessage(),
                    ctx.path()
                ));
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
                List<PhieuThu> phieuThuList = phieuThuService.getAllPhieuThu();
                // Manually serialize using our configured ObjectMapper
                String json = objectMapper.writeValueAsString(phieuThuList);
                ctx.result(json);
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
                    // Manually serialize using our configured ObjectMapper
                    String json = objectMapper.writeValueAsString(phieuThu);
                    ctx.result(json);
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
                // Parse body as Map to handle field mapping and date parsing
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                
                PhieuThu phieuThu = new PhieuThu();
                
                // Validate and set maHo
                Object maHoObj = body.get("maHo");
                if (maHoObj == null) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "MISSING_REQUIRED_FIELD",
                        "maHo is required",
                        ctx.path()
                    ));
                    return;
                }
                int maHo = parseIntegerFromObject(maHoObj);
                if (maHo <= 0) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "INVALID_MAHO",
                        "maHo must be a positive integer",
                        ctx.path()
                    ));
                    return;
                }
                phieuThu.setMaHo(maHo);
                
                // Validate and set maDot
                Object maDotObj = body.get("maDot");
                if (maDotObj == null) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "MISSING_REQUIRED_FIELD",
                        "maDot is required",
                        ctx.path()
                    ));
                    return;
                }
                int maDot = parseIntegerFromObject(maDotObj);
                if (maDot <= 0) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "INVALID_MADOT",
                        "maDot must be a positive integer",
                        ctx.path()
                    ));
                    return;
                }
                phieuThu.setMaDot(maDot);
                
                // Set maTaiKhoan (default to 1 if not provided)
                Object maTaiKhoanObj = body.get("maTaiKhoan");
                int maTaiKhoan = 1;
                if (maTaiKhoanObj != null) {
                    maTaiKhoan = parseIntegerFromObject(maTaiKhoanObj);
                    if (maTaiKhoan <= 0) {
                        maTaiKhoan = 1;
                    }
                }
                phieuThu.setMaTaiKhoan(maTaiKhoan);
                
                // Parse ngayLap (can be String or LocalDateTime)
                Object ngayLapObj = body.get("ngayLap");
                if (ngayLapObj != null) {
                    try {
                        if (ngayLapObj instanceof String) {
                            String ngayLapStr = (String) ngayLapObj;
                            // Try to parse ISO format: "2025-12-27T15:28" or "2025-12-27T15:28:00"
                            if (ngayLapStr.contains("T")) {
                                phieuThu.setNgayLap(java.time.LocalDateTime.parse(ngayLapStr.replace(" ", "T")));
                            } else {
                                phieuThu.setNgayLap(java.time.LocalDateTime.now());
                            }
                        } else {
                            // If already LocalDateTime, use objectMapper
                            phieuThu.setNgayLap(objectMapper.convertValue(ngayLapObj, java.time.LocalDateTime.class));
                        }
                    } catch (Exception e) {
                        // If parsing fails, use current time
                        phieuThu.setNgayLap(java.time.LocalDateTime.now());
                    }
                } else {
                    phieuThu.setNgayLap(java.time.LocalDateTime.now());
                }
                
                // Parse tongTien (can be Number or String)
                Object tongTienObj = body.get("tongTien");
                if (tongTienObj != null) {
                    try {
                        BigDecimal tongTien;
                        if (tongTienObj instanceof Number) {
                            tongTien = BigDecimal.valueOf(((Number) tongTienObj).doubleValue());
                        } else if (tongTienObj instanceof String) {
                            String tongTienStr = (String) tongTienObj;
                            if (tongTienStr.trim().isEmpty()) {
                                tongTien = BigDecimal.ZERO;
                            } else {
                                tongTien = new BigDecimal(tongTienStr);
                            }
                        } else {
                            tongTien = BigDecimal.ZERO;
                        }
                        phieuThu.setTongTien(tongTien);
                    } catch (NumberFormatException e) {
                        phieuThu.setTongTien(BigDecimal.ZERO);
                    }
                } else {
                    phieuThu.setTongTien(BigDecimal.ZERO);
                }
                
                // Set trangThai (default to "ChuaThu")
                String trangThai = (String) body.get("trangThai");
                phieuThu.setTrangThai(trangThai != null && !trangThai.trim().isEmpty() ? trangThai : "ChuaThu");
                
                // Set hinhThucThu (can be null)
                String hinhThucThu = (String) body.get("hinhThucThu");
                phieuThu.setHinhThucThu(hinhThucThu != null && !hinhThucThu.trim().isEmpty() ? hinhThucThu : null);
                
                // Check if chiTietList is provided in body
                @SuppressWarnings("unchecked")
                List<ChiTietThu> chiTietList = body.containsKey("chiTietList") && body.get("chiTietList") != null
                        ? objectMapper.convertValue(body.get("chiTietList"),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, ChiTietThu.class))
                        : null;
                
                int maPhieu;
                if (chiTietList != null && !chiTietList.isEmpty()) {
                    // If chiTietList is provided, use createPhieuThuWithDetails
                    maPhieu = phieuThuService.createPhieuThuWithDetails(phieuThu, chiTietList);
                } else {
                    // If no chiTietList, automatically generate from mandatory fees
                    // Generate ChiTietThu from mandatory fees and calculate total
                    chiTietList = generateChiTietThuFromMandatoryFees(maHo, maDot);
                    if (chiTietList == null || chiTietList.isEmpty()) {
                        ctx.status(400).json(createStandardErrorResponse(
                            "No mandatory fees",
                            "NO_MANDATORY_FEES",
                            "Không có khoản thu bắt buộc nào để tạo phiếu thu",
                            ctx.path()
                        ));
                        return;
                    }
                    
                    // Calculate total from ChiTietThu
                    BigDecimal calculatedTotal = BigDecimal.ZERO;
                    for (ChiTietThu chiTiet : chiTietList) {
                        if (chiTiet.getThanhTien() != null) {
                            calculatedTotal = calculatedTotal.add(chiTiet.getThanhTien());
                        }
                    }
                    
                    // Update tongTien with calculated total
                    phieuThu.setTongTien(calculatedTotal);
                    
                    // Create receipt with details
                    maPhieu = phieuThuService.createPhieuThuWithDetails(phieuThu, chiTietList);
                }
                
                if (maPhieu > 0) {
                    phieuThu.setId(maPhieu);
                    ctx.status(201).json(createSuccessResponse("Receipt created successfully", phieuThu));
                } else {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Failed to create receipt",
                        "CREATE_FAILED",
                        "Service layer returned -1",
                        ctx.path()
                    ));
                }
            } catch (IllegalArgumentException e) {
                // Handle business logic errors (e.g., drive is closed)
                ctx.status(400).json(createStandardErrorResponse(
                    "Cannot create receipt",
                    "BUSINESS_RULE_VIOLATION",
                    e.getMessage(),
                    ctx.path()
                ));
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
        app.get("/api/phieu-thu/calculate-total", ctx -> {
            try {
                Integer maHo = parseIntSafe(ctx, "maHo");
                Integer maDot = parseIntSafe(ctx, "maDot");
                if (maHo == null || maDot == null) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Validation failed",
                        "MISSING_PARAMETERS",
                        "maHo and maDot are required",
                        ctx.path()
                    ));
                    return;
                }
                BigDecimal totalAmount = phieuThuService.calculateTotalAmountForHousehold(maHo, maDot);
                if (totalAmount == null) {
                    ctx.status(400).json(createStandardErrorResponse(
                        "Calculation failed",
                        "CALCULATION_FAILED",
                        "Could not calculate total amount. Check if drive is open and household exists.",
                        ctx.path()
                    ));
                    return;
                }
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("maHo", maHo);
                result.put("maDot", maDot);
                result.put("tongTien", totalAmount);
                ctx.json(createSuccessResponse("Total amount calculated successfully", result));
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
                    // Return updated PhieuThu with details
                    PhieuThu updatedPhieuThu = phieuThuService.getPhieuThuWithDetails(id);
                    ctx.json(createSuccessResponse("Receipt updated successfully", updatedPhieuThu));
                } else {
                    ctx.status(400).json(createErrorResponse("Failed to update receipt (may be paid)"));
                }
            } catch (IllegalArgumentException e) {
                // Handle business logic errors (duplicate, closed drive, etc.)
                ctx.status(400).json(createStandardErrorResponse(
                    "Validation failed",
                    "VALIDATION_ERROR",
                    e.getMessage(),
                    ctx.path()
                ));
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
        // Create batch invoices
        app.post("/api/phieu-thu/batch", ctx -> {
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                Object maDotThuObj = body.get("maDotThu");
                if (maDotThuObj == null) {
                    ctx.status(400).json(createErrorResponse("maDotThu is required"));
                    return;
                }
                int maDotThu = Integer.parseInt(maDotThuObj.toString());
                int count = phieuThuService.createBatch(maDotThu);
                ctx.json(createSuccessResponse("Created " + count + " invoices successfully", count));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(createErrorResponse(e.getMessage()));
            } catch (Exception e) {
                logger.severe("Error creating batch invoices: " + e.getMessage());
                e.printStackTrace();
                handleException(ctx, e);
            }
        });
        // Get invoice detail with JOIN
        app.get("/api/phieu-thu/{id}/detail", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                try {
                    Map<String, Object> detail = phieuThuService.getInvoiceDetail(id);
                    ctx.json(createSuccessResponse("Invoice detail retrieved", detail));
                } catch (java.util.NoSuchElementException e) {
                    ctx.status(404).json(createErrorResponse(e.getMessage()));
                }
            } catch (Exception e) {
                logger.severe("Error getting invoice detail: " + e.getMessage());
                e.printStackTrace();
                handleException(ctx, e);
            }
        });
        // Export invoice to PDF
        app.get("/api/phieu-thu/{id}/export", ctx -> {
            try {
                Integer id = parseIntSafe(ctx, "id");
                if (id == null) return;
                try {
                    InputStream pdfStream = phieuThuService.exportInvoiceToPdf(id);
                    byte[] pdfBytes = pdfStream.readAllBytes();
                    
                    String filename = "PhieuThu_" + id + "_" + 
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
                    
                    ctx.header("Content-Type", "application/pdf");
                    ctx.header("Content-Disposition", "inline; filename=\"" + filename + "\"");
                    ctx.result(pdfBytes);
                } catch (java.util.NoSuchElementException e) {
                    ctx.status(404).json(createErrorResponse(e.getMessage()));
                }
            } catch (Exception e) {
                logger.severe("Error exporting invoice to PDF: " + e.getMessage());
                e.printStackTrace();
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
                ctx.json(thongKeService.getDebtDetails());
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
        app.get("/api/thong-ke/demographics", ctx -> {
            try {
                ctx.json(thongKeService.getResidentDemographics());
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });

        // ========== BAO CAO (Reports) ENDPOINTS ==========
        com.bluemoon.services.BaoCaoService baoCaoService = new com.bluemoon.services.impl.BaoCaoServiceImpl();
        
        // Revenue Report - JSON
        app.get("/api/reports/revenue", ctx -> {
            try {
                String monthStr = ctx.queryParam("month");
                String yearStr = ctx.queryParam("year");
                String fromDateStr = ctx.queryParam("fromDate");
                String toDateStr = ctx.queryParam("toDate");
                
                List<com.bluemoon.models.dto.BaoCaoThuDTO> result;
                
                if (monthStr != null && yearStr != null) {
                    int month = Integer.parseInt(monthStr);
                    int year = Integer.parseInt(yearStr);
                    result = baoCaoService.getRevenueReport(month, year);
                } else if (fromDateStr != null && toDateStr != null) {
                    java.time.LocalDate fromDate = java.time.LocalDate.parse(fromDateStr);
                    java.time.LocalDate toDate = java.time.LocalDate.parse(toDateStr);
                    result = baoCaoService.getRevenueReport(fromDate, toDate);
                } else {
                    ctx.status(400).json(createErrorResponse("Either month/year or fromDate/toDate parameters are required"));
                    return;
                }
                
                ctx.json(result);
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        
        // Revenue Report - Excel Export
        app.get("/api/reports/export/revenue", ctx -> {
            try {
                String monthStr = ctx.queryParam("month");
                String yearStr = ctx.queryParam("year");
                String fromDateStr = ctx.queryParam("fromDate");
                String toDateStr = ctx.queryParam("toDate");
                
                List<com.bluemoon.models.dto.BaoCaoThuDTO> data;
                java.time.LocalDate fromDate;
                java.time.LocalDate toDate;
                
                if (monthStr != null && yearStr != null) {
                    int month = Integer.parseInt(monthStr);
                    int year = Integer.parseInt(yearStr);
                    fromDate = java.time.LocalDate.of(year, month, 1);
                    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
                    data = baoCaoService.getRevenueReport(month, year);
                } else if (fromDateStr != null && toDateStr != null) {
                    fromDate = java.time.LocalDate.parse(fromDateStr);
                    toDate = java.time.LocalDate.parse(toDateStr);
                    data = baoCaoService.getRevenueReport(fromDate, toDate);
                } else {
                    ctx.status(400).json(createErrorResponse("Either month/year or fromDate/toDate parameters are required"));
                    return;
                }
                
                java.io.InputStream excelStream = baoCaoService.exportRevenueToExcel(data, fromDate, toDate);
                byte[] excelBytes = excelStream.readAllBytes();
                
                String filename = "BaoCaoThu_" + fromDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                                 "_" + toDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
                
                ctx.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ctx.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                ctx.result(excelBytes);
            } catch (Exception e) {
                logger.severe("Error exporting revenue report: " + e.getMessage());
                e.printStackTrace();
                handleException(ctx, e);
            }
        });
        
        // Debt Report - JSON
        app.get("/api/reports/debt", ctx -> {
            try {
                String maDotStr = ctx.queryParam("maDot");
                List<com.bluemoon.models.dto.BaoCaoCongNoDTO> result;
                
                if (maDotStr != null && !maDotStr.isEmpty()) {
                    int maDot = Integer.parseInt(maDotStr);
                    result = baoCaoService.getDebtReport(maDot);
                } else {
                    result = baoCaoService.getDebtReport();
                }
                
                ctx.json(result);
            } catch (Exception e) {
                handleException(ctx, e);
            }
        });
        
        // Debt Report - Excel Export
        app.get("/api/reports/export/debt", ctx -> {
            try {
                String maDotStr = ctx.queryParam("maDot");
                List<com.bluemoon.models.dto.BaoCaoCongNoDTO> data;
                
                if (maDotStr != null && !maDotStr.isEmpty()) {
                    int maDot = Integer.parseInt(maDotStr);
                    data = baoCaoService.getDebtReport(maDot);
                } else {
                    data = baoCaoService.getDebtReport();
                }
                
                java.io.InputStream excelStream = baoCaoService.exportDebtToExcel(data);
                byte[] excelBytes = excelStream.readAllBytes();
                
                String filename = "BaoCaoCongNo_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
                
                ctx.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ctx.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                ctx.result(excelBytes);
            } catch (Exception e) {
                logger.severe("Error exporting debt report: " + e.getMessage());
                e.printStackTrace();
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
                    "auth", "POST /api/login, POST /api/change-password, GET /api/check-username/{username}",
                    "accounts", "GET /api/tai-khoan, GET /api/tai-khoan/{id}, POST /api/tai-khoan, PUT /api/tai-khoan/{id}, DELETE /api/tai-khoan/{id}, PUT /api/tai-khoan/{id}/status",
                    "fees", "GET /api/khoan-thu, POST /api/khoan-thu, PUT /api/khoan-thu/{id}, DELETE /api/khoan-thu/{id}",
                    "collection_drives", "GET /api/dot-thu, GET /api/dot-thu/{id}, POST /api/dot-thu, PUT /api/dot-thu/{id}, DELETE /api/dot-thu/{id}",
                    "households", "GET /api/ho-gia-dinh, GET /api/ho-gia-dinh/{id}, POST /api/ho-gia-dinh, PUT /api/ho-gia-dinh/{id}, DELETE /api/ho-gia-dinh/{id}",
                    "residents", "GET /api/nhan-khau, GET /api/nhan-khau/{id}, POST /api/nhan-khau, PUT /api/nhan-khau/{id}, DELETE /api/nhan-khau/{id}, PUT /api/nhan-khau/{id}/status",
                    "receipts", "GET /api/phieu-thu, GET /api/phieu-thu/{id}, POST /api/phieu-thu, PUT /api/phieu-thu/{id}, DELETE /api/phieu-thu/{id}",
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
            // Parse body as Map<String, Object> to handle JSON deserialization safely
            Map<String, Object> body;
            try {
                body = ctx.bodyAsClass(Map.class);
            } catch (Exception parseError) {
                // Fallback: read body as string and parse manually
                String bodyText = ctx.body();
                if (bodyText == null || bodyText.trim().isEmpty()) {
                    ctx.status(400).json(createErrorResponse("Request body is required"));
                    return;
                }
                try {
                    body = objectMapper.readValue(bodyText, Map.class);
                } catch (Exception e) {
                    logger.severe("Failed to parse login request body: " + bodyText);
                    ctx.status(400).json(createErrorResponse("Invalid request format"));
                    return;
                }
            }

            // Extract username and password safely
            Object usernameObj = body.get("username");
            Object passwordObj = body.get("password");

            String username = usernameObj != null ? usernameObj.toString().trim() : null;
            String password = passwordObj != null ? passwordObj.toString() : null;

            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
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
            logger.severe("Error in handleLogin: " + e.getMessage());
            e.printStackTrace();
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
     * Generate ChiTietThu list from mandatory fees for a household and collection drive.
     * This method calculates fees based on tinhTheo (calculation method) and creates ChiTietThu objects.
     * 
     * @param maHo Household ID
     * @param maDot Collection drive ID
     * @return List of ChiTietThu, or null if error
     */
    private List<ChiTietThu> generateChiTietThuFromMandatoryFees(int maHo, int maDot) {
        try {
            // Get household
            HoGiaDinh household = hoGiaDinhService.findById(maHo);
            if (household == null) {
                logger.warning("Household not found: " + maHo);
                return null;
            }

            // Get all mandatory fees
            List<KhoanThu> allFees = khoanThuService.getAllKhoanThu();
            List<KhoanThu> mandatoryFees = new java.util.ArrayList<>();
            for (KhoanThu fee : allFees) {
                if (fee.isBatBuoc()) {
                    mandatoryFees.add(fee);
                }
            }

            if (mandatoryFees.isEmpty()) {
                logger.info("No mandatory fees found for household: " + maHo);
                return new java.util.ArrayList<>();
            }

            // Get household data
            List<NhanKhau> members = nhanKhauService.getNhanKhauByHoGiaDinh(maHo);
            int memberCount = members.size();

            // Count vehicles
            List<PhuongTien> vehicles = phuongTienService.getPhuongTienByHoGiaDinh(maHo);
            int motorbikeCount = 0;
            int carCount = 0;
            for (PhuongTien vehicle : vehicles) {
                String loaiXe = vehicle.getLoaiXe();
                if (loaiXe != null) {
                    String loaiXeLower = loaiXe.toLowerCase().trim();
                    if (loaiXeLower.contains("xe máy") || loaiXeLower.contains("xemay") || 
                        loaiXeLower.contains("moto") || loaiXeLower.contains("xe may")) {
                        motorbikeCount++;
                    } else if (loaiXeLower.contains("ô tô") || loaiXeLower.contains("oto") || 
                              loaiXeLower.contains("car") || loaiXeLower.contains("o to")) {
                        carCount++;
                    }
                }
            }

            // Generate ChiTietThu for each mandatory fee
            List<ChiTietThu> chiTietList = new java.util.ArrayList<>();
            for (KhoanThu fee : mandatoryFees) {
                BigDecimal feeAmount = calculateFeeAmountForHousehold(
                    fee, 
                    household, 
                    memberCount,
                    motorbikeCount,
                    carCount
                );

                if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    ChiTietThu chiTiet = new ChiTietThu();
                    chiTiet.setMaKhoan(fee.getId());
                    chiTiet.setSoLuong(BigDecimal.ONE); // Default quantity
                    chiTiet.setDonGia(fee.getDonGia()); // Snapshot current price
                    chiTiet.setThanhTien(feeAmount);
                    chiTietList.add(chiTiet);
                }
            }

            logger.info("Generated " + chiTietList.size() + " ChiTietThu for household " + maHo + " in drive " + maDot);
            return chiTietList;

        } catch (Exception e) {
            logger.severe("Error generating ChiTietThu from mandatory fees: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculate fee amount for a household based on tinhTheo (calculation method).
     * Similar to PhieuThuServiceImpl.calculateFeeAmount but accessible from WebServer.
     * 
     * @param fee Fee definition
     * @param household Household
     * @param memberCount Number of members
     * @param motorbikeCount Number of motorbikes
     * @param carCount Number of cars
     * @return Calculated fee amount
     */
    private BigDecimal calculateFeeAmountForHousehold(KhoanThu fee, HoGiaDinh household, 
                                                      int memberCount, int motorbikeCount, int carCount) {
        if (fee == null || fee.getDonGia() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal basePrice = fee.getDonGia();
        String tinhTheo = fee.getTinhTheo();
        
        if (tinhTheo == null || tinhTheo.trim().isEmpty()) {
            // Default: Fixed fee per household
            return basePrice;
        }

        String tinhTheoLower = tinhTheo.toLowerCase().trim();
        BigDecimal multiplier = BigDecimal.ONE;

        switch (tinhTheoLower) {
            case "hokhau":
            case "hộ khẩu":
            case "codinh":
            case "cố định":
            case "fixed":
                multiplier = BigDecimal.ONE;
                break;

            case "nhankhau":
            case "nhân khẩu":
            case "person":
            case "perperson":
                if (memberCount <= 0) {
                    return BigDecimal.ZERO;
                }
                multiplier = BigDecimal.valueOf(memberCount);
                break;

            case "dientich":
            case "diện tích":
            case "area":
            case "perarea":
                if (household.getDienTich() == null || household.getDienTich().compareTo(BigDecimal.ZERO) <= 0) {
                    return BigDecimal.ZERO;
                }
                multiplier = household.getDienTich();
                break;

            case "xemay":
            case "xe máy":
            case "xe may":
            case "motorbike":
            case "moto":
                if (motorbikeCount <= 0) {
                    return BigDecimal.ZERO;
                }
                multiplier = BigDecimal.valueOf(motorbikeCount);
                break;

            case "oto":
            case "ô tô":
            case "o to":
            case "car":
            case "automobile":
                if (carCount <= 0) {
                    return BigDecimal.ZERO;
                }
                multiplier = BigDecimal.valueOf(carCount);
                break;

            default:
                multiplier = BigDecimal.ONE;
                break;
        }

        return basePrice.multiply(multiplier);
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

    /**
     * Parse integer from Object (can be Number, String, or Integer).
     * @param obj Object to parse
     * @return parsed integer, or 0 if parsing fails
     */
    private int parseIntegerFromObject(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt(((String) obj).trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}

