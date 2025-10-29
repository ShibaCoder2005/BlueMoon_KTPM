package com.bluemoon.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.bluemoon.models.TaiKhoan;

/**
 * Controller xử lý đăng nhập cho đối tượng TaiKhoan.
 */
public class TaiKhoanController {

    /** Ô nhập tên đăng nhập. */
    @FXML private TextField usernameField;
    /** Ô nhập mật khẩu. */
    @FXML private PasswordField passwordField;
    /** Nhãn hiển thị thông báo lỗi/thành công. */
    @FXML private Label messageLabel;

    /** Dịch vụ xác thực (được set từ bên ngoài). */
    private Object authService; // TODO: thay bằng AuthService khi có

    /**
     * Thiết lập dịch vụ xác thực.
     * @param authService service xác thực
     */
    public void setAuthService(Object authService) {
        this.authService = authService;
    }

    /**
     * Xử lý sự kiện nhấn nút Đăng nhập.
     * @param event sự kiện nút bấm
     */
    @FXML
    private void handleLoginAction(ActionEvent event) {
        // TODO: Implement logic - xác thực với authService, cập nhật UI/điều hướng
        String username = usernameField != null ? usernameField.getText() : null;
        String password = passwordField != null ? passwordField.getText() : null;
        // sử dụng authService để đăng nhập với TaiKhoan
    }
}
