package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.GeofenceAlertEvent;
import models.GeofenceZone;

/**
 * GeofenceDAO - Facade that uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class GeofenceDAO extends BaseDAO<GeofenceZone> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final GeofenceZoneDAO zoneDAO;
    private final GeofenceAlertEventDAO eventDAO;

    public GeofenceDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.zoneDAO = new GeofenceZoneDAO();
        this.eventDAO = new GeofenceAlertEventDAO();
    }

    @Override
    public GeofenceZone findById(int id) throws SQLException {
        return zoneDAO.findById(id);
    }

    @Override
    public List<GeofenceZone> findAll() throws SQLException {
        return zoneDAO.findAll();
    }

    public List<GeofenceZone> findActiveZones() throws SQLException {
        return zoneDAO.findActiveZones();
    }

    public List<GeofenceZone> findByZoneType(String zoneType) throws SQLException {
        return zoneDAO.findByZoneType(zoneType);
    }

    @Override
    public boolean insert(GeofenceZone entity) throws SQLException {
        return zoneDAO.insert(entity);
    }

    @Override
    public boolean update(GeofenceZone entity) throws SQLException {
        return zoneDAO.update(entity);
    }

    public boolean deactivateZone(int zoneId) throws SQLException {
        return zoneDAO.deactivateZone(zoneId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return zoneDAO.delete(id);
    }

    // Methods that return GeofenceAlertEvent
    public List<GeofenceAlertEvent> getAlertEvents() throws SQLException {
        return eventDAO.findAll();
    }

    public List<GeofenceAlertEvent> getAlertEventsByVehicle(int vehicleId) throws SQLException {
        return eventDAO.findByVehicleId(vehicleId);
    }

    public List<GeofenceAlertEvent> getAlertEventsByZone(int zoneId) throws SQLException {
        return eventDAO.findByZoneId(zoneId);
    }

    public List<GeofenceAlertEvent> getUnnotifiedAlerts() throws SQLException {
        return eventDAO.findUnnotifiedAlerts();
    }

    public boolean sendGeofenceAlert(int vehicleId, int zoneId, String alertType) throws SQLException {
        return procedureCaller.executeSendGeofenceAlert(vehicleId, zoneId, alertType);
    }

    public boolean markAlertAsNotified(int eventId) throws SQLException {
        return eventDAO.markAsNotified(eventId);
    }

    public boolean isVehicleInZone(int vehicleId, int zoneId) throws SQLException {
        // Use view - NO direct SQL
        List<GeofenceZone> zones = zoneDAO.findActiveZones();
        for (GeofenceZone zone : zones) {
            if (zone.getId() == zoneId) {
                // Check if vehicle location is within zone using stored function
                return procedureCaller.executeIsPointInZone(vehicleId, zoneId);
            }
        }
        return false;
    }

    public List<GeofenceZone> findZonesContainingPoint(double latitude, double longitude) throws SQLException {
        // Use stored procedure - NO direct SQL
        return zoneDAO.findZonesContainingPoint(latitude, longitude);
    }

    public int countActiveZones() throws SQLException {
        return zoneDAO.countActiveZones();
    }

    public int countAlertEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return eventDAO.countByDateRange(startDate, endDate);
    }

    @Override
    protected GeofenceZone mapRow(ResultSet rs) throws SQLException {
        return zoneDAO.mapRow(rs);
    }
}