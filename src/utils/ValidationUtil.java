package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Utility class for validating user input and data.
 * Provides methods for validating emails, phone numbers, passwords, and more.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ValidationUtil {

    private static final Logger LOGGER = Logger.getLogger(ValidationUtil.class.getName());

    // Email pattern (more comprehensive)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone pattern (supports international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    // Lesotho vehicle registration pattern
    private static final Pattern REGISTRATION_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]{3,10}$"
    );

    // Lesotho National ID pattern
    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile(
            "^[0-9]{11,13}$"
    );

    // Driver's license pattern
    private static final Pattern LICENSE_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]{6,15}$"
    );

    // Password pattern (at least 6 chars, with uppercase, lowercase, and digit)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$"
    );

    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if a string is not null and not empty.
     *
     * @param value the string to check
     * @return true if not null and not empty after trimming
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates an email address format.
     *
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates a phone number format.
     *
     * @param phone the phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates a vehicle registration number.
     *
     * @param regNumber the registration number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRegistrationNumber(String regNumber) {
        if (!isNotEmpty(regNumber)) return false;
        return REGISTRATION_PATTERN.matcher(regNumber.toUpperCase()).matches();
    }

    /**
     * Validates a national ID number.
     *
     * @param nationalId the national ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidNationalId(String nationalId) {
        if (!isNotEmpty(nationalId)) return false;
        return NATIONAL_ID_PATTERN.matcher(nationalId).matches();
    }

    /**
     * Validates a driver's license number.
     *
     * @param licenseNumber the license number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDriversLicense(String licenseNumber) {
        if (!isNotEmpty(licenseNumber)) return false;
        return LICENSE_PATTERN.matcher(licenseNumber.toUpperCase()).matches();
    }

    /**
     * Validates a year (between 1900 and next year).
     *
     * @param year the year to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidYear(int year) {
        int currentYear = LocalDate.now().getYear();
        return year >= 1900 && year <= currentYear + 1;
    }

    /**
     * Validates a date string in ISO format (yyyy-MM-dd).
     *
     * @param dateStr the date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (!isNotEmpty(dateStr)) return false;
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks if a date is in the future.
     *
     * @param date the date to check
     * @return true if future, false otherwise
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Checks if a date is in the past.
     *
     * @param date the date to check
     * @return true if past, false otherwise
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Validates an amount (positive and under 1 million).
     *
     * @param amount the amount to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAmount(double amount) {
        return amount > 0 && amount <= 1_000_000.0;
    }

    /**
     * Validates an integer within a range.
     *
     * @param value the string value to parse and validate
     * @param min   minimum allowed value
     * @param max   maximum allowed value
     * @return true if valid, false otherwise
     */
    public static boolean isValidInteger(String value, int min, int max) {
        if (!isNotEmpty(value)) return false;
        try {
            int num = Integer.parseInt(value.trim());
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Alias for isValidInteger.
     */
    public static boolean isInteger(String value, int min, int max) {
        return isValidInteger(value, min, max);
    }

    /**
     * Validates a double within a range.
     *
     * @param value the string value to parse and validate
     * @param min   minimum allowed value
     * @param max   maximum allowed value
     * @return true if valid, false otherwise
     */
    public static boolean isValidDouble(String value, double min, double max) {
        if (!isNotEmpty(value)) return false;
        try {
            double num = Double.parseDouble(value.trim());
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a password strength.
     * Requirements: at least 6 characters, contains uppercase, lowercase, and digit.
     *
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (!isNotEmpty(password)) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Checks if two passwords match.
     *
     * @param password        the password
     * @param confirmPassword the confirmation password
     * @return true if they match, false otherwise
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    /**
     * Validates a rating (1-5).
     *
     * @param rating the rating to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /**
     * Validates a cost (0 to 100,000).
     *
     * @param cost the cost to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCost(double cost) {
        return cost >= 0 && cost <= 100_000.0;
    }

    /**
     * Validates an odometer reading (0 to 1,000,000).
     *
     * @param odometer the odometer reading to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidOdometer(int odometer) {
        return odometer >= 0 && odometer <= 1_000_000;
    }

    /**
     * Gets validation error message for a password.
     *
     * @return error message explaining password requirements
     */
    public static String getPasswordRequirementsMessage() {
        return "Password must be at least 6 characters long and contain at least one uppercase letter, one lowercase letter, and one number.";
    }
}