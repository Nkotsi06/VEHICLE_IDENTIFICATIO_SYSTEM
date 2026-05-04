package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.NoClaimBonusRecord;

/**
 * NoClaimBonusRecordDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class NoClaimBonusRecordDAO extends BaseDAO<NoClaimBonusRecord> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public NoClaimBonusRecordDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public NoClaimBonusRecord findById(int id) throws SQLException {
        List<NoClaimBonusRecord> results = viewLoader.loadViewWithCondition("vw_no_claim_bonus", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public NoClaimBonusRecord findByPolicyId(int policyId) throws SQLException {
        List<NoClaimBonusRecord> results = viewLoader.loadViewWithCondition("vw_no_claim_bonus", "insurance_policy_id = ?", policyId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<NoClaimBonusRecord> findAll() throws SQLException {
        return viewLoader.loadView("vw_no_claim_bonus");
    }

    public void calculateBonus(int policyId) throws SQLException {
        procedureCaller.executeCalculateNoClaimBonus(policyId);
    }

    @Override
    public boolean insert(NoClaimBonusRecord entity) throws SQLException {
        return procedureCaller.executeInsertNoClaimBonusRecord(
                entity.getInsurancePolicyId(),
                entity.getPolicyYear(),
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate()
        );
    }

    @Override
    public boolean update(NoClaimBonusRecord entity) throws SQLException {
        return procedureCaller.executeUpdateNoClaimBonusRecord(
                entity.getInsurancePolicyId(),
                entity.getPolicyYear(),
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteNoClaimBonusRecord(id);
    }

    public boolean deleteByPolicyId(int policyId) throws SQLException {
        return procedureCaller.executeDeleteNoClaimBonusRecordsByPolicy(policyId);
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