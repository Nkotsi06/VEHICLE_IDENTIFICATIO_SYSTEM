package utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.VehicleSighting;

/**
 * Utility class for route calculation and geospatial operations.
 * Calculates distances, speeds, bearings, and route segments for vehicle tracking.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class RouteCalculator {

    private static final Logger LOGGER = Logger.getLogger(RouteCalculator.class.getName());

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Speed thresholds
    private static final double SPEEDING_THRESHOLD_KMPH = 120.0;
    private static final double SUSPICIOUS_SPEED_THRESHOLD = 140.0;

    // Time gap thresholds (in seconds)
    private static final long LARGE_TIME_GAP_THRESHOLD = 14400; // 4 hours
    private static final long SUSPICIOUS_TIME_GAP_THRESHOLD = 28800; // 8 hours

    private RouteCalculator() {
        // Private constructor - utility class
    }

    /**
     * Calculates the great-circle distance between two points using Haversine formula.
     *
     * @param lat1 latitude of first point
     * @param lng1 longitude of first point
     * @param lat2 latitude of second point
     * @param lng2 longitude of second point
     * @return distance in kilometers
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Validate coordinates
        if (!isValidCoordinate(lat1, lng1) || !isValidCoordinate(lat2, lng2)) {
            LOGGER.warning("Invalid coordinates for distance calculation");
            return 0.0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the total distance traveled from a list of sightings.
     *
     * @param sightings list of vehicle sightings in chronological order
     * @return total distance in kilometers
     */
    public double calculateTotalDistance(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 1; i < sightings.size(); i++) {
            VehicleSighting prev = sightings.get(i - 1);
            VehicleSighting curr = sightings.get(i);

            if (prev == null || curr == null) continue;

            double distance = calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );
            totalDistance += distance;
        }

        return totalDistance;
    }

    /**
     * Calculates the average speed over a route.
     *
     * @param sightings list of vehicle sightings in chronological order
     * @return average speed in km/h
     */
    public double calculateAverageSpeed(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.size() < 2) {
            return 0.0;
        }

        double totalDistance = calculateTotalDistance(sightings);

        VehicleSighting first = sightings.get(0);
        VehicleSighting last = sightings.get(sightings.size() - 1);

        if (first == null || last == null || first.getTimestamp() == null || last.getTimestamp() == null) {
            return 0.0;
        }

        long startTime = first.getTimestamp().toEpochSecond(ZoneOffset.UTC);
        long endTime = last.getTimestamp().toEpochSecond(ZoneOffset.UTC);
        double totalHours = (endTime - startTime) / 3600.0;

        if (totalHours <= 0) return 0.0;
        return totalDistance / totalHours;
    }

    /**
     * Calculates the speed between two sightings.
     *
     * @param prev previous sighting
     * @param curr current sighting
     * @return speed in km/h
     */
    public double calculateSegmentSpeed(VehicleSighting prev, VehicleSighting curr) {
        if (prev == null || curr == null) return 0.0;

        double distance = calculateDistance(
                prev.getLatitude(), prev.getLongitude(),
                curr.getLatitude(), curr.getLongitude()
        );

        long timeDiff = curr.getTimestamp().toEpochSecond(ZoneOffset.UTC) -
                prev.getTimestamp().toEpochSecond(ZoneOffset.UTC);
        double hours = timeDiff / 3600.0;

        if (hours <= 0) return 0.0;
        return distance / hours;
    }

    /**
     * Calculates route segments from a list of sightings.
     *
     * @param sightings list of vehicle sightings in chronological order
     * @return list of route segments
     */
    public List<RouteSegment> calculateRouteSegments(List<VehicleSighting> sightings) {
        List<RouteSegment> segments = new ArrayList<>();

        if (sightings == null || sightings.size() < 2) {
            return segments;
        }

        for (int i = 1; i < sightings.size(); i++) {
            VehicleSighting prev = sightings.get(i - 1);
            VehicleSighting curr = sightings.get(i);

            if (prev == null || curr == null) continue;

            RouteSegment segment = new RouteSegment();
            segment.startPoint = new GeoPoint(prev.getLatitude(), prev.getLongitude());
            segment.endPoint = new GeoPoint(curr.getLatitude(), curr.getLongitude());
            segment.distance = calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );

            long timeDiff = Duration.between(prev.getTimestamp(), curr.getTimestamp()).getSeconds();
            segment.durationHours = timeDiff / 3600.0;
            segment.speedKmph = segment.distance / segment.durationHours;
            segment.startTime = prev.getTimestamp();
            segment.endTime = curr.getTimestamp();
            segment.sourceType = curr.getSourceType();

            segments.add(segment);
        }

        return segments;
    }

    /**
     * Estimates arrival time to destination.
     *
     * @param currentLat current latitude
     * @param currentLng current longitude
     * @param destLat destination latitude
     * @param destLng destination longitude
     * @param speedKmph current/average speed in km/h
     * @return estimated hours to arrival, or -1 if invalid
     */
    public double estimateArrivalTime(double currentLat, double currentLng,
                                      double destLat, double destLng, double speedKmph) {
        double distance = calculateDistance(currentLat, currentLng, destLat, destLng);
        if (speedKmph <= 0) return -1.0;
        return distance / speedKmph;
    }

    /**
     * Interpolates a position between two points.
     *
     * @param start start point
     * @param end end point
     * @param fraction fraction from start to end (0.0 to 1.0)
     * @return interpolated point
     */
    public GeoPoint interpolatePosition(GeoPoint start, GeoPoint end, double fraction) {
        if (start == null || end == null) return null;

        double clampedFraction = Math.max(0.0, Math.min(1.0, fraction));
        double lat = start.latitude + (end.latitude - start.latitude) * clampedFraction;
        double lng = start.longitude + (end.longitude - start.longitude) * clampedFraction;
        return new GeoPoint(lat, lng);
    }

    /**
     * Calculates the bearing (direction) from one point to another.
     *
     * @param lat1 start latitude
     * @param lng1 start longitude
     * @param lat2 end latitude
     * @param lng2 end longitude
     * @return bearing in degrees (0-360)
     */
    public double calculateBearing(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double y = Math.sin(dLng) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLng);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    /**
     * Checks if a speed is suspicious (above threshold).
     *
     * @param speedKmph speed in km/h
     * @return true if suspicious, false otherwise
     */
    public boolean isSuspiciousSpeed(double speedKmph) {
        return speedKmph > SPEEDING_THRESHOLD_KMPH;
    }

    /**
     * Checks if a time gap is large (above threshold).
     *
     * @param secondsGap time gap in seconds
     * @return true if large, false otherwise
     */
    public boolean isLargeTimeGap(long secondsGap) {
        return secondsGap > LARGE_TIME_GAP_THRESHOLD;
    }

    /**
     * Validates coordinates.
     *
     * @param latitude latitude value
     * @param longitude longitude value
     * @return true if valid, false otherwise
     */
    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Converts meters to kilometers.
     *
     * @param meters distance in meters
     * @return distance in kilometers
     */
    public static double metersToKm(double meters) {
        return meters / 1000.0;
    }

    /**
     * Converts kilometers to meters.
     *
     * @param km distance in kilometers
     * @return distance in meters
     */
    public static double kmToMeters(double km) {
        return km * 1000.0;
    }

    // ============================================
    // INNER CLASSES
    // ============================================

    /**
     * Represents a segment of a route between two sightings.
     */
    public static class RouteSegment {
        public GeoPoint startPoint;
        public GeoPoint endPoint;
        public double distance;
        public double durationHours;
        public double speedKmph;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public String sourceType;

        /**
         * Checks if this segment involved speeding.
         *
         * @return true if speed exceeds threshold
         */
        public boolean isSpeeding() {
            return speedKmph > SPEEDING_THRESHOLD_KMPH;
        }

        /**
         * Checks if this segment is highly suspicious.
         *
         * @return true if speed is extremely high
         */
        public boolean isHighlySuspicious() {
            return speedKmph > SUSPICIOUS_SPEED_THRESHOLD;
        }

        /**
         * Gets formatted duration string.
         *
         * @return human-readable duration
         */
        public String getFormattedDuration() {
            if (durationHours < 1) {
                return String.format("%.0f minutes", durationHours * 60);
            }
            return String.format("%.1f hours", durationHours);
        }

        /**
         * Gets formatted distance string.
         *
         * @return human-readable distance
         */
        public String getFormattedDistance() {
            if (distance < 1) {
                return String.format("%.0f meters", distance * 1000);
            }
            return String.format("%.2f km", distance);
        }

        /**
         * Gets formatted speed string.
         *
         * @return human-readable speed
         */
        public String getFormattedSpeed() {
            return String.format("%.1f km/h", speedKmph);
        }
    }

    /**
     * Represents a geographic point with latitude and longitude.
     */
    public static class GeoPoint {
        public double latitude;
        public double longitude;

        public GeoPoint(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return String.format("(%.6f, %.6f)", latitude, longitude);
        }
    }
}