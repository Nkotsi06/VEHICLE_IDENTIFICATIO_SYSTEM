package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.ServiceRecord;

public class ServiceRecordDAO extends BaseDAO<ServiceRecord> {

    @Override
    public ServiceRecord findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<ServiceRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_service_records ORDER BY service_date DESC";
        return executeQuery(sql);
    }

    public List<ServiceRecord> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE vehicle_id = ? ORDER BY service_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<ServiceRecord> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE workshop_id = ? ORDER BY service_date DESC";
        return executeQuery(sql, workshopId);
    }

    public List<ServiceRecord> findRecentByWorkshopId(int workshopId, int limit) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE workshop_id = ? ORDER BY service_date DESC LIMIT ?";
        return executeQuery(sql, workshopId, limit);
    }

    public List<ServiceRecord> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE service_date BETWEEN ? AND ? ORDER BY service_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public List<ServiceRecord> findByServiceType(String serviceType) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE service_type ILIKE ? ORDER BY service_date DESC";
        return executeQuery(sql, "%" + serviceType + "%");
    }

    public List<ServiceRecord> findByMechanicId(int mechanicId) throws SQLException {
        String sql = "SELECT * FROM vw_service_records WHERE mechanic_id = ? ORDER BY service_date DESC";
        return executeQuery(sql, mechanicId);
    }

    public int countByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_records WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double sumRevenueByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cost), 0) FROM service_records WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countUniqueVehiclesByWorkshopIdAndMonth(int workshopId, LocalDate month) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT vehicle_id) FROM service_records WHERE workshop_id = ? AND EXTRACT(YEAR FROM service_date) = ? AND EXTRACT(MONTH FROM service_date) = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            ps.setInt(2, month.getYear());
            ps.setInt(3, month.getMonthValue());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double averageCostByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(cost), 0) FROM service_records WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double getTotalRevenueByWorkshop(int workshopId) throws SQLException {
        return sumRevenueByWorkshopId(workshopId);
    }

    @Override
    public boolean insert(ServiceRecord entity) throws SQLException {
        return executeProcedure("sp_add_service_record",
                entity.getVehicleId(),
                entity.getWorkshopId(),
                entity.getMechanicId() > 0 ? entity.getMechanicId() : null,
                entity.getServiceDate(),
                entity.getServiceType(),
                entity.getDescription(),
                entity.getCost(),
                entity.getOdometerReading()
        );
    }

    @Override
    public boolean update(ServiceRecord entity) throws SQLException {
        return executeProcedure("sp_update_service_record",
                entity.getId(),
                entity.getVehicleId(),
                entity.getWorkshopId(),
                entity.getMechanicId(),
                entity.getServiceDate(),
                entity.getServiceType(),
                entity.getDescription(),
                entity.getCost(),
                entity.getOdometerReading()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_service_record", id);
    }

    @Override
    protected ServiceRecord mapRow(ResultSet rs) throws SQLException {
        ServiceRecord record = new ServiceRecord();
        record.setId(rs.getInt("id"));
        record.setVehicleId(rs.getInt("vehicle_id"));
        record.setRegistrationNumber(rs.getString("registration_number"));
        record.setMake(rs.getString("make"));
        record.setModel(rs.getString("model"));
        record.setWorkshopId(rs.getInt("workshop_id"));
        record.setWorkshopName(rs.getString("workshop_name"));

        if (rs.getObject("mechanic_id") != null) {
            record.setMechanicId(rs.getInt("mechanic_id"));
        }
        record.setMechanicName(rs.getString("mechanic_name"));

        if (rs.getDate("service_date") != null) {
            record.setServiceDate(rs.getDate("service_date").toLocalDate());
        }
        record.setServiceType(rs.getString("service_type"));
        record.setDescription(rs.getString("description"));
        record.setCost(rs.getDouble("cost"));

        if (rs.getObject("odometer_reading") != null) {
            record.setOdometerReading(rs.getInt("odometer_reading"));
        }
        record.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return record;
    }
}