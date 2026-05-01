package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;

public class FooterController {

    @FXML private Label copyrightLabel;

    @FXML
    public void initialize() {
        if (copyrightLabel != null) {
            copyrightLabel.setText("© " + LocalDateTime.now().getYear() + " Vehicle Identification System");
        }
    }
}