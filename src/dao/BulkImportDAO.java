package dao;

import java.sql.*;
import java.util.List;
import java.util.Map;
import models.Customer;
import models.User;
import models.Vehicle;

public class BulkImportDAO extends BaseDAO<Object> {

    @Override
    public Object findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Object> findAll() throws SQLException {
        return null;
    }

    public int bulkImportCustomers(List<Customer> customers, List<User> users) throws SQLException {
        Connection conn = null;
        int importedCount = 0;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (int i = 0; i < customers.size(); i++) {
                Customer customer = customers.get(i);
                User user = users.get(i);

                Integer userId = executeProcedureWithOutParameter("sp_create_user", Types.INTEGER,
                        user.getUsername(),
                        user.getPassword(),
                        "CUSTOMER",
                        user.getFullName(),
                        user.getEmail()
                );

                if (userId != null && userId > 0) {
                    executeProcedure("sp_create_customer", userId, customer.getName(), customer.getAddress(),
                            customer.getPhone(), customer.getNationalId(), customer.getDriversLicenseNumber());
                    importedCount++;
                }
            }

            conn.commit();
            return importedCount;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public int bulkImportVehicles(List<Vehicle> vehicles) throws SQLException {
        Connection conn = null;
        int importedCount = 0;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (Vehicle vehicle : vehicles) {
                Integer vehicleId = executeProcedureWithOutParameter("sp_register_vehicle", Types.INTEGER,
                        vehicle.getRegistrationNumber(),
                        vehicle.getMake(),
                        vehicle.getModel(),
                        vehicle.getYear(),
                        vehicle.getOwnerId(),
                        vehicle.getStatusId() > 0 ? vehicle.getStatusId() : 1,
                        vehicle.getColor(),
                        vehicle.getEngineNumber(),
                        vehicle.getChassisNumber()
                );
                if (vehicleId != null && vehicleId > 0) {
                    importedCount++;
                }
            }

            conn.commit();
            return importedCount;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public int bulkImportFromJson(String jsonData) throws SQLException {
        String sql = "CALL sp_bulk_import_customers(?)";
        int result = executeUpdate(sql, jsonData);
        return result;
    }

    @Override
    public boolean insert(Object entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(Object entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    // Required mapRow method implementation for BaseDAO
    @Override
    protected Object mapRow(ResultSet rs) throws SQLException {
        // This DAO doesn't map to a single entity - it returns counts for bulk operations
        // Returning null is acceptable since this DAO is not used for standard entity operations
        return null;
    }
}