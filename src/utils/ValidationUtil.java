package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    private static final Pattern REGISTRATION_PATTERN = Pattern.compile(
            "^[A-Z0-9]{3,10}$"
    );

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile(
            "^[0-9]{11,13}$"
    );

    private static final Pattern LICENSE_PATTERN = Pattern.compile(
            "^[A-Z0-9]{6,15}$"
    );

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidRegistrationNumber(String regNumber) {
        if (!isNotEmpty(regNumber)) return false;
        return REGISTRATION_PATTERN.matcher(regNumber.toUpperCase()).matches();
    }

    public static boolean isValidNationalId(String nationalId) {
        if (!isNotEmpty(nationalId)) return false;
        return NATIONAL_ID_PATTERN.matcher(nationalId).matches();
    }

    public static boolean isValidDriversLicense(String licenseNumber) {
        if (!isNotEmpty(licenseNumber)) return false;
        return LICENSE_PATTERN.matcher(licenseNumber.toUpperCase()).matches();
    }

    public static boolean isValidYear(int year) {
        int currentYear = LocalDate.now().getYear();
        return year >= 1900 && year <= currentYear + 1;
    }

    public static boolean isValidDate(String dateStr) {
        if (!isNotEmpty(dateStr)) return false;
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean isValidAmount(double amount) {
        return amount > 0 && amount <= 1000000;
    }

    public static boolean isValidInteger(String value, int min, int max) {
        try {
            int num = Integer.parseInt(value);
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInteger(String value, int min, int max) {
        return isValidInteger(value, min, max);
    }

    public static boolean isValidDouble(String value, double min, double max) {
        try {
            double num = Double.parseDouble(value);
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) return false;
        if (password.length() < 6) return false;
        return password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") && password.matches(".*[0-9].*");
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public static boolean isValidCost(double cost) {
        return cost >= 0 && cost <= 100000;
    }

    public static boolean isValidOdometer(int odometer) {
        return odometer >= 0 && odometer <= 1000000;
    }
}