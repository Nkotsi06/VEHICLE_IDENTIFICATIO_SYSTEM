package models;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer extends BaseEntity {
    private int id;
    private int userId;
    private String name;
    private String email;
    private String address;
    private String phone;
    private String nationalId;
    private String driversLicenseNumber;
    private int vehicleCount;
    private List<Vehicle> vehicles;

    // JavaFX Properties
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty addressProperty = new SimpleStringProperty();

    public Customer() {
        super();
        this.vehicles = new ArrayList<>();
        this.vehicleCount = 0;
    }

    public Customer(int userId, String name, String email, String address, String phone) {
        this();
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;

        this.nameProperty.set(name);
        this.emailProperty.set(email);
        this.phoneProperty.set(phone);
        this.addressProperty.set(address);
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        nameProperty.set(name);
    }
    public StringProperty nameProperty() { return nameProperty; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }
    public StringProperty emailProperty() { return emailProperty; }

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

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getDriversLicenseNumber() { return driversLicenseNumber; }
    public void setDriversLicenseNumber(String driversLicenseNumber) { this.driversLicenseNumber = driversLicenseNumber; }

    public int getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(int vehicleCount) { this.vehicleCount = vehicleCount; }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
        this.vehicleCount = vehicles.size();
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
        this.vehicleCount = this.vehicles.size();
    }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return name + " - " + email;
    }
}