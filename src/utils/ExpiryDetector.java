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

/**
 * Utility class for detecting expired or soon-to-expire vehicle documents.
 * Provides methods for checking document expiry status and generating alerts.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ExpiryDetector {

    private static ExpiryDetector instance;
    private VehicleDocumentDAO documentDAO;
    private ExpiredDocumentAlertDAO alertDAO;

    // Threshold constants for expiry warnings
    public static final int CRITICAL_DAYS = 7;
    public static final int WARNING_DAYS = 15;
    public static final int DUE_SOON_DAYS = 30;

    /**
     * Private constructor for singleton pattern.
     */
    private ExpiryDetector() {
        try {
            this.documentDAO = new VehicleDocumentDAO();
            this.alertDAO = new ExpiredDocumentAlertDAO();
        } catch (Exception e) {
            System.err.println("Failed to initialize ExpiryDetector DAOs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the singleton instance of ExpiryDetector.
     *
     * @return the ExpiryDetector instance
     */
    public static synchronized ExpiryDetector getInstance() {
        if (instance == null) {
            instance = new ExpiryDetector();
        }
        return instance;
    }

    /**
     * Checks all documents for a vehicle and returns expiry status.
     *
     * @param registrationNumber the vehicle's registration number
     * @return Map containing expiry information and categorized documents
     */
    public Map<String, Object> checkVehicleDocuments(String registrationNumber) {
        Map<String, Object> result = new HashMap<>();
        List<ExpiredDocumentAlert> expiredDocs = new ArrayList<>();
        List<ExpiredDocumentAlert> criticalDocs = new ArrayList<>();
        List<ExpiredDocumentAlert> warningDocs = new ArrayList<>();
        List<ExpiredDocumentAlert> dueSoonDocs = new ArrayList<>();
        List<ExpiredDocumentAlert> validDocs = new ArrayList<>();

        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            result.put("error", "Registration number cannot be null or empty");
            return result;
        }

        try {
            List<VehicleDocument> documents = documentDAO.findByRegistrationNumber(registrationNumber);

            if (documents == null || documents.isEmpty()) {
                result.put("error", "No documents found for vehicle: " + registrationNumber);
                result.put("expiredCount", 0);
                result.put("criticalCount", 0);
                result.put("warningCount", 0);
                result.put("dueSoonCount", 0);
                result.put("overallStatus", "NO_DOCUMENTS");
                return result;
            }

            for (VehicleDocument doc : documents) {
                if (doc == null || doc.getExpiryDate() == null) {
                    continue;
                }

                int daysRemaining = DateUtil.getDaysUntilExpiry(doc.getExpiryDate());
                ExpiredDocumentAlert alert = createAlertFromDocument(doc, registrationNumber, daysRemaining);

                if (daysRemaining < 0) {
                    alert.setAlertLevel("EXPIRED");
                    alert.setRecommendedAction("VEHICLE_IMPOUND_RECOMMENDED");
                    expiredDocs.add(alert);
                } else if (daysRemaining <= CRITICAL_DAYS) {
                    alert.setAlertLevel("CRITICAL");
                    alert.setRecommendedAction("ON_THE_SPOT_FINE");
                    criticalDocs.add(alert);
                } else if (daysRemaining <= WARNING_DAYS) {
                    alert.setAlertLevel("WARNING");
                    alert.setRecommendedAction("WARNING_NOTICE");
                    warningDocs.add(alert);
                } else if (daysRemaining <= DUE_SOON_DAYS) {
                    alert.setAlertLevel("DUE_SOON");
                    alert.setRecommendedAction("REMINDER");
                    dueSoonDocs.add(alert);
                } else {
                    alert.setAlertLevel("VALID");
                    alert.setRecommendedAction("NO_ACTION");
                    validDocs.add(alert);
                }
            }

            // Populate result map
            result.put("expired", expiredDocs);
            result.put("critical", criticalDocs);
            result.put("warning", warningDocs);
            result.put("dueSoon", dueSoonDocs);
            result.put("valid", validDocs);
            result.put("expiredCount", expiredDocs.size());
            result.put("criticalCount", criticalDocs.size());
            result.put("warningCount", warningDocs.size());
            result.put("dueSoonCount", dueSoonDocs.size());
            result.put("validCount", validDocs.size());
            result.put("totalDocuments", documents.size());

            // Determine overall status
            String overallStatus;
            if (expiredDocs.size() > 0) {
                overallStatus = "VEHICLE_IMPOUND_RECOMMENDED";
            } else if (criticalDocs.size() > 0) {
                overallStatus = "IMMEDIATE_FINE";
            } else if (warningDocs.size() > 0) {
                overallStatus = "WARNING_NOTICE";
            } else if (dueSoonDocs.size() > 0) {
                overallStatus = "REMINDER_REQUIRED";
            } else {
                overallStatus = "CLEAN";
            }
            result.put("overallStatus", overallStatus);

        } catch (Exception e) {
            System.err.println("Error checking vehicle documents: " + e.getMessage());
            e.printStackTrace();
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Creates an ExpiredDocumentAlert from a VehicleDocument.
     *
     * @param doc                the vehicle document
     * @param registrationNumber the registration number
     * @param daysRemaining      days until expiry (negative if expired)
     * @return configured ExpiredDocumentAlert
     */
    private ExpiredDocumentAlert createAlertFromDocument(VehicleDocument doc, String registrationNumber, int daysRemaining) {
        ExpiredDocumentAlert alert = new ExpiredDocumentAlert();
        alert.setVehicleId(doc.getVehicleId());
        alert.setRegistrationNumber(registrationNumber);
        alert.setDocumentType(doc.getDocumentType());
        alert.setDocumentNumber(doc.getDocumentNumber());
        alert.setExpiryDate(doc.getExpiryDate());
        alert.setDaysOverdue(daysRemaining < 0 ? Math.abs(daysRemaining) : 0);
        alert.setIssueDate(doc.getIssueDate());
        // FIXED: Use getVehicleMake and getVehicleModel
        alert.setMake(doc.getVehicleMake());
        alert.setModel(doc.getVehicleModel());
        return alert;
    }

    /**
     * Runs the automated expired document detection and generates violations.
     * This method should be called periodically (e.g., daily via scheduler).
     */
    public void detectAndGenerateViolations() {
        try {
            if (alertDAO == null) {
                System.err.println("AlertDAO is null, cannot detect violations");
                return;
            }
            alertDAO.runExpiredDocumentDetection();
            System.out.println("Expired document detection completed at: " + DateUtil.getCurrentDateTime());
        } catch (Exception e) {
            System.err.println("Failed to run expired document detection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the expiry status for a single date.
     *
     * @param expiryDate the expiry date to check
     * @return status string: "EXPIRED", "CRITICAL", "WARNING", "DUE_SOON", "VALID", or "UNKNOWN"
     */
    public String getExpiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return "UNKNOWN";

        int daysRemaining = DateUtil.getDaysUntilExpiry(expiryDate);

        if (daysRemaining < 0) return "EXPIRED";
        if (daysRemaining <= CRITICAL_DAYS) return "CRITICAL";
        if (daysRemaining <= WARNING_DAYS) return "WARNING";
        if (daysRemaining <= DUE_SOON_DAYS) return "DUE_SOON";
        return "VALID";
    }

    /**
     * Gets the CSS color for displaying expiry status.
     *
     * @param expiryDate the expiry date
     * @return hex color code
     */
    public String getExpiryColor(LocalDate expiryDate) {
        String status = getExpiryStatus(expiryDate);
        switch (status) {
            case "EXPIRED": return "#F44336"; // Red
            case "CRITICAL": return "#FF9800"; // Orange
            case "WARNING": return "#FFC107"; // Amber
            case "DUE_SOON": return "#8BC34A"; // Light Green
            case "VALID": return "#4CAF50"; // Green
            default: return "#9E9E9E"; // Gray
        }
    }

    /**
     * Calculates the fine amount for expired documents.
     *
     * @param expiredCount number of expired documents
     * @param daysOverdue  number of days overdue
     * @return calculated fine amount (max M10,000)
     */
    public double calculateFineForExpiredDocuments(int expiredCount, int daysOverdue) {
        if (expiredCount <= 0) return 0.0;

        double baseFine = 500.00;
        double overduePenalty = Math.min(daysOverdue * 50.00, 5000.00);
        double totalFine = (baseFine * expiredCount) + overduePenalty;

        // Cap at M10,000
        return Math.min(totalFine, 10000.00);
    }

    /**
     * Generates a violation for a vehicle with expired documents.
     *
     * @param vehicleId    the vehicle ID
     * @param expiredCount number of expired documents
     * @param daysOverdue  days overdue
     * @return true if violation was generated, false otherwise
     */
    public boolean generateViolationForVehicle(int vehicleId, int expiredCount, int daysOverdue) {
        if (vehicleId <= 0) {
            System.err.println("Invalid vehicle ID: " + vehicleId);
            return false;
        }

        if (expiredCount <= 0) {
            return true; // No violation needed
        }

        try {
            double fineAmount = FineCalculator.getInstance().calculateExpiredDocumentFine(daysOverdue);
            String violationType = "EXPIRED_VEHICLE_DOCUMENTS";

            // In a real implementation, this would insert into the violations table
            System.out.println("Violation generated: " + violationType +
                    " for vehicle ID: " + vehicleId +
                    " fine: M" + String.format("%.2f", fineAmount));
            return true;
        } catch (Exception e) {
            System.err.println("Failed to generate violation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the count of vehicles with expired documents.
     *
     * @return number of vehicles with expired documents
     */
    public int getVehiclesWithExpiredDocuments() {
        try {
            if (alertDAO == null) return 0;
            return alertDAO.getVehiclesWithExpiredDocuments();
        } catch (Exception e) {
            System.err.println("Error getting vehicles with expired documents: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Sends notifications for documents expiring soon.
     *
     * @param daysThreshold number of days to check for "soon" expiry
     */
    public void sendExpiryNotifications(int daysThreshold) {
        try {
            List<ExpiredDocumentAlert> dueSoonDocs = alertDAO.findDocumentsExpiringWithin(daysThreshold);

            for (ExpiredDocumentAlert alert : dueSoonDocs) {
                // Send notification to vehicle owner
                System.out.println("Notification sent: Document " + alert.getDocumentType() +
                        " expires in " + alert.getDaysRemaining() + " days for vehicle " +
                        alert.getRegistrationNumber());
            }

            System.out.println("Sent " + dueSoonDocs.size() + " expiry notifications");
        } catch (Exception e) {
            System.err.println("Failed to send expiry notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
}