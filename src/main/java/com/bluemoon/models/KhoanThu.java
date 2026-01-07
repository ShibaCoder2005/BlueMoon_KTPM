package com.bluemoon.models;

import java.math.BigDecimal;

/**
 * KhoanThu: danh mục khoản thu.
 */
public class KhoanThu {

    /** id: khóa chính. */
    private int id;
    /** tenKhoan: tên khoản thu (varchar(200)). */
    private String tenKhoan;
    /** loai: loại (varchar(50)). */
    private String loai;
    /** donGia: đơn giá (decimal(12,2)). */
    private BigDecimal donGia;
    /** donViTinh: đơn vị tính (varchar(50)). */
    private String donViTinh;
    /** tinhTheo: tính theo (varchar(50)). */
    private String tinhTheo;
    /** batBuoc: có bắt buộc hay không (boolean). */
    private boolean batBuoc;
    /** moTa: mô tả (text). */
    private String moTa;
    /** loaiKhoanThu: loại khoản thu (int - 0: Bắt buộc, 1: Tự nguyện/Đóng góp). */
    private int loaiKhoanThu;

    /** Constructor mặc định. */
    public KhoanThu() {}

    /** Constructor đầy đủ. */
    public KhoanThu(int id, String tenKhoan, String loai, BigDecimal donGia, String donViTinh,
                    String tinhTheo, boolean batBuoc, String moTa) {
        this.id = id;
        this.tenKhoan = tenKhoan;
        this.loai = loai;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.tinhTheo = tinhTheo;
        this.batBuoc = batBuoc;
        this.moTa = moTa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenKhoan() { return tenKhoan; }
    public void setTenKhoan(String tenKhoan) { this.tenKhoan = tenKhoan; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public String getTinhTheo() { return tinhTheo; }
    public void setTinhTheo(String tinhTheo) { this.tinhTheo = tinhTheo; }

    public boolean isBatBuoc() { return batBuoc; }
    public void setBatBuoc(boolean batBuoc) { this.batBuoc = batBuoc; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public int getLoaiKhoanThu() { return loaiKhoanThu; }
    public void setLoaiKhoanThu(int loaiKhoanThu) { this.loaiKhoanThu = loaiKhoanThu; }

    /** Getter cho maKhoanThu (alias của id). */
    public int getMaKhoanThu() { return id; }
    /** Setter cho maKhoanThu (alias của id). */
    public void setMaKhoanThu(int maKhoanThu) { this.id = maKhoanThu; }

    /** Getter cho tenKhoanThu (alias của tenKhoan). */
    public String getTenKhoanThu() { return tenKhoan; }
    /** Setter cho tenKhoanThu (alias của tenKhoan). */
    public void setTenKhoanThu(String tenKhoanThu) { this.tenKhoan = tenKhoanThu; }

    /** Getter cho soTien (alias của donGia). */
    public BigDecimal getSoTien() { return donGia; }
    /** Setter cho soTien (alias của donGia). */
    public void setSoTien(BigDecimal soTien) { this.donGia = soTien; }
}
