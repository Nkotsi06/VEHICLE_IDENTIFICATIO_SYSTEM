package utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Utility class for currency formatting and parsing.
 * Specifically configured for Lesotho Maloti (M) currency.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class CurrencyUtil {

    private static final NumberFormat currencyFormatter;
    private static final DecimalFormat decimalFormatter;
    private static final String CURRENCY_SYMBOL = "M";

    static {
        // Using South African locale as it's similar to Lesotho format
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setMaximumFractionDigits(2);

        decimalFormatter = new DecimalFormat("#,##0.00");
    }

    private CurrencyUtil() {} // Prevent instantiation

    /**
     * Formats a double amount as currency with M symbol.
     * Example: 1234.56 -> "M1,234.56"
     *
     * @param amount the amount to format
     * @return formatted currency string
     */
    public static String format(double amount) {
        String formatted = currencyFormatter.format(amount);
        // Replace ZAR symbol (R) with M for Maloti
        formatted = formatted.replace("R", CURRENCY_SYMBOL);
        return formatted;
    }

    /**
     * Formats a double amount without currency symbol.
     * Example: 1234.56 -> "1,234.56"
     *
     * @param amount the amount to format
     * @return formatted number string
     */
    public static String formatWithoutSymbol(double amount) {
        return decimalFormatter.format(amount);
    }

    /**
     * Formats a double amount with M symbol but without commas.
     * Example: 1234.56 -> "M1234.56"
     *
     * @param amount the amount to format
     * @return formatted currency string without commas
     */
    public static String formatCompact(double amount) {
        return CURRENCY_SYMBOL + String.format("%.2f", amount);
    }

    /**
     * Parses a currency string to a double value.
     * Handles various formats including M prefix, commas, and spaces.
     *
     * @param amountStr the currency string to parse
     * @return the parsed double value, or 0 if parsing fails
     */
    public static double parse(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Remove currency symbol, spaces, and other non-numeric characters except decimal points and minus signs
            String cleaned = amountStr
                    .replace(CURRENCY_SYMBOL, "")
                    .replace("R", "")
                    .replace(",", "")
                    .replace(" ", "")
                    .trim();

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse currency: " + amountStr);
            return 0.0;
        }
    }

    /**
     * Attempts to parse using NumberFormat for more robust parsing.
     *
     * @param amountStr the currency string to parse
     * @return the parsed double value, or 0 if parsing fails
     */
    public static double parseWithFormat(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }

        try {
            Number number = currencyFormatter.parse(amountStr);
            return number.doubleValue();
        } catch (ParseException e) {
            // Fall back to simple parsing
            return parse(amountStr);
        }
    }

    /**
     * Validates if a string is a valid currency amount.
     *
     * @param amountStr the string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCurrency(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return false;
        }

        try {
            double value = parse(amountStr);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Gets the currency symbol.
     *
     * @return the currency symbol (M)
     */
    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }
}