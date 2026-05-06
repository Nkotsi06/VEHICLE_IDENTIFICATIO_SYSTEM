package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.ServiceSchedule;

/**
 * ServiceScheduleDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ServiceScheduleDAO extends BaseDAO<ServiceSchedule> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ServiceScheduleDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public ServiceSchedule findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_schedules", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToServiceSchedule(results.get(0));
    }

    @Override
    public List<ServiceSchedule> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_service_schedules");
        return mapMapsToServiceSchedules(results);
    }

    public List<ServiceSchedule> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_schedules", "vehicle_id = ? ORDER BY due_date", vehicleId);
        return mapMapsToServiceSchedules(results);
    }

    public List<ServiceSchedule> findDueReminders() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_service_reminder");
        return mapMapsToServiceSchedules(results);
    }

    public List<ServiceSchedule> findOverdueSchedules() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_service_schedules", "due_date < CURRENT_DATE AND reminder_sent = false ORDER BY due_date");
        return mapMapsToServiceSchedules(results);
    }

    public int countDueByCustomerId(int customerId, int daysThreshold) throws SQLException {
        // You'll need to implement this method in ViewLoader or use a different approach
        // For now, let's query and count manually
        List<Map<String, Object>> allSchedules = viewLoader.loadView("vw_service_schedules");
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        int count = 0;

        for (Map<String, Object> row : allSchedules) {
            Integer vehicleCustomerId = (Integer) row.get("customer_id");
            Object dueDateObj = row.get("due_date");

            if (vehicleCustomerId != null && vehicleCustomerId == customerId && dueDateObj != null) {
                LocalDate dueDate = null;
                if (dueDateObj instanceof java.sql.Date) {
                    dueDate = ((java.sql.Date) dueDateObj).toLocalDate();
                } else if (dueDateObj instanceof LocalDate) {
                    dueDate = (LocalDate) dueDateObj;
                }

                if (dueDate != null && !dueDate.isAfter(thresholdDate)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks if a vehicle has any upcoming service reminders.
     *
     * @param vehicleId the vehicle ID to check
     * @return true if the vehicle has upcoming reminders (due or overdue), false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean hasUpcomingReminders(int vehicleId) throws SQLException {
        try {
            List<ServiceSchedule> schedules = findByVehicleId(vehicleId);
            if (schedules == null || schedules.isEmpty()) {
                return false;
            }

            LocalDate today = LocalDate.now();
            for (ServiceSchedule schedule : schedules) {
                if (!schedule.isReminderSent() && schedule.getDueDate() != null) {
                    if (!schedule.getDueDate().isBefore(today.minusMonths(1))) {
                        // Has upcoming or recent due date
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new SQLException("Error checking upcoming reminders for vehicle: " + vehicleId, e);
        }
    }

    public void sendReminders() throws SQLException {
        procedureCaller.executeSendServiceReminders();
    }

    @Override
    public boolean insert(ServiceSchedule entity) throws SQLException {
        return procedureCaller.executeInsertServiceSchedule(
                entity.getVehicleId(),
                entity.getServiceType(),
                entity.getDueDate(),
                entity.getDueOdometer(),
                entity.getLastServiceDate(),
                entity.getLastServiceOdometer()
        );
    }

    @Override
    public boolean update(ServiceSchedule entity) throws SQLException {
        return procedureCaller.executeUpdateServiceSchedule(
                entity.getId(),
                entity.getDueDate(),
                entity.getDueOdometer(),
                entity.isReminderSent(),
                entity.getReminderSentDate()
        );
    }

    public boolean markReminderSent(int scheduleId) throws SQLException {
        return procedureCaller.executeMarkServiceReminderSent(scheduleId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteServiceSchedule(id);
    }

    /**
     * Converts a List of Maps to a List of ServiceSchedule objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of ServiceSchedule objects
     */
    private List<ServiceSchedule> mapMapsToServiceSchedules(List<Map<String, Object>> maps) {
        List<ServiceSchedule> schedules = new ArrayList<>();
        if (maps == null) {
            return schedules;
        }
        for (Map<String, Object> map : maps) {
            ServiceSchedule schedule = mapMapToServiceSchedule(map);
            if (schedule != null) {
                schedules.add(schedule);
            }
        }
        return schedules;
    }

    /**
     * Converts a Map to a ServiceSchedule object.
     *
     * @param map the map from the view loader
     * @return ServiceSchedule object
     */
    private ServiceSchedule mapMapToServiceSchedule(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ServiceSchedule schedule = new ServiceSchedule();

        schedule.setId(getIntValue(map, "id"));
        schedule.setVehicleId(getIntValue(map, "vehicle_id"));
        schedule.setRegistrationNumber(getStringValue(map, "registration_number"));
        schedule.setCustomerId(getIntValue(map, "customer_id"));
        schedule.setCustomerEmail(getStringValue(map, "customer_email"));
        schedule.setServiceType(getStringValue(map, "service_type"));

        schedule.setDueDate(getLocalDateValue(map, "due_date"));
        schedule.setDueOdometer(getIntValue(map, "due_odometer"));
        schedule.setLastServiceDate(getLocalDateValue(map, "last_service_date"));
        schedule.setLastServiceOdometer(getIntValue(map, "last_service_odometer"));

        schedule.setReminderSent(getBooleanValue(map, "reminder_sent"));
        schedule.setReminderSentDate(getLocalDateValue(map, "reminder_sent_date"));

        schedule.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        schedule.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return schedule;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get Boolean values from Map.
     */
    private boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return false;
    }

    /**
     * Helper method to safely get LocalDate values from Map.
     */
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private java.time.LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) value;
        }
        return null;
    }

    @Override
    protected ServiceSchedule mapRow(ResultSet rs) throws SQLException {
        ServiceSchedule schedule = new ServiceSchedule();
        schedule.setId(rs.getInt("id"));
        schedule.setVehicleId(rs.getInt("vehicle_id"));

        try {
            schedule.setRegistrationNumber(rs.getString("registration_number"));
        } catch (SQLException e) {}

        try {
            schedule.setCustomerId(rs.getInt("customer_id"));
        } catch (SQLException e) {}

        try {
            schedule.setCustomerEmail(rs.getString("customer_email"));
        } catch (SQLException e) {}

        schedule.setServiceType(rs.getString("service_type"));

        if (rs.getDate("due_date") != null) {
            schedule.setDueDate(rs.getDate("due_date").toLocalDate());
        }
        if (rs.getObject("due_odometer") != null) {
            schedule.setDueOdometer(rs.getInt("due_odometer"));
        }
        if (rs.getDate("last_service_date") != null) {
            schedule.setLastServiceDate(rs.getDate("last_service_date").toLocalDate());
        }
        if (rs.getObject("last_service_odometer") != null) {
            schedule.setLastServiceOdometer(rs.getInt("last_service_odometer"));
        }
        schedule.setReminderSent(rs.getBoolean("reminder_sent"));
        if (rs.getDate("reminder_sent_date") != null) {
            schedule.setReminderSentDate(rs.getDate("reminder_sent_date").toLocalDate());
        }

        if (rs.getTimestamp("created_at") != null) {
            schedule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            schedule.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return schedule;
    }
}