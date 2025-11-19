package com.bluemoon.models;

import java.math.BigDecimal;

/**
 * ChiTietThu: chi tiết các khoản trong một phiếu thu.
 */
public class ChiTietThu {

    /** id: khóa chính. */
    private int id;
    /** maPhieu: mã phiếu thu (int). */
    private int maPhieu;
    /** maKhoan: mã khoản thu (int). */
    private int maKhoan;
    /** soLuong: số lượng (decimal(10,2)). */
    private BigDecimal soLuong;
    /** donGia: đơn giá (decimal(12,2)). */
    private BigDecimal donGia;
    /** thanhTien: thành tiền (decimal(14,2)). */
    private BigDecimal thanhTien;

    /** Constructor mặc định. */
    public ChiTietThu() {}

    /** Constructor đầy đủ. */
    public ChiTietThu(int id, int maPhieu, int maKhoan, BigDecimal soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this.id = id;
        this.maPhieu = maPhieu;
        this.maKhoan = maKhoan;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMaPhieu() { return maPhieu; }
    public void setMaPhieu(int maPhieu) { this.maPhieu = maPhieu; }

    public int getMaKhoan() { return maKhoan; }
    public void setMaKhoan(int maKhoan) { this.maKhoan = maKhoan; }

    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    /** Helper field để lưu tên khoản thu (String) cho hiển thị. */
    private String tenKhoan;

    /**
     * Lấy tên khoản thu (String) - helper method cho hiển thị.
     *
     * @return tên khoản thu
     */
    public String getTenKhoan() {
        return tenKhoan;
    }

    /**
     * Set tên khoản thu (String) - helper method cho hiển thị.
     *
     * @param tenKhoan tên khoản thu
     */
    public void setTenKhoan(String tenKhoan) {
        this.tenKhoan = tenKhoan;
    }
}
