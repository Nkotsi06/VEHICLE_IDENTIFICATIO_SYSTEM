package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.InsuranceClaim;

public class InsuranceClaimDAO extends BaseDAO<InsuranceClaim> {

    @Override
    public InsuranceClaim findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_claims WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<InsuranceClaim> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_claims ORDER BY claim_date DESC";
        return executeQuery(sql);
    }

    public List<InsuranceClaim> findByPolicyId(int policyId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_claims WHERE policy_id = ? ORDER BY claim_date DESC";
        return executeQuery(sql, policyId);
    }

    public List<InsuranceClaim> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_claims WHERE vehicle_id = ? ORDER BY claim_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<InsuranceClaim> findByProviderId(int providerId) throws SQLException {
        String sql = "SELECT c.* FROM vw_insurance_claims c " +
                "JOIN insurance_policies p ON c.policy_id = p.id " +
                "WHERE p.provider_id = ? " +
                "ORDER BY c.claim_date DESC";
        return executeQuery(sql, providerId);
    }

    public List<InsuranceClaim> findByProviderIdAndDateRange(int providerId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT c.* FROM vw_insurance_claims c " +
                "JOIN insurance_policies p ON c.policy_id = p.id " +
                "WHERE p.provider_id = ? " +
                "AND c.claim_date BETWEEN ? AND ? " +
                "ORDER BY c.claim_date DESC";
        return executeQuery(sql, providerId, startDate, endDate);
    }

    public List<InsuranceClaim> findPendingClaims() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_claims WHERE status = 'PENDING' ORDER BY claim_date";
        return executeQuery(sql);
    }

    public List<InsuranceClaim> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_claims WHERE status = ? ORDER BY claim_date DESC";
        return executeQuery(sql, status);
    }

    public int countByProviderId(int providerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_claims c " +
                "JOIN insurance_policies p ON c.policy_id = p.id " +
                "WHERE p.provider_id = ?";
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

    public int countPendingByProviderId(int providerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_claims c " +
                "JOIN insurance_policies p ON c.policy_id = p.id " +
                "WHERE p.provider_id = ? AND c.status = 'PENDING'";
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

    public int countPendingClaims() throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_claims WHERE status = 'PENDING'";
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

    public int countResolvedByProviderId(int providerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_claims c " +
                "JOIN insurance_policies p ON c.policy_id = p.id " +
                "WHERE p.provider_id = ? AND c.status IN ('APPROVED', 'PAID')";
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

    @Override
    public boolean insert(InsuranceClaim entity) throws SQLException {
        return executeProcedure("sp_submit_insurance_claim",
                entity.getPolicyId(),
                entity.getClaimAmount(),
                entity.getDescription()
        );
    }

    public boolean approveClaim(int claimId, double approvedAmount) throws SQLException {
        return executeProcedure("sp_approve_claim", claimId, approvedAmount);
    }

    public boolean rejectClaim(int claimId, String rejectionReason) throws SQLException {
        return executeProcedure("sp_reject_claim", claimId, rejectionReason);
    }

    @Override
    public boolean update(InsuranceClaim entity) throws SQLException {
        String sql = "UPDATE insurance_claims SET status = ?, approved_amount = ?, rejection_reason = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getApprovedAmount(), entity.getRejectionReason(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM insurance_claims WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected InsuranceClaim mapRow(ResultSet rs) throws SQLException {
        InsuranceClaim claim = new InsuranceClaim();
        claim.setId(rs.getInt("id"));
        claim.setPolicyId(rs.getInt("policy_id"));
        claim.setVehicleId(rs.getInt("vehicle_id"));
        claim.setRegistrationNumber(rs.getString("registration_number"));
        claim.setPolicyNumber(rs.getString("policy_number"));

        if (rs.getDate("claim_date") != null) {
            claim.setClaimDate(rs.getDate("claim_date").toLocalDate());
        }
        claim.setClaimAmount(rs.getDouble("claim_amount"));
        claim.setDescription(rs.getString("description"));
        claim.setStatus(rs.getString("status"));

        if (rs.getObject("approved_amount") != null) {
            claim.setApprovedAmount(rs.getDouble("approved_amount"));
        }
        claim.setRejectionReason(rs.getString("rejection_reason"));

        if (rs.getTimestamp("created_at") != null) {
            claim.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            claim.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return claim;
    }
}