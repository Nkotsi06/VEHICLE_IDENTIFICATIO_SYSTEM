package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PoliceUnit;

/**
 * PoliceUnitDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PoliceUnitDAO extends BaseDAO<PoliceUnit> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PoliceUnitDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PoliceUnit findById(int id) throws SQLException {
        List<PoliceUnit> results = viewLoader.loadViewWithCondition("vw_police_units", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public PoliceUnit findByUnitId(String unitId) throws SQLException {
        List<PoliceUnit> results = viewLoader.loadViewWithCondition("vw_police_units", "unit_id = ?", unitId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<PoliceUnit> findAll() throws SQLException {
        return viewLoader.loadView("vw_police_units");
    }

    public List<PoliceUnit> findByStatus(String status) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_units", "status = ? ORDER BY officer_name", status);
    }

    public List<PoliceUnit> findAvailableUnits() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_units", "status IN ('AVAILABLE', 'ON_PATROL') ORDER BY officer_name");
    }

    @Override
    public boolean insert(PoliceUnit entity) throws SQLException {
        return procedureCaller.executeRegisterPoliceUnit(
                entity.getUnitId(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getDeviceId()
        );
    }

    public boolean updateLocation(String unitId, double latitude, double longitude) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnitLocation(unitId, latitude, longitude);
    }

    public boolean updateStatus(int unitId, String status) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnitStatus(unitId, status);
    }

    @Override
    public boolean update(PoliceUnit entity) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnit(
                entity.getId(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getStatus(),
                entity.getDeviceId()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePoliceUnit(id);
    }

    @Override
    protected PoliceUnit mapRow(ResultSet rs) throws SQLException {
        PoliceUnit unit = new PoliceUnit();
        unit.setId(rs.getInt("id"));
        unit.setUnitId(rs.getString("unit_id"));
        unit.setOfficerName(rs.getString("officer_name"));
        unit.setBadgeNumber(rs.getString("badge_number"));

        if (rs.getObject("current_location_lat") != null) {
            unit.setCurrentLocationLat(rs.getDouble("current_location_lat"));
        }
        if (rs.getObject("current_location_lng") != null) {
            unit.setCurrentLocationLng(rs.getDouble("current_location_lng"));
        }
        if (rs.getTimestamp("last_location_update") != null) {
            unit.setLastLocationUpdate(rs.getTimestamp("last_location_update").toLocalDateTime());
        }
        unit.setStatus(rs.getString("status"));
        unit.setDeviceId(rs.getString("device_id"));

        if (rs.getTimestamp("created_at") != null) {
            unit.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            unit.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return unit;
    }
}