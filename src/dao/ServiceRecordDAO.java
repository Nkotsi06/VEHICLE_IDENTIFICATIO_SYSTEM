package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToServiceRecord(results.get(0));
    }

    @Override
    public List<ServiceRecord> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_service_records");
        return mapMapsToServiceRecords(results);
    }

    public List<ServiceRecord> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "vehicle_id = ? ORDER BY service_date DESC", vehicleId);
        return mapMapsToServiceRecords(results);
    }

    public List<ServiceRecord> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "workshop_id = ? ORDER BY service_date DESC", workshopId);
        return mapMapsToServiceRecords(results);
    }

    public List<ServiceRecord> findRecentByWorkshopId(int workshopId, int limit) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "workshop_id = ? ORDER BY service_date DESC LIMIT ?", workshopId, limit);
        return mapMapsToServiceRecords(results);
    }

    public List<ServiceRecord> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "service_date BETWEEN ? AND ? ORDER BY service_date DESC", startDate, endDate);
        return mapMapsToServiceRecords(results);
    }

    public List<ServiceRecord> findByServiceType(String serviceType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "service_type ILIKE ? ORDER BY service_date DESC", "%" + serviceType + "%");
        return mapMapsToServiceRecords(results);
    }

    public List<ServiceRecord> findByMechanicId(int mechanicId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_records", "mechanic_id = ? ORDER BY service_date DESC", mechanicId);
        return mapMapsToServiceRecords(results);
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
                entity.getServiceDate(),
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

    /**
     * Inserts a service record and returns the generated ID.
     *
     * @param entity the ServiceRecord entity to insert
     * @return the generated service record ID, or -1 if insertion failed
     * @throws SQLException if database error occurs
     */
    public int insertAndGetId(ServiceRecord entity) throws SQLException {
        Integer recordId = procedureCaller.executeAddServiceRecord(
                entity.getVehicleId(),
                entity.getWorkshopId(),
                entity.getMechanicId() > 0 ? entity.getMechanicId() : null,
                entity.getServiceDate(),
                entity.getServiceType(),
                entity.getDescription(),
                entity.getCost(),
                entity.getOdometerReading()
        );
        if (recordId != null && recordId > 0) {
            entity.setId(recordId);
            return recordId;
        }
        return -1;
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

    /**
     * Converts a List of Maps to a List of ServiceRecord objects.
     */
    private List<ServiceRecord> mapMapsToServiceRecords(List<Map<String, Object>> maps) {
        List<ServiceRecord> records = new ArrayList<>();
        if (maps == null) {
            return records;
        }
        for (Map<String, Object> map : maps) {
            ServiceRecord record = mapMapToServiceRecord(map);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    /**
     * Converts a Map to a ServiceRecord object.
     */
    private ServiceRecord mapMapToServiceRecord(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ServiceRecord record = new ServiceRecord();

        record.setId(getIntValue(map, "id"));
        record.setVehicleId(getIntValue(map, "vehicle_id"));
        record.setRegistrationNumber(getStringValue(map, "registration_number"));
        record.setMake(getStringValue(map, "make"));
        record.setModel(getStringValue(map, "model"));
        record.setWorkshopId(getIntValue(map, "workshop_id"));
        record.setWorkshopName(getStringValue(map, "workshop_name"));
        record.setMechanicId(getIntValue(map, "mechanic_id"));
        record.setMechanicName(getStringValue(map, "mechanic_name"));
        record.setServiceType(getStringValue(map, "service_type"));
        record.setDescription(getStringValue(map, "description"));
        record.setCost(getDoubleValue(map, "cost"));
        record.setOdometerReading(getIntValue(map, "odometer_reading"));
        record.setStatus(getStringValue(map, "status"));

        record.setServiceDate(getLocalDateValue(map, "service_date"));
        record.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        record.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return record;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate) value;
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
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