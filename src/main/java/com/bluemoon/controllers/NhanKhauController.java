package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.LichSuNhanKhau;
import com.bluemoon.models.NhanKhau;

/**
 * Controller quản lý nhân khẩu và lịch sử biến động.
 */
public class NhanKhauController {

    // Bảng nhân khẩu
    @FXML private TableView<NhanKhau> nhanKhauTable;
    @FXML private TableColumn<NhanKhau, Integer> colId;
    @FXML private TableColumn<NhanKhau, Integer> colMaHo;
    @FXML private TableColumn<NhanKhau, String> colHoTen;

    // Form nhân khẩu
    @FXML private TextField maHoField;
    @FXML private TextField hoTenField;
    @FXML private DatePicker ngaySinhPicker;
    @FXML private TextField gioiTinhField;
    @FXML private TextField soCCCDField;
    @FXML private TextField ngheNghiepField;
    @FXML private TextField quanHeVoiChuHoField;
    @FXML private TextField tinhTrangField;

    // Bảng lịch sử
    @FXML private TableView<LichSuNhanKhau> lichSuTable;

    private Object nhanKhauService; // TODO: thay bằng NhanKhauService khi có

    /** Khởi tạo bảng và dữ liệu mặc định. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp danh sách, cấu hình cột, bind selection
    }

    /** Thêm nhân khẩu mới từ form. */
    @FXML
    private void handleAddNhanKhauAction(ActionEvent event) {
        // TODO: Implement logic - đọc form, gọi service, refresh
    }

    /** Cập nhật thông tin nhân khẩu được chọn. */
    @FXML
    private void handleUpdateNhanKhauAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Xem lịch sử biến động của nhân khẩu đã chọn. */
    @FXML
    private void handleViewHistoryAction(ActionEvent event) {
        // TODO: Implement logic - nạp lịch sử vào lichSuTable
    }

    /** Thêm bản ghi lịch sử biến động cho nhân khẩu. */
    @FXML
    private void handleAddLichSuNhanKhauAction(ActionEvent event) {
        // TODO: Implement logic
    }
}
