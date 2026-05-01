package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.PolicyRenewal;

public class PolicyRenewalDAO extends BaseDAO<PolicyRenewal> {

    @Override
    public PolicyRenewal findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_policy_renewals WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<PolicyRenewal> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_policy_renewals ORDER BY renewal_date DESC";
        return executeQuery(sql);
    }

    public List<PolicyRenewal> findByInsuranceId(int insuranceId) throws SQLException {
        String sql = "SELECT * FROM vw_policy_renewals WHERE insurance_id = ? ORDER BY renewal_date DESC";
        return executeQuery(sql, insuranceId);
    }

    public List<PolicyRenewal> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_policy_renewals WHERE vehicle_id = ? ORDER BY renewal_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<PolicyRenewal> findPendingRenewals() throws SQLException {
        String sql = "SELECT * FROM vw_policy_renewals WHERE payment_status = 'PENDING' ORDER BY renewal_date";
        return executeQuery(sql);
    }

    public List<PolicyRenewal> findRenewalsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_policy_renewals WHERE renewal_date BETWEEN ? AND ? ORDER BY renewal_date";
        return executeQuery(sql, startDate, endDate);
    }

    public boolean createRenewal(int insuranceId, LocalDate renewalDate, double premium) throws SQLException {
        return executeProcedure("sp_create_policy_renewal", insuranceId, renewalDate, premium);
    }

    public boolean processRenewalPayment(int renewalId) throws SQLException {
        return executeProcedure("sp_process_renewal_payment", renewalId);
    }

    @Override
    public boolean insert(PolicyRenewal entity) throws SQLException {
        return executeProcedure("sp_create_policy_renewal",
                entity.getInsuranceId(),
                entity.getRenewalDate(),
                entity.getPremium()
        );
    }

    @Override
    public boolean update(PolicyRenewal entity) throws SQLException {
        String sql = "UPDATE policy_renewals SET payment_status = ?, payment_date = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getPaymentStatus(), entity.getPaymentDate(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_renewal", id);
    }

    @Override
    protected PolicyRenewal mapRow(ResultSet rs) throws SQLException {
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setId(rs.getInt("id"));
        renewal.setInsuranceId(rs.getInt("insurance_id"));
        renewal.setPolicyNumber(rs.getString("policy_number"));
        renewal.setVehicleId(rs.getInt("vehicle_id"));
        renewal.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getDate("renewal_date") != null) {
            renewal.setRenewalDate(rs.getDate("renewal_date").toLocalDate());
        }
        renewal.setPremium(rs.getDouble("premium"));
        renewal.setPaymentStatus(rs.getString("payment_status"));

        if (rs.getDate("payment_date") != null) {
            renewal.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        }

        if (rs.getTimestamp("created_at") != null) {
            renewal.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            renewal.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return renewal;
    }
}