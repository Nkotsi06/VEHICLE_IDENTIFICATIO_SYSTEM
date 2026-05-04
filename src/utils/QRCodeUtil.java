package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for generating and parsing QR code data.
 * Creates standardized QR code strings for vehicle check-in and identification.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class QRCodeUtil {

    private static final Logger LOGGER = Logger.getLogger(QRCodeUtil.class.getName());
    private static QRCodeUtil instance;

    // QR Code format constants
    private static final String PREFIX = "VIS:";
    private static final String DELIMITER = "|";
    private static final String KEY_VALUE_SEPARATOR = ":";

    // Key constants
    private static final String KEY_VIS = "VIS";
    private static final String KEY_REG = "REG";
    private static final String KEY_TS = "TS";
    private static final String KEY_VIN = "VIN";
    private static final String KEY_OWNER = "OWNER";

    private QRCodeUtil() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance of QRCodeUtil.
     *
     * @return the QRCodeUtil instance
     */
    public static synchronized QRCodeUtil getInstance() {
        if (instance == null) {
            instance = new QRCodeUtil();
        }
        return instance;
    }

    /**
     * Generates QR code data for a vehicle.
     *
     * @param vehicleId          the vehicle's ID
     * @param registrationNumber the vehicle's registration number
     * @return formatted QR code string
     */
    public String generateQRCodeData(int vehicleId, String registrationNumber) {
        if (vehicleId <= 0) {
            LOGGER.warning("Invalid vehicle ID: " + vehicleId);
            return "";
        }

        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            LOGGER.warning("Registration number is null or empty");
            return "";
        }

        StringBuilder qrData = new StringBuilder();
        qrData.append(PREFIX).append(vehicleId).append(DELIMITER);
        qrData.append(KEY_REG).append(KEY_VALUE_SEPARATOR).append(registrationNumber).append(DELIMITER);
        qrData.append(KEY_TS).append(KEY_VALUE_SEPARATOR).append(System.currentTimeMillis());

        LOGGER.fine("Generated QR code data for vehicle: " + registrationNumber);
        return qrData.toString();
    }

    /**
     * Generates QR code data with additional vehicle information.
     *
     * @param vehicleId          the vehicle's ID
     * @param registrationNumber the vehicle's registration number
     * @param vin                the vehicle's VIN (optional)
     * @param ownerName          the owner's name (optional)
     * @return formatted QR code string
     */
    public String generateQRCodeData(int vehicleId, String registrationNumber, String vin, String ownerName) {
        if (vehicleId <= 0) {
            LOGGER.warning("Invalid vehicle ID: " + vehicleId);
            return "";
        }

        StringBuilder qrData = new StringBuilder(generateQRCodeData(vehicleId, registrationNumber));

        if (vin != null && !vin.trim().isEmpty()) {
            qrData.append(KEY_VIN).append(KEY_VALUE_SEPARATOR).append(vin).append(DELIMITER);
        }

        if (ownerName != null && !ownerName.trim().isEmpty()) {
            qrData.append(KEY_OWNER).append(KEY_VALUE_SEPARATOR).append(ownerName);
        }

        return qrData.toString();
    }

    /**
     * Parses QR code data into a key-value map.
     *
     * @param qrData the QR code string to parse
     * @return map of parsed key-value pairs
     */
    public Map<String, String> parseQRCodeData(String qrData) {
        Map<String, String> parsed = new HashMap<>();

        if (qrData == null || qrData.trim().isEmpty()) {
            LOGGER.warning("QR code data is null or empty");
            return parsed;
        }

        // Check for valid prefix
        if (!qrData.startsWith(PREFIX)) {
            LOGGER.warning("Invalid QR code format: missing prefix");
            return parsed;
        }

        try {
            String[] parts = qrData.split("\\" + DELIMITER);
            for (String part : parts) {
                String[] keyValue = part.split(KEY_VALUE_SEPARATOR, 2);
                if (keyValue.length == 2) {
                    parsed.put(keyValue[0], keyValue[1]);
                }
            }

            LOGGER.fine("Successfully parsed QR code with " + parsed.size() + " fields");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse QR code data: " + qrData, e);
        }

        return parsed;
    }

    /**
     * Validates the format of a QR code string.
     *
     * @param qrData the QR code string to validate
     * @return true if valid, false otherwise
     */
    public boolean validateQRCode(String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            return false;
        }

        Map<String, String> parsed = parseQRCodeData(qrData);
        boolean hasVehicleId = parsed.containsKey(KEY_VIS);
        boolean hasRegistration = parsed.containsKey(KEY_REG);
        boolean hasTimestamp = parsed.containsKey(KEY_TS);

        // Try to parse vehicle ID
        if (hasVehicleId) {
            try {
                Integer.parseInt(parsed.get(KEY_VIS));
            } catch (NumberFormatException e) {
                hasVehicleId = false;
            }
        }

        // Try to parse timestamp
        if (hasTimestamp) {
            try {
                Long.parseLong(parsed.get(KEY_TS));
            } catch (NumberFormatException e) {
                hasTimestamp = false;
            }
        }

        return hasVehicleId && hasRegistration && hasTimestamp;
    }

    /**
     * Extracts the vehicle ID from QR code data.
     *
     * @param qrData the QR code string
     * @return vehicle ID, or -1 if not found
     */
    public int getVehicleIdFromQRCode(String qrData) {
        Map<String, String> parsed = parseQRCodeData(qrData);
        String vehicleIdStr = parsed.get(KEY_VIS);

        if (vehicleIdStr != null) {
            try {
                return Integer.parseInt(vehicleIdStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid vehicle ID format: " + vehicleIdStr);
            }
        }

        return -1;
    }

    /**
     * Extracts the registration number from QR code data.
     *
     * @param qrData the QR code string
     * @return registration number, or null if not found
     */
    public String getRegistrationFromQRCode(String qrData) {
        Map<String, String> parsed = parseQRCodeData(qrData);
        return parsed.get(KEY_REG);
    }

    /**
     * Checks if a QR code is expired (older than specified hours).
     *
     * @param qrData         the QR code string
     * @param maxAgeHours    maximum age in hours
     * @return true if expired, false otherwise
     */
    public boolean isQRCodeExpired(String qrData, int maxAgeHours) {
        Map<String, String> parsed = parseQRCodeData(qrData);
        String timestampStr = parsed.get(KEY_TS);

        if (timestampStr == null) {
            return true;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();
            long ageHours = (currentTime - timestamp) / (1000 * 60 * 60);
            return ageHours > maxAgeHours;
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid timestamp format: " + timestampStr);
            return true;
        }
    }
}