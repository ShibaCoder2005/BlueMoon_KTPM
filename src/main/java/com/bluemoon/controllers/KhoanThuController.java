package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.KhoanThu;

/**
 * Controller quản lý danh mục KhoanThu.
 */
public class KhoanThuController {

    @FXML private TableView<KhoanThu> khoanThuTable;
    @FXML private TableColumn<KhoanThu, Integer> colId;
    @FXML private TableColumn<KhoanThu, String> colTenKhoan;

    @FXML private TextField maKhoanThuField;
    @FXML private TextField tenKhoanField;
    @FXML private ComboBox<String> loaiCombo;
    @FXML private TextField donGiaField;
    @FXML private TextField donViTinhField;
    @FXML private ComboBox<String> dinhMucTheoCombo;
    @FXML private CheckBox batBuocCheck;

    private Object khoanThuService; // TODO: thay bằng KhoanThuService khi có

    /** Khởi tạo bảng và dữ liệu mặc định. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp danh sách, cấu hình cột
    }

    /** Thêm mới một KhoanThu. */
    @FXML
    private void handleAddKhoanThuAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Cập nhật KhoanThu được chọn. */
    @FXML
    private void handleUpdateKhoanThuAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Xóa KhoanThu được chọn. */
    @FXML
    private void handleDeleteKhoanThuAction(ActionEvent event) {
        // TODO: Implement logic
    }
}
