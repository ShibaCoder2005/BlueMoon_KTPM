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
    /** moTa: mô tả (text). */
    private String moTa;

    /** Constructor mặc định. */
    public DotThu() {}

    /** Constructor đầy đủ. */
    public DotThu(int id, String tenDot, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai, String moTa) {
        this.id = id;
        this.tenDot = tenDot;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
        this.moTa = moTa;
    }

    /** Constructor không có mô tả (backward compatibility). */
    public DotThu(int id, String tenDot, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
        this(id, tenDot, ngayBatDau, ngayKetThuc, trangThai, null);
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

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    /** Getter cho maDot (alias của id). */
    public int getMaDot() { return id; }
    /** Setter cho maDot (alias của id). */
    public void setMaDot(int maDot) { this.id = maDot; }
}
