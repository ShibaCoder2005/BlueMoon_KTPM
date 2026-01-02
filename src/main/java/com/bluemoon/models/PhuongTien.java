package com.bluemoon.models;

import java.time.LocalDate;

/**
 * PhuongTien: bảng phương tiện. Lưu loại xe, biển số, chủ xe và ngày đăng ký.
 */
public class PhuongTien {

    /** id: khóa chính. */
    private int id;
    /** maHo: mã hộ gia đình sở hữu. */
    private int maHo;
    /** loaiXe: loại xe (varchar(20)). */
    private String loaiXe;
    /** bienSo: biển số (varchar(50)). */
    private String bienSo;
    /** tenChuXe: tên chủ xe (varchar(100)). */
    private String tenChuXe;
    /** ngayDangKy: ngày đăng ký (date). */
    private LocalDate ngayDangKy;

    /** Constructor mặc định. */
    public PhuongTien() {}

    /**
     * Constructor đầy đủ.
     */
    public PhuongTien(int id, int maHo, String loaiXe, String bienSo, String tenChuXe, LocalDate ngayDangKy) {
        this.id = id;
        this.maHo = maHo;
        this.loaiXe = loaiXe;
        this.bienSo = bienSo;
        this.tenChuXe = tenChuXe;
        this.ngayDangKy = ngayDangKy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMaHo() { return maHo; }
    public void setMaHo(int maHo) { this.maHo = maHo; }

    public String getLoaiXe() { return loaiXe; }
    public void setLoaiXe(String loaiXe) { this.loaiXe = loaiXe; }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

    public String getTenChuXe() { return tenChuXe; }
    public void setTenChuXe(String tenChuXe) { this.tenChuXe = tenChuXe; }

    public LocalDate getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(LocalDate ngayDangKy) { this.ngayDangKy = ngayDangKy; }
}


