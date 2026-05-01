package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import dao.UserDAO;
import models.User;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label lastLoginLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField departmentField;
    @FXML private Button uploadPhotoButton;
    @FXML private Button changePasswordButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    private UserDAO userDAO;
    private User currentUser;
    private File selectedPhotoFile;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        loadProfileData();
        setupEventHandlers();
        applyVisualEffects();
        updateStatus("Ready");
    }

    private void loadProfileData() {
        showLoadProgress(true);
        updateStatus("Loading profile data...");

        try {
            int userId = SessionManager.getInstance().getUserId();
            if (userId > 0) {
                currentUser = userDAO.findById(userId);
                if (currentUser != null) {
                    displayProfileData();
                    updateStatus("Profile loaded successfully");
                } else {
                    updateStatus("User not found");
                }
            } else {
                updateStatus("No user session found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("Error loading profile: " + e.getMessage());
            AlertUtil.showError("Load Error", "Failed to load profile data.");
        } finally {
            showLoadProgress(false);
        }
    }

    private void displayProfileData() {
        if (currentUser == null) return;

        usernameLabel.setText(currentUser.getUsername());
        roleLabel.setText(currentUser.getRole());
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());

        if (currentUser.getPhone() != null) {
            phoneField.setText(currentUser.getPhone());
        } else {
            phoneField.setText("");
        }

        departmentField.setText("System Administration");

        // Format last login
        if (currentUser.getLastLogin() != null) {
            lastLoginLabel.setText(currentUser.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            lastLoginLabel.setText("Never");
        }

        // Load profile image if exists
        loadProfileImage();
    }

    private void loadProfileImage() {
        if (currentUser != null && currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getProfileImage());
                if (imageFile.exists()) {
                    Image image = new Image("file:" + currentUser.getProfileImage());
                    if (!image.isError()) {
                        profileImageView.setImage(image);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setDefaultProfileImage();
    }

    private void setDefaultProfileImage() {
        try {
            // Try to load default avatar from resources
            InputStream defaultImageStream = getClass().getResourceAsStream("/images/default-avatar.png");
            if (defaultImageStream != null) {
                Image defaultImage = new Image(defaultImageStream);
                profileImageView.setImage(defaultImage);
            } else {
                // Create colored circle as fallback
                profileImageView.setImage(null);
                profileImageView.setStyle("-fx-background-color: #006400; -fx-background-radius: 75;");
            }
        } catch (Exception e) {
            profileImageView.setImage(null);
            profileImageView.setStyle("-fx-background-color: #006400; -fx-background-radius: 75;");
        }
    }

    private void setupEventHandlers() {
        uploadPhotoButton.setOnAction(event -> handleUploadPhoto());
        changePasswordButton.setOnAction(event -> handleChangePassword());
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());
    }

    private void applyVisualEffects() {
        // Drop shadow for buttons
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        uploadPhotoButton.setEffect(dropShadow);
        changePasswordButton.setEffect(dropShadow);
        saveButton.setEffect(dropShadow);
        cancelButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
    }

    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadPhotoButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedPhotoFile = selectedFile;
            try {
                Image image = new Image("file:" + selectedFile.getAbsolutePath());
                if (!image.isError()) {
                    profileImageView.setImage(image);
                    profileImageView.setStyle("");
                    updateStatus("Photo selected. Click Save to update.");
                } else {
                    AlertUtil.showError("Image Error", "Could not load selected image. Please try another file.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Image Error", "Could not load selected image: " + e.getMessage());
            }
        }
    }

    private void handleChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change your password");
        dialog.setContentText("Enter new password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword.length() < 4) {
                AlertUtil.showWarning("Password Error", "Password must be at least 4 characters.");
                return;
            }

            showOperationProgress(true);
            updateStatus("Changing password...");

            try {
                boolean success = userDAO.updatePassword(currentUser.getId(), newPassword);
                if (success) {
                    AlertUtil.showSuccess("Password Changed", "Your password has been updated successfully.");
                    updateStatus("Password changed successfully");
                } else {
                    AlertUtil.showError("Change Failed", "Failed to change password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "Error changing password: " + e.getMessage());
            } finally {
                hideOperationProgress();
            }
        });
    }

    private void handleSave() {
        if (!validateInputs()) return;

        showOperationProgress(true);
        updateProgress(0.2);
        updateStatus("Saving profile changes...");

        try {
            // Update user basic info fields
            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());

            updateProgress(0.4);

            // First update basic user info in database
            boolean success = userDAO.update(currentUser);
            updateProgress(0.6);

            if (success) {
                updateStatus("Basic info saved successfully");

                // Then save profile image if a new one was selected
                if (selectedPhotoFile != null) {
                    String imagePath = saveProfileImage(selectedPhotoFile);
                    if (imagePath != null) {
                        boolean imageUpdateSuccess = userDAO.updateProfileImage(currentUser.getId(), imagePath);
                        if (imageUpdateSuccess) {
                            currentUser.setProfileImage(imagePath);
                            updateStatus("Profile image saved");
                        } else {
                            updateStatus("Warning: Could not save profile image to database");
                        }
                    } else {
                        updateStatus("Warning: Could not save profile image file");
                    }
                }

                updateProgress(0.9);

                // Update session information
                SessionManager.getInstance().setFullName(currentUser.getFullName());
                SessionManager.getInstance().setEmail(currentUser.getEmail());

                updateProgress(1.0);
                AlertUtil.showSuccess("Profile Updated", "Your profile has been updated successfully.");
                updateStatus("Profile saved successfully");
                selectedPhotoFile = null;

                // Reload profile to refresh image
                loadProfileImage();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update profile information.");
                updateStatus("Failed to save profile");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "Error saving profile: " + e.getMessage());
            updateStatus("Error: " + e.getMessage());
        } finally {
            hideOperationProgress();
        }
    }

    private void handleCancel() {
        displayProfileData();
        selectedPhotoFile = null;
        updateStatus("Changes cancelled");
        AlertUtil.showInfo("Cancelled", "Changes were not saved.");
    }

    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(fullNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Full name is required.");
            fullNameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Email is required.");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private String saveProfileImage(File imageFile) {
        try {
            // Create directory if it doesn't exist
            String userHome = System.getProperty("user.home");
            String appDir = userHome + File.separator + "VehicleIdentificationSystem" + File.separator + "profiles" + File.separator;
            File dir = new File(appDir);

            System.out.println("Creating directory: " + appDir);

            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    System.err.println("Failed to create directory: " + appDir);
                    appDir = "profiles" + File.separator;
                    dir = new File(appDir);
                    dir.mkdirs();
                }
            }

            // Generate unique filename
            String extension = "";
            String fileName = imageFile.getName();
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot > 0) {
                extension = fileName.substring(lastDot);
            } else {
                extension = ".jpg";
            }

            String uniqueFileName = "admin_" + currentUser.getId() + "_" + System.currentTimeMillis() + extension;
            String destinationPath = appDir + uniqueFileName;
            File destinationFile = new File(destinationPath);

            // Copy file
            try (InputStream in = Files.newInputStream(imageFile.toPath())) {
                Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("Image saved to: " + destinationPath);

            if (destinationFile.exists() && destinationFile.length() > 0) {
                return destinationPath;
            } else {
                System.err.println("File was not created successfully");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Image Save Error", "Could not save profile image: " + e.getMessage());
            return null;
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        System.out.println("Status: " + message);
    }

    private void showLoadProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) {
            operationProgress.setProgress(progress);
        }
    }

    private void hideOperationProgress() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}