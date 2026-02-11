package com.bluemoon.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.bluemoon.models.ThongKe;

/**
 * Định nghĩa các nghiệp vụ tổng hợp thống kê và báo cáo.
 */
public interface ThongKeService {

    /**
     * Lấy thống kê doanh thu theo khoảng thời gian.
     *
     * @param fromDate ngày bắt đầu
     * @param toDate   ngày kết thúc
     * @return Map với key là tên đợt thu/khoản thu, value là số tiền
     */
    Map<String, Number> getRevenueStats(LocalDate fromDate, LocalDate toDate);

    /**
     * Lấy thống kê công nợ (tình hình thanh toán).
     *
     * @return Map với key là trạng thái ("Đã thanh toán", "Chưa thanh toán"), value là số tiền
     */
    Map<String, Number> getDebtStats();

    /**
     * Lấy tổng doanh thu trong khoảng thời gian.
     *
     * @param fromDate ngày bắt đầu
     * @param toDate   ngày kết thúc
     * @return tổng doanh thu
     */
    BigDecimal getTotalRevenue(LocalDate fromDate, LocalDate toDate);

    /**
     * Lấy tổng công nợ (số tiền chưa thanh toán).
     *
     * @return tổng công nợ
     */
    BigDecimal getTotalDebt();

    /**
     * Lấy thống kê tổng quan cho dashboard.
     *
     * @return Map chứa các chỉ số tổng quan
     */
    Map<String, Object> getDashboardStats();

    /**
     * Tạo báo cáo thu tiền theo đợt.
     *
     * @param maDotThu mã đợt thu
     * @return dữ liệu báo cáo
     */
    Map<String, Object> generateCollectionReport(int maDotThu);

    /**
     * Lấy danh sách chi tiết báo cáo doanh thu.
     *
     * @param fromDate ngày bắt đầu
     * @param toDate   ngày kết thúc
     * @return danh sách các bản ghi chi tiết
     */
    List<Map<String, Object>> getRevenueDetails(LocalDate fromDate, LocalDate toDate);

    /**
     * Lấy danh sách chi tiết công nợ.
     *
     * @return danh sách các bản ghi công nợ
     */
    List<Map<String, Object>> getDebtDetails();

    /**
     * Lấy thống kê nhân khẩu (theo tình trạng, giới tính, độ tuổi, etc.).
     * Bao gồm các chỉ số như: tổng số nhân khẩu, phân bố theo tình trạng (Thường trú, Tạm vắng, etc.),
     * phân bố theo giới tính, phân bố theo độ tuổi, số hộ gia đình, v.v.
     *
     * @return Map chứa các chỉ số thống kê nhân khẩu với các key như:
     *         - "totalResidents": Tổng số nhân khẩu
     *         - "byStatus": Map<String, Integer> - Phân bố theo tình trạng
     *         - "byGender": Map<String, Integer> - Phân bố theo giới tính
     *         - "byAgeGroup": Map<String, Integer> - Phân bố theo nhóm tuổi
     *         - "totalHouseholds": Tổng số hộ gia đình
     */
    Map<String, Object> getResidentDemographics();

}


