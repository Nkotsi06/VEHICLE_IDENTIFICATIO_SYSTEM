package models;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Warrant extends BaseEntity {
    private int id;
    private int violationId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String judgeName;
    private String status;
    private int vehicleId;
    private String registrationNumber;
    private double fineAmount;

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty judgeNameProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final DoubleProperty fineAmountProperty = new SimpleDoubleProperty();

    public Warrant() {
        super();
    }

    public Warrant(int violationId, LocalDate issueDate, LocalDate expiryDate, String judgeName) {
        this();
        this.violationId = violationId;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.judgeName = judgeName;
        this.status = "ACTIVE";

        // Update properties
        issueDateProperty.set(issueDate);
        expiryDateProperty.set(expiryDate);
        judgeNameProperty.set(judgeName);
        statusProperty.set("ACTIVE");
    }

    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        issueDateProperty.set(issueDate);
    }

    public ObjectProperty<LocalDate> issueDateProperty() {
        return issueDateProperty;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDateProperty;
    }

    public String getJudgeName() {
        return judgeName;
    }

    public void setJudgeName(String judgeName) {
        this.judgeName = judgeName;
        judgeNameProperty.set(judgeName);
    }

    public StringProperty judgeNameProperty() {
        return judgeNameProperty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }

    public StringProperty statusProperty() {
        return statusProperty;
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

    public boolean isActive() {
        return "ACTIVE".equals(status) && expiryDate != null && expiryDate.isAfter(LocalDate.now());
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
        return "Warrant for " + registrationNumber + " - " + status;
    }
}