package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.NoClaimBonusRecord;

public class NoClaimBonusRecordDAO extends BaseDAO<NoClaimBonusRecord> {

    @Override
    public NoClaimBonusRecord findById(int id) throws SQLException {
        String sql = "SELECT ncb.*, ip.policy_number FROM no_claim_bonus_records ncb JOIN insurance_policies ip ON ncb.insurance_policy_id = ip.id WHERE ncb.id = ?";
        return executeQuerySingle(sql, id);
    }

    public NoClaimBonusRecord findByPolicyId(int policyId) throws SQLException {
        String sql = "SELECT ncb.*, ip.policy_number FROM no_claim_bonus_records ncb JOIN insurance_policies ip ON ncb.insurance_policy_id = ip.id WHERE ncb.insurance_policy_id = ?";
        return executeQuerySingle(sql, policyId);
    }

    @Override
    public List<NoClaimBonusRecord> findAll() throws SQLException {
        String sql = "SELECT ncb.*, ip.policy_number FROM no_claim_bonus_records ncb JOIN insurance_policies ip ON ncb.insurance_policy_id = ip.id ORDER BY ncb.insurance_policy_id";
        return executeQuery(sql);
    }

    public void calculateBonus(int policyId) throws SQLException {
        String sql = "CALL sp_calculate_no_claim_bonus(?)";
        executeUpdate(sql, policyId);
    }

    @Override
    public boolean insert(NoClaimBonusRecord entity) throws SQLException {
        String sql = "INSERT INTO no_claim_bonus_records (insurance_policy_id, policy_year, claim_free_years, bonus_percentage, calculated_date) VALUES (?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getInsurancePolicyId(),
                entity.getPolicyYear(),
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate()
        );
        return result > 0;
    }

    @Override
    public boolean update(NoClaimBonusRecord entity) throws SQLException {
        String sql = "UPDATE no_claim_bonus_records SET claim_free_years = ?, bonus_percentage = ?, calculated_date = ? WHERE insurance_policy_id = ? AND policy_year = ?";
        int result = executeUpdate(sql,
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate(),
                entity.getInsurancePolicyId(),
                entity.getPolicyYear()
        );
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM no_claim_bonus_records WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected NoClaimBonusRecord mapRow(ResultSet rs) throws SQLException {
        NoClaimBonusRecord record = new NoClaimBonusRecord();
        record.setId(rs.getInt("id"));
        record.setInsurancePolicyId(rs.getInt("insurance_policy_id"));
        record.setPolicyNumber(rs.getString("policy_number"));
        record.setPolicyYear(rs.getInt("policy_year"));
        record.setClaimFreeYears(rs.getInt("claim_free_years"));
        record.setBonusPercentage(rs.getDouble("bonus_percentage"));

        if (rs.getDate("calculated_date") != null) {
            record.setCalculatedDate(rs.getDate("calculated_date").toLocalDate());
        }

        if (rs.getTimestamp("created_at") != null) {
            record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return record;
    }
}