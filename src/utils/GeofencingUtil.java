package utils;

import java.util.ArrayList;
import java.util.List;

import dao.GeofenceZoneDAO;
import models.GeofenceZone;

public class GeofencingUtil {

    private static GeofencingUtil instance;
    private GeofenceZoneDAO zoneDAO;

    private GeofencingUtil() {
        this.zoneDAO = new GeofenceZoneDAO();
    }

    public static synchronized GeofencingUtil getInstance() {
        if (instance == null) {
            instance = new GeofencingUtil();
        }
        return instance;
    }

    public boolean isPointInZone(double latitude, double longitude, GeofenceZone zone) {
        double distance = calculateDistance(latitude, longitude, zone.getCenterLat(), zone.getCenterLng());
        double radiusKm = zone.getRadiusMeters() / 1000.0;
        return distance <= radiusKm;
    }

    public List<GeofenceZone> findZonesContainingPoint(double latitude, double longitude) {
        List<GeofenceZone> containingZones = new ArrayList<>();

        try {
            List<GeofenceZone> allZones = zoneDAO.findActiveZones();
            for (GeofenceZone zone : allZones) {
                if (isPointInZone(latitude, longitude, zone)) {
                    containingZones.add(zone);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return containingZones;
    }

    public List<GeofenceZone> findZonesNearPoint(double latitude, double longitude, double radiusKm) {
        List<GeofenceZone> nearbyZones = new ArrayList<>();

        try {
            List<GeofenceZone> allZones = zoneDAO.findActiveZones();
            for (GeofenceZone zone : allZones) {
                double distance = calculateDistance(latitude, longitude, zone.getCenterLat(), zone.getCenterLng());
                double zoneRadiusKm = zone.getRadiusMeters() / 1000.0;
                if (distance <= radiusKm + zoneRadiusKm) {
                    nearbyZones.add(zone);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nearbyZones;
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public String getAlertMessage(GeofenceZone zone, String vehicleRegistration, String alertType) {
        if ("ENTER".equals(alertType)) {
            return "ALERT: Vehicle " + vehicleRegistration + " has entered " + zone.getZoneName() +
                    " (" + zone.getZoneType() + " zone)";
        } else {
            return "ALERT: Vehicle " + vehicleRegistration + " has exited " + zone.getZoneName() +
                    " (" + zone.getZoneType() + " zone)";
        }
    }

    public String getPriorityForZone(GeofenceZone zone, String alertType) {
        if ("ENTER".equals(alertType)) {
            switch (zone.getZoneType()) {
                case "HIGH_CRIME": return "HIGH";
                case "SCHOOL_ZONE": return "HIGH";
                case "RESTRICTED": return "HIGH";
                case "MONITORED": return "MEDIUM";
                default: return "LOW";
            }
        } else {
            return "LOW";
        }
    }

    public void sendGeofenceAlert(int vehicleId, int zoneId, String alertType) {
        try {
            // This would call a stored procedure in a real implementation
            System.out.println("Geofence alert sent: Vehicle " + vehicleId +
                    " " + alertType + " zone " + zoneId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}