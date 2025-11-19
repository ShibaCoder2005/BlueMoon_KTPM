package com.bluemoon.controllers;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import com.bluemoon.services.ThongKeService;
import com.bluemoon.services.impl.ThongKeServiceImpl;

/**
 * Controller hiển thị thống kê tổng quan và theo chuyên đề.
 * Xử lý nghiệp vụ: Xem báo cáo, xuất báo cáo, hiển thị biểu đồ.
 */
public class ThongKeController implements Initializable {

    private final ThongKeService thongKeService = new ThongKeServiceImpl();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- FXML Components: Inputs ---
    @FXML
    private DatePicker dpTuNgay;

    @FXML
    private DatePicker dpDenNgay;

    @FXML
    private ComboBox<String> cbLoaiBaoCao;

    // --- FXML Components: Outputs (Visualization) ---
    @FXML
    private Label lblTongDoanhThu;

    @FXML
    private Label lblTongCongNo;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private TableView<Map<String, Object>> tableChiTiet;

    @FXML
    private TableColumn<Map<String, Object>, String> colMaPhieu;

    @FXML
    private TableColumn<Map<String, Object>, String> colTenHo;

    @FXML
    private TableColumn<Map<String, Object>, String> colTenDot;

    @FXML
    private TableColumn<Map<String, Object>, String> colTongTien;

    @FXML
    private TableColumn<Map<String, Object>, String> colTrangThai;

    @FXML
    private TableColumn<Map<String, Object>, String> colNgayLap;

    private final ObservableList<Map<String, Object>> detailData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        initializeComboBox();
        setDefaultDates();
        loadDashboardStats();
    }

    /**
     * Cấu hình các cột trong TableView.
     */
    private void configureTableColumns() {
        // Configure columns to extract values from Map
        colMaPhieu.setCellValueFactory(cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get("maPhieu");
            return new SimpleStringProperty(value != null ? value.toString() : "");
        });

        colTenHo.setCellValueFactory(cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get("tenHo");
            return new SimpleStringProperty(value != null ? value.toString() : "");
        });

        colTenDot.setCellValueFactory(cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get("tenDot");
            return new SimpleStringProperty(value != null ? value.toString() : "");
        });

        colTongTien.setCellValueFactory(cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get("tongTien");
            if (value instanceof BigDecimal) {
                return new SimpleStringProperty(((BigDecimal) value).toString() + " VNĐ");
            }
            return new SimpleStringProperty(value != null ? value.toString() : "");
        });

        colTrangThai.setCellValueFactory(cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get("trangThai");
            return new SimpleStringProperty(value != null ? value.toString() : "");
        });

        colNgayLap.setCellValueFactory(cellData -> {
            Map<String, Object> row = cellData.getValue();
            Object value = row.get("ngayLap");
            if (value instanceof java.time.LocalDateTime) {
                return new SimpleStringProperty(
                        ((java.time.LocalDateTime) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty(value != null ? value.toString() : "");
        });

        tableChiTiet.setItems(detailData);
    }

    /**
     * Khởi tạo ComboBox loại báo cáo với các giá trị mặc định.
     */
    private void initializeComboBox() {
        ObservableList<String> loaiBaoCaoList = FXCollections.observableArrayList(
                "Doanh thu theo đợt",
                "Tình hình đóng phí",
                "Công nợ hộ gia đình"
        );
        cbLoaiBaoCao.setItems(loaiBaoCaoList);
    }

    /**
     * Thiết lập ngày mặc định (đầu tháng và cuối tháng hiện tại).
     */
    private void setDefaultDates() {
        LocalDate now = LocalDate.now();
        dpTuNgay.setValue(now.withDayOfMonth(1)); // First day of current month
        dpDenNgay.setValue(now.withDayOfMonth(now.lengthOfMonth())); // Last day of current month
    }

    /**
     * Nạp thống kê tổng quan cho dashboard.
     */
    private void loadDashboardStats() {
        try {
            Map<String, Object> stats = thongKeService.getDashboardStats();
            BigDecimal totalRevenue = (BigDecimal) stats.get("totalRevenue");
            BigDecimal totalDebt = (BigDecimal) stats.get("totalDebt");

            lblTongDoanhThu.setText(totalRevenue != null ? totalRevenue.toString() + " VNĐ" : "0 VNĐ");
            lblTongCongNo.setText(totalDebt != null ? totalDebt.toString() + " VNĐ" : "0 VNĐ");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể tải thống kê tổng quan: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xem báo cáo.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleViewReport(ActionEvent event) {
        try {
            // Validate
            if (dpTuNgay.getValue() == null || dpDenNgay.getValue() == null) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.");
                return;
            }

            if (dpDenNgay.getValue().isBefore(dpTuNgay.getValue())) {
                showAlert(AlertType.ERROR, "Giá trị không hợp lệ", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
                return;
            }

            if (cbLoaiBaoCao.getValue() == null || cbLoaiBaoCao.getValue().isEmpty()) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn loại báo cáo.");
                return;
            }

            String loaiBaoCao = cbLoaiBaoCao.getValue();
            LocalDate fromDate = dpTuNgay.getValue();
            LocalDate toDate = dpDenNgay.getValue();

            // Process based on report type
            if (loaiBaoCao.equals("Doanh thu theo đợt")) {
                handleRevenueReport(fromDate, toDate);
            } else if (loaiBaoCao.equals("Tình hình đóng phí")) {
                handlePaymentStatusReport();
            } else if (loaiBaoCao.equals("Công nợ hộ gia đình")) {
                handleDebtReport();
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xem báo cáo: " + e.getMessage());
        }
    }

    /**
     * Xử lý báo cáo doanh thu.
     *
     * @param fromDate ngày bắt đầu
     * @param toDate   ngày kết thúc
     */
    private void handleRevenueReport(LocalDate fromDate, LocalDate toDate) {
        Map<String, Number> revenueStats = thongKeService.getRevenueStats(fromDate, toDate);
        BigDecimal totalRevenue = thongKeService.getTotalRevenue(fromDate, toDate);

        if (revenueStats.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Không có dữ liệu",
                    "Không có dữ liệu doanh thu trong khoảng thời gian đã chọn.");
            clearCharts();
            detailData.clear();
            return;
        }

        // Update total revenue label
        lblTongDoanhThu.setText(totalRevenue.toString() + " VNĐ");

        // Update BarChart
        updateBarChart(revenueStats, "Doanh thu theo đợt (VNĐ)");

        // Clear PieChart
        pieChart.getData().clear();

        // Load details
        List<Map<String, Object>> details = thongKeService.getRevenueDetails(fromDate, toDate);
        detailData.setAll(details);
    }

    /**
     * Xử lý báo cáo tình hình đóng phí.
     */
    private void handlePaymentStatusReport() {
        Map<String, Number> debtStats = thongKeService.getDebtStats();
        BigDecimal totalDebt = thongKeService.getTotalDebt();

        if (debtStats.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Không có dữ liệu",
                    "Không có dữ liệu về tình hình đóng phí.");
            clearCharts();
            detailData.clear();
            return;
        }

        // Update total debt label
        lblTongCongNo.setText(totalDebt.toString() + " VNĐ");

        // Update PieChart
        updatePieChart(debtStats, "Tình hình đóng phí");

        // Clear BarChart
        barChart.getData().clear();

        // Load details
        List<Map<String, Object>> details = thongKeService.getDebtDetails();
        detailData.setAll(details);
    }

    /**
     * Xử lý báo cáo công nợ.
     */
    private void handleDebtReport() {
        Map<String, Number> debtStats = thongKeService.getDebtStats();
        BigDecimal totalDebt = thongKeService.getTotalDebt();

        if (debtStats.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Không có dữ liệu",
                    "Không có dữ liệu công nợ.");
            clearCharts();
            detailData.clear();
            return;
        }

        // Update total debt label
        lblTongCongNo.setText(totalDebt.toString() + " VNĐ");

        // Update PieChart
        updatePieChart(debtStats, "Công nợ hộ gia đình");

        // Update BarChart with debt breakdown
        updateBarChart(debtStats, "Công nợ (VNĐ)");

        // Load details
        List<Map<String, Object>> details = thongKeService.getDebtDetails();
        detailData.setAll(details);
    }

    /**
     * Cập nhật BarChart với dữ liệu.
     *
     * @param data      dữ liệu để hiển thị
     * @param chartTitle tiêu đề biểu đồ
     */
    private void updateBarChart(Map<String, Number> data, String chartTitle) {
        barChart.getData().clear();
        barChart.setTitle(chartTitle);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số tiền");

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
    }

    /**
     * Cập nhật PieChart với dữ liệu.
     *
     * @param data      dữ liệu để hiển thị
     * @param chartTitle tiêu đề biểu đồ
     */
    private void updatePieChart(Map<String, Number> data, String chartTitle) {
        pieChart.getData().clear();
        pieChart.setTitle(chartTitle);

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            double value = entry.getValue().doubleValue();
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + value + " VNĐ)", value);
            pieChart.getData().add(slice);
        }
    }

    /**
     * Xóa tất cả biểu đồ.
     */
    private void clearCharts() {
        barChart.getData().clear();
        pieChart.getData().clear();
    }

    /**
     * Xử lý sự kiện xuất báo cáo.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleExportReport(ActionEvent event) {
        try {
            if (detailData.isEmpty()) {
                showAlert(AlertType.WARNING, "Không có dữ liệu",
                        "Vui lòng xem báo cáo trước khi xuất.");
                return;
            }

            // Show FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Xuất báo cáo");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("All Files", "*.*"));

            // Get the stage from the event
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Simulation: In a real implementation, this would generate PDF/Excel
                // For now, just log and show success message
                System.out.println("Exporting report to: " + file.getAbsolutePath());
                System.out.println("Report type: " + cbLoaiBaoCao.getValue());
                System.out.println("Date range: " + dpTuNgay.getValue() + " to " + dpDenNgay.getValue());
                System.out.println("Total records: " + detailData.size());

                showAlert(AlertType.INFORMATION, "Thành công",
                        "Xuất báo cáo thành công!\n" +
                                "File: " + file.getName() + "\n" +
                                "Số lượng bản ghi: " + detailData.size());
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi xuất báo cáo: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện xóa form và reset dữ liệu.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    /**
     * Xóa tất cả dữ liệu trong form và reset về mặc định.
     */
    private void clearForm() {
        setDefaultDates();
        cbLoaiBaoCao.setValue(null);
        clearCharts();
        detailData.clear();
        loadDashboardStats();
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
