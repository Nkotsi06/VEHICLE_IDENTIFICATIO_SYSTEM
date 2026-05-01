package utils;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import database.DatabaseConnection;

public class BackupUtil {

    private static final String BACKUP_DIR = "backups/";
    private static final String REPORTS_DIR = "reports/";

    static {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean createDatabaseBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return exportTablesAsCSV(timestamp);
    }

    public static boolean createFullSystemBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String zipFileName = BACKUP_DIR + "full_system_backup_" + timestamp + ".zip";

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName))) {

            String[] tables = {"users", "customers", "vehicles", "violations",
                    "insurance_policies", "service_records", "workshops",
                    "police_reports", "stolen_vehicles", "warrants",
                    "notifications", "audit_logs", "insurance_claims"};

            for (String table : tables) {
                String csvContent = exportTableToCSVString(table);
                if (csvContent != null && !csvContent.isEmpty()) {
                    ZipEntry entry = new ZipEntry("database/" + table + ".csv");
                    zipOut.putNextEntry(entry);
                    zipOut.write(csvContent.getBytes());
                    zipOut.closeEntry();
                    System.out.println("Backed up table: " + table);
                }
            }

            addDirectoryToZip(new File("documents/"), "documents/", zipOut);
            addDirectoryToZip(new File("logs/"), "logs/", zipOut);

            System.out.println("Full system backup created: " + zipFileName);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean exportTablesAsCSV(String timestamp) {
        String[] tables = {"users", "customers", "vehicles", "violations",
                "insurance_policies", "service_records", "workshops"};

        boolean allSuccess = true;

        for (String table : tables) {
            String csvFile = BACKUP_DIR + table + "_" + timestamp + ".csv";
            if (!exportTableToCSV(table, csvFile)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    private static boolean exportTableToCSV(String tableName, String filePath) {
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                writer.print(meta.getColumnName(i));
                if (i < columnCount) writer.print(",");
            }
            writer.println();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null && (value.contains(",") || value.contains("\""))) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    writer.print(value != null ? value : "");
                    if (i < columnCount) writer.print(",");
                }
                writer.println();
            }

            return true;
        } catch (Exception e) {
            System.err.println("Failed to export " + tableName + ": " + e.getMessage());
            return false;
        }
    }

    private static String exportTableToCSVString(String tableName) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                sb.append(meta.getColumnName(i));
                if (i < columnCount) sb.append(",");
            }
            sb.append("\n");

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null && (value.contains(",") || value.contains("\""))) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    sb.append(value != null ? value : "");
                    if (i < columnCount) sb.append(",");
                }
                sb.append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            System.err.println("Failed to export " + tableName + " to string: " + e.getMessage());
            return null;
        }
    }

    private static void addDirectoryToZip(File dir, String zipPath, ZipOutputStream zipOut) throws IOException {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    ZipEntry entry = new ZipEntry(zipPath + file.getName());
                    zipOut.putNextEntry(entry);
                    Files.copy(file.toPath(), zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    public static boolean restoreFromBackup(String backupFilePath) {
        if (backupFilePath == null || !new File(backupFilePath).exists()) {
            System.err.println("Backup file not found: " + backupFilePath);
            return false;
        }

        if (backupFilePath.endsWith(".zip")) {
            return restoreFromZipBackup(backupFilePath);
        } else if (backupFilePath.endsWith(".csv")) {
            return restoreFromCSVBackup(backupFilePath);
        } else {
            return false;
        }
    }

    private static boolean restoreFromZipBackup(String zipFilePath) {
        try (java.util.zip.ZipInputStream zipIn = new java.util.zip.ZipInputStream(new FileInputStream(zipFilePath))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                System.out.println("Found backup entry: " + entry.getName());
                zipIn.closeEntry();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean restoreFromCSVBackup(String csvFilePath) {
        System.out.println("CSV restore from: " + csvFilePath);
        return true;
    }

    public static boolean exportQueryToCSV(String query, String fileName) {
        String filePath = REPORTS_DIR + fileName + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                writer.print(meta.getColumnName(i));
                if (i < columnCount) writer.print(",");
            }
            writer.println();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null && (value.contains(",") || value.contains("\""))) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    writer.print(value != null ? value : "");
                    if (i < columnCount) writer.print(",");
                }
                writer.println();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isBackupDirectoryReady() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return dir.canWrite();
    }

    public static String getBackupDirectory() {
        return BACKUP_DIR;
    }
}