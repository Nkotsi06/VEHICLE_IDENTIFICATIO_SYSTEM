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
import models.InsurancePayment;

/**
 * InsurancePaymentDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsurancePaymentDAO extends BaseDAO<InsurancePayment> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InsurancePaymentDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InsurancePayment findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsurancePayment(results.get(0));
    }

    public InsurancePayment findByReceiptNumber(String receiptNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "receipt_number = ?", receiptNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsurancePayment(results.get(0));
    }

    @Override
    public List<InsurancePayment> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_insurance_payments");
        return mapMapsToInsurancePayments(results);
    }

    public List<InsurancePayment> findByInsuranceId(int insuranceId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "insurance_id = ? ORDER BY payment_date DESC", insuranceId);
        return mapMapsToInsurancePayments(results);
    }

    public List<InsurancePayment> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "vehicle_id = ? ORDER BY payment_date DESC", vehicleId);
        return mapMapsToInsurancePayments(results);
    }

    public List<InsurancePayment> findPendingPayments() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "status = 'PENDING' ORDER BY due_date");
        return mapMapsToInsurancePayments(results);
    }

    public List<InsurancePayment> findOverduePayments() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "due_date < CURRENT_DATE AND status = 'PENDING' ORDER BY due_date");
        return mapMapsToInsurancePayments(results);
    }

    public List<InsurancePayment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "payment_date BETWEEN ? AND ? ORDER BY payment_date DESC", startDate, endDate);
        return mapMapsToInsurancePayments(results);
    }

    @Override
    public boolean insert(InsurancePayment entity) throws SQLException {
        return procedureCaller.executeRecordInsurancePayment(
                entity.getInsuranceId(),
                entity.getAmount(),
                entity.getPaymentDate(),
                entity.getDueDate(),
                entity.getLateFee(),
                entity.getPaymentMethod(),
                entity.getReceiptNumber()
        );
    }

    @Override
    public boolean update(InsurancePayment entity) throws SQLException {
        return procedureCaller.executeUpdateInsurancePaymentStatus(entity.getId(), entity.getStatus());
    }

    public boolean markAsCompleted(int paymentId) throws SQLException {
        return procedureCaller.executeCompleteInsurancePayment(paymentId);
    }

    public boolean markAsFailed(int paymentId) throws SQLException {
        return procedureCaller.executeFailInsurancePayment(paymentId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInsurancePayment(id);
    }

    public double getTotalCollected() throws SQLException {
        return viewLoader.getSumInsurancePaymentsCompleted();
    }

    /**
     * Converts a List of Maps to a List of InsurancePayment objects.
     */
    private List<InsurancePayment> mapMapsToInsurancePayments(List<Map<String, Object>> maps) {
        List<InsurancePayment> payments = new ArrayList<>();
        if (maps == null) {
            return payments;
        }
        for (Map<String, Object> map : maps) {
            InsurancePayment payment = mapMapToInsurancePayment(map);
            if (payment != null) {
                payments.add(payment);
            }
        }
        return payments;
    }

    /**
     * Converts a Map to an InsurancePayment object.
     */
    private InsurancePayment mapMapToInsurancePayment(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InsurancePayment payment = new InsurancePayment();

        payment.setId(getIntValue(map, "id"));
        payment.setInsuranceId(getIntValue(map, "insurance_id"));
        payment.setPolicyNumber(getStringValue(map, "policy_number"));
        payment.setVehicleId(getIntValue(map, "vehicle_id"));
        payment.setRegistrationNumber(getStringValue(map, "registration_number"));
        payment.setAmount(getDoubleValue(map, "amount"));
        payment.setLateFee(getDoubleValue(map, "late_fee"));
        payment.setPaymentMethod(getStringValue(map, "payment_method"));
        payment.setReceiptNumber(getStringValue(map, "receipt_number"));
        payment.setStatus(getStringValue(map, "status"));

        payment.setPaymentDate(getLocalDateValue(map, "payment_date"));
        payment.setDueDate(getLocalDateValue(map, "due_date"));
        payment.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        payment.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return payment;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate) value;
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected InsurancePayment mapRow(ResultSet rs) throws SQLException {
        InsurancePayment payment = new InsurancePayment();
        payment.setId(rs.getInt("id"));
        payment.setInsuranceId(rs.getInt("insurance_id"));
        payment.setPolicyNumber(rs.getString("policy_number"));
        payment.setVehicleId(rs.getInt("vehicle_id"));
        payment.setRegistrationNumber(rs.getString("registration_number"));
        payment.setAmount(rs.getDouble("amount"));

        if (rs.getDate("payment_date") != null) {
            payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        }
        if (rs.getDate("due_date") != null) {
            payment.setDueDate(rs.getDate("due_date").toLocalDate());
        }
        payment.setLateFee(rs.getDouble("late_fee"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setReceiptNumber(rs.getString("receipt_number"));
        payment.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            payment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return payment;
    }
}