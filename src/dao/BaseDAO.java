package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;  // ADD THIS IMPORT

import database.DatabaseConnection;
import database.ProcedureCaller;
import database.ViewLoader;

/**
 * BaseDAO - Provides common functionality for all DAO classes.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public abstract class BaseDAO<T> {

    protected DatabaseConnection dbConnection;
    protected ProcedureCaller procedureCaller;
    protected ViewLoader viewLoader;

    public BaseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    protected Connection getConnection() throws SQLException {
        return dbConnection.getConnection();
    }

    protected void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        dbConnection.closeResources(rs, stmt, conn);
    }

    protected void closeResources(Statement stmt, Connection conn) {
        closeResources(null, stmt, conn);
    }

    // ============================================
    // AUDIT LOGGING METHODS
    // ============================================

    /**
     * Log an audit action using stored procedure.
     *
     * @param userId    The ID of the user performing the action
     * @param action    The action being performed
     * @param ipAddress The IP address of the user
     */
    protected void logAudit(int userId, String action, String ipAddress) {
        try {
            procedureCaller.executeLogAuditAction(userId, action, ipAddress);
        } catch (SQLException e) {
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }

    /**
     * Log an audit action with default IP address.
     *
     * @param userId The ID of the user performing the action
     * @param action The action being performed
     */
    protected void logAudit(int userId, String action) {
        logAudit(userId, action, "127.0.0.1");
    }

    /**
     * Get current user ID from session.
     */
    protected int getCurrentUserId() {
        try {
            Integer userId = utils.SessionManager.getInstance().getUserId();
            return userId != null ? userId : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ============================================
    // VIEW OPERATIONS (No direct SQL)
    // ============================================

    protected List<Map<String, Object>> loadView(String viewName) throws SQLException {
        return viewLoader.loadView(viewName);
    }

    protected List<Map<String, Object>> loadViewWithCondition(String viewName, String condition, Object... params)
            throws SQLException {
        return viewLoader.loadViewWithCondition(viewName, condition, params);
    }

    protected Map<String, Object> loadViewSingle(String viewName, String condition, Object... params)
            throws SQLException {
        return viewLoader.loadViewSingle(viewName, condition, params);
    }

    protected int countViewRows(String viewName) throws SQLException {
        return viewLoader.countViewRows(viewName);
    }

    protected int countViewRowsWithCondition(String viewName, String condition, Object... params) throws SQLException {
        return viewLoader.countViewRowsWithCondition(viewName, condition, params);
    }

    // ============================================
    // PROCEDURE CALL METHODS (No direct SQL)
    // ============================================

    protected boolean executeProcedure(String procedureName, Object... params) throws SQLException {
        return procedureCaller.executeProcedure(procedureName, params);
    }

    protected <R> R executeProcedureWithOutParameter(String procedureName, int outParamType, Object... params)
            throws SQLException {
        return procedureCaller.executeProcedureWithOutParameter(procedureName, outParamType, params);
    }

    protected Integer executeProcedureWithIntegerOut(String procedureName, Object... params) throws SQLException {
        return procedureCaller.executeProcedureWithIntegerOut(procedureName, params);
    }

    protected String executeProcedureWithStringOut(String procedureName, Object... params) throws SQLException {
        return procedureCaller.executeProcedureWithStringOut(procedureName, params);
    }

    protected boolean executeProcedureInTransaction(String procedureName, Object... params) throws SQLException {
        return procedureCaller.executeProcedureInTransaction(procedureName, params);
    }

    // ============================================
    // COMPATIBILITY METHODS (Deprecated - will be removed)
    // ============================================

    /**
     * @deprecated Use viewLoader or procedureCaller instead
     */
    @Deprecated
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        throw new UnsupportedOperationException("Direct SQL is not allowed. Use stored procedures or views.");
    }

    /**
     * @deprecated Use viewLoader or procedureCaller instead
     */
    @Deprecated
    protected int executeUpdateWithGeneratedKeys(String sql, Object... params) throws SQLException {
        throw new UnsupportedOperationException("Direct SQL is not allowed. Use stored procedures or views.");
    }

    /**
     * @deprecated Use viewLoader or procedureCaller instead
     */
    @Deprecated
    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        throw new UnsupportedOperationException("Direct SQL is not allowed. Use stored procedures or views.");
    }

    /**
     * @deprecated Use viewLoader or procedureCaller instead
     */
    @Deprecated
    protected T executeQuerySingle(String sql, Object... params) throws SQLException {
        throw new UnsupportedOperationException("Direct SQL is not allowed. Use stored procedures or views.");
    }

    // ============================================
    // ABSTRACT METHODS
    // ============================================

    protected abstract T mapRow(ResultSet rs) throws SQLException;
    public abstract T findById(int id) throws SQLException;
    public abstract List<T> findAll() throws SQLException;
    public abstract boolean insert(T entity) throws SQLException;
    public abstract boolean update(T entity) throws SQLException;
    public abstract boolean delete(int id) throws SQLException;
}