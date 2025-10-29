package com.bluemoon.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Màn hình ghi nhận lịch sử nộp tiền.
 */
public class LichSuNopTienScreen {

    /** Hiển thị màn hình LichSuNopTien từ FXML. */
    public void display() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/bluemoon/views/fxml/LichSuNopTienScreen.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Lịch Sử Nộp Tiền");
        stage.setScene(new Scene(root));
        stage.show();
    }
}


