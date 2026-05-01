package models;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Workshop extends BaseEntity {
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

    // JavaFX Properties
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final StringProperty addressProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty ownerNameProperty = new SimpleStringProperty();

    public Workshop() {
        super();
        this.mechanics = new ArrayList<>();
        this.serviceRecords = new ArrayList<>();
        this.isApproved = false;
    }

    public Workshop(int userId, String workshopName, String address, String phone, String email, String licenseNumber) {
        this();
        this.userId = userId;
        this.workshopName = workshopName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.licenseNumber = licenseNumber;

        this.workshopNameProperty.set(workshopName);
        this.addressProperty.set(address);
        this.phoneProperty.set(phone);
        this.emailProperty.set(email);
    }

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
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

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

    public void addMechanic(Mechanic mechanic) { this.mechanics.add(mechanic); }
    public void addServiceRecord(ServiceRecord record) { this.serviceRecords.add(record); }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return workshopName + " - " + address;
    }
}