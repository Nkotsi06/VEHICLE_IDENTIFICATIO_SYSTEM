package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for file system operations.
 * Handles reading, writing, copying, and managing files and directories.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class FileHandler {

    private static final String DOCUMENTS_DIR = "documents/";
    private static final String REPORTS_DIR = "reports/";
    private static final String LOGS_DIR = "logs/";
    private static final String BACKUPS_DIR = "backups/";
    private static final String TEMP_DIR = "temp/";

    static {
        createAllDirectories();
    }

    private FileHandler() {} // Prevent instantiation

    /**
     * Creates all application directories if they don't exist.
     */
    private static void createAllDirectories() {
        createDirectory(DOCUMENTS_DIR);
        createDirectory(REPORTS_DIR);
        createDirectory(LOGS_DIR);
        createDirectory(BACKUPS_DIR);
        createDirectory(TEMP_DIR);
    }

    /**
     * Creates a single directory if it doesn't exist.
     *
     * @param dirPath the directory path to create
     */
    private static void createDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created directory: " + dirPath);
            } else {
                System.err.println("Failed to create directory: " + dirPath);
            }
        }
    }

    /**
     * Saves byte content to a file.
     *
     * @param directory the target directory
     * @param fileName  the file name
     * @param content   the byte content to save
     * @return true if save was successful, false otherwise
     */
    public static boolean saveFile(String directory, String fileName, byte[] content) {
        if (directory == null || fileName == null || content == null) {
            return false;
        }

        try {
            createDirectory(directory);
            Path path = Paths.get(directory, fileName);
            Files.write(path, content);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save file: " + directory + fileName);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads a file's content as bytes.
     *
     * @param directory the source directory
     * @param fileName  the file name
     * @return byte array of file content, or null if error
     */
    public static byte[] readFile(String directory, String fileName) {
        if (directory == null || fileName == null) {
            return null;
        }

        try {
            Path path = Paths.get(directory, fileName);
            if (!Files.exists(path)) {
                System.err.println("File not found: " + directory + fileName);
                return null;
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + directory + fileName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a file.
     *
     * @param directory the directory containing the file
     * @param fileName  the file name
     * @return true if file was deleted, false otherwise
     */
    public static boolean deleteFile(String directory, String fileName) {
        if (directory == null || fileName == null) {
            return false;
        }

        try {
            Path path = Paths.get(directory, fileName);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + directory + fileName);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copies a file from source to destination.
     *
     * @param sourceDir the source directory
     * @param sourceFile the source file name
     * @param destDir    the destination directory
     * @param destFile   the destination file name
     * @return true if copy was successful, false otherwise
     */
    public static boolean copyFile(String sourceDir, String sourceFile, String destDir, String destFile) {
        if (sourceDir == null || sourceFile == null || destDir == null || destFile == null) {
            return false;
        }

        try {
            createDirectory(destDir);
            Path source = Paths.get(sourceDir, sourceFile);
            if (!Files.exists(source)) {
                System.err.println("Source file not found: " + sourceDir + sourceFile);
                return false;
            }
            Path dest = Paths.get(destDir, destFile);
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to copy file from " + sourceDir + sourceFile + " to " + destDir + destFile);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Moves a file from source to destination.
     *
     * @param sourceDir the source directory
     * @param sourceFile the source file name
     * @param destDir    the destination directory
     * @param destFile   the destination file name
     * @return true if move was successful, false otherwise
     */
    public static boolean moveFile(String sourceDir, String sourceFile, String destDir, String destFile) {
        if (sourceDir == null || sourceFile == null || destDir == null || destFile == null) {
            return false;
        }

        try {
            createDirectory(destDir);
            Path source = Paths.get(sourceDir, sourceFile);
            if (!Files.exists(source)) {
                System.err.println("Source file not found: " + sourceDir + sourceFile);
                return false;
            }
            Path dest = Paths.get(destDir, destFile);
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to move file from " + sourceDir + sourceFile + " to " + destDir + destFile);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lists all files in a directory.
     *
     * @param directory the directory to list
     * @return list of file names, or empty list if directory doesn't exist
     */
    public static List<String> listFiles(String directory) {
        List<String> files = new ArrayList<>();
        if (directory == null) return files;

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

    /**
     * Lists files in a directory with a specific extension.
     *
     * @param directory the directory to search
     * @param extension the file extension (e.g., ".csv", ".txt")
     * @return list of matching file names
     */
    public static List<String> listFilesByExtension(String directory, String extension) {
        List<String> files = new ArrayList<>();
        if (directory == null || extension == null) return files;

        String ext = extension.startsWith(".") ? extension : "." + extension;
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(ext.toLowerCase())) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files;
    }

    /**
     * Checks if a file exists.
     *
     * @param directory the directory
     * @param fileName  the file name
     * @return true if file exists, false otherwise
     */
    public static boolean fileExists(String directory, String fileName) {
        if (directory == null || fileName == null) return false;
        Path path = Paths.get(directory, fileName);
        return Files.exists(path);
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param directory the directory
     * @param fileName  the file name
     * @return file size in bytes, or 0 if error
     */
    public static long getFileSize(String directory, String fileName) {
        if (directory == null || fileName == null) return 0;

        Path path = Paths.get(directory, fileName);
        try {
            return Files.exists(path) ? Files.size(path) : 0;
        } catch (IOException e) {
            System.err.println("Failed to get file size: " + directory + fileName);
            return 0;
        }
    }

    /**
     * Recursively calculates the size of a directory.
     *
     * @param directory the directory path
     * @return total size in bytes
     */
    public static long getDirectorySize(String directory) {
        if (directory == null) return 0;

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

    /**
     * Writes text content to a file.
     *
     * @param directory the target directory
     * @param fileName  the file name
     * @param content   the text content to write
     * @return true if write was successful, false otherwise
     */
    public static boolean writeTextFile(String directory, String fileName, String content) {
        if (directory == null || fileName == null || content == null) {
            return false;
        }

        try {
            createDirectory(directory);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(directory + fileName))) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write text file: " + directory + fileName);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Appends text content to an existing file.
     *
     * @param directory the target directory
     * @param fileName  the file name
     * @param content   the text to append
     * @return true if append was successful, false otherwise
     */
    public static boolean appendToTextFile(String directory, String fileName, String content) {
        if (directory == null || fileName == null || content == null) {
            return false;
        }

        try {
            createDirectory(directory);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(directory + fileName, true))) {
                writer.write(content);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to append to text file: " + directory + fileName);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads text content from a file.
     *
     * @param directory the source directory
     * @param fileName  the file name
     * @return file content as string, or null if error
     */
    public static String readTextFile(String directory, String fileName) {
        if (directory == null || fileName == null) return null;

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(directory + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            System.err.println("Failed to read text file: " + directory + fileName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copies an input stream to a file.
     *
     * @param directory the target directory
     * @param fileName  the file name
     * @param input     the input stream
     * @return true if copy was successful, false otherwise
     */
    public static boolean copyFromStream(String directory, String fileName, InputStream input) {
        if (directory == null || fileName == null || input == null) {
            return false;
        }

        try {
            createDirectory(directory);
            Path path = Paths.get(directory, fileName);
            Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to copy from stream to file: " + directory + fileName);
            e.printStackTrace();
            return false;
        }
    }

    // ============================================
    // GETTER METHODS
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

    public static String getTempPath() {
        return TEMP_DIR;
    }

    /**
     * Checks if a directory exists and is ready for use.
     *
     * @param directory the directory path
     * @return true if directory exists or can be created
     */
    public static boolean isDirectoryReady(String directory) {
        if (directory == null) return false;

        File dir = new File(directory);
        return dir.exists() || dir.mkdirs();
    }

    /**
     * Formats a file size in bytes to a human-readable string.
     *
     * @param bytes the size in bytes
     * @return formatted string (e.g., "1.5 MB")
     */
    public static String getFormattedFileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param directory the directory to delete
     * @return true if directory was deleted, false otherwise
     */
    public static boolean deleteDirectory(String directory) {
        if (directory == null) return false;

        File dir = new File(directory);
        if (!dir.exists()) return true;

        return deleteDirectoryRecursive(dir);
    }

    private static boolean deleteDirectoryRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursive(file);
                } else {
                    file.delete();
                }
            }
        }
        return dir.delete();
    }
}