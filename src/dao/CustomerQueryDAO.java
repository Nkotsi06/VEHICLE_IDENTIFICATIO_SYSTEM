package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.CustomerQuery;

public class CustomerQueryDAO extends BaseDAO<CustomerQuery> {

    @Override
    public CustomerQuery findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<CustomerQuery> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries ORDER BY query_date DESC";
        return executeQuery(sql);
    }

    public List<CustomerQuery> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE customer_id = ? ORDER BY query_date DESC";
        return executeQuery(sql, customerId);
    }

    public List<CustomerQuery> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE vehicle_id = ? ORDER BY query_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<CustomerQuery> findPendingQueries() throws SQLException {
        String sql = "SELECT * FROM vw_pending_queries ORDER BY query_date";
        return executeQuery(sql);
    }

    public List<CustomerQuery> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM vw_customer_queries WHERE status = ? ORDER BY query_date DESC";
        return executeQuery(sql, status);
    }

    public int countPendingByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_queries WHERE customer_id = ? AND status = 'PENDING'";
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

    @Override
    public boolean insert(CustomerQuery entity) throws SQLException {
        return executeProcedure("sp_submit_query",
                entity.getCustomerId(),
                entity.getVehicleId(),
                entity.getQueryText()
        );
    }

    public boolean respondToQuery(int queryId, String responseText) throws SQLException {
        return executeProcedure("sp_respond_to_query", queryId, responseText);
    }

    public boolean closeQuery(int queryId) throws SQLException {
        return executeProcedure("sp_close_query", queryId);
    }

    @Override
    public boolean update(CustomerQuery entity) throws SQLException {
        String sql = "UPDATE customer_queries SET response_text = ?, response_date = ?, status = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getResponseText(), entity.getResponseDate(), entity.getStatus(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customer_queries WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
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