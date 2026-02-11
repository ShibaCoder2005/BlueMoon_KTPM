package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.Phong;

/**
 * Định nghĩa các nghiệp vụ quản lý thông tin Căn hộ (Phòng).
 * Bao gồm: Thêm, Sửa, Xóa và kiểm tra các ràng buộc dữ liệu.
 */
public interface PhongService {

    /**
     * Lấy danh sách toàn bộ phòng trong hệ thống.
     * Sắp xếp theo số phòng tăng dần.
     *
     * @return danh sách {@link Phong}
     */
    List<Phong> getAllPhong();

    /**
     * Tìm thông tin phòng theo Số phòng (ID).
     *
     * @param soPhong số phòng cần tìm (khóa chính)
     * @return {@link Phong} nếu tìm thấy, {@code null} nếu không tồn tại
     */
    Phong getPhongById(int soPhong);

    /**
     * Thêm mới một phòng vào hệ thống.
     * <br>
     * <b>Ràng buộc nghiệp vụ:</b>
     * <ul>
     * <li>Số phòng (soPhong) là duy nhất, không được trùng với phòng đã có.</li>
     * <li>Diện tích và Giá tiền nên là số dương.</li>
     * </ul>
     *
     * @param phong đối tượng chứa dữ liệu cần thêm
     * @return {@code true} nếu thêm thành công, {@code false} nếu lỗi hoặc trùng số phòng
     */
    boolean addPhong(Phong phong);

    /**
     * Cập nhật thông tin phòng.
     * <br>
     * Lưu ý: Không được sửa Số phòng (soPhong) vì là khóa chính.
     *
     * @param phong đối tượng chứa dữ liệu mới (diện tích, giá, trạng thái...)
     * @return {@code true} nếu cập nhật thành công, {@code false} nếu không tìm thấy phòng
     */
    boolean updatePhong(Phong phong);

    /**
     * Xóa phòng khỏi hệ thống.
     * <br>
     * <b>Ràng buộc:</b> Không thể xóa phòng nếu đang có Cư dân cư trú 
     * hoặc có dữ liệu liên quan (Hóa đơn, Xe...).
     *
     * @param soPhong số phòng cần xóa
     * @return {@code true} nếu xóa thành công, {@code false} nếu có lỗi ràng buộc
     */
    boolean deletePhong(int soPhong);
    
    /**
     * Kiểm tra xem số phòng đã tồn tại chưa.
     * Dùng để validate khi nhập liệu.
     * * @param soPhong số phòng cần kiểm tra
     * @return {@code true} nếu đã tồn tại
     */
    boolean checkSoPhongExists(int soPhong);
}