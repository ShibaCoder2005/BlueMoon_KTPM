package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình quản lý Nhân Khẩu.
 */
public class NhanKhauScreen {

    /** Hiển thị màn hình NhanKhau từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/NhanKhauScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Quản lý Nhân Khẩu");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


