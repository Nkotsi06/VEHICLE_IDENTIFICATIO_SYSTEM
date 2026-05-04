package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.ServiceRecord;

/**
 * ServiceRecordDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ServiceRecordDAO extends BaseDAO<ServiceRecord> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ServiceRecordDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public ServiceRecord findById(int id) throws SQLException {
        List<ServiceRecord> results = viewLoader.loadViewWithCondition("vw_service_records", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ServiceRecord> findAll() throws SQLException {
        return viewLoader.loadView("vw_service_records");
    }

    public List<ServiceRecord> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_records", "vehicle_id = ? ORDER BY service_date DESC", vehicleId);
    }

    public List<ServiceRecord> findByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_records", "workshop_id = ? ORDER BY service_date DESC", workshopId);
    }

    public List<ServiceRecord> findRecentByWorkshopId(int workshopId, int limit) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_records", "workshop_id = ? ORDER BY service_date DESC LIMIT ?", workshopId, limit);
    }

    public List<ServiceRecord> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_records", "service_date BETWEEN ? AND ? ORDER BY service_date DESC", startDate, endDate);
    }

    public List<ServiceRecord> findByServiceType(String serviceType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_records", "service_type ILIKE ? ORDER BY service_date DESC", "%" + serviceType + "%");
    }

    public List<ServiceRecord> findByMechanicId(int mechanicId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_records", "mechanic_id = ? ORDER BY service_date DESC", mechanicId);
    }

    public int countByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_service_records", "workshop_id = ?", workshopId);
    }

    public double sumRevenueByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.getSumServiceCostByWorkshop(workshopId);
    }

    public int countUniqueVehiclesByWorkshopIdAndMonth(int workshopId, LocalDate month) throws SQLException {
        return viewLoader.countDistinctVehiclesByWorkshopAndMonth(workshopId, month);
    }

    public double averageCostByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.getAverageServiceCostByWorkshop(workshopId);
    }

    public double getTotalRevenueByWorkshop(int workshopId) throws SQLException {
        return sumRevenueByWorkshopId(workshopId);
    }

    @Override
    public boolean insert(ServiceRecord entity) throws SQLException {
        Integer recordId = procedureCaller.executeAddServiceRecord(
                entity.getVehicleId(),
                entity.getWorkshopId(),
                entity.getMechanicId() > 0 ? entity.getMechanicId() : null,
                java.sql.Date.valueOf(entity.getServiceDate()),
                entity.getServiceType(),
                entity.getDescription(),
                entity.getCost(),
                entity.getOdometerReading()
        );
        if (recordId != null && recordId > 0) {
            entity.setId(recordId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ServiceRecord entity) throws SQLException {
        return procedureCaller.executeUpdateServiceRecord(
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
        return procedureCaller.executeDeleteServiceRecord(id);
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