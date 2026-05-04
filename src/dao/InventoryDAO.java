package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InventoryAlert;
import models.PartInventory;

/**
 * InventoryDAO - Facade that uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InventoryDAO extends BaseDAO<PartInventory> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final PartInventoryDAO partDAO;
    private final InventoryAlertDAO alertDAO;

    public InventoryDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.partDAO = new PartInventoryDAO();
        this.alertDAO = new InventoryAlertDAO();
    }

    @Override
    public PartInventory findById(int id) throws SQLException {
        return partDAO.findById(id);
    }

    public PartInventory findByPartNumber(String partNumber) throws SQLException {
        return partDAO.findByPartNumber(partNumber);
    }

    @Override
    public List<PartInventory> findAll() throws SQLException {
        return partDAO.findAll();
    }

    public List<PartInventory> findByWorkshopId(int workshopId) throws SQLException {
        return partDAO.findByWorkshopId(workshopId);
    }

    public List<PartInventory> findLowStockItems() throws SQLException {
        return partDAO.findLowStockItems();
    }

    public List<PartInventory> findOutOfStockItems() throws SQLException {
        return partDAO.findOutOfStockItems();
    }

    @Override
    public boolean insert(PartInventory entity) throws SQLException {
        return partDAO.insert(entity);
    }

    public int insertAndGetId(PartInventory entity) throws SQLException {
        return partDAO.insertAndGetId(entity);
    }

    public boolean updateQuantity(int partId, int quantityChange) throws SQLException {
        return partDAO.updateQuantity(partId, quantityChange);
    }

    public boolean addStock(int partId, int quantity) throws SQLException {
        return updateQuantity(partId, quantity);
    }

    public boolean removeStock(int partId, int quantity) throws SQLException {
        return updateQuantity(partId, -quantity);
    }

    @Override
    public boolean update(PartInventory entity) throws SQLException {
        return partDAO.update(entity);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return partDAO.delete(id);
    }

    public List<InventoryAlert> getInventoryAlerts() throws SQLException {
        return alertDAO.findUnresolvedAlerts();
    }

    public List<InventoryAlert> getAlertsByPart(int partId) throws SQLException {
        return alertDAO.findByPartInventoryId(partId);
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        return alertDAO.resolveAlert(alertId);
    }

    public double getTotalInventoryValue(int workshopId) throws SQLException {
        return viewLoader.getSumInventoryValueByWorkshop(workshopId);
    }

    public boolean checkAndGenerateAlerts(int workshopId) throws SQLException {
        return procedureCaller.executeCheckInventoryAlerts(workshopId);
    }

    @Override
    protected PartInventory mapRow(ResultSet rs) throws SQLException {
        return partDAO.mapRow(rs);
    }
}