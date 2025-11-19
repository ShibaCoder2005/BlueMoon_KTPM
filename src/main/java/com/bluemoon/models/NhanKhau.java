package com.bluemoon.models;

import java.time.LocalDate;

/**
 * NhanKhau: bảng nhân khẩu. Lưu thông tin cá nhân và tình trạng cư trú.
 */
public class NhanKhau {

    /** id: khóa chính. */
    private int id;
    /** maHo: mã hộ gia đình (int). */
    private int maHo;
    /** hoTen: họ tên (varchar(200)). */
    private String hoTen;
    /** ngaySinh: ngày sinh (date). */
    private LocalDate ngaySinh;
    /** gioiTinh: giới tính (varchar(10)). */
    private String gioiTinh;
    /** soCCCD: số căn cước (varchar(20)). */
    private String soCCCD;
    /** ngheNghiep: nghề nghiệp (varchar(100)). */
    private String ngheNghiep;
    /** quanHeVoiChuHo: quan hệ với chủ hộ (varchar(50)). */
    private String quanHeVoiChuHo;
    /** tinhTrang: tình trạng (enum)... lưu dạng String. */
    private String tinhTrang;

    /** Constructor mặc định. */
    public NhanKhau() {}

    /** Constructor đầy đủ. */
    public NhanKhau(int id, int maHo, String hoTen, LocalDate ngaySinh, String gioiTinh, String soCCCD,
                    String ngheNghiep, String quanHeVoiChuHo, String tinhTrang) {
        this.id = id;
        this.maHo = maHo;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soCCCD = soCCCD;
        this.ngheNghiep = ngheNghiep;
        this.quanHeVoiChuHo = quanHeVoiChuHo;
        this.tinhTrang = tinhTrang;
    }

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
}




