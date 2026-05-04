package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Violation;

/**
 * ViolationDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ViolationDAO extends BaseDAO<Violation> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ViolationDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Violation findById(int id) throws SQLException {
        List<Violation> results = viewLoader.loadViewWithCondition("vw_violations", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Violation> findAll() throws SQLException {
        return viewLoader.loadView("vw_violations");
    }

    public List<Violation> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_violations", "vehicle_id = ? ORDER BY violation_date DESC", vehicleId);
    }

    public List<Violation> findByRegistrationNumber(String registrationNumber) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_violations", "registration_number = ? ORDER BY violation_date DESC", registrationNumber);
    }

    public List<Violation> findUnpaidViolations() throws SQLException {
        return viewLoader.loadView("vw_unpaid_violations");
    }

    public List<Violation> findPaidViolations() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_violations", "payment_status = 'PAID' ORDER BY violation_date DESC");
    }

    public List<Violation> findByViolationType(String violationType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_violations", "violation_type ILIKE ? ORDER BY violation_date DESC", "%" + violationType + "%");
    }

    public List<Violation> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_violations", "violation_date BETWEEN ? AND ? ORDER BY violation_date DESC", startDate, endDate);
    }

    public List<Violation> findByOfficer(String officerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_violations", "officer_name ILIKE ? ORDER BY violation_date DESC", "%" + officerName + "%");
    }

    @Override
    public boolean insert(Violation entity) throws SQLException {
        Integer violationId = procedureCaller.executeAddViolation(
                entity.getVehicleId(),
                java.sql.Date.valueOf(entity.getViolationDate()),
                entity.getViolationType(),
                entity.getFineAmount(),
                entity.getLocation(),
                entity.getOfficerName(),
                entity.getLatitude(),
                entity.getLongitude()
        );
        if (violationId != null && violationId > 0) {
            entity.setId(violationId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Violation entity) throws SQLException {
        return procedureCaller.executeUpdateViolation(
                entity.getId(),
                entity.getVehicleId(),
                entity.getViolationDate(),
                entity.getViolationType(),
                entity.getFineAmount(),
                entity.getLocation(),
                entity.getOfficerName(),
                entity.getPaymentStatus()
        );
    }

    public boolean markAsPaid(int violationId) throws SQLException {
        return procedureCaller.executeMarkViolationPaid(violationId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteViolation(id);
    }

    public double getTotalUnpaidFines() throws SQLException {
        return viewLoader.getSumUnpaidFines();
    }

    public int countViolationsByVehicle(int vehicleId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_violations", "vehicle_id = ?", vehicleId);
    }

    @Override
    protected Violation mapRow(ResultSet rs) throws SQLException {
        Violation violation = new Violation();
        violation.setId(rs.getInt("id"));
        violation.setVehicleId(rs.getInt("vehicle_id"));
        violation.setRegistrationNumber(rs.getString("registration_number"));
        violation.setMake(rs.getString("make"));
        violation.setModel(rs.getString("model"));

        if (rs.getDate("violation_date") != null) {
            violation.setViolationDate(rs.getDate("violation_date").toLocalDate());
        }
        violation.setViolationType(rs.getString("violation_type"));
        violation.setFineAmount(rs.getDouble("fine_amount"));
        violation.setPaymentStatus(rs.getString("payment_status"));
        violation.setLocation(rs.getString("location"));
        violation.setOfficerName(rs.getString("officer_name"));

        if (rs.getTimestamp("created_at") != null) {
            violation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            violation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return violation;
    }
}