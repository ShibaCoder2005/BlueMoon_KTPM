package com.bluemoon.app.model;

import java.time.LocalDateTime;

public class ThongKe {

    private int id;
    private LocalDateTime thoiGian; // SQL TIMESTAMP
    private String kieuThongKe;
    private String noiDung; // Có thể lưu mô tả/tham số thống kê
    private String ketQua; // Có thể lưu kết quả (ví dụ: file JSON, hoặc số tiền)

    // Constructor mặc định
    public ThongKe() {
    }

    // Constructor đầy đủ tham số
    public ThongKe(int id, LocalDateTime thoiGian, String kieuThongKe, String noiDung, String ketQua) {
        this.id = id;
        this.thoiGian = thoiGian;
        this.kieuThongKe = kieuThongKe;
        this.noiDung = noiDung;
        this.ketQua = ketQua;
    }

    // --- Getters và Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getKieuThongKe() {
        return kieuThongKe;
    }

    public void setKieuThongKe(String kieuThongKe) {
        this.kieuThongKe = kieuThongKe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }
}


