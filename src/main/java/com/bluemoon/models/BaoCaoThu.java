package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho báo cáo thu (Revenue Report).
 * Chứa thông tin tổng hợp từ PhieuThu và ChiTietThu.
 */
public class BaoCaoThu {
    
    /** Mã hộ gia đình */
    private int maHo;
    
    /** Số phòng */
    private String soPhong;
    
    /** Tên chủ hộ */
    private String chuHo;
    
    /** Ngày thu */
    private LocalDate ngayThu;
    
    /** Tổng số tiền */
    private BigDecimal soTien;
    
    /** Nội dung (tên đợt thu hoặc khoản thu) */
    private String noiDung;
    
    /** Tên đợt thu */
    private String tenDot;
    
    /** Trạng thái phiếu thu */
    private String trangThai;
    
    /** Hình thức thu */
    private String hinhThuc;

    /** Người thu */
    private String nguoiThu;

    /** Mã thu */
    private int maPhieu;

    public BaoCaoThu() {}

    public BaoCaoThu(int maHo, String soPhong, String chuHo, LocalDate ngayThu, 
                    BigDecimal soTien, String noiDung, String tenDot, 
                    String trangThai, String hinhThuc, int maPhieu) {
        this.maHo = maHo;
        this.soPhong = soPhong;
        this.chuHo = chuHo;
        this.ngayThu = ngayThu;
        this.soTien = soTien;
        this.noiDung = noiDung;
        this.tenDot = tenDot;
        this.trangThai = trangThai;
        this.hinhThuc = hinhThuc;
        this.maPhieu = maPhieu;
    }

    public int getMaHo() { return maHo; }
    public void setMaHo(int maHo) { this.maHo = maHo; }

    public String getSoPhong() { return soPhong; }
    public void setSoPhong(String soPhong) { this.soPhong = soPhong; }

    public String getChuHo() { return chuHo; }
    public void setChuHo(String chuHo) { this.chuHo = chuHo; }

    public LocalDate getNgayThu() { return ngayThu; }
    public void setNgayThu(LocalDate ngayThu) { this.ngayThu = ngayThu; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhThuc() { return hinhThuc; }
    public void setHinhThuc(String hinhThuc) { this.hinhThuc = hinhThuc; }

    public String getNguoiThu() { return nguoiThu; }
    public void setNguoiThu(String nguoiThu) { this.nguoiThu = nguoiThu; }

    public int getMaPhieu() { return maPhieu; }
    public void setMaPhieu(int maPhieu) { this.maPhieu = maPhieu; }
}

