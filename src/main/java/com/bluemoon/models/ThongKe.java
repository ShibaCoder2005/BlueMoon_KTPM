package com.bluemoon.models;

import java.time.LocalDateTime;

/**
 * ThongKe: lưu bản ghi thống kê theo thời điểm và kiểu thống kê.
 */
public class ThongKe {

    /** id: khóa chính. */
    private int id;
    /** thoiGian: thời điểm thống kê (datetime). */
    private LocalDateTime thoiGian;
    /** kieuThongKe: kiểu thống kê (varchar(100)). */
    private String kieuThongKe;
    /** noiDung: nội dung (text). */
    private String noiDung;
    /** ketQua: kết quả (text). */
    private String ketQua;

    /** Constructor mặc định. */
    public ThongKe() {}

    /** Constructor đầy đủ. */
    public ThongKe(int id, LocalDateTime thoiGian, String kieuThongKe, String noiDung, String ketQua) {
        this.id = id;
        this.thoiGian = thoiGian;
        this.kieuThongKe = kieuThongKe;
        this.noiDung = noiDung;
        this.ketQua = ketQua;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getKieuThongKe() { return kieuThongKe; }
    public void setKieuThongKe(String kieuThongKe) { this.kieuThongKe = kieuThongKe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getKetQua() { return ketQua; }
    public void setKetQua(String ketQua) { this.ketQua = ketQua; }
}

