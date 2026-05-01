package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;

public class PoliceViewController {

    @FXML private Label welcomeLabel;
    @FXML private Label officerNameLabel;
    @FXML private Label badgeNumberLabel;
    @FXML private Label currentTimeLabel;

    @FXML private TabPane mainTabPane;

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

    @FXML
    public void initialize() {
        String fullName = SessionManager.getInstance().getFullName();
        welcomeLabel.setText("Welcome, Officer " + (fullName != null ? fullName : "User"));
        officerNameLabel.setText("Officer: " + (fullName != null ? fullName : "Unknown"));
        badgeNumberLabel.setText("Badge: " + (SessionManager.getInstance().getUserId()));

        updateTime();

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateTime())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

        setupButtonHandlers();
    }

    private void updateTime() {
        currentTimeLabel.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private void setupButtonHandlers() {
        searchVehicleButton.setOnAction(e -> SceneManager.getInstance().switchToSearchView());
        reportStolenButton.setOnAction(e -> SceneManager.getInstance().switchToStolenVehicleView());
        addViolationButton.setOnAction(e -> SceneManager.getInstance().switchToViolationView());
        issueWarrantButton.setOnAction(e -> SceneManager.getInstance().switchToWarrantView());
        generateBOLOButton.setOnAction(e -> SceneManager.getInstance().switchToBOLOView());
        viewStolenButton.setOnAction(e -> SceneManager.getInstance().switchToStolenVehicleView());
        viewViolationsButton.setOnAction(e -> SceneManager.getInstance().switchToViolationView());
        viewWarrantsButton.setOnAction(e -> SceneManager.getInstance().switchToWarrantView());
        reconstructMovementButton.setOnAction(e -> SceneManager.getInstance().switchToVehicleReconstructionView());
        checkExpiredDocsButton.setOnAction(e -> SceneManager.getInstance().switchToExpiredDocumentView());
        geofencingButton.setOnAction(e -> SceneManager.getInstance().switchToGeofencingView());
        trafficCameraButton.setOnAction(e -> SceneManager.getInstance().switchToTrafficCameraView());
        mobilePatrolButton.setOnAction(e -> SceneManager.getInstance().switchToMobilePatrolView());
        logoutButton.setOnAction(e -> handleLogout());
    }

    private void handleLogout() {
        boolean confirmed = AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirmed) {
            SessionManager.getInstance().clearSession();
            SceneManager.getInstance().switchToLogin();
        }
    }
}