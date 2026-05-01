package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.IncidentReport;

public class IncidentReportDAO extends BaseDAO<IncidentReport> {

    @Override
    public IncidentReport findById(int id) throws SQLException {
        String sql = "SELECT * FROM police_reports WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<IncidentReport> findAll() throws SQLException {
        String sql = "SELECT * FROM police_reports ORDER BY report_date DESC";
        return executeQuery(sql);
    }

    public List<IncidentReport> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM police_reports WHERE vehicle_id = ? ORDER BY report_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<IncidentReport> findByReportType(String reportType) throws SQLException {
        String sql = "SELECT * FROM police_reports WHERE report_type = ? ORDER BY report_date DESC";
        return executeQuery(sql, reportType);
    }

    public List<IncidentReport> findByOfficer(String officerName) throws SQLException {
        String sql = "SELECT * FROM police_reports WHERE officer_name ILIKE ? ORDER BY report_date DESC";
        return executeQuery(sql, "%" + officerName + "%");
    }

    public List<IncidentReport> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM police_reports WHERE report_date BETWEEN ? AND ? ORDER BY report_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    @Override
    public boolean insert(IncidentReport entity) throws SQLException {
        String sql = "CALL sp_create_police_report(?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getVehicleId(),
                entity.getIncidentDateTime().toLocalDate(),
                entity.getIncidentType(),
                entity.getDescription(),
                entity.getOfficerName(),
                "",
                ""
        );
        return result >= 0;
    }

    @Override
    public boolean update(IncidentReport entity) throws SQLException {
        String sql = "UPDATE police_reports SET description = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getDescription(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM police_reports WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected IncidentReport mapRow(ResultSet rs) throws SQLException {
        IncidentReport report = new IncidentReport();
        report.setId(rs.getInt("id"));
        report.setVehicleId(rs.getInt("vehicle_id"));
        report.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getDate("report_date") != null) {
            report.setIncidentDateTime(rs.getDate("report_date").toLocalDate().atStartOfDay());
        }
        report.setIncidentType(rs.getString("report_type"));
        report.setDescription(rs.getString("description"));
        report.setOfficerName(rs.getString("officer_name"));

        if (rs.getTimestamp("created_at") != null) {
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return report;
    }
}