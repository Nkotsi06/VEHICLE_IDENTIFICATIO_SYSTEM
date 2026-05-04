package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        List<Payment> results = viewLoader.loadViewWithCondition("vw_payments", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Payment findByReceiptNumber(String receiptNumber) throws SQLException {
        List<Payment> results = viewLoader.loadViewWithCondition("vw_payments", "receipt_number = ?", receiptNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    public Payment findByTransactionId(String transactionId) throws SQLException {
        List<Payment> results = viewLoader.loadViewWithCondition("vw_payments", "transaction_id = ?", transactionId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        return viewLoader.loadView("vw_payments");
    }

    public List<Payment> findByViolationId(int violationId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_payments", "violation_id = ? ORDER BY payment_date DESC", violationId);
    }

    public List<Payment> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_payments", "vehicle_id = ? ORDER BY payment_date DESC", vehicleId);
    }

    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_payments", "payment_date BETWEEN ? AND ? ORDER BY payment_date DESC", startDate, endDate);
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