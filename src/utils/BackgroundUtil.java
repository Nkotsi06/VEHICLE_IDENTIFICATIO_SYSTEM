package utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;

import java.util.function.Consumer;

public class BackgroundUtil {

    /**
     * Execute a task with progress bar binding
     */
    public static void executeWithProgress(Task<Void> task, ProgressBar progressBar, ProgressIndicator progressIndicator) {
        if (progressBar != null) {
            progressBar.progressProperty().bind(task.progressProperty());
            progressBar.setVisible(true);
        }
        if (progressIndicator != null) {
            progressIndicator.progressProperty().bind(task.progressProperty());
            progressIndicator.setVisible(true);
        }

        task.setOnSucceeded(event -> {
            if (progressBar != null) {
                progressBar.setVisible(false);
                progressBar.progressProperty().unbind();
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
                progressIndicator.progressProperty().unbind();
            }
        });

        task.setOnFailed(event -> {
            if (progressBar != null) {
                progressBar.setVisible(false);
                progressBar.progressProperty().unbind();
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
                progressIndicator.progressProperty().unbind();
            }
            Throwable exception = task.getException();
            if (exception != null) {
                ErrorHandler.handleException(new Exception(exception));
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Execute a task with progress bar and status label
     */
    public static void executeWithProgressAndStatus(Task<Void> task, ProgressBar progressBar,
                                                    ProgressIndicator progressIndicator, Label statusLabel) {
        if (progressBar != null) {
            progressBar.progressProperty().bind(task.progressProperty());
            progressBar.setVisible(true);
        }
        if (progressIndicator != null) {
            progressIndicator.progressProperty().bind(task.progressProperty());
            progressIndicator.setVisible(true);
        }

        task.setOnSucceeded(event -> {
            if (progressBar != null) {
                progressBar.setVisible(false);
                progressBar.progressProperty().unbind();
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
                progressIndicator.progressProperty().unbind();
            }
            if (statusLabel != null) {
                Platform.runLater(() -> statusLabel.setText("Task completed successfully"));
            }
        });

        task.setOnFailed(event -> {
            if (progressBar != null) {
                progressBar.setVisible(false);
                progressBar.progressProperty().unbind();
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
                progressIndicator.progressProperty().unbind();
            }
            if (statusLabel != null) {
                Platform.runLater(() -> statusLabel.setText("Task failed: " + task.getException().getMessage()));
            }
            Throwable exception = task.getException();
            if (exception != null) {
                ErrorHandler.handleException(new Exception(exception));
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Execute a task with success/failure callbacks
     */
    public static <T> void executeWithCallback(Task<T> task, Runnable onSuccess, Runnable onFailure) {
        executeWithCallback(task, onSuccess, onFailure, null);
    }

    /**
     * Execute a task with success/failure callbacks and result consumer
     */
    public static <T> void executeWithCallback(Task<T> task, Runnable onSuccess, Runnable onFailure, Consumer<T> resultConsumer) {
        task.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
            if (resultConsumer != null) {
                resultConsumer.accept(task.getValue());
            }
        });

        task.setOnFailed(event -> {
            if (onFailure != null) {
                onFailure.run();
            }
            Throwable exception = task.getException();
            if (exception != null) {
                ErrorHandler.handleException(new Exception(exception));
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Execute a simple task without progress tracking
     */
    public static void executeTask(Runnable runnable) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runnable.run();
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            if (exception != null) {
                ErrorHandler.handleException(new Exception(exception));
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Execute a task with a timeout
     */
    public static void executeWithTimeout(Runnable runnable, long timeoutMillis) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runnable.run();
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        // Schedule timeout check
        new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                if (thread.isAlive()) {
                    thread.interrupt();
                    Platform.runLater(() -> {
                        ErrorHandler.handleException(new Exception("Task timed out after " + timeoutMillis + "ms"));
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Sleep helper with better error handling
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ErrorHandler.handleException(e);
        }
    }

    /**
     * Sleep helper for UI operations (runs on background thread)
     */
    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Update progress on the UI thread
     */
    public static void updateProgress(ProgressBar progressBar, double progress) {
        if (progressBar != null) {
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
    }

    /**
     * Update progress indicator on the UI thread
     */
    public static void updateProgress(ProgressIndicator progressIndicator, double progress) {
        if (progressIndicator != null) {
            Platform.runLater(() -> progressIndicator.setProgress(progress));
        }
    }

    /**
     * Update status label on the UI thread
     */
    public static void updateStatus(Label statusLabel, String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    /**
     * Create a progress task with automatic progress update
     */
    public static Task<Void> createProgressTask(RunnableWithProgress runnable, int totalSteps) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runnable.run(this::updateProgress, totalSteps);
                return null;
            }
        };
    }

    /**
     * Functional interface for tasks that report progress
     */
    @FunctionalInterface
    public interface RunnableWithProgress {
        void run(ProgressCallback callback, int totalSteps);
    }

    /**
     * Progress callback interface
     */
    @FunctionalInterface
    public interface ProgressCallback {
        void update(int currentStep, int totalSteps);
    }
}