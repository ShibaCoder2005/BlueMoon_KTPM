package com.bluemoon.controllers;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.beans.property.SimpleStringProperty;

import com.bluemoon.models.KhoanThu;
import com.bluemoon.services.KhoanThuService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.impl.KhoanThuServiceImpl;
import com.bluemoon.services.impl.PhieuThuServiceImpl;

/**
 * Controller quản lý danh mục KhoanThu.
 * Xử lý CRUD operations, validation và kiểm tra constraint trước khi xóa.
 */
public class KhoanThuController implements Initializable {

    @FXML
    private TableView<KhoanThu> tableKhoanThu;

    @FXML
    private TableColumn<KhoanThu, Integer> colMaKhoanThu;

    @FXML
    private TableColumn<KhoanThu, String> colTenKhoanThu;

    @FXML
    private TableColumn<KhoanThu, BigDecimal> colSoTien;

    @FXML
    private TableColumn<KhoanThu, String> colLoaiKhoanThu;

    @FXML
    private TableColumn<KhoanThu, String> colDonViTinh;

    @FXML
    private TextField tfTenKhoanThu;

    @FXML
    private TextField tfSoTien;

    @FXML
    private TextField tfDonViTinh;

    @FXML
    private ComboBox<String> cbLoaiKhoanThu;

    @FXML
    private TextField tfSearch;

    private final KhoanThuService khoanThuService = new KhoanThuServiceImpl();
    private final PhieuThuService phieuThuService = new PhieuThuServiceImpl();
    private final ObservableList<KhoanThu> khoanThuData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        initializeComboBox();
        setupSelectionListener();
        loadData();
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong TableView.
     */
    private void configureColumns() {
        colMaKhoanThu.setCellValueFactory(new PropertyValueFactory<>("maKhoanThu"));
        colTenKhoanThu.setCellValueFactory(new PropertyValueFactory<>("tenKhoanThu"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colDonViTinh.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));

        // Custom CellFactory để convert int loaiKhoanThu (0/1) thành String ("Bắt buộc"/"Đóng góp")
        colLoaiKhoanThu.setCellValueFactory(cellData -> {
            KhoanThu khoanThu = cellData.getValue();
            if (khoanThu == null) {
                return new SimpleStringProperty("");
            }
            int loai = khoanThu.getLoaiKhoanThu();
            String loaiStr = (loai == 0) ? "Bắt buộc" : "Đóng góp";
            return new SimpleStringProperty(loaiStr);
        });

        tableKhoanThu.setItems(khoanThuData);
    }

    /**
     * Khởi tạo ComboBox loại khoản thu với các giá trị mặc định.
     */
    private void initializeComboBox() {
        ObservableList<String> loaiList = FXCollections.observableArrayList(
                "Bắt buộc",
                "Đóng góp"
        );
        cbLoaiKhoanThu.setItems(loaiList);
    }

    /**
     * Thiết lập listener cho TableView selection để tự động điền form khi chọn dòng.
     */
    private void setupSelectionListener() {
        tableKhoanThu.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateForm(newValue);
                    }
                });
    }

    /**
     * Nạp dữ liệu từ service vào TableView.
     */
    private void loadData() {
        try {
            khoanThuData.setAll(khoanThuService.getAllKhoanThu());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách khoản thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện thêm mới khoản thu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            KhoanThu newKhoanThu = buildKhoanThuFromForm();
            if (newKhoanThu == null) {
                return;
            }

            boolean success = khoanThuService.addKhoanThu(newKhoanThu);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm khoản thu mới.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại", "Không thể thêm khoản thu. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi thêm khoản thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện cập nhật khoản thu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            KhoanThu selected = tableKhoanThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một khoản thu để cập nhật.");
                return;
            }

            if (!validateInputs()) {
                return;
            }

            KhoanThu updatedKhoanThu = buildKhoanThuFromForm();
            if (updatedKhoanThu == null) {
                return;
            }

            // Giữ nguyên ID của khoản thu được chọn
            updatedKhoanThu.setId(selected.getId());

            boolean success = khoanThuService.updateKhoanThu(updatedKhoanThu);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật khoản thu.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại", "Không thể cập nhật khoản thu. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi cập nhật khoản thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa khoản thu.
     * Kiểm tra constraint (phí đã sử dụng) trước khi xóa theo Activity Diagram.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            KhoanThu selected = tableKhoanThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một khoản thu để xóa.");
                return;
            }

            // Kiểm tra constraint: Phí đã sử dụng? (theo Activity Diagram)
            boolean isUsed = phieuThuService.isFeeUsed(selected.getMaKhoanThu());
            if (isUsed) {
                showAlert(AlertType.ERROR, "Không thể xóa",
                        "Không thể xóa, phí đã được sử dụng trong các phiếu thu. Vui lòng xóa các chi tiết thu liên quan trước.");
                return;
            }

            // Nếu không được sử dụng, hiển thị confirmation
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Bạn có chắc muốn xóa khoản thu này?");
            confirmAlert.setContentText("Khoản thu: " + selected.getTenKhoanThu() + "\n" +
                    "Hành động này không thể hoàn tác.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = khoanThuService.deleteKhoanThu(selected.getMaKhoanThu());
                if (success) {
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa khoản thu.");
                    clearForm();
                    loadData();
                } else {
                    showAlert(AlertType.ERROR, "Thất bại", "Không thể xóa khoản thu. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xóa khoản thu: " + e.getMessage());
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
        String tenKhoanThu = tfTenKhoanThu.getText().trim();
        if (tenKhoanThu.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Tên khoản thu không được để trống.");
            return false;
        }

        String soTienStr = tfSoTien.getText().trim();
        if (soTienStr.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Số tiền không được để trống.");
            return false;
        }
        try {
            BigDecimal soTien = new BigDecimal(soTienStr);
            if (soTien.compareTo(BigDecimal.ZERO) < 0) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số tiền phải >= 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số tiền phải là số.");
            return false;
        }

        if (cbLoaiKhoanThu.getValue() == null || cbLoaiKhoanThu.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn loại khoản thu.");
            return false;
        }

        return true;
    }

    /**
     * Tạo đối tượng KhoanThu từ dữ liệu form.
     *
     * @return đối tượng KhoanThu hoặc null nếu có lỗi
     */
    private KhoanThu buildKhoanThuFromForm() {
        if (!validateInputs()) {
            return null;
        }

        String tenKhoanThu = tfTenKhoanThu.getText().trim();
        BigDecimal soTien = new BigDecimal(tfSoTien.getText().trim());
        String donViTinh = tfDonViTinh.getText().trim();
        String loaiStr = cbLoaiKhoanThu.getValue();

        // Map "Bắt buộc" -> 0, "Đóng góp" -> 1
        int loaiKhoanThu = loaiStr.equals("Bắt buộc") ? 0 : 1;

        KhoanThu khoanThu = new KhoanThu();
        khoanThu.setTenKhoanThu(tenKhoanThu);
        khoanThu.setSoTien(soTien);
        khoanThu.setDonViTinh(donViTinh.isEmpty() ? null : donViTinh);
        khoanThu.setLoaiKhoanThu(loaiKhoanThu);
        // Set các field khác để tương thích với model hiện tại
        khoanThu.setLoai(loaiStr);
        khoanThu.setBatBuoc(loaiKhoanThu == 0);

        return khoanThu;
    }

    /**
     * Điền dữ liệu từ KhoanThu vào form.
     *
     * @param khoanThu đối tượng KhoanThu cần hiển thị
     */
    private void populateForm(KhoanThu khoanThu) {
        if (khoanThu == null) {
            return;
        }
        tfTenKhoanThu.setText(khoanThu.getTenKhoanThu());
        tfSoTien.setText(khoanThu.getSoTien() != null ? khoanThu.getSoTien().toString() : "");
        tfDonViTinh.setText(khoanThu.getDonViTinh() != null ? khoanThu.getDonViTinh() : "");

        // Convert int loaiKhoanThu (0/1) thành String cho ComboBox
        int loai = khoanThu.getLoaiKhoanThu();
        String loaiStr = (loai == 0) ? "Bắt buộc" : "Đóng góp";
        cbLoaiKhoanThu.setValue(loaiStr);
    }

    /**
     * Xóa tất cả dữ liệu trong form và bỏ chọn dòng trong bảng.
     */
    private void clearForm() {
        tfTenKhoanThu.clear();
        tfSoTien.clear();
        tfDonViTinh.clear();
        cbLoaiKhoanThu.setValue(null);
        tableKhoanThu.getSelectionModel().clearSelection();
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
