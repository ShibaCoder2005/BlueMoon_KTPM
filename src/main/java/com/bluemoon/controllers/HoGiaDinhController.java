package com.bluemoon.controllers;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.NhanKhauService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.impl.HoGiaDinhServiceImpl;
import com.bluemoon.services.impl.NhanKhauServiceImpl;
import com.bluemoon.services.impl.PhieuThuServiceImpl;

/**
 * Controller quản lý dữ liệu hộ gia đình.
 * Xử lý CRUD operations, validation và kiểm tra nợ phí trước khi xóa.
 */
public class HoGiaDinhController implements Initializable {

    @FXML
    private TableView<HoGiaDinh> tableHoGiaDinh;

    @FXML
    private TableColumn<HoGiaDinh, String> colMaHo;

    @FXML
    private TableColumn<HoGiaDinh, Integer> colSoPhong;

    @FXML
    private TableColumn<HoGiaDinh, BigDecimal> colDienTich;

    @FXML
    private TableColumn<HoGiaDinh, String> colTenChuHo;

    @FXML
    private TextField tfMaHo;

    @FXML
    private TextField tfSoPhong;

    @FXML
    private TextField tfDienTich;

    @FXML
    private TextArea taGhiChu;

    @FXML
    private TextField tfSearch;

    @FXML
    private ComboBox<NhanKhau> cbChuHo;

    private final HoGiaDinhService hoGiaDinhService = new HoGiaDinhServiceImpl();
    private final NhanKhauService nhanKhauService = new NhanKhauServiceImpl();
    private final PhieuThuService phieuThuService = new PhieuThuServiceImpl();
    private final ObservableList<HoGiaDinh> hoGiaDinhData = FXCollections.observableArrayList();
    private final ObservableList<NhanKhau> nhanKhauList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        loadNhanKhauList();
        configureComboBox();
        setupSelectionListener();
        loadData();
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong TableView.
     */
    private void configureColumns() {
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colSoPhong.setCellValueFactory(new PropertyValueFactory<>("soPhong"));
        colDienTich.setCellValueFactory(new PropertyValueFactory<>("dienTich"));
        colTenChuHo.setCellValueFactory(new PropertyValueFactory<>("tenChuHo"));
        tableHoGiaDinh.setItems(hoGiaDinhData);
    }

    /**
     * Nạp danh sách nhân khẩu để chọn chủ hộ.
     */
    private void loadNhanKhauList() {
        try {
            nhanKhauList.setAll(nhanKhauService.getAll());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách nhân khẩu: " + e.getMessage());
        }
    }

    /**
     * Cấu hình ComboBox chủ hộ với StringConverter để hiển thị tên.
     */
    private void configureComboBox() {
        cbChuHo.setItems(nhanKhauList);
        cbChuHo.setConverter(new StringConverter<NhanKhau>() {
            @Override
            public String toString(NhanKhau nhanKhau) {
                return nhanKhau == null ? "" : nhanKhau.getHoTen();
            }

            @Override
            public NhanKhau fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
    }

    /**
     * Thiết lập listener cho TableView selection để tự động điền form khi chọn dòng.
     */
    private void setupSelectionListener() {
        tableHoGiaDinh.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateForm(newValue);
                    }
                });
    }

    /**
     * Nạp dữ liệu từ service vào TableView và resolve tên chủ hộ.
     */
    private void loadData() {
        try {
            hoGiaDinhData.setAll(hoGiaDinhService.getAll());
            // Resolve tên chủ hộ cho mỗi hộ gia đình
            for (HoGiaDinh hoGiaDinh : hoGiaDinhData) {
                resolveTenChuHo(hoGiaDinh);
            }
            // Refresh table để hiển thị tên chủ hộ
            tableHoGiaDinh.refresh();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách hộ gia đình: " + e.getMessage());
        }
    }

    /**
     * Resolve tên chủ hộ từ maChuHo.
     *
     * @param hoGiaDinh hộ gia đình cần resolve
     */
    private void resolveTenChuHo(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh.getMaChuHo() > 0) {
            NhanKhau chuHo = nhanKhauService.findById(hoGiaDinh.getMaChuHo());
            if (chuHo != null) {
                hoGiaDinh.setTenChuHo(chuHo.getHoTen());
            } else {
                hoGiaDinh.setTenChuHo("Không xác định");
            }
        } else {
            hoGiaDinh.setTenChuHo("Chưa có");
        }
    }

    /**
     * Xử lý sự kiện thêm mới hộ gia đình.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            HoGiaDinh newHoGiaDinh = buildHoGiaDinhFromForm();
            if (newHoGiaDinh == null) {
                return;
            }

            // Set ngày tạo
            newHoGiaDinh.setNgayTao(LocalDate.now());

            boolean success = hoGiaDinhService.add(newHoGiaDinh);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm hộ gia đình mới.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại",
                        "Không thể thêm hộ. Mã hộ có thể đã tồn tại hoặc dữ liệu không hợp lệ.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi thêm hộ gia đình: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện cập nhật hộ gia đình.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            HoGiaDinh selected = tableHoGiaDinh.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một hộ gia đình để cập nhật.");
                return;
            }

            if (!validateInputs()) {
                return;
            }

            HoGiaDinh updatedHoGiaDinh = buildHoGiaDinhFromForm();
            if (updatedHoGiaDinh == null) {
                return;
            }

            // Giữ nguyên ID và ngày tạo của hộ được chọn
            updatedHoGiaDinh.setId(selected.getId());
            updatedHoGiaDinh.setNgayTao(selected.getNgayTao());

            boolean success = hoGiaDinhService.update(updatedHoGiaDinh);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin hộ gia đình.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại",
                        "Không thể cập nhật. Mã hộ có thể đã tồn tại hoặc dữ liệu không hợp lệ.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi cập nhật hộ gia đình: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa hộ gia đình.
     * Kiểm tra nợ phí trước khi xóa theo Activity Diagram.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            HoGiaDinh selected = tableHoGiaDinh.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một hộ gia đình để xóa.");
                return;
            }

            // Kiểm tra nợ phí (theo Activity Diagram)
            boolean hasUnpaidFees = phieuThuService.hasUnpaidFees(selected.getId());
            if (hasUnpaidFees) {
                showAlert(AlertType.ERROR, "Không thể xóa",
                        "Hộ gia đình này còn nợ phí chưa thanh toán. Vui lòng thanh toán trước khi xóa.");
                return;
            }

            // Nếu không có nợ, hiển thị confirmation
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Bạn có chắc muốn xóa hộ gia đình này?");
            confirmAlert.setContentText("Mã hộ: " + selected.getMaHo() + "\n" +
                    "Hành động này không thể hoàn tác.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = hoGiaDinhService.delete(selected.getId());
                if (success) {
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa hộ gia đình.");
                    clearForm();
                    loadData();
                } else {
                    showAlert(AlertType.ERROR, "Thất bại", "Không thể xóa hộ. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xóa hộ gia đình: " + e.getMessage());
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
        String maHo = tfMaHo.getText().trim();
        if (maHo.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Mã hộ không được để trống.");
            return false;
        }

        String soPhongStr = tfSoPhong.getText().trim();
        if (soPhongStr.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Số phòng không được để trống.");
            return false;
        }
        try {
            int soPhong = Integer.parseInt(soPhongStr);
            if (soPhong <= 0) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số phòng phải lớn hơn 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Số phòng phải là số nguyên.");
            return false;
        }

        String dienTichStr = tfDienTich.getText().trim();
        if (dienTichStr.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Diện tích không được để trống.");
            return false;
        }
        try {
            BigDecimal dienTich = new BigDecimal(dienTichStr);
            if (dienTich.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Diện tích phải lớn hơn 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Diện tích phải là số.");
            return false;
        }

        if (cbChuHo.getValue() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn chủ hộ.");
            return false;
        }

        return true;
    }

    /**
     * Tạo đối tượng HoGiaDinh từ dữ liệu form.
     *
     * @return đối tượng HoGiaDinh hoặc null nếu có lỗi
     */
    private HoGiaDinh buildHoGiaDinhFromForm() {
        if (!validateInputs()) {
            return null;
        }

        String maHo = tfMaHo.getText().trim();
        int soPhong = Integer.parseInt(tfSoPhong.getText().trim());
        BigDecimal dienTich = new BigDecimal(tfDienTich.getText().trim());
        NhanKhau chuHo = cbChuHo.getValue();
        String ghiChu = taGhiChu.getText().trim();

        return new HoGiaDinh(0, maHo, soPhong, dienTich, chuHo.getId(), ghiChu.isEmpty() ? null : ghiChu, null);
    }

    /**
     * Điền dữ liệu từ HoGiaDinh vào form.
     *
     * @param hoGiaDinh đối tượng HoGiaDinh cần hiển thị
     */
    private void populateForm(HoGiaDinh hoGiaDinh) {
        if (hoGiaDinh == null) {
            return;
        }
        tfMaHo.setText(hoGiaDinh.getMaHo());
        tfSoPhong.setText(String.valueOf(hoGiaDinh.getSoPhong()));
        tfDienTich.setText(hoGiaDinh.getDienTich() != null ? hoGiaDinh.getDienTich().toString() : "");
        taGhiChu.setText(hoGiaDinh.getGhiChu() != null ? hoGiaDinh.getGhiChu() : "");

        // Tìm và chọn chủ hộ trong ComboBox
        NhanKhau chuHo = nhanKhauService.findById(hoGiaDinh.getMaChuHo());
        if (chuHo != null) {
            cbChuHo.setValue(chuHo);
        } else {
            cbChuHo.setValue(null);
        }
    }

    /**
     * Xóa tất cả dữ liệu trong form và bỏ chọn dòng trong bảng.
     */
    private void clearForm() {
        tfMaHo.clear();
        tfSoPhong.clear();
        tfDienTich.clear();
        taGhiChu.clear();
        cbChuHo.setValue(null);
        tableHoGiaDinh.getSelectionModel().clearSelection();
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
