package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DigitalInspection extends BaseEntity {
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

    public DigitalInspection() {
        super();
        this.checklistItems = new ArrayList<>();
        this.overallCondition = "NOT_CHECKED";
    }

    public DigitalInspection(int serviceRecordId, String inspectorName) {
        this();
        this.serviceRecordId = serviceRecordId;
        this.inspectorName = inspectorName;
        this.inspectionDate = LocalDate.now();
    }

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

    public void addChecklistItem(InspectionChecklistItem item) {
        this.checklistItems.add(item);
    }

    public int getTotalItems() {
        return checklistItems.size();
    }

    public int getPassedItems() {
        return (int) checklistItems.stream().filter(i -> "PASS".equals(i.getStatus())).count();
    }

    public int getFailedItems() {
        return (int) checklistItems.stream().filter(i -> "FAIL".equals(i.getStatus())).count();
    }

    public double getPassPercentage() {
        if (checklistItems.isEmpty()) return 0;
        return (double) getPassedItems() / checklistItems.size() * 100;
    }

    @Override
    public String toString() {
        return "Inspection for " + registrationNumber + " - " + overallCondition;
    }
}