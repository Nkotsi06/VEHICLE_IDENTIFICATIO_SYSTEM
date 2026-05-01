package utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {

    private static final NumberFormat currencyFormatter;

    static {
        // Using Lesotho Maloti (M) - similar to South African Rand format
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setMaximumFractionDigits(2);
    }

    public static String format(double amount) {
        String formatted = currencyFormatter.format(amount);
        // Replace R with M for Maloti
        formatted = formatted.replace("R", "M");
        return formatted;
    }

    public static String formatWithoutSymbol(double amount) {
        return String.format("%.2f", amount);
    }

    public static double parse(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) return 0;
        try {
            String cleaned = amountStr.replaceAll("[^0-9.-]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}