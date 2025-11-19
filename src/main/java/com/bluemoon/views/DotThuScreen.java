package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình quản lý Đợt Thu.
 */
public class DotThuScreen {

    /** Hiển thị màn hình DotThu từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/DotThuScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Quản lý Đợt Thu");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


