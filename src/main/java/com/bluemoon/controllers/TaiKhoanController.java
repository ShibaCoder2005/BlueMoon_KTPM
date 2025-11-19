package com.bluemoon.controllers;

import java.net.URL;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.TaiKhoanService;
import com.bluemoon.services.impl.TaiKhoanServiceImpl;
import com.bluemoon.utils.Helper;

/**
 * Controller quản lý tài khoản người dùng.
 * Xử lý CRUD operations, khóa/mở khóa tài khoản, và reset mật khẩu.
 */
public class TaiKhoanController implements Initializable {

    private final TaiKhoanService service = new TaiKhoanServiceImpl();
    private final ObservableList<TaiKhoan> taiKhoanData = FXCollections.observableArrayList();
    private FilteredList<TaiKhoan> filteredData;

    // --- FXML Components: Table ---
    @FXML
    private TableView<TaiKhoan> tableTaiKhoan;

    @FXML
    private TableColumn<TaiKhoan, Integer> colId;

    @FXML
    private TableColumn<TaiKhoan, String> colTenDangNhap;

    @FXML
    private TableColumn<TaiKhoan, String> colHoTen;

    @FXML
    private TableColumn<TaiKhoan, String> colVaiTro;

    @FXML
    private TableColumn<TaiKhoan, String> colTrangThai;

    // --- FXML Components: Inputs (Form) ---
    @FXML
    private TextField tfTenDangNhap;

    @FXML
    private TextField tfHoTen;

    @FXML
    private TextField tfDienThoai;

    @FXML
    private ComboBox<String> cbVaiTro;

    @FXML
    private PasswordField pfMatKhau;

    // --- FXML Components: Search ---
    @FXML
    private TextField tfSearch;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        initializeComboBox();
        setupSelectionListener();
        setupSearchListener();
        loadData();
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong TableView.
     */
    private void configureColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTenDangNhap.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colVaiTro.setCellValueFactory(new PropertyValueFactory<>("vaiTro"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        tableTaiKhoan.setItems(filteredData != null ? filteredData : taiKhoanData);
    }

    /**
     * Khởi tạo ComboBox vai trò với các giá trị mặc định.
     */
    private void initializeComboBox() {
        ObservableList<String> vaiTroList = FXCollections.observableArrayList(
                "BanQuanLy",
                "KeToan",
                "ToTruong"
        );
        cbVaiTro.setItems(vaiTroList);
    }

    /**
     * Thiết lập listener cho TableView selection để tự động điền form khi chọn dòng.
     */
    private void setupSelectionListener() {
        tableTaiKhoan.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateForm(newValue);
                        // Disable username field when editing
                        tfTenDangNhap.setDisable(true);
                    } else {
                        tfTenDangNhap.setDisable(false);
                    }
                });
    }

    /**
     * Thiết lập listener cho TextField tìm kiếm để lọc dữ liệu trong TableView.
     */
    private void setupSearchListener() {
        filteredData = new FilteredList<>(taiKhoanData, p -> true);
        tableTaiKhoan.setItems(filteredData);

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
    private Predicate<TaiKhoan> createSearchPredicate(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return taiKhoan -> true;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        return taiKhoan -> {
            if (taiKhoan.getTenDangNhap() != null && taiKhoan.getTenDangNhap().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (taiKhoan.getHoTen() != null && taiKhoan.getHoTen().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (taiKhoan.getVaiTro() != null && taiKhoan.getVaiTro().toLowerCase().contains(lowerKeyword)) {
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
            taiKhoanData.setAll(service.getAllTaiKhoan());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện thêm mới tài khoản.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            String tenDangNhap = tfTenDangNhap.getText().trim();
            String matKhau = pfMatKhau.getText();

            // Check if username already exists
            if (service.isUsernameExists(tenDangNhap, 0)) {
                showAlert(AlertType.ERROR, "Lỗi xác thực",
                        "Tên đăng nhập đã tồn tại. Vui lòng chọn tên đăng nhập khác.");
                return;
            }

            // Hash password
            String hashedPassword = Helper.hashPassword(matKhau);

            // Create TaiKhoan object
            TaiKhoan newTaiKhoan = buildTaiKhoanFromForm();
            if (newTaiKhoan == null) {
                return;
            }
            newTaiKhoan.setMatKhau(hashedPassword);
            newTaiKhoan.setTrangThai("Hoạt động"); // Default status

            boolean success = service.addTaiKhoan(newTaiKhoan);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm tài khoản mới.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại",
                        "Không thể thêm tài khoản. Tên đăng nhập có thể đã tồn tại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi thêm tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện cập nhật tài khoản.
     * Chỉ cập nhật thông tin (Name, Role, Phone), không cập nhật password.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            TaiKhoan selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một tài khoản để cập nhật.");
                return;
            }

            if (!validateInputsForUpdate()) {
                return;
            }

            // Check username uniqueness (exclude current account)
            String tenDangNhap = tfTenDangNhap.getText().trim();
            if (service.isUsernameExists(tenDangNhap, selected.getId())) {
                showAlert(AlertType.ERROR, "Lỗi xác thực",
                        "Tên đăng nhập đã tồn tại. Vui lòng chọn tên đăng nhập khác.");
                return;
            }

            // Update only info fields, keep password unchanged
            TaiKhoan updatedTaiKhoan = buildTaiKhoanFromForm();
            if (updatedTaiKhoan == null) {
                return;
            }
            updatedTaiKhoan.setId(selected.getId());
            updatedTaiKhoan.setMatKhau(selected.getMatKhau()); // Keep old password
            updatedTaiKhoan.setTrangThai(selected.getTrangThai()); // Keep current status

            boolean success = service.updateTaiKhoan(updatedTaiKhoan);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin tài khoản.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại",
                        "Không thể cập nhật tài khoản. Tên đăng nhập có thể đã tồn tại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi cập nhật tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện khóa/mở khóa tài khoản.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleLockUnlock(ActionEvent event) {
        try {
            TaiKhoan selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một tài khoản để khóa/mở khóa.");
                return;
            }

            String currentStatus = selected.getTrangThai();
            String newStatus;
            String actionText;
            String confirmText;

            if ("Hoạt động".equals(currentStatus)) {
                newStatus = "Bị khóa";
                actionText = "khóa";
                confirmText = "Bạn có chắc muốn khóa tài khoản này?";
            } else {
                newStatus = "Hoạt động";
                actionText = "mở khóa";
                confirmText = "Bạn có chắc muốn mở khóa tài khoản này?";
            }

            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận " + actionText);
            confirmAlert.setHeaderText(confirmText);
            confirmAlert.setContentText("Tài khoản: " + selected.getTenDangNhap() + "\n" +
                    "Họ tên: " + selected.getHoTen() + "\n" +
                    "Trạng thái hiện tại: " + currentStatus + " -> " + newStatus);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = service.updateStatus(selected.getId(), newStatus);
                if (success) {
                    showAlert(AlertType.INFORMATION, "Thành công",
                            "Đã " + actionText + " tài khoản thành công.");
                    loadData();
                } else {
                    showAlert(AlertType.ERROR, "Thất bại", "Không thể " + actionText + " tài khoản. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi khóa/mở khóa tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện reset mật khẩu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleResetPassword(ActionEvent event) {
        try {
            TaiKhoan selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một tài khoản để reset mật khẩu.");
                return;
            }

            // Show dialog to input new password
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Reset mật khẩu");
            passwordDialog.setHeaderText("Nhập mật khẩu mới cho tài khoản: " + selected.getTenDangNhap());
            passwordDialog.setContentText("Mật khẩu mới:");

            Optional<String> passwordResult = passwordDialog.showAndWait();
            if (!passwordResult.isPresent() || passwordResult.get().trim().isEmpty()) {
                return;
            }

            String newPassword = passwordResult.get().trim();

            // Show confirmation dialog
            TextInputDialog confirmDialog = new TextInputDialog();
            confirmDialog.setTitle("Xác nhận mật khẩu");
            confirmDialog.setHeaderText("Xác nhận mật khẩu mới");
            confirmDialog.setContentText("Nhập lại mật khẩu:");

            Optional<String> confirmResult = confirmDialog.showAndWait();
            if (!confirmResult.isPresent() || confirmResult.get().trim().isEmpty()) {
                return;
            }

            String confirmPassword = confirmResult.get().trim();

            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                showAlert(AlertType.ERROR, "Lỗi xác thực", "Mật khẩu xác nhận không khớp. Vui lòng thử lại.");
                return;
            }

            // Hash password
            String hashedPassword = Helper.hashPassword(newPassword);

            // Update password
            boolean success = service.updatePassword(selected.getId(), hashedPassword);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã reset mật khẩu thành công.");
            } else {
                showAlert(AlertType.ERROR, "Thất bại", "Không thể reset mật khẩu. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi reset mật khẩu: " + e.getMessage());
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
     * Xác thực dữ liệu nhập vào form (cho Add).
     *
     * @return true nếu hợp lệ, false nếu không
     */
    private boolean validateInputs() {
        String tenDangNhap = tfTenDangNhap.getText().trim();
        if (tenDangNhap.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Tên đăng nhập không được để trống.");
            return false;
        }

        String matKhau = pfMatKhau.getText();
        if (matKhau.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Mật khẩu không được để trống.");
            return false;
        }

        String hoTen = tfHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Họ tên không được để trống.");
            return false;
        }

        if (cbVaiTro.getValue() == null || cbVaiTro.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn vai trò.");
            return false;
        }

        return true;
    }

    /**
     * Xác thực dữ liệu nhập vào form (cho Update - không yêu cầu password).
     *
     * @return true nếu hợp lệ, false nếu không
     */
    private boolean validateInputsForUpdate() {
        String tenDangNhap = tfTenDangNhap.getText().trim();
        if (tenDangNhap.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Tên đăng nhập không được để trống.");
            return false;
        }

        String hoTen = tfHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Họ tên không được để trống.");
            return false;
        }

        if (cbVaiTro.getValue() == null || cbVaiTro.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn vai trò.");
            return false;
        }

        return true;
    }

    /**
     * Tạo đối tượng TaiKhoan từ dữ liệu form.
     *
     * @return đối tượng TaiKhoan hoặc null nếu có lỗi
     */
    private TaiKhoan buildTaiKhoanFromForm() {
        String tenDangNhap = tfTenDangNhap.getText().trim();
        String hoTen = tfHoTen.getText().trim();
        String dienThoai = tfDienThoai.getText().trim();
        String vaiTro = cbVaiTro.getValue();

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setHoTen(hoTen);
        taiKhoan.setDienThoai(dienThoai.isEmpty() ? null : dienThoai);
        taiKhoan.setVaiTro(vaiTro);

        return taiKhoan;
    }

    /**
     * Điền dữ liệu từ TaiKhoan vào form.
     *
     * @param taiKhoan đối tượng TaiKhoan cần hiển thị
     */
    private void populateForm(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            return;
        }
        tfTenDangNhap.setText(taiKhoan.getTenDangNhap());
        tfHoTen.setText(taiKhoan.getHoTen());
        tfDienThoai.setText(taiKhoan.getDienThoai() != null ? taiKhoan.getDienThoai() : "");
        cbVaiTro.setValue(taiKhoan.getVaiTro());
        pfMatKhau.clear(); // Don't show password
    }

    /**
     * Xóa tất cả dữ liệu trong form và bỏ chọn dòng trong bảng.
     */
    private void clearForm() {
        tfTenDangNhap.clear();
        tfHoTen.clear();
        tfDienThoai.clear();
        cbVaiTro.setValue(null);
        pfMatKhau.clear();
        tfTenDangNhap.setDisable(false); // Enable username field
        tableTaiKhoan.getSelectionModel().clearSelection();
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
