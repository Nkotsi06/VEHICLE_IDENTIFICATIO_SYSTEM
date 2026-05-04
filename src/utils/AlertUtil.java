package utils;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Utility class for displaying various types of alert dialogs.
 * Provides a clean, consistent interface for user notifications.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class AlertUtil {

    // Private constructor to prevent instantiation (utility class pattern)
    private AlertUtil() {}

    /**
     * Displays an information dialog with custom title and message.
     *
     * @param title   the dialog title
     * @param message the content message
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an information dialog with default title "Information".
     *
     * @param message the content message
     */
    public static void showInfo(String message) {
        showInfo("Information", message);
    }

    /**
     * Displays a warning dialog with custom title and message.
     *
     * @param title   the dialog title
     * @param message the content message
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a warning dialog with default title "Warning".
     *
     * @param message the content message
     */
    public static void showWarning(String message) {
        showWarning("Warning", message);
    }

    /**
     * Displays an error dialog with custom title and message.
     *
     * @param title   the dialog title
     * @param message the content message
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error dialog with default title "Error".
     *
     * @param message the content message
     */
    public static void showError(String message) {
        showError("Error", message);
    }

    /**
     * Displays a confirmation dialog and returns the user's choice.
     *
     * @param title   the dialog title
     * @param message the confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Displays a confirmation dialog with default title "Confirmation".
     *
     * @param message the confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirmation", message);
    }

    /**
     * Displays a confirmation dialog and returns the selected ButtonType.
     * Useful for dialogs with custom buttons (Yes/No/Cancel).
     *
     * @param title   the dialog title
     * @param message the confirmation message
     * @return Optional containing the selected ButtonType
     */
    public static Optional<ButtonType> showConfirmationWithOptions(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Displays a success message dialog.
     *
     * @param title   the dialog title
     * @param message the success message
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a success message dialog with default title "Success".
     *
     * @param message the success message
     */
    public static void showSuccess(String message) {
        showSuccess("Success", message);
    }

    /**
     * Displays a validation error specific to a form field.
     *
     * @param field   the name of the field with validation error
     * @param message the specific error message
     */
    public static void showValidationError(String field, String message) {
        showError("Validation Error", field + ": " + message);
    }

    /**
     * Displays a database error dialog with the exception details.
     *
     * @param e the exception that occurred
     */
    public static void showDatabaseError(Exception e) {
        String errorMessage = "An error occurred while accessing the database.\n\n";
        if (e != null && e.getMessage() != null) {
            errorMessage += e.getMessage();
        } else {
            errorMessage += "Unknown database error";
        }
        showError("Database Error", errorMessage);
    }

    /**
     * Checks if a string value is not null and not empty.
     *
     * @param value the string to check
     * @return true if the string is not null and not empty after trimming
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Shows a custom alert with expandable exception details.
     * Useful for showing detailed error information to developers.
     *
     * @param title       the alert title
     * @param header      the alert header text
     * @param content     the alert content
     * @param exception   the exception to display in expandable section
     */
    public static void showDetailedError(String title, String header, String content, Exception exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (exception != null) {
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(
                    exception.toString() + "\n\nStack Trace:\n" + getStackTraceString(exception)
            );
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setExpandableContent(textArea);
        }

        alert.showAndWait();
    }

    /**
     * Helper method to convert stack trace to string.
     *
     * @param e the exception
     * @return string representation of the stack trace
     */
    private static String getStackTraceString(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}