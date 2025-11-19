package com.bluemoon.controllers;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.models.DotThu;
import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.KhoanThu;
import com.bluemoon.models.PhieuThu;
import com.bluemoon.services.DotThuService;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.KhoanThuService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.impl.DotThuServiceImpl;
import com.bluemoon.services.impl.HoGiaDinhServiceImpl;
import com.bluemoon.services.impl.KhoanThuServiceImpl;
import com.bluemoon.services.impl.PhieuThuServiceImpl;

/**
 * Controller quản lý Phiếu Thu và Chi Tiết Thu.
 * Xử lý nghiệp vụ: Tạo hóa đơn, thêm khoản thu vào hóa đơn, tính toán tổng tiền.
 */
public class PhieuThuController implements Initializable {

    // --- Services ---
    private final PhieuThuService phieuThuService = new PhieuThuServiceImpl();
    private final HoGiaDinhService hoGiaDinhService = new HoGiaDinhServiceImpl();
    private final DotThuService dotThuService = new DotThuServiceImpl();
    private final KhoanThuService khoanThuService = new KhoanThuServiceImpl();

    // --- Data Models ---
    private final ObservableList<PhieuThu> receiptList = FXCollections.observableArrayList();
    private final ObservableList<ChiTietThu> currentDetails = FXCollections.observableArrayList();
    private final ObservableList<ChiTietThu> detailList = FXCollections.observableArrayList();
    private final ObservableList<HoGiaDinh> householdList = FXCollections.observableArrayList();
    private final ObservableList<DotThu> driveList = FXCollections.observableArrayList();
    private final ObservableList<KhoanThu> feeList = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- FXML Components: Main Table (Danh sách phiếu thu) ---
    @FXML
    private TableView<PhieuThu> tablePhieuThu;

    @FXML
    private TableColumn<PhieuThu, Integer> colMaPhieu;

    @FXML
    private TableColumn<PhieuThu, String> colTenHo;

    @FXML
    private TableColumn<PhieuThu, String> colTenDot;

    @FXML
    private TableColumn<PhieuThu, BigDecimal> colTongTien;

    @FXML
    private TableColumn<PhieuThu, LocalDateTime> colNgayLap;

    // --- Header Inputs ---
    @FXML
    private ComboBox<HoGiaDinh> cbHoGiaDinh;

    @FXML
    private ComboBox<DotThu> cbDotThu;

    @FXML
    private Label lblTongTien;

    // --- Detail Input (Adding items) ---
    @FXML
    private ComboBox<KhoanThu> cbKhoanThu;

    @FXML
    private TextField tfSoLuong;

    @FXML
    private Button btnAddDetail;

    // --- Detail Table (Items in current receipt) ---
    @FXML
    private TableView<ChiTietThu> tableChiTiet;

    @FXML
    private TableColumn<ChiTietThu, String> colTenKhoan;

    @FXML
    private TableColumn<ChiTietThu, BigDecimal> colDonGia;

    @FXML
    private TableColumn<ChiTietThu, BigDecimal> colSoLuong;

    @FXML
    private TableColumn<ChiTietThu, BigDecimal> colThanhTien;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        loadComboBoxData();
        setupSelectionListener();
        loadData();
        // Set default quantity to 1
        tfSoLuong.setText("1");
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong TableView.
     */
    private void configureColumns() {
        // Main table columns
        colMaPhieu.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Custom cell factory for colTenHo (lookup from maHo)
        colTenHo.setCellValueFactory(cellData -> {
            PhieuThu phieuThu = cellData.getValue();
            if (phieuThu == null) {
                return new SimpleStringProperty("");
            }
            HoGiaDinh hoGiaDinh = hoGiaDinhService.findById(phieuThu.getMaHo());
            String tenHo = hoGiaDinh != null ? hoGiaDinh.getTenChuHo() : "N/A";
            return new SimpleStringProperty(tenHo);
        });

        // Custom cell factory for colTenDot (lookup from maDot)
        colTenDot.setCellValueFactory(cellData -> {
            PhieuThu phieuThu = cellData.getValue();
            if (phieuThu == null) {
                return new SimpleStringProperty("");
            }
            DotThu dotThu = dotThuService.getDotThuById(phieuThu.getMaDot());
            String tenDot = dotThu != null ? dotThu.getTenDot() : "N/A";
            return new SimpleStringProperty(tenDot);
        });

        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));

        // Format LocalDateTime hiển thị trong bảng
        colNgayLap.setCellValueFactory(new PropertyValueFactory<>("ngayLap"));
        colNgayLap.setCellFactory(column -> new javafx.scene.control.TableCell<PhieuThu, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_TIME_FORMATTER));
                }
            }
        });

        tablePhieuThu.setItems(receiptList);

        // Detail table columns
        colTenKhoan.setCellValueFactory(new PropertyValueFactory<>("tenKhoan"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        tableChiTiet.setItems(currentDetails);
    }

    /**
     * Nạp dữ liệu vào các ComboBox.
     */
    private void loadComboBoxData() {
        try {
            // Load households
            householdList.setAll(hoGiaDinhService.getAll());
            cbHoGiaDinh.setItems(householdList);
            cbHoGiaDinh.setConverter(new StringConverter<HoGiaDinh>() {
                @Override
                public String toString(HoGiaDinh hoGiaDinh) {
                    if (hoGiaDinh == null) {
                        return "";
                    }
                    return hoGiaDinh.getMaHo() + " - " + (hoGiaDinh.getTenChuHo() != null ? hoGiaDinh.getTenChuHo() : "");
                }

                @Override
                public HoGiaDinh fromString(String string) {
                    return null;
                }
            });

            // Load drives
            driveList.setAll(dotThuService.getAllDotThu());
            cbDotThu.setItems(driveList);
            cbDotThu.setConverter(new StringConverter<DotThu>() {
                @Override
                public String toString(DotThu dotThu) {
                    if (dotThu == null) {
                        return "";
                    }
                    return dotThu.getMaDot() + " - " + dotThu.getTenDot();
                }

                @Override
                public DotThu fromString(String string) {
                    return null;
                }
            });

            // Load fees
            feeList.setAll(khoanThuService.getAllKhoanThu());
            cbKhoanThu.setItems(feeList);
            cbKhoanThu.setConverter(new StringConverter<KhoanThu>() {
                @Override
                public String toString(KhoanThu khoanThu) {
                    if (khoanThu == null) {
                        return "";
                    }
                    return khoanThu.getTenKhoanThu() + " - " + khoanThu.getSoTien() + " VNĐ";
                }

                @Override
                public KhoanThu fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải dữ liệu cho ComboBox: " + e.getMessage());
        }
    }

    /**
     * Thiết lập listener cho TableView selection để tự động load chi tiết khi chọn phiếu thu.
     */
    private void setupSelectionListener() {
        tablePhieuThu.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadDetailsForReceipt(newValue.getId());
                    } else {
                        detailList.clear();
                        tableChiTiet.setItems(detailList);
                    }
                });
    }

    /**
     * Nạp dữ liệu từ service vào TableView.
     */
    private void loadData() {
        try {
            receiptList.setAll(phieuThuService.getAllPhieuThu());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách phiếu thu: " + e.getMessage());
        }
    }

    /**
     * Nạp chi tiết thu cho một phiếu thu đã chọn.
     *
     * @param maPhieu mã phiếu thu
     */
    private void loadDetailsForReceipt(int maPhieu) {
        try {
            List<ChiTietThu> chiTietList = phieuThuService.getChiTietThuByPhieu(maPhieu);
            // Resolve tenKhoan for each ChiTietThu
            for (ChiTietThu chiTiet : chiTietList) {
                KhoanThu khoanThu = khoanThuService.getAllKhoanThu().stream()
                        .filter(k -> k.getId() == chiTiet.getMaKhoan())
                        .findFirst()
                        .orElse(null);
                if (khoanThu != null) {
                    chiTiet.setTenKhoan(khoanThu.getTenKhoanThu());
                }
            }
            detailList.setAll(chiTietList);
            tableChiTiet.setItems(detailList);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải chi tiết phiếu thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện thêm chi tiết vào phiếu thu đang tạo.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleAddDetail(ActionEvent event) {
        try {
            // Validate
            if (cbKhoanThu.getValue() == null) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn khoản thu.");
                return;
            }

            String soLuongStr = tfSoLuong.getText().trim();
            if (soLuongStr.isEmpty()) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập số lượng.");
                return;
            }

            BigDecimal soLuong;
            try {
                soLuong = new BigDecimal(soLuongStr);
                if (soLuong.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số lượng phải lớn hơn 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số lượng phải là số.");
                return;
            }

            // Get fee price
            KhoanThu khoanThu = cbKhoanThu.getValue();
            BigDecimal donGia = khoanThu.getSoTien();

            // Calculate subtotal
            BigDecimal thanhTien = donGia.multiply(soLuong);

            // Create ChiTietThu object (temporary maPhieu = 0)
            ChiTietThu chiTiet = new ChiTietThu();
            chiTiet.setMaPhieu(0); // Temporary, will be set when saving receipt
            chiTiet.setMaKhoan(khoanThu.getId());
            chiTiet.setSoLuong(soLuong);
            chiTiet.setDonGia(donGia);
            chiTiet.setThanhTien(thanhTien);
            chiTiet.setTenKhoan(khoanThu.getTenKhoanThu()); // For display

            // Add to currentDetails list
            currentDetails.add(chiTiet);

            // Update Total
            updateTotalLabel();

            // Clear input fields
            cbKhoanThu.setValue(null);
            tfSoLuong.setText("1");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi thêm chi tiết: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa chi tiết khỏi phiếu thu đang tạo.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleRemoveDetail(ActionEvent event) {
        try {
            ChiTietThu selected = tableChiTiet.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một chi tiết để xóa.");
                return;
            }

            // Remove from currentDetails (only if it's a new item, maPhieu = 0)
            if (selected.getMaPhieu() == 0) {
                currentDetails.remove(selected);
                updateTotalLabel();
            } else {
                showAlert(AlertType.WARNING, "Không thể xóa",
                        "Chỉ có thể xóa các chi tiết chưa lưu. Vui lòng xóa phiếu thu để xóa chi tiết đã lưu.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xóa chi tiết: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện lưu phiếu thu và chi tiết.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleSaveReceipt(ActionEvent event) {
        try {
            // Validate
            if (cbHoGiaDinh.getValue() == null) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn hộ gia đình.");
                return;
            }

            if (cbDotThu.getValue() == null) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn đợt thu.");
                return;
            }

            if (currentDetails.isEmpty()) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng thêm ít nhất một khoản thu.");
                return;
            }

            // Calculate total
            BigDecimal tongTien = calculateTotal();

            // Create PhieuThu object
            PhieuThu phieuThu = new PhieuThu();
            phieuThu.setMaHo(cbHoGiaDinh.getValue().getId());
            phieuThu.setMaDot(cbDotThu.getValue().getMaDot());
            phieuThu.setNgayLap(LocalDateTime.now());
            phieuThu.setTongTien(tongTien);
            phieuThu.setTrangThai("Chưa thanh toán");
            phieuThu.setHinhThucThu("Chưa xác định");
            phieuThu.setMaTaiKhoan(1); // Placeholder - should get from session

            // Save receipt with details
            int maPhieu = phieuThuService.createPhieuThuWithDetails(phieuThu, currentDetails);
            if (maPhieu == -1) {
                showAlert(AlertType.ERROR, "Thất bại", "Không thể lưu phiếu thu. Vui lòng thử lại.");
                return;
            }

            showAlert(AlertType.INFORMATION, "Thành công",
                    "Đã lưu phiếu thu thành công. Mã phiếu: " + maPhieu);

            // Clear form
            handleClear(event);
            loadData();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi lưu phiếu thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa form và reset dữ liệu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    /**
     * Tính tổng tiền từ danh sách chi tiết hiện tại.
     *
     * @return tổng tiền
     */
    private BigDecimal calculateTotal() {
        return currentDetails.stream()
                .map(ChiTietThu::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Cập nhật Label hiển thị tổng tiền.
     */
    private void updateTotalLabel() {
        BigDecimal total = calculateTotal();
        lblTongTien.setText(total.toString() + " VNĐ");
    }

    /**
     * Xóa tất cả dữ liệu trong form và reset danh sách chi tiết.
     */
    private void clearForm() {
        cbHoGiaDinh.setValue(null);
        cbDotThu.setValue(null);
        cbKhoanThu.setValue(null);
        tfSoLuong.setText("1");
        currentDetails.clear();
        lblTongTien.setText("0 VNĐ");
        tablePhieuThu.getSelectionModel().clearSelection();
    }

    /**
     * Hiển thị Alert dialog.
     *
     * @param type    loại alert (ERROR, WARNING, INFORMATION, CONFIRMATION)
     * @param title   tiêu đề
     * @param content nội dung
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
