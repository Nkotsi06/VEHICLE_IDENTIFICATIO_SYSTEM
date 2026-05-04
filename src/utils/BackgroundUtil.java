package utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;

import java.util.function.Consumer;

/**
 * Utility class for handling background tasks with progress indicators.
 * Provides methods to execute long-running operations without freezing the UI.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class BackgroundUtil {

    private BackgroundUtil() {} // Prevent instantiation

    /**
     * Executes a task with progress bar and progress indicator binding.
     *
     * @param task               the task to execute
     * @param progressBar        the progress bar to bind (can be null)
     * @param progressIndicator  the progress indicator to bind (can be null)
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

        task.setOnSucceeded(event -> cleanupProgress(task, progressBar, progressIndicator, null));
        task.setOnFailed(event -> {
            cleanupProgress(task, progressBar, progressIndicator, null);
            Throwable exception = task.getException();
            if (exception != null) {
                ErrorHandler.handleException(new Exception(exception));
            }
        });

        startThread(task);
    }

    /**
     * Executes a task with progress bar, indicator, and status label updates.
     *
     * @param task               the task to execute
     * @param progressBar        the progress bar to bind
     * @param progressIndicator  the progress indicator to bind
     * @param statusLabel        the label to update with status messages
     */
    public static void executeWithProgressAndStatus(Task<Void> task, ProgressBar progressBar,
                                                    ProgressIndicator progressIndicator, Label statusLabel) {
        bindProgress(task, progressBar, progressIndicator);

        task.setOnSucceeded(event -> {
            cleanupProgress(task, progressBar, progressIndicator, statusLabel);
            if (statusLabel != null) {
                Platform.runLater(() -> statusLabel.setText("Task completed successfully"));
            }
        });

        task.setOnFailed(event -> {
            cleanupProgress(task, progressBar, progressIndicator, statusLabel);
            if (statusLabel != null) {
                Platform.runLater(() -> statusLabel.setText("Task failed: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error")));
            }
            Throwable exception = task.getException();
            if (exception != null) {
                ErrorHandler.handleException(new Exception(exception));
            }
        });

        startThread(task);
    }

    /**
     * Executes a task with success/failure callbacks (no result consumer).
     *
     * @param <T>        the result type
     * @param task       the task to execute
     * @param onSuccess  callback to run on success
     * @param onFailure  callback to run on failure
     */
    public static <T> void executeWithCallback(Task<T> task, Runnable onSuccess, Runnable onFailure) {
        executeWithCallback(task, onSuccess, onFailure, null);
    }

    /**
     * Executes a task with success/failure callbacks and result consumer.
     *
     * @param <T>            the result type
     * @param task           the task to execute
     * @param onSuccess      callback to run on success
     * @param onFailure      callback to run on failure
     * @param resultConsumer consumer for the task result (can be null)
     */
    public static <T> void executeWithCallback(Task<T> task, Runnable onSuccess,
                                               Runnable onFailure, Consumer<T> resultConsumer) {
        task.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
            if (resultConsumer != null && task.getValue() != null) {
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

        startThread(task);
    }

    /**
     * Executes a simple runnable task without progress tracking.
     *
     * @param runnable the task to execute
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

        startThread(task);
    }

    /**
     * Executes a task with a timeout limit.
     *
     * @param runnable      the task to execute
     * @param timeoutMillis maximum execution time in milliseconds
     */
    public static void executeWithTimeout(Runnable runnable, long timeoutMillis) {
        Thread taskThread = new Thread(runnable);
        taskThread.setDaemon(true);
        taskThread.start();

        // Schedule timeout check
        Thread timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                if (taskThread.isAlive()) {
                    taskThread.interrupt();
                    Platform.runLater(() -> {
                        ErrorHandler.handleException(new Exception("Task timed out after " + timeoutMillis + "ms"));
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timeoutThread.setDaemon(true);
        timeoutThread.start();
    }

    /**
     * Sleep helper with better error handling (propagates interruption).
     *
     * @param millis milliseconds to sleep
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
     * Sleep helper that silently ignores interruptions.
     * Use for non-critical delays.
     *
     * @param millis milliseconds to sleep
     */
    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Updates a progress bar on the UI thread.
     *
     * @param progressBar the progress bar to update
     * @param progress    the progress value (0.0 to 1.0)
     */
    public static void updateProgress(ProgressBar progressBar, double progress) {
        if (progressBar != null) {
            Platform.runLater(() -> progressBar.setProgress(progress));
        }
    }

    /**
     * Updates a progress indicator on the UI thread.
     *
     * @param progressIndicator the progress indicator to update
     * @param progress          the progress value (0.0 to 1.0)
     */
    public static void updateProgress(ProgressIndicator progressIndicator, double progress) {
        if (progressIndicator != null) {
            Platform.runLater(() -> progressIndicator.setProgress(progress));
        }
    }

    /**
     * Updates a status label on the UI thread.
     *
     * @param statusLabel the label to update
     * @param message     the status message
     */
    public static void updateStatus(Label statusLabel, String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }

    /**
     * Creates a progress task that reports progress as it executes.
     *
     * @param runnable   the task with progress callback
     * @param totalSteps total number of steps for progress calculation
     * @return a Task configured with progress tracking
     */
    public static Task<Void> createProgressTask(RunnableWithProgress runnable, int totalSteps) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (runnable != null) {
                    runnable.run((currentStep, total) -> {
                        updateProgress(currentStep, total);
                    }, totalSteps);
                }
                return null;
            }
        };
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private static void bindProgress(Task<Void> task, ProgressBar progressBar, ProgressIndicator progressIndicator) {
        if (progressBar != null) {
            progressBar.progressProperty().bind(task.progressProperty());
            progressBar.setVisible(true);
        }
        if (progressIndicator != null) {
            progressIndicator.progressProperty().bind(task.progressProperty());
            progressIndicator.setVisible(true);
        }
    }

    private static void cleanupProgress(Task<Void> task, ProgressBar progressBar,
                                        ProgressIndicator progressIndicator, Label statusLabel) {
        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setVisible(false);
                progressBar.progressProperty().unbind();
            }
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
                progressIndicator.progressProperty().unbind();
            }
        });
    }

    private static void startThread(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ============================================
    // FUNCTIONAL INTERFACES
    // ============================================

    /**
     * Functional interface for tasks that report progress.
     */
    @FunctionalInterface
    public interface RunnableWithProgress {
        /**
         * Executes the task with progress reporting capabilities.
         *
         * @param callback   the progress callback
         * @param totalSteps total number of steps
         */
        void run(ProgressCallback callback, int totalSteps);
    }

    /**
     * Progress callback interface for reporting task progress.
     */
    @FunctionalInterface
    public interface ProgressCallback {
        /**
         * Updates the current progress.
         *
         * @param currentStep the current step number
         * @param totalSteps  the total number of steps
         */
        void update(int currentStep, int totalSteps);
    }
}