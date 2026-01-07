package com.bluemoon.services;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import com.bluemoon.models.BaoCaoCongNo;
import com.bluemoon.models.BaoCaoThu;

/**
 * Service interface cho báo cáo và xuất file.
 */
public interface BaoCaoService {
    
    /**
     * Lấy báo cáo thu theo tháng/năm.
     * 
     * @param month tháng (1-12)
     * @param year năm
     * @return danh sách báo cáo thu
     */
    List<BaoCaoThu> getRevenueReport(int month, int year);
    
    /**
     * Lấy báo cáo thu theo khoảng thời gian.
     * 
     * @param fromDate ngày bắt đầu
     * @param toDate ngày kết thúc
     * @return danh sách báo cáo thu
     */
    List<BaoCaoThu> getRevenueReport(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Lấy báo cáo công nợ.
     * 
     * @return danh sách báo cáo công nợ
     */
    List<BaoCaoCongNo> getDebtReport();
    
    /**
     * Lấy báo cáo công nợ theo đợt thu.
     * 
     * @param maDot mã đợt thu
     * @return danh sách báo cáo công nợ
     */
    List<BaoCaoCongNo> getDebtReport(int maDot);
    
    /**
     * Xuất báo cáo thu ra file Excel.
     * 
     * @param data danh sách dữ liệu báo cáo thu
     * @param fromDate ngày bắt đầu (cho tên file)
     * @param toDate ngày kết thúc (cho tên file)
     * @return InputStream của file Excel
     */
    InputStream exportRevenueToExcel(List<BaoCaoThu> data, LocalDate fromDate, LocalDate toDate);
    
    /**
     * Xuất báo cáo công nợ ra file Excel.
     * 
     * @param data danh sách dữ liệu báo cáo công nợ
     * @return InputStream của file Excel
     */
    InputStream exportDebtToExcel(List<BaoCaoCongNo> data);
}

