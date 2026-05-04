package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * Utility class for date and time operations.
 * Provides formatting, parsing, and comparison methods.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class DateUtil {

    // Date formats
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter COMPACT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Date patterns for validation
    private static final Pattern DD_MM_YYYY_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");
    private static final Pattern YYYY_MM_DD_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private DateUtil() {} // Prevent instantiation

    // ============================================
    // FORMATTING METHODS
    // ============================================

    /**
     * Formats a LocalDate for display (dd/MM/yyyy).
     *
     * @param date the date to format
     * @return formatted date string, or empty string if null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Formats a LocalDateTime for display (dd/MM/yyyy HH:mm:ss).
     *
     * @param dateTime the date-time to format
     * @return formatted date-time string, or empty string if null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }

    /**
     * Formats a LocalDate for database storage (yyyy-MM-dd).
     *
     * @param date the date to format
     * @return formatted date string, or null if null
     */
    public static String formatDateForDatabase(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a LocalDateTime for database storage (yyyy-MM-dd HH:mm:ss).
     *
     * @param dateTime the date-time to format
     * @return formatted date-time string, or null if null
     */
    public static String formatDateTimeForDatabase(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Formats a LocalDate in compact format (yyyyMMdd).
     *
     * @param date the date to format
     * @return compact date string
     */
    public static String formatCompact(LocalDate date) {
        if (date == null) return "";
        return date.format(COMPACT_DATE_FORMATTER);
    }

    // ============================================
    // PARSING METHODS
    // ============================================

    /**
     * Parses a date string from various formats.
     * Supports dd/MM/yyyy and yyyy-MM-dd.
     *
     * @param dateStr the date string to parse
     * @return parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = dateStr.trim();

        try {
            if (DD_MM_YYYY_PATTERN.matcher(trimmed).matches()) {
                return LocalDate.parse(trimmed, DISPLAY_DATE_FORMATTER);
            } else if (YYYY_MM_DD_PATTERN.matcher(trimmed).matches()) {
                return LocalDate.parse(trimmed, DATE_FORMATTER);
            } else {
                // Try automatic parsing
                return LocalDate.parse(trimmed);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse date: " + dateStr);
            return null;
        }
    }

    /**
     * Parses a date-time string from various formats.
     *
     * @param dateTimeStr the date-time string to parse
     * @return parsed LocalDateTime, or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        String trimmed = dateTimeStr.trim();

        try {
            return LocalDateTime.parse(trimmed, DISPLAY_DATE_TIME_FORMATTER);
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(trimmed, DATE_TIME_FORMATTER);
            } catch (Exception e2) {
                System.err.println("Failed to parse date-time: " + dateTimeStr);
                return null;
            }
        }
    }

    // ============================================
    // CURRENT DATE/TIME METHODS
    // ============================================

    /**
     * Gets the current date.
     *
     * @return current LocalDate
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Gets the current date-time.
     *
     * @return current LocalDateTime
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    // ============================================
    // DATE CALCULATION METHODS
    // ============================================

    /**
     * Calculates days between two dates.
     *
     * @param start the start date
     * @param end   the end date
     * @return number of days between, or 0 if either is null
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Adds days to a date.
     *
     * @param date the starting date
     * @param days number of days to add (can be negative)
     * @return new date, or null if input is null
     */
    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.plusDays(days);
    }

    /**
     * Subtracts days from a date.
     *
     * @param date the starting date
     * @param days number of days to subtract
     * @return new date, or null if input is null
     */
    public static LocalDate subtractDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.minusDays(days);
    }

    /**
     * Adds months to a date.
     *
     * @param date   the starting date
     * @param months number of months to add
     * @return new date, or null if input is null
     */
    public static LocalDate addMonths(LocalDate date, int months) {
        if (date == null) return null;
        return date.plusMonths(months);
    }

    /**
     * Adds years to a date.
     *
     * @param date  the starting date
     * @param years number of years to add
     * @return new date, or null if input is null
     */
    public static LocalDate addYears(LocalDate date, int years) {
        if (date == null) return null;
        return date.plusYears(years);
    }

    // ============================================
    // EXPIRY CHECKING METHODS
    // ============================================

    /**
     * Checks if a date has expired (is before today).
     *
     * @param expiryDate the expiry date to check
     * @return true if expired, false otherwise
     */
    public static boolean isExpired(LocalDate expiryDate) {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if a date is expiring within a threshold.
     *
     * @param expiryDate     the expiry date
     * @param daysThreshold  number of days to consider as "soon"
     * @return true if expiring within threshold, false otherwise
     */
    public static boolean isExpiringSoon(LocalDate expiryDate, int daysThreshold) {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return expiryDate.isAfter(now) && expiryDate.minusDays(daysThreshold).isBefore(now);
    }

    /**
     * Gets the number of days until expiry.
     *
     * @param expiryDate the expiry date
     * @return days until expiry, or Integer.MAX_VALUE if null
     */
    public static int getDaysUntilExpiry(LocalDate expiryDate) {
        if (expiryDate == null) return Integer.MAX_VALUE;
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Gets the expiry status as a string.
     *
     * @param expiryDate the expiry date
     * @return "EXPIRED", "CRITICAL", "WARNING", "DUE_SOON", or "VALID"
     */
    public static String getExpiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return "UNKNOWN";

        int daysRemaining = getDaysUntilExpiry(expiryDate);

        if (daysRemaining < 0) return "EXPIRED";
        if (daysRemaining <= 7) return "CRITICAL";
        if (daysRemaining <= 15) return "WARNING";
        if (daysRemaining <= 30) return "DUE_SOON";
        return "VALID";
    }

    // ============================================
    // RELATIVE TIME METHODS
    // ============================================

    /**
     * Gets a human-readable relative time string.
     * Example: "2 hours ago", "yesterday", "3 days ago"
     *
     * @param dateTime the date-time to compare
     * @return relative time string
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "") + " ago";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else if (days == 1) {
            return "yesterday";
        } else if (days < 7) {
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " week" + (weeks != 1 ? "s" : "") + " ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " month" + (months != 1 ? "s" : "") + " ago";
        } else {
            long years = days / 365;
            return years + " year" + (years != 1 ? "s" : "") + " ago";
        }
    }

    /**
     * Validates if a string is a valid date.
     *
     * @param dateStr the date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        return parseDate(dateStr) != null;
    }
}