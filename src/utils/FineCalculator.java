package utils;

/**
 * Utility class for calculating various types of fines.
 * Provides methods for speeding fines, expired document fines,
 * no insurance fines, and other violation-related calculations.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class FineCalculator {

    private static FineCalculator instance;

    // Fine rate constants
    private static final double BASE_SPEEDING_FINE = 300.00;
    private static final double MAX_SPEEDING_FINE = 5000.00;
    private static final double BASE_EXPIRED_DOC_FINE = 500.00;
    private static final double MAX_EXPIRED_DOC_FINE = 2500.00;
    private static final double BASE_NO_INSURANCE_FINE = 500.00;
    private static final double MAX_NO_INSURANCE_FINE = 5000.00;
    private static final double STOLEN_VEHICLE_FINE = 2000.00;

    private FineCalculator() {} // Private constructor for singleton

    /**
     * Gets the singleton instance of FineCalculator.
     *
     * @return the FineCalculator instance
     */
    public static synchronized FineCalculator getInstance() {
        if (instance == null) {
            instance = new FineCalculator();
        }
        return instance;
    }

    /**
     * Calculates the fine for speeding based on speed and limit.
     *
     * @param speedKmph   the actual speed in km/h
     * @param speedLimit  the speed limit in km/h
     * @return fine amount in Maloti (M)
     */
    public double calculateSpeedingFine(double speedKmph, int speedLimit) {
        if (speedKmph <= 0 || speedLimit <= 0) {
            return 0.0;
        }

        double excess = speedKmph - speedLimit;
        if (excess <= 0) return 0.0;

        double fine;
        if (excess <= 10) {
            fine = 300.00;
        } else if (excess <= 20) {
            fine = 500.00;
        } else if (excess <= 30) {
            fine = 1000.00;
        } else if (excess <= 40) {
            fine = 2000.00;
        } else {
            fine = MAX_SPEEDING_FINE;
        }

        return Math.min(fine, MAX_SPEEDING_FINE);
    }

    /**
     * Calculates the fine for expired documents.
     *
     * @param daysOverdue number of days the document is overdue
     * @return fine amount in Maloti (M)
     */
    public double calculateExpiredDocumentFine(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0;

        double fine;
        if (daysOverdue <= 30) {
            fine = BASE_EXPIRED_DOC_FINE;
        } else if (daysOverdue <= 90) {
            fine = 1000.00;
        } else {
            fine = MAX_EXPIRED_DOC_FINE;
        }

        return Math.min(fine, MAX_EXPIRED_DOC_FINE);
    }

    /**
     * Calculates the fine for driving without insurance.
     *
     * @param daysWithoutInsurance number of days without insurance
     * @return fine amount in Maloti (M)
     */
    public double calculateNoInsuranceFine(int daysWithoutInsurance) {
        if (daysWithoutInsurance <= 0) return 0.0;

        double fine;
        if (daysWithoutInsurance <= 7) {
            fine = BASE_NO_INSURANCE_FINE;
        } else if (daysWithoutInsurance <= 30) {
            fine = 1500.00;
        } else {
            fine = MAX_NO_INSURANCE_FINE;
        }

        return Math.min(fine, MAX_NO_INSURANCE_FINE);
    }

    /**
     * Calculates the fine related to a stolen vehicle.
     *
     * @return fine amount in Maloti (M)
     */
    public double calculateStolenVehicleFine() {
        return STOLEN_VEHICLE_FINE;
    }

    /**
     * Calculates a fine for failing to report an accident.
     *
     * @param daysLate number of days late in reporting
     * @return fine amount in Maloti (M)
     */
    public double calculateFailureToReportFine(int daysLate) {
        if (daysLate <= 0) return 0.0;

        double baseFine = 800.00;
        double dailyPenalty = Math.min(daysLate * 100.00, 2000.00);
        return Math.min(baseFine + dailyPenalty, 5000.00);
    }

    /**
     * Calculates a fine for illegal parking.
     *
     * @param zoneType the parking zone type (e.g., "DISABLED", "LOADING", "BUS")
     * @return fine amount in Maloti (M)
     */
    public double calculateParkingFine(String zoneType) {
        if (zoneType == null) return 300.00;

        switch (zoneType.toUpperCase()) {
            case "DISABLED":
                return 1500.00;
            case "LOADING":
                return 800.00;
            case "BUS":
                return 1000.00;
            case "TAXI":
                return 800.00;
            case "NO_PARKING":
                return 500.00;
            default:
                return 300.00;
        }
    }

    /**
     * Calculates a fine for driving without a valid license.
     *
     * @return fine amount in Maloti (M)
     */
    public double calculateNoLicenseFine() {
        return 1500.00;
    }

    /**
     * Calculates a fine for driving with an expired license.
     *
     * @param monthsExpired number of months the license has been expired
     * @return fine amount in Maloti (M)
     */
    public double calculateExpiredLicenseFine(int monthsExpired) {
        if (monthsExpired <= 0) return 0.0;

        double baseFine = 500.00;
        double monthlyPenalty = Math.min(monthsExpired * 100.00, 1000.00);
        return Math.min(baseFine + monthlyPenalty, 2500.00);
    }

    /**
     * Calculates the total fine from multiple fine amounts.
     *
     * @param fines array of fine amounts
     * @return total fine amount
     */
    public double calculateTotalFine(double... fines) {
        double total = 0.0;
        for (double fine : fines) {
            if (fine > 0) {
                total += fine;
            }
        }
        return total;
    }

    /**
     * Applies a discount to a fine amount.
     *
     * @param fineAmount    the original fine amount
     * @param discountPercent discount percentage (e.g., 25 for 25%)
     * @return discounted fine amount
     */
    public double applyDiscount(double fineAmount, int discountPercent) {
        if (fineAmount <= 0 || discountPercent <= 0) return fineAmount;
        if (discountPercent > 50) discountPercent = 50; // Max 50% discount
        return fineAmount * (1 - discountPercent / 100.0);
    }

    /**
     * Calculates late payment penalty.
     *
     * @param fineAmount      original fine amount
     * @param daysLate        number of days late
     * @return late payment penalty amount
     */
    public double calculateLatePenalty(double fineAmount, int daysLate) {
        if (fineAmount <= 0 || daysLate <= 0) return 0.0;

        double penaltyRate = 0.05; // 5% per month
        int monthsLate = (int) Math.ceil(daysLate / 30.0);
        double penalty = fineAmount * penaltyRate * monthsLate;

        return Math.min(penalty, fineAmount * 0.5); // Max 50% penalty
    }

    /**
     * Gets the fine description for display purposes.
     *
     * @param violationType the type of violation
     * @return human-readable description
     */
    public String getFineDescription(String violationType) {
        if (violationType == null) return "Unknown violation";

        switch (violationType.toUpperCase()) {
            case "SPEEDING":
                return "Exceeding speed limit";
            case "EXPIRED_VEHICLE_DOCUMENTS":
                return "Vehicle with expired documents";
            case "NO_INSURANCE":
                return "Driving without valid insurance";
            case "STOLEN_VEHICLE":
                return "Stolen vehicle registration";
            case "NO_LICENSE":
                return "Driving without a valid license";
            case "EXPIRED_LICENSE":
                return "Driving with an expired license";
            case "FAILURE_TO_REPORT":
                return "Failure to report accident";
            case "ILLEGAL_PARKING":
                return "Illegal parking violation";
            default:
                return violationType.replace("_", " ").toLowerCase();
        }
    }
}