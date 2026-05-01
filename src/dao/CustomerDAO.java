package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.Customer;

public class CustomerDAO extends BaseDAO<Customer> {

    @Override
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public Customer findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE user_id = ?";
        return executeQuerySingle(sql, userId);
    }

    public Customer findByNationalId(String nationalId) throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE national_id = ?";
        return executeQuerySingle(sql, nationalId);
    }

    public Customer findByDriversLicense(String driversLicense) throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE drivers_license_number = ?";
        return executeQuerySingle(sql, driversLicense);
    }

    public Customer findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE phone = ?";
        return executeQuerySingle(sql, phone);
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customers ORDER BY name";
        return executeQuery(sql);
    }

    public List<Customer> findCustomersWithVehicles() throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE vehicle_count > 0 ORDER BY name";
        return executeQuery(sql);
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_customers WHERE name ILIKE ? OR email ILIKE ? OR phone ILIKE ? ORDER BY name";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern, searchPattern, searchPattern);
    }

    @Override
    public boolean insert(Customer entity) throws SQLException {
        String sql = "CALL sp_create_customer(?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getUserId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getNationalId(),
                entity.getDriversLicenseNumber()
        );
        return result >= 0;
    }

    public int insertAndGetId(Customer entity) throws SQLException {
        return executeProcedureWithOutParameter("sp_create_customer", java.sql.Types.INTEGER,
                entity.getUserId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getNationalId(),
                entity.getDriversLicenseNumber()
        );
    }

    @Override
    public boolean update(Customer entity) throws SQLException {
        String sql = "CALL sp_update_customer(?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getNationalId(),
                entity.getDriversLicenseNumber()
        );
        return result >= 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "CALL sp_delete_customer(?)";
        int result = executeUpdate(sql, id);
        return result >= 0;
    }

    public int countCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
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