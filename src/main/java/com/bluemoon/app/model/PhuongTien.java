package com.bluemoon.app.model;

import java.time.LocalDate;

public class PhuongTien {

    private int id;
    private int maHo;
    private String loaiXe;
    private String bienSo;
    private String tenChuXe;
    private LocalDate ngayDangKy; // SQL DATE
    private String ghiChu;

    // Constructor mặc định
    public PhuongTien() {
    }

    // Constructor đầy đủ tham số
    public PhuongTien(int id, int maHo, String loaiXe, String bienSo, String tenChuXe, LocalDate ngayDangKy, String ghiChu) {
        this.id = id;
        this.maHo = maHo;
        this.loaiXe = loaiXe;
        this.bienSo = bienSo;
        this.tenChuXe = tenChuXe;
        this.ngayDangKy = ngayDangKy;
        this.ghiChu = ghiChu;
    }

    // --- Getters và Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaHo() {
        return maHo;
    }

    public void setMaHo(int maHo) {
        this.maHo = maHo;
    }

    public String getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(String loaiXe) {
        this.loaiXe = loaiXe;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public String getTenChuXe() {
        return tenChuXe;
    }

    public void setTenChuXe(String tenChuXe) {
        this.tenChuXe = tenChuXe;
    }

    public LocalDate getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDate ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}


