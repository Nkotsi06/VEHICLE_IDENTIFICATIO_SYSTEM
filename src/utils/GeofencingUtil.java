package utils;

import java.util.ArrayList;
import java.util.List;

import dao.GeofenceZoneDAO;
import models.GeofenceZone;

/**
 * Utility class for geofencing operations.
 * Provides methods for checking if points are within zones,
 * calculating distances, and generating geofence alerts.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class GeofencingUtil {

    private static GeofencingUtil instance;
    private GeofenceZoneDAO zoneDAO;

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Alert type constants
    public static final String ALERT_ENTER = "ENTER";
    public static final String ALERT_EXIT = "EXIT";

    private GeofencingUtil() {
        try {
            this.zoneDAO = new GeofenceZoneDAO();
        } catch (Exception e) {
            System.err.println("Failed to initialize GeofencingUtil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the singleton instance of GeofencingUtil.
     *
     * @return the GeofencingUtil instance
     */
    public static synchronized GeofencingUtil getInstance() {
        if (instance == null) {
            instance = new GeofencingUtil();
        }
        return instance;
    }

    /**
     * Checks if a point is inside a geofence zone.
     *
     * @param latitude  the point's latitude
     * @param longitude the point's longitude
     * @param zone      the geofence zone
     * @return true if point is inside the zone, false otherwise
     */
    public boolean isPointInZone(double latitude, double longitude, GeofenceZone zone) {
        if (zone == null) return false;

        double distance = calculateDistance(latitude, longitude, zone.getCenterLat(), zone.getCenterLng());
        double radiusKm = zone.getRadiusMeters() / 1000.0;
        return distance <= radiusKm;
    }

    /**
     * Finds all geofence zones that contain a given point.
     *
     * @param latitude  the point's latitude
     * @param longitude the point's longitude
     * @return list of zones containing the point
     */
    public List<GeofenceZone> findZonesContainingPoint(double latitude, double longitude) {
        List<GeofenceZone> containingZones = new ArrayList<>();

        try {
            List<GeofenceZone> allZones = zoneDAO.findActiveZones();
            if (allZones != null) {
                for (GeofenceZone zone : allZones) {
                    if (isPointInZone(latitude, longitude, zone)) {
                        containingZones.add(zone);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding zones containing point: " + e.getMessage());
            e.printStackTrace();
        }

        return containingZones;
    }

    /**
     * Finds geofence zones near a given point within a radius.
     *
     * @param latitude   the center latitude
     * @param longitude  the center longitude
     * @param radiusKm   the search radius in kilometers
     * @return list of nearby zones
     */
    public List<GeofenceZone> findZonesNearPoint(double latitude, double longitude, double radiusKm) {
        List<GeofenceZone> nearbyZones = new ArrayList<>();

        try {
            List<GeofenceZone> allZones = zoneDAO.findActiveZones();
            if (allZones != null) {
                for (GeofenceZone zone : allZones) {
                    double distance = calculateDistance(latitude, longitude, zone.getCenterLat(), zone.getCenterLng());
                    double zoneRadiusKm = zone.getRadiusMeters() / 1000.0;
                    if (distance <= radiusKm + zoneRadiusKm) {
                        nearbyZones.add(zone);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding zones near point: " + e.getMessage());
            e.printStackTrace();
        }

        return nearbyZones;
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
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Gets an alert message for zone entry or exit.
     *
     * @param zone              the geofence zone
     * @param vehicleRegistration the vehicle's registration number
     * @param alertType         "ENTER" or "EXIT"
     * @return formatted alert message
     */
    public String getAlertMessage(GeofenceZone zone, String vehicleRegistration, String alertType) {
        if (zone == null || vehicleRegistration == null || alertType == null) {
            return "Geofence alert triggered";
        }

        String action = "ENTER".equalsIgnoreCase(alertType) ? "entered" : "exited";

        return String.format("ALERT: Vehicle %s has %s %s (%s zone)",
                vehicleRegistration, action, zone.getZoneName(), zone.getZoneType());
    }

    /**
     * Gets the priority level for a geofence alert.
     *
     * @param zone      the geofence zone
     * @param alertType "ENTER" or "EXIT"
     * @return priority level ("HIGH", "MEDIUM", or "LOW")
     */
    public String getPriorityForZone(GeofenceZone zone, String alertType) {
        if (zone == null) return "LOW";

        if ("ENTER".equalsIgnoreCase(alertType)) {
            switch (zone.getZoneType().toUpperCase()) {
                case "HIGH_CRIME":
                case "SCHOOL_ZONE":
                case "RESTRICTED":
                case "GOVERNMENT":
                    return "HIGH";
                case "MONITORED":
                case "COMMERCIAL":
                    return "MEDIUM";
                default:
                    return "LOW";
            }
        } else {
            return "LOW";
        }
    }

    /**
     * Sends a geofence alert to the system.
     *
     * @param vehicleId the vehicle ID
     * @param zoneId    the zone ID
     * @param alertType "ENTER" or "EXIT"
     */
    public void sendGeofenceAlert(int vehicleId, int zoneId, String alertType) {
        if (vehicleId <= 0 || zoneId <= 0) {
            System.err.println("Invalid vehicle or zone ID for geofence alert");
            return;
        }

        try {
            // This would call a stored procedure in a real implementation
            System.out.println(String.format("Geofence alert: Vehicle %d %s zone %d at %s",
                    vehicleId, alertType, zoneId, DateUtil.getCurrentDateTime()));
        } catch (Exception e) {
            System.err.println("Failed to send geofence alert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates if coordinates are within valid ranges.
     *
     * @param latitude  the latitude to validate
     * @param longitude the longitude to validate
     * @return true if coordinates are valid, false otherwise
     */
    public boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Converts meters to kilometers.
     *
     * @param meters distance in meters
     * @return distance in kilometers
     */
    public double metersToKm(double meters) {
        return meters / 1000.0;
    }

    /**
     * Converts kilometers to meters.
     *
     * @param km distance in kilometers
     * @return distance in meters
     */
    public double kmToMeters(double km) {
        return km * 1000.0;
    }
}