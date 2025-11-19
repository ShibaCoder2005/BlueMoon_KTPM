package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * HoGiaDinh: đại diện bảng ho_khau trong hệ thống quản lý hộ gia đình.
 */
public class HoGiaDinh {

    /** id: khóa chính. */
    private int id;
    /** maHo: mã hộ (String). */
    private String maHo;
    /** soPhong: số phòng (int). */
    private int soPhong;
    /** dienTich: diện tích (BigDecimal). */
    private BigDecimal dienTich;
    /** maChuHo: mã nhân khẩu là chủ hộ (int). */
    private int maChuHo;
    /** ghiChu: ghi chú (String). */
    private String ghiChu;
    /** ngayTao: ngày tạo (LocalDate). */
    private LocalDate ngayTao;

    /** Constructor mặc định. */
    public HoGiaDinh() {
    }

    /**
     * Constructor đầy đủ.
     *
     * @param id        ID khóa chính
     * @param maHo      mã hộ
     * @param soPhong   số phòng
     * @param dienTich  diện tích
     * @param maChuHo   mã chủ hộ (ID của NhanKhau)
     * @param ghiChu    ghi chú
     * @param ngayTao   ngày tạo
     */
    public HoGiaDinh(int id, String maHo, int soPhong, BigDecimal dienTich, int maChuHo, String ghiChu, LocalDate ngayTao) {
        this.id = id;
        this.maHo = maHo;
        this.soPhong = soPhong;
        this.dienTich = dienTich;
        this.maChuHo = maChuHo;
        this.ghiChu = ghiChu;
        this.ngayTao = ngayTao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaHo() {
        return maHo;
    }

    public void setMaHo(String maHo) {
        this.maHo = maHo;
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

    public int getMaChuHo() {
        return maChuHo;
    }

    public void setMaChuHo(int maChuHo) {
        this.maChuHo = maChuHo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    /**
     * Helper field để lưu tên chủ hộ (cần resolve từ NhanKhauService).
     * Trả về null nếu chưa được set.
     */
    private String tenChuHo;

    /**
     * Lấy tên chủ hộ (được resolve từ maChuHo).
     *
     * @return tên chủ hộ
     */
    public String getTenChuHo() {
        return tenChuHo;
    }

    /**
     * Set tên chủ hộ (thường được resolve từ NhanKhauService).
     *
     * @param tenChuHo tên chủ hộ
     */
    public void setTenChuHo(String tenChuHo) {
        this.tenChuHo = tenChuHo;
    }
}


