

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Screen;
import javafx.stage.Stage;

import database.DatabaseConnection;
import utils.SceneManager;
import utils.SessionManager;
import utils.DatabaseInitializer;

public class Main extends Application {

    private static Stage primaryStage;
    private static SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;

            // Set application title
            stage.setTitle("Vehicle Identification System");

            // Set minimum window size
            stage.setMinWidth(900);
            stage.setMinHeight(600);

            // Get screen dimensions for proper sizing
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();

            // Set initial size to match screen (but not fullscreen)
            stage.setWidth(screenWidth);
            stage.setHeight(screenHeight);
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());

            // Initialize database connection
            if (!initializeDatabase()) {
                showErrorAndExit("Database Connection Failed",
                        "Could not connect to the PostgreSQL database. Please ensure:\n" +
                                "1. PostgreSQL service is running\n" +
                                "2. Database 'vehicle_db' exists\n" +
                                "3. Connection settings are correct in config/database.properties");
                return;
            }

            // Initialize database tables and configuration data
            DatabaseInitializer initializer = new DatabaseInitializer();
            if (!initializer.initialize()) {
                showErrorAndExit("Database Initialization Failed",
                        "Could not initialize database schema. Please check the logs.");
                return;
            }

            // Initialize scene manager
            sceneManager = SceneManager.getInstance();
            sceneManager.setPrimaryStage(stage);

            // Load the login view
            sceneManager.switchToLogin();

            // Set up shutdown hook
            stage.setOnCloseRequest(event -> shutdown());

            // Show the stage
            stage.show();

            // CRITICAL: Maximize the window AFTER showing
            // This gives a normal maximized window with title bar and taskbar
            Platform.runLater(() -> {
                stage.setMaximized(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndExit("Application Error",
                    "Failed to start application: " + e.getMessage());
        }
    }

    private boolean initializeDatabase() {
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            return db.testConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showErrorAndExit(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
        System.exit(1);
    }

    private void shutdown() {
        try {
            // Close database connection
            DatabaseConnection.getInstance().closeConnection();

            // Clear session
            SessionManager.getInstance().clearSession();

            System.out.println("Application shutdown completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}