package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.CustomerQuery;

public class QueryDAO extends BaseDAO<CustomerQuery> {

    @Override
    public CustomerQuery findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<CustomerQuery> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE customer_id = ? ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findByCustomerName(String customerName) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE customer_name ILIKE ? ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + customerName + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE vehicle_id = ? ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findByRegistrationNumber(String registrationNumber) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE registration_number = ? ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, registrationNumber);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findPendingQueries() throws SQLException {
        String sql = "SELECT * FROM vw_pending_queries ORDER BY query_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE status = ? ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerQuery> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE query_date BETWEEN ? AND ? ORDER BY query_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerQuery> queries = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, startDate);
            ps.setObject(2, endDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                queries.add(mapRow(rs));
            }
            return queries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(CustomerQuery entity) throws SQLException {
        String sql = "CALL sp_submit_query(?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_submit_query(?, ?, ?)}");
            cs.setInt(1, entity.getCustomerId());
            cs.setInt(2, entity.getVehicleId());
            cs.setString(3, entity.getQueryText());
            cs.execute();
            return true;
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public int insertAndGetId(CustomerQuery entity) throws SQLException {
        String sql = "CALL sp_submit_query(?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_submit_query(?, ?, ?)}");
            cs.setInt(1, entity.getCustomerId());
            cs.setInt(2, entity.getVehicleId());
            cs.setString(3, entity.getQueryText());
            cs.execute();

            String querySql = "SELECT id FROM customer_queries WHERE customer_id = ? ORDER BY query_date DESC LIMIT 1";
            ps = conn.prepareStatement(querySql);
            ps.setInt(1, entity.getCustomerId());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } finally {
            closeResources(rs, ps, null);
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean respondToQuery(int queryId, String responseText) throws SQLException {
        String sql = "CALL sp_respond_to_query(?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_respond_to_query(?, ?)}");
            cs.setInt(1, queryId);
            cs.setString(2, responseText);
            cs.execute();
            return true;
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean closeQuery(int queryId) throws SQLException {
        String sql = "CALL sp_close_query(?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_close_query(?)}");
            cs.setInt(1, queryId);
            cs.execute();
            return true;
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    @Override
    public boolean update(CustomerQuery entity) throws SQLException {
        String sql = "UPDATE customer_queries SET response_text = ?, response_date = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getResponseText());
            ps.setObject(2, entity.getResponseDate());
            ps.setString(3, entity.getStatus());
            ps.setInt(4, entity.getId());
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customer_queries WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public int countPendingQueries() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_queries WHERE status = 'PENDING'";
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

    public int countQueriesByCustomer(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_queries WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double getAverageResponseTimeHours() throws SQLException {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (response_date - query_date)) / 3600) FROM customer_queries WHERE response_date IS NOT NULL";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    protected CustomerQuery mapRow(ResultSet rs) throws SQLException {
        CustomerQuery query = new CustomerQuery();
        query.setId(rs.getInt("id"));
        query.setCustomerId(rs.getInt("customer_id"));
        query.setCustomerName(rs.getString("customer_name"));
        query.setVehicleId(rs.getInt("vehicle_id"));
        query.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getTimestamp("query_date") != null) {
            query.setQueryDate(rs.getTimestamp("query_date").toLocalDateTime());
        }
        query.setQueryText(rs.getString("query_text"));
        query.setResponseText(rs.getString("response_text"));

        if (rs.getTimestamp("response_date") != null) {
            query.setResponseDate(rs.getTimestamp("response_date").toLocalDateTime());
        }
        query.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            query.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            query.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return query;
    }
}