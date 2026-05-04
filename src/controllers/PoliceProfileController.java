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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Police Officer Profile Management
 * Handles viewing and editing police officer personal and professional information
 * Includes rank change requests, password management, and activity logging
 */
public class PoliceProfileController {

    // ============================================
    // FXML UI COMPONENTS - DISPLAY LABELS
    // ============================================

    @FXML private Label badgeNumberLabel;
    @FXML private Label rankDisplayLabel;
    @FXML private Label departmentDisplayLabel;
    @FXML private Label stationDisplayLabel;
    @FXML private Label hireDateLabel;
    @FXML private Label usernameLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label accountStatusLabel;
    @FXML private Label statusLabel;

    // ============================================
    // FORM FIELDS
    // ============================================

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField stationField;
    @FXML private TextField supervisorField;

    // ============================================
    // COMBO BOXES
    // ============================================

    @FXML private ComboBox<String> rankComboBox;
    @FXML private ComboBox<String> departmentComboBox;

    // ============================================
    // IMAGE COMPONENTS
    // ============================================

    @FXML private ImageView profileImageView;

    // ============================================
    // BUTTONS
    // ============================================

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button uploadPhotoButton;
    @FXML private Button changePasswordButton;
    @FXML private Button requestRankChangeButton;
    @FXML private Button refreshActivityButton;

    // ============================================
    // ACTIVITY TABLE
    // ============================================

    @FXML private TableView<OfficerActivityLog> activityTable;
    @FXML private TableColumn<OfficerActivityLog, String> activityTimeColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityActionColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityDescriptionColumn;
    @FXML private TableColumn<OfficerActivityLog, String> activityTargetColumn;

    // ============================================
    // PROGRESS INDICATORS
    // ============================================

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination activityPagination;

    // ============================================
    // DAO INSTANCES
    // ============================================

    private PoliceOfficerDAO policeOfficerDAO;
    private OfficerActivityLogDAO activityLogDAO;
    private RankChangeRequestDAO rankChangeRequestDAO;
    private UserDAO userDAO;

    // ============================================
    // DATA MODELS
    // ============================================

    private PoliceOfficer currentOfficer;
    private List<OfficerActivityLog> fullActivityList;
    private int currentPage = 0;
    private int pageSize = 20;
    private boolean isEditing = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String profileImageDirectory = "uploads/police_profiles/";

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the police profile controller
     * Sets up DAOs, table columns, loads profile data, and configures UI
     */
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

    /**
     * Creates directory for storing profile images if it doesn't exist
     */
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

    /**
     * Configures activity table columns with cell value factories
     */
    private void setupTableColumns() {
        activityTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDateTime(cellData.getValue().getCreatedAt())));
        activityActionColumn.setCellValueFactory(cellData -> cellData.getValue().actionTypeProperty());
        activityDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().actionDescriptionProperty());
        activityTargetColumn.setCellValueFactory(cellData -> cellData.getValue().targetTypeProperty());

        // Center align columns
        activityTimeColumn.setStyle("-fx-alignment: CENTER;");
        activityActionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityDescriptionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityTargetColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Configures pagination for activity log table
     */
    private void setupPagination() {
        if (activityPagination != null) {
            activityPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    /**
     * Updates activity table to show current page
     */
    private void updateTablePage() {
        if (fullActivityList == null || fullActivityList.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullActivityList.size());
        if (start < fullActivityList.size()) {
            activityTable.getItems().setAll(fullActivityList.subList(start, end));
        }
    }

    /**
     * Loads available ranks and departments into combo boxes
     */
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

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        backButton.setOnAction(event -> handleBack());
        uploadPhotoButton.setOnAction(event -> handleUploadPhoto());
        changePasswordButton.setOnAction(event -> handleChangePassword());
        requestRankChangeButton.setOnAction(event -> handleRequestRankChange());
        refreshActivityButton.setOnAction(event -> loadActivityLog());
    }

    /**
     * Applies visual effects to buttons
     */
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

    // ============================================
    // PROFILE LOADING METHODS
    // ============================================

    /**
     * Loads police officer profile from database for current user
     */
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
                // Disable editing features when no officer record exists
                setEditMode(false);
                disableProfileFeatures();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load profile: " + e.getMessage());
            statusLabel.setText("Error loading profile");
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Disables profile features when no officer record exists
     */
    private void disableProfileFeatures() {
        uploadPhotoButton.setDisable(true);
        changePasswordButton.setDisable(true);
        requestRankChangeButton.setDisable(true);
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        refreshActivityButton.setDisable(true);

        // Clear form fields
        fullNameField.setEditable(false);
        emailField.setEditable(false);
        phoneField.setEditable(false);
        addressField.setEditable(false);
        stationField.setEditable(false);
        rankComboBox.setDisable(true);
        departmentComboBox.setDisable(true);

        // Set placeholder text
        badgeNumberLabel.setText("N/A");
        rankDisplayLabel.setText("N/A");
        departmentDisplayLabel.setText("N/A");
        stationDisplayLabel.setText("N/A");
        usernameLabel.setText(SessionManager.getInstance().getUsername());
    }

    /**
     * Displays profile data in the UI
     */
    private void displayProfileData() {
        if (currentOfficer == null) return;

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

        // Populate form fields
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

    /**
     * Loads profile image from disk or sets default
     */
    private void loadProfileImage() {
        if (currentOfficer == null) {
            setDefaultProfileImage();
            return;
        }

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

    /**
     * Sets default profile image when no image is available
     */
    private void setDefaultProfileImage() {
        try {
            profileImageView.setImage(null);
            profileImageView.setStyle("-fx-background-color: #006400; -fx-background-radius: 80; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        } catch (Exception e) {
            profileImageView.setImage(null);
        }
    }

    /**
     * Toggles edit mode for the profile form
     * @param edit true to enable editing, false to disable
     */
    private void setEditMode(boolean edit) {
        if (currentOfficer == null) {
            edit = false;
        }

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

        if (!edit && currentOfficer != null) {
            fullNameField.setText(currentOfficer.getFullName());
            emailField.setText(currentOfficer.getEmail());
            phoneField.setText(currentOfficer.getPhone());
            addressField.setText(currentOfficer.getAddress());
            stationField.setText(currentOfficer.getStationAssigned());
            rankComboBox.setValue(currentOfficer.getRank());
            departmentComboBox.setValue(currentOfficer.getDepartment());
        }
    }

    // ============================================
    // PROFILE UPDATE METHODS
    // ============================================

    /**
     * Handles saving profile changes to database
     */
    private void handleSave() {
        if (currentOfficer == null) {
            AlertUtil.showError("Error", "No officer profile loaded. Please refresh and try again.");
            return;
        }

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

                // Log rank change if applicable
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

    /**
     * Handles cancelling edit mode and reverting changes
     */
    private void handleCancel() {
        setEditMode(false);
        statusLabel.setText("Edit cancelled");
    }

    /**
     * Navigates back to police dashboard
     */
    private void handleBack() {
        SceneManager.getInstance().switchToPoliceView();
    }

    // ============================================
    // PROFILE PICTURE HANDLING
    // ============================================

    /**
     * Handles uploading a new profile picture
     * FIXED: Added null check for currentOfficer
     */
    private void handleUploadPhoto() {
        // Check if currentOfficer is null before proceeding
        if (currentOfficer == null) {
            AlertUtil.showError("Profile Not Loaded",
                    "Your police officer profile could not be loaded. Please refresh the page and try again.\n\n" +
                            "If this issue persists, please contact your system administrator.");
            statusLabel.setText("Cannot upload photo - profile not loaded");
            return;
        }

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

                // Create directory if not exists
                Path path = Paths.get(profileImageDirectory);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                // Copy file to destination
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
                statusLabel.setText("Upload error: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    /**
     * Extracts file extension from filename
     * @param fileName The full filename
     * @return File extension including dot
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return ".jpg";
    }

    // ============================================
    // PASSWORD MANAGEMENT
    // ============================================

    /**
     * Handles changing user password
     * Prompts for new password and confirmation
     * FIXED: Added null check for currentOfficer
     */
    private void handleChangePassword() {
        if (currentOfficer == null) {
            AlertUtil.showError("Profile Not Loaded",
                    "Your police officer profile could not be loaded. Please refresh the page and try again.");
            return;
        }

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

    // ============================================
    // RANK CHANGE REQUEST
    // ============================================

    /**
     * Handles requesting a rank change/promotion
     * Submits request for admin approval
     * FIXED: Added null check for currentOfficer
     */
    private void handleRequestRankChange() {
        if (currentOfficer == null) {
            AlertUtil.showError("Profile Not Loaded",
                    "Your police officer profile could not be loaded. Please refresh the page and try again.");
            return;
        }

        String newRank = rankComboBox.getValue();
        String currentRank = currentOfficer.getRank();

        if (newRank == null || newRank.equals(currentRank)) {
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
            AlertUtil.showError("Error", "Failed to check rank requirements: " + e.getMessage());
        }
    }

    // ============================================
    // ACTIVITY LOG METHODS
    // ============================================

    /**
     * Loads activity log for the current officer
     */
    private void loadActivityLog() {
        if (currentOfficer == null) {
            activityTable.getItems().clear();
            statusLabel.setText("No officer profile loaded");
            return;
        }

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

    /**
     * Logs an activity entry for the current officer
     * @param actionType Type of action performed
     * @param description Detailed description of the action
     */
    private void logActivity(String actionType, String description) {
        if (currentOfficer == null) return;

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

    // ============================================
    // VALIDATION METHODS
    // ============================================

    /**
     * Validates all form inputs before saving
     * @return true if all inputs are valid
     */
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

    /**
     * Formats LocalDateTime for display
     * @param dateTime The date time to format
     * @return Formatted date-time string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(formatter);
    }

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides progress indicators
     * @param show true to show, false to hide
     */
    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    /**
     * Hides progress indicators after a short delay
     */
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