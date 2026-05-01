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

public class VehicleRiskScore extends BaseEntity {
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private double riskScore;
    private String riskFactors;
    private LocalDate lastCalculationDate;
    private String riskLevel;

    // JavaFX Properties for TableView binding
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final DoubleProperty riskScoreProperty = new SimpleDoubleProperty();
    private final StringProperty riskFactorsProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> lastCalculationDateProperty = new SimpleObjectProperty<>();
    private final StringProperty riskLevelProperty = new SimpleStringProperty();

    public VehicleRiskScore() {
        super();
    }

    public VehicleRiskScore(int vehicleId, double riskScore, String riskFactors, LocalDate lastCalculationDate) {
        this();
        this.vehicleId = vehicleId;
        this.riskScore = riskScore;
        this.riskFactors = riskFactors;
        this.lastCalculationDate = lastCalculationDate;
        updateRiskLevel();

        this.vehicleIdProperty.set(vehicleId);
        this.riskScoreProperty.set(riskScore);
        this.riskFactorsProperty.set(riskFactors);
        this.lastCalculationDateProperty.set(lastCalculationDate);
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
        vehicleIdProperty.set(vehicleId);
    }
    public IntegerProperty vehicleIdProperty() { return vehicleIdProperty; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
        riskScoreProperty.set(riskScore);
        updateRiskLevel();
    }
    public DoubleProperty riskScoreProperty() { return riskScoreProperty; }

    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
        riskFactorsProperty.set(riskFactors);
    }
    public StringProperty riskFactorsProperty() { return riskFactorsProperty; }

    public LocalDate getLastCalculationDate() { return lastCalculationDate; }
    public void setLastCalculationDate(LocalDate lastCalculationDate) {
        this.lastCalculationDate = lastCalculationDate;
        lastCalculationDateProperty.set(lastCalculationDate);
    }
    public ObjectProperty<LocalDate> lastCalculationDateProperty() { return lastCalculationDateProperty; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
        riskLevelProperty.set(riskLevel);
    }
    public StringProperty riskLevelProperty() { return riskLevelProperty; }

    private void updateRiskLevel() {
        if (riskScore >= 0.8) {
            this.riskLevel = "CRITICAL";
        } else if (riskScore >= 0.6) {
            this.riskLevel = "HIGH";
        } else if (riskScore >= 0.4) {
            this.riskLevel = "MEDIUM";
        } else if (riskScore >= 0.2) {
            this.riskLevel = "LOW";
        } else {
            this.riskLevel = "MINIMAL";
        }
        riskLevelProperty.set(this.riskLevel);
    }

    public String getRiskLevelColor() {
        switch (riskLevel) {
            case "CRITICAL": return "#D32F2F";
            case "HIGH": return "#F44336";
            case "MEDIUM": return "#FF9800";
            case "LOW": return "#FFC107";
            case "MINIMAL": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return registrationNumber + " - Risk: " + riskLevel + " (" + String.format("%.1f", riskScore * 100) + "%)";
    }
}