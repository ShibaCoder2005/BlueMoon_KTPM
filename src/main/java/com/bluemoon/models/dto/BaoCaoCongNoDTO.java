package com.bluemoon.models.dto;

import java.math.BigDecimal;

/**
 * DTO cho báo cáo công nợ (Debt Report).
 * Chứa thông tin các hộ gia đình chưa thanh toán.
 */
public class BaoCaoCongNoDTO {
    
    /** Mã hộ gia đình */
    private int maHo;
    
    /** Số phòng */
    private int soPhong;
    
    /** Tên chủ hộ */
    private String chuHo;
    
    /** Tên đợt thu */
    private String tenDot;
    
    /** Mã đợt thu */
    private int maDot;
    
    /** Tổng số tiền cần đóng */
    private BigDecimal tongTien;
    
    /** Số tiền đã đóng */
    private BigDecimal daThu;
    
    /** Số tiền còn nợ */
    private BigDecimal conNo;
    
    /** Trạng thái (Chưa thanh toán / Đã thanh toán một phần) */
    private String trangThai;
    
    /** Ghi chú */
    private String ghiChu;

    public BaoCaoCongNoDTO() {}

    public BaoCaoCongNoDTO(int maHo, int soPhong, String chuHo, String tenDot, 
                          int maDot, BigDecimal tongTien, BigDecimal daThu, 
                          BigDecimal conNo, String trangThai, String ghiChu) {
        this.maHo = maHo;
        this.soPhong = soPhong;
        this.chuHo = chuHo;
        this.tenDot = tenDot;
        this.maDot = maDot;
        this.tongTien = tongTien;
        this.daThu = daThu;
        this.conNo = conNo;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public int getMaHo() { return maHo; }
    public void setMaHo(int maHo) { this.maHo = maHo; }

    public int getSoPhong() { return soPhong; }
    public void setSoPhong(int soPhong) { this.soPhong = soPhong; }

    public String getChuHo() { return chuHo; }
    public void setChuHo(String chuHo) { this.chuHo = chuHo; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public int getMaDot() { return maDot; }
    public void setMaDot(int maDot) { this.maDot = maDot; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public BigDecimal getDaThu() { return daThu; }
    public void setDaThu(BigDecimal daThu) { this.daThu = daThu; }

    public BigDecimal getConNo() { return conNo; }
    public void setConNo(BigDecimal conNo) { this.conNo = conNo; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

