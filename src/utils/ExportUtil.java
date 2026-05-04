package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Utility class for exporting data to CSV format.
 * Supports exporting from TableView, Map data, and raw string arrays.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ExportUtil {

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String DEFAULT_REPORTS_DIR = "reports/";

    private ExportUtil() {} // Prevent instantiation

    /**
     * Gets the reports directory path.
     * Creates the directory if it doesn't exist.
     *
     * @return absolute path to reports directory
     */
    public static String getReportsDirectory() {
        // Try user home first for better persistence
        String userHome = System.getProperty("user.home");
        String reportsDir = userHome + File.separator + "VehicleIdentificationSystem" + File.separator + "reports" + File.separator;

        File dir = new File(reportsDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                // Fall back to local directory
                reportsDir = DEFAULT_REPORTS_DIR;
                dir = new File(reportsDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
        }
        return reportsDir;
    }

    /**
     * Exports a TableView to a CSV file.
     *
     * @param <T>        the table item type
     * @param tableView  the TableView to export
     * @param fileName   base name for the export file (without extension)
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> void exportToCSV(TableView<T> tableView, String fileName) throws IOException {
        if (tableView == null) {
            throw new IllegalArgumentException("TableView cannot be null");
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "export";
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullFileName = sanitizeFileName(fileName + "_" + timestamp + ".csv");
        String filePath = getReportsDirectory() + fullFileName;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            ObservableList<TableColumn<T, ?>> columns = (ObservableList<TableColumn<T, ?>>) tableView.getColumns();

            // Write headers
            for (int i = 0; i < columns.size(); i++) {
                writer.print(escapeCSV(columns.get(i).getText()));
                if (i < columns.size() - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            // Write data rows
            for (T rowItem : tableView.getItems()) {
                for (int i = 0; i < columns.size(); i++) {
                    TableColumn<T, ?> column = columns.get(i);
                    Object value = column.getCellData(rowItem);
                    String cellValue = value != null ? value.toString() : "";
                    writer.print(escapeCSV(cellValue));
                    if (i < columns.size() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        }

        AlertUtil.showInfo("Export Complete", "File saved to: " + filePath);
    }

    /**
     * Exports a list of maps to a CSV file.
     *
     * @param data     the list of maps to export
     * @param fileName base name for the export file
     * @param headers  column headers
     * @param fields   map keys corresponding to each column
     * @throws IOException if an I/O error occurs
     */
    public static void exportToCSV(List<Map<String, Object>> data, String fileName,
                                   String[] headers, String[] fields) throws IOException {
        if (data == null || data.isEmpty()) {
            AlertUtil.showWarning("Export Failed", "No data to export.");
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "export";
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullFileName = sanitizeFileName(fileName + "_" + timestamp + ".csv");
        String filePath = getReportsDirectory() + fullFileName;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write headers
            if (headers != null && headers.length > 0) {
                for (int i = 0; i < headers.length; i++) {
                    writer.print(escapeCSV(headers[i]));
                    if (i < headers.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            // Write data rows
            if (fields != null && fields.length > 0) {
                for (Map<String, Object> row : data) {
                    if (row == null) continue;

                    for (int i = 0; i < fields.length; i++) {
                        Object value = row.get(fields[i]);
                        String cellValue = value != null ? value.toString() : "";
                        writer.print(escapeCSV(cellValue));
                        if (i < fields.length - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            }
        }

        AlertUtil.showInfo("Export Complete", "File saved to: " + filePath);
    }

    /**
     * Exports simple string array data to a CSV file.
     *
     * @param data     the list of string arrays to export
     * @param fileName base name for the export file
     * @param headers  column headers (can be null)
     * @throws IOException if an I/O error occurs
     */
    public static void exportToCSVSimple(List<String[]> data, String fileName, String[] headers) throws IOException {
        if (data == null || data.isEmpty()) {
            AlertUtil.showWarning("Export Failed", "No data to export.");
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "export";
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullFileName = sanitizeFileName(fileName + "_" + timestamp + ".csv");
        String filePath = getReportsDirectory() + fullFileName;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write headers
            if (headers != null && headers.length > 0) {
                for (int i = 0; i < headers.length; i++) {
                    writer.print(escapeCSV(headers[i]));
                    if (i < headers.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            // Write data rows
            for (String[] row : data) {
                if (row == null) continue;

                for (int i = 0; i < row.length; i++) {
                    String cellValue = row[i] != null ? row[i] : "";
                    writer.print(escapeCSV(cellValue));
                    if (i < row.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        }

        AlertUtil.showInfo("Export Complete", "File saved to: " + filePath);
    }

    /**
     * Exports a result set to CSV using a custom query.
     *
     * @param fileName base name for the export file
     * @param query    the SQL query to execute
     * @return true if export was successful
     */
    public static boolean exportQueryToCSV(String fileName, String query) {
        return BackupUtil.exportQueryToCSV(query, fileName);
    }

    /**
     * Sanitizes a filename by removing invalid characters.
     *
     * @param fileName the original filename
     * @return sanitized filename
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null) return "export.csv";

        // Remove characters that are invalid in filenames
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Escapes a value for CSV format.
     * Handles commas, quotes, and newlines.
     *
     * @param value the value to escape
     * @return escaped value
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";

        // If the value contains special characters, wrap in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Checks if the reports directory is writable.
     *
     * @return true if directory is writable, false otherwise
     */
    public static boolean isReportsDirectoryWritable() {
        File dir = new File(getReportsDirectory());
        return dir.exists() && dir.canWrite();
    }

    /**
     * Cleans up old export files (older than specified days).
     *
     * @param daysToKeep number of days to keep files for
     * @return number of files deleted
     */
    public static int cleanupOldExports(int daysToKeep) {
        File dir = new File(getReportsDirectory());
        if (!dir.exists()) return 0;

        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
        int deletedCount = 0;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            }
        }

        return deletedCount;
    }
}