package models;

import java.time.LocalDateTime;

public class InventoryAlert extends BaseEntity {
    private int partInventoryId;
    private String partName;
    private String alertType;
    private String message;
    private boolean isResolved;
    private LocalDateTime resolvedAt;

    public InventoryAlert() {
        super();
        this.isResolved = false;
    }

    public InventoryAlert(int partInventoryId, String alertType, String message) {
        this();
        this.partInventoryId = partInventoryId;
        this.alertType = alertType;
        this.message = message;
    }

    public int getPartInventoryId() {
        return partInventoryId;
    }

    public void setPartInventoryId(int partInventoryId) {
        this.partInventoryId = partInventoryId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public String toString() {
        return alertType + " - " + message;
    }
}