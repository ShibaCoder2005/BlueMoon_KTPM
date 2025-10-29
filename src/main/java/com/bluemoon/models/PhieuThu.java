package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PhieuThu: phiếu thu tiền cho một hộ, thuộc một đợt thu.
 */
public class PhieuThu {

    /** id: khóa chính. */
    private int id;
    /** maHo: mã hộ (int). */
    private int maHo;
    /** maDot: mã đợt thu (int). */
    private int maDot;
    /** maTaiKhoan: mã tài khoản lập phiếu (int). */
    private int maTaiKhoan;
    /** ngayLap: thời điểm lập phiếu (datetime). */
    private LocalDateTime ngayLap;
    /** tongTien: tổng tiền (decimal(14,2)). */
    private BigDecimal tongTien;
    /** trangThai: trạng thái (varchar(50)). */
    private String trangThai;
    /** hinhThucThu: hình thức thu (varchar(50)). */
    private String hinhThucThu;

    /** Constructor mặc định. */
    public PhieuThu() {}

    /** Constructor đầy đủ. */
    public PhieuThu(int id, int maHo, int maDot, int maTaiKhoan, LocalDateTime ngayLap, BigDecimal tongTien,
                    String trangThai, String hinhThucThu) {
        this.id = id;
        this.maHo = maHo;
        this.maDot = maDot;
        this.maTaiKhoan = maTaiKhoan;
        this.ngayLap = ngayLap;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.hinhThucThu = hinhThucThu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMaHo() { return maHo; }
    public void setMaHo(int maHo) { this.maHo = maHo; }

    public int getMaDot() { return maDot; }
    public void setMaDot(int maDot) { this.maDot = maDot; }

    public int getMaTaiKhoan() { return maTaiKhoan; }
    public void setMaTaiKhoan(int maTaiKhoan) { this.maTaiKhoan = maTaiKhoan; }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhThucThu() { return hinhThucThu; }
    public void setHinhThucThu(String hinhThucThu) { this.hinhThucThu = hinhThucThu; }
}
