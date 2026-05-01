package models;

import javafx.beans.property.*;

public class InsuranceProvider {
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
    private final ObjectProperty<java.time.LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<java.time.LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Related fields from joined queries
    private final StringProperty username = new SimpleStringProperty();

    // Getters and Setters
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

    public java.time.LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(java.time.LocalDateTime value) { createdAt.set(value); }
    public ObjectProperty<java.time.LocalDateTime> createdAtProperty() { return createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(java.time.LocalDateTime value) { updatedAt.set(value); }
    public ObjectProperty<java.time.LocalDateTime> updatedAtProperty() { return updatedAt; }

    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }
}