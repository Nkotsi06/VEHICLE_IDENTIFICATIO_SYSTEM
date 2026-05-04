package utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RootManager handles the main scene and view switching for the application.
 * Maintains a single Scene instance and swaps root nodes to prevent window resizing.
 * Implements caching for loaded FXML views for better performance.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class RootManager {

    private static final Logger LOGGER = Logger.getLogger(RootManager.class.getName());
    private static RootManager instance;

    private Stage primaryStage;
    private Scene mainScene;
    private Parent currentRoot;
    private final Map<String, Parent> rootCache;
    private final Map<String, Object> controllerCache;
    private String currentScene;

    // Default window dimensions
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double MIN_WIDTH = 900;
    private static final double MIN_HEIGHT = 600;

    /**
     * Private constructor for singleton pattern.
     */
    private RootManager() {
        rootCache = new HashMap<>();
        controllerCache = new HashMap<>();
    }

    /**
     * Gets the singleton instance of RootManager.
     *
     * @return the RootManager instance
     */
    public static synchronized RootManager getInstance() {
        if (instance == null) {
            instance = new RootManager();
        }
        return instance;
    }

    /**
     * Sets the primary stage and initializes the main scene.
     *
     * @param stage the primary stage
     * @throws IllegalArgumentException if stage is null
     */
    public void setPrimaryStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Primary stage cannot be null");
        }

        this.primaryStage = stage;

        // Create a single scene with a temporary root
        StackPane tempRoot = new StackPane();
        tempRoot.setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.mainScene = new Scene(tempRoot, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Load CSS stylesheet
        loadStylesheet();

        // Configure the stage
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setTitle("Vehicle Identification System");

        LOGGER.info("Primary stage initialized successfully");
    }

    /**
     * Loads the application CSS stylesheet.
     */
    private void loadStylesheet() {
        try {
            String cssPath = getClass().getResource("/css/application.css").toExternalForm();
            if (cssPath != null) {
                mainScene.getStylesheets().add(cssPath);
                LOGGER.info("CSS stylesheet loaded successfully");
            } else {
                LOGGER.warning("CSS stylesheet not found at /css/application.css");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load CSS stylesheet", e);
        }
    }

    /**
     * Switches to a different view by swapping the root node of the existing Scene.
     * This NEVER creates a new Scene, so the window NEVER resizes/shrinks.
     *
     * @param fxmlFile  the FXML file path (relative to /views/)
     * @param sceneName the logical name for the scene (used for caching)
     */
    public void setRoot(String fxmlFile, String sceneName) {
        // Validate inputs
        if (fxmlFile == null || fxmlFile.trim().isEmpty()) {
            LOGGER.warning("Cannot set root: fxmlFile is null or empty");
            AlertUtil.showWarning("Navigation Error", "Cannot load view: File name not specified");
            return;
        }

        if (sceneName == null || sceneName.trim().isEmpty()) {
            sceneName = fxmlFile.replace(".fxml", "");
        }

        try {
            // Check if root is already cached
            Parent newRoot = rootCache.get(sceneName);
            Object controller = null;

            if (newRoot == null) {
                // Load the FXML
                String fxmlPath = "/views/" + fxmlFile;
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                newRoot = loader.load();
                controller = loader.getController();

                // Set explicit preferred size on the root if it's a Region
                if (newRoot instanceof Region) {
                    Region regionRoot = (Region) newRoot;
                    regionRoot.setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    regionRoot.setMinSize(MIN_WIDTH, MIN_HEIGHT);
                }

                // Cache for future use
                rootCache.put(sceneName, newRoot);
                if (controller != null) {
                    controllerCache.put(sceneName, controller);
                }

                LOGGER.info("Loaded and cached view: " + sceneName + " from " + fxmlFile);
            } else {
                controller = controllerCache.get(sceneName);
                LOGGER.fine("Using cached view: " + sceneName);
            }

            // Swap the root - the Scene remains the same
            mainScene.setRoot(newRoot);
            currentRoot = newRoot;
            currentScene = sceneName;

            // Restore maximized state after root swap
            Platform.runLater(() -> {
                if (primaryStage != null && primaryStage.isShowing() && !primaryStage.isMaximized()) {
                    primaryStage.setMaximized(true);
                }
            });

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML: " + fxmlFile, e);
            AlertUtil.showError("Scene Error", "Failed to load " + fxmlFile + ":\n" + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error loading " + fxmlFile, e);
            AlertUtil.showError("Scene Error", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Refreshes the current scene by clearing its cache.
     * The view will be reloaded next time it's accessed.
     */
    public void refreshCurrentScene() {
        if (currentScene != null && rootCache.containsKey(currentScene)) {
            rootCache.remove(currentScene);
            controllerCache.remove(currentScene);
            LOGGER.info("Refreshed cache for scene: " + currentScene);
        }
    }

    /**
     * Gets the primary stage.
     *
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Gets the name of the current scene.
     *
     * @return current scene name
     */
    public String getCurrentScene() {
        return currentScene;
    }

    /**
     * Gets the controller of the current scene.
     *
     * @return current controller instance, or null if not found
     */
    public Object getCurrentController() {
        return controllerCache.get(currentScene);
    }

    /**
     * Gets the current root node.
     *
     * @return current root node
     */
    public Parent getCurrentRoot() {
        return currentRoot;
    }

    /**
     * Gets a cached controller by scene name.
     *
     * @param sceneName the scene name
     * @return the cached controller, or null if not found
     */
    public Object getController(String sceneName) {
        return controllerCache.get(sceneName);
    }

    /**
     * Clears all caches.
     */
    public void clearCache() {
        rootCache.clear();
        controllerCache.clear();
        LOGGER.info("RootManager cache cleared");
    }

    /**
     * Checks if a scene is cached.
     *
     * @param sceneName the scene name
     * @return true if cached, false otherwise
     */
    public boolean isSceneCached(String sceneName) {
        return rootCache.containsKey(sceneName);
    }

    /**
     * Preloads a scene into cache without displaying it.
     * Useful for improving perceived performance.
     *
     * @param fxmlFile  the FXML file path
     * @param sceneName the logical scene name
     * @return true if preload was successful, false otherwise
     */
    public boolean preloadScene(String fxmlFile, String sceneName) {
        if (isSceneCached(sceneName)) {
            return true;
        }

        try {
            String fxmlPath = "/views/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (root instanceof Region) {
                Region regionRoot = (Region) root;
                regionRoot.setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
                regionRoot.setMinSize(MIN_WIDTH, MIN_HEIGHT);
            }

            rootCache.put(sceneName, root);
            if (controller != null) {
                controllerCache.put(sceneName, controller);
            }

            LOGGER.info("Preloaded scene: " + sceneName);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to preload scene: " + sceneName, e);
            return false;
        }
    }
}