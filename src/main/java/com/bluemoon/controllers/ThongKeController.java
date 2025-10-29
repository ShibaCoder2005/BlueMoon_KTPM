package com.bluemoon.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import com.bluemoon.models.ThongKe;

/**
 * Controller hiển thị thống kê tổng quan và theo chuyên đề.
 */
public class ThongKeController {

    @FXML private Label tongThuLabel;
    @FXML private Label tongHoLabel;
    @FXML private TableView<ThongKe> thongKeTable;
    @FXML private BarChart<String, Number> thuTheoThangChart;
    @FXML private PieChart tyLeKhoanThuChart;

    private Object thongKeService; // TODO: thay bằng ThongKeService khi có

    /** Nạp thống kê mặc định khi mở màn hình. */
    @FXML
    private void initialize() {
        // TODO: Implement logic - gọi service để lấy dữ liệu mặc định
    }

    /** Làm mới thống kê thu theo đợt/khoản. */
    public void refreshCollectionStats() {
        // TODO: Implement logic - cập nhật bảng/biểu đồ liên quan thu
    }

    /** Làm mới thống kê cư dân/hộ gia đình. */
    public void refreshResidentStats() {
        // TODO: Implement logic - cập nhật các nhãn và bảng cư dân
    }
}
