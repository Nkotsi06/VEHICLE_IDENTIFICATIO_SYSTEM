package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PolicyRenewal;

/**
 * PolicyRenewalDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PolicyRenewalDAO extends BaseDAO<PolicyRenewal> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PolicyRenewalDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PolicyRenewal findById(int id) throws SQLException {
        List<PolicyRenewal> results = viewLoader.loadViewWithCondition("vw_policy_renewals", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<PolicyRenewal> findAll() throws SQLException {
        return viewLoader.loadView("vw_policy_renewals");
    }

    public List<PolicyRenewal> findByInsuranceId(int insuranceId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_policy_renewals", "insurance_id = ? ORDER BY renewal_date DESC", insuranceId);
    }

    public List<PolicyRenewal> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_policy_renewals", "vehicle_id = ? ORDER BY renewal_date DESC", vehicleId);
    }

    public List<PolicyRenewal> findPendingRenewals() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_policy_renewals", "payment_status = 'PENDING' ORDER BY renewal_date");
    }

    public List<PolicyRenewal> findRenewalsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_policy_renewals", "renewal_date BETWEEN ? AND ? ORDER BY renewal_date", startDate, endDate);
    }

    public boolean createRenewal(int insuranceId, LocalDate renewalDate, double premium) throws SQLException {
        return procedureCaller.executeCreatePolicyRenewal(insuranceId, renewalDate, premium);
    }

    public boolean processRenewalPayment(int renewalId) throws SQLException {
        return procedureCaller.executeProcessRenewalPayment(renewalId);
    }

    @Override
    public boolean insert(PolicyRenewal entity) throws SQLException {
        return createRenewal(entity.getInsuranceId(), entity.getRenewalDate(), entity.getPremium());
    }

    @Override
    public boolean update(PolicyRenewal entity) throws SQLException {
        if ("PAID".equals(entity.getPaymentStatus())) {
            return processRenewalPayment(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePolicyRenewal(id);
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