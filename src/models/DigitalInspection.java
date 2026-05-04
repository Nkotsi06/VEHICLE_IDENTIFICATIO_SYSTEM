package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DigitalInspection model representing vehicle inspection records.
 * Contains checklist items and overall condition assessment.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class DigitalInspection extends BaseEntity {

    // Core fields
    private int id;
    private int serviceRecordId;
    private String serviceType;
    private int vehicleId;
    private String registrationNumber;
    private String workshopName;
    private String inspectorName;
    private LocalDate inspectionDate;
    private String overallCondition;
    private String recommendations;
    private List<InspectionChecklistItem> checklistItems;

    // Overall condition constants
    public static final String CONDITION_EXCELLENT = "EXCELLENT";
    public static final String CONDITION_GOOD = "GOOD";
    public static final String CONDITION_FAIR = "FAIR";
    public static final String CONDITION_NEEDS_REPAIR = "NEEDS_REPAIR";
    public static final String CONDITION_POOR = "POOR";
    public static final String CONDITION_NOT_CHECKED = "NOT_CHECKED";

    /**
     * Default constructor - initializes empty checklist.
     */
    public DigitalInspection() {
        super();
        this.checklistItems = new ArrayList<>();
        this.overallCondition = CONDITION_NOT_CHECKED;
        this.inspectionDate = LocalDate.now();
    }

    /**
     * Constructor for creating a new inspection.
     *
     * @param serviceRecordId the service record ID
     * @param inspectorName   the inspector's name
     */
    public DigitalInspection(int serviceRecordId, String inspectorName) {
        this();
        this.serviceRecordId = serviceRecordId;
        this.inspectorName = inspectorName;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getServiceRecordId() {
        return serviceRecordId;
    }

    public void setServiceRecordId(int serviceRecordId) {
        this.serviceRecordId = serviceRecordId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDate inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getOverallCondition() {
        return overallCondition;
    }

    public void setOverallCondition(String overallCondition) {
        this.overallCondition = overallCondition;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public List<InspectionChecklistItem> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<InspectionChecklistItem> checklistItems) {
        this.checklistItems = checklistItems;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Adds a checklist item.
     *
     * @param item the item to add
     */
    public void addChecklistItem(InspectionChecklistItem item) {
        if (item != null) {
            this.checklistItems.add(item);
        }
    }

    /**
     * Removes a checklist item.
     *
     * @param item the item to remove
     * @return true if removed, false otherwise
     */
    public boolean removeChecklistItem(InspectionChecklistItem item) {
        return this.checklistItems.remove(item);
    }

    /**
     * Gets total number of checklist items.
     *
     * @return total items count
     */
    public int getTotalItems() {
        return checklistItems.size();
    }

    /**
     * Gets number of passed items (status PASS).
     *
     * @return passed items count
     */
    public int getPassedItems() {
        return (int) checklistItems.stream()
                .filter(i -> i != null && "PASS".equals(i.getStatus()))
                .count();
    }

    /**
     * Gets number of failed items (status FAIL).
     *
     * @return failed items count
     */
    public int getFailedItems() {
        return (int) checklistItems.stream()
                .filter(i -> i != null && "FAIL".equals(i.getStatus()))
                .count();
    }

    /**
     * Gets number of pending items (status PENDING).
     *
     * @return pending items count
     */
    public int getPendingItems() {
        return (int) checklistItems.stream()
                .filter(i -> i != null && "PENDING".equals(i.getStatus()))
                .count();
    }

    /**
     * Gets pass percentage.
     *
     * @return percentage (0-100)
     */
    public double getPassPercentage() {
        if (checklistItems.isEmpty()) return 0.0;
        return (double) getPassedItems() / checklistItems.size() * 100;
    }

    /**
     * Gets fail percentage.
     *
     * @return percentage (0-100)
     */
    public double getFailPercentage() {
        if (checklistItems.isEmpty()) return 0.0;
        return (double) getFailedItems() / checklistItems.size() * 100;
    }

    /**
     * Checks if inspection is complete (no pending items).
     *
     * @return true if complete, false otherwise
     */
    public boolean isComplete() {
        return getPendingItems() == 0 && checklistItems.size() > 0;
    }

    /**
     * Gets the formatted inspection date.
     *
     * @return formatted date string
     */
    public String getFormattedInspectionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return inspectionDate != null ? inspectionDate.format(formatter) : "";
    }

    /**
     * Gets the overall condition display name.
     *
     * @return human-readable condition
     */
    public String getConditionDisplay() {
        switch (overallCondition) {
            case CONDITION_EXCELLENT: return "Excellent";
            case CONDITION_GOOD: return "Good";
            case CONDITION_FAIR: return "Fair";
            case CONDITION_NEEDS_REPAIR: return "Needs Repair";
            case CONDITION_POOR: return "Poor";
            default: return "Not Checked";
        }
    }

    /**
     * Gets the CSS color for the overall condition.
     *
     * @return hex color code
     */
    public String getConditionColor() {
        switch (overallCondition) {
            case CONDITION_EXCELLENT: return "#4CAF50";
            case CONDITION_GOOD: return "#8BC34A";
            case CONDITION_FAIR: return "#FFC107";
            case CONDITION_NEEDS_REPAIR: return "#FF9800";
            case CONDITION_POOR: return "#F44336";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets an item by name.
     *
     * @param itemName the item name
     * @return the item, or null if not found
     */
    public InspectionChecklistItem getItemByName(String itemName) {
        if (itemName == null) return null;
        return checklistItems.stream()
                .filter(i -> i != null && itemName.equalsIgnoreCase(i.getItemName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the overall condition based on checklist results.
     */
    public void autoCalculateOverallCondition() {
        int total = getTotalItems();
        if (total == 0) {
            this.overallCondition = CONDITION_NOT_CHECKED;
            return;
        }

        double passRate = getPassPercentage();

        if (passRate >= 90) {
            this.overallCondition = CONDITION_EXCELLENT;
        } else if (passRate >= 75) {
            this.overallCondition = CONDITION_GOOD;
        } else if (passRate >= 60) {
            this.overallCondition = CONDITION_FAIR;
        } else if (passRate >= 40) {
            this.overallCondition = CONDITION_NEEDS_REPAIR;
        } else {
            this.overallCondition = CONDITION_POOR;
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
        return "Inspection for " + registrationNumber + " - " + getConditionDisplay();
    }

    /**
     * Creates a copy of this inspection.
     *
     * @return a new DigitalInspection instance
     */
    public DigitalInspection copy() {
        DigitalInspection copy = new DigitalInspection();
        copy.setId(this.id);
        copy.setServiceRecordId(this.serviceRecordId);
        copy.setServiceType(this.serviceType);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setWorkshopName(this.workshopName);
        copy.setInspectorName(this.inspectorName);
        copy.setInspectionDate(this.inspectionDate);
        copy.setOverallCondition(this.overallCondition);
        copy.setRecommendations(this.recommendations);

        // Deep copy checklist items
        List<InspectionChecklistItem> itemsCopy = new ArrayList<>();
        for (InspectionChecklistItem item : this.checklistItems) {
            if (item != null) {
                itemsCopy.add(item.copy());
            }
        }
        copy.setChecklistItems(itemsCopy);

        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}