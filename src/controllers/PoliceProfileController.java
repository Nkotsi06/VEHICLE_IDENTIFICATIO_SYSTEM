package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
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
import dao.UserDAO;
import models.PoliceOfficer;
import models.OfficerActivityLog;
import models.RankChangeRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
    @FXML private Button changePasswordButton;  // Added
    @FXML private Button requestRankChangeButton;
    @FXML private Button refreshActivityButton;

    @FXML private TableView<OfficerActivityLog> activityTable;
    @FXML private TableColumn<OfficerActivityLog, String> activityTimeColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityActionColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityDescriptionColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityTargetColumn;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination activityPagination;

    private PoliceOfficerDAO policeOfficerDAO;
    private OfficerActivityLogDAO activityLogDAO;
    private RankChangeRequestDAO rankChangeRequestDAO;
    private UserDAO userDAO;
    private PoliceOfficer currentOfficer;
    private List<OfficerActivityLog> fullActivityList;
    private int currentPage = 0;
    private int pageSize = 20;
    private boolean isEditing = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String profileImageDirectory = "uploads/police_profiles/";

    @FXML
    public void initialize() {
        policeOfficerDAO = new PoliceOfficerDAO();
        activityLogDAO = new OfficerActivityLogDAO();
        rankChangeRequestDAO = new RankChangeRequestDAO();
        userDAO = new UserDAO();

        createImageDirectory();
        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();
        loadPoliceProfile();
        statusLabel.setText("Ready");
    }

    private void createImageDirectory() {
        try {
            Path path = Paths.get(profileImageDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setupPagination() {
        if (activityPagination != null) {
            activityPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullActivityList == null || fullActivityList.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullActivityList.size());
        if (start < fullActivityList.size()) {
            activityTable.getItems().setAll(fullActivityList.subList(start, end));
        }
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
        changePasswordButton.setOnAction(event -> handleChangePassword());  // Added
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
        changePasswordButton.setEffect(dropShadow);  // Added
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

        fullNameField.setText(currentOfficer.getFullName());
        emailField.setText(currentOfficer.getEmail());
        phoneField.setText(currentOfficer.getPhone());
        addressField.setText(currentOfficer.getAddress());
        stationField.setText(currentOfficer.getStationAssigned());
        supervisorField.setText(currentOfficer.getSupervisorName());

        rankComboBox.setValue(currentOfficer.getRank());
        departmentComboBox.setValue(currentOfficer.getDepartment());

        loadProfileImage();

        setEditMode(false);
    }

    private void loadProfileImage() {
        if (currentOfficer.getProfileImage() != null && !currentOfficer.getProfileImage().isEmpty()) {
            try {
                File imageFile = new File(currentOfficer.getProfileImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setDefaultProfileImage();
    }

    private void setDefaultProfileImage() {
        try {
            profileImageView.setImage(null);
            profileImageView.setStyle("-fx-background-color: #006400; -fx-background-radius: 80; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        } catch (Exception e) {
            profileImageView.setImage(null);
        }
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
        if (!validateInputs()) return;

        showProgress(true);
        statusLabel.setText("Saving profile...");

        try {
            currentOfficer.setFullName(fullNameField.getText().trim());
            currentOfficer.setEmail(emailField.getText().trim());
            currentOfficer.setPhone(phoneField.getText().trim());
            currentOfficer.setAddress(addressField.getText().trim());
            currentOfficer.setStationAssigned(stationField.getText().trim());

            String newRank = rankComboBox.getValue();
            String oldRank = currentOfficer.getRank();

            currentOfficer.setRank(newRank);
            currentOfficer.setDepartment(departmentComboBox.getValue());

            boolean success = policeOfficerDAO.update(currentOfficer);

            if (success) {
                logActivity("PROFILE_UPDATE", "Updated profile information");

                if (!oldRank.equals(newRank)) {
                    logActivity("RANK_CHANGE", "Rank changed from " + oldRank + " to " + newRank);
                }

                SessionManager.getInstance().setFullName(currentOfficer.getFullName());

                AlertUtil.showSuccess("Success", "Profile updated successfully!");
                statusLabel.setText("Profile saved successfully");
                setEditMode(false);
                displayProfileData();
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
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            showProgress(true);
            statusLabel.setText("Uploading photo...");

            try {
                // Generate unique filename
                String fileExtension = getFileExtension(selectedFile.getName());
                String newFileName = "police_" + currentOfficer.getId() + "_" + System.currentTimeMillis() + fileExtension;
                String destinationPath = profileImageDirectory + newFileName;
                File destinationFile = new File(destinationPath);

                // Copy file
                try (FileInputStream in = new FileInputStream(selectedFile)) {
                    Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // Update database with image path
                boolean success = policeOfficerDAO.updateProfileImage(currentOfficer.getId(), destinationPath);

                if (success) {
                    currentOfficer.setProfileImage(destinationPath);
                    Image image = new Image(destinationFile.toURI().toString());
                    profileImageView.setImage(image);
                    profileImageView.setStyle("");
                    logActivity("PHOTO_UPDATE", "Updated profile picture");
                    statusLabel.setText("Photo uploaded successfully");
                    AlertUtil.showSuccess("Photo Uploaded", "Profile picture updated successfully.");
                } else {
                    AlertUtil.showError("Upload Failed", "Could not save image path to database.");
                    statusLabel.setText("Upload failed");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Upload Failed", "Could not upload image: " + e.getMessage());
                statusLabel.setText("Upload error");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return ".jpg";
    }

    /**
     * ADDED: Change Password functionality
     */
    private void handleChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change Your Password");
        dialog.setContentText("Enter new password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword == null || newPassword.trim().isEmpty()) {
                AlertUtil.showWarning("Invalid Input", "Password cannot be empty.");
                return;
            }

            if (newPassword.length() < 6) {
                AlertUtil.showWarning("Invalid Input", "Password must be at least 6 characters.");
                return;
            }

            // Confirm password
            TextInputDialog confirmDialog = new TextInputDialog();
            confirmDialog.setTitle("Confirm Password");
            confirmDialog.setHeaderText("Confirm New Password");
            confirmDialog.setContentText("Re-enter new password:");

            confirmDialog.showAndWait().ifPresent(confirmedPassword -> {
                if (!newPassword.equals(confirmedPassword)) {
                    AlertUtil.showWarning("Password Mismatch", "Passwords do not match. Please try again.");
                    return;
                }

                showProgress(true);
                statusLabel.setText("Changing password...");

                try {
                    boolean success = userDAO.updatePassword(currentOfficer.getUserId(), newPassword);

                    if (success) {
                        logActivity("PASSWORD_CHANGE", "Changed account password");
                        AlertUtil.showSuccess("Password Changed", "Your password has been updated successfully.");
                        statusLabel.setText("Password changed successfully");
                    } else {
                        AlertUtil.showError("Change Failed", "Failed to change password.");
                        statusLabel.setText("Password change failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertUtil.showError("Error", "Failed to change password: " + e.getMessage());
                    statusLabel.setText("Error changing password");
                } finally {
                    hideProgressAfterDelay();
                }
            });
        });
    }

    private void handleRequestRankChange() {
        String newRank = rankComboBox.getValue();
        String currentRank = currentOfficer.getRank();

        if (newRank.equals(currentRank)) {
            AlertUtil.showWarning("No Change", "Please select a different rank.");
            return;
        }

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
        showProgress(true);
        statusLabel.setText("Loading activity log...");

        try {
            fullActivityList = activityLogDAO.findByOfficerId(currentOfficer.getId());
            int totalPages = (int) Math.ceil((double) fullActivityList.size() / pageSize);
            if (activityPagination != null) activityPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullActivityList.size() + " activity records");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading activity log");
        } finally {
            hideProgressAfterDelay();
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
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}