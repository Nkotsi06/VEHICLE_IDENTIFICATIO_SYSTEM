package dao;

import java.sql.Connection;
import java.sql.ResultSet;  // ADD THIS IMPORT
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import models.Customer;
import models.User;
import models.Vehicle;

/**
 * BulkImportDAO - Uses ONLY stored procedures for bulk operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class BulkImportDAO extends BaseDAO<Object> {

    private final ProcedureCaller procedureCaller;

    public BulkImportDAO() {
        this.procedureCaller = new ProcedureCaller();
    }

    /**
     * Bulk import customers using stored procedures in a transaction.
     *
     * @param customers list of customers to import
     * @param users list of users to import
     * @return number of successfully imported customers
     * @throws SQLException if database error occurs
     */
    public int bulkImportCustomers(List<Customer> customers, List<User> users) throws SQLException {
        if (customers == null || users == null || customers.size() != users.size()) {
            throw new IllegalArgumentException("Customers and users lists must be non-null and same size");
        }

        int importedCount = 0;

        try {
            dbConnection.beginTransaction();

            for (int i = 0; i < customers.size(); i++) {
                Customer customer = customers.get(i);
                User user = users.get(i);

                // Use stored procedure - NO direct SQL
                Integer userId = procedureCaller.executeCreateUserWithId(
                        user.getUsername(),
                        user.getPassword(),
                        "CUSTOMER",
                        user.getFullName(),
                        user.getEmail()
                );

                if (userId != null && userId > 0) {
                    // Use stored procedure - NO direct SQL
                    Integer customerId = procedureCaller.executeCreateCustomer(
                            userId,
                            customer.getName(),
                            customer.getAddress(),
                            customer.getPhone(),
                            customer.getNationalId(),
                            customer.getDriversLicenseNumber()
                    );
                    if (customerId != null && customerId > 0) {
                        importedCount++;
                    }
                }
            }

            dbConnection.commitTransaction();
            return importedCount;

        } catch (SQLException e) {
            dbConnection.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Bulk import vehicles using stored procedures in a transaction.
     *
     * @param vehicles list of vehicles to import
     * @return number of successfully imported vehicles
     * @throws SQLException if database error occurs
     */
    public int bulkImportVehicles(List<Vehicle> vehicles) throws SQLException {
        if (vehicles == null || vehicles.isEmpty()) {
            return 0;
        }

        int importedCount = 0;

        try {
            dbConnection.beginTransaction();

            for (Vehicle vehicle : vehicles) {
                // Use stored procedure - NO direct SQL
                Integer vehicleId = procedureCaller.executeRegisterVehicle(
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

            dbConnection.commitTransaction();
            return importedCount;

        } catch (SQLException e) {
            dbConnection.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Bulk import from JSON using stored procedure.
     *
     * @param jsonData JSON string containing customer data
     * @return number of imported records
     * @throws SQLException if database error occurs
     */
    public int bulkImportFromJson(String jsonData) throws SQLException {
        // Use stored procedure - NO direct SQL
        // Note: This method expects a stored procedure named 'bulk_import_customers'
        // that accepts a JSON string parameter and returns the number of imported records
        return procedureCaller.executeBulkImportCustomers(jsonData);
    }

    @Override
    public Object findById(int id) throws SQLException {
        // BulkImportDAO doesn't support single entity retrieval
        throw new UnsupportedOperationException("BulkImportDAO does not support findById");
    }

    @Override
    public List<Object> findAll() throws SQLException {
        throw new UnsupportedOperationException("BulkImportDAO does not support findAll");
    }

    @Override
    public boolean insert(Object entity) throws SQLException {
        throw new UnsupportedOperationException("BulkImportDAO does not support single insert");
    }

    @Override
    public boolean update(Object entity) throws SQLException {
        throw new UnsupportedOperationException("BulkImportDAO does not support update");
    }

    @Override
    public boolean delete(int id) throws SQLException {
        throw new UnsupportedOperationException("BulkImportDAO does not support delete");
    }

    @Override
    protected Object mapRow(ResultSet rs) throws SQLException {
        // This DAO doesn't map to a single entity
        // Return null as this method is not used for bulk operations
        return null;
    }
}