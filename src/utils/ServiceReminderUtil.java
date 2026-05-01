package utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dao.ServiceScheduleDAO;
import models.ServiceSchedule;

public class ServiceReminderUtil {

    private static ServiceReminderUtil instance;
    private ServiceScheduleDAO scheduleDAO;

    private ServiceReminderUtil() {
        this.scheduleDAO = new ServiceScheduleDAO();
    }

    public static synchronized ServiceReminderUtil getInstance() {
        if (instance == null) {
            instance = new ServiceReminderUtil();
        }
        return instance;
    }

    public List<ServiceSchedule> getDueReminders() {
        try {
            return scheduleDAO.findDueReminders();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ServiceSchedule> getOverdueReminders() {
        try {
            return scheduleDAO.findOverdueSchedules();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void sendReminders() {
        try {
            scheduleDAO.sendReminders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LocalDate calculateNextServiceDate(LocalDate lastServiceDate, int monthsInterval) {
        if (lastServiceDate == null) return LocalDate.now();
        return lastServiceDate.plusMonths(monthsInterval);
    }

    public int calculateNextServiceOdometer(int lastOdometer, int kmInterval) {
        return lastOdometer + kmInterval;
    }

    public String getReminderStatus(ServiceSchedule schedule) {
        if (schedule.isOverdue()) return "OVERDUE";
        if (schedule.isDueSoon()) return "DUE_SOON";
        return "FUTURE";
    }
}