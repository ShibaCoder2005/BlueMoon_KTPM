package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.DotThu;

/**
 * Định nghĩa các nghiệp vụ quản lý đợt thu (DotThu).
 */
public interface DotThuService {

    /**
     * Lấy toàn bộ đợt thu.
     *
     * @return danh sách {@link DotThu}
     */
    List<DotThu> getAllDotThu();

    /**
     * Tìm đợt thu theo ID.
     *
     * @param id ID đợt thu
     * @return {@link DotThu} nếu tồn tại, {@code null} nếu không
     */
    DotThu getDotThuById(int id);

    /**
     * Thêm mới một đợt thu.
     *
     * @param dotThu đối tượng đợt thu cần thêm
     * @return true nếu thành công
     */
    boolean addDotThu(DotThu dotThu);

    /**
     * Cập nhật chi tiết hoặc trạng thái đợt thu.
     *
     * @param dotThu đối tượng đợt thu cần cập nhật
     * @return true nếu thành công
     */
    boolean updateDotThu(DotThu dotThu);

    /**
     * Xóa đợt thu theo ID.
     * Kiểm tra ràng buộc: không được xóa nếu có PhieuThu liên quan.
     *
     * @param id ID của đợt thu cần xóa
     * @return true nếu thành công, false nếu có ràng buộc hoặc lỗi
     */
    boolean deleteDotThu(int id);

    /**
     * Tìm kiếm đợt thu theo từ khóa (tên đợt thu, mô tả).
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách đợt thu khớp
     */
    List<DotThu> searchDotThu(String keyword);
}


