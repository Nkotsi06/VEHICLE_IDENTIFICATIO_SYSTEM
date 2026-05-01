package utils;

public class FineCalculator {

    private static FineCalculator instance;

    private FineCalculator() {}

    public static synchronized FineCalculator getInstance() {
        if (instance == null) {
            instance = new FineCalculator();
        }
        return instance;
    }

    public double calculateSpeedingFine(double speedKmph, int speedLimit) {
        double excess = speedKmph - speedLimit;
        if (excess <= 0) return 0;

        if (excess <= 10) return 300.00;
        if (excess <= 20) return 500.00;
        if (excess <= 30) return 1000.00;
        if (excess <= 40) return 2000.00;
        return 5000.00;
    }

    public double calculateExpiredDocumentFine(int daysOverdue) {
        if (daysOverdue <= 0) return 0;
        if (daysOverdue <= 30) return 500.00;
        if (daysOverdue <= 90) return 1000.00;
        return 2500.00;
    }

    public double calculateNoInsuranceFine(int daysWithoutInsurance) {
        if (daysWithoutInsurance <= 0) return 0;
        if (daysWithoutInsurance <= 7) return 500.00;
        if (daysWithoutInsurance <= 30) return 1500.00;
        return 5000.00;
    }

    public double calculateStolenVehicleFine() {
        return 2000.00;
    }

    public double calculateTotalFine(double... fines) {
        double total = 0;
        for (double fine : fines) {
            total += fine;
        }
        return total;
    }
}