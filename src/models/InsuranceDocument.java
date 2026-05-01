package models;

import java.time.LocalDateTime;

public class InsuranceDocument extends BaseEntity {
    private int insuranceId;
    private String fileName;
    private String filePath;
    private String documentType;
    private long fileSize;
    private LocalDateTime uploadDate;

    public InsuranceDocument() {
        super();
        this.uploadDate = LocalDateTime.now();
    }

    public InsuranceDocument(int insuranceId, String fileName, String filePath, String documentType, long fileSize) {
        this();
        this.insuranceId = insuranceId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.documentType = documentType;
        this.fileSize = fileSize;
    }

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
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public String toString() {
        return fileName + " (" + getFormattedFileSize() + ") - " + documentType;
    }
}