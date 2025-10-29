package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình hiển thị thống kê.
 */
public class ThongKeScreen {

    /** Hiển thị màn hình ThongKe từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/ThongKeScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Thống Kê");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


