package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình quản lý Hộ Gia Đình.
 */
public class HoGiaDinhScreen {

    /** Hiển thị màn hình HoGiaDinh từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/HoGiaDinhScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Quản lý Hộ Gia Đình");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


