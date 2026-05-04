package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.VehicleMovementRecordDAO;
import dao.VehicleSightingDAO;
import models.VehicleMovementRecord;
import models.VehicleSighting;

/**
 * VehicleTrackerReconstructor reconstructs vehicle movement paths and generates reports.
 * Integrates sightings, geospatial calculations, and suspicious activity detection.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleTrackerReconstructor {

    private static final Logger LOGGER = Logger.getLogger(VehicleTrackerReconstructor.class.getName());
    private static VehicleTrackerReconstructor instance;

    private VehicleSightingDAO sightingDAO;
    private VehicleMovementRecordDAO movementDAO;
    private RouteCalculator routeCalculator;
    private TimestampOrderingUtil timestampUtil;

    // Suspicious detection thresholds
    private static final double HIGH_SPEED_THRESHOLD = 120.0;
    private static final long SUSPICIOUS_GAP_THRESHOLD = 14400; // 4 hours
    private static final double HIGH_SUSPICIOUS_SCORE = 0.7;
    private static final double MEDIUM_SUSPICIOUS_SCORE = 0.4;

    /**
     * Private constructor for singleton pattern.
     */
    private VehicleTrackerReconstructor() {
        try {
            this.sightingDAO = new VehicleSightingDAO();
            this.movementDAO = new VehicleMovementRecordDAO();
            this.routeCalculator = new RouteCalculator();
            this.timestampUtil = new TimestampOrderingUtil();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize VehicleTrackerReconstructor", e);
        }
    }

    /**
     * Gets the singleton instance.
     *
     * @return the VehicleTrackerReconstructor instance
     */
    public static synchronized VehicleTrackerReconstructor getInstance() {
        if (instance == null) {
            instance = new VehicleTrackerReconstructor();
        }
        return instance;
    }

    /**
     * Reconstructs vehicle movement for a date range.
     *
     * @param vehicleId the vehicle ID
     * @param startDate start date
     * @param endDate   end date
     * @return VehicleMovementRecord containing movement data
     */
    public VehicleMovementRecord reconstructMovement(int vehicleId, LocalDate startDate, LocalDate endDate) {
        if (vehicleId <= 0 || startDate == null || endDate == null) {
            LOGGER.warning("Invalid parameters for reconstructMovement");
            return new VehicleMovementRecord();
        }

        try {
            return movementDAO.reconstructMovement(vehicleId, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reconstruct movement for vehicle " + vehicleId, e);
            return new VehicleMovementRecord();
        }
    }

    /**
     * Reconstructs movement with map data.
     *
     * @param vehicleId the vehicle ID
     * @param startDate start date
     * @param endDate   end date
     * @return VehicleMovementRecord with map data
     */
    public VehicleMovementRecord reconstructMovementWithMap(int vehicleId, LocalDate startDate, LocalDate endDate) {
        if (vehicleId <= 0 || startDate == null || endDate == null) {
            LOGGER.warning("Invalid parameters for reconstructMovementWithMap");
            return new VehicleMovementRecord();
        }

        try {
            return movementDAO.getReconstructionWithMap(vehicleId, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reconstruct movement with map for vehicle " + vehicleId, e);
            return new VehicleMovementRecord();
        }
    }

    /**
     * Gets all sightings for a vehicle.
     *
     * @param vehicleId the vehicle ID
     * @return list of sightings
     */
    public List<VehicleSighting> getSightingsForVehicle(int vehicleId) {
        if (vehicleId <= 0) {
            return new ArrayList<>();
        }

        try {
            return sightingDAO.findByVehicleId(vehicleId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get sightings for vehicle " + vehicleId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets sightings for a vehicle within a date range.
     *
     * @param vehicleId the vehicle ID
     * @param startDate start date-time
     * @param endDate   end date-time
     * @return filtered list of sightings
     */
    public List<VehicleSighting> getSightingsForVehicleByDateRange(int vehicleId,
                                                                   LocalDateTime startDate,
                                                                   LocalDateTime endDate) {
        if (vehicleId <= 0 || startDate == null || endDate == null) {
            return new ArrayList<>();
        }

        try {
            return sightingDAO.findByVehicleAndDateRange(vehicleId, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get sightings by date range for vehicle " + vehicleId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Generates a complete reconstruction report.
     *
     * @param vehicleId the vehicle ID
     * @param startDate start date
     * @param endDate   end date
     * @return ReconstructionReport with analysis
     */
    public ReconstructionReport generateReconstructionReport(int vehicleId, LocalDate startDate, LocalDate endDate) {
        ReconstructionReport report = new ReconstructionReport();
        report.vehicleId = vehicleId;
        report.startDate = startDate;
        report.endDate = endDate;

        if (vehicleId <= 0 || startDate == null || endDate == null) {
            report.recommendations = List.of("Invalid parameters for report generation");
            return report;
        }

        try {
            List<VehicleSighting> sightings = getSightingsForVehicleByDateRange(
                    vehicleId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
            );

            report.sightings = sightings;
            report.sightingCount = sightings.size();
            report.totalDistance = routeCalculator.calculateTotalDistance(sightings);
            report.averageSpeed = routeCalculator.calculateAverageSpeed(sightings);
            report.segments = routeCalculator.calculateRouteSegments(sightings);
            report.timeGaps = TimestampOrderingUtil.findTimeGaps(sightings);

            report.suspiciousSegments = new ArrayList<>();
            report.suspiciousTimeGaps = new ArrayList<>();

            for (RouteCalculator.RouteSegment segment : report.segments) {
                if (segment.isSpeeding()) {
                    report.suspiciousSegments.add(segment);
                }
            }

            for (TimestampOrderingUtil.TimeGap gap : report.timeGaps) {
                if (gap.gapSeconds > SUSPICIOUS_GAP_THRESHOLD) {
                    report.suspiciousTimeGaps.add(gap);
                }
            }

            report.suspiciousScore = calculateSuspiciousScore(report);
            report.recommendations = generateRecommendations(report);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate reconstruction report", e);
            report.recommendations = List.of("Error generating report: " + e.getMessage());
        }

        return report;
    }

    /**
     * Calculates a suspiciousness score based on various factors.
     *
     * @param report the reconstruction report
     * @return score between 0.0 and 1.0
     */
    private double calculateSuspiciousScore(ReconstructionReport report) {
        double score = 0.0;

        if (report.suspiciousSegments.size() > 0) {
            score += Math.min(0.5, report.suspiciousSegments.size() * 0.1);
        }

        if (report.suspiciousTimeGaps.size() > 0) {
            score += Math.min(0.3, report.suspiciousTimeGaps.size() * 0.15);
        }

        if (report.sightingCount < 3 && report.totalDistance > 50) {
            score += 0.2;
        }

        if (report.averageSpeed > HIGH_SPEED_THRESHOLD) {
            score += 0.2;
        }

        return Math.min(1.0, score);
    }

    /**
     * Generates recommendations based on suspicious activity.
     *
     * @param report the reconstruction report
     * @return list of recommendation strings
     */
    private List<String> generateRecommendations(ReconstructionReport report) {
        List<String> recommendations = new ArrayList<>();

        if (report.suspiciousScore >= HIGH_SUSPICIOUS_SCORE) {
            recommendations.add("HIGH PRIORITY: Vehicle movement pattern is highly suspicious. Immediate investigation recommended.");
        } else if (report.suspiciousScore >= MEDIUM_SUSPICIOUS_SCORE) {
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

    /**
     * Adds a traffic camera sighting.
     *
     * @param vehicleId   the vehicle ID
     * @param licensePlate the license plate
     * @param cameraId    the camera ID
     * @param latitude    latitude
     * @param longitude   longitude
     * @param timestamp   timestamp
     */
    public void addTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                         double latitude, double longitude, LocalDateTime timestamp) {
        if (vehicleId <= 0 || licensePlate == null || cameraId == null || timestamp == null) {
            LOGGER.warning("Invalid parameters for addTrafficCameraSighting");
            return;
        }

        try {
            sightingDAO.insertTrafficCameraSighting(vehicleId, licensePlate, cameraId, latitude, longitude, timestamp);
            LOGGER.info("Added traffic camera sighting for vehicle " + vehicleId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add traffic camera sighting", e);
        }
    }

    /**
     * Adds an ANPR sighting.
     *
     * @param vehicleId     the vehicle ID
     * @param licensePlate  the license plate
     * @param anprDeviceId  the ANPR device ID
     * @param latitude      latitude
     * @param longitude     longitude
     * @param timestamp     timestamp
     * @param confidence    confidence score
     */
    public void addANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                double latitude, double longitude, LocalDateTime timestamp, double confidence) {
        if (vehicleId <= 0 || licensePlate == null || anprDeviceId == null || timestamp == null) {
            LOGGER.warning("Invalid parameters for addANPRSighting");
            return;
        }

        try {
            sightingDAO.insertANPRSighting(vehicleId, licensePlate, anprDeviceId, latitude, longitude, timestamp, confidence);
            LOGGER.info("Added ANPR sighting for vehicle " + vehicleId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add ANPR sighting", e);
        }
    }

    // ============================================
    // INNER CLASS - ReconstructionReport
    // ============================================

    /**
     * Container for reconstruction report data.
     */
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

        /**
         * Gets the suspicious level as a string.
         *
         * @return "HIGH", "MEDIUM", "LOW", or "MINIMAL"
         */
        public String getSuspiciousLevel() {
            if (suspiciousScore >= 0.7) return "HIGH";
            if (suspiciousScore >= 0.4) return "MEDIUM";
            if (suspiciousScore >= 0.2) return "LOW";
            return "MINIMAL";
        }

        /**
         * Gets the CSS color for the suspicious level.
         *
         * @return hex color code
         */
        public String getSuspiciousColor() {
            switch (getSuspiciousLevel()) {
                case "HIGH": return "#F44336";
                case "MEDIUM": return "#FF9800";
                case "LOW": return "#FFC107";
                default: return "#4CAF50";
            }
        }

        /**
         * Checks if the report contains suspicious activity.
         *
         * @return true if suspicious, false otherwise
         */
        public boolean isSuspicious() {
            return suspiciousScore >= MEDIUM_SUSPICIOUS_SCORE;
        }
    }
}