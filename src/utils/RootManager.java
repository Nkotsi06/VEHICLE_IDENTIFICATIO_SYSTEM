package utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RootManager {

    private static RootManager instance;
    private Stage primaryStage;
    private Scene mainScene;
    private Parent currentRoot;
    private final Map<String, Parent> rootCache;
    private final Map<String, Object> controllerCache;
    private String currentScene;

    private RootManager() {
        rootCache = new HashMap<>();
        controllerCache = new HashMap<>();
    }

    public static synchronized RootManager getInstance() {
        if (instance == null) {
            instance = new RootManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;

        // Create ONE scene that never changes - this is the key!
        // Use a temporary empty StackPane as initial root
        javafx.scene.layout.StackPane tempRoot = new javafx.scene.layout.StackPane();
        tempRoot.setPrefSize(1280, 800);
        this.mainScene = new Scene(tempRoot, 1280, 800);

        // Load CSS
        String cssPath = getClass().getResource("/css/application.css").toExternalForm();
        if (cssPath != null) {
            mainScene.getStylesheets().add(cssPath);
        }

        // Set the scene on the stage
        primaryStage.setScene(mainScene);

        // Set min sizes
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
    }

    /**
     * Switch to a different view by swapping the root node of the existing Scene.
     * This NEVER creates a new Scene, so the window NEVER resizes/shrinks.
     */
    public void setRoot(String fxmlFile, String sceneName) {
        try {
            // Check if root is already cached
            Parent newRoot = rootCache.get(sceneName);
            Object controller = null;

            if (newRoot == null) {
                // Load the FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFile));
                newRoot = loader.load();
                controller = loader.getController();

                // CRITICAL: Set explicit preferred size on the root if it's a Region
                if (newRoot instanceof Region) {
                    Region regionRoot = (Region) newRoot;
                    regionRoot.setPrefSize(1280, 800);
                    regionRoot.setMinSize(900, 600);
                }

                // Cache for future use
                rootCache.put(sceneName, newRoot);
                if (controller != null) {
                    controllerCache.put(sceneName, controller);
                }
            } else {
                controller = controllerCache.get(sceneName);
            }

            // Swap the root - THIS IS THE KEY OPERATION
            // The Scene remains the same, only the root node changes
            mainScene.setRoot(newRoot);
            currentRoot = newRoot;
            currentScene = sceneName;

            // CRITICAL: Restore maximized state after root swap
            // This ensures the window stays maximized when switching scenes
            Platform.runLater(() -> {
                if (primaryStage != null && !primaryStage.isMaximized()) {
                    primaryStage.setMaximized(true);
                }
            });

        } catch (IOException e) {
            java.util.logging.Logger.getLogger(RootManager.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Failed to load " + fxmlFile, e);
            AlertUtil.showError("Scene Error", "Failed to load " + fxmlFile + ": " + e.getMessage());
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(RootManager.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Unexpected error loading " + fxmlFile, e);
            AlertUtil.showError("Scene Error", "Unexpected error: " + e.getMessage());
        }
    }

    public void refreshCurrentScene() {
        if (currentScene != null && rootCache.containsKey(currentScene)) {
            // Force recreate the root for the current scene
            rootCache.remove(currentScene);
            controllerCache.remove(currentScene);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getCurrentScene() {
        return currentScene;
    }

    public Object getCurrentController() {
        return controllerCache.get(currentScene);
    }

    public Parent getCurrentRoot() {
        return currentRoot;
    }
}