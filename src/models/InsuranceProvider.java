package models;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * InsuranceProvider model representing an insurance company/provider.
 * Uses JavaFX properties for TableView binding.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InsuranceProvider {

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";

    // Core properties
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty registrationNumber = new SimpleStringProperty();
    private final StringProperty licenseNumber = new SimpleStringProperty();
    private final StringProperty contactPhone = new SimpleStringProperty();
    private final StringProperty contactEmail = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final ObjectProperty<Double> rating = new SimpleObjectProperty<>();
    private final StringProperty coverageDetails = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Related fields from joined queries
    private final StringProperty username = new SimpleStringProperty();

    // Computed properties
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();
    private final StringProperty ratingDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public InsuranceProvider() {
        status.set(STATUS_PENDING_APPROVAL);
        rating.set(0.0);
        createdAt.set(LocalDateTime.now());
        updatedAt.set(LocalDateTime.now());

        status.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
        rating.addListener((obs, oldVal, newVal) -> updateRatingDisplay());

        updateStatusDisplay();
        updateRatingDisplay();
    }

    /**
     * Constructor with essential fields.
     *
     * @param name     the provider name
     * @param email    the contact email
     * @param phone    the contact phone
     */
    public InsuranceProvider(String name, String email, String phone) {
        this();
        this.name.set(name);
        this.contactEmail.set(email);
        this.contactPhone.set(phone);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        String currentStatus = status.get();
        switch (currentStatus) {
            case STATUS_ACTIVE:
                statusDisplayProperty.set("Active");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_INACTIVE:
                statusDisplayProperty.set("Inactive");
                statusColorProperty.set("#9E9E9E");
                break;
            case STATUS_SUSPENDED:
                statusDisplayProperty.set("Suspended");
                statusColorProperty.set("#FF9800");
                break;
            case STATUS_PENDING_APPROVAL:
                statusDisplayProperty.set("Pending Approval");
                statusColorProperty.set("#FFC107");
                break;
            default:
                statusDisplayProperty.set(currentStatus);
                statusColorProperty.set("#9E9E9E");
        }
    }

    private void updateRatingDisplay() {
        Double currentRating = rating.get();
        if (currentRating == null || currentRating == 0.0) {
            ratingDisplayProperty.set("Not Rated");
        } else {
            ratingDisplayProperty.set(String.format("%.1f ★", currentRating));
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status.get());
    }

    public boolean isPendingApproval() {
        return STATUS_PENDING_APPROVAL.equals(status.get());
    }

    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt.get() != null ? createdAt.get().format(formatter) : "";
    }

    public String getFormattedRating() {
        Double currentRating = rating.get();
        if (currentRating == null || currentRating == 0.0) {
            return "Not Rated";
        }
        return String.format("%.1f / 5.0", currentRating);
    }

    public int getRatingStars() {
        Double currentRating = rating.get();
        if (currentRating == null) return 0;
        return (int) Math.round(currentRating);
    }

    public String getRatingStarsDisplay() {
        int stars = getRatingStars();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append("★");
        }
        for (int i = stars; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }

    public void approve() {
        this.status.set(STATUS_ACTIVE);
    }

    public void suspend() {
        this.status.set(STATUS_SUSPENDED);
    }

    public void deactivate() {
        this.status.set(STATUS_INACTIVE);
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getRegistrationNumber() { return registrationNumber.get(); }
    public void setRegistrationNumber(String value) { registrationNumber.set(value); }
    public StringProperty registrationNumberProperty() { return registrationNumber; }

    public String getLicenseNumber() { return licenseNumber.get(); }
    public void setLicenseNumber(String value) { licenseNumber.set(value); }
    public StringProperty licenseNumberProperty() { return licenseNumber; }

    public String getContactPhone() { return contactPhone.get(); }
    public void setContactPhone(String value) { contactPhone.set(value); }
    public StringProperty contactPhoneProperty() { return contactPhone; }

    public String getContactEmail() { return contactEmail.get(); }
    public void setContactEmail(String value) { contactEmail.set(value); }
    public StringProperty contactEmailProperty() { return contactEmail; }

    public String getAddress() { return address.get(); }
    public void setAddress(String value) { address.set(value); }
    public StringProperty addressProperty() { return address; }

    public Double getRating() { return rating.get(); }
    public void setRating(Double value) { rating.set(value); }
    public ObjectProperty<Double> ratingProperty() { return rating; }

    public String getCoverageDetails() { return coverageDetails.get(); }
    public void setCoverageDetails(String value) { coverageDetails.set(value); }
    public StringProperty coverageDetailsProperty() { return coverageDetails; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }

    // Computed property getters
    public String getStatusDisplay() { return statusDisplayProperty.get(); }
    public StringProperty statusDisplayProperty() { return statusDisplayProperty; }

    public String getStatusColor() { return statusColorProperty.get(); }
    public StringProperty statusColorProperty() { return statusColorProperty; }

    public String getRatingDisplay() { return ratingDisplayProperty.get(); }
    public StringProperty ratingDisplayProperty() { return ratingDisplayProperty; }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return name.get() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this provider.
     *
     * @return a new InsuranceProvider instance
     */
    public InsuranceProvider copy() {
        InsuranceProvider copy = new InsuranceProvider();
        copy.setId(this.getId());
        copy.setUserId(this.getUserId());
        copy.setName(this.getName());
        copy.setRegistrationNumber(this.getRegistrationNumber());
        copy.setLicenseNumber(this.getLicenseNumber());
        copy.setContactPhone(this.getContactPhone());
        copy.setContactEmail(this.getContactEmail());
        copy.setAddress(this.getAddress());
        copy.setRating(this.getRating());
        copy.setCoverageDetails(this.getCoverageDetails());
        copy.setStatus(this.getStatus());
        copy.setUsername(this.getUsername());
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}