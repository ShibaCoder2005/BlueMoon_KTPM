package com.bluemoon.services;

import java.util.List;

import com.bluemoon.models.PhuongTien;

/**
 * Định nghĩa các nghiệp vụ quản lý phương tiện (PhuongTien).
 */
public interface PhuongTienService {

    /**
     * Lấy toàn bộ phương tiện.
     *
     * @return danh sách {@link PhuongTien}
     */
    List<PhuongTien> getAllPhuongTien();

    /**
     * Tìm phương tiện theo ID.
     *
     * @param id ID phương tiện
     * @return {@link PhuongTien} nếu tồn tại, {@code null} nếu không
     */
    PhuongTien getPhuongTienById(int id);

    /**
     * Tìm phương tiện theo mã hộ gia đình.
     *
     * @param maHo mã hộ gia đình
     * @return danh sách phương tiện của hộ
     */
    List<PhuongTien> getPhuongTienByHoGiaDinh(int maHo);

    /**
     * Tìm phương tiện theo biển số.
     *
     * @param bienSo biển số
     * @return {@link PhuongTien} nếu tồn tại, {@code null} nếu không
     */
    PhuongTien getPhuongTienByBienSo(String bienSo);

    /**
     * Thêm mới một phương tiện.
     *
     * @param phuongTien đối tượng phương tiện cần thêm
     * @return true nếu thành công
     */
    boolean addPhuongTien(PhuongTien phuongTien);

    /**
     * Cập nhật thông tin phương tiện.
     *
     * @param phuongTien đối tượng phương tiện cần cập nhật
     * @return true nếu thành công
     */
    boolean updatePhuongTien(PhuongTien phuongTien);

    /**
     * Xóa phương tiện theo ID.
     *
     * @param id ID của phương tiện cần xóa
     * @return true nếu thành công, false nếu có lỗi
     */
    boolean deletePhuongTien(int id);

    /**
     * Tìm kiếm phương tiện theo từ khóa (biển số, tên chủ xe, loại xe).
     *
     * @param keyword từ khóa tìm kiếm
     * @return danh sách phương tiện khớp
     */
    List<PhuongTien> searchPhuongTien(String keyword);
}

