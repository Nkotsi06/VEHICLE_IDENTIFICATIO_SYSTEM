package utils;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import database.DatabaseConnection;

/**
 * Utility class for database backup and restore operations.
 * Handles CSV exports, zip compression, and data recovery.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class BackupUtil {

    private static final String BACKUP_DIR = "backups/";
    private static final String REPORTS_DIR = "reports/";

    // List of all major tables in the system
    private static final String[] ALL_TABLES = {
            "users", "customers", "vehicles", "violations", "insurance_policies",
            "service_records", "workshops", "police_reports", "stolen_vehicles",
            "warrants", "notifications", "audit_logs", "insurance_claims",
            "geofence_zones", "geofence_alerts", "vehicle_documents",
            "expired_document_alerts", "customer_queries", "workshop_services",
            "mechanics", "parts_inventory", "service_reminders", "user_roles",
            "role_permissions", "system_settings"
    };

    static {
        initializeDirectories();
    }

    private BackupUtil() {} // Prevent instantiation

    /**
     * Creates all necessary backup directories if they don't exist.
     */
    private static void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create backup directories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a timestamped database backup (CSV format).
     *
     * @return true if backup was successful for all tables, false otherwise
     */
    public static boolean createDatabaseBackup() {
        String timestamp = getTimestamp();
        boolean allSuccess = true;

        for (String table : ALL_TABLES) {
            String csvFile = BACKUP_DIR + table + "_" + timestamp + ".csv";
            if (!exportTableToCSV(table, csvFile)) {
                System.err.println("Failed to backup table: " + table);
                allSuccess = false;
            }
        }

        if (allSuccess) {
            System.out.println("Database backup completed successfully at: " + timestamp);
        }
        return allSuccess;
    }

    /**
     * Creates a full system backup including all tables and documents.
     *
     * @return true if backup was successful, false otherwise
     */
    public static boolean createFullSystemBackup() {
        String timestamp = getTimestamp();
        String zipFileName = BACKUP_DIR + "full_system_backup_" + timestamp + ".zip";

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName))) {

            // Backup all database tables
            for (String table : ALL_TABLES) {
                String csvContent = exportTableToCSVString(table);
                if (csvContent != null && !csvContent.isEmpty()) {
                    ZipEntry entry = new ZipEntry("database/" + table + ".csv");
                    zipOut.putNextEntry(entry);
                    zipOut.write(csvContent.getBytes());
                    zipOut.closeEntry();
                    System.out.println("Backed up table: " + table);
                }
            }

            // Backup documents directory
            addDirectoryToZip(new File("documents/"), "documents/", zipOut);

            // Backup logs directory
            addDirectoryToZip(new File("logs/"), "logs/", zipOut);

            // Backup configuration files
            addFileToZip(new File("config/config.properties"), "config/config.properties", zipOut);
            addFileToZip(new File("config/database.properties"), "config/database.properties", zipOut);

            System.out.println("Full system backup created: " + zipFileName);
            return true;

        } catch (Exception e) {
            System.err.println("Failed to create full system backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exports a single table to CSV file.
     *
     * @param tableName name of the table to export
     * @param filePath  path where the CSV file will be saved
     * @return true if export was successful, false otherwise
     */
    private static boolean exportTableToCSV(String tableName, String filePath) {
        String sql = "SELECT * FROM " + escapeTableName(tableName);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Write headers
            for (int i = 1; i <= columnCount; i++) {
                writer.print(escapeCSV(meta.getColumnName(i)));
                if (i < columnCount) writer.print(",");
            }
            writer.println();

            // Write data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    writer.print(escapeCSV(value));
                    if (i < columnCount) writer.print(",");
                }
                writer.println();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Database error exporting " + tableName + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("IO error exporting " + tableName + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error exporting " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exports a table to CSV string (for zip backup).
     *
     * @param tableName name of the table to export
     * @return CSV content as string, or null if export failed
     */
    private static String exportTableToCSVString(String tableName) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM " + escapeTableName(tableName);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Write headers
            for (int i = 1; i <= columnCount; i++) {
                sb.append(escapeCSV(meta.getColumnName(i)));
                if (i < columnCount) sb.append(",");
            }
            sb.append("\n");

            // Write data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    sb.append(escapeCSV(value));
                    if (i < columnCount) sb.append(",");
                }
                sb.append("\n");
            }
            return sb.toString();

        } catch (SQLException e) {
            System.err.println("Failed to export " + tableName + " to string: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adds a directory to a zip output stream.
     *
     * @param dir     the directory to add
     * @param zipPath the path prefix in the zip
     * @param zipOut  the zip output stream
     * @throws IOException if an I/O error occurs
     */
    private static void addDirectoryToZip(File dir, String zipPath, ZipOutputStream zipOut) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    addFileToZip(file, zipPath + file.getName(), zipOut);
                } else if (file.isDirectory()) {
                    addDirectoryToZip(file, zipPath + file.getName() + "/", zipOut);
                }
            }
        }
    }

    /**
     * Adds a single file to a zip output stream.
     *
     * @param file    the file to add
     * @param zipPath the path in the zip
     * @param zipOut  the zip output stream
     * @throws IOException if an I/O error occurs
     */
    private static void addFileToZip(File file, String zipPath, ZipOutputStream zipOut) throws IOException {
        if (!file.exists() || !file.isFile()) return;

        ZipEntry entry = new ZipEntry(zipPath);
        zipOut.putNextEntry(entry);
        Files.copy(file.toPath(), zipOut);
        zipOut.closeEntry();
    }

    /**
     * Restores data from a backup file.
     *
     * @param backupFilePath path to the backup file
     * @return true if restore was successful, false otherwise
     */
    public static boolean restoreFromBackup(String backupFilePath) {
        if (backupFilePath == null || backupFilePath.isEmpty()) {
            System.err.println("Backup file path is null or empty");
            return false;
        }

        File backupFile = new File(backupFilePath);
        if (!backupFile.exists()) {
            System.err.println("Backup file not found: " + backupFilePath);
            return false;
        }

        if (backupFilePath.endsWith(".zip")) {
            return restoreFromZipBackup(backupFilePath);
        } else if (backupFilePath.endsWith(".csv")) {
            return restoreFromCSVBackup(backupFilePath);
        } else {
            System.err.println("Unsupported backup format: " + backupFilePath);
            return false;
        }
    }

    /**
     * Restores from a zip backup file.
     *
     * @param zipFilePath path to the zip backup
     * @return true if restore was successful, false otherwise
     */
    private static boolean restoreFromZipBackup(String zipFilePath) {
        try (java.util.zip.ZipInputStream zipIn = new java.util.zip.ZipInputStream(new FileInputStream(zipFilePath))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                System.out.println("Found backup entry: " + entryName);

                // TODO: Implement actual restore logic for each entry
                // This would read the entry and insert into appropriate tables

                zipIn.closeEntry();
            }
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Zip backup file not found: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("Error reading zip backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restores from a CSV backup file.
     *
     * @param csvFilePath path to the CSV backup
     * @return true if restore was successful, false otherwise
     */
    private static boolean restoreFromCSVBackup(String csvFilePath) {
        System.out.println("CSV restore from: " + csvFilePath);
        // TODO: Implement CSV restore logic
        return true;
    }

    /**
     * Exports a query result to CSV file.
     *
     * @param query    the SQL query to execute
     * @param fileName base name for the output file (without extension)
     * @return true if export was successful, false otherwise
     */
    public static boolean exportQueryToCSV(String query, String fileName) {
        if (query == null || query.isEmpty()) {
            System.err.println("Query is null or empty");
            return false;
        }

        String filePath = REPORTS_DIR + fileName + "_" + getTimestamp() + ".csv";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Write headers
            for (int i = 1; i <= columnCount; i++) {
                writer.print(escapeCSV(meta.getColumnName(i)));
                if (i < columnCount) writer.print(",");
            }
            writer.println();

            // Write data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    writer.print(escapeCSV(value));
                    if (i < columnCount) writer.print(",");
                }
                writer.println();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("SQL error exporting query: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("IO error exporting query: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the backup directory is ready for use.
     *
     * @return true if directory exists or can be created, false otherwise
     */
    public static boolean isBackupDirectoryReady() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return dir.canWrite();
    }

    /**
     * Gets the backup directory path.
     *
     * @return the backup directory path
     */
    public static String getBackupDirectory() {
        return BACKUP_DIR;
    }

    /**
     * Gets the reports directory path.
     *
     * @return the reports directory path
     */
    public static String getReportsDirectory() {
        return REPORTS_DIR;
    }

    /**
     * Gets a list of all backup files in the backup directory.
     *
     * @return list of backup file names
     */
    public static String[] listBackupFiles() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            return new String[0];
        }

        // FIXED: Convert File[] to String[] using filename method
        File[] files = dir.listFiles((d, name) ->
                name.endsWith(".csv") || name.endsWith(".zip")
        );

        if (files == null || files.length == 0) {
            return new String[0];
        }

        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }
        return fileNames;
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    /**
     * Gets a database connection.
     *
     * @return a Connection object
     * @throws SQLException if connection fails
     */
    private static Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Gets the current timestamp formatted for filenames.
     *
     * @return formatted timestamp string
     */
    private static String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * Escapes a value for CSV format (handles commas and quotes).
     *
     * @param value the value to escape
     * @return escaped value
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Escapes table name to prevent SQL injection.
     *
     * @param tableName the table name
     * @return escaped table name
     */
    private static String escapeTableName(String tableName) {
        // Simple validation - only allow alphanumeric and underscore
        if (!tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        return tableName;
    }
}