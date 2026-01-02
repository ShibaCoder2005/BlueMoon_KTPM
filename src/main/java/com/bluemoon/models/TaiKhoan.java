package com.bluemoon.models;

/**
 * TaiKhoan: tài khoản người dùng hệ thống.
 */
public class TaiKhoan {

    /** id: khóa chính. */
    private int id;
    /** tenDangNhap: tên đăng nhập (varchar(100)). */
    private String tenDangNhap;
    /** matKhau: mật khẩu (varchar(255)). */
    private String matKhau;
    /** hoTen: họ tên (varchar(200)). */
    private String hoTen;
    /** vaiTro: vai trò (varchar(50)). */
    private String vaiTro;
    /** email: email (varchar(100)). */
    private String email;
    /** dienThoai: số điện thoại (varchar(20)). */
    private String dienThoai;
    /** trangThai: trạng thái (varchar(20) - "Hoạt động"/"Bị khóa"). */
    private String trangThai;
    /** ghiChu: ghi chú (TEXT). */
    private String ghiChu;

    /** Constructor mặc định. */
    public TaiKhoan() {}

    /** Constructor đầy đủ. */
    public TaiKhoan(int id, String tenDangNhap, String matKhau, String hoTen, String vaiTro, 
                    String email, String dienThoai, String trangThai, String ghiChu) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.email = email;
        this.dienThoai = dienThoai;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}



