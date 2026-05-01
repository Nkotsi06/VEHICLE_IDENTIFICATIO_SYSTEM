package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.ServiceSchedule;

public class ServiceScheduleDAO extends BaseDAO<ServiceSchedule> {

    @Override
    public ServiceSchedule findById(int id) throws SQLException {
        String sql = "SELECT * FROM service_schedules WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<ServiceSchedule> findAll() throws SQLException {
        String sql = "SELECT * FROM service_schedules ORDER BY due_date";
        return executeQuery(sql);
    }

    public List<ServiceSchedule> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM service_schedules WHERE vehicle_id = ? ORDER BY due_date";
        return executeQuery(sql, vehicleId);
    }

    public List<ServiceSchedule> findDueReminders() throws SQLException {
        String sql = "SELECT * FROM vw_service_reminder WHERE reminder_sent = false ORDER BY due_date";
        return executeQuery(sql);
    }

    public List<ServiceSchedule> findOverdueSchedules() throws SQLException {
        String sql = "SELECT * FROM service_schedules WHERE due_date < CURRENT_DATE AND reminder_sent = false ORDER BY due_date";
        return executeQuery(sql);
    }

    // Count services due by customer ID
    public int countDueByCustomerId(int customerId, int daysThreshold) throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_schedules ss " +
                "JOIN vehicles v ON ss.vehicle_id = v.id " +
                "JOIN customers c ON v.owner_id = c.id " +
                "WHERE c.id = ? AND ss.due_date <= CURRENT_DATE + INTERVAL '" + daysThreshold + " days'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public void sendReminders() throws SQLException {
        String sql = "CALL sp_send_service_reminders()";
        executeUpdate(sql);
    }

    @Override
    public boolean insert(ServiceSchedule entity) throws SQLException {
        String sql = "INSERT INTO service_schedules (vehicle_id, service_type, due_date, due_odometer, last_service_date, last_service_odometer) VALUES (?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getVehicleId(),
                entity.getServiceType(),
                entity.getDueDate(),
                entity.getDueOdometer(),
                entity.getLastServiceDate(),
                entity.getLastServiceOdometer()
        );
        return result > 0;
    }

    @Override
    public boolean update(ServiceSchedule entity) throws SQLException {
        String sql = "UPDATE service_schedules SET due_date = ?, due_odometer = ?, reminder_sent = ?, reminder_sent_date = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getDueDate(),
                entity.getDueOdometer(),
                entity.isReminderSent(),
                entity.getReminderSentDate(),
                entity.getId()
        );
        return result > 0;
    }

    public boolean markReminderSent(int scheduleId) throws SQLException {
        String sql = "UPDATE service_schedules SET reminder_sent = true, reminder_sent_date = CURRENT_DATE WHERE id = ?";
        int result = executeUpdate(sql, scheduleId);
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM service_schedules WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected ServiceSchedule mapRow(ResultSet rs) throws SQLException {
        ServiceSchedule schedule = new ServiceSchedule();
        schedule.setId(rs.getInt("id"));
        schedule.setVehicleId(rs.getInt("vehicle_id"));

        try {
            schedule.setRegistrationNumber(rs.getString("registration_number"));
        } catch (SQLException e) {
            // Column may not exist - ignore
        }

        try {
            schedule.setCustomerId(rs.getInt("customer_id"));
        } catch (SQLException e) {
            // Column may not exist - ignore
        }

        try {
            schedule.setCustomerEmail(rs.getString("customer_email"));
        } catch (SQLException e) {
            // Column may not exist - ignore
        }

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