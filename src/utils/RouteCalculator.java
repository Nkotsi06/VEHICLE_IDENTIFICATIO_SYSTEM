package utils;

import java.util.ArrayList;
import java.util.List;

import models.VehicleSighting;

public class RouteCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public double calculateTotalDistance(List<VehicleSighting> sightings) {
        double totalDistance = 0;

        for (int i = 1; i < sightings.size(); i++) {
            VehicleSighting prev = sightings.get(i - 1);
            VehicleSighting curr = sightings.get(i);

            double distance = calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );
            totalDistance += distance;
        }

        return totalDistance;
    }

    public double calculateAverageSpeed(List<VehicleSighting> sightings) {
        if (sightings.size() < 2) return 0;

        double totalDistance = calculateTotalDistance(sightings);

        long startTime = sightings.get(0).getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC);
        long endTime = sightings.get(sightings.size() - 1).getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC);
        double totalHours = (endTime - startTime) / 3600.0;

        if (totalHours <= 0) return 0;
        return totalDistance / totalHours;
    }

    public double calculateSegmentSpeed(VehicleSighting prev, VehicleSighting curr) {
        double distance = calculateDistance(
                prev.getLatitude(), prev.getLongitude(),
                curr.getLatitude(), curr.getLongitude()
        );

        long timeDiff = curr.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) -
                prev.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC);
        double hours = timeDiff / 3600.0;

        if (hours <= 0) return 0;
        return distance / hours;
    }

    public List<RouteSegment> calculateRouteSegments(List<VehicleSighting> sightings) {
        List<RouteSegment> segments = new ArrayList<>();

        for (int i = 1; i < sightings.size(); i++) {
            VehicleSighting prev = sightings.get(i - 1);
            VehicleSighting curr = sightings.get(i);

            RouteSegment segment = new RouteSegment();
            segment.startPoint = new GeoPoint(prev.getLatitude(), prev.getLongitude());
            segment.endPoint = new GeoPoint(curr.getLatitude(), curr.getLongitude());
            segment.distance = calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );
            segment.durationHours = (curr.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC) -
                    prev.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC)) / 3600.0;
            segment.speedKmph = segment.distance / segment.durationHours;
            segment.startTime = prev.getTimestamp();
            segment.endTime = curr.getTimestamp();
            segment.sourceType = curr.getSourceType();

            segments.add(segment);
        }

        return segments;
    }

    public double estimateArrivalTime(double currentLat, double currentLng, double destLat, double destLng, double speedKmph) {
        double distance = calculateDistance(currentLat, currentLng, destLat, destLng);
        if (speedKmph <= 0) return -1;
        return distance / speedKmph;
    }

    public GeoPoint interpolatePosition(GeoPoint start, GeoPoint end, double fraction) {
        if (fraction < 0) fraction = 0;
        if (fraction > 1) fraction = 1;

        double lat = start.latitude + (end.latitude - start.latitude) * fraction;
        double lng = start.longitude + (end.longitude - start.longitude) * fraction;

        return new GeoPoint(lat, lng);
    }

    public double calculateBearing(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double y = Math.sin(dLng) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLng);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    public boolean isSuspiciousSpeed(double speedKmph) {
        return speedKmph > 120;
    }

    public boolean isLargeTimeGap(long secondsGap) {
        return secondsGap > 14400;
    }

    public static class RouteSegment {
        public GeoPoint startPoint;
        public GeoPoint endPoint;
        public double distance;
        public double durationHours;
        public double speedKmph;
        public java.time.LocalDateTime startTime;
        public java.time.LocalDateTime endTime;
        public String sourceType;

        public boolean isSpeeding() {
            return speedKmph > 120;
        }

        public String getFormattedDuration() {
            if (durationHours < 1) {
                return String.format("%.0f minutes", durationHours * 60);
            }
            return String.format("%.1f hours", durationHours);
        }

        public String getFormattedDistance() {
            if (distance < 1) {
                return String.format("%.0f meters", distance * 1000);
            }
            return String.format("%.2f km", distance);
        }
    }

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