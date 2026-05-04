package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;

/**
 * Controller for Police Dashboard View
 * Provides quick access buttons for all police module functions
 * Displays officer information and current time
 */
public class PoliceViewController {

    // ============================================
    // DISPLAY LABELS
    // ============================================
    @FXML private Label welcomeLabel;
    @FXML private Label officerNameLabel;
    @FXML private Label badgeNumberLabel;
    @FXML private Label currentTimeLabel;

    // ============================================
    // UI CONTAINERS
    // ============================================
    @FXML private TabPane mainTabPane;

    // ============================================
    // QUICK ACCESS BUTTONS
    // ============================================
    @FXML private Button searchVehicleButton;
    @FXML private Button reportStolenButton;
    @FXML private Button addViolationButton;
    @FXML private Button issueWarrantButton;
    @FXML private Button generateBOLOButton;
    @FXML private Button viewStolenButton;
    @FXML private Button viewViolationsButton;
    @FXML private Button viewWarrantsButton;
    @FXML private Button reconstructMovementButton;
    @FXML private Button checkExpiredDocsButton;
    @FXML private Button geofencingButton;
    @FXML private Button trafficCameraButton;
    @FXML private Button mobilePatrolButton;
    @FXML private Button logoutButton;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the police view controller
     * Sets up officer information, current time display, and button handlers
     */
    @FXML
    public void initialize() {
        // Set officer information from session
        String fullName = SessionManager.getInstance().getFullName();
        welcomeLabel.setText("Welcome, Officer " + (fullName != null ? fullName : "User"));
        officerNameLabel.setText("Officer: " + (fullName != null ? fullName : "Unknown"));
        badgeNumberLabel.setText("Badge: " + (SessionManager.getInstance().getUserId()));

        // Start clock update
        updateTime();
        startClockTimer();

        setupButtonHandlers();
    }

    /**
     * Updates the current time display
     */
    private void updateTime() {
        currentTimeLabel.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * Starts a timer to update the clock every second
     */
    private void startClockTimer() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateTime())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Sets up button click handlers for all quick access buttons
     */
    private void setupButtonHandlers() {
        // Vehicle Search
        searchVehicleButton.setOnAction(e -> SceneManager.getInstance().switchToSearchView());

        // Stolen Vehicle Management
        reportStolenButton.setOnAction(e -> SceneManager.getInstance().switchToStolenVehicleView());
        viewStolenButton.setOnAction(e -> SceneManager.getInstance().switchToStolenVehicleView());

        // Violation Management
        addViolationButton.setOnAction(e -> SceneManager.getInstance().switchToViolationView());
        viewViolationsButton.setOnAction(e -> SceneManager.getInstance().switchToViolationView());

        // Warrant Management
        issueWarrantButton.setOnAction(e -> SceneManager.getInstance().switchToWarrantView());
        viewWarrantsButton.setOnAction(e -> SceneManager.getInstance().switchToWarrantView());

        // BOLO Alerts
        generateBOLOButton.setOnAction(e -> SceneManager.getInstance().switchToBOLOView());

        // Vehicle Tracking & Reconstruction
        reconstructMovementButton.setOnAction(e -> SceneManager.getInstance().switchToVehicleReconstructionView());

        // Document Management
        checkExpiredDocsButton.setOnAction(e -> SceneManager.getInstance().switchToExpiredDocumentView());

        // Geofencing
        geofencingButton.setOnAction(e -> SceneManager.getInstance().switchToGeofencingView());

        // Traffic Camera Integration
        trafficCameraButton.setOnAction(e -> SceneManager.getInstance().switchToTrafficCameraView());

        // Mobile Patrol
        mobilePatrolButton.setOnAction(e -> SceneManager.getInstance().switchToMobilePatrolView());

        // Logout
        logoutButton.setOnAction(e -> handleLogout());
    }

    /**
     * Handles user logout with confirmation
     */
    private void handleLogout() {
        boolean confirmed = AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirmed) {
            SessionManager.getInstance().clearSession();
            SceneManager.getInstance().switchToLogin();
        }
    }
}