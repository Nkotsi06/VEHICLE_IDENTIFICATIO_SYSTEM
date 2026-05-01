package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.Payment;

public class PaymentDAO extends BaseDAO<Payment> {

    @Override
    public Payment findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_payments WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public Payment findByReceiptNumber(String receiptNumber) throws SQLException {
        String sql = "SELECT * FROM vw_payments WHERE receipt_number = ?";
        return executeQuerySingle(sql, receiptNumber);
    }

    public Payment findByTransactionId(String transactionId) throws SQLException {
        String sql = "SELECT * FROM vw_payments WHERE transaction_id = ?";
        return executeQuerySingle(sql, transactionId);
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_payments ORDER BY payment_date DESC";
        return executeQuery(sql);
    }

    public List<Payment> findByViolationId(int violationId) throws SQLException {
        String sql = "SELECT * FROM vw_payments WHERE violation_id = ? ORDER BY payment_date DESC";
        return executeQuery(sql, violationId);
    }

    public List<Payment> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_payments WHERE vehicle_id = ? ORDER BY payment_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<Payment> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    @Override
    public boolean insert(Payment entity) throws SQLException {
        String sql = "CALL sp_process_fine_payment(?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getViolationId(),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getReceiptNumber(),
                entity.getPaymentDate()
        );
        return result >= 0;
    }

    @Override
    public boolean update(Payment entity) throws SQLException {
        String sql = "UPDATE payments SET amount = ?, payment_method = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getAmount(), entity.getPaymentMethod(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM payments WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public double getTotalPaymentsByVehicle(int vehicleId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN violations v ON p.violation_id = v.id WHERE v.vehicle_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
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