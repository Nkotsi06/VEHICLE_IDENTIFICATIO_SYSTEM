package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private static final String DOCUMENTS_DIR = "documents/";
    private static final String REPORTS_DIR = "reports/";
    private static final String LOGS_DIR = "logs/";
    private static final String BACKUPS_DIR = "backups/";

    static {
        createDirectories();
    }

    private static void createDirectories() {
        createDirectory(DOCUMENTS_DIR);
        createDirectory(REPORTS_DIR);
        createDirectory(LOGS_DIR);
        createDirectory(BACKUPS_DIR);
    }

    private static void createDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ============================================
    // FILE OPERATIONS
    // ============================================

    public static boolean saveFile(String directory, String fileName, byte[] content) {
        try {
            createDirectory(directory);
            Path path = Paths.get(directory, fileName);
            Files.write(path, content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] readFile(String directory, String fileName) {
        try {
            Path path = Paths.get(directory, fileName);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteFile(String directory, String fileName) {
        try {
            Path path = Paths.get(directory, fileName);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyFile(String sourceDir, String sourceFile, String destDir, String destFile) {
        try {
            createDirectory(destDir);
            Path source = Paths.get(sourceDir, sourceFile);
            Path dest = Paths.get(destDir, destFile);
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean moveFile(String sourceDir, String sourceFile, String destDir, String destFile) {
        try {
            createDirectory(destDir);
            Path source = Paths.get(sourceDir, sourceFile);
            Path dest = Paths.get(destDir, destFile);
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> listFiles(String directory) {
        List<String> files = new ArrayList<>();
        File dir = new File(directory);
        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files;
    }

    public static List<String> listFilesByExtension(String directory, String extension) {
        List<String> files = new ArrayList<>();
        File dir = new File(directory);
        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile() && file.getName().endsWith(extension)) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files;
    }

    public static boolean fileExists(String directory, String fileName) {
        Path path = Paths.get(directory, fileName);
        return Files.exists(path);
    }

    public static long getFileSize(String directory, String fileName) {
        Path path = Paths.get(directory, fileName);
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }

    public static long getDirectorySize(String directory) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += getDirectorySize(file.getAbsolutePath());
                }
            }
        }
        return size;
    }

    // ============================================
    // TEXT FILE OPERATIONS
    // ============================================

    public static boolean writeTextFile(String directory, String fileName, String content) {
        try {
            createDirectory(directory);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(directory + fileName))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean appendToTextFile(String directory, String fileName, String content) {
        try {
            createDirectory(directory);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(directory + fileName, true))) {
                writer.write(content);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String readTextFile(String directory, String fileName) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(directory + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    public static String getDocumentsPath() {
        return DOCUMENTS_DIR;
    }

    public static String getReportsPath() {
        return REPORTS_DIR;
    }

    public static String getLogsPath() {
        return LOGS_DIR;
    }

    public static String getBackupsPath() {
        return BACKUPS_DIR;
    }

    public static boolean isDirectoryReady(String directory) {
        File dir = new File(directory);
        return dir.exists() || dir.mkdirs();
    }

    public static String getFormattedFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}