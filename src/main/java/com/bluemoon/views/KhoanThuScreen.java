package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình quản lý Khoản Thu.
 */
public class KhoanThuScreen {

    /** Hiển thị màn hình KhoanThu từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/KhoanThuScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Quản lý Khoản Thu");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


