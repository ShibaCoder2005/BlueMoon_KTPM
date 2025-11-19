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
    List<HoGiaDinh> getAll();

    /**
     * Tìm hộ gia đình theo mã.
     *
     * @param maHo mã hộ cần tìm
     * @return {@link HoGiaDinh} nếu tồn tại, {@code null} nếu không
     */
    HoGiaDinh findById(int maHo);

    /**
     * Tìm kiếm theo từ khóa (tên chủ hộ, địa chỉ).
     *
     * @param keyword từ khóa người dùng nhập
     * @return danh sách kết quả
     */
    List<HoGiaDinh> search(String keyword);

    /**
     * Thêm mới một hộ gia đình.
     *
     * @param hoGiaDinh dữ liệu cần thêm
     * @return true nếu thành công
     */
    boolean add(HoGiaDinh hoGiaDinh);

    /**
     * Cập nhật hộ gia đình.
     *
     * @param hoGiaDinh dữ liệu cần cập nhật
     * @return true nếu thành công
     */
    boolean update(HoGiaDinh hoGiaDinh);

    /**
     * Xóa hộ gia đình theo mã.
     *
     * @param maHo mã hộ cần xóa
     * @return true nếu thành công
     */
    boolean delete(int maHo);
}