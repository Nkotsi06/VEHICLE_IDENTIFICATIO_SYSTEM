package models;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Violation extends BaseEntity {
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private LocalDate violationDate;
    private String violationType;
    private double fineAmount;
    private String paymentStatus;
    private String location;
    private String officerName;
    private String description;  // ADDED: missing field

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty violationTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> violationDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty fineAmountProperty = new SimpleDoubleProperty();
    private final StringProperty paymentStatusProperty = new SimpleStringProperty();
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();  // ADDED

    public Violation() {
        super();
    }

    public Violation(int vehicleId, LocalDate violationDate, String violationType, double fineAmount, String officerName) {
        this();
        this.vehicleId = vehicleId;
        this.violationDate = violationDate;
        this.violationType = violationType;
        this.fineAmount = fineAmount;
        this.officerName = officerName;
        this.paymentStatus = "UNPAID";
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
        registrationNumberProperty.set(registrationNumber);
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public LocalDate getViolationDate() {
        return violationDate;
    }

    public void setViolationDate(LocalDate violationDate) {
        this.violationDate = violationDate;
        violationDateProperty.set(violationDate);
    }

    public ObjectProperty<LocalDate> violationDateProperty() {
        return violationDateProperty;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
        violationTypeProperty.set(violationType);
    }

    public StringProperty violationTypeProperty() {
        return violationTypeProperty;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
        fineAmountProperty.set(fineAmount);
    }

    public DoubleProperty fineAmountProperty() {
        return fineAmountProperty;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        paymentStatusProperty.set(paymentStatus);
    }

    public StringProperty paymentStatusProperty() {
        return paymentStatusProperty;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
        officerNameProperty.set(officerName);
    }

    public StringProperty officerNameProperty() {
        return officerNameProperty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }

    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }

    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }

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
        return violationType + " - " + violationDate + " - $" + fineAmount;
    }
}