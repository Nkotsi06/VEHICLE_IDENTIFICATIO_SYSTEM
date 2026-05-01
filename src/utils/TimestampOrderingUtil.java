package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import models.VehicleSighting;

public class TimestampOrderingUtil {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public List<VehicleSighting> orderByTimestamp(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        return sightings.stream()
                .sorted(Comparator.comparing(VehicleSighting::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<VehicleSighting> orderByTimestampDescending(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        return sightings.stream()
                .sorted(Comparator.comparing(VehicleSighting::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<VehicleSighting> filterByTimeRange(List<VehicleSighting> sightings, LocalDateTime startTime, LocalDateTime endTime) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        return sightings.stream()
                .filter(s -> (startTime == null || s.getTimestamp().isAfter(startTime) || s.getTimestamp().equals(startTime)) &&
                        (endTime == null || s.getTimestamp().isBefore(endTime) || s.getTimestamp().equals(endTime)))
                .sorted(Comparator.comparing(VehicleSighting::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<VehicleSighting> groupByTimeWindow(List<VehicleSighting> sightings, int windowMinutes) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        List<VehicleSighting> grouped = new ArrayList<>();
        List<VehicleSighting> sorted = orderByTimestamp(sightings);

        LocalDateTime currentWindowStart = sorted.get(0).getTimestamp();
        VehicleSighting representative = sorted.get(0);

        for (VehicleSighting sighting : sorted) {
            long minutesDiff = java.time.Duration.between(currentWindowStart, sighting.getTimestamp()).toMinutes();

            if (minutesDiff <= windowMinutes) {
                if (sighting.getConfidenceScore() > representative.getConfidenceScore()) {
                    representative = sighting;
                }
            } else {
                grouped.add(representative);
                currentWindowStart = sighting.getTimestamp();
                representative = sighting;
            }
        }

        grouped.add(representative);
        return grouped;
    }

    public List<TimeGap> findTimeGaps(List<VehicleSighting> sightings, long maxGapSeconds) {
        List<TimeGap> gaps = new ArrayList<>();
        List<VehicleSighting> sorted = orderByTimestamp(sightings);

        for (int i = 1; i < sorted.size(); i++) {
            VehicleSighting prev = sorted.get(i - 1);
            VehicleSighting curr = sorted.get(i);

            long gapSeconds = java.time.Duration.between(prev.getTimestamp(), curr.getTimestamp()).getSeconds();

            if (gapSeconds > maxGapSeconds) {
                TimeGap gap = new TimeGap();
                gap.startTime = prev.getTimestamp();
                gap.endTime = curr.getTimestamp();
                gap.gapSeconds = gapSeconds;
                gap.startLocation = new RouteCalculator.GeoPoint(prev.getLatitude(), prev.getLongitude());
                gap.endLocation = new RouteCalculator.GeoPoint(curr.getLatitude(), curr.getLongitude());
                gaps.add(gap);
            }
        }

        return gaps;
    }

    public double calculateTimeDensity(List<VehicleSighting> sightings, LocalDateTime windowStart, LocalDateTime windowEnd) {
        List<VehicleSighting> inWindow = filterByTimeRange(sightings, windowStart, windowEnd);
        long windowSeconds = java.time.Duration.between(windowStart, windowEnd).getSeconds();

        if (windowSeconds <= 0) return 0;
        return (double) inWindow.size() / windowSeconds;
    }

    public LocalDateTime findMostActiveTime(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return null;
        }

        java.util.Map<LocalDateTime, Integer> hourCounts = new java.util.HashMap<>();

        for (VehicleSighting sighting : sightings) {
            LocalDateTime hourStart = sighting.getTimestamp().withMinute(0).withSecond(0).withNano(0);
            hourCounts.put(hourStart, hourCounts.getOrDefault(hourStart, 0) + 1);
        }

        return hourCounts.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }

    public String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        return timestamp.format(TIME_FORMATTER);
    }

    public String formatTimeGap(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, secs);
        } else {
            return String.format("%d seconds", secs);
        }
    }

    public static class TimeGap {
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public long gapSeconds;
        public RouteCalculator.GeoPoint startLocation;
        public RouteCalculator.GeoPoint endLocation;

        public String getFormattedGap() {
            long hours = gapSeconds / 3600;
            long minutes = (gapSeconds % 3600) / 60;
            long seconds = gapSeconds % 60;

            if (hours > 0) {
                return String.format("%d hours, %d minutes", hours, minutes);
            } else if (minutes > 0) {
                return String.format("%d minutes, %d seconds", minutes, seconds);
            } else {
                return String.format("%d seconds", seconds);
            }
        }

        public double getEstimatedDistance() {
            if (startLocation == null || endLocation == null) return 0;
            RouteCalculator calculator = new RouteCalculator();
            return calculator.calculateDistance(
                    startLocation.latitude, startLocation.longitude,
                    endLocation.latitude, endLocation.longitude
            );
        }
    }
}