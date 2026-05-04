package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Customer;

/**
 * CustomerDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class CustomerDAO extends BaseDAO<Customer> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public CustomerDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Customer findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers", "id = ?", id);
        return results.isEmpty() ? null : mapToCustomer(results.get(0));
    }

    public Customer findByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers", "user_id = ?", userId);
        return results.isEmpty() ? null : mapToCustomer(results.get(0));
    }

    public Customer findByNationalId(String nationalId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers", "national_id = ?", nationalId);
        return results.isEmpty() ? null : mapToCustomer(results.get(0));
    }

    public Customer findByDriversLicense(String driversLicense) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers", "drivers_license_number = ?", driversLicense);
        return results.isEmpty() ? null : mapToCustomer(results.get(0));
    }

    public Customer findByPhone(String phone) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers", "phone = ?", phone);
        return results.isEmpty() ? null : mapToCustomer(results.get(0));
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_customers");
        return mapToCustomerList(results);
    }

    public List<Customer> findCustomersWithVehicles() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers", "vehicle_count > 0 ORDER BY name");
        return mapToCustomerList(results);
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        String searchPattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customers",
                "name ILIKE ? OR email ILIKE ? OR phone ILIKE ? ORDER BY name",
                searchPattern, searchPattern, searchPattern);
        return mapToCustomerList(results);
    }

    @Override
    public boolean insert(Customer entity) throws SQLException {
        Integer customerId = procedureCaller.executeCreateCustomer(
                entity.getUserId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getNationalId(),
                entity.getDriversLicenseNumber()
        );
        if (customerId != null && customerId > 0) {
            entity.setId(customerId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(Customer entity) throws SQLException {
        Integer customerId = procedureCaller.executeCreateCustomer(
                entity.getUserId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getNationalId(),
                entity.getDriversLicenseNumber()
        );
        if (customerId != null && customerId > 0) {
            entity.setId(customerId);
            return customerId;
        }
        return -1;
    }

    @Override
    public boolean update(Customer entity) throws SQLException {
        return procedureCaller.executeUpdateCustomer(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getNationalId(),
                entity.getDriversLicenseNumber()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteCustomer(id);
    }

    public int countCustomers() throws SQLException {
        return viewLoader.countViewRows("vw_customers");
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private Customer mapToCustomer(Map<String, Object> map) {
        if (map == null) return null;

        Customer customer = new Customer();
        if (map.get("id") != null) customer.setId(((Number) map.get("id")).intValue());
        if (map.get("user_id") != null) customer.setUserId(((Number) map.get("user_id")).intValue());
        if (map.get("name") != null) customer.setName(map.get("name").toString());
        if (map.get("email") != null) customer.setEmail(map.get("email").toString());
        if (map.get("address") != null) customer.setAddress(map.get("address").toString());
        if (map.get("phone") != null) customer.setPhone(map.get("phone").toString());
        if (map.get("national_id") != null) customer.setNationalId(map.get("national_id").toString());
        if (map.get("drivers_license_number") != null) customer.setDriversLicenseNumber(map.get("drivers_license_number").toString());
        if (map.get("vehicle_count") != null) customer.setVehicleCount(((Number) map.get("vehicle_count")).intValue());

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            customer.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            customer.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }
        return customer;
    }

    private List<Customer> mapToCustomerList(List<Map<String, Object>> maps) {
        List<Customer> customers = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                customers.add(mapToCustomer(map));
            }
        }
        return customers;
    }

    @Override
    protected Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setUserId(rs.getInt("user_id"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        customer.setPhone(rs.getString("phone"));
        customer.setNationalId(rs.getString("national_id"));
        customer.setDriversLicenseNumber(rs.getString("drivers_license_number"));
        customer.setVehicleCount(rs.getInt("vehicle_count"));

        if (rs.getTimestamp("created_at") != null) {
            customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            customer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return customer;
    }
}