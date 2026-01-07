package com.bluemoon.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
/**
 * HoGiaDinh: đại diện bảng HoGiaDinh trong hệ thống quản lý hộ gia đình.
 * Lưu ý: maChuHo tham chiếu đến NhanKhau.soCCCD (VARCHAR) thay vì NhanKhau.id
 */
public class HoGiaDinh {

    /** id: khóa chính. */
    private int id;
    /** soPhong: số phòng (int) - tham chiếu đến Phong.soPhong. */
    private int soPhong;
    /** maChuHo: mã chủ hộ (VARCHAR(20)) - tham chiếu đến NhanKhau.soCCCD. */
    private String maChuHo;
    /** trangThai: trạng thái hộ (VARCHAR(10)) - DEFAULT 'DangO'. */
    private String trangThai;
    /** ghiChu: ghi chú (TEXT). */
    private String ghiChu;
    /** thoiGianBatDauO: thời gian bắt đầu ở (TIMESTAMP). */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime thoiGianBatDauO;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime thoiGianKetThucO;

    /** Constructor mặc định. */
    public HoGiaDinh() {
    }

    /**
     * Constructor đầy đủ.
     *
     * @param id                  ID khóa chính
     * @param soPhong             số phòng (tham chiếu đến Phong.soPhong)
     * @param maChuHo             mã chủ hộ (soCCCD của NhanKhau)
     * @param trangThai           trạng thái hộ
     * @param ghiChu              ghi chú
     * @param thoiGianBatDauO     thời gian bắt đầu ở
     * @param thoiGianKetThucO    thời gian kết thúc ở (có thể null)
     */
    public HoGiaDinh(int id, int soPhong, String maChuHo, String trangThai, String ghiChu, 
                     LocalDateTime thoiGianBatDauO, LocalDateTime thoiGianKetThucO) {
        this.id = id;
        this.soPhong = soPhong;
        this.maChuHo = maChuHo;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.thoiGianBatDauO = thoiGianBatDauO;
        this.thoiGianKetThucO = thoiGianKetThucO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(int soPhong) {
        this.soPhong = soPhong;
    }

    public String getMaChuHo() {
        return maChuHo;
    }

    public void setMaChuHo(String maChuHo) {
        this.maChuHo = maChuHo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiGianBatDauO() {
        return thoiGianBatDauO;
    }

    public void setThoiGianBatDauO(LocalDateTime thoiGianBatDauO) {
        this.thoiGianBatDauO = thoiGianBatDauO;
    }

    public LocalDateTime getThoiGianKetThucO() {
        return thoiGianKetThucO;
    }

    public void setThoiGianKetThucO(LocalDateTime thoiGianKetThucO) {
        this.thoiGianKetThucO = thoiGianKetThucO;
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

    /**
     * Helper field để lưu diện tích phòng (lấy từ bảng Phong theo soPhong).
     * Trả về null nếu chưa được set.
     */
    private java.math.BigDecimal dienTich;

    /**
     * Lấy diện tích phòng (được resolve từ Phong theo soPhong).
     *
     * @return diện tích phòng
     */
    public java.math.BigDecimal getDienTich() {
        return dienTich;
    }

    /**
     * Set diện tích phòng (thường được resolve từ PhongService).
     *
     * @param dienTich diện tích phòng
     */
    public void setDienTich(java.math.BigDecimal dienTich) {
        this.dienTich = dienTich;
    }
}


