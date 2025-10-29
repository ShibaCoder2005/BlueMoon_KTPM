package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.HoGiaDinh;

/**
 * Controller quản lý dữ liệu HoGiaDinh.
 */
public class HoGiaDinhController {

    // Bảng danh sách hộ gia đình
    @FXML private TableView<HoGiaDinh> hoGiaDinhTable;
    @FXML private TableColumn<HoGiaDinh, Integer> colId;
    @FXML private TableColumn<HoGiaDinh, Integer> colSoPhong;
    @FXML private TableColumn<HoGiaDinh, String> colDienTich;
    @FXML private TableColumn<HoGiaDinh, Integer> colMaChuHo;

    // Trường nhập liệu
    @FXML private TextField maHoField;
    @FXML private TextField soPhongField;
    @FXML private TextField dienTichField;
    @FXML private TextField maChuHoField;
    @FXML private TextField ghiChuField;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button viewDetailButton;

    private Object hoGiaDinhService; // TODO: thay bằng HoGiaDinhService khi có

    /** Khởi tạo dữ liệu bảng và binding cột. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp danh sách, map cột, bind selection -> form
    }

    /** Thêm mới một HoGiaDinh từ form nhập liệu. */
    @FXML
    private void handleAddHoGiaDinhAction(ActionEvent event) {
        // TODO: Implement logic - đọc form, gọi service thêm mới, refresh bảng
    }

    /** Cập nhật HoGiaDinh được chọn với dữ liệu form. */
    @FXML
    private void handleUpdateHoGiaDinhAction(ActionEvent event) {
        // TODO: Implement logic - cập nhật bản ghi, refresh bảng
    }

    /** Xem chi tiết HoGiaDinh đang được chọn trong bảng. */
    @FXML
    private void handleViewDetailsAction(ActionEvent event) {
        // TODO: Implement logic - hiển thị chi tiết
    }
}
