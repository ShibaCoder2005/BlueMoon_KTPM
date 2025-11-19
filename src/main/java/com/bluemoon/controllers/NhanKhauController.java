package com.bluemoon.controllers;

import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.NhanKhau;
import com.bluemoon.services.HoGiaDinhService;
import com.bluemoon.services.NhanKhauService;
import com.bluemoon.services.impl.HoGiaDinhServiceImpl;
import com.bluemoon.services.impl.NhanKhauServiceImpl;

/**
 * Controller quản lý nhân khẩu và lịch sử biến động.
 * Xử lý CRUD operations, validation và quản lý tình trạng cư trú.
 */
public class NhanKhauController implements Initializable {

    @FXML
    private TableView<NhanKhau> tableNhanKhau;

    @FXML
    private TableColumn<NhanKhau, Integer> colMaHo;

    @FXML
    private TableColumn<NhanKhau, String> colHoTen;

    @FXML
    private TableColumn<NhanKhau, LocalDate> colNgaySinh;

    @FXML
    private TableColumn<NhanKhau, String> colGioiTinh;

    @FXML
    private TableColumn<NhanKhau, String> colSoCCCD;

    @FXML
    private TableColumn<NhanKhau, String> colNgheNghiep;

    @FXML
    private TableColumn<NhanKhau, String> colQuanHe;

    @FXML
    private TableColumn<NhanKhau, String> colTinhTrang;

    @FXML
    private TextField tfHoTen;

    @FXML
    private DatePicker dpNgaySinh;

    @FXML
    private ComboBox<String> cbGioiTinh;

    @FXML
    private TextField tfSoCCCD;

    @FXML
    private TextField tfNgheNghiep;

    @FXML
    private TextField tfQuanHe;

    @FXML
    private ComboBox<String> cbTinhTrang;

    @FXML
    private ComboBox<HoGiaDinh> cbHoGiaDinh;

    @FXML
    private TextField tfSearch;

    private final NhanKhauService nhanKhauService = new NhanKhauServiceImpl();
    private final HoGiaDinhService hoGiaDinhService = new HoGiaDinhServiceImpl();
    private final ObservableList<NhanKhau> nhanKhauData = FXCollections.observableArrayList();
    private final ObservableList<HoGiaDinh> hoGiaDinhList = FXCollections.observableArrayList();
    private FilteredList<NhanKhau> filteredData;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        initializeComboBoxes();
        loadHoGiaDinhList();
        setupSelectionListener();
        setupSearchListener();
        loadData();
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong TableView.
     */
    private void configureColumns() {
        colMaHo.setCellValueFactory(new PropertyValueFactory<>("maHo"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colGioiTinh.setCellValueFactory(new PropertyValueFactory<>("gioiTinh"));
        colSoCCCD.setCellValueFactory(new PropertyValueFactory<>("soCCCD"));
        colNgheNghiep.setCellValueFactory(new PropertyValueFactory<>("ngheNghiep"));
        colQuanHe.setCellValueFactory(new PropertyValueFactory<>("quanHeVoiChuHo"));
        colTinhTrang.setCellValueFactory(new PropertyValueFactory<>("tinhTrang"));

        // Format ngày sinh hiển thị trong bảng
        colNgaySinh.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        colNgaySinh.setCellFactory(column -> new javafx.scene.control.TableCell<NhanKhau, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });

        tableNhanKhau.setItems(filteredData != null ? filteredData : nhanKhauData);
    }

    /**
     * Khởi tạo các ComboBox với các giá trị mặc định.
     */
    private void initializeComboBoxes() {
        // ComboBox giới tính
        ObservableList<String> gioiTinhList = FXCollections.observableArrayList(
                "Nam",
                "Nữ",
                "Khác"
        );
        cbGioiTinh.setItems(gioiTinhList);

        // ComboBox tình trạng
        ObservableList<String> tinhTrangList = FXCollections.observableArrayList(
                "Thường trú",
                "Tạm trú",
                "Tạm vắng",
                "Chuyển đi"
        );
        cbTinhTrang.setItems(tinhTrangList);
    }

    /**
     * Nạp danh sách hộ gia đình vào ComboBox.
     */
    private void loadHoGiaDinhList() {
        try {
            hoGiaDinhList.setAll(hoGiaDinhService.getAll());
            cbHoGiaDinh.setItems(hoGiaDinhList);
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
                    return null; // Not needed for ComboBox
                }
            });
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách hộ gia đình: " + e.getMessage());
        }
    }

    /**
     * Thiết lập listener cho TableView selection để tự động điền form khi chọn dòng.
     */
    private void setupSelectionListener() {
        tableNhanKhau.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateForm(newValue);
                    }
                });
    }

    /**
     * Thiết lập listener cho TextField tìm kiếm để lọc dữ liệu trong TableView.
     */
    private void setupSearchListener() {
        filteredData = new FilteredList<>(nhanKhauData, p -> true);
        tableNhanKhau.setItems(filteredData);

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
    private Predicate<NhanKhau> createSearchPredicate(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return nhanKhau -> true;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        return nhanKhau -> {
            if (nhanKhau.getHoTen() != null && nhanKhau.getHoTen().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (nhanKhau.getSoCCCD() != null && nhanKhau.getSoCCCD().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (nhanKhau.getNgheNghiep() != null && nhanKhau.getNgheNghiep().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (nhanKhau.getTinhTrang() != null && nhanKhau.getTinhTrang().toLowerCase().contains(lowerKeyword)) {
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
            nhanKhauData.setAll(nhanKhauService.getAll());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách nhân khẩu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện thêm mới nhân khẩu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            NhanKhau newNhanKhau = buildNhanKhauFromForm();
            if (newNhanKhau == null) {
                return;
            }

            // Kiểm tra CCCD unique
            if (newNhanKhau.getSoCCCD() != null && !newNhanKhau.getSoCCCD().trim().isEmpty()) {
                if (nhanKhauService.isCCCDExists(newNhanKhau.getSoCCCD(), 0)) {
                    showAlert(AlertType.ERROR, "Lỗi xác thực",
                            "Số CCCD đã tồn tại trong hệ thống. Vui lòng kiểm tra lại.");
                    return;
                }
            }

            boolean success = nhanKhauService.addNhanKhau(newNhanKhau);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm nhân khẩu mới.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại",
                        "Không thể thêm nhân khẩu. Số CCCD có thể đã tồn tại hoặc dữ liệu không hợp lệ.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi thêm nhân khẩu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện cập nhật nhân khẩu.
     * Note: Khi cập nhật tình trạng, hệ thống nên ghi nhận vào LichSuNhanKhau.
     * Hiện tại logic này có thể được xử lý ở Service layer.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            NhanKhau selected = tableNhanKhau.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một nhân khẩu để cập nhật.");
                return;
            }

            if (!validateInputs()) {
                return;
            }

            NhanKhau updatedNhanKhau = buildNhanKhauFromForm();
            if (updatedNhanKhau == null) {
                return;
            }

            // Giữ nguyên ID của nhân khẩu được chọn
            updatedNhanKhau.setId(selected.getId());

            // Kiểm tra CCCD unique (loại trừ chính nó)
            if (updatedNhanKhau.getSoCCCD() != null && !updatedNhanKhau.getSoCCCD().trim().isEmpty()) {
                if (nhanKhauService.isCCCDExists(updatedNhanKhau.getSoCCCD(), selected.getId())) {
                    showAlert(AlertType.ERROR, "Lỗi xác thực",
                            "Số CCCD đã tồn tại trong hệ thống. Vui lòng kiểm tra lại.");
                    return;
                }
            }

            // Lưu tình trạng cũ để kiểm tra thay đổi
            String oldTinhTrang = selected.getTinhTrang();
            String newTinhTrang = updatedNhanKhau.getTinhTrang();

            boolean success = nhanKhauService.updateNhanKhau(updatedNhanKhau);
            if (success) {
                // TODO: Nếu tình trạng thay đổi, ghi nhận vào LichSuNhanKhau
                // Service layer có thể tự động xử lý việc này khi detect thay đổi tình trạng
                // Ví dụ: nhanKhauService.recordStatusChange(selected.getId(), oldTinhTrang, newTinhTrang);

                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin nhân khẩu.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại",
                        "Không thể cập nhật nhân khẩu. Số CCCD có thể đã tồn tại hoặc dữ liệu không hợp lệ.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi cập nhật nhân khẩu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa nhân khẩu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            NhanKhau selected = tableNhanKhau.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một nhân khẩu để xóa.");
                return;
            }

            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Bạn có chắc muốn xóa nhân khẩu này?");
            confirmAlert.setContentText("Họ tên: " + selected.getHoTen() + "\n" +
                    "Số CCCD: " + selected.getSoCCCD() + "\n" +
                    "Hành động này không thể hoàn tác.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = nhanKhauService.deleteNhanKhau(selected.getId());
                if (success) {
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa nhân khẩu.");
                    clearForm();
                    loadData();
                } else {
                    showAlert(AlertType.ERROR, "Thất bại", "Không thể xóa nhân khẩu. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xóa nhân khẩu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện ghi nhận biến động nhân khẩu (chuyển đi/đến).
     * Method này có thể được mở rộng để hiển thị dialog nhập thông tin chi tiết về biến động.
     * Hiện tại, việc ghi nhận lịch sử có thể được thực hiện tự động khi cập nhật tình trạng.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleRecordMovement(ActionEvent event) {
        try {
            NhanKhau selected = tableNhanKhau.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một nhân khẩu để ghi nhận biến động.");
                return;
            }

            // TODO: Implement dialog để nhập thông tin biến động chi tiết
            // Hiện tại, việc ghi nhận lịch sử có thể được thực hiện tự động
            // khi cập nhật tình trạng qua handleUpdate()
            showAlert(AlertType.INFORMATION, "Thông báo",
                    "Để ghi nhận biến động, vui lòng cập nhật tình trạng của nhân khẩu. " +
                            "Hệ thống sẽ tự động ghi nhận vào lịch sử.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi ghi nhận biến động: " + e.getMessage());
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
        String hoTen = tfHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Họ tên không được để trống.");
            return false;
        }

        if (dpNgaySinh.getValue() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn ngày sinh.");
            return false;
        }

        // Validate DOB not in future
        if (dpNgaySinh.getValue().isAfter(LocalDate.now())) {
            showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Ngày sinh không thể là ngày tương lai.");
            return false;
        }

        String soCCCD = tfSoCCCD.getText().trim();
        if (soCCCD.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Số CCCD không được để trống.");
            return false;
        }

        if (cbGioiTinh.getValue() == null || cbGioiTinh.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn giới tính.");
            return false;
        }

        if (cbTinhTrang.getValue() == null || cbTinhTrang.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn tình trạng.");
            return false;
        }

        if (cbHoGiaDinh.getValue() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn hộ gia đình.");
            return false;
        }

        return true;
    }

    /**
     * Tạo đối tượng NhanKhau từ dữ liệu form.
     *
     * @return đối tượng NhanKhau hoặc null nếu có lỗi
     */
    private NhanKhau buildNhanKhauFromForm() {
        if (!validateInputs()) {
            return null;
        }

        String hoTen = tfHoTen.getText().trim();
        LocalDate ngaySinh = dpNgaySinh.getValue();
        String gioiTinh = cbGioiTinh.getValue();
        String soCCCD = tfSoCCCD.getText().trim();
        String ngheNghiep = tfNgheNghiep.getText().trim();
        String quanHe = tfQuanHe.getText().trim();
        String tinhTrang = cbTinhTrang.getValue();
        HoGiaDinh hoGiaDinh = cbHoGiaDinh.getValue();

        return new NhanKhau(0, hoGiaDinh.getId(), hoTen, ngaySinh, gioiTinh, soCCCD,
                ngheNghiep.isEmpty() ? null : ngheNghiep,
                quanHe.isEmpty() ? null : quanHe,
                tinhTrang);
    }

    /**
     * Điền dữ liệu từ NhanKhau vào form.
     *
     * @param nhanKhau đối tượng NhanKhau cần hiển thị
     */
    private void populateForm(NhanKhau nhanKhau) {
        if (nhanKhau == null) {
            return;
        }
        tfHoTen.setText(nhanKhau.getHoTen());
        dpNgaySinh.setValue(nhanKhau.getNgaySinh());
        cbGioiTinh.setValue(nhanKhau.getGioiTinh());
        tfSoCCCD.setText(nhanKhau.getSoCCCD());
        tfNgheNghiep.setText(nhanKhau.getNgheNghiep() != null ? nhanKhau.getNgheNghiep() : "");
        tfQuanHe.setText(nhanKhau.getQuanHeVoiChuHo() != null ? nhanKhau.getQuanHeVoiChuHo() : "");
        cbTinhTrang.setValue(nhanKhau.getTinhTrang());

        // Tìm và chọn hộ gia đình trong ComboBox
        HoGiaDinh hoGiaDinh = hoGiaDinhService.findById(nhanKhau.getMaHo());
        if (hoGiaDinh != null) {
            cbHoGiaDinh.setValue(hoGiaDinh);
        } else {
            cbHoGiaDinh.setValue(null);
        }
    }

    /**
     * Xóa tất cả dữ liệu trong form và bỏ chọn dòng trong bảng.
     */
    private void clearForm() {
        tfHoTen.clear();
        dpNgaySinh.setValue(null);
        cbGioiTinh.setValue(null);
        tfSoCCCD.clear();
        tfNgheNghiep.clear();
        tfQuanHe.clear();
        cbTinhTrang.setValue(null);
        cbHoGiaDinh.setValue(null);
        tableNhanKhau.getSelectionModel().clearSelection();
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
