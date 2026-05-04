package utils;

import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for printing JavaFX nodes and panes.
 * Provides methods for printing reports, tables, and other UI components.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PrintUtil {

    private static final Logger LOGGER = Logger.getLogger(PrintUtil.class.getName());

    private PrintUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Prints a JavaFX Node using the system printer dialog.
     *
     * @param node the node to print
     * @return true if printing was successful, false otherwise
     */
    public static boolean printNode(Node node) {
        if (node == null) {
            LOGGER.warning("Cannot print null node");
            AlertUtil.showWarning("Print Error", "No content to print.");
            return false;
        }

        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                LOGGER.warning("No printer service available");
                AlertUtil.showWarning("Print Error", "No printer is available on this system.");
                return false;
            }

            Window ownerWindow = node.getScene() != null ? node.getScene().getWindow() : null;

            if (job.showPrintDialog(ownerWindow)) {
                boolean success = job.printPage(node);
                if (success) {
                    job.endJob();
                    AlertUtil.showInfo("Print", "Print job completed successfully.");
                    LOGGER.info("Print job completed successfully");
                    return true;
                } else {
                    LOGGER.warning("Print job failed");
                    AlertUtil.showWarning("Print Error", "Failed to print the document.");
                    return false;
                }
            } else {
                LOGGER.fine("Print dialog cancelled by user");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during printing", e);
            ErrorHandler.handleException(e);
            return false;
        }
    }

    /**
     * Prints a Pane with a custom job title.
     *
     * @param pane  the pane to print
     * @param title the print job title
     * @return true if printing was successful, false otherwise
     */
    public static boolean printPane(Pane pane, String title) {
        if (pane == null) {
            LOGGER.warning("Cannot print null pane");
            AlertUtil.showWarning("Print Error", "No content to print.");
            return false;
        }

        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                LOGGER.warning("No printer service available");
                AlertUtil.showWarning("Print Error", "No printer is available on this system.");
                return false;
            }

            // Set job name if provided
            if (title != null && !title.trim().isEmpty()) {
                job.getJobSettings().setJobName(title);
            }

            Window ownerWindow = pane.getScene() != null ? pane.getScene().getWindow() : null;

            if (job.showPrintDialog(ownerWindow)) {
                boolean success = job.printPage(pane);
                if (success) {
                    job.endJob();
                    AlertUtil.showInfo("Print", "Print job '" + (title != null ? title : "Untitled") + "' completed successfully.");
                    LOGGER.info("Print job completed: " + title);
                    return true;
                } else {
                    LOGGER.warning("Print job failed for: " + title);
                    AlertUtil.showWarning("Print Error", "Failed to print the document.");
                    return false;
                }
            } else {
                LOGGER.fine("Print dialog cancelled by user for: " + title);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during printing", e);
            ErrorHandler.handleException(e);
            return false;
        }
    }

    /**
     * Prints a region with scaling to fit the page.
     *
     * @param region the region to print
     * @param title  the print job title
     * @param scaleToPage whether to scale the content to fit the page
     * @return true if printing was successful, false otherwise
     */
    public static boolean printScaled(Region region, String title, boolean scaleToPage) {
        if (region == null) {
            return false;
        }

        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                AlertUtil.showWarning("Print Error", "No printer is available.");
                return false;
            }

            if (title != null && !title.trim().isEmpty()) {
                job.getJobSettings().setJobName(title);
            }

            if (scaleToPage) {
                // Store original dimensions
                double originalWidth = region.getWidth();
                double originalHeight = region.getHeight();

                // Get page dimensions
                double pageWidth = job.getPrinter().getDefaultPageLayout().getPrintableWidth();
                double pageHeight = job.getPrinter().getDefaultPageLayout().getPrintableHeight();

                // Calculate scale factor
                double scaleX = pageWidth / originalWidth;
                double scaleY = pageHeight / originalHeight;
                double scale = Math.min(scaleX, scaleY);

                // Apply scale transform
                region.setScaleX(scale);
                region.setScaleY(scale);

                boolean success = job.printPage(region);

                // Reset scale
                region.setScaleX(1.0);
                region.setScaleY(1.0);

                if (success) {
                    job.endJob();
                    AlertUtil.showInfo("Print", "Print job completed successfully.");
                    return true;
                }
            } else {
                return printPane(region instanceof Pane ? (Pane) region : null, title);
            }

            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during scaled printing", e);
            ErrorHandler.handleException(e);
            return false;
        }
    }

    /**
     * Checks if a printer is available on the system.
     *
     * @return true if a printer is available, false otherwise
     */
    public static boolean isPrinterAvailable() {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            return job != null && job.getPrinter() != null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking printer availability", e);
            return false;
        }
    }

    /**
     * Gets the default printer name.
     *
     * @return the default printer name, or null if no printer available
     */
    public static String getDefaultPrinterName() {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.getPrinter() != null) {
                return job.getPrinter().getName();
            }
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting default printer name", e);
            return null;
        }
    }
}