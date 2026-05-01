package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.PoliceReport;

public class PoliceReportDAO extends BaseDAO<PoliceReport> {

    @Override
    public PoliceReport findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_police_reports WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public PoliceReport findByCaseNumber(String caseNumber) throws SQLException {
        String sql = "SELECT * FROM vw_police_reports WHERE case_number = ?";
        return executeQuerySingle(sql, caseNumber);
    }

    @Override
    public List<PoliceReport> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_police_reports ORDER BY report_date DESC";
        return executeQuery(sql);
    }

    public List<PoliceReport> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_police_reports WHERE vehicle_id = ? ORDER BY report_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<PoliceReport> findByReportType(String reportType) throws SQLException {
        String sql = "SELECT * FROM vw_police_reports WHERE report_type = ? ORDER BY report_date DESC";
        return executeQuery(sql, reportType);
    }

    public List<PoliceReport> findByOfficer(String officerName) throws SQLException {
        String sql = "SELECT * FROM vw_police_reports WHERE officer_name ILIKE ? ORDER BY report_date DESC";
        return executeQuery(sql, "%" + officerName + "%");
    }

    public List<PoliceReport> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_police_reports WHERE report_date BETWEEN ? AND ? ORDER BY report_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    @Override
    public boolean insert(PoliceReport entity) throws SQLException {
        return executeProcedure("sp_create_police_report",
                entity.getVehicleId(),
                entity.getReportDate(),
                entity.getReportType(),
                entity.getDescription(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getCaseNumber()
        );
    }

    @Override
    public boolean update(PoliceReport entity) throws SQLException {
        return executeProcedure("sp_update_police_report",
                entity.getId(),
                entity.getVehicleId(),
                entity.getReportDate(),
                entity.getReportType(),
                entity.getDescription(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getCaseNumber()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_police_report", id);
    }

    public int countReportsByType(String reportType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM police_reports WHERE report_type = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, reportType);
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
    protected PoliceReport mapRow(ResultSet rs) throws SQLException {
        PoliceReport report = new PoliceReport();
        report.setId(rs.getInt("id"));
        report.setVehicleId(rs.getInt("vehicle_id"));
        report.setRegistrationNumber(rs.getString("registration_number"));
        report.setMake(rs.getString("make"));
        report.setModel(rs.getString("model"));

        if (rs.getDate("report_date") != null) {
            report.setReportDate(rs.getDate("report_date").toLocalDate());
        }
        report.setReportType(rs.getString("report_type"));
        report.setDescription(rs.getString("description"));
        report.setOfficerName(rs.getString("officer_name"));
        report.setBadgeNumber(rs.getString("badge_number"));
        report.setCaseNumber(rs.getString("case_number"));

        if (rs.getTimestamp("created_at") != null) {
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return report;
    }
}