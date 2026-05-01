package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.InsurancePolicy;

public class InsurancePolicyDAO extends BaseDAO<InsurancePolicy> {

    @Override
    public InsurancePolicy findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public InsurancePolicy findByPolicyNumber(String policyNumber) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE policy_number = ?";
        return executeQuerySingle(sql, policyNumber);
    }

    public List<InsurancePolicy> searchByPolicyNumber(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE policy_number ILIKE ? ORDER BY policy_number";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern);
    }

    public List<InsurancePolicy> searchByRegistrationNumber(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE registration_number ILIKE ? ORDER BY registration_number";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern);
    }

    public List<InsurancePolicy> searchByProviderName(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE provider_name ILIKE ? ORDER BY provider_name";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern);
    }

    public List<InsurancePolicy> searchInsurance(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE " +
                "policy_number ILIKE ? OR " +
                "registration_number ILIKE ? OR " +
                "provider_name ILIKE ? OR " +
                "status ILIKE ? " +
                "ORDER BY policy_number";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern, searchPattern, searchPattern, searchPattern);
    }

    @Override
    public List<InsurancePolicy> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies ORDER BY start_date DESC";
        return executeQuery(sql);
    }

    public List<InsurancePolicy> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE vehicle_id = ? ORDER BY start_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<InsurancePolicy> findActivePolicies() throws SQLException {
        String sql = "SELECT * FROM vw_active_insurance ORDER BY end_date";
        return executeQuery(sql);
    }

    public List<InsurancePolicy> findByProviderId(int providerId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE provider_id = ? ORDER BY start_date DESC";
        return executeQuery(sql, providerId);
    }

    public List<InsurancePolicy> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT ip.* FROM vw_insurance_policies ip " +
                "JOIN vehicles v ON ip.vehicle_id = v.id " +
                "WHERE v.owner_id = ? ORDER BY ip.start_date DESC";
        return executeQuery(sql, customerId);
    }

    public List<InsurancePolicy> findExpiringPolicies(int daysThreshold) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE status = 'ACTIVE' AND end_date <= CURRENT_DATE + interval '" + daysThreshold + " days' ORDER BY end_date";
        return executeQuery(sql);
    }

    public List<InsurancePolicy> findExpiredPolicies() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_policies WHERE end_date < CURRENT_DATE AND status = 'ACTIVE'";
        return executeQuery(sql);
    }

    public int countExpiredByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_policies ip " +
                "JOIN vehicles v ON ip.vehicle_id = v.id " +
                "WHERE v.owner_id = ? AND (ip.status = 'EXPIRED' OR ip.end_date < CURRENT_DATE)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countByProviderId(int providerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_policies WHERE provider_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, providerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countActiveByProviderId(int providerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_policies WHERE provider_id = ? AND status = 'ACTIVE' AND end_date >= CURRENT_DATE";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, providerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double getTotalPremiumByProvider(int providerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(premium), 0) FROM insurance_policies WHERE provider_id = ? AND status = 'ACTIVE'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, providerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countActivePolicies() throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_policies WHERE status = 'ACTIVE' AND end_date >= CURRENT_DATE";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(InsurancePolicy entity) throws SQLException {
        Integer policyId = executeProcedureWithInOutParameter("sp_add_insurance_policy",
                entity.getVehicleId(),
                entity.getProviderId(),
                entity.getPolicyNumber(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPremium(),
                entity.getCoverageAmount(),
                entity.getStatus()
        );
        if (policyId != null && policyId > 0) {
            entity.setId(policyId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(InsurancePolicy entity) throws SQLException {
        return executeProcedure("sp_update_insurance_policy",
                entity.getId(),
                entity.getVehicleId(),
                entity.getProviderId(),
                entity.getPolicyNumber(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPremium(),
                entity.getCoverageAmount(),
                entity.getStatus()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_insurance_policy", id);
    }

    @Override
    protected InsurancePolicy mapRow(ResultSet rs) throws SQLException {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(rs.getInt("id"));
        policy.setVehicleId(rs.getInt("vehicle_id"));
        policy.setRegistrationNumber(rs.getString("registration_number"));
        policy.setMake(rs.getString("make"));
        policy.setModel(rs.getString("model"));
        policy.setProviderId(rs.getInt("provider_id"));
        policy.setProviderName(rs.getString("provider_name"));
        policy.setPolicyNumber(rs.getString("policy_number"));

        if (rs.getDate("start_date") != null) {
            policy.setStartDate(rs.getDate("start_date").toLocalDate());
        }
        if (rs.getDate("end_date") != null) {
            policy.setEndDate(rs.getDate("end_date").toLocalDate());
        }
        policy.setPremium(rs.getDouble("premium"));
        policy.setCoverageAmount(rs.getDouble("coverage_amount"));
        policy.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            policy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            policy.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return policy;
    }
}