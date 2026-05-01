package utils;

import java.util.HashMap;
import java.util.Map;

public class QRCodeUtil {

    private static QRCodeUtil instance;

    private QRCodeUtil() {}

    public static synchronized QRCodeUtil getInstance() {
        if (instance == null) {
            instance = new QRCodeUtil();
        }
        return instance;
    }

    public String generateQRCodeData(int vehicleId, String registrationNumber) {
        Map<String, String> data = new HashMap<>();
        data.put("vehicleId", String.valueOf(vehicleId));
        data.put("registrationNumber", registrationNumber);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        StringBuilder qrData = new StringBuilder();
        qrData.append("VIS:").append(vehicleId).append("|");
        qrData.append("REG:").append(registrationNumber).append("|");
        qrData.append("TS:").append(System.currentTimeMillis());

        return qrData.toString();
    }

    public Map<String, String> parseQRCodeData(String qrData) {
        Map<String, String> parsed = new HashMap<>();

        if (qrData == null || !qrData.startsWith("VIS:")) {
            return parsed;
        }

        String[] parts = qrData.split("\\|");
        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2) {
                parsed.put(keyValue[0], keyValue[1]);
            }
        }

        return parsed;
    }

    public boolean validateQRCode(String qrData) {
        Map<String, String> parsed = parseQRCodeData(qrData);
        return parsed.containsKey("VIS") && parsed.containsKey("REG") && parsed.containsKey("TS");
    }
}