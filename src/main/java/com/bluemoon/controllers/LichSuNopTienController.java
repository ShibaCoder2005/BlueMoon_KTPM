package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.LichSuNopTien;
import com.bluemoon.models.PhieuThu;

/**
 * Controller ghi nhận các lần nộp tiền cho Phiếu Thu.
 */
public class LichSuNopTienController {

    @FXML private ComboBox<PhieuThu> phieuThuCombo;
    @FXML private TextField soTienField;
    @FXML private DatePicker ngayNopPicker;
    @FXML private ComboBox<String> phuongThucCombo;

    @FXML private TableView<LichSuNopTien> lichSuNopTienTable;

    private Object lichSuNopTienService; // TODO: thay bằng LichSuNopTienService
    private Object phieuThuService;      // TODO: PhieuThuService

    /** Khởi tạo dữ liệu ban đầu. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp danh sách phiếu, cấu hình bảng
    }

    /** Tìm kiếm phiếu thu/chưa thanh toán theo điều kiện nhập. */
    @FXML
    private void handleSearchAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Ghi nhận một lần nộp tiền cho phiếu đã chọn. */
    @FXML
    private void handleRecordPaymentAction(ActionEvent event) {
        // TODO: Implement logic
    }
}
