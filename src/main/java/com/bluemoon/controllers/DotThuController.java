package com.bluemoon.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.bluemoon.models.DotThu;
import com.bluemoon.services.DotThuService;
import com.bluemoon.services.PhieuThuService;
import com.bluemoon.services.impl.DotThuServiceImpl;
import com.bluemoon.services.impl.PhieuThuServiceImpl;

/**
 * Controller quản lý các đợt thu (DotThu).
 * Xử lý CRUD operations, validation và tạo hóa đơn hàng loạt.
 */
public class DotThuController implements Initializable {

    @FXML
    private TableView<DotThu> tableDotThu;

    @FXML
    private TableColumn<DotThu, Integer> colMaDot;

    @FXML
    private TableColumn<DotThu, String> colTenDot;

    @FXML
    private TableColumn<DotThu, LocalDate> colNgayBatDau;

    @FXML
    private TableColumn<DotThu, LocalDate> colNgayKetThuc;

    @FXML
    private TableColumn<DotThu, String> colTrangThai;

    @FXML
    private TextField tfTenDot;

    @FXML
    private DatePicker dpNgayBatDau;

    @FXML
    private DatePicker dpNgayKetThuc;

    @FXML
    private TextArea taMoTa;

    @FXML
    private TextField tfSearch;

    @FXML
    private ComboBox<String> cbTrangThai;

    private final DotThuService dotThuService = new DotThuServiceImpl();
    private final PhieuThuService phieuThuService = new PhieuThuServiceImpl();
    private final ObservableList<DotThu> dotThuData = FXCollections.observableArrayList();
    private FilteredList<DotThu> filteredData;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo controller và cấu hình UI components.
     *
     * @param location  đường dẫn FXML
     * @param resources resource bundle
     */
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
        colMaDot.setCellValueFactory(new PropertyValueFactory<>("maDot"));
        colTenDot.setCellValueFactory(new PropertyValueFactory<>("tenDot"));
        colNgayBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // Format ngày tháng hiển thị trong bảng
        colNgayBatDau.setCellFactory(column -> new javafx.scene.control.TableCell<DotThu, LocalDate>() {
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

        colNgayKetThuc.setCellFactory(column -> new javafx.scene.control.TableCell<DotThu, LocalDate>() {
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
    }

    /**
     * Khởi tạo ComboBox trạng thái với các giá trị mặc định.
     */
    private void initializeComboBox() {
        ObservableList<String> trangThaiList = FXCollections.observableArrayList(
                "Kế hoạch",
                "Đang thu",
                "Đóng",
                "Hoàn thành"
        );
        cbTrangThai.setItems(trangThaiList);
    }

    /**
     * Thiết lập listener cho TableView selection để tự động điền form khi chọn dòng.
     */
    private void setupSelectionListener() {
        tableDotThu.getSelectionModel().selectedItemProperty()
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
        filteredData = new FilteredList<>(dotThuData, p -> true);
        tableDotThu.setItems(filteredData);

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
    private Predicate<DotThu> createSearchPredicate(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dotThu -> true;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        return dotThu -> {
            if (dotThu.getTenDot() != null && dotThu.getTenDot().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (dotThu.getTrangThai() != null && dotThu.getTrangThai().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
            if (dotThu.getMoTa() != null && dotThu.getMoTa().toLowerCase().contains(lowerKeyword)) {
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
            dotThuData.setAll(dotThuService.getAllDotThu());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải danh sách đợt thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện thêm mới đợt thu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            DotThu newDotThu = buildDotThuFromForm();
            if (newDotThu == null) {
                return;
            }

            // Validate ngày kết thúc phải bằng hoặc sau ngày bắt đầu
            if (newDotThu.getNgayKetThuc().isBefore(newDotThu.getNgayBatDau())) {
                showAlert(AlertType.ERROR, "Lỗi xác thực",
                        "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu!");
                return;
            }

            boolean success = dotThuService.addDotThu(newDotThu);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm đợt thu mới.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại", "Không thể thêm đợt thu. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi thêm đợt thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện cập nhật đợt thu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            DotThu selected = tableDotThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một đợt thu để cập nhật.");
                return;
            }

            if (!validateInputs()) {
                return;
            }

            DotThu updatedDotThu = buildDotThuFromForm();
            if (updatedDotThu == null) {
                return;
            }

            // Validate ngày kết thúc phải bằng hoặc sau ngày bắt đầu
            if (updatedDotThu.getNgayKetThuc().isBefore(updatedDotThu.getNgayBatDau())) {
                showAlert(AlertType.ERROR, "Lỗi xác thực",
                        "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu!");
                return;
            }

            // Giữ nguyên ID của đợt thu được chọn
            updatedDotThu.setId(selected.getId());

            boolean success = dotThuService.updateDotThu(updatedDotThu);
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật đợt thu.");
                clearForm();
                loadData();
            } else {
                showAlert(AlertType.ERROR, "Thất bại", "Không thể cập nhật đợt thu. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi cập nhật đợt thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa đợt thu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            DotThu selected = tableDotThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một đợt thu để xóa.");
                return;
            }

            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setHeaderText("Bạn có chắc muốn xóa đợt thu này?");
            confirmAlert.setContentText("Đợt thu: " + selected.getTenDot() + "\n" +
                    "Hành động này không thể hoàn tác.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = dotThuService.deleteDotThu(selected.getMaDot());
                if (success) {
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa đợt thu.");
                    clearForm();
                    loadData();
                } else {
                    showAlert(AlertType.ERROR, "Thất bại", "Không thể xóa đợt thu. Vui lòng thử lại.");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xóa đợt thu: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện tạo hóa đơn hàng loạt cho đợt thu được chọn.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleGenerateReceipts(ActionEvent event) {
        try {
            DotThu selected = tableDotThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "Chưa chọn dữ liệu",
                        "Vui lòng chọn một đợt thu để tạo hóa đơn hàng loạt.");
                return;
            }

            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận tạo hóa đơn");
            confirmAlert.setHeaderText("Tạo hóa đơn hàng loạt?");
            confirmAlert.setContentText("Bạn có muốn tạo hóa đơn cho tất cả hộ gia đình trong đợt thu:\n" +
                    "\"" + selected.getTenDot() + "\"?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int count = phieuThuService.generateReceiptsForDrive(selected.getMaDot());
                if (count > 0) {
                    showAlert(AlertType.INFORMATION, "Thành công",
                            "Đã tạo thành công " + count + " phiếu thu cho đợt thu \"" + selected.getTenDot() + "\".");
                } else {
                    showAlert(AlertType.WARNING, "Thông báo",
                            "Không có hóa đơn nào được tạo. Có thể đã tồn tại hoặc không có hộ gia đình nào.");
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi tạo hóa đơn hàng loạt: " + e.getMessage());
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
        String tenDot = tfTenDot.getText().trim();
        if (tenDot.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Tên đợt thu không được để trống.");
            return false;
        }

        if (dpNgayBatDau.getValue() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn ngày bắt đầu.");
            return false;
        }

        if (dpNgayKetThuc.getValue() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn ngày kết thúc.");
            return false;
        }

        if (cbTrangThai.getValue() == null || cbTrangThai.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn trạng thái.");
            return false;
        }

        return true;
    }

    /**
     * Tạo đối tượng DotThu từ dữ liệu form.
     *
     * @return đối tượng DotThu hoặc null nếu có lỗi
     */
    private DotThu buildDotThuFromForm() {
        if (!validateInputs()) {
            return null;
        }

        String tenDot = tfTenDot.getText().trim();
        LocalDate ngayBatDau = dpNgayBatDau.getValue();
        LocalDate ngayKetThuc = dpNgayKetThuc.getValue();
        String trangThai = cbTrangThai.getValue();
        String moTa = taMoTa.getText().trim();

        return new DotThu(0, tenDot, ngayBatDau, ngayKetThuc, trangThai, moTa.isEmpty() ? null : moTa);
    }

    /**
     * Điền dữ liệu từ DotThu vào form.
     *
     * @param dotThu đối tượng DotThu cần hiển thị
     */
    private void populateForm(DotThu dotThu) {
        if (dotThu == null) {
            return;
        }
        tfTenDot.setText(dotThu.getTenDot());
        dpNgayBatDau.setValue(dotThu.getNgayBatDau());
        dpNgayKetThuc.setValue(dotThu.getNgayKetThuc());
        cbTrangThai.setValue(dotThu.getTrangThai());
        taMoTa.setText(dotThu.getMoTa() != null ? dotThu.getMoTa() : "");
    }

    /**
     * Xóa tất cả dữ liệu trong form và bỏ chọn dòng trong bảng.
     */
    private void clearForm() {
        tfTenDot.clear();
        dpNgayBatDau.setValue(null);
        dpNgayKetThuc.setValue(null);
        cbTrangThai.setValue(null);
        taMoTa.clear();
        tableDotThu.getSelectionModel().clearSelection();
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
