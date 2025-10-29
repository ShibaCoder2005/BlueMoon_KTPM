package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.DotThu;

/**
 * Controller quản lý các đợt thu (DotThu).
 */
public class DotThuController {

    @FXML private TableView<DotThu> dotThuTable;
    @FXML private TableColumn<DotThu, Integer> colId;
    @FXML private TableColumn<DotThu, String> colTenDot;

    @FXML private TextField maDotThuField;
    @FXML private TextField tenDotField;
    @FXML private DatePicker ngayBatDauPicker;
    @FXML private DatePicker ngayKetThucPicker;
    @FXML private TextField trangThaiField;

    private Object dotThuService; // TODO: thay bằng DotThuService khi có

    /** Khởi tạo dữ liệu bảng và form. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp danh sách, thiết lập cột
    }

    /** Tạo mới một đợt thu. */
    @FXML
    private void handleCreateDotThuAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Cập nhật trạng thái hoặc thông tin đợt thu. */
    @FXML
    private void handleUpdateDotThuAction(ActionEvent event) {
        // TODO: Implement logic
    }
}
