package com.bluemoon.models;

import java.time.LocalDate;

/**
 * DotThu: đợt thu tiền theo khoảng thời gian.
 */
public class DotThu {

    /** id: khóa chính. */
    private int id;
    /** tenDot: tên đợt thu (varchar(150)). */
    private String tenDot;
    /** ngayBatDau: ngày bắt đầu (date). */
    private LocalDate ngayBatDau;
    /** ngayKetThuc: ngày kết thúc (date). */
    private LocalDate ngayKetThuc;
    /** trangThai: trạng thái (varchar(50)). */
    private String trangThai;

    /** Constructor mặc định. */
    public DotThu() {}

    /** Constructor đầy đủ. */
    public DotThu(int id, String tenDot, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
        this.id = id;
        this.tenDot = tenDot;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
