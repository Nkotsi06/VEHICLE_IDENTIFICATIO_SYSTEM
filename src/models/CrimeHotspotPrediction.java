package models;

import java.time.LocalDate;

public class CrimeHotspotPrediction extends BaseEntity {
    private LocalDate predictionDate;
    private double centerLat;
    private double centerLng;
    private int radiusMeters;
    private String crimeType;
    private double probabilityScore;
    private String riskLevel;

    public CrimeHotspotPrediction() {
        super();
    }

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

    public double getProbabilityPercentage() {
        return probabilityScore * 100;
    }

    public String getRiskLevelColor() {
        switch (riskLevel) {
            case "CRITICAL": return "#F44336";
            case "HIGH": return "#FF9800";
            case "MEDIUM": return "#FFC107";
            case "LOW": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    @Override
    public String toString() {
        return crimeType + " - " + riskLevel + " - " + String.format("%.1f", getProbabilityPercentage()) + "%";
    }
}