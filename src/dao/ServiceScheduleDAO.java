package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        List<ServiceSchedule> results = viewLoader.loadViewWithCondition("vw_service_schedules", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ServiceSchedule> findAll() throws SQLException {
        return viewLoader.loadView("vw_service_schedules");
    }

    public List<ServiceSchedule> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_schedules", "vehicle_id = ? ORDER BY due_date", vehicleId);
    }

    public List<ServiceSchedule> findDueReminders() throws SQLException {
        return viewLoader.loadView("vw_service_reminder");
    }

    public List<ServiceSchedule> findOverdueSchedules() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_service_schedules", "due_date < CURRENT_DATE AND reminder_sent = false ORDER BY due_date");
    }

    public int countDueByCustomerId(int customerId, int daysThreshold) throws SQLException {
        return viewLoader.countDueServiceRemindersByCustomer(customerId, daysThreshold);
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