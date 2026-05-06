package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import utils.SceneManager;
import utils.SessionManager;
import dao.UserDAO;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.WorkshopDAO;
import dao.PoliceOfficerDAO;

/**
 * Controller for Welcome/Splash Screen
 * Displays when application starts - explains system purpose with visual effects
 */
public class WelcomeController {

    @FXML private ImageView logoImageView;
    @FXML private Text titleText;
    @FXML private ProgressIndicator loadingProgress;
    @FXML private Label loadingLabel;
    @FXML private Button continueButton;

    // Statistics Labels
    @FXML private Label totalVehiclesLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalWorkshopsLabel;
    @FXML private Label totalPoliceLabel;

    private UserDAO userDAO;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private WorkshopDAO workshopDAO;
    private PoliceOfficerDAO policeOfficerDAO;

    @FXML
    public void initialize() {
        // Initialize DAOs
        userDAO = new UserDAO();
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        workshopDAO = new WorkshopDAO();
        policeOfficerDAO = new PoliceOfficerDAO();

        // Load statistics from database
        loadStatistics();

        // Load logo
        loadLogo();

        // Clear any existing session
        SessionManager.getInstance().clearSession();

        // Setup button handler
        continueButton.setOnAction(event -> goToLogin());

        // Start visual animations
        startWelcomeAnimations();

        // Simulate system loading
        simulateSystemLoad();

        // Apply clip to make logo round
        makeLogoRound();
    }

    /**
     * Load statistics from database (no hardcoded figures)
     */
    private void loadStatistics() {
        try {
            // Load total vehicles
            int vehicleCount = vehicleDAO.countVehicles();
            totalVehiclesLabel.setText(String.valueOf(vehicleCount));

            // Load total customers
            int customerCount = customerDAO.countCustomers();
            totalCustomersLabel.setText(String.valueOf(customerCount));

            // Load total workshops
            int workshopCount = workshopDAO.countWorkshops();
            totalWorkshopsLabel.setText(String.valueOf(workshopCount));

            // Load total police officers
            int policeCount = policeOfficerDAO.countPoliceOfficers();
            totalPoliceLabel.setText(String.valueOf(policeCount));

            System.out.println("Statistics loaded: Vehicles=" + vehicleCount +
                    ", Customers=" + customerCount +
                    ", Workshops=" + workshopCount +
                    ", Police=" + policeCount);
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            // Set default values on error
            totalVehiclesLabel.setText("--");
            totalCustomersLabel.setText("--");
            totalWorkshopsLabel.setText("--");
            totalPoliceLabel.setText("--");
        }
    }

    private void makeLogoRound() {
        Circle clip = new Circle(90, 90, 90);
        logoImageView.setClip(clip);
    }

    private void loadLogo() {
        try {
            // Try multiple possible paths
            String[] paths = {
                    "/images/logo.png",
                    "/images/Logo.png",
                    "/images/Logo.jpg",
                    "/images/logo.jpg",
                    "/icons/logo.png"
            };

            Image logo = null;
            for (String path : paths) {
                java.net.URL imageUrl = getClass().getResource(path);
                if (imageUrl != null) {
                    logo = new Image(imageUrl.toExternalForm());
                    if (logo != null && !logo.isError()) {
                        break;
                    }
                }
            }

            if (logo != null && !logo.isError()) {
                logoImageView.setImage(logo);
                logoImageView.setStyle("-fx-background-color: transparent;");
            }
        } catch (Exception e) {
            // No logo found - keep default circle
        }
    }

    private void startWelcomeAnimations() {
        // Logo entrance animation
        ScaleTransition logoScale = new ScaleTransition(Duration.seconds(0.8), logoImageView);
        logoScale.setFromX(0.5);
        logoScale.setFromY(0.5);
        logoScale.setToX(1);
        logoScale.setToY(1);

        FadeTransition logoFade = new FadeTransition(Duration.seconds(0.8), logoImageView);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);

        SequentialTransition logoEntrance = new SequentialTransition(logoScale, logoFade);
        logoEntrance.play();

        // Title entrance animation
        TranslateTransition titleMove = new TranslateTransition(Duration.seconds(0.6), titleText);
        titleMove.setFromY(-30);
        titleMove.setToY(0);

        FadeTransition titleFade = new FadeTransition(Duration.seconds(0.6), titleText);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);

        ParallelTransition titleEntrance = new ParallelTransition(titleMove, titleFade);
        titleEntrance.setDelay(Duration.seconds(0.3));
        titleEntrance.play();

        // Button hover effect
        continueButton.setOnMouseEntered(e -> {
            ScaleTransition buttonScale = new ScaleTransition(Duration.seconds(0.2), continueButton);
            buttonScale.setToX(1.05);
            buttonScale.setToY(1.05);
            buttonScale.play();
        });

        continueButton.setOnMouseExited(e -> {
            ScaleTransition buttonScale = new ScaleTransition(Duration.seconds(0.2), continueButton);
            buttonScale.setToX(1);
            buttonScale.setToY(1);
            buttonScale.play();
        });
    }

    private void simulateSystemLoad() {
        loadingProgress.setVisible(true);
        loadingLabel.setVisible(true);

        String[] loadingMessages = {
                "Initializing system modules...",
                "Loading security protocols...",
                "Establishing database connection...",
                "Preparing user interface...",
                "System ready..."
        };

        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        timeline.setCycleCount(loadingMessages.length);

        for (int i = 0; i < loadingMessages.length; i++) {
            final int index = i;
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(i * 0.8),
                    e -> {
                        loadingLabel.setText(loadingMessages[index]);
                        if (index == loadingMessages.length - 1) {
                            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), loadingProgress);
                            fadeOut.setFromValue(1);
                            fadeOut.setToValue(0);
                            fadeOut.setOnFinished(event -> {
                                loadingProgress.setVisible(false);
                                loadingLabel.setVisible(false);
                            });
                            fadeOut.play();
                        }
                    }
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    private void goToLogin() {
        ScaleTransition pressEffect = new ScaleTransition(Duration.seconds(0.1), continueButton);
        pressEffect.setToX(0.95);
        pressEffect.setToY(0.95);
        pressEffect.setOnFinished(e -> {
            continueButton.setDisable(true);
            loadingProgress.setVisible(true);
            loadingLabel.setText("Redirecting to login portal...");

            PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
            delay.setOnFinished(event -> {
                try {
                    SceneManager.getInstance().switchToLogin();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    loadingLabel.setText("Error loading login. Please try again.");
                    continueButton.setDisable(false);
                    loadingProgress.setVisible(false);
                }
            });
            delay.play();
        });
        pressEffect.play();
    }
}