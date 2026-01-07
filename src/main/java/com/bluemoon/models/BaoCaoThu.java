package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho báo cáo thu (Revenue Report).
 * Chứa thông tin tổng hợp từ PhieuThu và ChiTietThu.
 */
public class BaoCaoThu {
    
    /** Mã hộ gia đình */
    private int maHo;
    
    /** Số phòng */
    private int soPhong;
    
    /** Tên chủ hộ */
    private String chuHo;
    
    /** Ngày thu */
    private LocalDateTime ngayThu;
    
    /** Tổng số tiền */
    private BigDecimal soTien;
    
    /** Nội dung (tên đợt thu hoặc khoản thu) */
    private String noiDung;
    
    /** Tên đợt thu */
    private String tenDot;
    
    /** Trạng thái phiếu thu */
    private String trangThai;
    
    /** Hình thức thu */
    private String hinhThucThu;

    public BaoCaoThu() {}

    public BaoCaoThu(int maHo, int soPhong, String chuHo, LocalDateTime ngayThu, 
                    BigDecimal soTien, String noiDung, String tenDot, 
                    String trangThai, String hinhThucThu) {
        this.maHo = maHo;
        this.soPhong = soPhong;
        this.chuHo = chuHo;
        this.ngayThu = ngayThu;
        this.soTien = soTien;
        this.noiDung = noiDung;
        this.tenDot = tenDot;
        this.trangThai = trangThai;
        this.hinhThucThu = hinhThucThu;
    }

    public int getMaHo() { return maHo; }
    public void setMaHo(int maHo) { this.maHo = maHo; }

    public int getSoPhong() { return soPhong; }
    public void setSoPhong(int soPhong) { this.soPhong = soPhong; }

    public String getChuHo() { return chuHo; }
    public void setChuHo(String chuHo) { this.chuHo = chuHo; }

    public LocalDateTime getNgayThu() { return ngayThu; }
    public void setNgayThu(LocalDateTime ngayThu) { this.ngayThu = ngayThu; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhThucThu() { return hinhThucThu; }
    public void setHinhThucThu(String hinhThucThu) { this.hinhThucThu = hinhThucThu; }
}

