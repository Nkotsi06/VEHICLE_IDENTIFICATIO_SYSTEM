package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * CrimeHotspotPrediction model representing AI-generated crime hotspot predictions.
 * Used for predictive policing and resource allocation.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class CrimeHotspotPrediction extends BaseEntity {

    // Core fields
    private int id;
    private LocalDate predictionDate;
    private double centerLat;
    private double centerLng;
    private int radiusMeters;
    private String crimeType;
    private double probabilityScore;
    private String riskLevel;

    // Risk level constants
    public static final String RISK_CRITICAL = "CRITICAL";
    public static final String RISK_HIGH = "HIGH";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_LOW = "LOW";

    // Crime type constants
    public static final String CRIME_VEHICLE_THEFT = "VEHICLE_THEFT";
    public static final String CRIME_BURGLARY = "BURGLARY";
    public static final String CRIME_ACCIDENT = "ACCIDENT";
    public static final String CRIME_VANDALISM = "VANDALISM";

    // Default radius
    private static final int DEFAULT_RADIUS_METERS = 500;

    /**
     * Default constructor.
     */
    public CrimeHotspotPrediction() {
        super();
        this.predictionDate = LocalDate.now();
        this.radiusMeters = DEFAULT_RADIUS_METERS;
        this.probabilityScore = 0.0;
        this.riskLevel = RISK_LOW;
    }

    /**
     * Constructor with all parameters.
     *
     * @param predictionDate   the date of the prediction
     * @param centerLat        center latitude
     * @param centerLng        center longitude
     * @param radiusMeters     radius in meters
     * @param crimeType        type of crime
     * @param probabilityScore probability score (0.0 to 1.0)
     * @param riskLevel        risk level
     */
    public CrimeHotspotPrediction(LocalDate predictionDate, double centerLat, double centerLng,
                                  int radiusMeters, String crimeType, double probabilityScore, String riskLevel) {
        this();
        this.predictionDate = predictionDate;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.radiusMeters = radiusMeters;
        this.crimeType = crimeType;
        this.probabilityScore = probabilityScore;
        this.riskLevel = riskLevel;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public LocalDate getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(LocalDate predictionDate) {
        this.predictionDate = predictionDate;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(double centerLng) {
        this.centerLng = centerLng;
    }

    public int getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(int radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(String crimeType) {
        this.crimeType = crimeType;
    }

    public double getProbabilityScore() {
        return probabilityScore;
    }

    public void setProbabilityScore(double probabilityScore) {
        this.probabilityScore = probabilityScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Gets the probability as a percentage.
     *
     * @return percentage value (0-100)
     */
    public double getProbabilityPercentage() {
        return probabilityScore * 100;
    }

    /**
     * Gets the formatted probability percentage.
     *
     * @return formatted percentage string
     */
    public String getFormattedProbability() {
        return String.format("%.1f%%", getProbabilityPercentage());
    }

    /**
     * Gets the CSS color for the risk level.
     *
     * @return hex color code
     */
    public String getRiskLevelColor() {
        switch (riskLevel) {
            case RISK_CRITICAL: return "#F44336";
            case RISK_HIGH: return "#FF9800";
            case RISK_MEDIUM: return "#FFC107";
            case RISK_LOW: return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the risk level display name.
     *
     * @return human-readable risk level
     */
    public String getRiskLevelDisplay() {
        switch (riskLevel) {
            case RISK_CRITICAL: return "Critical";
            case RISK_HIGH: return "High";
            case RISK_MEDIUM: return "Medium";
            case RISK_LOW: return "Low";
            default: return riskLevel;
        }
    }

    /**
     * Gets the crime type display name.
     *
     * @return human-readable crime type
     */
    public String getCrimeTypeDisplay() {
        switch (crimeType) {
            case CRIME_VEHICLE_THEFT: return "Vehicle Theft";
            case CRIME_BURGLARY: return "Burglary";
            case CRIME_ACCIDENT: return "Accident";
            case CRIME_VANDALISM: return "Vandalism";
            default: return crimeType != null ? crimeType.replace("_", " ") : "Unknown";
        }
    }

    /**
     * Gets the formatted prediction date.
     *
     * @return formatted date string
     */
    public String getFormattedPredictionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return predictionDate != null ? predictionDate.format(formatter) : "";
    }

    /**
     * Gets the radius in kilometers.
     *
     * @return radius in kilometers
     */
    public double getRadiusKm() {
        return radiusMeters / 1000.0;
    }

    /**
     * Gets the formatted radius.
     *
     * @return formatted radius string
     */
    public String getFormattedRadius() {
        if (radiusMeters >= 1000) {
            return String.format("%.1f km", getRadiusKm());
        }
        return radiusMeters + " m";
    }

    /**
     * Checks if this is a high-risk prediction.
     *
     * @return true if risk is CRITICAL or HIGH
     */
    public boolean isHighRisk() {
        return RISK_CRITICAL.equals(riskLevel) || RISK_HIGH.equals(riskLevel);
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
        return getCrimeTypeDisplay() + " - " + getRiskLevelDisplay() + " - " + getFormattedProbability();
    }

    /**
     * Creates a copy of this prediction.
     *
     * @return a new CrimeHotspotPrediction instance
     */
    public CrimeHotspotPrediction copy() {
        CrimeHotspotPrediction copy = new CrimeHotspotPrediction();
        copy.setId(this.id);
        copy.setPredictionDate(this.predictionDate);
        copy.setCenterLat(this.centerLat);
        copy.setCenterLng(this.centerLng);
        copy.setRadiusMeters(this.radiusMeters);
        copy.setCrimeType(this.crimeType);
        copy.setProbabilityScore(this.probabilityScore);
        copy.setRiskLevel(this.riskLevel);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}