package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình đăng nhập. Chịu trách nhiệm nạp FXML và hiển thị Stage.
 */
public class LoginScreen {

    /**
     * Nạp giao diện từ FXML và hiển thị trong một Stage mới.
     * @throws IOException nếu không tìm thấy hoặc nạp FXML thất bại
     */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/LoginScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Login");
        stage.setScene(new Scene(root));
        stage.show();
    }
}