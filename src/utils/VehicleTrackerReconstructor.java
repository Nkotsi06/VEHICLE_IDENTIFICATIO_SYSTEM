package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.VehicleMovementRecordDAO;
import dao.VehicleSightingDAO;
import models.VehicleMovementRecord;
import models.VehicleSighting;

public class VehicleTrackerReconstructor {

    private static VehicleTrackerReconstructor instance;
    private VehicleSightingDAO sightingDAO;
    private VehicleMovementRecordDAO movementDAO;
    private RouteCalculator routeCalculator;
    private TimestampOrderingUtil timestampUtil;

    private VehicleTrackerReconstructor() {
        this.sightingDAO = new VehicleSightingDAO();
        this.movementDAO = new VehicleMovementRecordDAO();
        this.routeCalculator = new RouteCalculator();
        this.timestampUtil = new TimestampOrderingUtil();
    }

    public static synchronized VehicleTrackerReconstructor getInstance() {
        if (instance == null) {
            instance = new VehicleTrackerReconstructor();
        }
        return instance;
    }

    public VehicleMovementRecord reconstructMovement(int vehicleId, LocalDate startDate, LocalDate endDate) {
        try {
            return movementDAO.reconstructMovement(vehicleId, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new VehicleMovementRecord();
        }
    }

    public VehicleMovementRecord reconstructMovementWithMap(int vehicleId, LocalDate startDate, LocalDate endDate) {
        try {
            return movementDAO.getReconstructionWithMap(vehicleId, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new VehicleMovementRecord();
        }
    }

    public List<VehicleSighting> getSightingsForVehicle(int vehicleId) {
        try {
            return sightingDAO.findByVehicleId(vehicleId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<VehicleSighting> getSightingsForVehicleByDateRange(int vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return sightingDAO.findByVehicleAndDateRange(vehicleId, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ReconstructionReport generateReconstructionReport(int vehicleId, LocalDate startDate, LocalDate endDate) {
        ReconstructionReport report = new ReconstructionReport();
        report.vehicleId = vehicleId;
        report.startDate = startDate;
        report.endDate = endDate;

        VehicleMovementRecord movement = reconstructMovement(vehicleId, startDate, endDate);
        List<VehicleSighting> sightings = getSightingsForVehicleByDateRange(
                vehicleId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
        );

        report.sightings = sightings;
        report.sightingCount = sightings.size();
        report.totalDistance = routeCalculator.calculateTotalDistance(sightings);
        report.averageSpeed = routeCalculator.calculateAverageSpeed(sightings);
        report.segments = routeCalculator.calculateRouteSegments(sightings);
        report.timeGaps = timestampUtil.findTimeGaps(sightings, 7200);

        report.suspiciousSegments = new ArrayList<>();
        report.suspiciousTimeGaps = new ArrayList<>();

        for (RouteCalculator.RouteSegment segment : report.segments) {
            if (segment.isSpeeding()) {
                report.suspiciousSegments.add(segment);
            }
        }

        for (TimestampOrderingUtil.TimeGap gap : report.timeGaps) {
            if (gap.gapSeconds > 14400) {
                report.suspiciousTimeGaps.add(gap);
            }
        }

        report.suspiciousScore = calculateSuspiciousScore(report);
        report.recommendations = generateRecommendations(report);

        return report;
    }

    private double calculateSuspiciousScore(ReconstructionReport report) {
        double score = 0;

        if (report.suspiciousSegments.size() > 0) {
            score += Math.min(0.5, report.suspiciousSegments.size() * 0.1);
        }

        if (report.suspiciousTimeGaps.size() > 0) {
            score += Math.min(0.3, report.suspiciousTimeGaps.size() * 0.15);
        }

        if (report.sightingCount < 3 && report.totalDistance > 50) {
            score += 0.2;
        }

        if (report.averageSpeed > 120) {
            score += 0.2;
        }

        return Math.min(1.0, score);
    }

    private List<String> generateRecommendations(ReconstructionReport report) {
        List<String> recommendations = new ArrayList<>();

        if (report.suspiciousScore >= 0.7) {
            recommendations.add("HIGH PRIORITY: Vehicle movement pattern is highly suspicious. Immediate investigation recommended.");
        } else if (report.suspiciousScore >= 0.4) {
            recommendations.add("MEDIUM PRIORITY: Vehicle movement shows unusual patterns. Further monitoring advised.");
        }

        if (report.suspiciousSegments.size() > 0) {
            recommendations.add("Speeding detected in " + report.suspiciousSegments.size() + " segments.");
        }

        if (report.suspiciousTimeGaps.size() > 0) {
            recommendations.add("Large time gaps detected. Possible data loss or intentional avoidance of surveillance.");
        }

        if (report.sightingCount < 5 && report.totalDistance > 100) {
            recommendations.add("Low sighting count for distance traveled. Vehicle may be avoiding detection.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("No suspicious activity detected in this time period.");
        }

        return recommendations;
    }

    public Map<String, Object> generateTimelineJson(VehicleMovementRecord record) {
        Map<String, Object> timeline = new HashMap<>();
        List<Map<String, Object>> events = new ArrayList<>();

        for (VehicleSighting sighting : record.getSightings()) {
            Map<String, Object> event = new HashMap<>();
            event.put("timestamp", sighting.getTimestamp().toString());
            event.put("latitude", sighting.getLatitude());
            event.put("longitude", sighting.getLongitude());
            event.put("source", sighting.getSourceType());
            event.put("sequence", sighting.getSequenceNumber());
            events.add(event);
        }

        timeline.put("events", events);
        timeline.put("totalEvents", events.size());
        timeline.put("startTime", record.getStartDateTime());
        timeline.put("endTime", record.getEndDateTime());
        timeline.put("totalDistanceKm", record.getTotalDistanceKm());
        timeline.put("averageSpeedKmph", record.getAverageSpeedKmph());
        timeline.put("suspiciousScore", record.getSuspiciousScore());

        return timeline;
    }

    public void addTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                         double latitude, double longitude, LocalDateTime timestamp) {
        try {
            sightingDAO.insertTrafficCameraSighting(vehicleId, licensePlate, cameraId, latitude, longitude, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                double latitude, double longitude, LocalDateTime timestamp, double confidence) {
        try {
            sightingDAO.insertANPRSighting(vehicleId, licensePlate, anprDeviceId, latitude, longitude, timestamp, confidence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ReconstructionReport {
        public int vehicleId;
        public String registrationNumber;
        public LocalDate startDate;
        public LocalDate endDate;
        public List<VehicleSighting> sightings;
        public int sightingCount;
        public double totalDistance;
        public double averageSpeed;
        public List<RouteCalculator.RouteSegment> segments;
        public List<TimestampOrderingUtil.TimeGap> timeGaps;
        public List<RouteCalculator.RouteSegment> suspiciousSegments;
        public List<TimestampOrderingUtil.TimeGap> suspiciousTimeGaps;
        public double suspiciousScore;
        public List<String> recommendations;

        public String getSuspiciousLevel() {
            if (suspiciousScore >= 0.7) return "HIGH";
            if (suspiciousScore >= 0.4) return "MEDIUM";
            if (suspiciousScore >= 0.2) return "LOW";
            return "MINIMAL";
        }

        public String getSuspiciousColor() {
            switch (getSuspiciousLevel()) {
                case "HIGH": return "#F44336";
                case "MEDIUM": return "#FF9800";
                case "LOW": return "#FFC107";
                default: return "#4CAF50";
            }
        }
    }
}