package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        List<InsurancePayment> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public InsurancePayment findByReceiptNumber(String receiptNumber) throws SQLException {
        List<InsurancePayment> results = viewLoader.loadViewWithCondition("vw_insurance_payments", "receipt_number = ?", receiptNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<InsurancePayment> findAll() throws SQLException {
        return viewLoader.loadView("vw_insurance_payments");
    }

    public List<InsurancePayment> findByInsuranceId(int insuranceId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_payments", "insurance_id = ? ORDER BY payment_date DESC", insuranceId);
    }

    public List<InsurancePayment> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_payments", "vehicle_id = ? ORDER BY payment_date DESC", vehicleId);
    }

    public List<InsurancePayment> findPendingPayments() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_payments", "status = 'PENDING' ORDER BY due_date");
    }

    public List<InsurancePayment> findOverduePayments() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_payments", "due_date < CURRENT_DATE AND status = 'PENDING' ORDER BY due_date");
    }

    public List<InsurancePayment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_payments", "payment_date BETWEEN ? AND ? ORDER BY payment_date DESC", startDate, endDate);
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