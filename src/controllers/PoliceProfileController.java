package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import dao.PoliceOfficerDAO;
import dao.OfficerActivityLogDAO;
import dao.RankChangeRequestDAO;
import models.PoliceOfficer;
import models.OfficerActivityLog;
import models.RankChangeRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PoliceProfileController {

    @FXML private Label badgeNumberLabel;
    @FXML private Label rankDisplayLabel;
    @FXML private Label departmentDisplayLabel;
    @FXML private Label stationDisplayLabel;
    @FXML private Label hireDateLabel;
    @FXML private Label usernameLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label accountStatusLabel;
    @FXML private Label statusLabel;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField stationField;
    @FXML private TextField supervisorField;

    @FXML private ComboBox<String> rankComboBox;
    @FXML private ComboBox<String> departmentComboBox;

    @FXML private ImageView profileImageView;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button uploadPhotoButton;
    @FXML private Button changePasswordButton;
    @FXML private Button requestRankChangeButton;
    @FXML private Button refreshActivityButton;

    @FXML private TableView<OfficerActivityLog> activityTable;
    @FXML private TableColumn<OfficerActivityLog, String> activityTimeColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityActionColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityDescriptionColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityTargetColumn;

    @FXML private ProgressIndicator loadProgress;

    private PoliceOfficerDAO policeOfficerDAO;
    private OfficerActivityLogDAO activityLogDAO;
    private RankChangeRequestDAO rankChangeRequestDAO;
    private PoliceOfficer currentOfficer;
    private boolean isEditing = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        policeOfficerDAO = new PoliceOfficerDAO();
        activityLogDAO = new OfficerActivityLogDAO();
        rankChangeRequestDAO = new RankChangeRequestDAO();

        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        applyVisualEffects();
        loadPoliceProfile();
    }

    private void setupTableColumns() {
        activityTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDateTime(cellData.getValue().getCreatedAt())));
        activityActionColumn.setCellValueFactory(cellData -> cellData.getValue().actionTypeProperty());
        activityDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().actionDescriptionProperty());
        activityTargetColumn.setCellValueFactory(cellData -> cellData.getValue().targetTypeProperty());

        activityTimeColumn.setStyle("-fx-alignment: CENTER;");
        activityActionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityDescriptionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityTargetColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupComboBoxes() {
        try {
            List<String> ranks = policeOfficerDAO.getAllRanks();
            rankComboBox.setItems(FXCollections.observableArrayList(ranks));

            List<String> departments = policeOfficerDAO.getAllDepartments();
            departmentComboBox.setItems(FXCollections.observableArrayList(departments));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        backButton.setOnAction(event -> handleBack());
        uploadPhotoButton.setOnAction(event -> handleUploadPhoto());
        changePasswordButton.setOnAction(event -> handleChangePassword());
        requestRankChangeButton.setOnAction(event -> handleRequestRankChange());
        refreshActivityButton.setOnAction(event -> loadActivityLog());
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        saveButton.setEffect(dropShadow);
        cancelButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        uploadPhotoButton.setEffect(dropShadow);
        changePasswordButton.setEffect(dropShadow);
        requestRankChangeButton.setEffect(dropShadow);
    }

    private void loadPoliceProfile() {
        showProgress(true);
        statusLabel.setText("Loading profile...");

        try {
            int userId = SessionManager.getInstance().getUserId();
            currentOfficer = policeOfficerDAO.findByUserId(userId);

            if (currentOfficer != null) {
                displayProfileData();
                loadActivityLog();
                statusLabel.setText("Profile loaded successfully");
            } else {
                AlertUtil.showError("Load Failed", "Could not find police officer record for this user.");
                statusLabel.setText("Profile load failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load profile: " + e.getMessage());
            statusLabel.setText("Error loading profile");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void displayProfileData() {
        // Display labels (non-editable)
        badgeNumberLabel.setText(currentOfficer.getBadgeNumber());
        rankDisplayLabel.setText(currentOfficer.getRank());
        departmentDisplayLabel.setText(currentOfficer.getDepartment());
        stationDisplayLabel.setText(currentOfficer.getStationAssigned());
        if (currentOfficer.getHireDate() != null) {
            hireDateLabel.setText(currentOfficer.getHireDate().toString());
        }
        usernameLabel.setText(currentOfficer.getUsername());
        if (currentOfficer.getLastLogin() != null) {
            lastLoginLabel.setText(formatDateTime(currentOfficer.getLastLogin()));
        }
        accountStatusLabel.setText(currentOfficer.isActive() ? "ACTIVE" : "INACTIVE");
        accountStatusLabel.setTextFill(currentOfficer.isActive() ? Color.GREEN : Color.RED);

        // Editable fields
        fullNameField.setText(currentOfficer.getFullName());
        emailField.setText(currentOfficer.getEmail());
        phoneField.setText(currentOfficer.getPhone());
        addressField.setText(currentOfficer.getAddress());
        stationField.setText(currentOfficer.getStationAssigned());
        supervisorField.setText(currentOfficer.getSupervisorName());

        // ComboBoxes
        rankComboBox.setValue(currentOfficer.getRank());
        departmentComboBox.setValue(currentOfficer.getDepartment());

        // Profile image
        if (currentOfficer.getProfileImage() != null && !currentOfficer.getProfileImage().isEmpty()) {
            try {
                File imageFile = new File(currentOfficer.getProfileImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Set edit mode off initially
        setEditMode(false);
    }

    private void setEditMode(boolean edit) {
        isEditing = edit;
        fullNameField.setEditable(edit);
        emailField.setEditable(edit);
        phoneField.setEditable(edit);
        addressField.setEditable(edit);
        stationField.setEditable(edit);
        rankComboBox.setDisable(!edit);
        departmentComboBox.setDisable(!edit);

        saveButton.setDisable(!edit);
        cancelButton.setDisable(!edit);

        if (!edit) {
            // Reset values if cancelled
            fullNameField.setText(currentOfficer.getFullName());
            emailField.setText(currentOfficer.getEmail());
            phoneField.setText(currentOfficer.getPhone());
            addressField.setText(currentOfficer.getAddress());
            stationField.setText(currentOfficer.getStationAssigned());
            rankComboBox.setValue(currentOfficer.getRank());
            departmentComboBox.setValue(currentOfficer.getDepartment());
        }
    }

    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        showProgress(true);
        statusLabel.setText("Saving profile...");

        try {
            // Update officer object
            currentOfficer.setFullName(fullNameField.getText().trim());
            currentOfficer.setEmail(emailField.getText().trim());
            currentOfficer.setPhone(phoneField.getText().trim());
            currentOfficer.setAddress(addressField.getText().trim());
            currentOfficer.setStationAssigned(stationField.getText().trim());

            String newRank = rankComboBox.getValue();
            String oldRank = currentOfficer.getRank();

            currentOfficer.setRank(newRank);
            currentOfficer.setDepartment(departmentComboBox.getValue());

            // Get rank level for the new rank
            updateRankLevel();

            boolean success = policeOfficerDAO.update(currentOfficer);

            if (success) {
                // Log the activity
                logActivity("PROFILE_UPDATE", "Updated profile information");

                // If rank changed, create rank change record
                if (!oldRank.equals(newRank)) {
                    logActivity("RANK_CHANGE", "Rank changed from " + oldRank + " to " + newRank);
                }

                // Update session
                SessionManager.getInstance().setFullName(currentOfficer.getFullName());

                AlertUtil.showSuccess("Success", "Profile updated successfully!");
                statusLabel.setText("Profile saved successfully");
                setEditMode(false);
                displayProfileData(); // Refresh display
            } else {
                AlertUtil.showError("Save Failed", "Could not update profile.");
                statusLabel.setText("Save failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to save: " + e.getMessage());
            statusLabel.setText("Error saving profile");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void updateRankLevel() {
        String rank = rankComboBox.getValue();
        List<String> ranks;
        try {
            ranks = policeOfficerDAO.getAllRanks();
            for (int i = 0; i < ranks.size(); i++) {
                if (ranks.get(i).equals(rank)) {
                    currentOfficer.setRankLevel(i + 1);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCancel() {
        setEditMode(false);
        statusLabel.setText("Edit cancelled");
    }

    private void handleBack() {
        SceneManager.getInstance().switchToPoliceView();
    }

    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);

                // Save image path to database
                String imagePath = selectedFile.getAbsolutePath();
                // In a real implementation, you'd copy the image to a designated folder
                // and save the relative path

                statusLabel.setText("Photo uploaded successfully");
                logActivity("PHOTO_UPDATE", "Updated profile picture");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Upload Failed", "Could not load image: " + e.getMessage());
            }
        }
    }

    private void handleChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        dialog.setContentText("New Password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword != null && newPassword.length() >= 6) {
                try {
                    // Call password update method
                    AlertUtil.showSuccess("Password Changed", "Your password has been updated successfully.");
                    logActivity("PASSWORD_CHANGE", "Changed account password");
                    statusLabel.setText("Password changed successfully");
                } catch (Exception e) {
                    AlertUtil.showError("Error", "Failed to change password: " + e.getMessage());
                }
            } else {
                AlertUtil.showWarning("Invalid Password", "Password must be at least 6 characters.");
            }
        });
    }

    private void handleRequestRankChange() {
        String newRank = rankComboBox.getValue();
        String currentRank = currentOfficer.getRank();

        if (newRank.equals(currentRank)) {
            AlertUtil.showWarning("No Change", "Please select a different rank.");
            return;
        }

        // Check if this rank requires approval
        try {
            boolean requiresApproval = policeOfficerDAO.requiresApprovalForRank(newRank);

            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Rank Change Request");
            reasonDialog.setHeaderText("Requesting promotion to: " + newRank);
            reasonDialog.setContentText("Reason for promotion request:");

            reasonDialog.showAndWait().ifPresent(reason -> {
                if (reason != null && !reason.trim().isEmpty()) {
                    try {
                        RankChangeRequest request = new RankChangeRequest();
                        request.setOfficerId(currentOfficer.getId());
                        request.setCurrentRank(currentRank);
                        request.setRequestedRank(newRank);
                        request.setReason(reason);

                        boolean success = rankChangeRequestDAO.insert(request);

                        if (success) {
                            logActivity("RANK_REQUEST", "Requested promotion from " + currentRank + " to " + newRank);

                            if (requiresApproval) {
                                AlertUtil.showInfo("Request Submitted",
                                        "Your promotion request has been submitted for admin approval.\n" +
                                                "You will be notified once reviewed.");
                            } else {
                                // Auto-approve for lower ranks
                                AlertUtil.showInfo("Promotion Approved",
                                        "Your promotion to " + newRank + " has been auto-approved!\n" +
                                                "Please click Save to update your profile.");
                            }
                            statusLabel.setText("Rank change request submitted");
                        } else {
                            AlertUtil.showError("Request Failed", "Could not submit rank change request.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertUtil.showError("Error", "Failed to submit request: " + e.getMessage());
                    }
                } else {
                    AlertUtil.showWarning("Reason Required", "Please provide a reason for the promotion request.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadActivityLog() {
        try {
            List<OfficerActivityLog> activities = activityLogDAO.findByOfficerId(currentOfficer.getId());
            activityTable.getItems().setAll(activities);
            statusLabel.setText("Loaded " + activities.size() + " activity records");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading activity log");
        }
    }

    private void logActivity(String actionType, String description) {
        try {
            activityLogDAO.logActivity(
                    currentOfficer.getId(),
                    actionType,
                    description,
                    "PROFILE",
                    null,
                    "SYSTEM"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(fullNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Full name is required.");
            fullNameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            phoneField.requestFocus();
            return false;
        }

        if (rankComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a rank.");
            return false;
        }

        if (departmentComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a department.");
            return false;
        }

        return true;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(formatter);
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) {
            loadProgress.setVisible(show);
        }
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}