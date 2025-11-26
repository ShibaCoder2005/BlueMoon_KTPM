package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.LichSuNhanKhau;
import com.bluemoon.models.NhanKhau;

/**
 * Định nghĩa các nghiệp vụ quản lý NhanKhau và lịch sử biến động.
 */
public interface NhanKhauService {

    /**
     * Lấy toàn bộ danh sách nhân khẩu.
     *
     * @return danh sách {@link NhanKhau}
     */
    List<NhanKhau> getAll();

    /**
     * Tìm nhân khẩu theo ID.
     *
     * @param id ID nhân khẩu
     * @return {@link NhanKhau} nếu tồn tại, {@code null} nếu không
     */
    NhanKhau findById(int id);

    /**
     * Lấy danh sách nhân khẩu thuộc một hộ gia đình.
     *
     * @param maHo mã hộ
     * @return danh sách NhanKhau
     */
    List<NhanKhau> getNhanKhauByHoGiaDinh(int maHo);

    /**
     * Thêm mới một nhân khẩu.
     *
     * @param nhanKhau đối tượng nhân khẩu
     * @return true nếu thành công
     */
    boolean addNhanKhau(NhanKhau nhanKhau);

    /**
     * Cập nhật thông tin nhân khẩu.
     *
     * @param nhanKhau đối tượng nhân khẩu
     * @return true nếu thành công
     */
    boolean updateNhanKhau(NhanKhau nhanKhau);

    /**
     * Thêm bản ghi lịch sử cho nhân khẩu.
     *
     * @param history bản ghi lịch sử
     * @return true nếu thành công
     */
    boolean addLichSuNhanKhau(LichSuNhanKhau history);

    /**
     * Lấy lịch sử của một nhân khẩu theo mã.
     *
     * @param maNhanKhau mã nhân khẩu
     * @return danh sách lịch sử
     */
    List<LichSuNhanKhau> getLichSuNhanKhau(int maNhanKhau);

    /**
     * Xóa nhân khẩu theo ID.
     *
     * @param id ID nhân khẩu cần xóa
     * @return true nếu thành công
     */
    boolean deleteNhanKhau(int id);

    /**
     * Kiểm tra xem số CCCD đã tồn tại chưa.
     *
     * @param soCCCD số căn cước công dân
     * @param excludeId ID nhân khẩu cần loại trừ (khi update), 0 nếu không loại trừ
     * @return true nếu đã tồn tại, false nếu không
     */
    boolean isCCCDExists(String soCCCD, int excludeId);

    /**
     * Cập nhật trạng thái nhân khẩu và ghi nhận lịch sử biến động trong một transaction.
     * Sử dụng khi thay đổi tình trạng nhân khẩu (e.g., "Thường trú" -> "Tạm vắng", "Chuyển đi").
     *
     * @param nhanKhauId ID nhân khẩu
     * @param newStatus trạng thái mới (e.g., "Tạm vắng", "Chuyển đi", "TamTru", "ThuongTru")
     * @param historyRecord bản ghi lịch sử (có thể null nếu không cần ghi lịch sử)
     * @return true nếu thành công, false nếu thất bại
     */
    boolean updateStatusWithHistory(int nhanKhauId, String newStatus, LichSuNhanKhau historyRecord);
}


