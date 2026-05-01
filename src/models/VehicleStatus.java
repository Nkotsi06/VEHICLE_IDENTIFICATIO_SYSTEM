package models;

public class VehicleStatus extends BaseEntity {
    private String statusName;
    private String description;
    private String colorCode;

    public VehicleStatus() {
        super();
    }

    public VehicleStatus(String statusName, String description, String colorCode) {
        this();
        this.statusName = statusName;
        this.description = description;
        this.colorCode = colorCode;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    @Override
    public String toString() {
        return statusName;
    }
}