package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.CustomerQuery;

/**
 * QueryDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class QueryDAO extends BaseDAO<CustomerQuery> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public QueryDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public CustomerQuery findById(int id) throws SQLException {
        List<CustomerQuery> results = viewLoader.loadViewWithCondition("vw_customer_queries", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<CustomerQuery> findAll() throws SQLException {
        return viewLoader.loadView("vw_customer_queries");
    }

    public List<CustomerQuery> findByCustomerId(int customerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_queries", "customer_id = ? ORDER BY query_date DESC", customerId);
    }

    public List<CustomerQuery> findByCustomerName(String customerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_queries", "customer_name ILIKE ? ORDER BY query_date DESC", "%" + customerName + "%");
    }

    public List<CustomerQuery> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_queries", "vehicle_id = ? ORDER BY query_date DESC", vehicleId);
    }

    public List<CustomerQuery> findByRegistrationNumber(String registrationNumber) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_queries", "registration_number = ? ORDER BY query_date DESC", registrationNumber);
    }

    public List<CustomerQuery> findPendingQueries() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_pending_queries", "1=1 ORDER BY query_date");
    }

    public List<CustomerQuery> findByStatus(String status) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_queries", "status = ? ORDER BY query_date DESC", status);
    }

    public List<CustomerQuery> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_queries", "query_date BETWEEN ? AND ? ORDER BY query_date DESC", startDate, endDate);
    }

    @Override
    public boolean insert(CustomerQuery entity) throws SQLException {
        Integer queryId = procedureCaller.executeSubmitQuery(
                entity.getCustomerId(),
                entity.getVehicleId(),
                entity.getQueryText()
        );
        if (queryId != null && queryId > 0) {
            entity.setId(queryId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(CustomerQuery entity) throws SQLException {
        return procedureCaller.executeSubmitQuery(
                entity.getCustomerId(),
                entity.getVehicleId(),
                entity.getQueryText()
        );
    }

    public boolean respondToQuery(int queryId, String responseText) throws SQLException {
        return procedureCaller.executeRespondToQuery(queryId, responseText);
    }

    public boolean closeQuery(int queryId) throws SQLException {
        return procedureCaller.executeCloseQuery(queryId);
    }

    @Override
    public boolean update(CustomerQuery entity) throws SQLException {
        return respondToQuery(entity.getId(), entity.getResponseText());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteQuery(id);
    }

    public int countPendingQueries() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_customer_queries", "status = 'PENDING'");
    }

    public int countQueriesByCustomer(int customerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_customer_queries", "customer_id = ?", customerId);
    }

    public double getAverageResponseTimeHours() throws SQLException {
        return viewLoader.getAverageQueryResponseTimeHours();
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