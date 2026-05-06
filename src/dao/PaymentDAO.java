package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Payment;

/**
 * PaymentDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PaymentDAO extends BaseDAO<Payment> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PaymentDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Payment findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payments", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPayment(results.get(0));
    }

    public Payment findByReceiptNumber(String receiptNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payments", "receipt_number = ?", receiptNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPayment(results.get(0));
    }

    public Payment findByTransactionId(String transactionId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payments", "transaction_id = ?", transactionId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPayment(results.get(0));
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_payments");
        return mapMapsToPayments(results);
    }

    public List<Payment> findByViolationId(int violationId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payments", "violation_id = ? ORDER BY payment_date DESC", violationId);
        return mapMapsToPayments(results);
    }

    public List<Payment> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payments", "vehicle_id = ? ORDER BY payment_date DESC", vehicleId);
        return mapMapsToPayments(results);
    }

    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payments", "payment_date BETWEEN ? AND ? ORDER BY payment_date DESC", startDate, endDate);
        return mapMapsToPayments(results);
    }

    @Override
    public boolean insert(Payment entity) throws SQLException {
        Integer paymentId = procedureCaller.executeProcessFinePayment(
                entity.getViolationId(),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getReceiptNumber(),
                entity.getPaymentDate()
        );
        if (paymentId != null && paymentId > 0) {
            entity.setId(paymentId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Payment entity) throws SQLException {
        return procedureCaller.executeUpdatePayment(
                entity.getId(),
                entity.getAmount(),
                entity.getPaymentMethod()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePayment(id);
    }

    public double getTotalPaymentsByVehicle(int vehicleId) throws SQLException {
        return viewLoader.getSumPaymentsByVehicle(vehicleId);
    }

    public double getTotalPaymentsByViolation(int violationId) throws SQLException {
        List<Payment> payments = findByViolationId(violationId);
        double total = 0;
        for (Payment payment : payments) {
            total += payment.getAmount();
        }
        return total;
    }

    /**
     * Converts a List of Maps to a List of Payment objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of Payment objects
     */
    private List<Payment> mapMapsToPayments(List<Map<String, Object>> maps) {
        List<Payment> payments = new ArrayList<>();
        if (maps == null) {
            return payments;
        }
        for (Map<String, Object> map : maps) {
            Payment payment = mapMapToPayment(map);
            if (payment != null) {
                payments.add(payment);
            }
        }
        return payments;
    }

    /**
     * Converts a Map to a Payment object.
     *
     * @param map the map from the view loader
     * @return Payment object
     */
    private Payment mapMapToPayment(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Payment payment = new Payment();

        payment.setId(getIntValue(map, "id"));
        payment.setViolationId(getIntValue(map, "violation_id"));
        payment.setVehicleId(getIntValue(map, "vehicle_id"));
        payment.setRegistrationNumber(getStringValue(map, "registration_number"));
        payment.setAmount(getDoubleValue(map, "amount"));
        payment.setPaymentMethod(getStringValue(map, "payment_method"));
        payment.setTransactionId(getStringValue(map, "transaction_id"));
        payment.setReceiptNumber(getStringValue(map, "receipt_number"));

        payment.setPaymentDate(getLocalDateValue(map, "payment_date"));
        payment.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        payment.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return payment;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get Double values from Map.
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDate values from Map.
     */
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
    }

    @Override
    protected Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setViolationId(rs.getInt("violation_id"));
        payment.setVehicleId(rs.getInt("vehicle_id"));
        payment.setRegistrationNumber(rs.getString("registration_number"));
        payment.setAmount(rs.getDouble("amount"));

        if (rs.getDate("payment_date") != null) {
            payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        }
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setReceiptNumber(rs.getString("receipt_number"));

        if (rs.getTimestamp("created_at") != null) {
            payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            payment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return payment;
    }
}