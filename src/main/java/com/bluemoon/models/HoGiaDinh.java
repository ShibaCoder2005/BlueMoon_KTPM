package com.bluemoon.models;

import java.math.BigDecimal;

/**
 * HoGiaDinh: bảng hộ gia đình. Lưu thông tin số phòng, diện tích, chủ hộ và ghi chú.
 */
public class HoGiaDinh {

    /** id: khóa chính. */
    private int id;
    /** soPhong: số phòng (int). */
    private int soPhong;
    /** dienTich: diện tích (decimal(6,2)). */
    private BigDecimal dienTich;
    /** maChuHo: mã nhân khẩu là chủ hộ (int). */
    private int maChuHo;
    /** ghiChu: ghi chú (text). */
    private String ghiChu;
    /** thoiGianBatDauO: mô tả thời gian bắt đầu ở (varchar(255)). */
    private String thoiGianBatDauO;
    /** thoiGianKetThucO: mô tả thời gian kết thúc ở (varchar(255)). */
    private String thoiGianKetThucO;

    /** Constructor mặc định. */
    public HoGiaDinh() {}

    /** Constructor đầy đủ. */
    public HoGiaDinh(int id, int soPhong, BigDecimal dienTich, int maChuHo, String ghiChu,
                     String thoiGianBatDauO, String thoiGianKetThucO) {
        this.id = id;
        this.soPhong = soPhong;
        this.dienTich = dienTich;
        this.maChuHo = maChuHo;
        this.ghiChu = ghiChu;
        this.thoiGianBatDauO = thoiGianBatDauO;
        this.thoiGianKetThucO = thoiGianKetThucO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSoPhong() { return soPhong; }
    public void setSoPhong(int soPhong) { this.soPhong = soPhong; }

    public BigDecimal getDienTich() { return dienTich; }
    public void setDienTich(BigDecimal dienTich) { this.dienTich = dienTich; }

    public int getMaChuHo() { return maChuHo; }
    public void setMaChuHo(int maChuHo) { this.maChuHo = maChuHo; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getThoiGianBatDauO() { return thoiGianBatDauO; }
    public void setThoiGianBatDauO(String thoiGianBatDauO) { this.thoiGianBatDauO = thoiGianBatDauO; }

    public String getThoiGianKetThucO() { return thoiGianKetThucO; }
    public void setThoiGianKetThucO(String thoiGianKetThucO) { this.thoiGianKetThucO = thoiGianKetThucO; }
}


