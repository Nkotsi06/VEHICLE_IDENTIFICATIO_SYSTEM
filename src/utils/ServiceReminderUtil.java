package utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.ServiceScheduleDAO;
import models.ServiceSchedule;

/**
 * Utility class for managing vehicle service reminders.
 * Handles due reminder detection, overdue tracking, and notification sending.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ServiceReminderUtil {

    private static final Logger LOGGER = Logger.getLogger(ServiceReminderUtil.class.getName());
    private static ServiceReminderUtil instance;
    private ServiceScheduleDAO scheduleDAO;

    /**
     * Private constructor for singleton pattern.
     */
    private ServiceReminderUtil() {
        try {
            this.scheduleDAO = new ServiceScheduleDAO();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize ServiceScheduleDAO", e);
        }
    }

    /**
     * Gets the singleton instance of ServiceReminderUtil.
     *
     * @return the ServiceReminderUtil instance
     */
    public static synchronized ServiceReminderUtil getInstance() {
        if (instance == null) {
            instance = new ServiceReminderUtil();
        }
        return instance;
    }

    /**
     * Gets all due reminders (service due today or soon).
     *
     * @return list of due service schedules
     */
    public List<ServiceSchedule> getDueReminders() {
        try {
            if (scheduleDAO == null) {
                LOGGER.warning("ServiceScheduleDAO is null, cannot get due reminders");
                return new ArrayList<>();
            }
            return scheduleDAO.findDueReminders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get due reminders", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all overdue reminders.
     *
     * @return list of overdue service schedules
     */
    public List<ServiceSchedule> getOverdueReminders() {
        try {
            if (scheduleDAO == null) {
                LOGGER.warning("ServiceScheduleDAO is null, cannot get overdue reminders");
                return new ArrayList<>();
            }
            return scheduleDAO.findOverdueSchedules();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get overdue reminders", e);
            return new ArrayList<>();
        }
    }

    /**
     * Sends reminders to customers.
     */
    public void sendReminders() {
        try {
            if (scheduleDAO == null) {
                LOGGER.warning("ServiceScheduleDAO is null, cannot send reminders");
                return;
            }
            scheduleDAO.sendReminders();
            LOGGER.info("Service reminders sent successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send reminders", e);
        }
    }

    /**
     * Calculates the next service date based on last service date and interval.
     *
     * @param lastServiceDate the date of last service
     * @param monthsInterval  the interval in months
     * @return next service date
     */
    public LocalDate calculateNextServiceDate(LocalDate lastServiceDate, int monthsInterval) {
        if (lastServiceDate == null) {
            return LocalDate.now();
        }

        if (monthsInterval <= 0) {
            monthsInterval = 6; // Default to 6 months
        }

        return lastServiceDate.plusMonths(monthsInterval);
    }

    /**
     * Calculates the next service odometer reading.
     *
     * @param lastOdometer the last odometer reading
     * @param kmInterval   the interval in kilometers
     * @return next service odometer reading
     */
    public int calculateNextServiceOdometer(int lastOdometer, int kmInterval) {
        if (lastOdometer < 0) {
            lastOdometer = 0;
        }

        if (kmInterval <= 0) {
            kmInterval = 5000; // Default to 5000 km
        }

        return lastOdometer + kmInterval;
    }

    /**
     * Gets the reminder status for a service schedule.
     *
     * @param schedule the service schedule
     * @return status string: "OVERDUE", "DUE_SOON", or "FUTURE"
     */
    public String getReminderStatus(ServiceSchedule schedule) {
        if (schedule == null) {
            return "UNKNOWN";
        }

        try {
            if (schedule.isOverdue()) return "OVERDUE";
            if (schedule.isDueSoon()) return "DUE_SOON";
            return "FUTURE";
        } catch (Exception e) {
            LOGGER.warning("Error getting reminder status: " + e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Gets the CSS color class for a reminder status.
     *
     * @param schedule the service schedule
     * @return CSS color class name
     */
    public String getReminderColorClass(ServiceSchedule schedule) {
        String status = getReminderStatus(schedule);
        switch (status) {
            case "OVERDUE":
                return "danger";
            case "DUE_SOON":
                return "warning";
            default:
                return "success";
        }
    }

    /**
     * Gets the count of due reminders.
     *
     * @return number of due reminders
     */
    public int getDueRemindersCount() {
        return getDueReminders().size();
    }

    /**
     * Gets the count of overdue reminders.
     *
     * @return number of overdue reminders
     */
    public int getOverdueRemindersCount() {
        return getOverdueReminders().size();
    }

    /**
     * Checks if a vehicle has any upcoming service reminders.
     *
     * @param vehicleId the vehicle ID
     * @return true if vehicle has upcoming reminders, false otherwise
     */
    public boolean hasUpcomingReminders(int vehicleId) {
        try {
            if (scheduleDAO == null) return false;
            return scheduleDAO.hasUpcomingReminders(vehicleId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking upcoming reminders for vehicle " + vehicleId, e);
            return false;
        }
    }
}