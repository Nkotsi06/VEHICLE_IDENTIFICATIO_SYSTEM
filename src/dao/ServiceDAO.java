package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;  // ADDED MISSING IMPORT

import database.ProcedureCaller;
import database.ViewLoader;
import models.Mechanic;
import models.ServiceRecord;
import models.ServiceSchedule;
import models.Workshop;

/**
 * ServiceDAO - Facade that uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ServiceDAO extends BaseDAO<ServiceRecord> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final ServiceRecordDAO recordDAO;
    private final MechanicDAO mechanicDAO;
    private final WorkshopDAO workshopDAO;
    private final ServiceScheduleDAO scheduleDAO;

    public ServiceDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.recordDAO = new ServiceRecordDAO();
        this.mechanicDAO = new MechanicDAO();
        this.workshopDAO = new WorkshopDAO();
        this.scheduleDAO = new ServiceScheduleDAO();
    }

    @Override
    public ServiceRecord findById(int id) throws SQLException {
        return recordDAO.findById(id);
    }

    @Override
    public List<ServiceRecord> findAll() throws SQLException {
        return recordDAO.findAll();
    }

    public List<ServiceRecord> findByVehicleId(int vehicleId) throws SQLException {
        return recordDAO.findByVehicleId(vehicleId);
    }

    public List<ServiceRecord> findByWorkshopId(int workshopId) throws SQLException {
        return recordDAO.findByWorkshopId(workshopId);
    }

    public List<ServiceRecord> findByMechanicId(int mechanicId) throws SQLException {
        return recordDAO.findByMechanicId(mechanicId);
    }

    public List<ServiceRecord> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return recordDAO.findByDateRange(startDate, endDate);
    }

    public List<ServiceRecord> findByServiceType(String serviceType) throws SQLException {
        return recordDAO.findByServiceType(serviceType);
    }

    @Override
    public boolean insert(ServiceRecord entity) throws SQLException {
        return recordDAO.insert(entity);
    }

    public int insertAndGetId(ServiceRecord entity) throws SQLException {
        return recordDAO.insertAndGetId(entity);
    }

    @Override
    public boolean update(ServiceRecord entity) throws SQLException {
        return recordDAO.update(entity);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return recordDAO.delete(id);
    }

    public List<Mechanic> getMechanicsByWorkshop(int workshopId) throws SQLException {
        return mechanicDAO.findByWorkshopId(workshopId);
    }

    public Mechanic getMechanicById(int mechanicId) throws SQLException {
        return mechanicDAO.findById(mechanicId);
    }

    public boolean addMechanic(int workshopId, String name, String specialization, String phone) throws SQLException {
        Mechanic mechanic = new Mechanic(workshopId, name, specialization, phone);
        return mechanicDAO.insert(mechanic);
    }

    public boolean updateMechanic(Mechanic mechanic) throws SQLException {
        return mechanicDAO.update(mechanic);
    }

    public boolean deleteMechanic(int mechanicId) throws SQLException {
        return mechanicDAO.delete(mechanicId);
    }

    public Workshop getWorkshopById(int workshopId) throws SQLException {
        return workshopDAO.findById(workshopId);
    }

    public List<Workshop> getAllWorkshops() throws SQLException {
        return workshopDAO.findAll();
    }

    public List<Workshop> getApprovedWorkshops() throws SQLException {
        return workshopDAO.findApprovedWorkshops();
    }

    public List<ServiceSchedule> getServiceSchedulesByVehicle(int vehicleId) throws SQLException {
        return scheduleDAO.findByVehicleId(vehicleId);
    }

    public List<ServiceSchedule> getDueServiceReminders() throws SQLException {
        return scheduleDAO.findDueReminders();
    }

    public boolean createServiceSchedule(int vehicleId, String serviceType, LocalDate dueDate, Integer dueOdometer) throws SQLException {
        ServiceSchedule schedule = new ServiceSchedule(vehicleId, serviceType, dueDate);
        schedule.setDueOdometer(dueOdometer);
        return scheduleDAO.insert(schedule);
    }

    public boolean markReminderSent(int scheduleId) throws SQLException {
        return scheduleDAO.markReminderSent(scheduleId);
    }

    public void sendServiceReminders() throws SQLException {
        scheduleDAO.sendReminders();
    }

    public double getTotalRevenueByWorkshop(int workshopId) throws SQLException {
        return recordDAO.getTotalRevenueByWorkshop(workshopId);
    }

    public List<WorkshopPerformance> getWorkshopPerformance() throws SQLException {
        List<WorkshopPerformance> performances = new ArrayList<>();
        List<Map<String, Object>> results = viewLoader.loadView("vw_workshop_performance");

        for (Map<String, Object> row : results) {
            WorkshopPerformance perf = new WorkshopPerformance();
            perf.workshopId = getIntValue(row, "workshop_id");
            perf.workshopName = getStringValue(row, "workshop_name");
            perf.serviceCount = getIntValue(row, "service_count");
            perf.totalRevenue = getDoubleValue(row, "total_revenue");
            perf.averageServiceCost = getDoubleValue(row, "average_service_cost");
            perf.averageRating = getDoubleValue(row, "avg_rating");
            performances.add(perf);
        }
        return performances;
    }

    public int estimateWaitTime(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wait_time_estimator", "workshop_id = ?", workshopId);
        if (results.isEmpty()) return 0;
        return (int) Math.ceil(getDoubleValue(results.get(0), "estimated_wait_hours"));
    }

    // Helper methods for safe type conversion
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

    public static class WorkshopPerformance {
        public int workshopId;
        public String workshopName;
        public int serviceCount;
        public double totalRevenue;
        public double averageServiceCost;
        public double averageRating;
    }

    public ServiceRecord mapRow(ResultSet rs) throws SQLException {
        return recordDAO.mapRow(rs);
    }
}