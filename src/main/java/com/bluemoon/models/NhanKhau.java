package com.bluemoon.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * NhanKhau: bảng nhân khẩu (SCD Type 2). 
 * Lưu thông tin cá nhân và tình trạng cư trú với lịch sử thay đổi.
 */
public class NhanKhau {

    /** id: khóa chính. */
    private int id;
    /** maHo: mã hộ gia đình (int). */
    private int maHo;
    /** hoTen: họ tên (varchar(100)). */
    private String hoTen;
    /** ngaySinh: ngày sinh (date). */
    private LocalDate ngaySinh;
    /** gioiTinh: giới tính (varchar(10)). */
    private String gioiTinh;
    /** soCCCD: số căn cước (varchar(20) UNIQUE). */
    private String soCCCD;
    /** ngheNghiep: nghề nghiệp (varchar(20)). */
    private String ngheNghiep;
    /** quanHeVoiChuHo: quan hệ với chủ hộ (varchar(20)). */
    private String quanHeVoiChuHo;
    /** tinhTrang: tình trạng (varchar(10)) - 'CuTru', 'TamVang', 'TamTru', 'ChuyenDi'. */
    private String tinhTrang;
    /** ngayBatDau: ngày bắt đầu (TIMESTAMP) - cho SCD Type 2. */
    private LocalDateTime ngayBatDau;
    /** ngayKetThuc: ngày kết thúc (TIMESTAMP, có thể NULL) - cho SCD Type 2. */
    private LocalDateTime ngayKetThuc;
    /** hieuLuc: hiệu lực (BOOLEAN) - TRUE cho bản ghi hiện tại, FALSE cho bản ghi lịch sử. */
    private boolean hieuLuc;
    /** nguoiGhi: người ghi (int) - tham chiếu đến TaiKhoan.id. */
    private Integer nguoiGhi;
    /** ghiChu: ghi chú (TEXT). */
    private String ghiChu;

    /** Constructor mặc định. */
    public NhanKhau() {}

    /** Constructor đầy đủ. */
    public NhanKhau(int id, int maHo, String hoTen, LocalDate ngaySinh, String gioiTinh, String soCCCD,
                    String ngheNghiep, String quanHeVoiChuHo, String tinhTrang,
                    LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, boolean hieuLuc, 
                    Integer nguoiGhi, String ghiChu) {
        this.id = id;
        this.maHo = maHo;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soCCCD = soCCCD;
        this.ngheNghiep = ngheNghiep;
        this.quanHeVoiChuHo = quanHeVoiChuHo;
        this.tinhTrang = tinhTrang;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.hieuLuc = hieuLuc;
        this.nguoiGhi = nguoiGhi;
        this.ghiChu = ghiChu;
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

    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDateTime ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public boolean isHieuLuc() { return hieuLuc; }
    public void setHieuLuc(boolean hieuLuc) { this.hieuLuc = hieuLuc; }

    public Integer getNguoiGhi() { return nguoiGhi; }
    public void setNguoiGhi(Integer nguoiGhi) { this.nguoiGhi = nguoiGhi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}




