package utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.ExpiredDocumentAlertDAO;
import dao.VehicleDocumentDAO;
import models.ExpiredDocumentAlert;
import models.VehicleDocument;

public class ExpiryDetector {

    private static ExpiryDetector instance;
    private VehicleDocumentDAO documentDAO;
    private ExpiredDocumentAlertDAO alertDAO;

    private ExpiryDetector() {
        this.documentDAO = new VehicleDocumentDAO();
        this.alertDAO = new ExpiredDocumentAlertDAO();
    }

    public static synchronized ExpiryDetector getInstance() {
        if (instance == null) {
            instance = new ExpiryDetector();
        }
        return instance;
    }

    public Map<String, Object> checkVehicleDocuments(String registrationNumber) {
        Map<String, Object> result = new HashMap<>();
        List<ExpiredDocumentAlert> expiredDocs = new ArrayList<>();
        List<ExpiredDocumentAlert> criticalDocs = new ArrayList<>();
        List<ExpiredDocumentAlert> warningDocs = new ArrayList<>();

        try {
            List<VehicleDocument> documents = documentDAO.findByRegistrationNumber(registrationNumber);

            for (VehicleDocument doc : documents) {
                int daysRemaining = DateUtil.getDaysUntilExpiry(doc.getExpiryDate());

                ExpiredDocumentAlert alert = new ExpiredDocumentAlert();
                alert.setVehicleId(doc.getVehicleId());
                alert.setRegistrationNumber(registrationNumber);
                alert.setDocumentType(doc.getDocumentType());
                alert.setDocumentNumber(doc.getDocumentNumber());
                alert.setExpiryDate(doc.getExpiryDate());
                alert.setDaysOverdue(daysRemaining < 0 ? Math.abs(daysRemaining) : 0);

                if (daysRemaining < 0) {
                    alert.setAlertLevel("EXPIRED");
                    alert.setRecommendedAction("VEHICLE_IMPOUND_RECOMMENDED");
                    expiredDocs.add(alert);
                } else if (daysRemaining <= 7) {
                    alert.setAlertLevel("CRITICAL");
                    alert.setRecommendedAction("ON_THE_SPOT_FINE");
                    criticalDocs.add(alert);
                } else if (daysRemaining <= 15) {
                    alert.setAlertLevel("WARNING");
                    alert.setRecommendedAction("WARNING_NOTICE");
                    warningDocs.add(alert);
                } else if (daysRemaining <= 30) {
                    alert.setAlertLevel("DUE_SOON");
                    alert.setRecommendedAction("REMINDER");
                    warningDocs.add(alert);
                } else {
                    alert.setAlertLevel("VALID");
                    alert.setRecommendedAction("NO_ACTION");
                }
            }

            result.put("expired", expiredDocs);
            result.put("critical", criticalDocs);
            result.put("warning", warningDocs);
            result.put("expiredCount", expiredDocs.size());
            result.put("criticalCount", criticalDocs.size());
            result.put("warningCount", warningDocs.size());

            String overallStatus;
            if (expiredDocs.size() > 0) {
                overallStatus = "VEHICLE_IMPOUND_RECOMMENDED";
            } else if (criticalDocs.size() > 0) {
                overallStatus = "IMMEDIATE_FINE";
            } else if (warningDocs.size() > 0) {
                overallStatus = "WARNING_NOTICE";
            } else {
                overallStatus = "CLEAR";
            }
            result.put("overallStatus", overallStatus);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getMessage());
        }

        return result;
    }

    public void detectAndGenerateViolations() {
        try {
            alertDAO.runExpiredDocumentDetection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getExpiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return "UNKNOWN";

        int daysRemaining = DateUtil.getDaysUntilExpiry(expiryDate);

        if (daysRemaining < 0) return "EXPIRED";
        if (daysRemaining <= 7) return "CRITICAL";
        if (daysRemaining <= 15) return "WARNING";
        if (daysRemaining <= 30) return "DUE_SOON";
        return "VALID";
    }

    public String getExpiryColor(LocalDate expiryDate) {
        String status = getExpiryStatus(expiryDate);
        switch (status) {
            case "EXPIRED": return "#F44336";
            case "CRITICAL": return "#FF9800";
            case "WARNING": return "#FFC107";
            case "DUE_SOON": return "#8BC34A";
            default: return "#4CAF50";
        }
    }

    public double calculateFineForExpiredDocuments(int expiredCount, int daysOverdue) {
        double baseFine = 500.00;
        double overduePenalty = daysOverdue * 50.00;
        double totalFine = (baseFine * expiredCount) + overduePenalty;
        return Math.min(totalFine, 10000.00);
    }

    public boolean generateViolationForVehicle(int vehicleId, int expiredCount, int daysOverdue) {
        try {
            double fineAmount = calculateFineForExpiredDocuments(expiredCount, daysOverdue);
            String violationType = "EXPIRED_VEHICLE_DOCUMENTS";
            System.out.println("Violation generated: " + violationType + " fine: " + fineAmount);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}