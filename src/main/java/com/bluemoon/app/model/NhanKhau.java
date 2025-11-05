package com.bluemoon.app.model;

import java.time.LocalDate;
import java.util.Objects;

public class NhanKhau {
    private int id;
    private int maHo;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String soCCCD;
    private String ngheNghiep;
    private String quanHeVoiChuHo;
    private String tinhTrang;
    private String ghiChu;

    public NhanKhau() { }

    public NhanKhau(int id, int maHo, String hoTen, LocalDate ngaySinh, String gioiTinh,
                    String soCCCD, String ngheNghiep, String quanHeVoiChuHo,
                    String tinhTrang, String ghiChu) {
        this.id = id;
        this.maHo = maHo;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soCCCD = soCCCD;
        this.ngheNghiep = ngheNghiep;
        this.quanHeVoiChuHo = quanHeVoiChuHo;
        this.tinhTrang = tinhTrang;
        this.ghiChu = ghiChu;
    }
//Getter and Setter 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMaHo() { return maHo; }
    public void setMaHo(int maHo) { this.maHo = maHo; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }
    public String getNgheNghiep() { return ngheNghiep; }
    public void setNgheNghiep(String ngheNghiep) { this.ngheNghiep = ngheNghiep; }
    public String getQuanHeVoiChuHo() { return quanHeVoiChuHo; }
    public void setQuanHeVoiChuHo(String quanHeVoiChuHo) { this.quanHeVoiChuHo = quanHeVoiChuHo; }
    public String getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(String tinhTrang) { this.tinhTrang = tinhTrang; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhanKhau)) return false;
        NhanKhau other = (NhanKhau) o;
        return id == other.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "NhanKhau{" +
                "id=" + id +
                ", maHo=" + maHo +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", soCCCD='" + soCCCD + '\'' +
                ", ngheNghiep='" + ngheNghiep + '\'' +
                ", quanHeVoiChuHo='" + quanHeVoiChuHo + '\'' +
                ", tinhTrang='" + tinhTrang + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
