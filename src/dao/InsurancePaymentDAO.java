package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.InsurancePayment;

public class InsurancePaymentDAO extends BaseDAO<InsurancePayment> {

    @Override
    public InsurancePayment findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public InsurancePayment findByReceiptNumber(String receiptNumber) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE receipt_number = ?";
        return executeQuerySingle(sql, receiptNumber);
    }

    @Override
    public List<InsurancePayment> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments ORDER BY payment_date DESC";
        return executeQuery(sql);
    }

    public List<InsurancePayment> findByInsuranceId(int insuranceId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE insurance_id = ? ORDER BY payment_date DESC";
        return executeQuery(sql, insuranceId);
    }

    public List<InsurancePayment> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE vehicle_id = ? ORDER BY payment_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<InsurancePayment> findPendingPayments() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE status = 'PENDING' ORDER BY due_date";
        return executeQuery(sql);
    }

    public List<InsurancePayment> findOverduePayments() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE due_date < CURRENT_DATE AND status = 'PENDING' ORDER BY due_date";
        return executeQuery(sql);
    }

    public List<InsurancePayment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    @Override
    public boolean insert(InsurancePayment entity) throws SQLException {
        String sql = "CALL sp_record_insurance_payment(?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getInsuranceId(),
                entity.getAmount(),
                entity.getPaymentDate(),
                entity.getDueDate(),
                entity.getLateFee(),
                entity.getPaymentMethod(),
                entity.getReceiptNumber()
        );
        return result >= 0;
    }

    @Override
    public boolean update(InsurancePayment entity) throws SQLException {
        String sql = "UPDATE insurance_payments SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getId());
        return result > 0;
    }

    public boolean markAsCompleted(int paymentId) throws SQLException {
        String sql = "UPDATE insurance_payments SET status = 'COMPLETED' WHERE id = ?";
        int result = executeUpdate(sql, paymentId);
        return result > 0;
    }

    public boolean markAsFailed(int paymentId) throws SQLException {
        String sql = "UPDATE insurance_payments SET status = 'FAILED' WHERE id = ?";
        int result = executeUpdate(sql, paymentId);
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM insurance_payments WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public double getTotalCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount + COALESCE(late_fee, 0)), 0) FROM insurance_payments WHERE status = 'COMPLETED'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
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