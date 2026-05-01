package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }

    public static String formatDateForDatabase(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DISPLAY_DATE_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr, DISPLAY_DATE_TIME_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    public static boolean isExpired(LocalDate expiryDate) {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }

    public static boolean isExpiringSoon(LocalDate expiryDate, int daysThreshold) {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return expiryDate.isAfter(now) && expiryDate.minusDays(daysThreshold).isBefore(now);
    }

    public static int getDaysUntilExpiry(LocalDate expiryDate) {
        if (expiryDate == null) return Integer.MAX_VALUE;
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.plusDays(days);
    }

    public static LocalDate subtractDays(LocalDate date, int days) {
        if (date == null) return null;
        return date.minusDays(days);
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        if (date == null) return null;
        return date.plusMonths(months);
    }

    public static LocalDate addYears(LocalDate date, int years) {
        if (date == null) return null;
        return date.plusYears(years);
    }

    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours ago";
        } else if (seconds < 604800) {
            return (seconds / 86400) + " days ago";
        } else {
            return formatDateTime(dateTime);
        }
    }
}