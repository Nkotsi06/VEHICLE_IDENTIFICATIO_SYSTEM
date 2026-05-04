package utils;

import java.time.LocalDate;

/**
 * Utility class for calculating insurance premiums.
 * Handles base premium calculation, risk adjustments, and no-claim discounts.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PremiumCalculator {

    private static PremiumCalculator instance;

    // Vehicle age thresholds and base premiums
    private static final double NEW_VEHICLE_PREMIUM = 2500.00;
    private static final double MODERATE_AGE_PREMIUM = 1800.00;
    private static final double OLD_VEHICLE_PREMIUM = 1200.00;
    private static final double VERY_OLD_VEHICLE_PREMIUM = 800.00;

    private static final int NEW_VEHICLE_MAX_AGE = 3;
    private static final int MODERATE_VEHICLE_MAX_AGE = 7;
    private static final int OLD_VEHICLE_MAX_AGE = 10;

    // Risk multipliers
    private static final double VIOLATION_MULTIPLIER = 0.1;
    private static final double ACCIDENT_MULTIPLIER = 0.2;
    private static final double MAX_RISK_MULTIPLIER = 2.5;
    private static final double MIN_RISK_MULTIPLIER = 1.0;

    // No-claim discount rates
    private static final double FIVE_PLUS_YEARS_DISCOUNT = 0.50;
    private static final double FOUR_YEARS_DISCOUNT = 0.40;
    private static final double THREE_YEARS_DISCOUNT = 0.30;
    private static final double TWO_YEARS_DISCOUNT = 0.20;
    private static final double ONE_YEAR_DISCOUNT = 0.10;
    private static final double NO_DISCOUNT = 0.00;

    private PremiumCalculator() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance of PremiumCalculator.
     *
     * @return the PremiumCalculator instance
     */
    public static synchronized PremiumCalculator getInstance() {
        if (instance == null) {
            instance = new PremiumCalculator();
        }
        return instance;
    }

    /**
     * Calculates the base premium based on vehicle age, make, and model.
     *
     * @param vehicleYear the vehicle's manufacturing year
     * @param make        the vehicle's make (optional, for future enhancement)
     * @param model       the vehicle's model (optional, for future enhancement)
     * @return base premium amount
     */
    public double calculateBasePremium(int vehicleYear, String make, String model) {
        if (vehicleYear <= 0) {
            return VERY_OLD_VEHICLE_PREMIUM;
        }

        int currentYear = LocalDate.now().getYear();
        int vehicleAge = currentYear - vehicleYear;

        if (vehicleAge < 0) {
            // Future year? Use new vehicle premium
            return NEW_VEHICLE_PREMIUM;
        }

        if (vehicleAge <= NEW_VEHICLE_MAX_AGE) {
            return NEW_VEHICLE_PREMIUM;
        } else if (vehicleAge <= MODERATE_VEHICLE_MAX_AGE) {
            return MODERATE_AGE_PREMIUM;
        } else if (vehicleAge <= OLD_VEHICLE_MAX_AGE) {
            return OLD_VEHICLE_PREMIUM;
        } else {
            return VERY_OLD_VEHICLE_PREMIUM;
        }
    }

    /**
     * Calculates the risk premium multiplier based on violation and accident history.
     *
     * @param violationCount number of past violations
     * @param accidentCount  number of past accidents
     * @return risk multiplier (between 1.0 and 2.5)
     */
    public double calculateRiskPremium(int violationCount, int accidentCount) {
        double riskMultiplier = MIN_RISK_MULTIPLIER;

        // Ensure non-negative counts
        violationCount = Math.max(0, violationCount);
        accidentCount = Math.max(0, accidentCount);

        riskMultiplier += violationCount * VIOLATION_MULTIPLIER;
        riskMultiplier += accidentCount * ACCIDENT_MULTIPLIER;

        // Cap at maximum
        return Math.min(riskMultiplier, MAX_RISK_MULTIPLIER);
    }

    /**
     * Calculates the no-claim discount based on claim-free years.
     *
     * @param claimFreeYears number of consecutive years without claims
     * @return discount rate (0.0 to 0.5)
     */
    public double calculateNoClaimDiscount(int claimFreeYears) {
        claimFreeYears = Math.max(0, claimFreeYears);

        if (claimFreeYears >= 5) {
            return FIVE_PLUS_YEARS_DISCOUNT;
        } else if (claimFreeYears >= 4) {
            return FOUR_YEARS_DISCOUNT;
        } else if (claimFreeYears >= 3) {
            return THREE_YEARS_DISCOUNT;
        } else if (claimFreeYears >= 2) {
            return TWO_YEARS_DISCOUNT;
        } else if (claimFreeYears >= 1) {
            return ONE_YEAR_DISCOUNT;
        } else {
            return NO_DISCOUNT;
        }
    }

    /**
     * Calculates the final premium after applying risk multiplier and discount.
     *
     * @param basePremium       the base premium amount
     * @param riskMultiplier    the risk multiplier (1.0 to 2.5)
     * @param noClaimDiscount   the no-claim discount rate (0.0 to 0.5)
     * @return final premium amount
     */
    public double calculateFinalPremium(double basePremium, double riskMultiplier, double noClaimDiscount) {
        if (basePremium <= 0) {
            return 0.0;
        }

        // Validate inputs
        riskMultiplier = Math.max(MIN_RISK_MULTIPLIER, Math.min(MAX_RISK_MULTIPLIER, riskMultiplier));
        noClaimDiscount = Math.max(NO_DISCOUNT, Math.min(FIVE_PLUS_YEARS_DISCOUNT, noClaimDiscount));

        double withRisk = basePremium * riskMultiplier;
        return withRisk * (1 - noClaimDiscount);
    }

    /**
     * Convenience method to calculate final premium from vehicle data.
     *
     * @param vehicleYear      vehicle year
     * @param make             vehicle make
     * @param model            vehicle model
     * @param violationCount   violation count
     * @param accidentCount    accident count
     * @param claimFreeYears   claim-free years
     * @return final premium amount
     */
    public double calculatePremium(int vehicleYear, String make, String model,
                                   int violationCount, int accidentCount, int claimFreeYears) {
        double basePremium = calculateBasePremium(vehicleYear, make, model);
        double riskMultiplier = calculateRiskPremium(violationCount, accidentCount);
        double noClaimDiscount = calculateNoClaimDiscount(claimFreeYears);
        return calculateFinalPremium(basePremium, riskMultiplier, noClaimDiscount);
    }

    /**
     * Calculates the monthly installment amount.
     *
     * @param annualPremium the annual premium amount
     * @return monthly installment amount
     */
    public double calculateMonthlyPremium(double annualPremium) {
        if (annualPremium <= 0) {
            return 0.0;
        }
        return annualPremium / 12.0;
    }

    /**
     * Gets the premium range for display purposes.
     *
     * @param basePremium the base premium
     * @return formatted premium range
     */
    public String getPremiumRange(double basePremium) {
        double minPremium = basePremium * MIN_RISK_MULTIPLIER * (1 - FIVE_PLUS_YEARS_DISCOUNT);
        double maxPremium = basePremium * MAX_RISK_MULTIPLIER;
        return String.format("M%.2f - M%.2f", minPremium, maxPremium);
    }
}