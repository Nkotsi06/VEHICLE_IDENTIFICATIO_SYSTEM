package utils;

public class PremiumCalculator {

    private static PremiumCalculator instance;

    private PremiumCalculator() {}

    public static synchronized PremiumCalculator getInstance() {
        if (instance == null) {
            instance = new PremiumCalculator();
        }
        return instance;
    }

    public double calculateBasePremium(int vehicleYear, String make, String model) {
        int vehicleAge = java.time.LocalDate.now().getYear() - vehicleYear;

        if (vehicleAge <= 3) return 2500.00;
        if (vehicleAge <= 7) return 1800.00;
        if (vehicleAge <= 10) return 1200.00;
        return 800.00;
    }

    public double calculateRiskPremium(int violationCount, int accidentCount) {
        double riskMultiplier = 1.0;
        riskMultiplier += violationCount * 0.1;
        riskMultiplier += accidentCount * 0.2;
        return Math.min(riskMultiplier, 2.5);
    }

    public double calculateNoClaimDiscount(int claimFreeYears) {
        if (claimFreeYears >= 5) return 0.50;
        if (claimFreeYears >= 4) return 0.40;
        if (claimFreeYears >= 3) return 0.30;
        if (claimFreeYears >= 2) return 0.20;
        if (claimFreeYears >= 1) return 0.10;
        return 0;
    }

    public double calculateFinalPremium(double basePremium, double riskMultiplier, double noClaimDiscount) {
        double withRisk = basePremium * riskMultiplier;
        return withRisk * (1 - noClaimDiscount);
    }
}