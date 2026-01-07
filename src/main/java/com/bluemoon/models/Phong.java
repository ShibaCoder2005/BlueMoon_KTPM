package com.bluemoon.models;

import java.math.BigDecimal;

/**
 * Phong: bảng phòng. Lưu thông tin về các phòng trong chung cư.
 */
public class Phong {
    
    /** soPhong: số phòng (primary key). */
    private int soPhong;
    
    /** dienTich: diện tích (DECIMAL(8,2)). */
    private BigDecimal dienTich;
    
    /** giaTien: giá tiền (DECIMAL(14,2)). */
    private BigDecimal giaTien;
    
    /** trangThai: trạng thái phòng (VARCHAR(20)) - 'Trong', 'DangO', 'BaoTri'. */
    private String trangThai;
    
    /** ghiChu: ghi chú (TEXT). */
    private String ghiChu;

    /** Constructor mặc định. */
    public Phong() {}

    /**
     * Constructor đầy đủ.
     */
    public Phong(int soPhong, BigDecimal dienTich, BigDecimal giaTien, String trangThai, String ghiChu) {
        this.soPhong = soPhong;
        this.dienTich = dienTich;
        this.giaTien = giaTien;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }
    
    /**
     * Constructor không có trangThai (backward compatibility).
     */
    public Phong(int soPhong, BigDecimal dienTich, BigDecimal giaTien, String ghiChu) {
        this(soPhong, dienTich, giaTien, "Trong", ghiChu);
    }

    public int getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(int soPhong) {
        this.soPhong = soPhong;
    }

    public BigDecimal getDienTich() {
        return dienTich;
    }

    public void setDienTich(BigDecimal dienTich) {
        this.dienTich = dienTich;
    }

    public BigDecimal getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(BigDecimal giaTien) {
        this.giaTien = giaTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

