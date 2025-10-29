package com.bluemoon.models;

import java.time.LocalDate;

/**
 * LichSuNhanKhau: lịch sử biến động nhân khẩu.
 */
public class LichSuNhanKhau {

    /** id: khóa chính. */
    private int id;
    /** maNhanKhau: mã nhân khẩu (int). */
    private int maNhanKhau;
    /** loaiBienDong: loại biến động (varchar(50)). */
    private String loaiBienDong;
    /** ngayBatDau: ngày bắt đầu (date). */
    private LocalDate ngayBatDau;
    /** ngayKetThuc: ngày kết thúc (date). */
    private LocalDate ngayKetThuc;
    /** nguoiGhi: id người ghi nhận (int). */
    private int nguoiGhi;

    /** Constructor mặc định. */
    public LichSuNhanKhau() {}

    /** Constructor đầy đủ. */
    public LichSuNhanKhau(int id, int maNhanKhau, String loaiBienDong, LocalDate ngayBatDau,
                          LocalDate ngayKetThuc, int nguoiGhi) {
        this.id = id;
        this.maNhanKhau = maNhanKhau;
        this.loaiBienDong = loaiBienDong;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.nguoiGhi = nguoiGhi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMaNhanKhau() { return maNhanKhau; }
    public void setMaNhanKhau(int maNhanKhau) { this.maNhanKhau = maNhanKhau; }

    public String getLoaiBienDong() { return loaiBienDong; }
    public void setLoaiBienDong(String loaiBienDong) { this.loaiBienDong = loaiBienDong; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public int getNguoiGhi() { return nguoiGhi; }
    public void setNguoiGhi(int nguoiGhi) { this.nguoiGhi = nguoiGhi; }
}
