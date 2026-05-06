package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * VehicleRiskScore model representing calculated risk scores for vehicles.
 * Used for insurance premium calculations and risk assessment.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleRiskScore extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private double riskScore;
    private String riskFactors;
    private LocalDate lastCalculationDate;
    private String riskLevel;

    // ADDED FIELDS
    private String make;
    private String model;

    // Risk level constants
    public static final String RISK_CRITICAL = "CRITICAL";
    public static final String RISK_HIGH = "HIGH";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_LOW = "LOW";
    public static final String RISK_MINIMAL = "MINIMAL";

    // Score thresholds
    public static final double THRESHOLD_CRITICAL = 0.8;
    public static final double THRESHOLD_HIGH = 0.6;
    public static final double THRESHOLD_MEDIUM = 0.4;
    public static final double THRESHOLD_LOW = 0.2;

    // JavaFX Properties for TableView binding
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final DoubleProperty riskScoreProperty = new SimpleDoubleProperty();
    private final StringProperty riskFactorsProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> lastCalculationDateProperty = new SimpleObjectProperty<>();
    private final StringProperty riskLevelProperty = new SimpleStringProperty();
    private final StringProperty riskLevelDisplayProperty = new SimpleStringProperty();
    private final StringProperty riskColorProperty = new SimpleStringProperty();
    private final StringProperty formattedScoreProperty = new SimpleStringProperty();

    // ADDED PROPERTIES
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public VehicleRiskScore() {
        super();
        this.riskScore = 0.0;
        this.riskLevel = RISK_MINIMAL;

        riskScoreProperty.set(0.0);
        riskLevelProperty.set(RISK_MINIMAL);
        updateDisplayProperties();

        riskScoreProperty.addListener((obs, oldVal, newVal) -> {
            updateRiskLevel();
            updateDisplayProperties();
        });
    }

    /**
     * Constructor for creating a new risk score.
     *
     * @param vehicleId          the vehicle ID
     * @param riskScore          the risk score (0.0 to 1.0)
     * @param riskFactors        the risk factors description
     * @param lastCalculationDate the last calculation date
     */
    public VehicleRiskScore(int vehicleId, double riskScore, String riskFactors, LocalDate lastCalculationDate) {
        this();
        this.vehicleId = vehicleId;
        this.riskScore = riskScore;
        this.riskFactors = riskFactors;
        this.lastCalculationDate = lastCalculationDate;

        vehicleIdProperty.set(vehicleId);
        riskScoreProperty.set(riskScore);
        riskFactorsProperty.set(riskFactors);
        lastCalculationDateProperty.set(lastCalculationDate);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateRiskLevel() {
        if (riskScore >= THRESHOLD_CRITICAL) {
            this.riskLevel = RISK_CRITICAL;
        } else if (riskScore >= THRESHOLD_HIGH) {
            this.riskLevel = RISK_HIGH;
        } else if (riskScore >= THRESHOLD_MEDIUM) {
            this.riskLevel = RISK_MEDIUM;
        } else if (riskScore >= THRESHOLD_LOW) {
            this.riskLevel = RISK_LOW;
        } else {
            this.riskLevel = RISK_MINIMAL;
        }
        riskLevelProperty.set(this.riskLevel);
    }

    private void updateDisplayProperties() {
        // Update risk level display
        switch (riskLevel) {
            case RISK_CRITICAL:
                riskLevelDisplayProperty.set("Critical");
                riskColorProperty.set("#D32F2F");
                break;
            case RISK_HIGH:
                riskLevelDisplayProperty.set("High");
                riskColorProperty.set("#F44336");
                break;
            case RISK_MEDIUM:
                riskLevelDisplayProperty.set("Medium");
                riskColorProperty.set("#FF9800");
                break;
            case RISK_LOW:
                riskLevelDisplayProperty.set("Low");
                riskColorProperty.set("#FFC107");
                break;
            case RISK_MINIMAL:
                riskLevelDisplayProperty.set("Minimal");
                riskColorProperty.set("#4CAF50");
                break;
            default:
                riskLevelDisplayProperty.set(riskLevel);
                riskColorProperty.set("#9E9E9E");
        }

        // Update formatted score
        formattedScoreProperty.set(String.format("%.1f%%", riskScore * 100));
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

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

    // ADDED GETTERS AND SETTERS
    public String getMake() { return make; }
    public void setMake(String make) {
        this.make = make;
        makeProperty.set(make);
    }
    public StringProperty makeProperty() { return makeProperty; }

    public String getModel() { return model; }
    public void setModel(String model) {
        this.model = model;
        modelProperty.set(model);
    }
    public StringProperty modelProperty() { return modelProperty; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
        riskScoreProperty.set(riskScore);
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
        updateDisplayProperties();
    }
    public StringProperty riskLevelProperty() { return riskLevelProperty; }

    public String getRiskLevelDisplay() { return riskLevelDisplayProperty.get(); }
    public StringProperty riskLevelDisplayProperty() { return riskLevelDisplayProperty; }

    public String getRiskColor() { return riskColorProperty.get(); }
    public StringProperty riskColorProperty() { return riskColorProperty; }

    public String getFormattedScore() { return formattedScoreProperty.get(); }
    public StringProperty formattedScoreProperty() { return formattedScoreProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedLastCalculationDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return lastCalculationDate != null ? lastCalculationDate.format(formatter) : "";
    }

    public boolean isHighRisk() {
        return RISK_HIGH.equals(riskLevel) || RISK_CRITICAL.equals(riskLevel);
    }

    public boolean isCriticalRisk() {
        return RISK_CRITICAL.equals(riskLevel);
    }

    public double getRiskPercentage() {
        return riskScore * 100;
    }

    public String getRiskFactorsPreview() {
        if (riskFactors == null) return "";
        if (riskFactors.length() <= 100) return riskFactors;
        return riskFactors.substring(0, 100) + "...";
    }

    public String getVehicleInfo() {
        String info = registrationNumber != null ? registrationNumber : "";
        if (make != null && model != null) {
            info += " (" + make + " " + model + ")";
        }
        return info;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return registrationNumber + " - Risk: " + getRiskLevelDisplay() + " (" + getFormattedScore() + ")";
    }

    /**
     * Creates a copy of this risk score.
     *
     * @return a new VehicleRiskScore instance
     */
    public VehicleRiskScore copy() {
        VehicleRiskScore copy = new VehicleRiskScore();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setRiskScore(this.riskScore);
        copy.setRiskFactors(this.riskFactors);
        copy.setLastCalculationDate(this.lastCalculationDate);
        copy.setRiskLevel(this.riskLevel);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}