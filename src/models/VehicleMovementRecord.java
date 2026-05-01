package models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VehicleMovementRecord extends BaseEntity {
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

    public VehicleMovementRecord() {
        super();
        this.sightings = new ArrayList<>();
        this.segments = new ArrayList<>();
    }

    public VehicleMovementRecord(int vehicleId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this();
        this.vehicleId = vehicleId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public Double getTotalDurationHours() {
        return totalDurationHours;
    }

    public void setTotalDurationHours(Double totalDurationHours) {
        this.totalDurationHours = totalDurationHours;
    }

    public Double getAverageSpeedKmph() {
        return averageSpeedKmph;
    }

    public void setAverageSpeedKmph(Double averageSpeedKmph) {
        this.averageSpeedKmph = averageSpeedKmph;
    }

    public Integer getNumberOfSightings() {
        return numberOfSightings;
    }

    public void setNumberOfSightings(Integer numberOfSightings) {
        this.numberOfSightings = numberOfSightings;
    }

    public Double getSuspiciousScore() {
        return suspiciousScore;
    }

    public void setSuspiciousScore(Double suspiciousScore) {
        this.suspiciousScore = suspiciousScore;
    }

    public List<VehicleSighting> getSightings() {
        return sightings;
    }

    public void setSightings(List<VehicleSighting> sightings) {
        this.sightings = sightings;
        this.numberOfSightings = sightings.size();
        calculateDerivedData();
    }

    public void addSighting(VehicleSighting sighting) {
        this.sightings.add(sighting);
        this.numberOfSightings = this.sightings.size();
        calculateDerivedData();
    }

    public List<MovementSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<MovementSegment> segments) {
        this.segments = segments;
    }

    public void addSegment(MovementSegment segment) {
        this.segments.add(segment);
    }

    private void calculateDerivedData() {
        if (sightings == null || sightings.size() < 2) {
            return;
        }

        // Set start and end points
        VehicleSighting first = sightings.get(0);
        VehicleSighting last = sightings.get(sightings.size() - 1);

        this.startDateTime = first.getTimestamp();
        this.endDateTime = last.getTimestamp();
        this.startLatitude = first.getLatitude();
        this.startLongitude = first.getLongitude();
        this.endLatitude = last.getLatitude();
        this.endLongitude = last.getLongitude();

        // Calculate total duration in hours
        this.totalDurationHours = (double) (endDateTime.toEpochSecond(java.time.ZoneOffset.UTC) -
                startDateTime.toEpochSecond(java.time.ZoneOffset.UTC)) / 3600.0;

        // Calculate suspicious score based on speed patterns and time gaps
        long suspiciousPatterns = sightings.stream()
                .filter(s -> s.getEstimatedSpeed() != null && s.getEstimatedSpeed() > 120)
                .count();

        long largeTimeGaps = 0;
        for (int i = 1; i < sightings.size(); i++) {
            VehicleSighting prev = sightings.get(i - 1);
            VehicleSighting curr = sightings.get(i);
            long gapSeconds = curr.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) -
                    prev.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC);
            if (gapSeconds > 14400) {
                largeTimeGaps++;
            }
        }

        this.suspiciousScore = (suspiciousPatterns * 0.3) + (largeTimeGaps * 0.2);
        if (this.suspiciousScore > 1.0) {
            this.suspiciousScore = 1.0;
        }
    }

    public boolean isSuspicious() {
        return suspiciousScore != null && suspiciousScore > 0.6;
    }

    public String getSuspiciousLevel() {
        if (suspiciousScore == null) return "UNKNOWN";
        if (suspiciousScore >= 0.8) return "VERY_HIGH";
        if (suspiciousScore >= 0.6) return "HIGH";
        if (suspiciousScore >= 0.4) return "MEDIUM";
        if (suspiciousScore >= 0.2) return "LOW";
        return "MINIMAL";
    }

    @Override
    public String toString() {
        return "Movement: " + registrationNumber + " from " + startDateTime + " to " + endDateTime;
    }

    // Inner class for movement segments
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
            this.durationHours = (double) (endTime.toEpochSecond(java.time.ZoneOffset.UTC) -
                    startTime.toEpochSecond(java.time.ZoneOffset.UTC)) / 3600.0;
            if (durationHours > 0 && distanceKm > 0) {
                this.speedKmph = distanceKm / durationHours;
            }
        }

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
            return speedKmph != null && speedKmph > 80;
        }
    }
}