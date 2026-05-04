package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * InsuranceDocument model representing documents attached to insurance policies.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InsuranceDocument extends BaseEntity {

    // Core fields
    private int id;
    private int insuranceId;
    private String fileName;
    private String filePath;
    private String documentType;
    private long fileSize;
    private LocalDateTime uploadDate;

    // Document type constants
    public static final String TYPE_POLICY = "POLICY";
    public static final String TYPE_CLAIM = "CLAIM";
    public static final String TYPE_ID_PROOF = "ID_PROOF";
    public static final String TYPE_VEHICLE_PHOTO = "VEHICLE_PHOTO";
    public static final String TYPE_ACCIDENT_REPORT = "ACCIDENT_REPORT";
    public static final String TYPE_OTHER = "OTHER";

    private static final java.util.Map<String, String> TYPE_DISPLAY = new java.util.HashMap<>();
    static {
        TYPE_DISPLAY.put(TYPE_POLICY, "Policy Document");
        TYPE_DISPLAY.put(TYPE_CLAIM, "Claim Document");
        TYPE_DISPLAY.put(TYPE_ID_PROOF, "ID Proof");
        TYPE_DISPLAY.put(TYPE_VEHICLE_PHOTO, "Vehicle Photo");
        TYPE_DISPLAY.put(TYPE_ACCIDENT_REPORT, "Accident Report");
        TYPE_DISPLAY.put(TYPE_OTHER, "Other");
    }

    // JavaFX Properties
    private final StringProperty fileNameProperty = new SimpleStringProperty();
    private final StringProperty documentTypeProperty = new SimpleStringProperty();
    private final LongProperty fileSizeProperty = new SimpleLongProperty();
    private final ObjectProperty<LocalDateTime> uploadDateProperty = new SimpleObjectProperty<>();
    private final StringProperty formattedSizeProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes upload date to now.
     */
    public InsuranceDocument() {
        super();
        this.uploadDate = LocalDateTime.now();
        uploadDateProperty.set(uploadDate);
    }

    /**
     * Constructor for creating a new document.
     *
     * @param insuranceId  the insurance ID
     * @param fileName     the file name
     * @param filePath     the file path
     * @param documentType the document type
     * @param fileSize     the file size in bytes
     */
    public InsuranceDocument(int insuranceId, String fileName, String filePath, String documentType, long fileSize) {
        this();
        this.insuranceId = insuranceId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.documentType = documentType;
        this.fileSize = fileSize;

        fileNameProperty.set(fileName);
        documentTypeProperty.set(documentType);
        fileSizeProperty.set(fileSize);
        formattedSizeProperty.set(formatFileSize(fileSize));
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(int insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        fileNameProperty.set(fileName);
        formattedSizeProperty.set(formatFileSize(fileSize));
    }

    public StringProperty fileNameProperty() {
        return fileNameProperty;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
        documentTypeProperty.set(documentType);
    }

    public StringProperty documentTypeProperty() {
        return documentTypeProperty;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
        fileSizeProperty.set(fileSize);
        formattedSizeProperty.set(formatFileSize(fileSize));
    }

    public LongProperty fileSizeProperty() {
        return fileSizeProperty;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
        uploadDateProperty.set(uploadDate);
    }

    public ObjectProperty<LocalDateTime> uploadDateProperty() {
        return uploadDateProperty;
    }

    public StringProperty formattedSizeProperty() {
        return formattedSizeProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Formats the file size for display.
     *
     * @param bytes the file size in bytes
     * @return formatted string
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Gets the formatted file size.
     *
     * @return formatted size string
     */
    public String getFormattedFileSize() {
        return formattedSizeProperty.get();
    }

    /**
     * Gets the file extension.
     *
     * @return file extension (e.g., "pdf", "jpg")
     */
    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Gets the document type display name.
     *
     * @return human-readable document type
     */
    public String getDocumentTypeDisplay() {
        return TYPE_DISPLAY.getOrDefault(documentType, documentType != null ? documentType : "Unknown");
    }

    /**
     * Gets the formatted upload date.
     *
     * @return formatted date-time string
     */
    public String getFormattedUploadDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return uploadDate != null ? uploadDate.format(formatter) : "";
    }

    /**
     * Gets the icon class for the file type.
     *
     * @return CSS icon class
     */
    public String getFileIconClass() {
        String ext = getFileExtension();
        switch (ext) {
            case "pdf": return "file-pdf";
            case "jpg": case "jpeg": case "png": case "gif": return "file-image";
            case "doc": case "docx": return "file-word";
            case "xls": case "xlsx": return "file-excel";
            case "txt": return "file-text";
            default: return "file";
        }
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return fileName + " (" + getFormattedFileSize() + ") - " + getDocumentTypeDisplay();
    }

    /**
     * Creates a copy of this document.
     *
     * @return a new InsuranceDocument instance
     */
    public InsuranceDocument copy() {
        InsuranceDocument copy = new InsuranceDocument();
        copy.setId(this.id);
        copy.setInsuranceId(this.insuranceId);
        copy.setFileName(this.fileName);
        copy.setFilePath(this.filePath);
        copy.setDocumentType(this.documentType);
        copy.setFileSize(this.fileSize);
        copy.setUploadDate(this.uploadDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}