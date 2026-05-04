package utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Centralized error handling utility for the application.
 * Provides consistent error dialogs and logging.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ErrorHandler {

    private ErrorHandler() {} // Prevent instantiation

    /**
     * Handles a general exception by showing an error dialog with stack trace.
     *
     * @param e the exception to handle
     */
    public static void handleException(Exception e) {
        if (e == null) {
            handleException(new NullPointerException("Unknown error occurred"));
            return;
        }

        // Log to console
        e.printStackTrace();

        // Show user-friendly dialog
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An unexpected error occurred");

        // Get user-friendly message
        String userMessage = getUserFriendlyMessage(e);
        alert.setContentText(userMessage);

        // Create expandable stack trace section
        String stackTrace = getStackTraceString(e);

        Label label = new Label("Technical Details:");
        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Handles a SQLException specifically.
     *
     * @param e the SQLException to handle
     */
    public static void handleSQLException(SQLException e) {
        if (e == null) return;

        e.printStackTrace();

        String message = String.format(
                "Database error occurred.\n\nError Code: %d\nSQL State: %s\nMessage: %s",
                e.getErrorCode(),
                e.getSQLState() != null ? e.getSQLState() : "N/A",
                e.getMessage()
        );

        AlertUtil.showDatabaseError(new Exception(message));
    }

    /**
     * Handles a validation error for a specific form field.
     *
     * @param field   the field name
     * @param message the validation error message
     */
    public static void handleValidationError(String field, String message) {
        AlertUtil.showValidationError(field, message);
    }

    /**
     * Handles a generic warning with custom message.
     *
     * @param title   the warning title
     * @param message the warning message
     */
    public static void handleWarning(String title, String message) {
        AlertUtil.showWarning(title, message);
    }

    /**
     * Handles a runtime exception with a custom user message.
     *
     * @param e            the exception
     * @param userMessage  user-friendly message to display
     */
    public static void handleExceptionWithMessage(Exception e, String userMessage) {
        e.printStackTrace();
        AlertUtil.showError("Error", userMessage);
    }

    /**
     * Logs an exception without showing a dialog (for background tasks).
     *
     * @param e the exception to log
     */
    public static void logException(Exception e) {
        if (e != null) {
            System.err.println("Exception logged: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets a user-friendly message from an exception.
     *
     * @param e the exception
     * @return user-friendly message
     */
    private static String getUserFriendlyMessage(Exception e) {
        if (e == null) return "Unknown error";

        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = e.getClass().getSimpleName();
        }

        // Make messages user-friendly
        if (message.contains("SQLException") || message.contains("Connection")) {
            return "Database connection error. Please check your database configuration.";
        }
        if (message.contains("NullPointer")) {
            return "An internal error occurred. Please contact support.";
        }
        if (message.contains("FileNotFound")) {
            return "Required file was not found. Please verify installation.";
        }

        return message;
    }

    /**
     * Converts an exception to a stack trace string.
     *
     * @param e the exception
     * @return stack trace as string
     */
    private static String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Shows a success message.
     *
     * @param operation the operation that succeeded
     * @see AlertUtil#showSuccess(String)
     */
    public static void showSuccess(String operation) {
        AlertUtil.showSuccess(operation + " completed successfully.");
    }
}