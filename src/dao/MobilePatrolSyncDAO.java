package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PoliceUnit;

/**
 * MobilePatrolSyncDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class MobilePatrolSyncDAO extends BaseDAO<Object> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public MobilePatrolSyncDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    public List<Map<String, Object>> getMobilePatrolData(String unitId) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_mobile_patrol_sync", "unit_id = ?", unitId);
        return results;
    }

    public boolean syncPoliceUnitLocation(String unitId, double latitude, double longitude) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnitLocation(unitId, latitude, longitude);
    }

    public boolean queueSyncData(String unitId, String actionType, String actionData) throws SQLException {
        return procedureCaller.executeQueueMobilePatrolSync(unitId, actionType, actionData);
    }

    public List<Map<String, Object>> getPendingSyncData(String unitId) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_mobile_patrol_sync_queue", "unit_id = ? AND sync_status = 'PENDING' ORDER BY created_at", unitId);
    }

    public List<String> getPendingSyncItems(String unitId) throws SQLException {
        List<Map<String, Object>> pendingData = getPendingSyncData(unitId);
        List<String> results = new ArrayList<>();
        for (Map<String, Object> data : pendingData) {
            String item = data.get("action_type") + " - " + data.get("created_at");
            results.add(item);
        }
        return results;
    }

    public boolean markSyncCompleted(int syncId) throws SQLException {
        return procedureCaller.executeMarkMobilePatrolSyncCompleted(syncId);
    }

    public boolean registerPoliceUnit(String unitId, String officerName, String badgeNumber, String deviceId) throws SQLException {
        return procedureCaller.executeRegisterPoliceUnit(unitId, officerName, badgeNumber, deviceId);
    }

    public boolean syncUnitData(String unitId) throws SQLException {
        return procedureCaller.executeSyncMobilePatrolUnit(unitId);
    }

    public boolean sendBroadcastAlert(String unitId, String message) throws SQLException {
        return procedureCaller.executeSendBroadcastAlert(message);
    }

    public PoliceUnit getPoliceUnitByUnitId(String unitId) throws SQLException {
        List<PoliceUnit> results = viewLoader.loadViewWithCondition("vw_police_units", "unit_id = ?", unitId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Object findById(int id) throws SQLException {
        throw new UnsupportedOperationException("MobilePatrolSyncDAO does not support findById");
    }

    @Override
    public List<Object> findAll() throws SQLException {
        throw new UnsupportedOperationException("MobilePatrolSyncDAO does not support findAll");
    }

    @Override
    public boolean insert(Object entity) throws SQLException {
        throw new UnsupportedOperationException("MobilePatrolSyncDAO does not support single insert");
    }

    @Override
    public boolean update(Object entity) throws SQLException {
        throw new UnsupportedOperationException("MobilePatrolSyncDAO does not support update");
    }

    @Override
    public boolean delete(int id) throws SQLException {
        throw new UnsupportedOperationException("MobilePatrolSyncDAO does not support delete");
    }

    @Override
    protected Object mapRow(ResultSet rs) throws SQLException {
        return null;
    }
}