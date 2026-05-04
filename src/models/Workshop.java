package models;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Workshop model representing vehicle repair workshops.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Workshop extends BaseEntity {

    // Core fields
    private int id;
    private int userId;
    private String workshopName;
    private String address;
    private String phone;
    private String email;
    private String licenseNumber;
    private boolean isApproved;
    private String ownerName;
    private List<Mechanic> mechanics;
    private List<ServiceRecord> serviceRecords;
    private double rating;
    private String operatingHours;
    private String description;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    // JavaFX Properties
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final StringProperty addressProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty ownerNameProperty = new SimpleStringProperty();
    private final StringProperty licenseNumberProperty = new SimpleStringProperty();
    private final BooleanProperty approvedProperty = new SimpleBooleanProperty();
    private final StringProperty approvalStatusProperty = new SimpleStringProperty();
    private final StringProperty approvalColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes empty lists.
     */
    public Workshop() {
        super();
        this.mechanics = new ArrayList<>();
        this.serviceRecords = new ArrayList<>();
        this.isApproved = false;

        approvedProperty.set(false);
        updateApprovalDisplay();

        approvedProperty.addListener((obs, oldVal, newVal) -> updateApprovalDisplay());
    }

    /**
     * Constructor for creating a new workshop.
     *
     * @param userId        the user ID
     * @param workshopName  the workshop name
     * @param address       the address
     * @param phone         the phone number
     * @param email         the email address
     * @param licenseNumber the license number
     */
    public Workshop(int userId, String workshopName, String address, String phone, String email, String licenseNumber) {
        this();
        this.userId = userId;
        this.workshopName = workshopName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.licenseNumber = licenseNumber;

        workshopNameProperty.set(workshopName);
        addressProperty.set(address);
        phoneProperty.set(phone);
        emailProperty.set(email);
        licenseNumberProperty.set(licenseNumber);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateApprovalDisplay() {
        if (isApproved) {
            approvalStatusProperty.set("Approved");
            approvalColorProperty.set("#4CAF50");
        } else {
            approvalStatusProperty.set("Pending Approval");
            approvalColorProperty.set("#FFC107");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
        workshopNameProperty.set(workshopName);
    }
    public StringProperty workshopNameProperty() { return workshopNameProperty; }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        this.address = address;
        addressProperty.set(address);
    }
    public StringProperty addressProperty() { return addressProperty; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
        phoneProperty.set(phone);
    }
    public StringProperty phoneProperty() { return phoneProperty; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }
    public StringProperty emailProperty() { return emailProperty; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        licenseNumberProperty.set(licenseNumber);
    }
    public StringProperty licenseNumberProperty() { return licenseNumberProperty; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) {
        isApproved = approved;
        approvedProperty.set(approved);
    }
    public BooleanProperty approvedProperty() { return approvedProperty; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        ownerNameProperty.set(ownerName);
    }
    public StringProperty ownerNameProperty() { return ownerNameProperty; }

    public List<Mechanic> getMechanics() { return mechanics; }
    public void setMechanics(List<Mechanic> mechanics) { this.mechanics = mechanics; }

    public List<ServiceRecord> getServiceRecords() { return serviceRecords; }
    public void setServiceRecords(List<ServiceRecord> serviceRecords) { this.serviceRecords = serviceRecords; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getApprovalStatus() { return approvalStatusProperty.get(); }
    public StringProperty approvalStatusProperty() { return approvalStatusProperty; }

    public String getApprovalColor() { return approvalColorProperty.get(); }
    public StringProperty approvalColorProperty() { return approvalColorProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public void addMechanic(Mechanic mechanic) {
        if (mechanic != null) {
            this.mechanics.add(mechanic);
        }
    }

    public void addServiceRecord(ServiceRecord record) {
        if (record != null) {
            this.serviceRecords.add(record);
        }
    }

    public void approve() {
        setApproved(true);
    }

    public void reject() {
        setApproved(false);
    }

    public int getMechanicCount() {
        return mechanics != null ? mechanics.size() : 0;
    }

    public int getServiceCount() {
        return serviceRecords != null ? serviceRecords.size() : 0;
    }

    public String getRatingStars() {
        int fullStars = (int) Math.floor(rating);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return workshopName + " - " + address;
    }

    /**
     * Creates a copy of this workshop.
     *
     * @return a new Workshop instance
     */
    public Workshop copy() {
        Workshop copy = new Workshop();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setWorkshopName(this.workshopName);
        copy.setAddress(this.address);
        copy.setPhone(this.phone);
        copy.setEmail(this.email);
        copy.setLicenseNumber(this.licenseNumber);
        copy.setApproved(this.isApproved);
        copy.setOwnerName(this.ownerName);
        copy.setRating(this.rating);
        copy.setOperatingHours(this.operatingHours);
        copy.setDescription(this.description);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        // Note: Doesn't copy mechanics and serviceRecords lists - use separate method if needed
        return copy;
    }
}