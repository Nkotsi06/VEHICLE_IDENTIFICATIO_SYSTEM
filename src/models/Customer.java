package models;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 * Customer model representing vehicle owners/customers.
 * Contains personal information and associated vehicles.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Customer extends BaseEntity {

    // Core fields
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

    // JavaFX Properties for TableView binding
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty addressProperty = new SimpleStringProperty();
    private final StringProperty nationalIdProperty = new SimpleStringProperty();
    private final StringProperty driversLicenseProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleCountProperty = new SimpleIntegerProperty();

    /**
     * Default constructor - initializes empty vehicle list.
     */
    public Customer() {
        super();
        this.vehicles = new ArrayList<>();
        this.vehicleCount = 0;
        vehicleCountProperty.set(0);
    }

    /**
     * Constructor for creating a customer with basic info.
     *
     * @param userId   the associated user ID
     * @param name     the customer's full name
     * @param email    the customer's email
     * @param address  the customer's address
     * @param phone    the customer's phone number
     */
    public Customer(int userId, String name, String email, String address, String phone) {
        this();
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;

        userIdProperty.set(userId);
        nameProperty.set(name);
        emailProperty.set(email);
        phoneProperty.set(phone);
        addressProperty.set(address);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        userIdProperty.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userIdProperty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        nameProperty.set(name);
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }

    public StringProperty emailProperty() {
        return emailProperty;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        addressProperty.set(address);
    }

    public StringProperty addressProperty() {
        return addressProperty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        phoneProperty.set(phone);
    }

    public StringProperty phoneProperty() {
        return phoneProperty;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
        nationalIdProperty.set(nationalId);
    }

    public StringProperty nationalIdProperty() {
        return nationalIdProperty;
    }

    public String getDriversLicenseNumber() {
        return driversLicenseNumber;
    }

    public void setDriversLicenseNumber(String driversLicenseNumber) {
        this.driversLicenseNumber = driversLicenseNumber;
        driversLicenseProperty.set(driversLicenseNumber);
    }

    public StringProperty driversLicenseProperty() {
        return driversLicenseProperty;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int vehicleCount) {
        this.vehicleCount = vehicleCount;
        vehicleCountProperty.set(vehicleCount);
    }

    public IntegerProperty vehicleCountProperty() {
        return vehicleCountProperty;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
        this.vehicleCount = vehicles != null ? vehicles.size() : 0;
        vehicleCountProperty.set(this.vehicleCount);
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Adds a vehicle to the customer's collection.
     *
     * @param vehicle the vehicle to add
     */
    public void addVehicle(Vehicle vehicle) {
        if (vehicle != null && !this.vehicles.contains(vehicle)) {
            this.vehicles.add(vehicle);
            this.vehicleCount = this.vehicles.size();
            vehicleCountProperty.set(this.vehicleCount);
        }
    }

    /**
     * Removes a vehicle from the customer's collection.
     *
     * @param vehicle the vehicle to remove
     * @return true if removed, false otherwise
     */
    public boolean removeVehicle(Vehicle vehicle) {
        boolean removed = this.vehicles.remove(vehicle);
        if (removed) {
            this.vehicleCount = this.vehicles.size();
            vehicleCountProperty.set(this.vehicleCount);
        }
        return removed;
    }

    /**
     * Gets a vehicle by registration number.
     *
     * @param registrationNumber the registration number to search for
     * @return the vehicle, or null if not found
     */
    public Vehicle getVehicleByRegistration(String registrationNumber) {
        if (registrationNumber == null) return null;

        for (Vehicle vehicle : vehicles) {
            if (registrationNumber.equalsIgnoreCase(vehicle.getRegistrationNumber())) {
                return vehicle;
            }
        }
        return null;
    }

    /**
     * Gets the display name (full name with email).
     *
     * @return formatted display name
     */
    public String getDisplayName() {
        return name + " (" + email + ")";
    }

    /**
     * Gets the short display name.
     *
     * @return name only
     */
    public String getShortName() {
        return name;
    }

    /**
     * Validates that all required fields are present.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty();
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
        return name + " - " + email;
    }

    /**
     * Creates a copy of this customer.
     *
     * @return a new Customer instance with the same values
     */
    public Customer copy() {
        Customer copy = new Customer();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setName(this.name);
        copy.setEmail(this.email);
        copy.setAddress(this.address);
        copy.setPhone(this.phone);
        copy.setNationalId(this.nationalId);
        copy.setDriversLicenseNumber(this.driversLicenseNumber);
        copy.setVehicleCount(this.vehicleCount);
        copy.setVehicles(new ArrayList<>(this.vehicles));
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}