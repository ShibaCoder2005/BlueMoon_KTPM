package com.bluemoon.controllers;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.bluemoon.models.LichSuNopTien;
import com.bluemoon.models.PhieuThu;
import com.bluemoon.services.LichSuNopTienService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.impl.LichSuNopTienServiceImpl;
import com.bluemoon.services.impl.PhieuThuServiceImpl;

/**
 * Controller ghi nhận các lần nộp tiền cho Phiếu Thu.
 * Xử lý logic ghi nhận thanh toán và cập nhật trạng thái phiếu thu.
 */
public class LichSuNopTienController implements Initializable {

    @FXML
    private TableView<LichSuNopTien> tableLichSu;

    @FXML
    private TableColumn<LichSuNopTien, Integer> colId;

    @FXML
    private TableColumn<LichSuNopTien, Integer> colMaPhieu;

    @FXML
    private TableColumn<LichSuNopTien, LocalDateTime> colNgayNop;

    @FXML
    private TableColumn<LichSuNopTien, BigDecimal> colSoTien;

    @FXML
    private TableColumn<LichSuNopTien, String> colPhuongThuc;

    @FXML
    private TableColumn<LichSuNopTien, String> colNguoiThu;

    @FXML
    private TextField tfMaPhieu;

    @FXML
    private TextField tfSoTien;

    @FXML
    private TextField tfNguoiThu;

    @FXML
    private ComboBox<String> cbPhuongThuc;

    @FXML
    private TextField tfSearch;

    private final LichSuNopTienService paymentService = new LichSuNopTienServiceImpl();
    private final PhieuThuService receiptService = new PhieuThuServiceImpl();
    private final ObservableList<LichSuNopTien> lichSuData = FXCollections.observableArrayList();
    private FilteredList<LichSuNopTien> filteredData;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        initializeComboBox();
        setupSearchListener();
        loadData();
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong TableView.
     */
    private void configureColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMaPhieu.setCellValueFactory(new PropertyValueFactory<>("maPhieu"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colPhuongThuc.setCellValueFactory(new PropertyValueFactory<>("phuongThuc"));
        colNguoiThu.setCellValueFactory(new PropertyValueFactory<>("nguoiThuName"));

        // Format LocalDateTime hiển thị trong bảng
        colNgayNop.setCellValueFactory(new PropertyValueFactory<>("ngayNop"));
        colNgayNop.setCellFactory(column -> new javafx.scene.control.TableCell<LichSuNopTien, LocalDateTime>() {
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
    }

    /**
     * Khởi tạo ComboBox phương thức thanh toán với các giá trị mặc định.
     */
    private void initializeComboBox() {
        ObservableList<String> phuongThucList = FXCollections.observableArrayList(
                "Tiền mặt",
                "Chuyển khoản",
                "Ví điện tử"
        );
        cbPhuongThuc.setItems(phuongThucList);
    }

    /**
     * Thiết lập listener cho TextField tìm kiếm để lọc dữ liệu trong TableView.
     */
    private void setupSearchListener() {
        filteredData = new FilteredList<>(lichSuData, p -> true);
        tableLichSu.setItems(filteredData);

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(createSearchPredicate(newValue));
        });
    }

    /**
     * Tạo predicate để lọc dữ liệu theo từ khóa tìm kiếm.
     *
     * @param keyword từ khóa tìm kiếm
     * @return predicate để lọc
     */
    private Predicate<LichSuNopTien> createSearchPredicate(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return lichSu -> true;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        return lichSu -> {
            if (String.valueOf(lichSu.getMaPhieu()).contains(lowerKeyword)) {
                return true;
            }
            if (lichSu.getPhuongThuc() != null && lichSu.getPhuongThuc().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (lichSu.getNguoiThuName() != null && lichSu.getNguoiThuName().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            return false;
        };
    }

    /**
     * Nạp dữ liệu từ service vào TableView.
     */
    private void loadData() {
        try {
            lichSuData.setAll(paymentService.getAllLichSuNopTien());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải lịch sử nộp tiền: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện ghi nhận thanh toán.
     * Logic theo Activity Diagram "Ghi nhận thanh toán".
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleRecordPayment(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            // Parse mã phiếu
            int maPhieu;
            try {
                maPhieu = Integer.parseInt(tfMaPhieu.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Mã phiếu phải là số.");
                return;
            }

            // Kiểm tra receipt tồn tại
            PhieuThu phieuThu = receiptService.getPhieuThuWithDetails(maPhieu);
            if (phieuThu == null) {
                showAlert(AlertType.ERROR, "Không tìm thấy phiếu thu",
                        "Không tìm thấy phiếu thu với mã: " + maPhieu);
                return;
            }

            // Parse số tiền
            BigDecimal soTien;
            try {
                soTien = new BigDecimal(tfSoTien.getText().trim());
                if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số tiền phải lớn hơn 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số tiền phải là số.");
                return;
            }

            String phuongThuc = cbPhuongThuc.getValue();
            String nguoiThu = tfNguoiThu.getText().trim();

            // Confirmation Dialog
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận thanh toán");
            confirmAlert.setHeaderText("Xác nhận thanh toán cho hóa đơn?");
            confirmAlert.setContentText("Mã phiếu: " + maPhieu + "\n" +
                    "Số tiền: " + soTien + " VNĐ\n" +
                    "Phương thức: " + phuongThuc + "\n" +
                    "Người thu: " + nguoiThu);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Tạo bản ghi lịch sử nộp tiền
                LichSuNopTien paymentRecord = new LichSuNopTien();
                paymentRecord.setMaPhieu(maPhieu);
                paymentRecord.setNgayNop(LocalDateTime.now());
                paymentRecord.setSoTien(soTien);
                paymentRecord.setPhuongThuc(phuongThuc);
                paymentRecord.setNguoiThu(0); // Placeholder - có thể lấy từ session
                paymentRecord.setNguoiThuName(nguoiThu); // Set tên người thu cho hiển thị

                // Ghi nhận thanh toán
                boolean paymentSuccess = paymentService.addLichSuNopTien(paymentRecord);
                if (!paymentSuccess) {
                    showAlert(AlertType.ERROR, "Thất bại", "Không thể ghi nhận thanh toán. Vui lòng thử lại.");
                    return;
                }

                // Cập nhật trạng thái phiếu thu thành "Đã thu" (Crucial step from diagram)
                boolean updateSuccess = receiptService.updatePhieuThuStatus(maPhieu, "Đã thu");
                if (!updateSuccess) {
                    showAlert(AlertType.WARNING, "Cảnh báo",
                            "Đã ghi nhận thanh toán nhưng không thể cập nhật trạng thái phiếu thu.");
                }

                showAlert(AlertType.INFORMATION, "Thành công",
                        "Đã ghi nhận thanh toán thành công cho phiếu thu #" + maPhieu);
                clearForm();
                loadData();
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi ghi nhận thanh toán: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa form và bỏ chọn dòng trong bảng.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    /**
     * Xác thực dữ liệu nhập vào form.
     *
     * @return true nếu hợp lệ, false nếu không
     */
    private boolean validateInputs() {
        String maPhieuStr = tfMaPhieu.getText().trim();
        if (maPhieuStr.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Mã phiếu không được để trống.");
            return false;
        }

        String soTienStr = tfSoTien.getText().trim();
        if (soTienStr.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Số tiền không được để trống.");
            return false;
        }

        if (cbPhuongThuc.getValue() == null || cbPhuongThuc.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn phương thức thanh toán.");
            return false;
        }

        String nguoiThu = tfNguoiThu.getText().trim();
        if (nguoiThu.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Tên người thu không được để trống.");
            return false;
        }

        return true;
    }

    /**
     * Xóa tất cả dữ liệu trong form và bỏ chọn dòng trong bảng.
     */
    private void clearForm() {
        tfMaPhieu.clear();
        tfSoTien.clear();
        tfNguoiThu.clear();
        cbPhuongThuc.setValue(null);
        tableLichSu.getSelectionModel().clearSelection();
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
