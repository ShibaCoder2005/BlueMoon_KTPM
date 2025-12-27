package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.ChiTietThu;
import com.bluemoon.models.PhieuThu;

/**
 * Định nghĩa các nghiệp vụ quản lý Phiếu Thu và chi tiết thu.
 */
public interface PhieuThuService {

    /**
     * Tạo mới phần header của phiếu thu.
     *
     * @param phieuThu đối tượng phiếu thu
     * @return mã phiếu tạo ra hoặc -1 nếu thất bại
     */
    int createPhieuThu(PhieuThu phieuThu);

    /**
     * Tạo mới phiếu thu kèm danh sách chi tiết (transaction).
     *
     * @param phieuThu đối tượng phiếu thu
     * @param chiTietList danh sách chi tiết thu
     * @return mã phiếu tạo ra hoặc -1 nếu thất bại
     */
    int createPhieuThuWithDetails(PhieuThu phieuThu, List<ChiTietThu> chiTietList);

    /**
     * Thêm một dòng chi tiết vào phiếu thu.
     *
     * @param chiTiet chi tiết cần thêm
     * @return true nếu thành công
     */
    boolean addChiTietThu(ChiTietThu chiTiet);

    /**
     * Lấy phiếu thu kèm chi tiết.
     *
     * @param maPhieu mã phiếu
     * @return đối tượng PhieuThu (bao gồm chi tiết nếu có cơ chế đính kèm)
     */
    PhieuThu getPhieuThuWithDetails(int maPhieu);

    /**
     * Lấy danh sách chi tiết thu của một phiếu thu.
     *
     * @param maPhieu mã phiếu thu
     * @return danh sách chi tiết thu
     */
    List<ChiTietThu> getChiTietThuByPhieu(int maPhieu);

    /**
     * Tìm các phiếu thu theo mã hộ gia đình.
     *
     * @param maHo mã hộ
     * @return danh sách phiếu thu
     */
    List<PhieuThu> findPhieuThuByHoGiaDinh(int maHo);

    /**
     * Tìm các phiếu thu theo đợt thu.
     *
     * @param maDotThu mã đợt thu
     * @return danh sách phiếu thu
     */
    List<PhieuThu> findPhieuThuByDotThu(int maDotThu);

    /**
     * Lấy toàn bộ danh sách phiếu thu.
     *
     * @return danh sách phiếu thu
     */
    List<PhieuThu> getAllPhieuThu();

    /**
     * Cập nhật trạng thái phiếu thu.
     *
     * @param maPhieu mã phiếu
     * @param newStatus trạng thái mới
     * @return true nếu cập nhật thành công
     */
    boolean updatePhieuThuStatus(int maPhieu, String newStatus);

    /**
     * Tạo hóa đơn hàng loạt cho tất cả hộ gia đình trong một đợt thu.
     *
     * @param maDot mã đợt thu
     * @return số lượng phiếu thu đã tạo thành công
     */
    int generateReceiptsForDrive(int maDot);

    /**
     * Kiểm tra xem hộ gia đình có còn nợ phí chưa thanh toán không.
     *
     * @param maHo mã hộ gia đình (id)
     * @return true nếu có nợ phí, false nếu không
     */
    boolean hasUnpaidFees(int maHo);

    /**
     * Kiểm tra xem khoản thu có đang được sử dụng trong bất kỳ chi tiết thu nào không.
     *
     * @param maKhoanThu mã khoản thu
     * @return true nếu đang được sử dụng, false nếu không
     */
    boolean isFeeUsed(int maKhoanThu);

    /**
     * Kiểm tra xem phiếu thu có thể được chỉnh sửa/xóa không.
     * Phiếu thu chỉ có thể chỉnh sửa/xóa nếu chưa thanh toán (trạng thái khác "Đã thu", "Đã thanh toán").
     *
     * @param maPhieu mã phiếu thu
     * @return true nếu có thể chỉnh sửa (chưa thanh toán), false nếu đã thanh toán
     */
    boolean canModifyPhieuThu(int maPhieu);

    /**
     * Cập nhật phiếu thu và chi tiết thu (chỉ cho phép nếu chưa thanh toán).
     * Kiểm tra trạng thái phiếu thu trước khi cập nhật. Nếu đã thanh toán, không cho phép cập nhật.
     *
     * @param phieuThu đối tượng phiếu thu đã cập nhật
     * @param chiTietList danh sách chi tiết mới (có thể null để giữ nguyên chi tiết hiện tại)
     * @return true nếu thành công, false nếu đã thanh toán hoặc lỗi
     */
    boolean updatePhieuThu(PhieuThu phieuThu, List<ChiTietThu> chiTietList);

    /**
     * Xóa phiếu thu (chỉ cho phép nếu chưa thanh toán).
     * Kiểm tra trạng thái phiếu thu trước khi xóa. Nếu đã thanh toán, không cho phép xóa.
     *
     * @param maPhieu mã phiếu thu
     * @return true nếu thành công, false nếu đã thanh toán hoặc lỗi
     */
    boolean deletePhieuThu(int maPhieu);

    /**
     * Tính tổng số tiền cần đóng của một hộ gia đình cho một đợt thu.
     * Tính dựa trên các khoản thu bắt buộc và cách tính (diện tích, nhân khẩu, hộ khẩu, xe máy, ô tô).
     *
     * @param maHo mã hộ gia đình
     * @param maDot mã đợt thu
     * @return tổng số tiền cần đóng, hoặc null nếu có lỗi
     */
    java.math.BigDecimal calculateTotalAmountForHousehold(int maHo, int maDot);
}


