package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.Mechanic;
import models.ServiceRecord;
import models.ServiceSchedule;
import models.Workshop;

public class ServiceDAO extends BaseDAO<ServiceRecord> {

    private ServiceRecordDAO recordDAO = new ServiceRecordDAO();
    private MechanicDAO mechanicDAO = new MechanicDAO();
    private WorkshopDAO workshopDAO = new WorkshopDAO();
    private ServiceScheduleDAO scheduleDAO = new ServiceScheduleDAO();

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
        String sql = "CALL sp_add_service_record(?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_add_service_record(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setInt(1, entity.getVehicleId());
            cs.setInt(2, entity.getWorkshopId());
            if (entity.getMechanicId() > 0) {
                cs.setInt(3, entity.getMechanicId());
            } else {
                cs.setNull(3, Types.INTEGER);
            }
            cs.setDate(4, java.sql.Date.valueOf(entity.getServiceDate()));
            cs.setString(5, entity.getServiceType());
            cs.setString(6, entity.getDescription());
            cs.setDouble(7, entity.getCost());
            cs.setInt(8, entity.getOdometerReading());
            cs.execute();

            String querySql = "SELECT id FROM service_records WHERE vehicle_id = ? ORDER BY service_date DESC LIMIT 1";
            ps = conn.prepareStatement(querySql);
            ps.setInt(1, entity.getVehicleId());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (cs != null) cs.close();
            if (conn != null) conn.close();
        }
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
        String sql = "SELECT * FROM vw_workshop_performance ORDER BY total_revenue DESC";
        List<WorkshopPerformance> performances = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                WorkshopPerformance perf = new WorkshopPerformance();
                perf.workshopId = rs.getInt("workshop_id");
                perf.workshopName = rs.getString("workshop_name");
                perf.serviceCount = rs.getInt("service_count");
                perf.totalRevenue = rs.getDouble("total_revenue");
                perf.averageServiceCost = rs.getDouble("average_service_cost");
                perf.averageRating = rs.getDouble("avg_rating");
                performances.add(perf);
            }

            return performances;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int estimateWaitTime(int workshopId) throws SQLException {
        String sql = "SELECT estimated_wait_hours FROM vw_wait_time_estimator WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return (int) Math.ceil(rs.getDouble("estimated_wait_hours"));
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public static class WorkshopPerformance {
        public int workshopId;
        public String workshopName;
        public int serviceCount;
        public double totalRevenue;
        public double averageServiceCost;
        public double averageRating;
    }

    // REMOVE THE @Override ANNOTATION - this method does NOT override a superclass method
    // The BaseDAO class does not have an abstract mapRow method
    public ServiceRecord mapRow(ResultSet rs) throws SQLException {
        return recordDAO.mapRow(rs);
    }
}