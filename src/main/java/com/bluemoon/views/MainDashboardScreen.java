package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình bảng điều khiển chính của ứng dụng.
 */
public class MainDashboardScreen {

    /** Hiển thị dashboard chính từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/MainDashboardScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("BlueMoon - Dashboard");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


