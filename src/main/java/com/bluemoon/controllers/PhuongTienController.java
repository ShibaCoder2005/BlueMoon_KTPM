package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.PhuongTien;

/**
 * Controller quản lý phương tiện của hộ gia đình.
 */
public class PhuongTienController {

    @FXML private TableView<PhuongTien> phuongTienTable;
    @FXML private TableColumn<PhuongTien, Integer> colId;
    @FXML private TableColumn<PhuongTien, String> colBienSo;

    @FXML private ComboBox<HoGiaDinh> hoGiaDinhCombo;
    @FXML private TextField maHoField;
    @FXML private TextField loaiXeField;
    @FXML private TextField bienSoField;
    @FXML private TextField tenChuXeField;
    @FXML private DatePicker ngayDangKyPicker;

    private Object phuongTienService; // TODO: thay bằng PhuongTienService/HoGiaDinhService

    /** Khởi tạo bảng và dữ liệu mặc định. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp danh sách, cấu hình cột
    }

    /** Thêm phương tiện mới từ form. */
    @FXML
    private void handleAddPhuongTienAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Cập nhật thông tin phương tiện đã chọn. */
    @FXML
    private void handleUpdatePhuongTienAction(ActionEvent event) {
        // TODO: Implement logic
    }
}
