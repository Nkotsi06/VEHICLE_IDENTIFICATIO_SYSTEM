package utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import models.VehicleSighting;

/**
 * Utility class for ordering and analyzing timestamped vehicle sightings.
 * Provides methods for sorting, filtering, grouping, and finding gaps in data.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class TimestampOrderingUtil {

    private static final Logger LOGGER = Logger.getLogger(TimestampOrderingUtil.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Default thresholds
    private static final long DEFAULT_MAX_GAP_SECONDS = 7200; // 2 hours

    private TimestampOrderingUtil() {
        // Private constructor - utility class
    }

    /**
     * Orders sightings by timestamp (ascending).
     *
     * @param sightings list of sightings to order
     * @return ordered list (ascending)
     */
    public static List<VehicleSighting> orderByTimestamp(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        return sightings.stream()
                .filter(s -> s != null && s.getTimestamp() != null)
                .sorted(Comparator.comparing(VehicleSighting::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Orders sightings by timestamp (descending).
     *
     * @param sightings list of sightings to order
     * @return ordered list (descending)
     */
    public static List<VehicleSighting> orderByTimestampDescending(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        return sightings.stream()
                .filter(s -> s != null && s.getTimestamp() != null)
                .sorted(Comparator.comparing(VehicleSighting::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Filters sightings within a time range.
     *
     * @param sightings list of sightings
     * @param startTime start of range (inclusive, null = unbounded)
     * @param endTime   end of range (inclusive, null = unbounded)
     * @return filtered list ordered by timestamp
     */
    public static List<VehicleSighting> filterByTimeRange(List<VehicleSighting> sightings,
                                                          LocalDateTime startTime,
                                                          LocalDateTime endTime) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        return sightings.stream()
                .filter(s -> s != null && s.getTimestamp() != null)
                .filter(s -> (startTime == null || !s.getTimestamp().isBefore(startTime)) &&
                        (endTime == null || !s.getTimestamp().isAfter(endTime)))
                .sorted(Comparator.comparing(VehicleSighting::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * Groups sightings into time windows, selecting the highest confidence sighting per window.
     *
     * @param sightings      list of sightings
     * @param windowMinutes  window size in minutes
     * @return grouped list with one representative per window
     */
    public static List<VehicleSighting> groupByTimeWindow(List<VehicleSighting> sightings, int windowMinutes) {
        if (sightings == null || sightings.isEmpty()) {
            return new ArrayList<>();
        }

        List<VehicleSighting> grouped = new ArrayList<>();
        List<VehicleSighting> sorted = orderByTimestamp(sightings);

        if (sorted.isEmpty()) return grouped;

        LocalDateTime currentWindowStart = sorted.get(0).getTimestamp();
        VehicleSighting representative = sorted.get(0);

        for (VehicleSighting sighting : sorted) {
            if (sighting == null || sighting.getTimestamp() == null) continue;

            long minutesDiff = Duration.between(currentWindowStart, sighting.getTimestamp()).toMinutes();

            if (minutesDiff <= windowMinutes) {
                // Same window - keep the one with higher confidence
                if (sighting.getConfidenceScore() > representative.getConfidenceScore()) {
                    representative = sighting;
                }
            } else {
                // New window - add previous representative and start new window
                grouped.add(representative);
                currentWindowStart = sighting.getTimestamp();
                representative = sighting;
            }
        }

        // Add the last representative
        grouped.add(representative);
        return grouped;
    }

    /**
     * Finds time gaps between consecutive sightings.
     *
     * @param sightings     list of sightings
     * @param maxGapSeconds maximum allowed gap (seconds)
     * @return list of time gaps exceeding the threshold
     */
    public static List<TimeGap> findTimeGaps(List<VehicleSighting> sightings, long maxGapSeconds) {
        List<TimeGap> gaps = new ArrayList<>();
        List<VehicleSighting> sorted = orderByTimestamp(sightings);

        for (int i = 1; i < sorted.size(); i++) {
            VehicleSighting prev = sorted.get(i - 1);
            VehicleSighting curr = sorted.get(i);

            if (prev == null || curr == null || prev.getTimestamp() == null || curr.getTimestamp() == null) {
                continue;
            }

            long gapSeconds = Duration.between(prev.getTimestamp(), curr.getTimestamp()).getSeconds();

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

    /**
     * Finds time gaps with default threshold.
     *
     * @param sightings list of sightings
     * @return list of time gaps
     */
    public static List<TimeGap> findTimeGaps(List<VehicleSighting> sightings) {
        return findTimeGaps(sightings, DEFAULT_MAX_GAP_SECONDS);
    }

    /**
     * Calculates time density (sightings per second) within a window.
     *
     * @param sightings   list of sightings
     * @param windowStart start of window
     * @param windowEnd   end of window
     * @return density (sightings per second)
     */
    public static double calculateTimeDensity(List<VehicleSighting> sightings,
                                              LocalDateTime windowStart,
                                              LocalDateTime windowEnd) {
        List<VehicleSighting> inWindow = filterByTimeRange(sightings, windowStart, windowEnd);
        long windowSeconds = Duration.between(windowStart, windowEnd).getSeconds();

        if (windowSeconds <= 0) return 0.0;
        return (double) inWindow.size() / windowSeconds;
    }

    /**
     * Finds the hour with the most sightings.
     *
     * @param sightings list of sightings
     * @return the start time of the busiest hour, or null if none
     */
    public static LocalDateTime findMostActiveTime(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return null;
        }

        Map<LocalDateTime, Integer> hourCounts = new HashMap<>();

        for (VehicleSighting sighting : sightings) {
            if (sighting == null || sighting.getTimestamp() == null) continue;

            LocalDateTime hourStart = sighting.getTimestamp().withMinute(0).withSecond(0).withNano(0);
            hourCounts.put(hourStart, hourCounts.getOrDefault(hourStart, 0) + 1);
        }

        return hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Gets the time range of sightings.
     *
     * @param sightings list of sightings
     * @return array of [earliest, latest] times, or [null, null] if empty
     */
    public static LocalDateTime[] getTimeRange(List<VehicleSighting> sightings) {
        if (sightings == null || sightings.isEmpty()) {
            return new LocalDateTime[]{null, null};
        }

        List<VehicleSighting> sorted = orderByTimestamp(sightings);
        if (sorted.isEmpty()) {
            return new LocalDateTime[]{null, null};
        }

        return new LocalDateTime[]{sorted.get(0).getTimestamp(), sorted.get(sorted.size() - 1).getTimestamp()};
    }

    /**
     * Formats a timestamp as HH:mm:ss.
     *
     * @param timestamp the timestamp to format
     * @return formatted time string
     */
    public static String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        return timestamp.format(TIME_FORMATTER);
    }

    /**
     * Formats a time gap in seconds to a human-readable string.
     *
     * @param seconds the gap in seconds
     * @return formatted string (e.g., "2 hours, 30 minutes")
     */
    public static String formatTimeGap(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d hour%s, %d minute%s",
                    hours, hours != 1 ? "s" : "",
                    minutes, minutes != 1 ? "s" : "");
        } else if (minutes > 0) {
            return String.format("%d minute%s, %d second%s",
                    minutes, minutes != 1 ? "s" : "",
                    secs, secs != 1 ? "s" : "");
        } else {
            return String.format("%d second%s", secs, secs != 1 ? "s" : "");
        }
    }

    // ============================================
    // INNER CLASS - TimeGap
    // ============================================

    /**
     * Represents a time gap between two sightings.
     */
    public static class TimeGap {
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public long gapSeconds;
        public RouteCalculator.GeoPoint startLocation;
        public RouteCalculator.GeoPoint endLocation;

        /**
         * Gets the formatted gap duration.
         *
         * @return human-readable gap duration
         */
        public String getFormattedGap() {
            return formatTimeGap(gapSeconds);
        }

        /**
         * Gets the estimated distance that could have been traveled during the gap.
         *
         * @return estimated distance in kilometers
         */
        public double getEstimatedDistance() {
            if (startLocation == null || endLocation == null) return 0.0;
            RouteCalculator calculator = new RouteCalculator();
            return calculator.calculateDistance(
                    startLocation.latitude, startLocation.longitude,
                    endLocation.latitude, endLocation.longitude
            );
        }

        /**
         * Gets the gap hours.
         *
         * @return gap in hours
         */
        public double getGapHours() {
            return gapSeconds / 3600.0;
        }
    }
}