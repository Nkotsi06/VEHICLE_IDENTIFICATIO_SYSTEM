package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneOffset;

/**
 * VehicleMovementRecord model representing reconstructed vehicle movement paths.
 * Used for tracking suspicious vehicle activity.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleMovementRecord extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private Double totalDistanceKm;
    private Double totalDurationHours;
    private Double averageSpeedKmph;
    private Integer numberOfSightings;
    private Double suspiciousScore;
    private List<VehicleSighting> sightings;
    private List<MovementSegment> segments;

    // Suspicious score thresholds
    public static final double VERY_HIGH_THRESHOLD = 0.8;
    public static final double HIGH_THRESHOLD = 0.6;
    public static final double MEDIUM_THRESHOLD = 0.4;
    public static final double LOW_THRESHOLD = 0.2;

    // Speed thresholds
    public static final double SPEEDING_THRESHOLD = 120.0;
    public static final long LARGE_GAP_THRESHOLD_SECONDS = 14400; // 4 hours

    /**
     * Default constructor - initializes empty lists.
     */
    public VehicleMovementRecord() {
        super();
        this.sightings = new ArrayList<>();
        this.segments = new ArrayList<>();
        this.suspiciousScore = 0.0;
    }

    /**
     * Constructor for creating a movement record.
     *
     * @param vehicleId     the vehicle ID
     * @param startDateTime the start date-time
     * @param endDateTime   the end date-time
     */
    public VehicleMovementRecord(int vehicleId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this();
        this.vehicleId = vehicleId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public Double getStartLatitude() { return startLatitude; }
    public void setStartLatitude(Double startLatitude) { this.startLatitude = startLatitude; }

    public Double getStartLongitude() { return startLongitude; }
    public void setStartLongitude(Double startLongitude) { this.startLongitude = startLongitude; }

    public Double getEndLatitude() { return endLatitude; }
    public void setEndLatitude(Double endLatitude) { this.endLatitude = endLatitude; }

    public Double getEndLongitude() { return endLongitude; }
    public void setEndLongitude(Double endLongitude) { this.endLongitude = endLongitude; }

    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public Double getTotalDurationHours() { return totalDurationHours; }
    public void setTotalDurationHours(Double totalDurationHours) { this.totalDurationHours = totalDurationHours; }

    public Double getAverageSpeedKmph() { return averageSpeedKmph; }
    public void setAverageSpeedKmph(Double averageSpeedKmph) { this.averageSpeedKmph = averageSpeedKmph; }

    public Integer getNumberOfSightings() { return numberOfSightings; }
    public void setNumberOfSightings(Integer numberOfSightings) { this.numberOfSightings = numberOfSightings; }

    public Double getSuspiciousScore() { return suspiciousScore; }
    public void setSuspiciousScore(Double suspiciousScore) { this.suspiciousScore = suspiciousScore; }

    public List<VehicleSighting> getSightings() { return sightings; }
    public void setSightings(List<VehicleSighting> sightings) {
        this.sightings = sightings;
        if (sightings != null) {
            this.numberOfSightings = sightings.size();
        }
        calculateDerivedData();
    }

    public void addSighting(VehicleSighting sighting) {
        if (sighting != null) {
            this.sightings.add(sighting);
            this.numberOfSightings = this.sightings.size();
            calculateDerivedData();
        }
    }

    public List<MovementSegment> getSegments() { return segments; }
    public void setSegments(List<MovementSegment> segments) { this.segments = segments; }

    public void addSegment(MovementSegment segment) {
        if (segment != null) {
            this.segments.add(segment);
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    private void calculateDerivedData() {
        if (sightings == null || sightings.size() < 2) {
            return;
        }

        // Set start and end points from sightings
        VehicleSighting first = sightings.get(0);
        VehicleSighting last = sightings.get(sightings.size() - 1);

        this.startDateTime = first.getTimestamp();
        this.endDateTime = last.getTimestamp();
        this.startLatitude = first.getLatitude();
        this.startLongitude = first.getLongitude();
        this.endLatitude = last.getLatitude();
        this.endLongitude = last.getLongitude();

        // Calculate total duration in hours
        if (startDateTime != null && endDateTime != null) {
            this.totalDurationHours = (double) (endDateTime.toEpochSecond(ZoneOffset.UTC) -
                    startDateTime.toEpochSecond(ZoneOffset.UTC)) / 3600.0;
        }

        // Calculate suspicious score based on speed patterns and time gaps
        long suspiciousPatterns = sightings.stream()
                .filter(s -> s.getEstimatedSpeed() != null && s.getEstimatedSpeed() > SPEEDING_THRESHOLD)
                .count();

        long largeTimeGaps = 0;
        for (int i = 1; i < sightings.size(); i++) {
            VehicleSighting prev = sightings.get(i - 1);
            VehicleSighting curr = sightings.get(i);
            if (prev != null && curr != null && prev.getTimestamp() != null && curr.getTimestamp() != null) {
                long gapSeconds = curr.getTimestamp().toEpochSecond(ZoneOffset.UTC) -
                        prev.getTimestamp().toEpochSecond(ZoneOffset.UTC);
                if (gapSeconds > LARGE_GAP_THRESHOLD_SECONDS) {
                    largeTimeGaps++;
                }
            }
        }

        double suspiciousScoreValue = (suspiciousPatterns * 0.3) + (largeTimeGaps * 0.2);
        if (suspiciousScoreValue > 1.0) {
            suspiciousScoreValue = 1.0;
        }
        this.suspiciousScore = suspiciousScoreValue;
    }

    public boolean isSuspicious() {
        return suspiciousScore != null && suspiciousScore > MEDIUM_THRESHOLD;
    }

    public boolean isHighlySuspicious() {
        return suspiciousScore != null && suspiciousScore > HIGH_THRESHOLD;
    }

    public String getSuspiciousLevel() {
        if (suspiciousScore == null) return "UNKNOWN";
        if (suspiciousScore >= VERY_HIGH_THRESHOLD) return "VERY_HIGH";
        if (suspiciousScore >= HIGH_THRESHOLD) return "HIGH";
        if (suspiciousScore >= MEDIUM_THRESHOLD) return "MEDIUM";
        if (suspiciousScore >= LOW_THRESHOLD) return "LOW";
        return "MINIMAL";
    }

    public String getSuspiciousLevelDisplay() {
        switch (getSuspiciousLevel()) {
            case "VERY_HIGH": return "Very High";
            case "HIGH": return "High";
            case "MEDIUM": return "Medium";
            case "LOW": return "Low";
            case "MINIMAL": return "Minimal";
            default: return "Unknown";
        }
    }

    public String getSuspiciousColor() {
        switch (getSuspiciousLevel()) {
            case "VERY_HIGH": return "#D32F2F";
            case "HIGH": return "#F44336";
            case "MEDIUM": return "#FF9800";
            case "LOW": return "#FFC107";
            case "MINIMAL": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    public String getFormattedStartDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return startDateTime != null ? startDateTime.format(formatter) : "";
    }

    public String getFormattedEndDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return endDateTime != null ? endDateTime.format(formatter) : "";
    }

    public String getFormattedTotalDistance() {
        if (totalDistanceKm == null) return "N/A";
        return String.format("%.2f km", totalDistanceKm);
    }

    public String getFormattedAverageSpeed() {
        if (averageSpeedKmph == null) return "N/A";
        if (averageSpeedKmph > SPEEDING_THRESHOLD) {
            return String.format("%.1f km/h (Speeding)", averageSpeedKmph);
        }
        return String.format("%.1f km/h", averageSpeedKmph);
    }

    public String getFormattedTotalDuration() {
        if (totalDurationHours == null) return "N/A";
        if (totalDurationHours < 1) {
            return String.format("%.0f minutes", totalDurationHours * 60);
        }
        return String.format("%.1f hours", totalDurationHours);
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Movement: " + registrationNumber + " from " + getFormattedStartDateTime() + " to " + getFormattedEndDateTime();
    }

    // ============================================
    // INNER CLASS - MovementSegment
    // ============================================

    /**
     * Represents a segment of movement between two sightings.
     */
    public static class MovementSegment {
        private int segmentNumber;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double startLat;
        private Double startLng;
        private Double endLat;
        private Double endLng;
        private Double distanceKm;
        private Double durationHours;
        private Double speedKmph;
        private String sourceType;

        public MovementSegment() {}

        public MovementSegment(int segmentNumber, LocalDateTime startTime, LocalDateTime endTime,
                               Double startLat, Double startLng, Double endLat, Double endLng,
                               Double distanceKm, String sourceType) {
            this.segmentNumber = segmentNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.startLat = startLat;
            this.startLng = startLng;
            this.endLat = endLat;
            this.endLng = endLng;
            this.distanceKm = distanceKm;
            this.sourceType = sourceType;

            if (startTime != null && endTime != null) {
                this.durationHours = (double) (endTime.toEpochSecond(ZoneOffset.UTC) -
                        startTime.toEpochSecond(ZoneOffset.UTC)) / 3600.0;
                if (durationHours > 0 && distanceKm > 0) {
                    this.speedKmph = distanceKm / durationHours;
                }
            }
        }

        // Getters and Setters
        public int getSegmentNumber() { return segmentNumber; }
        public void setSegmentNumber(int segmentNumber) { this.segmentNumber = segmentNumber; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public Double getStartLat() { return startLat; }
        public void setStartLat(Double startLat) { this.startLat = startLat; }
        public Double getStartLng() { return startLng; }
        public void setStartLng(Double startLng) { this.startLng = startLng; }
        public Double getEndLat() { return endLat; }
        public void setEndLat(Double endLat) { this.endLat = endLat; }
        public Double getEndLng() { return endLng; }
        public void setEndLng(Double endLng) { this.endLng = endLng; }
        public Double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
        public Double getDurationHours() { return durationHours; }
        public void setDurationHours(Double durationHours) { this.durationHours = durationHours; }
        public Double getSpeedKmph() { return speedKmph; }
        public void setSpeedKmph(Double speedKmph) { this.speedKmph = speedKmph; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }

        public boolean isSpeeding() {
            return speedKmph != null && speedKmph > SPEEDING_THRESHOLD;
        }

        public String getFormattedDistance() {
            if (distanceKm == null) return "N/A";
            if (distanceKm < 1) return String.format("%.0f meters", distanceKm * 1000);
            return String.format("%.2f km", distanceKm);
        }

        public String getFormattedSpeed() {
            if (speedKmph == null) return "N/A";
            if (isSpeeding()) {
                return String.format("%.1f km/h (Speeding)", speedKmph);
            }
            return String.format("%.1f km/h", speedKmph);
        }

        public String getFormattedDuration() {
            if (durationHours == null) return "N/A";
            if (durationHours < 1) return String.format("%.0f minutes", durationHours * 60);
            return String.format("%.1f hours", durationHours);
        }
    }
}