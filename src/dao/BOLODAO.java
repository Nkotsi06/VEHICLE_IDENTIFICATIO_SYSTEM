package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.BOLOAlert;

/**
 * BOLODAO - Legacy class. Use BOLOAlertDAO instead.
 *
 * @deprecated Use BOLOAlertDAO instead
 * @author Vehicle Identification System Team
 * @version 2.0
 */
@Deprecated
public class BOLODAO extends BaseDAO<BOLOAlert> {

    private final BOLOAlertDAO boloAlertDAO;

    public BOLODAO() {
        this.boloAlertDAO = new BOLOAlertDAO();
    }

    @Override
    public BOLOAlert findById(int id) throws SQLException {
        return boloAlertDAO.findById(id);
    }

    @Override
    public List<BOLOAlert> findAll() throws SQLException {
        return boloAlertDAO.findAll();
    }

    public List<BOLOAlert> findActiveAlerts() throws SQLException {
        return boloAlertDAO.findActiveAlerts();
    }

    public List<BOLOAlert> findByVehicleId(int vehicleId) throws SQLException {
        return boloAlertDAO.findByVehicleId(vehicleId);
    }

    @Override
    public boolean insert(BOLOAlert entity) throws SQLException {
        return boloAlertDAO.insert(entity);
    }

    public boolean cancelAlert(int alertId) throws SQLException {
        return boloAlertDAO.cancelAlert(alertId);
    }

    @Override
    public boolean update(BOLOAlert entity) throws SQLException {
        return boloAlertDAO.update(entity);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return boloAlertDAO.delete(id);
    }

    @Override
    protected BOLOAlert mapRow(ResultSet rs) throws SQLException {
        return boloAlertDAO.mapRow(rs);
    }
}