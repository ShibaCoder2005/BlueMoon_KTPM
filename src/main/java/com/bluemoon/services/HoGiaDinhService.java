package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.HoGiaDinh;

/**
 * Định nghĩa các nghiệp vụ đọc/ghi dữ liệu hộ gia đình.
 */
public interface HoGiaDinhService {

    /**
     * Lấy toàn bộ hộ gia đình.
     *
     * @return danh sách {@link HoGiaDinh}
     */
    List<HoGiaDinh> getAllHoGiaDinh();

    /**
     * Tìm hộ gia đình theo ID.
     *
     * @param id ID hộ gia đình
     * @return {@link HoGiaDinh} nếu tồn tại, {@code null} nếu không
     */
    HoGiaDinh findById(int id);

    /**
     * Thêm mới một hộ gia đình.
     * Kiểm tra maHo unique và dienTich > 0.
     *
     * @param hoGiaDinh dữ liệu cần thêm
     * @return true nếu thành công
     */
    boolean addHoGiaDinh(HoGiaDinh hoGiaDinh);

    /**
     * Cập nhật hộ gia đình.
     * Kiểm tra maHo unique và dienTich > 0.
     *
     * @param hoGiaDinh dữ liệu cần cập nhật
     * @return true nếu thành công
     */
    boolean updateHoGiaDinh(HoGiaDinh hoGiaDinh);

    /**
     * Xóa hộ gia đình theo ID.
     * Kiểm tra ràng buộc: không được xóa nếu có NhanKhau hoặc PhieuThu liên quan.
     *
     * @param id ID hộ gia đình cần xóa
     * @return true nếu thành công, false nếu có ràng buộc hoặc lỗi
     */
    boolean deleteHoGiaDinh(int id);

    /**
     * Tìm kiếm hộ gia đình theo từ khóa (maHo, ghiChu).
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách kết quả
     */
    List<HoGiaDinh> searchHoGiaDinh(String keyword);

    /**
     * Kiểm tra xem mã hộ đã tồn tại chưa.
     *
     * @param maHo mã hộ cần kiểm tra
     * @return true nếu đã tồn tại, false nếu không
     */
    boolean checkMaHoExists(String maHo);
}