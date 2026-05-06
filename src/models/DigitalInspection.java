package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * DigitalInspection model representing digital vehicle inspections.
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
    private int workshopId;  // ADDED
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
    public static final String CONDITION_POOR = "POOR";
    public static final String CONDITION_CRITICAL = "CRITICAL";

    // JavaFX Properties
    private final IntegerProperty serviceRecordIdProperty = new SimpleIntegerProperty();
    private final StringProperty serviceTypeProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final IntegerProperty workshopIdProperty = new SimpleIntegerProperty();  // ADDED
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final StringProperty inspectorNameProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> inspectionDateProperty = new SimpleObjectProperty<>();
    private final StringProperty overallConditionProperty = new SimpleStringProperty();
    private final StringProperty recommendationsProperty = new SimpleStringProperty();
    private final StringProperty conditionDisplayProperty = new SimpleStringProperty();
    private final StringProperty conditionColorProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public DigitalInspection() {
        super();
        this.checklistItems = new ArrayList<>();
        this.inspectionDate = LocalDate.now();

        inspectionDateProperty.set(inspectionDate);
        updateConditionDisplay();
    }

    /**
     * Constructor for creating a new inspection.
     *
     * @param serviceRecordId the service record ID
     * @param inspectorName   the inspector name
     */
    public DigitalInspection(int serviceRecordId, String inspectorName) {
        this();
        this.serviceRecordId = serviceRecordId;
        this.inspectorName = inspectorName;

        serviceRecordIdProperty.set(serviceRecordId);
        inspectorNameProperty.set(inspectorName);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateConditionDisplay() {
        switch (overallCondition) {
            case CONDITION_EXCELLENT:
                conditionDisplayProperty.set("Excellent");
                conditionColorProperty.set("#2ecc71");
                break;
            case CONDITION_GOOD:
                conditionDisplayProperty.set("Good");
                conditionColorProperty.set("#27ae60");
                break;
            case CONDITION_FAIR:
                conditionDisplayProperty.set("Fair");
                conditionColorProperty.set("#f39c12");
                break;
            case CONDITION_POOR:
                conditionDisplayProperty.set("Poor");
                conditionColorProperty.set("#e67e22");
                break;
            case CONDITION_CRITICAL:
                conditionDisplayProperty.set("Critical");
                conditionColorProperty.set("#e74c3c");
                break;
            default:
                conditionDisplayProperty.set(overallCondition != null ? overallCondition : "Not Completed");
                conditionColorProperty.set("#95a5a6");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getServiceRecordId() {
        return serviceRecordId;
    }

    public void setServiceRecordId(int serviceRecordId) {
        this.serviceRecordId = serviceRecordId;
        serviceRecordIdProperty.set(serviceRecordId);
    }

    public IntegerProperty serviceRecordIdProperty() {
        return serviceRecordIdProperty;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
        serviceTypeProperty.set(serviceType);
    }

    public StringProperty serviceTypeProperty() {
        return serviceTypeProperty;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
        vehicleIdProperty.set(vehicleId);
    }

    public IntegerProperty vehicleIdProperty() {
        return vehicleIdProperty;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    // ADDED GETTERS AND SETTERS
    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
        workshopIdProperty.set(workshopId);
    }

    public IntegerProperty workshopIdProperty() {
        return workshopIdProperty;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
        workshopNameProperty.set(workshopName);
    }

    public StringProperty workshopNameProperty() {
        return workshopNameProperty;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
        inspectorNameProperty.set(inspectorName);
    }

    public StringProperty inspectorNameProperty() {
        return inspectorNameProperty;
    }

    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDate inspectionDate) {
        this.inspectionDate = inspectionDate;
        inspectionDateProperty.set(inspectionDate);
    }

    public ObjectProperty<LocalDate> inspectionDateProperty() {
        return inspectionDateProperty;
    }

    public String getOverallCondition() {
        return overallCondition;
    }

    public void setOverallCondition(String overallCondition) {
        this.overallCondition = overallCondition;
        overallConditionProperty.set(overallCondition);
        updateConditionDisplay();
    }

    public StringProperty overallConditionProperty() {
        return overallConditionProperty;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
        recommendationsProperty.set(recommendations);
    }

    public StringProperty recommendationsProperty() {
        return recommendationsProperty;
    }

    public List<InspectionChecklistItem> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<InspectionChecklistItem> checklistItems) {
        this.checklistItems = checklistItems;
    }

    public String getConditionDisplay() {
        return conditionDisplayProperty.get();
    }

    public StringProperty conditionDisplayProperty() {
        return conditionDisplayProperty;
    }

    public String getConditionColor() {
        return conditionColorProperty.get();
    }

    public StringProperty conditionColorProperty() {
        return conditionColorProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedInspectionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return inspectionDate != null ? inspectionDate.format(formatter) : "";
    }

    public int getTotalItems() {
        return checklistItems != null ? checklistItems.size() : 0;
    }

    public int getPassedItems() {
        if (checklistItems == null) return 0;
        return (int) checklistItems.stream()
                .filter(item -> "PASS".equals(item.getStatus()))
                .count();
    }

    public int getFailedItems() {
        if (checklistItems == null) return 0;
        return (int) checklistItems.stream()
                .filter(item -> "FAIL".equals(item.getStatus()))
                .count();
    }

    public int getWarningItems() {
        if (checklistItems == null) return 0;
        return (int) checklistItems.stream()
                .filter(item -> "WARNING".equals(item.getStatus()))
                .count();
    }

    public double getPassRate() {
        int total = getTotalItems();
        if (total == 0) return 0;
        return (double) getPassedItems() / total * 100;
    }

    public boolean isCompleted() {
        return overallCondition != null && !overallCondition.isEmpty();
    }

    public String getVehicleInfo() {
        return registrationNumber != null ? registrationNumber : "Vehicle #" + vehicleId;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return "Inspection #" + id + " - " + getFormattedInspectionDate() + " - " + getConditionDisplay();
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
        copy.setWorkshopId(this.workshopId);
        copy.setWorkshopName(this.workshopName);
        copy.setInspectorName(this.inspectorName);
        copy.setInspectionDate(this.inspectionDate);
        copy.setOverallCondition(this.overallCondition);
        copy.setRecommendations(this.recommendations);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        if (this.checklistItems != null) {
            List<InspectionChecklistItem> copiedItems = new ArrayList<>();
            for (InspectionChecklistItem item : this.checklistItems) {
                copiedItems.add(item.copy());
            }
            copy.setChecklistItems(copiedItems);
        }
        return copy;
    }
}