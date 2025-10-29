package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình tạo và xem Phiếu Thu.
 */
public class PhieuThuScreen {

    /** Hiển thị màn hình PhieuThu từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/PhieuThuScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Phiếu Thu");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


