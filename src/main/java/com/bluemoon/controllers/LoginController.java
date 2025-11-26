package com.bluemoon.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.bluemoon.models.TaiKhoan;
import com.bluemoon.services.AuthService;
import com.bluemoon.services.impl.AuthServiceImpl;

/**
 * Controller xử lý đăng nhập người dùng.
 * Validate inputs, authenticate via AuthService, và chuyển đến MainDashboard khi thành công.
 */
public class LoginController implements Initializable {

    @FXML
    private TextField tfUsername;

    @FXML
    private PasswordField pfPassword;

    private final AuthService authService = new AuthServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Có thể thêm initialization logic nếu cần
    }

    /**
     * Xử lý sự kiện đăng nhập.
     * Flow: Validate inputs -> Call AuthService.login -> Check result -> Switch scene hoặc show error.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            // Validate inputs
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(AlertType.WARNING, "Thiếu thông tin",
                        "Vui lòng nhập tên đăng nhập và mật khẩu.");
                return;
            }

            // Call AuthService.login
            TaiKhoan taiKhoan = authService.login(username, password);

            if (taiKhoan != null) {
                // Login successful - Switch to MainDashboard
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/MainDashboardScreen.fxml"));
                    Stage stage = (Stage) tfUsername.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("BlueMoon Manager - Dashboard");
                    stage.centerOnScreen();
                } catch (IOException e) {
                    showAlert(AlertType.ERROR, "Lỗi hệ thống",
                            "Không thể chuyển đến màn hình chính: " + e.getMessage());
                }
            } else {
                // Login failed - Show Error Alert
                showAlert(AlertType.ERROR, "Đăng nhập thất bại",
                        "Tên đăng nhập hoặc mật khẩu không đúng. Vui lòng thử lại.");
                // Clear password field for security
                pfPassword.clear();
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống",
                    "Đã xảy ra lỗi khi đăng nhập: " + e.getMessage());
        }
    }

    /**
     * Xử lý sự kiện hủy/đóng form đăng nhập.
     *
     * @param event sự kiện ActionEvent
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) tfUsername.getScene().getWindow();
        stage.close();
    }

    /**
     * Hiển thị Alert dialog.
     *
     * @param type    loại alert (ERROR, WARNING, INFORMATION, CONFIRMATION)
     * @param title   tiêu đề
     * @param content nội dung
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

