package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.models.DotThu;
import com.bluemoon.models.HoGiaDinh;
import com.bluemoon.models.PhieuThu;

/**
 * Controller tạo và quản lý Phiếu Thu và chi tiết.
 */
public class PhieuThuController {

    // Chọn hộ gia đình và đợt thu
    @FXML private ComboBox<HoGiaDinh> hoGiaDinhCombo;
    @FXML private ComboBox<DotThu> dotThuCombo;

    // Thông tin phiếu thu
    @FXML private TextField maPhieuField;
    @FXML private DatePicker ngayLapPicker;
    @FXML private TextField tongTienField;
    @FXML private TextField trangThaiField;
    @FXML private TextField hinhThucThuField;

    // Bảng chi tiết phiếu thu
    @FXML private TableView<ChiTietThu> chiTietTable;
    @FXML private TableColumn<ChiTietThu, Integer> colMaKhoan;
    @FXML private TableColumn<ChiTietThu, String> colSoLuong;
    @FXML private TableColumn<ChiTietThu, String> colDonGia;
    @FXML private TableColumn<ChiTietThu, String> colThanhTien;

    private Object phieuThuService; // TODO: thay bằng PhieuThuService khi có
    private Object hoGiaDinhService; // TODO: HoGiaDinhService
    private Object dotThuService;    // TODO: DotThuService
    private Object khoanThuService;  // TODO: KhoanThuService

    /** Khởi tạo dữ liệu mặc định. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - nạp dữ liệu combo, cấu hình bảng chi tiết
    }

    /** Tạo mới một phiếu thu dựa trên lựa chọn. */
    @FXML
    private void handleCreatePhieuThuAction(ActionEvent event) {
        // TODO: Implement logic - khởi tạo PhieuThu
    }

    /** Thêm một chi tiết vào bảng chi tiết phiếu thu. */
    @FXML
    private void handleAddChiTietThuAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Lưu phiếu thu và chi tiết xuống cơ sở dữ liệu. */
    @FXML
    private void handleSavePhieuThuAction(ActionEvent event) {
        // TODO: Implement logic
    }

    /** Cập nhật trạng thái phiếu thu. */
    @FXML
    private void handleUpdateTrangThaiAction(ActionEvent event) {
        // TODO: Implement logic
    }
}
