package com.bluemoon.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LichSuNopTien: các lần nộp tiền thuộc một phiếu thu.
 */
public class LichSuNopTien {

    /** id: khóa chính. */
    private int id;
    /** maPhieu: mã phiếu thu (int). */
    private int maPhieu;
    /** ngayNop: thời điểm nộp (datetime). */
    private LocalDateTime ngayNop;
    /** soTien: số tiền nộp (decimal(14,2)). */
    private BigDecimal soTien;
    /** phuongThuc: phương thức (varchar(50)). */
    private String phuongThuc;
    /** nguoiThu: id người thu (int). */
    private int nguoiThu;

    /** Constructor mặc định. */
    public LichSuNopTien() {}

    /** Constructor đầy đủ. */
    public LichSuNopTien(int id, int maPhieu, LocalDateTime ngayNop, BigDecimal soTien, String phuongThuc, int nguoiThu) {
        this.id = id;
        this.maPhieu = maPhieu;
        this.ngayNop = ngayNop;
        this.soTien = soTien;
        this.phuongThuc = phuongThuc;
        this.nguoiThu = nguoiThu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMaPhieu() { return maPhieu; }
    public void setMaPhieu(int maPhieu) { this.maPhieu = maPhieu; }

    public LocalDateTime getNgayNop() { return ngayNop; }
    public void setNgayNop(LocalDateTime ngayNop) { this.ngayNop = ngayNop; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public int getNguoiThu() { return nguoiThu; }
    public void setNguoiThu(int nguoiThu) { this.nguoiThu = nguoiThu; }
}

