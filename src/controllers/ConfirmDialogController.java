package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDialogController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private Runnable onConfirm;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        confirmButton.setOnAction(event -> handleConfirm());
        cancelButton.setOnAction(event -> handleCancel());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    private void handleConfirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}